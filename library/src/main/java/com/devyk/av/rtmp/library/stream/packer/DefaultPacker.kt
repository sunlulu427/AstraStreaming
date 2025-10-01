package com.devyk.av.rtmp.library.stream.packer

import android.media.MediaCodec
import com.devyk.av.rtmp.library.stream.PacketType
import java.nio.ByteBuffer
import kotlin.experimental.and

class DefaultPacker : Packer {
    override fun start() {

    }

    override fun stop() {
    }

    override fun onVideoSpsPpsData(sps: ByteArray, pps: ByteArray, spsPps: PacketType) {
        mPacketListener?.onPacket(sps, pps, PacketType.SPS_PPS)
    }

    override fun onVideoData(bb: ByteBuffer?, bi: MediaCodec.BufferInfo?) {
        bb?.let { buffer ->
            bi?.let { mediaBuffer ->
                buffer.position(mediaBuffer.offset)
                buffer.limit(mediaBuffer.offset + mediaBuffer.size)
                val video = ByteArray(mediaBuffer.size)
                buffer.get(video)
                val tag = video[4].and(0x1f).toInt()

                var keyFrame = PacketType.VIDEO
                if (tag == 0x05) {//关键帧
                    keyFrame = PacketType.KEY_FRAME
                } else {
                    keyFrame = PacketType.VIDEO
                }
                mPacketListener?.onPacket(video, keyFrame)
            }
        }
    }

    private var mPacketListener: Packer.OnPacketListener? = null


    override fun onAudioData(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        bb.position(bi.offset)
        bb.limit(bi.offset + bi.size)
        val audio = ByteArray(bi.size)
        bb.get(audio)
        mPacketListener?.onPacket(audio, PacketType.AUDIO)
    }

    override fun setPacketListener(packetListener: Packer.OnPacketListener) {
        mPacketListener = packetListener
    }
}