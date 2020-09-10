package com.martin.audiolib.play

interface IPlayListener {
    fun onPreparing(filePath: String)
    fun onPrepared(filePath: String)
    fun onStart(filePath: String)
    fun onPause(filePath: String)
    fun onStop(filePath: String)
    fun onComplete(filePath: String)
    fun onProgressNotify(filePath: String, currentDuration: Long, duration: Long)
    fun onError(filePath: String, code: Int, msg: String)
}

interface IPlay {
    fun play(url: String, tagId: Long)
    fun play(url: String, targetPosition: Int, tagId: Long)
    fun pause()
    fun resume()
    fun stop()
    fun isSupportSeekTo():Boolean
    fun seekTo(progress: Int)
    fun getDuration(): Int
    fun getCurrentPosition(): Long
    fun getPlayPath():String
    fun isPlaying(): Boolean
    fun getState(): Int
    fun addPlayListener(listener: IPlayListener?)
    fun removePlayListener(listener: IPlayListener?)
}