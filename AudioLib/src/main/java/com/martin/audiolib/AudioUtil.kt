package com.martin.audiolib

import android.text.TextUtils
import android.util.Log
import java.io.File

val TAG = "AudioLib"

const val ERROR_FILE_PATH_EMPTY = 1
const val ERROR_NO_RECORD_PERMISSION = 2
const val ERROR_LIB_SO_LOAD_FAILED = 3
const val ERROR_FILE_WRITE_EXCEPTION = 4
const val ERROR_FILE_NOT_EXIST = 10
//不支持的播放文件类型
const val ERROR_PLAY_FILE_TYPE_NOT_SUPPORT = 11
const val ERROR_PLAY_FILE_OPEN_ERROR = 12
const val ERROR_PLAY_FILE_IS_EMPTY =13
const val ERROR_PLAY_EXCEPTION =14

const val PLAY_STATE_IDLE = 0
const val PLAY_STATE_INITIALIZED = 1
const val PLAY_STATE_PREPARING = 2
const val PLAY_STATE_PREPARED = 3
const val PLAY_STATE_RUNNING = 4
const val PLAY_STATE_PAUSE = 5

fun log_d(log: String) {
    Log.d(TAG, log)
}

fun log_e(log: String) {
    Log.e(TAG, log)
}

fun isFileExists(filePath: String?): Boolean {
    return if (!TextUtils.isEmpty(filePath)) {
        File(filePath).exists()
    } else false
}