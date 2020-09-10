package com.martin.audiolib

import java.nio.ByteBuffer

class OpusTool {

    companion object {
        const val READ_OPUS_FINISHED = 1
        private const val START_RECORD_SUC = 1
        private const val WRITE_RECORD_FRAME_SUC = 1
        private const val OPEN_RECORD_SUC = 1
        private const val IS_OPUS_FILE_FORMAT = 1

        var isLoadSucceed = false
            private set

        init {
            isLoadSucceed = try {
                System.loadLibrary("opustool")
                true
            } catch (e: Exception) {
                log_d("load library opus failed,${e.message}")
                false
            }
        }

        fun convertPcm2NormalDuration(pcmDuration: Long): Long {
            return (pcmDuration / 48.0).toLong()
        }
    }

    private external fun startRecord(path: String): Int
    private external fun writeFrame(frame: ByteBuffer, len: Int): Int
    private external fun stopRecord()
    private external fun openOpusFile(path: String): Int
    private external fun seekOpusFile(position: Float): Int
    private external fun isOpusFile(path: String): Int
    private external fun closeOpusFile()
    private external fun readOpusFile(buffer: ByteBuffer, capacity: Int, args: IntArray)
    private external fun getTotalPcmDuration(): Long
    external fun getWaveform(path: String?): ByteArray?
    external fun getWaveform2(array: ShortArray?, length: Int): ByteArray?

    fun startOpusRecord(filePath: String?): Boolean {
        return startRecord(filePath!!) == START_RECORD_SUC
    }

    fun writeOpusFrame(frame: ByteBuffer?, len: Int): Boolean {
        return writeFrame(frame!!, len) == WRITE_RECORD_FRAME_SUC
    }

    fun stopOpusRecord() {
        stopRecord()
    }

    fun openFile(path: String?): Boolean {
        return openOpusFile(path!!) != 0
    }

    fun seekFile(position: Float): Int {
        return seekOpusFile(position)
    }

    fun isOpusFileFormat(path: String?): Boolean {
        try {
            return isOpusFile(path!!) == IS_OPUS_FILE_FORMAT
        } catch (e: Throwable) {
            log_e("[isOpusFile] error, msg:${e.message}")
        }
        return false
    }

    fun closeFile() {
        closeOpusFile()
    }

    fun readFile(buffer: ByteBuffer?, capacity: Int, args: IntArray?) {
        readOpusFile(buffer!!, capacity, args!!)
    }

    fun getPcmDuration(): Long {
        return getTotalPcmDuration()
    }

    fun getDuration(): Long {
        return convertPcm2NormalDuration(getTotalPcmDuration())
    }

    fun getPcmWaveForm(path: String?): ByteArray? {
        return getWaveform(path)
    }

    fun getPcmWaveform2(array: ShortArray?, length: Int): ByteArray? {
        return getWaveform2(array, length)
    }

}