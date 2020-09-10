package com.martin.audiolib.play

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.HandlerThread
import android.text.TextUtils
import android.util.Log
import com.martin.audiolib.*
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.max

class OpusAudioPlayer : BasePlay() {

    private val SAMPLE_RATE = 48000
    private val CHANNEL_OUT_COUNT = AudioFormat.CHANNEL_OUT_MONO
    private val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    private val AUDIO_PLAY_BUFFER_MIN_SIZE = 3840

    private var mFilePath: String? = null
    private var mHasError: Boolean = false
    private var mPlayComplete: Boolean = false

    private var mAudioTrack: AudioTrack? = null
    private var mPlayBufferSize = 0
    private var mFileReadSemaphore = Semaphore(0)
    private var mState: AtomicInteger = AtomicInteger(PLAY_STATE_IDLE)
    private var mOpusTool: OpusTool? = null


    @Volatile
    private var mDecodePos: Long = 0L
    private var mDecodeFinished = false
    private var mUniqueId = 0L

    private var mCurrentSourceMode = AudioManager.STREAM_MUSIC

    private var playBackPosUpdateListener = object : AudioTrack.OnPlaybackPositionUpdateListener {
        override fun onMarkerReached(track: AudioTrack?) {
        }

        override fun onPeriodicNotification(track: AudioTrack?) {
        }
    }

    private var mHandlerThreadPlay = HandlerThread("audio_play")
    private var mHandlerThreadControl = HandlerThread("audio_control")
    private var mHandlerPlay: Handler
    private var mHandlerControl: Handler

    init {
        mHandlerThreadPlay.start()
        mHandlerThreadControl.start()

        mHandlerPlay = Handler(mHandlerThreadPlay.looper)
        mHandlerControl = Handler(mHandlerThreadControl.looper)
    }

    private fun initAudioTrack(mode: Int) {
        mPlayBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, CHANNEL_OUT_COUNT, AUDIO_FORMAT)
        if (mPlayBufferSize <= 0) {
            mPlayBufferSize = AUDIO_PLAY_BUFFER_MIN_SIZE
        }
        mAudioTrack = AudioTrack(
            mode,
            SAMPLE_RATE,
            CHANNEL_OUT_COUNT,
            AUDIO_FORMAT,
            mPlayBufferSize,
            AudioTrack.MODE_STREAM
        )
        mAudioTrack?.setPlaybackPositionUpdateListener(playBackPosUpdateListener)

