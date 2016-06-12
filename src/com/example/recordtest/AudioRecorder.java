package com.example.recordtest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.MediaRecorder;
import android.os.Environment;

public class AudioRecorder// implements RecordImp
{

	private MediaRecorder recorder;
	private String fileName;
	private String downloadFileFolder = "";
	private String uploadFileFolder = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/nercms-Schedule/Attachments/";

	private boolean isRecording = false;

	public String getFileName() {
		return fileName;
	}

	// @Override
	public void ready() {
		File file = new File(uploadFileFolder);
		if (!file.exists())
			file.mkdir();

		fileName = getCurrentDate() + ".amr";
		recorder = new MediaRecorder();
		recorder.setOutputFile(uploadFileFolder + "/" + fileName);
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);// 设置MediaRecorder的音频源为麦克风
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);// 设置MediaRecorder录制的音频格式
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// 设置MediaRecorder录制音频的编码为amr

	}

	// 以当前时间作为文件名
	private String getCurrentDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		String str = formatter.format(curDate);
		return str;
	}

	public void start() {
		if (!isRecording) {
			try {
				recorder.prepare();
				recorder.start();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			isRecording = true;
		}

	}

	public void stop() {
		if (isRecording) {
			recorder.stop();
			recorder.release();
			isRecording = false;
		}

	}

	public void deleteOldFile() {
		File file = new File(uploadFileFolder + "/" + fileName);
		file.deleteOnExit();
	}

	// 获取录音音量的大小
	public double getAmplitude() {
		if (!isRecording)
			return 0;

		return recorder.getMaxAmplitude();
	}

}
