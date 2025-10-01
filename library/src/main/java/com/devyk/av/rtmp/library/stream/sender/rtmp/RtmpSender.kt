package com.devyk.av.rtmp.library.stream.sender.rtmp

import com.devyk.av.rtmp.library.Contacts
import com.devyk.av.rtmp.library.callback.OnConnectListener
import com.devyk.av.rtmp.library.stream.PacketType
import com.devyk.av.rtmp.library.stream.sender.Sender

class RtmpSender : Sender {
    private var listener: OnConnectListener? = null
    private var mRtmpUrl: String? = null

    companion object {
        init {
            System.loadLibrary("AVRtmpPush")
        }
    }


    override fun onData(data: ByteArray, type: PacketType) {
        if (type == PacketType.FIRST_AUDIO || type == PacketType.AUDIO) {
            //音频数据
            pushAudio(data, data.size, type.type)
        } else if (type == PacketType.FIRST_VIDEO ||
            type == PacketType.KEY_FRAME || type == PacketType.VIDEO
        ) {
            //视频数据
            pushVideo(data, data.size, type.type)
        }
    }


    fun setDataSource(source: String) {
        mRtmpUrl = source
    }

    fun connect() {
        NativeRtmpConnect(mRtmpUrl)

    }

    fun close() {
        NativeRtmpClose()
        onClose()
    }

    fun setOnConnectListener(lis: OnConnectListener) {
        listener = lis
    }


    /**
     * C++ 层调用
     * 开始链接
     */
    fun onConnecting() {
        listener?.onConnecting()
    }

    /**
     * C++ 层调用
     * 连接成功
     */
    fun onConnected() {
        listener?.onConnected()
    }

    /**
     * C++ 层调用
     * 关闭成功
     */
    fun onClose() {
        listener?.onClose()
    }


    /**
     * C++ 层调用
     * 推送失败
     */
    fun onError(errorCode: Int) {
        listener?.onFail(errorCode2errorMessage(errorCode))
    }

    private fun errorCode2errorMessage(errorCode: Int): String {

        var message = "未知错误，请联系管理员!"
        if (errorCode == Contacts.RTMP_CONNECT_ERROR) {
            message = "RTMP server connect fail!"
        } else if (errorCode == Contacts.RTMP_INIT_ERROR) {
            message = "RTMP native init fail!"
        } else if (errorCode == Contacts.RTMP_SET_URL_ERROR) {
            message = "RTMP url set fail!"
        }
        return message
    }


    private external fun NativeRtmpConnect(url: String?);
    private external fun NativeRtmpClose();
    private external fun pushAudio(data: ByteArray, size: Int, type: Int)
    private external fun pushVideo(data: ByteArray, size: Int, isKeyFrame: Int)
    private external fun pushSpsPps(sps: ByteArray, size: Int, pps: ByteArray, size1: Int)
}