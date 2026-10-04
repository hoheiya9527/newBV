package dev.frost819.newbv.biliapi.repositories

import bilibili.app.interfaces.v1.Cursor
import bilibili.app.interfaces.v1.CursorV2Req
import bilibili.app.interfaces.v1.HistoryGrpcKt
import dev.frost819.newbv.biliapi.entity.ApiType
import dev.frost819.newbv.biliapi.entity.user.HistoryData
import dev.frost819.newbv.biliapi.http.BiliHttpApi

class HistoryRepository(
    private val authRepository: AuthRepository,
    private val channelRepository: ChannelRepository,
) {
    private val historyStub
        get() =
            runCatching {
                HistoryGrpcKt.HistoryCoroutineStub(channelRepository.requireDefaultChannel())
            }.getOrNull()

    suspend fun getHistories(
        cursor: Long,
        preferApiType: ApiType,
    ): HistoryData =
        when (preferApiType) {
            ApiType.Web -> {
                val data =
                    BiliHttpApi
                        .getHistories(
                            viewAt = cursor,
                        ).getResponseData()
                HistoryData.fromHistoryResponse(data)
            }

            ApiType.App -> {
                val reply =
                    historyStub?.cursorV2(
                        CursorV2Req
                            .newBuilder()
                            .apply {
                                this.cursor = Cursor.newBuilder().setMax(cursor).build()
                                business = "archive"
                            }.build(),
                    )
                HistoryData.fromHistoryResponse(reply!!)
            }
        }
}
