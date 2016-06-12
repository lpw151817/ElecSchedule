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
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);// ����MediaRecorder����ƵԴΪ��˷�
		recorder.setOutputFormat(MediaRecorder.OutputFormat.RAW_AMR);// ����MediaRecorder¼�Ƶ���Ƶ��ʽ
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);// ����MediaRecorder¼����Ƶ�ı���Ϊamr

	}

	// �Ե�ǰʱ����Ϊ�ļ���
	private String getCurrentDate() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
		Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
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

	// ��ȡ¼�������Ĵ�С
	public double getAmplitude() {
		if (!isRecording)
			return 0;

		return recorder.getMaxAmplitude();
	}

}
