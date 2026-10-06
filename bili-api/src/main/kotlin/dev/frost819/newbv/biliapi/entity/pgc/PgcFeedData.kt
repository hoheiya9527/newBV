package dev.frost819.newbv.biliapi.entity.pgc

data class PgcFeedData(
    var hasNext: Boolean,
    var cursor: Int,
    var items: List<PgcItem> = emptyList(),
    var ranks: List<FeedRank> = emptyList(),
) {
    companion object {
        fun fromPgcFeedData(data: dev.frost819.newbv.biliapi.http.entity.pgc.PgcFeedData): PgcFeedData =
            PgcFeedData(
                hasNext = data.hasNext,
                cursor = data.coursor,
                // 电影/电视剧等旧 Feed 会混入没有 seasonId 的运营卡，不能让一条脏数据丢掉整页
                items = data.items.mapNotNull { PgcItem.fromFeedSubItemOrNull(it) },
                ranks = emptyList(),
            )

        fun fromPgcFeedData(data: dev.frost819.newbv.biliapi.http.entity.pgc.PgcFeedV3Data): PgcFeedData {
            val itemsList = data.items.firstOrNull { it.subItems.firstOrNull()?.cardStyle == "v_card" }
            val ranksList = data.items.firstOrNull { it.subItems.firstOrNull()?.cardStyle == "rank" }
            val vCards = itemsList?.subItems?.mapNotNull { PgcItem.fromFeedSubItemOrNull(it) }.orEmpty()
            // 国创等分区首页有时只有排行榜。片名在榜单卡片的内层 subItems，不在模块第一层。
            val cards =
                vCards.ifEmpty {
                    data.items
                        .flatMap { module ->
                            module.subItems.flatMap { card ->
                                listOf(card) + card.subItems.orEmpty()
                            }
                        }.mapNotNull { PgcItem.fromFeedSubItemOrNull(it) }
                        .distinctBy { it.seasonId }
                }
            return PgcFeedData(
                hasNext = data.hasNext,
                cursor = data.coursor,
                items = cards,
                ranks = ranksList?.subItems?.mapNotNull { FeedRank.fromFeedSubItemOrNull(it) } ?: emptyList(),
            )
        }
    }

    data class FeedRank(
        var cover: String,
        var title: String,
        var subTitle: String,
        var items: List<PgcItem>,
    ) {
        companion object {
            fun fromFeedSubItem(
                feedSubItem: dev.frost819.newbv.biliapi.http.entity.pgc.PgcFeedV3Data.FeedItem.FeedSubItem,
            ): FeedRank =
                fromFeedSubItemOrNull(feedSubItem)
                    ?: FeedRank(
                        cover = feedSubItem.cover,
                        title = feedSubItem.title,
                        subTitle = feedSubItem.subTitle,
                        items = emptyList(),
                    )

            /**
             * 转成排行榜。内层条目缺字段时跳过该条，不让一条脏数据丢掉整页。
             */
            fun fromFeedSubItemOrNull(
                feedSubItem: dev.frost819.newbv.biliapi.http.entity.pgc.PgcFeedV3Data.FeedItem.FeedSubItem,
            ): FeedRank? {
                if (feedSubItem.title.isBlank()) return null
                return FeedRank(
                    cover = feedSubItem.cover,
                    title = feedSubItem.title,
                    subTitle = feedSubItem.subTitle,
                    items = feedSubItem.subItems?.mapNotNull { PgcItem.fromFeedSubItemOrNull(it) } ?: emptyList(),
                )
            }
        }
    }
}
