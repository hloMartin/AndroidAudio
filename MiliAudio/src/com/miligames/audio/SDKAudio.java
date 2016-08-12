package com.miligames.audio;

import java.io.File;
import java.io.IOException;

import org.json.JSONObject;

import com.czt.mp3recorder.MP3Recorder;
import com.miligames.utils.DeviceInfo;
import com.miligames.utils.SDKLog;
import com.miligames.utils.UCommUtil;
import com.miligames.utils.http.HttpInvoker;
import com.miligames.utils.http.HttpInvoker.OnResponsetListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class SDKAudio {

	private static final String TAG = SDKAudio.class.toString();

	private static final String AUDIO_DIR = "/mili/game/audio/";
	private static final String AUDIO_SUFFIX_MP3 = ".mp3";

	private static final long MIN_AUDIO_CALLBACK_INTERVAL = 50; // 回调音频播放信息的最小时间间隔（单位毫秒）

	/** 录音文件 */
	private String mAudioPath; // 录音文件
	/** 录音对象 */
	private MP3Recorder mMp3Recorer;// 录音对象
	/** 播放器 */
	private MediaPlayer mPlayer;// 播放器
	/** 标记当前是否处于录音过程 */
	private boolean mIsRecording;// 标记当前是否处于录音过程

	/** 上下文环境 */
	private Context mContext;
	/** 定义单例 */
	private static SDKAudio mInstance;// 定义单例

	private SDKAudio() {
		mAudioPath = "";
		mIsRecording = false;
		mMp3Recorer = null;
	}

	/**
	 * 定义单例
	 * 
	 * @return MiliSDK 返回单例
	 */
	protected static SDKAudio getInstance() {
		if (mInstance == null) {
			mInstance = new SDKAudio();
		}
		return mInstance;
	}

	/**
	 * 设置上下文环境
	 * 
	 * @param context 游戏上下文环境
	 */
	protected void setContext(Context context) {
		mContext = context;
	}

	/**
	 * 录音开始
	 */
	@SuppressLint("InlinedApi")
	protected void startRecord() {
		if (mIsRecording) {
			SDKLog.d(TAG, "Audio is already on recording...You need to stop it first");
			return;
		}
		mAudioPath = DeviceInfo.getSdPath() + AUDIO_DIR + System.currentTimeMillis() + AUDIO_SUFFIX_MP3;
		File file = new File(mAudioPath);
		if (!file.getParentFile().exists()) {
			if (!file.getParentFile().mkdir()) {
				SDKLog.w(TAG, "create dir failed:" + file.getParent());
			}
		}
		SDKLog.d(TAG, "start record ...");
		mMp3Recorer = new MP3Recorder(file);
		try {
			mMp3Recorer.start();
			mIsRecording = true;
		} catch (Exception e) {
			SDKLog.e(TAG, "record audio error", e);
		}
	}

	/**
	 * 停止录音
	 */
	protected void stopRecord() {
		SDKLog.d(TAG, "stop record ...");
		if (mIsRecording) {
			mMp3Recorer.stop();
			mIsRecording = false;
		}
	}

	/**
	 * 将录制好的音频上传到服务器，并通过回调回传服务器上音频的 url 和一些音频相关的其他信息
	 * 
	 * @param listener 录音回调
	 */
	protected void upload(final MiliAudio.UploadListener listener) {
		if (mIsRecording) {
			SDKLog.w(TAG, "Audio is already on recording...You need to stop it first");
			return;
		}
		SDKLog.d(TAG, "start to upload record file");
		new Thread() {
			public void run() {
				File file = new File(mAudioPath);
				if (!file.exists()) {
					SDKLog.w(TAG, "upload file is not exists:" + file.getAbsolutePath());
					return;
				}

				final long duration = getDuration(mAudioPath);

				String fileName = file.getName().substring(0, file.getName().length() - AUDIO_SUFFIX_MP3.length());
				String newFileName = UCommUtil.md5(fileName);
				String newFilePath = file.getAbsolutePath().replace(fileName, newFileName);
				final File newFile = new File(newFilePath);
				file.renameTo(newFile);
				try {
					new HttpInvoker().upload(ConfigManager.getInstance(mContext).getAudioUploadUrl(), newFilePath,
							new OnResponsetListener() {
								@Override
								public void OnResponse(String result) {
									try {
										JSONObject data = new JSONObject(result);
										if (data.getInt("status") == 0) {
											newFile.delete();
											data.put("duration", duration);
											listener.onFinish(data.toString());
										} else {
											SDKLog.w(TAG, "upload record file error:" + result);
										}
									} catch (Exception e) {
										SDKLog.e(TAG, "upload onResonse error", e);
									}
								}
							});
				} catch (Exception e) {
					SDKLog.e(TAG, "upload mp3 file failed", e);
				}
			};
		}.start();
	}

	/**
	 * 获取音频文件的时长
	 * 
	 * @param filePath
	 * @return
	 */
	private long getDuration(String filePath) {
		long duration = 0;
		try {
			MediaPlayer player = new MediaPlayer();
			player.setDataSource(filePath);
			player.prepare();
			duration = player.getDuration();
			player.release();
			player = null;
		} catch (Exception e) {
			SDKLog.e(TAG, "get audio duration error", e);
		}
		return duration;
	}

	/**
	 * 播放，指定路径的音频文件
	 * 
	 * @param path 播放文件的路径
	 */
	protected void startPlay(String path) {
		if (mPlayer != null) {
			stopPlay();
		}
		SDKLog.d(TAG, "start to play audio:" + path);
		mPlayer = new MediaPlayer();
		try {
			mPlayer.setDataSource(path);
			mPlayer.prepare();
			mPlayer.start();
			final long duration = mPlayer.getDuration();
			mPlayer.setOnCompletionListener(new OnCompletionListener() {
				public void onCompletion(MediaPlayer mp) {
					try {
						if (mp != null) {
							mPlayer.stop();
							mPlayer.prepare();
							mPlayer.release();
							mPlayer = null;
						}
					} catch (IOException e) {
						SDKLog.e(TAG, "on record play completion error", e);
					}
				}
			});
		} catch (IOException e) {
			SDKLog.e(TAG, "play audio error:" + path, e);
		}
	}

	/**
	 * 关闭当前音频播放
	 */
	protected void stopPlay() {
		SDKLog.d(TAG, "stop to play current audio");
		if (mPlayer != null && mPlayer.isPlaying()) {
			mPlayer.stop();
			mPlayer.release();
			mPlayer = null;
		}
	}

}
