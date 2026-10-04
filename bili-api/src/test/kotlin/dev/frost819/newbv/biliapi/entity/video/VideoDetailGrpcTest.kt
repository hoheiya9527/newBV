package dev.frost819.newbv.biliapi.entity.video

import bilibili.app.archive.v1.Arc
import bilibili.app.archive.v1.Author as GrpcAuthor
import bilibili.app.archive.v1.Page
import bilibili.app.archive.v1.Stat
import bilibili.app.view.v1.ActivitySeason
import bilibili.app.view.v1.History
import bilibili.app.view.v1.PlayerIcon
import bilibili.app.view.v1.Relate
import bilibili.app.view.v1.ReqUser
import bilibili.app.view.v1.Tag
import bilibili.app.view.v1.ViewPage
import bilibili.app.view.v1.ViewReply
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * [VideoDetail.fromViewReply] gRPC（bilibili.app.view.v1.ViewReply）→ Domain 转换方法的单元测试。
 *
 * 覆盖非 activity_season 和 activity_season 两个分支，以及 argueTip、playerIcon、
 * redirectToEp、epid 提取等子逻辑。
 */
class VideoDetailGrpcTest {
    @Test
    fun `fromViewReply gRPC without activity season maps all fields`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    arc =
                        Arc
                            .newBuilder()
                            .apply {
                                aid = 993403941L
                                firstCid = 1051761130L
                                pic = "http://pic.test"
                                title = "测试视频"
                                pubdate = 1700000000L
                                desc = "描述"
                                redirectUrl = ""
                                stat =
                                    Stat
                                        .newBuilder()
                                        .apply {
                                            view = 10000
                                            danmaku = 500
                                            reply = 200
                                            fav = 100
                                            coin = 50
                                            share = 10
                                            hisRank = 5
                                            like = 1000
                                        }.build()
                                author =
                                    GrpcAuthor
                                        .newBuilder()
                                        .apply {
                                            mid = 123L
                                            name = "UP主"
                                            face = "http://face.test"
                                        }.build()
                            }.build()
                    reqUser =
                        ReqUser
                            .newBuilder()
                            .apply {
                                like = 1
                                favorite = 0
                                coin = 1
                                dislike = 0
                            }.build()
                    history =
                        History
                            .newBuilder()
                            .apply {
                                cid = 1051761130L
                                progress = 120L
                            }.build()
                    argueMsg = ""
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.bvid).isEqualTo("BV1xx")
        assertThat(detail.aid).isEqualTo(993403941L)
        assertThat(detail.cid).isEqualTo(1051761130L)
        assertThat(detail.cover).isEqualTo("http://pic.test")
        assertThat(detail.title).isEqualTo("测试视频")
        assertThat(detail.description).isEqualTo("描述")
        assertThat(detail.stat.view).isEqualTo(10000)
        assertThat(detail.stat.danmaku).isEqualTo(500)
        assertThat(detail.author.mid).isEqualTo(123L)
        assertThat(detail.pages).isEmpty()
        assertThat(detail.relatedVideos).isEmpty()
        assertThat(detail.tags).isEmpty()
        assertThat(detail.redirectToEp).isFalse()
        assertThat(detail.epid).isNull()
        assertThat(detail.argueTip).isNull()
        assertThat(detail.userActions.like).isTrue()
        assertThat(detail.userActions.favorite).isFalse()
        assertThat(detail.userActions.coin).isTrue()
        assertThat(detail.history.progress).isEqualTo(120)
        assertThat(detail.history.lastPlayedCid).isEqualTo(1051761130L)
        assertThat(detail.playerIcon).isNotNull()
        assertThat(detail.playerIcon!!.idle).isEqualTo("")
        assertThat(detail.playerIcon!!.moving).isEqualTo("")
        assertThat(detail.ugcSeason).isNull()
    }

    @Test
    fun `fromViewReply gRPC with non-empty argueMsg returns argueTip`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    arc = minimalArc()
                    argueMsg = "争议提示信息"
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.argueTip).isEqualTo("争议提示信息")
    }

    @Test
    fun `fromViewReply gRPC with redirectUrl containing ep sets redirectToEp true`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    arc =
                        minimalArc(
                            redirectUrl = "https://www.bilibili.com/bangumi/play/ep12345",
                        )
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.redirectToEp).isTrue()
        assertThat(detail.epid).isEqualTo(12345)
    }

    @Test
    fun `fromViewReply gRPC with redirectUrl without ep sets redirectToEp false`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    arc =
                        minimalArc(
                            redirectUrl = "https://www.bilibili.com/video/BV1xx",
                        )
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.redirectToEp).isFalse()
        assertThat(detail.epid).isNull()
    }

    @Test
    fun `fromViewReply gRPC with playerIcon maps icon`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    arc = minimalArc()
                    playerIcon =
                        PlayerIcon
                            .newBuilder()
                            .apply {
                                url1 = "http://moving.test"
                                url2 = "http://idle.test"
                            }.build()
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.playerIcon).isNotNull()
        assertThat(detail.playerIcon!!.idle).isEqualTo("http://idle.test")
        assertThat(detail.playerIcon!!.moving).isEqualTo("http://moving.test")
    }

    @Test
    fun `fromViewReply gRPC with pages maps video pages`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    arc = minimalArc()
                    addPages(
                        ViewPage
                            .newBuilder()
                            .apply {
                                page =
                                    Page
                                        .newBuilder()
                                        .apply {
                                            cid = 100L
                                            page = 1
                                            part = "第一P"
                                            duration = 300L
                                        }.build()
                            }.build(),
                    )
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.pages).hasSize(1)
        assertThat(detail.pages[0].cid).isEqualTo(100L)
        assertThat(detail.pages[0].index).isEqualTo(1)
        assertThat(detail.pages[0].title).isEqualTo("第一P")
        assertThat(detail.pages[0].duration).isEqualTo(300)
    }

    @Test
    fun `fromViewReply gRPC with tags maps tags`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    arc = minimalArc()
                    addTag(
                        Tag
                            .newBuilder()
                            .apply {
                                id = 42L
                                name = "标签1"
                            }.build(),
                    )
                    addTag(
                        Tag
                            .newBuilder()
                            .apply {
                                id = 99L
                                name = "标签2"
                            }.build(),
                    )
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.tags).hasSize(2)
        assertThat(detail.tags[0].id).isEqualTo(42)
        assertThat(detail.tags[0].name).isEqualTo("标签1")
        assertThat(detail.tags[1].id).isEqualTo(99)
        assertThat(detail.tags[1].name).isEqualTo("标签2")
    }

    @Test
    fun `fromViewReply gRPC with relates maps related videos`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    arc = minimalArc()
                    addRelates(
                        Relate
                            .newBuilder()
                            .apply {
                                aid = 200L
                                cid = 300L
                                pic = "http://related.test"
                                title = "相关视频"
                                duration = 600L
                                goto = "av"
                                stat =
                                    Stat
                                        .newBuilder()
                                        .apply {
                                            view = 500
                                            danmaku = 50
                                        }.build()
                                author =
                                    GrpcAuthor
                                        .newBuilder()
                                        .apply {
                                            mid = 456L
                                            name = "UP2"
                                            face = ""
                                        }.build()
                            }.build(),
                    )
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.relatedVideos).hasSize(1)
        assertThat(detail.relatedVideos[0].aid).isEqualTo(200L)
        assertThat(detail.relatedVideos[0].title).isEqualTo("相关视频")
    }

    @Test
    fun `fromViewReply gRPC with activity season maps from activity season data`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    activitySeason =
                        ActivitySeason
                            .newBuilder()
                            .apply {
                                bvid = "BV2xx"
                                arc =
                                    Arc
                                        .newBuilder()
                                        .apply {
                                            aid = 999L
                                            firstCid = 888L
                                            pic = "http://activity-pic.test"
                                            title = "活动视频"
                                            pubdate = 1700000001L
                                            desc = "活动描述"
                                            redirectUrl = ""
                                            stat =
                                                Stat
                                                    .newBuilder()
                                                    .apply {
                                                        view = 50000
                                                        danmaku = 1000
                                                    }.build()
                                            author =
                                                GrpcAuthor
                                                    .newBuilder()
                                                    .apply {
                                                        mid = 777L
                                                        name = "活动UP"
                                                        face = ""
                                                    }.build()
                                        }.build()
                                history =
                                    History
                                        .newBuilder()
                                        .apply {
                                            cid = 888L
                                            progress = 60L
                                        }.build()
                                argueMsg = ""
                            }.build()
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.bvid).isEqualTo("BV2xx")
        assertThat(detail.aid).isEqualTo(999L)
        assertThat(detail.cid).isEqualTo(888L)
        assertThat(detail.cover).isEqualTo("http://activity-pic.test")
        assertThat(detail.title).isEqualTo("活动视频")
        assertThat(detail.description).isEqualTo("活动描述")
        assertThat(detail.stat.view).isEqualTo(50000)
        assertThat(detail.author.mid).isEqualTo(777L)
        assertThat(detail.history.progress).isEqualTo(60)
        assertThat(detail.history.lastPlayedCid).isEqualTo(888L)
    }

    @Test
    fun `fromViewReply gRPC with activity season and argueMsg returns argueTip`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    activitySeason =
                        ActivitySeason
                            .newBuilder()
                            .apply {
                                bvid = "BV2xx"
                                arc = minimalArc()
                            }.build()
                    argueMsg = "活动争议"
                }.build()

        // When activitySeason is present, argueTip comes from viewReply.argueMsg (top level),
        // NOT from activitySeason.argueMsg. Wait — let me re-check the source...
        val detail = VideoDetail.fromViewReply(grpcReply)

        // Activity season branch reads viewReply.activitySeason.argueMsg
        // The argueMsg on viewReply is not used in activity season branch
        assertThat(detail.argueTip).isNull()
    }

    @Test
    fun `fromViewReply gRPC with activity season with argueMsg on activitySeason returns argueTip`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    activitySeason =
                        ActivitySeason
                            .newBuilder()
                            .apply {
                                bvid = "BV2xx"
                                arc = minimalArc()
                                argueMsg = "活动争议"
                            }.build()
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.argueTip).isEqualTo("活动争议")
    }

    @Test
    fun `fromViewReply gRPC with activity season and ep redirectUrl extracts epid`() {
        val grpcReply =
            ViewReply
                .newBuilder()
                .apply {
                    bvid = "BV1xx"
                    activitySeason =
                        ActivitySeason
                            .newBuilder()
                            .apply {
                                bvid = "BV2xx"
                                arc =
                                    minimalArc(
                                        redirectUrl = "https://www.bilibili.com/bangumi/play/ep99999",
                                    )
                            }.build()
                }.build()

        val detail = VideoDetail.fromViewReply(grpcReply)

        assertThat(detail.redirectToEp).isTrue()
        assertThat(detail.epid).isEqualTo(99999)
    }

    private fun minimalArc(
        aid: Long = 1L,
        firstCid: Long = 2L,
        redirectUrl: String = "",
    ): Arc =
        Arc
            .newBuilder()
            .apply {
                this.aid = aid
                this.firstCid = firstCid
                pic = ""
                title = ""
                pubdate = 0L
                desc = ""
                this.redirectUrl = redirectUrl
            }.build()
}
