package dev.frost819.newbv.player.impl.exo

import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.mediacodec.MediaCodecSelector
import androidx.media3.exoplayer.mediacodec.MediaCodecUtil

/**
 * 仅选择 AOSP 软件视频解码器（`OMX.google.*` / `c2.android.*`）。
 *
 * 列表为空时回退为全部解码器，避免设备无软解时无法起播。
 * 与强制软解偏好配套；默认播放路径应使用 [MediaCodecSelector.DEFAULT]（硬解优先）。
 */
@OptIn(UnstableApi::class)
val softwareVideoCodecSelector: MediaCodecSelector =
    MediaCodecSelector { mimeType, requiresSecureDecoder, requiresTunnelingDecoder ->
        val allDecoders =
            MediaCodecUtil.getDecoderInfos(
                mimeType,
                requiresSecureDecoder,
                requiresTunnelingDecoder,
            )
        val softwareDecoders =
            allDecoders.filter { info ->
                val name = info.name
                name.startsWith("OMX.google.", ignoreCase = true) ||
                    name.startsWith("c2.android.", ignoreCase = true)
            }
        softwareDecoders.ifEmpty { allDecoders }
    }
