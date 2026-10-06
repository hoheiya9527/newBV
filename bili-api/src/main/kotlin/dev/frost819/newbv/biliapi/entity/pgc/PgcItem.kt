package dev.frost819.newbv.biliapi.entity.pgc

import dev.frost819.newbv.biliapi.http.SeasonIndexType

data class PgcItem(
    var cover: String,
    var title: String,
    var subTitle: String,
    var seasonId: Int,
    var episodeId: Int,
    var seasonType: SeasonIndexType,
    var rating: String,
) {
    companion object {
        fun fromFeedSubItem(feedSubItem: dev.frost819.newbv.biliapi.http.entity.pgc.PgcFeedData.FeedSubItem): PgcItem =
            fromFeedSubItemOrNull(feedSubItem)
                ?: throw IllegalArgumentException("Feed 条目缺少 seasonId 或 seasonType")

        /**
         * 转成网格卡片。缺少季 ID、分区或标题时返回 null，调用方跳过该条。
         */
        fun fromFeedSubItemOrNull(
            feedSubItem: dev.frost819.newbv.biliapi.http.entity.pgc.PgcFeedData.FeedSubItem,
        ): PgcItem? {
            val seasonId = feedSubItem.seasonId ?: return null
            val seasonType = feedSubItem.seasonType ?: return null
            if (feedSubItem.title.isBlank()) return null
            return PgcItem(
                cover = feedSubItem.cover,
                title = feedSubItem.title,
                subTitle = feedSubItem.subTitle,
                seasonId = seasonId,
                episodeId = feedSubItem.episodeId,
                seasonType = SeasonIndexType.fromId(seasonType),
                rating = feedSubItem.rating ?: "0",
            )
        }

        fun fromFeedSubItem(
            feedSubItem: dev.frost819.newbv.biliapi.http.entity.pgc.PgcFeedV3Data.FeedItem.FeedSubItem,
        ): PgcItem =
            fromFeedSubItemOrNull(feedSubItem)
                ?: throw IllegalArgumentException("FeedV3 条目缺少 seasonId 或 seasonType")

        /**
         * 转成网格卡片。缺少季 ID、分区或标题时返回 null，调用方跳过该条。
         */
        fun fromFeedSubItemOrNull(
            feedSubItem: dev.frost819.newbv.biliapi.http.entity.pgc.PgcFeedV3Data.FeedItem.FeedSubItem,
        ): PgcItem? {
            val seasonId = feedSubItem.seasonId ?: return null
            val seasonType = feedSubItem.seasonType ?: return null
            val episodeId = feedSubItem.episodeId ?: feedSubItem.inline?.epId ?: return null
            if (feedSubItem.title.isBlank()) return null
            return PgcItem(
                cover = feedSubItem.cover,
                title = feedSubItem.title,
                subTitle = feedSubItem.subTitle,
                seasonId = seasonId,
                episodeId = episodeId,
                seasonType = SeasonIndexType.fromId(seasonType),
                rating = feedSubItem.rating ?: "0",
            )
        }

        fun fromIndexResultItem(
            indexResultItem: dev.frost819.newbv.biliapi.http.entity.index.IndexResultData.IndexResultItem,
        ): PgcItem =
            PgcItem(
                cover = indexResultItem.cover,
                title = indexResultItem.title,
                subTitle = indexResultItem.subTitle,
                seasonId = indexResultItem.seasonId,
                episodeId = indexResultItem.firstEp.epId,
                seasonType = SeasonIndexType.fromId(indexResultItem.seasonType),
                rating = indexResultItem.score,
            )
    }
}
