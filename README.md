# AndroidAudio
封装好的音频模块，有录制音频和播放音频的接口（音频格式为 opus ）

so 文件是使用 [opus 源码](https://opus-codec.org/downloads/) 加上一些处理编译生成的 so 文件


## 导入
项目根目录下添加 jitpack 仓库
```
...
allprojects {
    repositories {
        ...
        //添加 jitpack 仓库
        maven { url 'https://jitpack.io' }
    }
}
...
```
APP 模块的 build.gradle 引用
```
implementation 'com.github.hloMartin:AndroidAudio:1.0.0'
```

## 录制音频
开始录制
```kotlin
var mAudioRecord = OpusAudioRecord(context)
//传入音频文件生成路径
mAudioRecord.startRecord(filePath)
```
停止录制
```kotlin
mAudioRecord.stopRecord()
```
设置录制监听
```kotlin
var mRecordListener = object : IRecordListener {
        override fun onRecordStart(filePath: String) {
        }

        override fun onRecordStop(filePath: String) {
        }

        override fun onRecordError(filePath: String, code: Int, msg: String?) {
        }
    }
mAudioRecord?.addRecordListener(mRecordListener)
```


## 播放音频

开始播放
```kotlin
var mAudioPlayer = OpusAudioPlayer()
//filePath:指定的 0 音频文件 | 0：tagId 唯一标识，可用于查询指定音频是否播放
mAudioPlayer.play(filePath, 0)
```
暂停播放
```kotlin
mAudioPlayer.pause()
```
继续播放
```kotlin
mAudioPlayer.resume()
```
停止播放
```kotlin
mAudioPlayer.stop()
```

设置播放监听，查看播放进度
```kotlin
var mPlayListener = object : IPlayListener {
        override fun onPreparing(filePath: String) {
        }

        override fun onPrepared(filePath: String) {
        }

        override fun onStart(filePath: String) {
        }

        override fun onPause(filePath: String) {
        }

        override fun onStop(filePath: String) {
        }

        override fun onComplete(filePath: String) {
        }

        override fun onProgressNotify(filePath: String, currentDuration: Long, duration: Long) {
        }

        override fun onError(filePath: String, code: Int, msg: String) {
        }
    }
mAudioPlayer.addPlayListener(mPlayListener)

```