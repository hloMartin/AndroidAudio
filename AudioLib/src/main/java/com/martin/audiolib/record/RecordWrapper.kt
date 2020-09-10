package com.martin.audiolib.record

import android.content.Context
import android.media.AudioRecord
import android.media.AudioTrack
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.text.TextUtils
import com.martin.audiolib.*
import java.lang.Exception
import java.util.concurrent.Semaphore

abstract class RecordWrapper(context: Context) : BaserRecord() {

    private val MAX_RECORD_TIME = 5 * 60 * 1000L

    private var mContext = context
    private var mHasError = false
    private var mAudioRecord: AudioRecord? = null
    private var mIsRecording = false
    private var mFilePath: String? = null

    private var mFileWriteSemaphore = Semaphore(0)
    private var mStartRecordTime = 0L

    private var stopHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
        }
    }

    override fun startRecord(filePath: String) {
        if (isRecording()) {
            return
        }
        mIsRecording = true
        mHasError = false
        mFilePath = filePath
        stopHandler.removeCallbacksAndMessages(null)
        stopHandler.sendEmptyMessageDelayed(0, MAX_RECORD_TIME)
        mStartRecordTime = SystemClock.uptimeMillis()
        //new a thread to record audio
        AudioThreadManager.execute(Runnable {
            doRecordStart()
        })
    }

    /**
     * 执行录制操作
     */
    private fun doRecordStart() {
        if (TextUtils.isEmpty(mFilePath)) {
            mHasError = true
            notifyRecordError("", ERROR_FILE_PATH_EMPTY, null)
            return
        }
        mAudioRecord = initAudioRecord()
        if (mAudioRecord == null) {
            throw RuntimeException("AudioRecord must be init")
        }
        try {
            mAudioRecord!!.startRecording()
        } catch (e: Exception) {
            e.printStackTrace()
            mHasError = true
            notifyRecordError(mFilePath!!, ERROR_NO_RECORD_PERMISSION, "no permission ---")
            return
        }
        if (!isAudioRecordRecording()) {
            log_e("start record failed, no record permission")
            mHasError = true
            notifyRecordError(mFilePath!!, ERROR_NO_RECORD_PERMISSION, "no permission ===")
            return
        }
        //开启读写的线程,写入数据即可
        AudioThreadManager.execute(Runnable {
            readAudioDataAndWrite2File()
        })
    }

    override fun stopRecord() {
        stopHandler.removeCallbacksAndMessages(null)
        if (!isRecording()) {
            return
        }
        AudioThreadManager.execute(Runnable {
            doRecordStop()
        })
    }

    private fun doRecordStop() {
        if (isAudioRecordRecording()) {
            try {
                mAudioRecord?.stop()
                mAudioRecord?.release()
                //等待写文件结束
                mFileWriteSemaphore.acquire()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (!mHasError) {
                notifyRecordStop(mFilePath!!)
            }
        }
        mIsRecording = false
    }

    private fun isAudioRecordRecording(): Boolean {
        return mAudioRecord != null && mAudioRecord!!.state == AudioTrack.STATE_INITIALIZED && mAudioRecord!!.recordingState == AudioRecord.RECORDSTATE_RECORDING
    }

    override fun isRecording(): Boolean {
        return mIsRecording
    }

    /**
     * 获取录制音频的时长
     * NOTE：这个时长不够精准的
     */
    override fun getRecordDuration(): Long {
        return SystemClock.uptimeMillis() - mStartRecordTime
    }

    override fun getRecordPath(): String? {
        return mFilePath
    }

    private fun readAudioDataAndWrite2File() {
        readAudioDataAndWrite2FileImpl(mFilePath, mAudioRecord)
        mFileWriteSemaphore.release()
    }

    fun setHasError(hasError: Boolean) {
        mHasError = hasError
    }


    protected abstract fun initAudioRecord(): AudioRecord?

    /**
     * 执行真正的从AudioRecord中读取数据和写入到文件中(此线程在工作线程中,不会阻塞控制线程)
     * 1. 需要通知开始真正的写入文件
     * 3. 有异常,通知异常
     *
     * @param filePath
     * @param audioRecord
     */
    protected abstract fun readAudioDataAndWrite2FileImpl(
        filePath: String?,
        audioRecord: AudioRecord?
    )

    protected abstract fun getFileSuffix(): String
}