package com.martin.audiolib.play

import android.os.Handler
import android.os.Looper
import java.util.concurrent.CopyOnWriteArraySet

abstract class BasePlay : IPlay {

    private val listeners = CopyOnWriteArraySet<IPlayListener>()

    override fun addPlayListener(listener: IPlayListener?) {
        listener?.run {
            listeners.add(this)
        }
    }

    override fun removePlayListener(listener: IPlayListener?) {
        listener?.run {
            if (listeners.contains(this)) {
                listeners.remove(this)
            }
        }
    }

    protected fun notifyPreparing(filePath: String) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onPreparing(filePath)
            }
        }
    }

    protected fun notifyPrepared(filePath: String) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onPrepared(filePath)
            }
        }
    }

    protected fun notifyStart(filePath: String) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onStart(filePath)
            }
        }
    }

    protected fun notifyPause(filePath: String) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onPause(filePath)
            }
        }
    }

    protected fun notifyStop(filePath: String) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onStop(filePath)
            }
        }
    }

    protected fun notifyComplete(filePath: String) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onComplete(filePath)
            }
        }
    }

    protected fun notifyProgress(filePath: String, progress: Long, duration: Long) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onProgressNotify(filePath, progress, duration)
            }
        }
    }

    protected fun notifyError(filePath: String, code: Int, msg: String?) {
        Handler(Looper.getMainLooper()).post {
            listeners.forEach {
                it.onError(filePath, code, msg ?: "")
            }
        }
    }

}