        mCurrentSourceMode = mode
    }

    /**
     * 切换播放模式（外音或者耳机）
     */
    fun switchMode(mode: Int) {
        if (mode == mCurrentSourceMode) {
            return
        }
        mHandlerControl.post {
            mAudioTrack?.release()
            initAudioTrack(mode)
            if (mAudioTrack!!.state == AudioTrack.STATE_INITIALIZED) {
                mAudioTrack!!.play()
                mHandlerPlay.post {
                    writeFileToAudioTrack()
                }
            } else {
                stop()
            }
        }
    }

    override fun play(url: String, tagId: Long) {
        play(url, 0, tagId)
    }

    override fun play(url: String, targetPosition: Int, tagId: Long) {
        cleanUp()
        initAudioTrack(mCurrentSourceMode)
        mHandlerControl.post {
            if (TextUtils.isEmpty(url)) {
                return@post
            }
            if (!OpusTool.isLoadSucceed) {
                mHasError = true
                stop()
                notifyError(url, ERROR_LIB_SO_LOAD_FAILED, "opus lib load failed")
                return@post
            }
            mFilePath = url
            if (!isFileExists(mFilePath)) {
                Log.e(TAG, "play file not exist")
                mHasError = true
                stop()
                notifyError(mFilePath!!, ERROR_FILE_NOT_EXIST, "[play] file not exist")
                return@post
            }
            mUniqueId = tagId
            mFilePath = File(mFilePath).absolutePath
            if (mOpusTool == null) {
                mOpusTool = OpusTool()
            }
            if (!mOpusTool!!.isOpusFileFormat(mFilePath)) {
                mHasError = true
                stop()
                notifyError(
                    mFilePath!!,
                    ERROR_PLAY_FILE_TYPE_NOT_SUPPORT,
                    "file is not opus file, can not play..."
                )
                return@post
            }
            //回调状态
            mState.set(PLAY_STATE_PREPARING)
            notifyPreparing(mFilePath!!)
            mOpusTool?.let {
                if (!it.openFile(mFilePath)) {
                    mHasError = true
                    stop()
                    notifyError(mFilePath!!, ERROR_PLAY_FILE_OPEN_ERROR, "open file error...")
                    return@post
                }
                mState.set(PLAY_STATE_PREPARED)
                notifyPrepared(mFilePath!!)

                if (targetPosition > 0) {
                    mAudioTrack?.pause()
                    seekByPosition((targetPosition * 1.0f / 100 * getDuration()).toInt())
                } else {
                    mAudioTrack?.play()
                    mHandlerPlay.post {
                        writeFileToAudioTrack()
                    }
                }
            }
        }
    }

    override fun pause() {
        mHandlerControl.post {
            doPause(true)
        }
    }

    override fun resume() {
        mHandlerControl.post {
            if (!isAudioTrackPaused()) {
                return@post
            }
            mAudioTrack?.play()
            mHandlerPlay.post {
                writeFileToAudioTrack()
            }
        }
    }

    override fun stop() {
        mHandlerControl.post {
            var result = releaseAudioTrack()
            mState.set(PLAY_STATE_IDLE)
            if (result) {
                if (mHasError) {
                    return@post
                }
                if (mPlayComplete) {
                    notifyComplete(mFilePath!!)
                    return@post
                }
                notifyStop(mFilePath!!)
            }
        }
    }

    override fun isSupportSeekTo(): Boolean {
        return true
    }

    /**
     * @param process : 百分比进度
     */
    override fun seekTo(process: Int) {
        mHandlerControl.post {
            seekByPosition((process * 1.0f / 100 * getDuration()).toInt())
        }
    }

    override fun getPlayPath(): String {
        return mFilePath ?: ""
    }

    override fun getDuration(): Int {
        if (mOpusTool != null) {
            return mOpusTool!!.getDuration().toInt()
        }
        return 0
    }

    override fun getCurrentPosition(): Long {
        return OpusTool.convertPcm2NormalDuration(mDecodePos)
    }

    override fun isPlaying(): Boolean {
        return mState.get() == PLAY_STATE_RUNNING && isAudioTrackPlaying()
    }

    fun isPlaying(path: String?): Boolean {
        return isPlaying() && TextUtils.equals(path, mFilePath)
    }

    fun isPlaying(path: String?, id: Long): Boolean {
        return isPlaying() && TextUtils.equals(path, mFilePath) && mUniqueId == id
    }

    override fun getState(): Int {
        return mState.get()
    }

    fun cleanUp() {
        releaseAudioTrack()
        mFilePath = ""
        mUniqueId = 0
        mHasError = false
        mPlayComplete = false
        mDecodeFinished = false
        mHandlerPlay.removeCallbacksAndMessages(null)
        mHandlerControl.removeCallbacksAndMessages(null)
    }

    private fun releaseAudioTrack(): Boolean {
        mAudioTrack?.run {
            if (isAudioTrackValid()) {
                when (playState) {
                    AudioTrack.PLAYSTATE_PLAYING -> {
                        try {
                            flush()
                            stop()
                            release()
                            mFileReadSemaphore.acquire()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }
                    AudioTrack.PLAYSTATE_PAUSED -> {
                        try {
                            flush()
                            stop()
                            release()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun doPause(needNotify: Boolean) {
        if (isAudioTrackPlaying()) {
            try {
                mAudioTrack!!.pause()
                mState.set(PLAY_STATE_PAUSE)
            } catch (e: Exception) {
                e.printStackTrace()
                mHasError = true
            }
            try {
                mFileReadSemaphore.acquire()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (!mHasError && isAudioTrackPaused()) {
                if (needNotify) {
                    notifyPause(mFilePath!!)
                }
            } else if (mHasError) {
                stop()
                notifyError(mFilePath!!, ERROR_PLAY_EXCEPTION, "")
            }
        }
    }

    /**
     * @param targetPosition : 时间点进度
     */
    private fun seekByPosition(targetPosition: Int) {
        if (isAudioTrackValid()) {
            when (mAudioTrack!!.playState) {
                AudioTrack.PLAYSTATE_PAUSED -> {
                    seekOpusFile(targetPosition)
                    resume()
                }
                AudioTrack.PLAYSTATE_PLAYING -> {
                    doPause(false)
                    seekOpusFile(targetPosition)
                    resume()
                }
            }
        }
    }

    private fun seekOpusFile(targetPosition: Int) {
        var position = max(targetPosition, 0)
        if (mDecodeFinished) {
            mOpusTool!!.closeFile()
            mOpusTool!!.openFile(mFilePath)
            mDecodeFinished = false
        }
        if (mOpusTool!!.getPcmDuration() <= 0) {
            mHasError = true
            notifyError(mFilePath!!, ERROR_PLAY_FILE_IS_EMPTY, "audio duration is 0")
            stop()
            return
        }
        var pcmDuration = (position * 48.0f).toLong()
        if (pcmDuration > mOpusTool!!.getPcmDuration()) {
            pcmDuration = 0
        }
        var progress = pcmDuration * 1.0f / mOpusTool!!.getPcmDuration()
        mOpusTool!!.seekFile(progress)
    }

    private fun writeFileToAudioTrack() {
        var readOpusBuffer = ByteBuffer.allocateDirect(mPlayBufferSize)
        readOpusBuffer.order(ByteOrder.nativeOrder())
        val readOpusArgs = IntArray(3)
        var size = 0
        var isFirstRead = true
        var finished = false
        var count = 0
        //回调状态
        mState.set(PLAY_STATE_RUNNING)
        notifyStart(mFilePath!!)
        while (isAudioTrackPlaying()) {
            mOpusTool!!.readFile(readOpusBuffer, mPlayBufferSize, readOpusArgs)
            size = readOpusArgs[0]
            mDecodePos = readOpusArgs[1].toLong()
            finished = readOpusArgs[2] == OpusTool.READ_OPUS_FINISHED
            //有读取的数据
            if (size > 0) {
                if (isFirstRead) {
                    isFirstRead = false
                }
                readOpusBuffer.rewind()
                var data = ByteArray(size)
                readOpusBuffer.get(data)
                try {
                    mAudioTrack!!.write(data, 0, size)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                count++
                if (count >= 3) {
                    notifyProgress(
                        mFilePath!!,
                        getCurrentPosition(),
                        getDuration().toLong()
                    )
                    count = 0
                }
            }
            if (finished) {
                mDecodeFinished = true
                mAudioTrack!!.notificationMarkerPosition = 1
                mOpusTool!!.closeFile()
                //回调播放完成
                mState.set(PLAY_STATE_IDLE)
                notifyComplete(mFilePath!!)
                break
            }
        }
        mFileReadSemaphore.release()
    }

    private fun isAudioTrackValid(): Boolean {
        return mAudioTrack != null && mAudioTrack!!.state == AudioTrack.STATE_INITIALIZED
    }

    private fun isAudioTrackPlaying(): Boolean {
        return isAudioTrackValid() && mAudioTrack!!.playState == AudioTrack.PLAYSTATE_PLAYING
    }

    private fun isAudioTrackPaused(): Boolean {
        return isAudioTrackValid() && mAudioTrack!!.playState == AudioTrack.PLAYSTATE_PAUSED
    }
}
