package com.astrastream.avpush.application.controller

import android.media.MediaCodec
import android.media.MediaFormat
import com.astrastream.avpush.infrastructure.audio.AudioProcessor
import com.astrastream.avpush.domain.callback.IController
import com.astrastream.avpush.domain.callback.OnAudioEncodeListener
import com.astrastream.avpush.domain.config.AudioConfiguration
import com.astrastream.avpush.infrastructure.codec.AudioEncoder
import java.nio.ByteBuffer

class AudioController(private val audioConfiguration: AudioConfiguration) : IController,
    AudioProcessor.OnRecordListener,
    OnAudioEncodeListener {

    /**
     * 音频编解码用到的实体程序
     */
    private val mAudioEncoder: AudioEncoder = AudioEncoder(audioConfiguration)

    /**
     * 音频采集用到的实体程序
     */
    private val mAudioProcessor: AudioProcessor = AudioProcessor()

    /**
     * 音频数据的监听
     */
    private var mAudioDataListener: IController.OnAudioDataListener? = null


    init {
        mAudioProcessor.init(
            audioConfiguration.audioSource,
            audioConfiguration.sampleRate,
            audioConfiguration.channelCount
        )
        mAudioProcessor.addRecordListener(this)
        mAudioEncoder.setOnAudioEncodeListener(this)
    }

    /**
     * 触发 开始
     */
    override fun start() {
        mAudioProcessor.startRcording()
    }

    /**
     * 触发 暂停
     */
    override fun pause() {
        mAudioProcessor.setPause(true)
    }

    /**
     * 触发恢复
     */
    override fun resume() {
        mAudioProcessor.setPause(false)
    }

    /**
     * 触发停止
     */
    override fun stop() {
        mAudioProcessor.stop()

    }

    /**
     * 当采集 PCM 数据的时候返回
     */
    override fun onPcmData(byteArray: ByteArray) {
        mAudioEncoder.enqueueCodec(byteArray)
    }

    /**
     * 当开始采集
     */
    override fun onStart(sampleRate: Int, channels: Int, sampleFormat: Int) {
        mAudioEncoder.start()
    }

    /**
     * 设置禁言
     */
    override fun setMute(isMute: Boolean) {
        super.setMute(isMute)
        mAudioProcessor.setMute(isMute)
    }

    override fun onStop() {
        super.onStop()
        mAudioEncoder.stop()
    }

    /**
     * 当采集出现错误
     */
    override fun onError(meg: String?) {
        mAudioDataListener?.onError(meg)
    }

    /**
     * 当 Audio 编码数据的时候
     */
    override fun onAudioEncode(bb: ByteBuffer, bi: MediaCodec.BufferInfo) {
        mAudioDataListener?.onAudioData(bb, bi)
    }

    /**
     * 编码的输出格式
     */
    override fun onAudioOutformat(outputFormat: MediaFormat?) {
        mAudioDataListener?.onAudioOutformat(outputFormat)
    }

    override fun setAudioDataListener(audioDataListener: IController.OnAudioDataListener) {
        super.setAudioDataListener(audioDataListener)
        mAudioDataListener = audioDataListener
    }
}
