package nercms.schedule.view;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import nercms.schedule.activity.NewTask;

import android.media.MediaRecorder;
import android.os.Environment;

public class AudioRecorder implements RecordStrategy {

	private MediaRecorder recorder;
	private String fileName;
//	private String fileFolder = Environment.getExternalStorageDirectory().getPath() + "/TestRecord";
	private String fileFolder = NewTask.fileFolder;

	private boolean isRecording = false;

	public static String path = "";

	@Override
	public void ready() {
		File file = new File(fileFolder);
		if (!file.exists()) {
			file.mkdir();
		}
		fileName = getCurrentDate();
		recorder = new MediaRecorder();

		path = fileFolder + "/" + fileName + ".amr";

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

	@Override
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

	@Override
	public void stop() {
		if (isRecording) {
			recorder.stop();
			recorder.release();
			isRecording = false;
		}

	}

	@Override
	public void deleteOldFile() {
		 File file = new File(fileFolder + "/" + fileName + ".amr");
		// file.deleteOnExit();

//		String path = fileFolder + "/" + fileName + ".mp3";
//		// ��ȡ�ļ����жϴ������
//		File file = new File(path);
		 
		if (!file.exists()) {
			// return true;
		}
		// ɾ���ļ�
		if (file.delete()) {
			// return true;
		} else {
			// return false;
		}
	}

	@Override
	public double getAmplitude() {
		if (!isRecording) {
			return 0;
		}
		return recorder.getMaxAmplitude();
	}

	@Override
	public String getFilePath() {
//		return fileFolder + "/" + fileName + ".mp3";
		return fileFolder + "/" + fileName + ".amr";
	}

}
