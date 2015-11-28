package nercms.schedule.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.wxapp.service.util.MySharedPreference;

public class VoiceMeeting extends BaseActivity {

	private String userID;// 本人ID

	private RelativeLayout btnAnswerVoice;
	private RelativeLayout btnHangupVoice;

	private TextView tvCallstatus;

	private LinearLayout allSpeakerLayout;
	private LinearLayout allParticipatorLayout;

	// 计时器控件
	private Chronometer timer;
	// 通话时间（秒）
	private int durationTime;
	// 开始时间
	private String startTime;

	// 呼叫类型:1-呼入；2-呼出
	private int callType;

	// 发言人ID和姓名列表；参与者ID和姓名列表
	// 用于多发言人、多参与者的语音会议
	private ArrayList<String> speakerIDList = new ArrayList<String>();
	private ArrayList<String> speakerNameList = new ArrayList<String>();
	private ArrayList<String> participatorIDList = new ArrayList<String>();
	private ArrayList<String> participatorNameList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.voice_meeting);

		// 初始化数据
		callType = getIntent().getIntExtra("call_type", 2);
		speakerIDList = getIntent().getStringArrayListExtra("speaker_id_list");
		speakerNameList = getIntent().getStringArrayListExtra(
				"speaker_name_list");
		participatorIDList = getIntent().getStringArrayListExtra(
				"participator_id_list");
		participatorNameList = getIntent().getStringArrayListExtra(
				"participator_name_list");

		userID = MySharedPreference.get(VoiceMeeting.this,
				MySharedPreference.USER_ID, null);

		// 初始化电话开始时间
		startTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
				.format(new Date(System.currentTimeMillis()));

		initView();

	}

	@SuppressLint("NewApi")
	private void initView() {
		timer = (Chronometer) findViewById(R.id.voice_meeting_timer);
		tvCallstatus = (TextView) findViewById(R.id.voice_meeting_status_tv);
		btnAnswerVoice = (RelativeLayout) findViewById(R.id.voice_meeting_answer_rl);
		btnHangupVoice = (RelativeLayout) findViewById(R.id.voice_meeting_hangup_rl);

		allSpeakerLayout = (LinearLayout) findViewById(R.id.voice_meeting_speaker_ll);
		allParticipatorLayout = (LinearLayout) findViewById(R.id.voice_meeting_total_listener_ll);

		// 2014-8-7
		// 根据发言人和参与者列表，动态的生成控件进行显示
		// 发言人
		for (int i = 0; i < speakerNameList.size(); i++) {
			// 外围LinearLayout
			LinearLayout speakerLayout = new LinearLayout(this);
			LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			int marginPx = dip2px(this, 10);
			// linearLayoutParams.setMargins(marginPx, marginPx, marginPx,
			// marginPx);
			linearLayoutParams.leftMargin = marginPx;
			linearLayoutParams.topMargin = marginPx;
			linearLayoutParams.rightMargin = marginPx;
			linearLayoutParams.bottomMargin = marginPx;
			speakerLayout.setOrientation(LinearLayout.VERTICAL);
			speakerLayout.setGravity(Gravity.CENTER);

			// 内部 头像
			ImageView speakerImg = new ImageView(this);
			MarginLayoutParams imgLayoutParams = new MarginLayoutParams(dip2px(
					this, 45), dip2px(this, 45));
			imgLayoutParams.topMargin = marginPx;
			speakerImg.setBackgroundResource(R.drawable.default_user_avatar);
			speakerImg.setLayoutParams(imgLayoutParams);

			// 内部 名字文本
			TextView speakerName = new TextView(this);
			MarginLayoutParams nameLayoutParams = new MarginLayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			nameLayoutParams.topMargin = marginPx;
			nameLayoutParams.bottomMargin = marginPx;
			speakerName.setLayoutParams(nameLayoutParams);
			speakerName.setText(speakerNameList.get(i).toString());

			// 集中到外围LinearLayout
			speakerLayout.addView(speakerImg, imgLayoutParams);
			speakerLayout.addView(speakerName, nameLayoutParams);

			// 添加到最外围布局
			allSpeakerLayout.addView(speakerLayout, linearLayoutParams);
		}
		// 参与者
		// 多于四人时，排列算法
		int parNum = participatorNameList.size();
		int lineNum;
		if (parNum % 4 == 0) {
			lineNum = parNum / 4;
		} else {
			lineNum = parNum / 4 + 1;
		}
		List<String> tempNameList;
		for (int i = 1; i <= lineNum; i++) {
			tempNameList = participatorNameList.subList((i - 1) * 4,
					(i * 4) <= (parNum) ? (i * 4) : (parNum));
			LinearLayout lineParticipatorLayout = new LinearLayout(this);
			LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			linearLayoutParams.topMargin = dip2px(this, 20);
			linearLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
			lineParticipatorLayout.setOrientation(LinearLayout.HORIZONTAL);
			lineParticipatorLayout.setGravity(Gravity.CENTER_HORIZONTAL);
			lineParticipatorLayout.setLayoutParams(linearLayoutParams);

			// 调用内部方法添加单行Layout
			allParticipatorLayout.addView(createParticipatorLayoutView(
					lineParticipatorLayout, tempNameList));

		}

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
				// 先判断是呼入会议，还是呼出会议
				if (callType == 1) { // 呼入
					// 判断是否开始通话
					if (timer.getVisibility() == View.VISIBLE) {
						// 已开始通话，正常挂断
						timer.stop();
						durationTime = (int) (SystemClock.elapsedRealtime() - timer
								.getBase());
						// 保存此次会议
						// ...
						Utils.showShortToast(VoiceMeeting.this, "通话结束");

					} else {
						// 未开始通话，拒绝接听
						// 保存此次会议
						// ...
						Utils.showShortToast(VoiceMeeting.this, "拒绝接听");
					}
				} else if (callType == 2) { // 呼出
					// 判断是否开始通话
					if (timer.getVisibility() == View.VISIBLE) {
						// 已开始通话，正常挂断
						timer.stop();
						durationTime = (int) (SystemClock.elapsedRealtime() - timer
								.getBase());
						// 保存此次会议
						// ...
						Utils.showShortToast(VoiceMeeting.this, "通话结束");

					} else {
						// 未开始通话，取消呼叫
						// 保存此次会议
						// ...
						Utils.showShortToast(VoiceMeeting.this, "取消呼叫");
					}
				}

				VoiceMeeting.this.finish();
			}
		});

	}

	/**
	 * 动态绘制参与者布局和视图
	 * 
	 * @param participatorLayout
	 *            单行参与者的Layout
	 * @param nameList
	 *            单行内的参与者nameList
	 * @return LinearLayout 该行Layout
	 */
	private LinearLayout createParticipatorLayoutView(
			LinearLayout lineParticipatorLayout, List<String> nameList) {
		for (int i = 0; i < nameList.size(); i++) {
			// 单个参与者的外围LinearLayout
			LinearLayout participatorLayout = new LinearLayout(this);
			LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			int marginPx = dip2px(this, 5);
			linearLayoutParams.leftMargin = marginPx;
			linearLayoutParams.topMargin = marginPx;
			linearLayoutParams.rightMargin = marginPx;
			linearLayoutParams.bottomMargin = marginPx;
			participatorLayout.setLayoutParams(linearLayoutParams);
			participatorLayout.setOrientation(LinearLayout.VERTICAL);
			participatorLayout.setGravity(Gravity.CENTER);

			// 内部 头像
			ImageView participatorImg = new ImageView(this);
			MarginLayoutParams imgLayoutParams = new MarginLayoutParams(dip2px(
					this, 45), dip2px(this, 45));
			imgLayoutParams.setMargins(0, marginPx, 0, 0);
			participatorImg
					.setBackgroundResource(R.drawable.default_user_avatar);
			participatorImg.setLayoutParams(imgLayoutParams);

			// 内部 名字文本
			TextView participatorName = new TextView(this);
			MarginLayoutParams nameLayoutParams = new MarginLayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			nameLayoutParams.setMargins(0, marginPx, 0, marginPx);
			participatorName.setLayoutParams(nameLayoutParams);
			participatorName.setText(nameList.get(i).toString());

			// 集中到外围LinearLayout
			participatorLayout.addView(participatorImg);
			participatorLayout.addView(participatorName);

			// 返回该行Layout
			lineParticipatorLayout.addView(participatorLayout);
		}

		return lineParticipatorLayout;
	}

	// dip转px
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 返回键点击
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setTitle("结束会议？")
					.setPositiveButton("结束",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// 执行会议结束操作
									btnHangupVoice.performClick();
								}
							}).setNegativeButton("取消", null).create().show();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
