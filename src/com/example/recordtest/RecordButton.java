package com.example.recordtest;

import com.example.recordtest.RecordButton.RecordListener;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import nercms.schedule.R;

public class RecordButton extends Button {

	private static final float MIN_RECORD_TIME = 1.0f; // 最短录制时间，单位秒，0为无时间限制
	private static final int RECORD_OFF = 0; // 不在录音
	private static final int RECORD_ON = 1; // 正在录音

	private Dialog mRecordDialog;
	private AudioRecorder mAudioRecorder;
	private Thread mRecordThread;
	private RecordListener listener;

	private int recordState = 0; // 录音状态
	private float recodeTime = 0.0f; // 录音时长，如果录音时间太短则录音失败
	private double voiceValue = 0.0; // 录音的音量值
	private float downY;

	private ImageView dialogImg;
	private Context mContext;

	public RecordButton(Context context) {
		super(context);
		init(context);
	}

	public RecordButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public RecordButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
		this.setText("长按说话");
	}

	public void setAudioRecord(AudioRecorder record) {
		this.mAudioRecorder = record;
	}

	public void setRecordListener(RecordListener listener) {
		this.listener = listener;
	}

	// 录音时显示Dialog
	private void showVoiceDialog() {
		if (mRecordDialog == null) {
			mRecordDialog = new Dialog(mContext, R.style.Dialogstyle);
			mRecordDialog.setContentView(R.layout.dialog_record);
			dialogImg = (ImageView) mRecordDialog.findViewById(R.id.record_dialog_img);
		}

		dialogImg.setImageResource(R.drawable.record_cancel);
		this.setText("松开 发送");

		mRecordDialog.show();
	}

	// 录音时间太短时Toast显示
	private void showWarnToast(String toastText) {
		Toast.makeText(mContext, toastText, Toast.LENGTH_LONG).show();

		// Toast toast = new Toast(mContext);
		// View warnView =
		// LayoutInflater.from(mContext).inflate(R.layout.toast_warn, null);
		// toast.setView(warnView);
		// toast.setGravity(Gravity.CENTER, 0, 0);// 起点位置为中间
		// toast.show();
	}

	// 开启录音计时线程
	private void callRecordTimeThread() {
		mRecordThread = new Thread(recordThread);
		mRecordThread.start();
	}

	// 录音Dialog图片随录音音量大小切换
	private void setDialogImage() {
		if (voiceValue < 600.0) {
			dialogImg.setImageResource(R.drawable.record_animate_01);
		} else if (voiceValue > 600.0 && voiceValue < 1000.0) {
			dialogImg.setImageResource(R.drawable.record_animate_02);
		} else if (voiceValue > 1000.0 && voiceValue < 1200.0) {
			dialogImg.setImageResource(R.drawable.record_animate_03);
		} else if (voiceValue > 1200.0 && voiceValue < 1400.0) {
			dialogImg.setImageResource(R.drawable.record_animate_04);
		} else if (voiceValue > 1400.0 && voiceValue < 1600.0) {
			dialogImg.setImageResource(R.drawable.record_animate_05);
		} else if (voiceValue > 1600.0 && voiceValue < 1800.0) {
			dialogImg.setImageResource(R.drawable.record_animate_06);
		} else if (voiceValue > 1800.0 && voiceValue < 2000.0) {
			dialogImg.setImageResource(R.drawable.record_animate_07);
		} else if (voiceValue > 2000.0 && voiceValue < 3000.0) {
			dialogImg.setImageResource(R.drawable.record_animate_08);
		} else if (voiceValue > 3000.0 && voiceValue < 4000.0) {
			dialogImg.setImageResource(R.drawable.record_animate_09);
		} else if (voiceValue > 4000.0 && voiceValue < 6000.0) {
			dialogImg.setImageResource(R.drawable.record_animate_10);
		} else if (voiceValue > 6000.0 && voiceValue < 8000.0) {
			dialogImg.setImageResource(R.drawable.record_animate_11);
		} else if (voiceValue > 8000.0 && voiceValue < 10000.0) {
			dialogImg.setImageResource(R.drawable.record_animate_12);
		} else if (voiceValue > 10000.0 && voiceValue < 12000.0) {
			dialogImg.setImageResource(R.drawable.record_animate_13);
		} else if (voiceValue > 12000.0) {
			dialogImg.setImageResource(R.drawable.record_animate_14);
		}
	}

	// 录音线程
	private Runnable recordThread = new Runnable() {

		@Override
		public void run() {
			long start = System.currentTimeMillis();//fym
			
			recodeTime = 0.0f;
			while (recordState == RECORD_ON)
			{
				{
					try {
						Thread.sleep(100);
						recodeTime += 0.1;
						// 获取音量，更新dialog
						voiceValue = mAudioRecorder.getAmplitude();
						recordHandler.sendEmptyMessage(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				if(30000 < System.currentTimeMillis() - start)
				{
					mAudioRecorder.stop();
					voiceValue = 0.0f;
					recordHandler.sendEmptyMessage(1);
					recordHandler.sendEmptyMessage(2);
					break;
				}
			}
		}
	};

	@SuppressLint("HandlerLeak")
	private Handler recordHandler = new Handler() {
		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what)
			{
				case 1:
					setDialogImage();
					break;
					
				case 2:
					Toast.makeText(mContext, "单次录音时间不超过30秒", Toast.LENGTH_LONG).show();
					break;
			}
		}
	};
	
	private void record_button_up()
	{
		if (recordState == RECORD_ON)
		{
			recordState = RECORD_OFF;

			if (mRecordDialog.isShowing())
				mRecordDialog.dismiss();

			mAudioRecorder.stop();
			mRecordThread.interrupt();
			voiceValue = 0.0;
			if (recodeTime < MIN_RECORD_TIME)
			{
				showWarnToast("录音时间太短,不发送!");
				mAudioRecorder.deleteOldFile();
			}
			else
			{
				if (listener != null)
					listener.recordEnd(mAudioRecorder.getFileName());
			}
			this.setText("长按说话");
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN: // 按下按钮
			if (recordState != RECORD_ON)
			{
				showVoiceDialog();
				downY = event.getY();
				if (mAudioRecorder != null)
				{
					mAudioRecorder.ready();
					recordState = RECORD_ON;
					mAudioRecorder.start();
					callRecordTimeThread();
				}
			}
			break;

		case MotionEvent.ACTION_CANCEL:// 提示获取录音权限时无法正常松开
		case MotionEvent.ACTION_UP: // 松开手指
			if (recordState == RECORD_ON)
			{
				recordState = RECORD_OFF;

				if (mRecordDialog.isShowing())
					mRecordDialog.dismiss();

				mAudioRecorder.stop();
				mRecordThread.interrupt();
				voiceValue = 0.0;
				if (recodeTime < MIN_RECORD_TIME)
				{
					showWarnToast("录音时间太短,不发送!");
					mAudioRecorder.deleteOldFile();
				}
				else
				{
					if (listener != null)
						listener.recordEnd(mAudioRecorder.getFileName());
				}
				this.setText("长按说话");
			}
			break;
		}
		return true;
	}

	public interface RecordListener {
		public void recordEnd(String fileName);
	}
}
