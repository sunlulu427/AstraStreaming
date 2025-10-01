package com.astrastream.avpush.infrastructure.audio

import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import java.nio.ByteBuffer


/**
 * 调用 startRecording 开始录音，调用 stopRecord 停止录音
 * 利用adb pull 导出PCM文件
 * 	adb pull record .
 * 利用ffplay播放声音
 * 		ffplay -f s16le  -sample_rate 44100  -channels 1 -i /record.pcm
 * 利用ffmpeg将PCM文件转换为WAV文件
 * 		ffmpeg -f s16le  -sample_rate 44100  -channels 1 -i record.pcm -acodec pcm_s16le record.wav
 */
object AudioUtils {
    private var TAG = javaClass.simpleName;

    /**
     * 录音对象
     * @see AudioRecord
     */
    private var mAudioRecord: AudioRecord? = null;

    /**
     * 声音通道
     * 默认 单声道
     * @see AudioFormat.CHANNEL_IN_MONO 单声道
     * @see AudioFormat.CHANNEL_IN_STEREO 立体声
     */
    var AUDIO_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO

    /**
     * 采样率 如果 AudioRecord 初始化失败，那么可以降低为 16000 ，或者检查权限是否开启
     * 默认 44100
     */
    var SAMPLE_RATE_IN_HZ = 44100

    /**
     * 采样格式
     * 默认 16bit 存储
     *
     * @see AudioFormat.ENCODING_PCM_16BIT 兼容大部分手机
     */
    var AUDIO_FROMAT = AudioFormat.ENCODING_PCM_16BIT

    /**
     * 录音源
     * @see MediaRecorder.AudioSource.MIC 手机麦克风
     * @see MediaRecorder.AudioSource.VOICE_RECOGNITION 用于语音识别，等同于默认
     * @see MediaRecorder.AudioSource.VOICE_COMMUNICATION 用于 VOIP 应用
     */
    var AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;

    /**
     * 配置内部音频缓冲区的大小，由于不同厂商会有不同的实现。那么我们可以通过一个静态函数来 getMinBufferSize 来定义
     * @see AudioRecord.getMinBufferSize
     */
    private var mBufferSizeInBytes = 0;


    /**
     * 获取音频缓冲区大小
     */
    fun getMinBufferSize(
        sampleRateInHz: Int = SAMPLE_RATE_IN_HZ,
        channelConfig: Int = AUDIO_CHANNEL_CONFIG,
        audioFormat: Int = AUDIO_FROMAT
    ): Int = AudioRecord.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);

    /**
     * 拿到 AudioRecord 对象
     */
    fun initAudioRecord(
        audioSource: Int = AUDIO_SOURCE,
        sampleRateInHz: Int = SAMPLE_RATE_IN_HZ,
        channelConfig: Int = AUDIO_CHANNEL_CONFIG,
        audioFormat: Int = AUDIO_FROMAT
    ): Boolean {
        this.AUDIO_FROMAT = audioFormat
        this.AUDIO_CHANNEL_CONFIG = channelConfig
        this.AUDIO_SOURCE = audioSource
        this.SAMPLE_RATE_IN_HZ = sampleRateInHz

        //如果 AudioRecord 不为 null  那么直接销毁
        mAudioRecord?.run {
            release();
        }
        try {
            //得到录音缓冲大小
            mBufferSizeInBytes = getMinBufferSize(sampleRateInHz, channelConfig, audioFormat)
            mAudioRecord = AudioRecord(
                audioSource,
                sampleRateInHz,
                channelConfig,
                audioFormat,
                mBufferSizeInBytes
            )
        } catch (error: Exception) {
            Log.e(TAG, "AudioRecord init error :${error.message}")
            return false
        }

        //如果初始化失败那么降低采样率
        if (mAudioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            throw RuntimeException("检查音频源是否为占用，或者是否打开录音权限？")
        }
        return true
    }

    /**
     * 拿到录音设备
     */
    fun getAudioRecord(): AudioRecord? = mAudioRecord

    /**
     * 开始录制
     */
    fun startRecord() {
        mAudioRecord?.takeIf { it.state == AudioRecord.STATE_INITIALIZED }
            ?.startRecording()
    }

    /**
     * 开始录制
     */
    fun stopRecord() {
        mAudioRecord?.takeIf { it.state == AudioRecord.STATE_INITIALIZED }
            ?.stop()
    }

    /**
     * 释放资源
     */
    fun releaseRecord() {
        mAudioRecord?.release()
        mAudioRecord = null
    }

    /**
     * 读取音频数据
     */
    fun read(bufferSize: Int = mBufferSizeInBytes, offsetInBytes: Int = 0, byte: ByteArray): Int {
        return mAudioRecord?.read(byte, offsetInBytes, bufferSize) ?: 0
    }

    /**
     * 读取音频数据
     */
    fun read(bufferSize: Int = mBufferSizeInBytes, offsetInBytes: Int = 0, short: ShortArray): Int {
        return mAudioRecord?.read(short, offsetInBytes, bufferSize) ?: 0
    }

    /**
     * 读取音频数据
     */
    fun read(bufferSize: Int = mBufferSizeInBytes, buffer: ByteBuffer): Int {
        return mAudioRecord?.read(buffer, bufferSize) ?: 0
    }

    /**
     * 读取音频数据
     */
    fun read(bufferSize: Int = mBufferSizeInBytes, buffer: ByteArray): Int {
        return mAudioRecord?.read(buffer, 0, bufferSize) ?: 0
    }

    /**
     * 拿到缓冲大小
     */
    fun getBufferSize(): Int = mBufferSizeInBytes
}
