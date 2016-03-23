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

	// 录音的路径
	private String audiopath;

	private boolean isShow = false;
	private Button mStart;
	private Button mStop;

	private static final int MIN_RECORD_TIME = 1; // 最短录音时间，单位秒
	private static final int RECORD_OFF = 0; // 不在录音
	private static final int RECORD_ON = 1; // 正在录音

	private RecordStrategy mAudioRecorder;
	private Thread mRecordThread;
	private RecordListener listener;

	private int recordState = 0; // 录音状态
	private float recodeTime = 0.0f; // 录音时长，如果录音时间太短则录音失败
	private boolean isCanceled = false; // 是否取消录音

	private final float MAX_TIME = 60.0f;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_record);

		iniActionBar(true, null, "附件上传");
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
				// showWarnToast("达到最大录音时长，录音结束");
				// Toast.makeText(RecordActivity.this, "达到最大录音时长，录音结束",
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
	 * onCreateOptionsMenu： 只会调用一次，他只会在Menu显示之前去调用一次，之后就不会在去调用。
	 * 
	 * onPrepareOptionsMenu：
	 * 
	 * onPrepareOptionsMenu是每次在display
	 * Menu之前，都会去调用，只要按一次Menu按鍵，就会调用一次。所以可以在这里动态的改变menu。
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

					showWarnToast("开始录音");

					isShow = false;
					mImage.setVisibility(View.INVISIBLE);

					// 更新操作栏菜单
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
					showWarnToast("时间太短  录音失败");
					mAudioRecorder.deleteOldFile();
				}
				// else {
				// if (mListener!=null) {// 并且callbackActivity，保存录音
				//
				// mListener.onFinished(mAudioRecorder.getFilePath());
				// }

				// }

				mImage.setVisibility(View.VISIBLE);

				isShow = true;
				invalidateOptionsMenu();
				showWarnToast("录音结束");

				cho.stop();

				mStop.setVisibility(View.GONE);
			}
			break;

		default:
			break;
		}
	}

	// 录音时间太短时Toast显示
	private void showWarnToast(String toastText) {

		Toast.makeText(RecordActivity.this, toastText, Toast.LENGTH_SHORT).show();
	}

	// 开启录音计时线程
	private void callRecordTimeThread() {
		mRecordThread = new Thread(recordThread);
		mRecordThread.start();
	}

	// 录音线程
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
