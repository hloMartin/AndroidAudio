package com.martin.audiolib.record

import android.os.Handler
import android.os.Looper
import com.martin.audiolib.log_d
import java.util.concurrent.CopyOnWriteArraySet

abstract class BaserRecord : IRecord {

    private val listeners = CopyOnWriteArraySet<IRecordListener>()

    override fun addRecordListener(listener: IRecordListener?) {
        listener?.run {
            listeners.add(this)
        }
    }

    override fun removeRecordListener(listener: IRecordListener?) {
        listener?.run {
            if (listeners.contains(this)) {
                listeners.remove(this)
            }
        }
    }

    fun notifyRecordStart(filePath: String) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onRecordStart(filePath)
            }
        }
    }

    fun notifyRecordStop(filePath: String) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onRecordStop(filePath)
            }
        }
    }

    fun notifyRecordError(filePath: String, code: Int, msg: String?) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onRecordError(filePath, code, msg)
            }
        }
    }

}