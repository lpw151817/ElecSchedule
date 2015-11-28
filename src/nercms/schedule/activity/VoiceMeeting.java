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

	private String userID;// ����ID

	private RelativeLayout btnAnswerVoice;
	private RelativeLayout btnHangupVoice;

	private TextView tvCallstatus;

	private LinearLayout allSpeakerLayout;
	private LinearLayout allParticipatorLayout;

	// ��ʱ���ؼ�
	private Chronometer timer;
	// ͨ��ʱ�䣨�룩
	private int durationTime;
	// ��ʼʱ��
	private String startTime;

	// ��������:1-���룻2-����
	private int callType;

	// ������ID�������б�������ID�������б�
	// ���ڶ෢���ˡ�������ߵ���������
	private ArrayList<String> speakerIDList = new ArrayList<String>();
	private ArrayList<String> speakerNameList = new ArrayList<String>();
	private ArrayList<String> participatorIDList = new ArrayList<String>();
	private ArrayList<String> participatorNameList = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.voice_meeting);

		// ��ʼ������
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

		// ��ʼ���绰��ʼʱ��
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
		// ���ݷ����˺Ͳ������б���̬�����ɿؼ�������ʾ
		// ������
		for (int i = 0; i < speakerNameList.size(); i++) {
			// ��ΧLinearLayout
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

			// �ڲ� ͷ��
			ImageView speakerImg = new ImageView(this);
			MarginLayoutParams imgLayoutParams = new MarginLayoutParams(dip2px(
					this, 45), dip2px(this, 45));
			imgLayoutParams.topMargin = marginPx;
			speakerImg.setBackgroundResource(R.drawable.default_user_avatar);
			speakerImg.setLayoutParams(imgLayoutParams);

			// �ڲ� �����ı�
			TextView speakerName = new TextView(this);
			MarginLayoutParams nameLayoutParams = new MarginLayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			nameLayoutParams.topMargin = marginPx;
			nameLayoutParams.bottomMargin = marginPx;
			speakerName.setLayoutParams(nameLayoutParams);
			speakerName.setText(speakerNameList.get(i).toString());

			// ���е���ΧLinearLayout
			speakerLayout.addView(speakerImg, imgLayoutParams);
			speakerLayout.addView(speakerName, nameLayoutParams);

			// ��ӵ�����Χ����
			allSpeakerLayout.addView(speakerLayout, linearLayoutParams);
		}
		// ������
		// ��������ʱ�������㷨
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

			// �����ڲ�������ӵ���Layout
			allParticipatorLayout.addView(createParticipatorLayoutView(
					lineParticipatorLayout, tempNameList));

		}

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
				// ���ж��Ǻ�����飬���Ǻ�������
				if (callType == 1) { // ����
					// �ж��Ƿ�ʼͨ��
					if (timer.getVisibility() == View.VISIBLE) {
						// �ѿ�ʼͨ���������Ҷ�
						timer.stop();
						durationTime = (int) (SystemClock.elapsedRealtime() - timer
								.getBase());
						// ����˴λ���
						// ...
						Utils.showShortToast(VoiceMeeting.this, "ͨ������");

					} else {
						// δ��ʼͨ�����ܾ�����
						// ����˴λ���
						// ...
						Utils.showShortToast(VoiceMeeting.this, "�ܾ�����");
					}
				} else if (callType == 2) { // ����
					// �ж��Ƿ�ʼͨ��
					if (timer.getVisibility() == View.VISIBLE) {
						// �ѿ�ʼͨ���������Ҷ�
						timer.stop();
						durationTime = (int) (SystemClock.elapsedRealtime() - timer
								.getBase());
						// ����˴λ���
						// ...
						Utils.showShortToast(VoiceMeeting.this, "ͨ������");

					} else {
						// δ��ʼͨ����ȡ������
						// ����˴λ���
						// ...
						Utils.showShortToast(VoiceMeeting.this, "ȡ������");
					}
				}

				VoiceMeeting.this.finish();
			}
		});

	}

	/**
	 * ��̬���Ʋ����߲��ֺ���ͼ
	 * 
	 * @param participatorLayout
	 *            ���в����ߵ�Layout
	 * @param nameList
	 *            �����ڵĲ�����nameList
	 * @return LinearLayout ����Layout
	 */
	private LinearLayout createParticipatorLayoutView(
			LinearLayout lineParticipatorLayout, List<String> nameList) {
		for (int i = 0; i < nameList.size(); i++) {
			// ���������ߵ���ΧLinearLayout
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

			// �ڲ� ͷ��
			ImageView participatorImg = new ImageView(this);
			MarginLayoutParams imgLayoutParams = new MarginLayoutParams(dip2px(
					this, 45), dip2px(this, 45));
			imgLayoutParams.setMargins(0, marginPx, 0, 0);
			participatorImg
					.setBackgroundResource(R.drawable.default_user_avatar);
			participatorImg.setLayoutParams(imgLayoutParams);

			// �ڲ� �����ı�
			TextView participatorName = new TextView(this);
			MarginLayoutParams nameLayoutParams = new MarginLayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			nameLayoutParams.setMargins(0, marginPx, 0, marginPx);
			participatorName.setLayoutParams(nameLayoutParams);
			participatorName.setText(nameList.get(i).toString());

			// ���е���ΧLinearLayout
			participatorLayout.addView(participatorImg);
			participatorLayout.addView(participatorName);

			// ���ظ���Layout
			lineParticipatorLayout.addView(participatorLayout);
		}

		return lineParticipatorLayout;
	}

	// dipתpx
	public static int dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// ���ؼ����
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			new AlertDialog.Builder(this)
					.setTitle("�������飿")
					.setPositiveButton("����",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface arg0,
										int arg1) {
									// ִ�л����������
									btnHangupVoice.performClick();
								}
							}).setNegativeButton("ȡ��", null).create().show();
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
