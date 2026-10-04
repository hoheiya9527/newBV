package dev.frost819.newbv.player

/**
 * 播放器配置选项。
 *
 * @param userAgent 自定义 User-Agent，用于 HTTP 请求
 * @param referer 自定义 Referer，用于 B 站防盗链验证
 * @param enableFfmpegAudioRenderer 是否启用 FFmpeg 音频渲染器（用于解码特殊音频编码如 FLAC）
 * @param enableSoftwareVideoDecoder 为 true 时强制全程软解（兼容坏硬解）；
 * 为 false 时硬解优先，解码器初始化失败再尝试列表中的后续解码器（含软解）。
 */
data class VideoPlayerOptions(
    val userAgent: String? = null,
    val referer: String? = null,
    val enableFfmpegAudioRenderer: Boolean = false,
    val enableSoftwareVideoDecoder: Boolean = false,
)
