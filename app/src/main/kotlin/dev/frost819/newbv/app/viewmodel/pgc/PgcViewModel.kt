package dev.frost819.newbv.app.viewmodel.pgc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.frost819.newbv.biliapi.entity.CarouselData
import dev.frost819.newbv.biliapi.entity.pgc.PgcFeedData
import dev.frost819.newbv.biliapi.entity.pgc.PgcItem
import dev.frost819.newbv.biliapi.entity.pgc.PgcType
import dev.frost819.newbv.biliapi.repositories.PgcRepository
import dev.frost819.newbv.core.log.Loggers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

private const val LOAD_TIMEOUT_MS = 10_000L

/**
 * PGC 影视分区 UI 状态。
 *
 * @property carouselItems 轮播图数据。
 * @property items 当前分区的番剧/影视列表。
 * @property loading 是否正在加载 Feed 数据。
 * @property carouselLoading 是否正在加载轮播图。
 * @property hasMore 是否还有更多数据。
 * @property error Feed 加载是否失败。
 */
data class PgcUiState(
    val type: PgcType = PgcType.Anime,
    val carouselItems: List<CarouselData.CarouselItem> = emptyList(),
    val items: List<PgcItem> = emptyList(),
    val loading: Boolean = false,
    val carouselLoading: Boolean = false,
    val hasMore: Boolean = true,
    val error: Boolean = false,
)

/**
 * PGC 影视分区 ViewModel。
 *
 * 管理番剧/国创/电影/纪录片/电视剧/综艺的轮播图和推荐 Feed 数据加载。
 * 轮播图从 PGC 页面 HTML 解析，Feed 使用 cursor 分页。
 * 切换 Tab 时清空重新加载轮播图和 Feed。
 *
 * @param pgcRepository PGC 数据仓库。
 */
@HiltViewModel
class PgcViewModel
    @Inject
    constructor(
        private val pgcRepository: PgcRepository,
    ) : ViewModel() {
        private val logger = Loggers.get("PgcViewModel")

        private val _uiState = MutableStateFlow(PgcUiState())
        val uiState: StateFlow<PgcUiState> = _uiState.asStateFlow()

        /** 当前选中的分区。 */
        private var currentType: PgcType = PgcType.Anime

        /** 当前分区的分页游标。 */
        private var cursor: Int = 0

        /**
         * 切换分区或刷新时递增。进行中的请求回来后若代数已变，丢弃结果，
         * 避免旧 Feed 把新分区的 cursor / hasMore 写成空页。
         */
        private var requestGeneration: Int = 0

        private var carouselJob: Job? = null
        private var feedJob: Job? = null

        /** 连续空页次数。有的分区第一页只有横幅模块，需要接着翻页才能拿到片名。 */
        private var emptyPageSkips: Int = 0

        init {
            loadCarousel()
            loadMore()
        }

        /**
         * 切换到指定分区。
         *
         * @param type 目标分区。
         */
        fun switchType(type: PgcType) {
            if (type == currentType && _uiState.value.items.isNotEmpty()) return
            currentType = type
            cursor = 0
            emptyPageSkips = 0
            requestGeneration += 1
            carouselJob?.cancel()
            feedJob?.cancel()
            _uiState.value = PgcUiState(type = type)
            loadCarousel()
            loadMore()
        }

        /**
         * 加载轮播图数据。
         *
         * 失败时静默处理（轮播图为辅助展示，不阻塞 Feed）。
         */
        fun loadCarousel() {
            val generation = requestGeneration
            val type = currentType
            carouselJob =
                viewModelScope.launch {
                    _uiState.update { state ->
                        if (generation != requestGeneration) state else state.copy(carouselLoading = true)
                    }

                    runCatching {
                        withTimeout(LOAD_TIMEOUT_MS) {
                            val carouselData = pgcRepository.getCarousel(type)
                            if (generation != requestGeneration) return@withTimeout
                            _uiState.update {
                                it.copy(carouselItems = carouselData.items)
                            }
                        }
                    }.onFailure { error ->
                        if (error is CancellationException && error !is TimeoutCancellationException) {
                            throw error
                        }
                        if (generation != requestGeneration) return@onFailure
                        logger.error(error) { "Failed to load PGC carousel: $type" }
                    }

                    if (generation != requestGeneration) return@launch
                    _uiState.update { it.copy(carouselLoading = false) }
                }
        }

        /**
         * 加载更多当前分区 Feed 数据。
         *
         * 超时或失败时标记 error，不中断已有数据。
         */
        fun loadMore() {
            val current = _uiState.value
            if (current.loading || !current.hasMore) return

            val generation = requestGeneration
            val type = currentType
            val requestCursor = cursor
            feedJob =
                viewModelScope.launch {
                    _uiState.update { state ->
                        if (generation != requestGeneration) state else state.copy(loading = true, error = false)
                    }

                    var continueEmptyPage = false
                    runCatching {
                        withTimeout(LOAD_TIMEOUT_MS) {
                            val data: PgcFeedData =
                                pgcRepository.getFeed(
                                    pgcType = type,
                                    cursor = requestCursor,
                                )
                            if (generation != requestGeneration) return@withTimeout
                            cursor = data.cursor
                            val playable =
                                data.items
                                    .distinctBy { it.seasonId }
                                    .filter { item -> _uiState.value.items.none { it.seasonId == item.seasonId } }
                            if (playable.isEmpty() && data.hasNext && emptyPageSkips < MAX_EMPTY_PAGE_SKIPS) {
                                emptyPageSkips += 1
                                continueEmptyPage = true
                                _uiState.update { it.copy(hasMore = true, error = false) }
                            } else if (playable.isEmpty()) {
                                // 空页次数用尽，或接口声明没有下一页。停住，避免 hasMore 仍为 true 时网格不再触发请求。
                                emptyPageSkips = 0
                                _uiState.update { it.copy(hasMore = false, error = false) }
                            } else {
                                emptyPageSkips = 0
                                _uiState.update {
                                    it.copy(
                                        items = it.items + playable,
                                        hasMore = data.hasNext,
                                    )
                                }
                            }
                        }
                    }.onFailure { error ->
                        if (error is CancellationException && error !is TimeoutCancellationException) {
                            throw error
                        }
                        if (generation != requestGeneration) return@onFailure
                        logger.error(error) { "Failed to load PGC feed: $type" }
                        _uiState.update { it.copy(error = true) }
                    }

                    if (generation != requestGeneration) return@launch
                    _uiState.update { it.copy(loading = false) }
                    if (continueEmptyPage && generation == requestGeneration) {
                        loadMore()
                    }
                }
        }

        /**
         * 刷新当前分区数据（轮播图 + Feed）。
         */
        fun refresh() {
            cursor = 0
            emptyPageSkips = 0
            requestGeneration += 1
            carouselJob?.cancel()
            feedJob?.cancel()
            _uiState.value = PgcUiState(type = currentType)
            loadCarousel()
            loadMore()
        }

        private companion object {
            const val MAX_EMPTY_PAGE_SKIPS = 3
        }
    }
