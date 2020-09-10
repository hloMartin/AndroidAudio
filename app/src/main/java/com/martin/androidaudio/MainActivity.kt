package com.martin.androidaudio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.martin.audiolib.log_d
import com.martin.audiolib.play.IPlayListener
import com.martin.audiolib.play.OpusAudioPlayer
import com.martin.audiolib.record.IRecordListener
import com.martin.audiolib.record.OpusAudioRecord
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var mAudioRecord: OpusAudioRecord? = null
    private var mAudioPlayer: OpusAudioPlayer? = null

    private var mRecordListener = object : IRecordListener {
        override fun onRecordStart(filePath: String) {
            log_d("[onRecordStart] filePath:$filePath")
        }

        override fun onRecordStop(filePath: String) {
            log_d("[onRecordStop] filePath:$filePath")
        }

        override fun onRecordError(filePath: String, code: Int, msg: String?) {
            log_d("[onRecordError] filePath:$filePath code:$code msg:$msg")
        }
    }

    private var mPlayListener = object : IPlayListener {
        override fun onPreparing(filePath: String) {
            log_d("[onPreparing] filePath:$filePath")
        }

        override fun onPrepared(filePath: String) {
            log_d("[onPrepared] filePath:$filePath")
        }

        override fun onStart(filePath: String) {
            log_d("[onStart] filePath:$filePath")
        }

        override fun onPause(filePath: String) {
            log_d("[onPause] filePath:$filePath")
        }

        override fun onStop(filePath: String) {
            log_d("[onStop] filePath:$filePath")
        }

        override fun onComplete(filePath: String) {
            log_d("[onComplete] filePath:$filePath")
        }

        override fun onProgressNotify(filePath: String, currentDuration: Long, duration: Long) {
            log_d("[onProgressNotify] filePath:$filePath currentDuration:$currentDuration  total:$duration")
        }


        override fun onError(filePath: String, code: Int, msg: String) {
            log_d("[onPreparing] filePath:$filePath code:$code msg:$msg")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAudioRecord = OpusAudioRecord(this)
        mAudioPlayer = OpusAudioPlayer()

        var filePath = "/sdcard/martin/test.opus"

        btn_start_record.setOnClickListener {
            mAudioRecord?.startRecord(filePath)
        }
        btn_stop_record.setOnClickListener {
            mAudioRecord?.stopRecord()
        }

        btn_play.setOnClickListener {
            //NOET：tagId 唯一标识，可以用来查询指定语音是否处于播放状态
            mAudioPlayer?.play(filePath, 0)
        }
        btn_stop.setOnClickListener {
            mAudioPlayer?.stop()
        }
        btn_pause.setOnClickListener {
            mAudioPlayer?.pause()
        }
        btn_resume.setOnClickListener {
            mAudioPlayer?.resume()
        }

        mAudioRecord?.addRecordListener(mRecordListener)

        mAudioPlayer?.addPlayListener(mPlayListener)

    }
}