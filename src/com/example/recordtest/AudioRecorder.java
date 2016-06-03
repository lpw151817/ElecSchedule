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
	private String fileFolder = Environment.getExternalStorageDirectory().getPath() + "/TestRecord";

	private boolean isRecording = false;

	// @Override
	public void ready() {
		// TODO Auto-generated method stub
		File file = new File(fileFolder);
		if (!file.exists())
			file.mkdir();

		fileName = getCurrentDate();
		recorder = new MediaRecorder();
		recorder.setOutputFile(fileFolder + "/" + fileName + ".amr");
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

	// @Override
	public void start() {
		// TODO Auto-generated method stub
		if (!isRecording) {
			try {
				recorder.prepare();
				recorder.start();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
		// TODO Auto-generated method stub
		File file = new File(fileFolder + "/" + fileName + ".amr");
		file.deleteOnExit();
	}

	// ��ȡ¼�������Ĵ�С
	public double getAmplitude() {
		// TODO Auto-generated method stub
		if (!isRecording)
			return 0;

		return recorder.getMaxAmplitude();
	}

}
