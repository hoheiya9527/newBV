package dev.frost819.newbv.app.ui.component.videocard

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme

/** 首屏/刷新空列表时的骨架数量（4 列 × 2 行）。 */
const val VIDEO_GRID_SKELETON_COUNT = 8

/**
 * 与 [SmallVideoCard] 同比例的占位骨架，用于首屏和刷新时空窗。
 */
@Composable
fun VideoCardSkeleton(modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition(label = "videoCardSkeleton")
    val alpha by pulse.animateFloat(
        initialValue = 0.08f,
        targetValue = 0.18f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 900),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "skeletonAlpha",
    )
    val fill = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)

    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.6f)
                    .clip(MaterialTheme.shapes.large)
                    .background(fill),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(0.92f)
                    .height(16.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(fill),
        )
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier =
                Modifier
                    .padding(bottom = 4.dp)
                    .fillMaxWidth(0.45f)
                    .height(12.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(fill),
        )
    }
}

/**
 * 列表为空且正在加载时插入骨架卡，避免刷新后整页空白。
 *
 * @param visible 是否显示骨架。
 * @param keyPrefix 骨架 item key 前缀，避免多列表冲突。
 */
fun LazyGridScope.videoGridSkeleton(
    visible: Boolean,
    keyPrefix: String,
    count: Int = VIDEO_GRID_SKELETON_COUNT,
) {
    if (!visible) return
    items(
        count = count,
        key = { index -> "${keyPrefix}_skel_$index" },
    ) {
        VideoCardSkeleton()
    }
}
