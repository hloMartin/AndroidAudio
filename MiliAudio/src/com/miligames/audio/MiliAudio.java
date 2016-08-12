package com.miligames.audio;

import com.miligames.utils.SDKLog;

import android.content.Context;

public class MiliAudio {

	private static final String TAG = MiliAudio.class.getName();

	public interface UploadListener {
		public void onFinish(String result);
	}

	public static void init(Context context) {
		SDKLog.d(TAG, "audio module init...");
		SDKAudio.getInstance().setContext(context);
	}

	/**
	 * 录音开始
	 */
	public static void startRecord() {
		SDKAudio.getInstance().startRecord();
	}

	/**
	 * 停止录音,生成录音文件
	 */
	public static void stopRecord() {
		SDKAudio.getInstance().stopRecord();
	}

	/**
	 * 播放指定url的音频文件
	 * 
	 * @param path 音频文件的路径
	 */
	public static void startPlay(String path) {
		SDKAudio.getInstance().startPlay(path);
	}

	/**
	 * 停止当前播放的音频
	 */
	public static void stopPlay() {
		SDKAudio.getInstance().stopPlay();
	}

	/**
	 * 上传音频，在调用 upload 先进行转码操作再执行 upload 操作
	 * 
	 * @param listener 回调函数，通过回调函数回调给游戏 uplaod之后的得到的音频url，还可以附加一些信息
	 */
	public static void upload(UploadListener listener) {
		SDKAudio.getInstance().upload(listener);
	}

}
