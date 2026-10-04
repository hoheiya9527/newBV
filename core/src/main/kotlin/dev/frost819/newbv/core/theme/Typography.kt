package dev.frost819.newbv.core.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Typography as CommonTypography
import androidx.tv.material3.Typography as TvTypography

/**
 * 字体排版定义。
 *
 * 提供 TV Material3 与普通 Material3 两套 Typography，字号与 Material3 默认一致。
 * 界面按语义选用，不要手写 sp：页标题用 headlineSmall，区块标题用 titleLarge，
 * 卡片标题用 titleMedium，空态 / 失败提示用 bodyLarge。
 */
object BVTypography {
    /** TV Material3 Typography。 */
    val tv: TvTypography =
        TvTypography(
            displaySmall = TextStyle(fontSize = 36.sp),
            displayMedium = TextStyle(fontSize = 45.sp),
            displayLarge = TextStyle(fontSize = 57.sp),
            headlineSmall = TextStyle(fontSize = 24.sp),
            headlineMedium = TextStyle(fontSize = 28.sp),
            headlineLarge = TextStyle(fontSize = 32.sp),
            titleSmall = TextStyle(fontSize = 14.sp),
            titleMedium = TextStyle(fontSize = 16.sp),
            titleLarge = TextStyle(fontSize = 22.sp),
            bodySmall = TextStyle(fontSize = 12.sp),
            bodyMedium = TextStyle(fontSize = 14.sp),
            bodyLarge = TextStyle(fontSize = 16.sp),
            labelSmall = TextStyle(fontSize = 11.sp),
            labelMedium = TextStyle(fontSize = 12.sp),
            labelLarge = TextStyle(fontSize = 14.sp),
        )

    /** 普通 Material3 Typography（用于非 TV 组件，使用 Material3 默认字号）。 */
    val common: CommonTypography =
        CommonTypography(
            displaySmall = TextStyle(fontSize = 36.sp),
            displayMedium = TextStyle(fontSize = 45.sp),
            displayLarge = TextStyle(fontSize = 57.sp),
            headlineSmall = TextStyle(fontSize = 24.sp),
            headlineMedium = TextStyle(fontSize = 28.sp),
            headlineLarge = TextStyle(fontSize = 32.sp),
            titleSmall = TextStyle(fontSize = 14.sp),
            titleMedium = TextStyle(fontSize = 16.sp),
            titleLarge = TextStyle(fontSize = 22.sp),
            bodySmall = TextStyle(fontSize = 12.sp),
            bodyMedium = TextStyle(fontSize = 14.sp),
            bodyLarge = TextStyle(fontSize = 16.sp),
            labelSmall = TextStyle(fontSize = 11.sp),
            labelMedium = TextStyle(fontSize = 12.sp),
            labelLarge = TextStyle(fontSize = 14.sp),
        )
}
