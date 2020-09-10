package com.martin.audiolib.record

/**
 * 回调方法是在主线程执行的
 */
interface IRecordListener {
    fun onRecordStart(filePath: String)
    fun onRecordStop(filePath: String)
    fun onRecordError(filePath: String, code: Int, msg: String?)
}

interface IRecord {
    fun startRecord(filePath: String)
    fun stopRecord()
    fun isRecording(): Boolean
    fun getRecordDuration(): Long
    fun getRecordPath(): String?

    fun addRecordListener(listener: IRecordListener?)
    fun removeRecordListener(listener: IRecordListener?)
}
