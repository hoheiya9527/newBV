package dev.frost819.newbv.biliapi.repositories

import bilibili.pagination.PaginationReply
import bilibili.polymer.app.search.v1.Item
import bilibili.polymer.app.search.v1.SearchAllResponse
import bilibili.polymer.app.search.v1.SearchBangumiCard
import bilibili.polymer.app.search.v1.SearchUpperCard
import bilibili.polymer.app.search.v1.SearchVideoCard
import bilibili.polymer.app.search.v1.Share
import bilibili.polymer.app.search.v1.Video as SearchShareVideo
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * [SearchAllResult.fromGrpc] 的单元测试。
 *
 * 验证 gRPC SearchAllResponse 中 AV / BANGUMI / AUTHOR / 未知卡片类型的分类转换逻辑，
 * 以及 pagination.next 的页码解析与 hasMore 计算。
 */
class SearchAllResultGrpcTest {
    @Test
    fun `fromGrpc maps video bangumi and author items`() {
        val reply =
            SearchAllResponse
                .newBuilder()
                .apply {
                    keyword = "测试"
                    addItem(
                        Item
                            .newBuilder()
                            .apply {
                                param = "100"
                                av =
                                    SearchVideoCard
                                        .newBuilder()
                                        .apply {
                                            title = "视频1"
                                            cover = "http://cover1.test"
                                            author = "UP1"
                                            mid = 1L
                                            duration = "10:00"
                                            play = 1000
                                            danmaku = 50
                                            share =
                                                Share
                                                    .newBuilder()
                                                    .apply {
                                                        video =
                                                            SearchShareVideo
                                                                .newBuilder()
                                                                .apply {
                                                                    bvid = "BV100"
                                                                }.build()
                                                    }.build()
                                        }.build()
                            }.build(),
                    )
                    addItem(
                        Item
                            .newBuilder()
                            .apply {
                                bangumi =
                                    SearchBangumiCard
                                        .newBuilder()
                                        .apply {
                                            title = "番剧1"
                                            cover = "http://cover2.test"
                                            rating = 9.0
                                            seasonId = 20000L
                                        }.build()
                            }.build(),
                    )
                    addItem(
                        Item
                            .newBuilder()
                            .apply {
                                param = "999"
                                author =
                                    SearchUpperCard
                                        .newBuilder()
                                        .apply {
                                            title = "用户1"
                                            cover = "http://avatar.test"
                                            sign = "签名"
                                        }.build()
                            }.build(),
                    )
                    pagination =
                        PaginationReply
                            .newBuilder()
                            .apply {
                                next = "2"
                            }.build()
                }.build()

        val result = SearchAllResult.fromGrpc(reply)

        assertThat(result.keyword).isEqualTo("测试")
        assertThat(result.videos).hasSize(1)
        assertThat(result.videos[0].aid).isEqualTo(100L)
        assertThat(result.videos[0].title).isEqualTo("视频1")
        assertThat(result.pgcs).hasSize(1)
        assertThat(result.pgcs[0].title).isEqualTo("番剧1")
        assertThat(result.users).hasSize(1)
        assertThat(result.users[0].mid).isEqualTo(999L)
        assertThat(result.page).isEqualTo(2)
        assertThat(result.hasMore).isTrue()
    }

    @Test
    fun `fromGrpc with empty items returns empty lists`() {
        val reply =
            SearchAllResponse
                .newBuilder()
                .apply {
                    keyword = ""
                }.build()

        val result = SearchAllResult.fromGrpc(reply)

        assertThat(result.videos).isEmpty()
        assertThat(result.pgcs).isEmpty()
        assertThat(result.users).isEmpty()
        assertThat(result.page).isEqualTo(1)
        assertThat(result.hasMore).isFalse()
    }

    @Test
    fun `fromGrpc with non-numeric pagination next returns page 1 and hasMore false`() {
        val reply =
            SearchAllResponse
                .newBuilder()
                .apply {
                    keyword = "test"
                    pagination =
                        PaginationReply
                            .newBuilder()
                            .apply {
                                next = "abc"
                            }.build()
                }.build()

        val result = SearchAllResult.fromGrpc(reply)

        assertThat(result.page).isEqualTo(1)
        assertThat(result.hasMore).isFalse()
    }

    @Test
    fun `fromGrpc with zero pagination next returns page 1 and hasMore false`() {
        val reply =
            SearchAllResponse
                .newBuilder()
                .apply {
                    keyword = "test"
                    pagination =
                        PaginationReply
                            .newBuilder()
                            .apply {
                                next = "0"
                            }.build()
                }.build()

        val result = SearchAllResult.fromGrpc(reply)

        assertThat(result.page).isEqualTo(1)
        assertThat(result.hasMore).isFalse()
    }

    @Test
    fun `fromGrpc with null pagination returns defaults`() {
        val reply =
            SearchAllResponse
                .newBuilder()
                .apply {
                    keyword = "test"
                }.build()

        val result = SearchAllResult.fromGrpc(reply)

        assertThat(result.page).isEqualTo(1)
        assertThat(result.hasMore).isFalse()
    }

    @Test
    fun `fromGrpc ignores unknown card types`() {
        val reply =
            SearchAllResponse
                .newBuilder()
                .apply {
                    keyword = "test"
                    addItem(Item.getDefaultInstance())
                }.build()

        val result = SearchAllResult.fromGrpc(reply)

        assertThat(result.videos).isEmpty()
        assertThat(result.pgcs).isEmpty()
        assertThat(result.users).isEmpty()
    }

    @Test
    fun `fromGrpc maps multiple video items`() {
        val reply =
            SearchAllResponse
                .newBuilder()
                .apply {
                    keyword = "多视频"
                    addItem(
                        Item
                            .newBuilder()
                            .apply {
                                param = "100"
                                av =
                                    SearchVideoCard
                                        .newBuilder()
                                        .apply {
                                            title = "视频A"
                                            cover = ""
                                            author = "UP_A"
                                            mid = 1L
                                            duration = "5:00"
                                            play = 100
                                            danmaku = 10
                                            share =
                                                Share
                                                    .newBuilder()
                                                    .apply {
                                                        video =
                                                            SearchShareVideo
                                                                .newBuilder()
                                                                .apply {
                                                                    bvid = "BVA"
                                                                }.build()
                                                    }.build()
                                        }.build()
                            }.build(),
                    )
                    addItem(
                        Item
                            .newBuilder()
                            .apply {
                                param = "200"
                                av =
                                    SearchVideoCard
                                        .newBuilder()
                                        .apply {
                                            title = "视频B"
                                            cover = ""
                                            author = "UP_B"
                                            mid = 2L
                                            duration = "10:00"
                                            play = 200
                                            danmaku = 20
                                            share =
                                                Share
                                                    .newBuilder()
                                                    .apply {
                                                        video =
                                                            SearchShareVideo
                                                                .newBuilder()
                                                                .apply {
                                                                    bvid = "BVB"
                                                                }.build()
                                                    }.build()
                                        }.build()
                            }.build(),
                    )
                    pagination =
                        PaginationReply
                            .newBuilder()
                            .apply {
                                next = "3"
                            }.build()
                }.build()

        val result = SearchAllResult.fromGrpc(reply)

        assertThat(result.videos).hasSize(2)
        assertThat(result.videos[0].title).isEqualTo("视频A")
        assertThat(result.videos[1].title).isEqualTo("视频B")
        assertThat(result.page).isEqualTo(3)
        assertThat(result.hasMore).isTrue()
    }
}
