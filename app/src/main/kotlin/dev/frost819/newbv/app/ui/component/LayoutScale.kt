package dev.frost819.newbv.app.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt

/**
 * 布局按此密度设计。界面缩放高于该值时，封面不按同样倍数放大。
 */
const val DESIGN_DENSITY = 2f

/**
 * 按设计密度换算 dp，使封面、轮播的像素尺寸不随界面缩放变大。
 *
 * 文字仍用 sp，会随缩放变大。当前密度不高于 [DESIGN_DENSITY] 时原样返回。
 */
@Composable
@ReadOnlyComposable
fun Dp.atDesignDensity(designDensity: Float = DESIGN_DENSITY): Dp {
    val density = LocalDensity.current.density
    if (density <= designDensity || density <= 0f) return this
    return this * (designDensity / density)
}

/**
 * 网格列数。缩放高于设计密度时增加列数，避免单张海报占满一行。
 *
 * 设计密度及以下保持 [designColumns]。最多比设计列数多 2 列。
 */
@Composable
@ReadOnlyComposable
fun scaledGridColumns(
    designColumns: Int,
    designDensity: Float = DESIGN_DENSITY,
): Int {
    val density = LocalDensity.current.density
    if (density <= designDensity || designDensity <= 0f) return designColumns
    val scaled = (designColumns * density / designDensity).roundToInt()
    return scaled.coerceIn(designColumns, designColumns + 2)
}
