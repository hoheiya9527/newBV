package dev.frost819.newbv.biliapi.entity.video

import dev.frost819.newbv.biliapi.entity.user.Author

data class RelatedVideo(
    val aid: Long,
    val cid: Long,
    val cover: String,
    val title: String,
    val duration: Int,
    val author: Author?,
    val jumpToSeason: Boolean,
    val epid: Int?,
    val view: Int,
    val danmaku: Int,
) {
    companion object {
        fun fromRelate(relate: bilibili.app.view.v1.Relate): RelatedVideo {
            val epId = parseEpIdFromUri(relate.uri).takeIf { relate.goto.needJumpToSeason() }
            return RelatedVideo(
                aid = relate.aid,
                cid = relate.cid,
                cover = relate.pic,
                title = relate.title,
                duration = relate.duration.toInt(),
                author =
                    if (relate.hasAuthor()) {
                        Author.fromAuthor(relate.author)
                    } else {
                        Author(0, relate.desc, "")
                    },
                jumpToSeason = epId != null,
                epid = epId,
                view = relate.stat.view,
                danmaku = relate.stat.danmaku,
            )
        }

        fun fromRelate(relate: dev.frost819.newbv.biliapi.http.entity.video.RelatedVideoInfo): RelatedVideo {
            val epId = parseEpIdFromUri(relate.redirectUrl)
            return RelatedVideo(
                aid = relate.aid,
                cid = relate.cid,
                cover = relate.pic,
                title = relate.title,
                duration = relate.duration,
                author = relate.owner.let { Author.fromVideoOwner(it) },
                jumpToSeason = epId != null,
                epid = epId,
                view = relate.stat.view,
                danmaku = relate.stat.danmaku,
            )
        }
    }
}

private val EP_ID_REGEX = Regex("""/ep(\d+)""")

/**
 * 从跳转链接 / uri 中解析番剧 EP ID。
 *
 * 兼容 `https://www.bilibili.com/bangumi/play/ep1364037?theme=movie`、
 * `.../ep284272/`、`bilibili://.../ep12345` 等格式；解析失败返回 null。
 */
internal fun parseEpIdFromUri(url: String?): Int? {
    val match = url?.let { EP_ID_REGEX.find(it) } ?: return null
    return match.groupValues.getOrNull(1)?.toIntOrNull()
}

private fun String.needJumpToSeason() = this.contains("bangumi_ep") || this.contains("special")
