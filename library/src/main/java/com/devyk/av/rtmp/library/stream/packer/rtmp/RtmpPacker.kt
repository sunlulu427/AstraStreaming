package com.devyk.av.rtmp.library.stream.packer.rtmp

import android.media.MediaCodec
import com.devyk.av.rtmp.library.stream.AnnexbHelper
import com.devyk.av.rtmp.library.stream.PacketType
import com.devyk.av.rtmp.library.stream.packer.Packer
import com.devyk.av.rtmp.library.stream.packer.flv.FlvPackerHelper
import com.devyk.av.rtmp.library.stream.packer.flv.FlvPackerHelper.AUDIO_HEADER_SIZE
import com.devyk.av.rtmp.library.stream.packer.flv.FlvPackerHelper.AUDIO_SPECIFIC_CONFIG_SIZE
import com.devyk.av.rtmp.library.stream.packer.flv.FlvPackerHelper.VIDEO_HEADER_SIZE
import com.devyk.av.rtmp.library.stream.packer.flv.FlvPackerHelper.VIDEO_SPECIFIC_CONFIG_EXTEND_SIZE
import java.nio.ByteBuffer


class RtmpPacker : Packer, AnnexbHelper.AnnexbNaluListener {

    private var packetListener: Packer.OnPacketListener? = null
    private var isHeaderWrite: Boolean = false
    private var isKeyFrameWrite: Boolean = false

    private var mAudioSampleRate: Int = 44100
    private var mAudioSampleSize: Int = 16
    private var mIsStereo: Boolean = false
    private val mAnnexbHelper = AnnexbHelper()

    override fun setPacketListener(listener: Packer.OnPacketListener) {
        packetListener = listener
    }

    override fun start() {
        mAnnexbHelper.setAnnexbNaluListener(this)
    }

    override fun onVideoData(bb: ByteBuffer?, bi: MediaCodec.BufferInfo?) {
        mAnnexbHelper.analyseVideoData(bb!!, bi!!)
    }

    override fun onAudioData(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        if (packetListener == null || !isHeaderWrite || !isKeyFrameWrite) {
            return
        }
        bb.position(bi.offset)
        bb.limit(bi.offset + bi.size)

        val audio = ByteArray(bi.size)
        bb.get(audio)
        val size = AUDIO_HEADER_SIZE + audio.size
        val buffer = ByteBuffer.allocate(size)
        FlvPackerHelper.writeAudioTag(buffer, audio, false, mAudioSampleSize)
        packetListener!!.onPacket(buffer.array(), PacketType.AUDIO)
    }

    override fun stop() {
        isHeaderWrite = false
        isKeyFrameWrite = false
        mAnnexbHelper.stop()
    }

    override fun onVideo(video: ByteArray, isKeyFrame: Boolean) {
        if (packetListener == null || !isHeaderWrite) {
            return
        }
        var packetType = PacketType.VIDEO
        if (isKeyFrame) {
            isKeyFrameWrite = true
            packetType = PacketType.KEY_FRAME
        }
        //确保第一帧是关键帧，避免一开始出现灰色模糊界面
        if (!isKeyFrameWrite) {
            return
        }
        val size = VIDEO_HEADER_SIZE + video.size
        val buffer = ByteBuffer.allocate(size)
        FlvPackerHelper.writeH264Packet(buffer, video, isKeyFrame)
        packetListener!!.onPacket(buffer.array(),packetType)
    }


    override fun onSpsPps(sps: ByteArray?, pps: ByteArray?) {
        if (packetListener == null) {
            return
        }
        //写入第一个视频信息
        writeFirstVideoTag(sps, pps)
        //写入第一个音频信息
        writeFirstAudioTag()
        isHeaderWrite = true
    }

    private fun writeFirstVideoTag(sps: ByteArray?, pps: ByteArray?) {
        val size = VIDEO_HEADER_SIZE + VIDEO_SPECIFIC_CONFIG_EXTEND_SIZE + sps!!.size + pps!!.size
        val buffer = ByteBuffer.allocate(size)
        FlvPackerHelper.writeFirstVideoTag(buffer, sps, pps)
        packetListener!!.onPacket(buffer.array(), PacketType.FIRST_VIDEO)
    }

    private fun writeFirstAudioTag() {
        val size = AUDIO_SPECIFIC_CONFIG_SIZE + AUDIO_HEADER_SIZE
        val buffer = ByteBuffer.allocate(size)
        FlvPackerHelper.writeFirstAudioTag(buffer, mAudioSampleRate, mIsStereo, mAudioSampleSize)
        packetListener!!.onPacket(buffer.array(), PacketType.FIRST_AUDIO)
    }
}
