package dev.frost819.newbv.biliapi.entity.user

import bilibili.app.dynamic.v2.CardVideoDynList
import bilibili.app.dynamic.v2.DynModuleType
import bilibili.app.dynamic.v2.DynVideoReply
import bilibili.app.dynamic.v2.DynamicItem
import bilibili.app.dynamic.v2.MdlDynArchive
import bilibili.app.dynamic.v2.MdlDynPGC
import bilibili.app.dynamic.v2.Module
import bilibili.app.dynamic.v2.ModuleAuthor
import bilibili.app.dynamic.v2.ModuleDesc
import bilibili.app.dynamic.v2.ModuleDynamic
import bilibili.app.dynamic.v2.UserInfo
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

/**
 * [DynamicVideo] / [DynamicVideoData] gRPC 转换方法的单元测试。
 *
 * 覆盖 `fromDynamicVideoItem(DynamicItem)` 的 DYN_ARCHIVE / DYN_PGC / 未知类型分支，
 * 以及 `fromDynamicData(DynVideoReply)` 的字段映射。
 */
class DynamicGrpcConversionTest {
    @Test
    fun `fromDynamicVideoItem grpc archive maps all fields`() {
        val item =
            DynamicItem
                .newBuilder()
                .apply {
                    addModules(
                        Module
                            .newBuilder()
                            .apply {
                                moduleType = DynModuleType.module_author
                                moduleAuthor =
                                    ModuleAuthor
                                        .newBuilder()
                                        .apply {
                                            mid = 999L
                                            ptimeLabelText = "2024-01-01 12:00"
                                            author =
                                                UserInfo
                                                    .newBuilder()
                                                    .apply {
                                                        mid = 999L
                                                        name = "gRPC UP"
                                                    }.build()
                                        }.build()
                            }.build(),
                    )
                    addModules(
                        Module
                            .newBuilder()
                            .apply {
                                moduleType = DynModuleType.module_dynamic
                                moduleDynamic =
                                    ModuleDynamic
                                        .newBuilder()
                                        .apply {
                                            dynArchive =
                                                MdlDynArchive
                                                    .newBuilder()
                                                    .apply {
                                                        title = "gRPC视频标题"
                                                        cover = "http://cover.grpc"
                                                        avid = 200L
                                                        bvid = "BV200"
                                                        cid = 300L
                                                        coverLeftText1 = "10:30"
                                                        coverLeftText2 = "5万"
                                                        coverLeftText3 = "200"
                                                    }.build()
                                        }.build()
                            }.build(),
                    )
                }.build()

        val video = DynamicVideo.fromDynamicVideoItem(item)

        assertThat(video).isNotNull()
        video!!
        assertThat(video.aid).isEqualTo(200L)
        assertThat(video.bvid).isEqualTo("BV200")
        assertThat(video.cid).isEqualTo(300L)
        assertThat(video.title).isEqualTo("gRPC视频标题")
        assertThat(video.cover).isEqualTo("http://cover.grpc")
        assertThat(video.author).isEqualTo("gRPC UP")
        assertThat(video.authorMid).isEqualTo(999L)
        assertThat(video.duration).isEqualTo(630)
        assertThat(video.play).isEqualTo(50000)
        assertThat(video.danmaku).isEqualTo(200)
        assertThat(video.pubTime).isEqualTo("2024-01-01")
    }

