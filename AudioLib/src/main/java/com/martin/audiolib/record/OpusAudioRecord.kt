package com.martin.audiolib.record

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.martin.audiolib.ERROR_FILE_WRITE_EXCEPTION
import com.martin.audiolib.ERROR_LIB_SO_LOAD_FAILED
import com.martin.audiolib.OpusTool
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.max

class OpusAudioRecord(context: Context) : RecordWrapper(context) {

    private val WRITE_OPUS_FILE_BUFFER_SIZE = 1920
    private val AUDIO_RECORD_READ_BUFFER_MIN_SIZE = 4096

    private var mSampleRateInHz = 16000
    private var mAudioSource = MediaRecorder.AudioSource.MIC
    private var mChannelCount = AudioFormat.CHANNEL_IN_MONO
    private var mAudioFormat = AudioFormat.ENCODING_PCM_16BIT

    private var recordBufferSize = 0

    private var mOpusTool: OpusTool = OpusTool()

    override fun initAudioRecord(): AudioRecord? {
        recordBufferSize =
            AudioRecord.getMinBufferSize(mSampleRateInHz, mChannelCount, mAudioFormat)
        if (recordBufferSize <= 0) {
            recordBufferSize = 1280
        }
        return AudioRecord(
            mAudioSource,
            mSampleRateInHz,
            mChannelCount,
            mAudioFormat,
            recordBufferSize * 10
        )
    }

    override fun readAudioDataAndWrite2FileImpl(filePath: String?, audioRecord: AudioRecord?) {
        if (!OpusTool.isLoadSucceed) {
            notifyRecordError(filePath ?: "", ERROR_LIB_SO_LOAD_FAILED, "opus lib load failed")
            return
        }
        if (!mOpusTool.startOpusRecord(filePath)) {
            notifyRecordError(filePath ?: "", ERROR_FILE_WRITE_EXCEPTION, "start record failed")
            return
        }
        //回调录制开始
        notifyRecordStart(filePath!!)

        var readLength = 0
        var readStarted = false

        var writeFileBuffer = ByteBuffer.allocateDirect(WRITE_OPUS_FILE_BUFFER_SIZE)
        writeFileBuffer.order(ByteOrder.nativeOrder())
        writeFileBuffer.clear()

        var audioRecordBufferSize = max(recordBufferSize, AUDIO_RECORD_READ_BUFFER_MIN_SIZE)
        var audioRecordBuffer = ByteBuffer.allocateDirect(audioRecordBufferSize)
        audioRecordBuffer.order(ByteOrder.nativeOrder())
        audioRecordBuffer.clear()

        var writeFileSucceed = false
        while (audioRecord!!.read(audioRecordBuffer, audioRecordBuffer.limit())
                .also { readLength = it } > 0
        ) {
            if (!readStarted) {
                readStarted = true
            }
            //1. 将byteBuffer设置为读出来的模式
            audioRecordBuffer.limit(readLength)
            audioRecordBuffer.rewind()
            //2. 将数据写入到一个新的buffer
            //3. 写满后,开始写入到opus的文件
            writeFileSucceed = writeAudioBufferToFile(audioRecordBuffer, writeFileBuffer, false)
            //4. 再次设置为写入的模式即可 done
            audioRecordBuffer.clear()
            if (!writeFileSucceed) {
                setHasError(true)
                notifyRecordError(filePath, ERROR_FILE_WRITE_EXCEPTION, "write opus frame error")
                break
            }
        }
        //再次判断是否还有剩余的数据,如果存在,将剩余的数据,同样写入到文件中
        if (readStarted) {
            if (!writeFileSucceed) {
                writeAudioBufferToFile(audioRecordBuffer, writeFileBuffer, true)
            }
        }
        mOpusTool.stopOpusRecord()
    }

    private fun writeAudioBufferToFile(
        audioRecordBuffer: ByteBuffer,
        writeFileBuffer: ByteBuffer,
        forceFlush: Boolean
    ): Boolean {
        if (!forceFlush) {
            while (audioRecordBuffer.hasRemaining()) {
                var audioBufferOldLimit = -1

                //读取的数据量大于可以写入的数据量
                if (audioRecordBuffer.remaining() > writeFileBuffer.remaining()) {
                    audioBufferOldLimit = audioRecordBuffer.limit()
                    audioRecordBuffer.limit(audioRecordBuffer.position() + writeFileBuffer.remaining())
                }
                writeFileBuffer.put(audioRecordBuffer)
                //数据写满了
                if (writeFileBuffer.limit() == writeFileBuffer.position()) {
                    writeFileBuffer.flip()
                    var succeed = mOpusTool.writeOpusFrame(writeFileBuffer, writeFileBuffer.limit())
                    if (!succeed) {
                        return false
                    }
                    writeFileBuffer.clear()
                }
                if (audioBufferOldLimit > 0) {
                    audioRecordBuffer.limit(audioBufferOldLimit)
                }
            }
        } else {
            //有剩余未写入到文件的数据
            if (writeFileBuffer.position() > 0) {
                writeFileBuffer.flip()
                mOpusTool.writeOpusFrame(writeFileBuffer, writeFileBuffer.limit())
                writeFileBuffer.rewind()
            }
        }
        return true
    }

    override fun getFileSuffix(): String {
        return "opus"
    }

}