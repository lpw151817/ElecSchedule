package nercms.schedule.activity;

import java.io.File;

import nercms.schedule.R;
import nercms.schedule.view.AudioRecorder;
import nercms.schedule.view.RecordButton;
import nercms.schedule.view.RecordButton.AudioFinishRecorderListener;
import nercms.schedule.view.RecordButton.AudioStartRecorderListener;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class RecordActivity extends SherlockActivity {

	RecordButton mRecord;
	ImageView mImage;

	// ¼����·��
	private String audiopath;

	private boolean isShow = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_record);
		com.actionbarsherlock.app.ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mRecord = (RecordButton) findViewById(R.id.btn_record);
		mImage = (ImageView) findViewById(R.id.img_record);
		
		

		mRecord.setAudioRecord(new AudioRecorder());
		mRecord.setAudioFinishRecorderListener(new AudioFinishRecorderListener() {

			@Override
			public void onFinished(String filePath) {
				Log.e("TAG", "�ص��ɹ�");
				mImage.setVisibility(View.VISIBLE);
				
				isShow = true;
				invalidateOptionsMenu();
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

			}
		});

		mRecord.setAudioStartRecorderListener(new AudioStartRecorderListener() {

			@Override
			public void onStart() {
				isShow = false;
				mImage.setVisibility(View.INVISIBLE);
				
				//���²������˵�
				invalidateOptionsMenu();
			}
		});
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
			System.out.println("RecordActivity audiopath: "+ audiopath);
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
}