    @Test
    fun `fromDynamicVideoItem grpc archive with dynamic video prefix uses desc text`() {
        val item =
            DynamicItem
                .newBuilder()
                .apply {
                    addModules(
                        Module
                            .newBuilder()
                            .apply {
                                moduleType = DynModuleType.module_author
                                moduleAuthor =
                                    ModuleAuthor
                                        .newBuilder()
                                        .apply {
                                            ptimeLabelText = "动态视频 2024-01-01"
                                            author =
                                                UserInfo
                                                    .newBuilder()
                                                    .apply {
                                                        mid = 1L
                                                        name = "UP"
                                                    }.build()
                                        }.build()
                            }.build(),
                    )
                    addModules(
                        Module
                            .newBuilder()
                            .apply {
                                moduleType = DynModuleType.module_dynamic
                                moduleDynamic =
                                    ModuleDynamic
                                        .newBuilder()
                                        .apply {
                                            dynArchive =
                                                MdlDynArchive
                                                    .newBuilder()
                                                    .apply {
                                                        title = "原标题"
                                                        cover = ""
                                                        avid = 1L
                                                        bvid = "BV1"
                                                        cid = 2L
                                                    }.build()
                                        }.build()
                            }.build(),
                    )
                    addModules(
                        Module
                            .newBuilder()
                            .apply {
                                moduleType = DynModuleType.module_desc
                                moduleDesc =
                                    ModuleDesc
                                        .newBuilder()
                                        .apply {
                                            text = "动态视频｜实际标题"
                                        }.build()
                            }.build(),
                    )
                }.build()

        val video = DynamicVideo.fromDynamicVideoItem(item)

        assertThat(video!!.title).isEqualTo("实际标题")
    }

    @Test
    fun `fromDynamicVideoItem grpc pgc maps all fields`() {
        val item =
            DynamicItem
                .newBuilder()
                .apply {
                    addModules(
                        Module
                            .newBuilder()
                            .apply {
                                moduleType = DynModuleType.module_author
                                moduleAuthor =
                                    ModuleAuthor
                                        .newBuilder()
                                        .apply {
                                            ptimeLabelText = "2024-03-15"
                                            author =
                                                UserInfo
                                                    .newBuilder()
                                                    .apply {
                                                        mid = 888L
                                                        name = "番剧UP"
                                                    }.build()
                                        }.build()
                            }.build(),
                    )
                    addModules(
                        Module
                            .newBuilder()
                            .apply {
                                moduleType = DynModuleType.module_dynamic
                                moduleDynamic =
                                    ModuleDynamic
                                        .newBuilder()
                                        .apply {
                                            dynPgc =
                                                MdlDynPGC
                                                    .newBuilder()
                                                    .apply {
                                                        title = "番剧标题"
                                                        cover = "http://cover.pgc"
                                                        cid = 400L
                                                        seasonId = 500L
                                                        epid = 600L
                                                        aid = 700L
                                                        coverLeftText1 = "24:00"
                                                        coverLeftText2 = "100万"
                                                        coverLeftText3 = "5000"
                                                    }.build()
                                        }.build()
                            }.build(),
                    )
                }.build()

        val video = DynamicVideo.fromDynamicVideoItem(item)

        assertThat(video).isNotNull()
        video!!
        assertThat(video.aid).isEqualTo(700L)
        assertThat(video.bvid).isNull()
        assertThat(video.cid).isEqualTo(400L)
        assertThat(video.epid).isEqualTo(600)
        assertThat(video.seasonId).isEqualTo(500)
        assertThat(video.title).isEqualTo("番剧标题")
        assertThat(video.cover).isEqualTo("http://cover.pgc")
        assertThat(video.author).isEqualTo("番剧UP")
        assertThat(video.authorMid).isEqualTo(888L)
        assertThat(video.duration).isEqualTo(1440)
        assertThat(video.play).isEqualTo(1000000)
        assertThat(video.danmaku).isEqualTo(5000)
    }

    @Test
    fun `fromDynamicVideoItem grpc unknown module type returns null`() {
        val item =
            DynamicItem
                .newBuilder()
                .apply {
                    addModules(
                        Module
                            .newBuilder()
                            .apply {
                                moduleType = DynModuleType.module_author
                                moduleAuthor =
                                    ModuleAuthor
                                        .newBuilder()
                                        .apply {
                                            ptimeLabelText = "2024-01-01"
                                            author =
                                                UserInfo
                                                    .newBuilder()
                                                    .apply {
                                                        mid = 1L
                                                        name = "UP"
                                                    }.build()
                                        }.build()
                            }.build(),
                    )
                    addModules(
                        Module
                            .newBuilder()
                            .apply {
                                moduleType = DynModuleType.module_dynamic
                                moduleDynamic = ModuleDynamic.getDefaultInstance()
                            }.build(),
                    )
                }.build()

        val video = DynamicVideo.fromDynamicVideoItem(item)

        assertThat(video).isNull()
    }

