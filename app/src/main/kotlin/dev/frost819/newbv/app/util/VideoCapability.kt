package dev.frost819.newbv.app.util

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.exoplayer.RendererCapabilities
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.video.MediaCodecVideoRenderer
import dev.frost819.newbv.data.datastore.Prefs
import dev.frost819.newbv.data.datastore.VideoCodec
import dev.frost819.newbv.player.impl.exo.softwareVideoCodecSelector

/**
 * 视频解码能力描述。
 *
 * 用于查询本机解码器能否处理某条 DASH 视频流。
 *
 * @property codec 视频编码。
 * @property width 视频宽度（像素）。
 * @property height 视频高度（像素）。
 * @property frameRate 帧率（fps），未知为 null。
 * @property codecs RFC 6381 编码串（如 `avc1.640034`），用于 profile/level 判定。
 */
data class VideoDecodeProfile(
    val codec: VideoCodec,
    val width: Int,
    val height: Int,
    val frameRate: Float?,
    val codecs: String?,
)

/**
 * 设备视频解码能力查询接口。
 *
 * 抽象为接口以便单测注入确定性实现（真实实现依赖 Android MediaCodec）。
 */
interface VideoCapabilityProvider {
    /**
     * 本机能否解码该视频格式。
     *
     * 判定需与播放器实际使用的解码器选择策略一致（硬解优先，用户在设置中
     * 开启软解时按软解选择器判定），避免出现「选流判可解、播放却失败」。
     *
     * @param profile 待判定的视频格式。
     * @return 可解码返回 true。
     */
    fun isDecodable(profile: VideoDecodeProfile): Boolean
}

/**
 * 基于 Media3 [MediaCodecVideoRenderer.supportsFormat] 的实现。
 *
 * 复用播放器（ExoPlayer）自身的格式支持判定，涵盖 mime、profile/level、
 * 尺寸×帧率（宏块率）等，保证与 ExoPlayer 的判断一致。查询异常视为不可解，
 * 由选流降档 / 运行时回退承接，避免把超能力流送进解码器。
 *
 * @param context 应用上下文（查询系统解码器能力）。
 * @param useSoftwareDecoder 是否按软件解码选择器判定；默认读取用户偏好。测试可注入固定值。
 */
class MediaCodecVideoCapabilityProvider(
    private val context: Context,
    private val useSoftwareDecoder: () -> Boolean = { Prefs.enableSoftwareVideoDecoder },
) : VideoCapabilityProvider {
    private val cache = mutableMapOf<String, Boolean>()

    override fun isDecodable(profile: VideoDecodeProfile): Boolean {
        val mime = profile.codec.mimeType ?: return true
        val software = useSoftwareDecoder()
        val key =
            "$mime|${profile.width}x${profile.height}|" +
                "${profile.frameRate}|${profile.codecs}|$software"
        return cache.getOrPut(key) { query(mime, profile, software) }
    }

    private fun query(
        mime: String,
        profile: VideoDecodeProfile,
        software: Boolean,
    ): Boolean =
        runCatching {
            val format =
                Format
                    .Builder()
                    .setSampleMimeType(mime)
                    .setWidth(profile.width)
                    .setHeight(profile.height)
                    .apply { profile.frameRate?.let { setFrameRate(it) } }
                    .apply { profile.codecs?.let { setCodecs(it) } }
                    .build()
            val selector = if (software) softwareVideoCodecSelector else MediaCodecSelector.DEFAULT
            val support = MediaCodecVideoRenderer.supportsFormat(context, selector, format)
            (support and RendererCapabilities.FORMAT_SUPPORT_MASK) == C.FORMAT_HANDLED
        }.getOrDefault(false)

    /** [VideoCodec] 对应的视频 MIME 类型，未知为 null。 */
    private val VideoCodec.mimeType: String?
        get() =
            when (this) {
                VideoCodec.AVC -> "video/avc"
                VideoCodec.HEVC -> "video/hevc"
                VideoCodec.AV1 -> "video/av01"
                VideoCodec.DVH1 -> "video/dolby-vision"
            }
}
