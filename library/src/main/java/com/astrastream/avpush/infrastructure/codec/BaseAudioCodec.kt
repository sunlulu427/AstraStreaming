package com.astrastream.avpush.infrastructure.codec

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import com.astrastream.avpush.domain.config.AudioConfiguration
import com.astrastream.avpush.core.utils.LogHelper

import java.nio.ByteBuffer

abstract class BaseAudioCodec(private val mAudioConfiguration: AudioConfiguration?) : IAudioCodec {
    private var mMediaCodec: MediaCodec? = null
    internal var mBufferInfo = MediaCodec.BufferInfo()

    private var TAG = javaClass.simpleName
    private var mPts = 0L

    /**
     * 编码完成的函数自己不处理，交由子类处理
     */
    abstract fun onAudioData(bb: ByteBuffer, bi: MediaCodec.BufferInfo);

    @Synchronized
    override fun start() {
        mMediaCodec = AudioMediaCodec.getAudioMediaCodec(mAudioConfiguration!!)
        mMediaCodec!!.start()
        Log.e("encode", "--start")
    }

    /**
     * 将数据入队 java.lang.IllegalStateException
     */
    @Synchronized
    override fun enqueueCodec(input: ByteArray?) {
        if (mMediaCodec == null || input == null) {
            return
        }
        val inputBuffers = mMediaCodec!!.inputBuffers
        val outputBuffers = mMediaCodec!!.outputBuffers
        val inputBufferIndex = mMediaCodec!!.dequeueInputBuffer(12000)

        if (inputBufferIndex >= 0) {
            val inputBuffer = inputBuffers[inputBufferIndex]
            inputBuffer.clear()
            inputBuffer.put(input)
            mMediaCodec!!.queueInputBuffer(inputBufferIndex, 0, input.size, 0, 0)
        }

        var outputBufferIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo, 12000)
        if (outputBufferIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
            onAudioOutformat(mMediaCodec?.outputFormat)
        }

        while (outputBufferIndex >= 0) {
            val outputBuffer = outputBuffers[outputBufferIndex]

            if (mPts == 0L)
                mPts = System.nanoTime() / 1000;

            mBufferInfo.presentationTimeUs = System.nanoTime() / 1000 - mPts;

            LogHelper.e(TAG, "音频时间戳：${mBufferInfo.presentationTimeUs / 1000_000}")
            onAudioData(outputBuffer, mBufferInfo)
            mMediaCodec!!.releaseOutputBuffer(outputBufferIndex, false)
            outputBufferIndex = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo, 0)
        }
    }

    abstract fun onAudioOutformat(outputFormat: MediaFormat?)

    @Synchronized
    override fun stop() {
        if (mMediaCodec != null) {
            mMediaCodec!!.stop()
            mMediaCodec!!.release()
            mMediaCodec = null
        }
    }

    /**
     * 获取输出的格式
     */
    fun getOutputFormat(): MediaFormat? = mMediaCodec?.outputFormat
}
