package nercms.schedule.activity;

import java.io.File;

import nercms.schedule.R;
import nercms.schedule.view.AudioRecorder;
import nercms.schedule.view.RecordButton.RecordListener;
import nercms.schedule.view.RecordStrategy;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class RecordActivity extends BaseActivity implements OnClickListener {

	ImageView mImage;

	// ¼����·��
	private String audiopath;

	private boolean isShow = false;
	private Button mStart;
	private Button mStop;

	private static final int MIN_RECORD_TIME = 1; // ���¼��ʱ�䣬��λ��
	private static final int RECORD_OFF = 0; // ����¼��
	private static final int RECORD_ON = 1; // ����¼��

	private RecordStrategy mAudioRecorder;
	private Thread mRecordThread;
	private RecordListener listener;

	private int recordState = 0; // ¼��״̬
	private float recodeTime = 0.0f; // ¼��ʱ�������¼��ʱ��̫����¼��ʧ��
	private boolean isCanceled = false; // �Ƿ�ȡ��¼��

	private final float MAX_TIME = 60.0f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_record);

		iniActionBar(true, null, "�����ϴ�");
		com.actionbarsherlock.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mImage = (ImageView) findViewById(R.id.img_record);

		mStart = (Button) findViewById(R.id.btn_record);
		mStop = (Button) findViewById(R.id.stop_record);

		cho = (Chronometer) findViewById(R.id.chro);
		cho.setFormat("%s");

		mStart.setOnClickListener(this);
		mStop.setOnClickListener(this);

		mAudioRecorder = new AudioRecorder();

		mImage.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				audiopath = AudioRecorder.path;
				File file = new File(audiopath);
				Uri uri = Uri.fromFile(file);
				Intent intent1 = new Intent(Intent.ACTION_MAIN);
				intent1.setAction(Intent.ACTION_DEFAULT);
				intent1.setDataAndType(uri, "audio/*");
				startActivity(intent1);

			}
		});

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				mAudioRecorder.stop();
				mRecordThread.interrupt();

				mImage.setVisibility(View.VISIBLE);

				isShow = true;
				invalidateOptionsMenu();
				// showWarnToast("�ﵽ���¼��ʱ����¼������");
				// Toast.makeText(RecordActivity.this, "�ﵽ���¼��ʱ����¼������",
				// Toast.LENGTH_SHORT).show();
				cho.stop();

				mStop.setVisibility(View.GONE);
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.record_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		case R.id.action_sure:
			audiopath = AudioRecorder.path;
			Intent intent = new Intent();
			intent.putExtra("path", audiopath);
			System.out.println("RecordActivity audiopath: " + audiopath);
			setResult(RESULT_OK, intent);
			finish();
			break;

		}

		return super.onOptionsItemSelected(item);
	}

	/*
	 * onCreateOptionsMenu�� ֻ�����һ�Σ���ֻ����Menu��ʾ֮ǰȥ����һ�Σ�֮��Ͳ�����ȥ���á�
	 * 
	 * onPrepareOptionsMenu��
	 * 
	 * onPrepareOptionsMenu��ÿ����display
	 * Menu֮ǰ������ȥ���ã�ֻҪ��һ��Menu���I���ͻ����һ�Ρ����Կ��������ﶯ̬�ĸı�menu��
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (isShow) {
			menu.findItem(R.id.action_sure).setVisible(true);
		} else {
			menu.findItem(R.id.action_sure).setVisible(false);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_record:
			if (recordState != RECORD_ON) {
				if (mAudioRecorder != null) {
					mAudioRecorder.ready();
					recordState = RECORD_ON;
					mAudioRecorder.start();
					callRecordTimeThread();

					showWarnToast("��ʼ¼��");

					isShow = false;
					mImage.setVisibility(View.INVISIBLE);

					// ���²������˵�
					invalidateOptionsMenu();

					cho.setBase(SystemClock.elapsedRealtime());
					cho.start();

					mStart.setVisibility(View.GONE);
					mStop.setVisibility(View.VISIBLE);
				}

			}
			break;

		case R.id.stop_record:
			if (recordState == RECORD_ON) {
				recordState = RECORD_OFF;
				mAudioRecorder.stop();
				mRecordThread.interrupt();
				if (recodeTime < MIN_RECORD_TIME) {
					showWarnToast("ʱ��̫��  ¼��ʧ��");
					mAudioRecorder.deleteOldFile();
				}
				// else {
				// if (mListener!=null) {// ����callbackActivity������¼��
				//
				// mListener.onFinished(mAudioRecorder.getFilePath());
				// }

				// }

				mImage.setVisibility(View.VISIBLE);

				isShow = true;
				invalidateOptionsMenu();
				showWarnToast("¼������");

				cho.stop();

				mStop.setVisibility(View.GONE);
			}
			break;

		default:
			break;
		}
	}

	// ¼��ʱ��̫��ʱToast��ʾ
	private void showWarnToast(String toastText) {

		Toast.makeText(RecordActivity.this, toastText, Toast.LENGTH_SHORT).show();
	}

	// ����¼����ʱ�߳�
	private void callRecordTimeThread() {
		mRecordThread = new Thread(recordThread);
		mRecordThread.start();
	}

	// ¼���߳�
	private Runnable recordThread = new Runnable() {

		@Override
		public void run() {
			recodeTime = 0.0f;
			while (recordState == RECORD_ON) {
				{
					try {
						Thread.sleep(100);
						recodeTime += 0.1;

						if (recodeTime > MAX_TIME) {
							handler.sendEmptyMessage(0);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}
	};

	private Chronometer cho;

	private Handler handler;

}
