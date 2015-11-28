package nercms.schedule.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.wxapp.service.model.PhoneModel;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

/**
 * ����ͨ������
 * 
 * @author WEIHAO
 * @since 2014-8-5 �����������߼�������ʵ�֣������зŵķ������ܽӣ��Ҷϣ��߼���û���ȴ�������������
 * 
 */

public class VoiceCall extends BaseActivity {

	private String userID;// ����ID

	private RelativeLayout btnAnswerVoice;
	private RelativeLayout btnHangupVoice;

	private TextView tvCallstatus;

	// ��Ե�ͨ��ʱ
	private ImageView ivSpeakerImg;
	private TextView tvSpeakerName;

	// �����ˣ��Է���������ID
	// ���ڵ�Ե�ipͨ��
	private String speakerName = "";
	private String speakerID;

	private String startTime;

	// ��ʱ���ؼ�
	private Chronometer timer;
	// ͨ��ʱ�䣨�룩
	private int durationTime;

	// ��������:1-���룻2-����
	private int callType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.voice_call);

		callType = getIntent().getIntExtra("call_type", 2);
		speakerID = getIntent().getExtras().getString("callee_id");
		speakerName = getIntent().getExtras().getString("callee_name");

		userID = MySharedPreference.get(VoiceCall.this,
				MySharedPreference.USER_ID, null);

		// ��ʼ���绰��ʼʱ��
		startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date(System.currentTimeMillis()));

		initView();
	}

	private void initView() {
		timer = (Chronometer) findViewById(R.id.voice_call_timer);
		tvSpeakerName = (TextView) findViewById(R.id.voice_call_speaker_name_a);
		tvCallstatus = (TextView) findViewById(R.id.voice_call_status_tv);
		btnAnswerVoice = (RelativeLayout) findViewById(R.id.voice_call_answer_rl);
		btnHangupVoice = (RelativeLayout) findViewById(R.id.voice_call_hangup_rl);

		btnAnswerVoice.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ������������ؽ�����ť
				btnAnswerVoice.setVisibility(View.GONE);
				tvCallstatus.setVisibility(View.GONE);
				timer.setVisibility(View.VISIBLE);
				timer.setBase(SystemClock.elapsedRealtime());
				timer.start();
			}
		});
		btnHangupVoice.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ����Ҷ�
				String phoneID = Utils.producePhoneID(userID);
				String endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date(System.currentTimeMillis()));
				PhoneModel phone;
				// ���ж��Ǻ���绰�����Ǻ����绰
				if (callType == 1) { // ����
					// �ж��Ƿ�ʼͨ��
					if (timer.getVisibility() == View.VISIBLE) {
						// �ѿ�ʼͨ���������Ҷ�
						timer.stop();
						durationTime = (int) (SystemClock.elapsedRealtime() - timer
								.getBase());
						// callerID Ϊ������ID
						phone = new PhoneModel(phoneID, 1, Integer
								.parseInt(speakerID), Integer.parseInt(userID),
								startTime, 1, endTime, String
										.valueOf(durationTime), Constant.READ);
						phone.save(VoiceCall.this);
						Utils.showShortToast(VoiceCall.this, "ͨ������");

					} else {
						// δ��ʼͨ�����ܾ�����
						phone = new PhoneModel(phoneID, 1, Integer
								.parseInt(speakerID), Integer.parseInt(userID),
								startTime, 0, "", "", Constant.READ);
						phone.save(VoiceCall.this);
						Utils.showShortToast(VoiceCall.this, "�ܾ�����");
					}
				} else if (callType == 2) { // ����
					// �ж��Ƿ�ʼͨ��
					if (timer.getVisibility() == View.VISIBLE) {
						// �ѿ�ʼͨ���������Ҷ�
						timer.stop();
						durationTime = (int) (SystemClock.elapsedRealtime() - timer
								.getBase());
						// callerID Ϊ����ID
						phone = new PhoneModel(phoneID, 1, Integer
								.parseInt(userID), Integer.parseInt(speakerID),
								startTime, 1, endTime, String
										.valueOf(durationTime), Constant.READ);
						phone.save(VoiceCall.this);
						Utils.showShortToast(VoiceCall.this, "ͨ������");

					} else {
						// δ��ʼͨ����ȡ������
						phone = new PhoneModel(phoneID, 1, Integer
								.parseInt(userID), Integer.parseInt(speakerID),
								startTime, 0, "", "", Constant.READ);
						phone.save(VoiceCall.this);
						Utils.showShortToast(VoiceCall.this, "ȡ������");
					}
				}

				VoiceCall.this.finish();
			}
		});

		if (callType == 1) {
			// ���룬��ʾ�����͹Ҷ�
			btnAnswerVoice.setVisibility(View.VISIBLE);
			btnHangupVoice.setVisibility(View.VISIBLE);
		} else {
			// ������ֻ��ʾ�Ҷ�
			btnAnswerVoice.setVisibility(View.GONE);
			btnHangupVoice.setVisibility(View.VISIBLE);
		}

		tvSpeakerName.setText(speakerName);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// ���ؼ����
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setTitle("����ͨ����")
					.setPositiveButton("����",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// ִ�н���ͨ������
									btnHangupVoice.performClick();
								}
							}).setNegativeButton("ȡ��", null).create().show();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
