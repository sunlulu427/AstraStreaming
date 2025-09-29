package com.devyk.av.rtmp.library.utils

import android.util.Log
import com.devyk.av.rtmp.library.callback.ILog

/**
 * <pre>
 *     author  : devyk on 2020-06-02 00:07
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is LogHelper
 * </pre>
 */
object LogHelper : ILog {

    var isShowLog = false


    override fun i(tag: String, info: String?) {
        if (isShowLog)
            Log.i(tag, info.orEmpty())

    }

    override fun e(tag: String, info: String?) {
        if (isShowLog)
            Log.e(tag, info.orEmpty())
    }

    override fun w(tag: String, info: String?) {
        if (isShowLog)
            Log.w(tag, info.orEmpty())
    }

    override fun d(tag: String, info: String?) {
        if (isShowLog)
            Log.d(tag, info.orEmpty())
    }
}
