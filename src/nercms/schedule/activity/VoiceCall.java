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
 * 语音通话界面
 * 
 * @author WEIHAO
 * @since 2014-8-5 本机操作的逻辑部分已实现；被呼叫放的反馈（拒接，挂断）逻辑还没，等待集成再做完善
 * 
 */

public class VoiceCall extends BaseActivity {

	private String userID;// 本人ID

	private RelativeLayout btnAnswerVoice;
	private RelativeLayout btnHangupVoice;

	private TextView tvCallstatus;

	// 点对点通话时
	private ImageView ivSpeakerImg;
	private TextView tvSpeakerName;

	// 发言人（对方）姓名和ID
	// 用于点对点ip通话
	private String speakerName = "";
	private String speakerID;

	private String startTime;

	// 计时器控件
	private Chronometer timer;
	// 通话时间（秒）
	private int durationTime;

	// 呼叫类型:1-呼入；2-呼出
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

		// 初始化电话开始时间
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
				// 点击接听后，隐藏接听按钮
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
				// 点击挂断
				String phoneID = Utils.producePhoneID(userID);
				String endTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date(System.currentTimeMillis()));
				PhoneModel phone;
				// 先判断是呼入电话，还是呼出电话
				if (callType == 1) { // 呼入
					// 判断是否开始通话
					if (timer.getVisibility() == View.VISIBLE) {
						// 已开始通话，正常挂断
						timer.stop();
						durationTime = (int) (SystemClock.elapsedRealtime() - timer
								.getBase());
						// callerID 为呼入者ID
						phone = new PhoneModel(phoneID, 1, Integer
								.parseInt(speakerID), Integer.parseInt(userID),
								startTime, 1, endTime, String
										.valueOf(durationTime), Constant.READ);
						phone.save(VoiceCall.this);
						Utils.showShortToast(VoiceCall.this, "通话结束");

					} else {
						// 未开始通话，拒绝接听
						phone = new PhoneModel(phoneID, 1, Integer
								.parseInt(speakerID), Integer.parseInt(userID),
								startTime, 0, "", "", Constant.READ);
						phone.save(VoiceCall.this);
						Utils.showShortToast(VoiceCall.this, "拒绝接听");
					}
				} else if (callType == 2) { // 呼出
					// 判断是否开始通话
					if (timer.getVisibility() == View.VISIBLE) {
						// 已开始通话，正常挂断
						timer.stop();
						durationTime = (int) (SystemClock.elapsedRealtime() - timer
								.getBase());
						// callerID 为本人ID
						phone = new PhoneModel(phoneID, 1, Integer
								.parseInt(userID), Integer.parseInt(speakerID),
								startTime, 1, endTime, String
										.valueOf(durationTime), Constant.READ);
						phone.save(VoiceCall.this);
						Utils.showShortToast(VoiceCall.this, "通话结束");

					} else {
						// 未开始通话，取消呼叫
						phone = new PhoneModel(phoneID, 1, Integer
								.parseInt(userID), Integer.parseInt(speakerID),
								startTime, 0, "", "", Constant.READ);
						phone.save(VoiceCall.this);
						Utils.showShortToast(VoiceCall.this, "取消呼叫");
					}
				}

				VoiceCall.this.finish();
			}
		});

		if (callType == 1) {
			// 呼入，显示接听和挂断
			btnAnswerVoice.setVisibility(View.VISIBLE);
			btnHangupVoice.setVisibility(View.VISIBLE);
		} else {
			// 呼出，只显示挂断
			btnAnswerVoice.setVisibility(View.GONE);
			btnHangupVoice.setVisibility(View.VISIBLE);
		}

		tvSpeakerName.setText(speakerName);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 返回键点击
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setTitle("结束通话？")
					.setPositiveButton("结束",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// 执行结束通话操作
									btnHangupVoice.performClick();
								}
							}).setNegativeButton("取消", null).create().show();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