    @Test
    fun `fromDynamicData grpc maps list and pagination`() {
        val reply =
            DynVideoReply
                .newBuilder()
                .apply {
                    dynamicList =
                        CardVideoDynList
                            .newBuilder()
                            .apply {
                                addList(fakeArchiveDynamicItem(aid = 100L, title = "视频1"))
                                addList(fakeArchiveDynamicItem(aid = 200L, title = "视频2"))
                                hasMore = true
                                historyOffset = "offset-789"
                                updateBaseline = "baseline-012"
                            }.build()
                }.build()

        val result = DynamicVideoData.fromDynamicData(reply)

        assertThat(result.videos).hasSize(2)
        assertThat(result.videos[0].aid).isEqualTo(100L)
        assertThat(result.videos[1].aid).isEqualTo(200L)
        assertThat(result.hasMore).isTrue()
        assertThat(result.historyOffset).isEqualTo("offset-789")
        assertThat(result.updateBaseline).isEqualTo("baseline-012")
    }

    @Test
    fun `fromDynamicData grpc with empty list returns empty videos`() {
        val reply =
            DynVideoReply
                .newBuilder()
                .apply {
                    dynamicList =
                        CardVideoDynList
                            .newBuilder()
                            .apply {
                                hasMore = false
                            }.build()
                }.build()

        val result = DynamicVideoData.fromDynamicData(reply)

        assertThat(result.videos).isEmpty()
        assertThat(result.hasMore).isFalse()
    }

    @Test
    fun `fromDynamicData grpc filters out null items`() {
        val reply =
            DynVideoReply
                .newBuilder()
                .apply {
                    dynamicList =
                        CardVideoDynList
                            .newBuilder()
                            .apply {
                                addList(fakeArchiveDynamicItem(aid = 100L, title = "有效视频"))
                                addList(fakeUnknownDynamicItem())
                                hasMore = true
                            }.build()
                }.build()

        val result = DynamicVideoData.fromDynamicData(reply)

        assertThat(result.videos).hasSize(1)
        assertThat(result.videos[0].title).isEqualTo("有效视频")
    }

    private fun fakeArchiveDynamicItem(
        aid: Long,
        title: String,
    ) = DynamicItem
        .newBuilder()
        .apply {
            addModules(
                Module
                    .newBuilder()
                    .apply {
                        moduleType = DynModuleType.module_author
                        moduleAuthor =
                            ModuleAuthor
                                .newBuilder()
                                .apply {
                                    ptimeLabelText = "2024-01-01 12:00"
                                    author =
                                        UserInfo
                                            .newBuilder()
                                            .apply {
                                                mid = 1L
                                                name = "UP"
                                            }.build()
                                }.build()
                    }.build(),
            )
            addModules(
                Module
                    .newBuilder()
                    .apply {
                        moduleType = DynModuleType.module_dynamic
                        moduleDynamic =
                            ModuleDynamic
                                .newBuilder()
                                .apply {
                                    dynArchive =
                                        MdlDynArchive
                                            .newBuilder()
                                            .apply {
                                                this.title = title
                                                cover = "http://cover.test"
                                                avid = aid
                                                bvid = "BV$aid"
                                                cid = aid * 10
                                            }.build()
                                }.build()
                    }.build(),
            )
        }.build()

    private fun fakeUnknownDynamicItem() =
        DynamicItem
            .newBuilder()
            .apply {
                addModules(
                    Module
                        .newBuilder()
                        .apply {
                            moduleType = DynModuleType.module_author
                            moduleAuthor =
                                ModuleAuthor
                                    .newBuilder()
                                    .apply {
                                        ptimeLabelText = "2024-01-01"
                                        author =
                                            UserInfo
                                                .newBuilder()
                                                .apply {
                                                    mid = 1L
                                                    name = "UP"
                                                }.build()
                                    }.build()
                        }.build(),
                )
                addModules(
                    Module
                        .newBuilder()
                        .apply {
                            moduleType = DynModuleType.module_dynamic
                            moduleDynamic = ModuleDynamic.getDefaultInstance()
                        }.build(),
                )
            }.build()
}
