package nercms.schedule.activity;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.imooc.treeview.utils.Node;

import nercms.schedule.R;
import nercms.schedule.dateSelect.NumericWheelAdapter;
import nercms.schedule.dateSelect.OnWheelChangedListener;
import nercms.schedule.dateSelect.WheelView;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.ConferenceDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.conference.ConferenceUpdateQueryResponseRids;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.model.ConferenceModel;
import android.wxapp.service.model.ConferencePersonModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MeetingAdd extends BaseActivity {

	private static final String TAG = "MeetingAdd";
	// �����������
	private WebRequestManager webRequestManager;
	// ���������������
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	// ��Ա��������������
	private PersonDao personDao;
	// ���û�ID
	private String userID;
	// ���û���Ա
	// private StructuredStaffModel userSSM = null;
	private Handler handler;
	private String _meetingID;

	// ѡ��Ļ�������� ID�������б�
	private List<Node> participator = new ArrayList<Node>();

	// ѡ��Ļ��鷢���� ID�������б�
	private List<Node> speaker = new ArrayList<Node>();

	// �ؼ�
	private EditText meeting_title;// ��������
	private EditText meeting_starter;// ���鷢���� ��Ϊ��ǰ��¼�û�
	// private RadioGroup meeting_type;// ��������
	private RadioButton type1_select;// ԤԼ
	// private RadioButton type2_select;// ��ʱ
	private EditText meeting_participator;// ���������
	private EditText meeting_time;// ԤԼ����ʱ��
	private ImageButton select_participator;
	private ImageButton select_time;
	private LinearLayout timePickerLayout;

	private EditText meeting_speaker;
	private ImageButton select_speaker;

	private Dialog dialog;
	private static int START_YEAR = 2010, END_YEAR = 2020;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meeting_add);

		webRequestManager = new WebRequestManager(AppApplication.getInstance(), this);

		// ��ǰ��¼�û���Ϣ
		userID = MySharedPreference.get(MeetingAdd.this, MySharedPreference.USER_ID, "");
		personDao = daoFactory.getPersonDao(MeetingAdd.this);
		// ��ʼ��ActionBar
		initActionBar();

		_meetingID = Utils.produceConferenceID(userID);// ���ɻ���ID
		// ��ʼ���ؼ�
		meeting_title = (EditText) findViewById(R.id.meeting_title);
		meeting_starter = (EditText) findViewById(R.id.meeting_starter);
		// meeting_type = (RadioGroup)
		// findViewById(R.id.radioGroup_meeting_type);
		type1_select = (RadioButton) findViewById(R.id.radio_meeting_type1);
		// type2_select = (RadioButton) findViewById(R.id.radio_meeting_type2);
		timePickerLayout = (LinearLayout) findViewById(R.id.meeting_add_time_ll);
		// type2_select.setChecked(true);
		timePickerLayout.setVisibility(View.VISIBLE);

		meeting_participator = (EditText) findViewById(R.id.meeting_participator);
		meeting_participator.setEnabled(false);
		meeting_participator.setTextColor(Color.BLACK);
		select_participator = (ImageButton) findViewById(R.id.meeting_participator_picker_ib);

		meeting_speaker = (EditText) findViewById(R.id.meeting_speaker);
		meeting_speaker.setEnabled(false);
		meeting_speaker.setTextColor(Color.BLACK);
		select_speaker = (ImageButton) findViewById(R.id.meeting_speaker_picker_ib);

		meeting_time = (EditText) findViewById(R.id.add_meeting_time);
		select_time = (ImageButton) findViewById(R.id.meeting_add_select_time);// ѡ��ʱ��ؼ�
		// // ���� ��ѡ��ť
		// meeting_type.setOnCheckedChangeListener(new
		// RadioGroup.OnCheckedChangeListener() {
		// public void onCheckedChanged(RadioGroup group, int checkedId) {
		// // ѡ��ʱ���� ����ʱ��ѡ�񲼾� ѡ��ԤԼ���� ��ʾʱ��ѡ�񲼾�
		// if (type1_select.getId() == checkedId) {
		// timePickerLayout.setVisibility(View.VISIBLE);
		// } else {
		// timePickerLayout.setVisibility(View.GONE);
		// }
		// }
		// });
		// ���� ������ѡ��
		select_participator.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(MeetingAdd.this, ContactSelect.class);
				intent.putExtra("entrance_flag", 3);
				intent.putExtra("pod", (Serializable) speaker);
				intent.putExtra("receiver", (Serializable) participator);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent,
						LocalConstant.MEETING_PARTICIPATOR_SELECT_REQUEST_CODE);
			}
		});
		// ���� ʱ��ѡ��
		select_time.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDateTimePicker();
			}
		});

		// ������ѡ��
		select_speaker.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.setClass(MeetingAdd.this, ContactSelect.class);
				intent.putExtra("entrance_flag", 4);
				intent.putExtra("pod", (Serializable) speaker);
				intent.putExtra("receiver", (Serializable) participator);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, LocalConstant.MEETING_SPEAKER_SELECT_REQUEST_CODE);
			}
		});

		meeting_starter.setEnabled(false);// ���鷢���� ���ɱ༭
		meeting_starter.setText(personDao.getPersonInfo(userID).getN());
		meeting_time.setEnabled(false);// ԤԼ����ʱ��Ĭ����ʾΪ��ǰϵͳʱ��
		meeting_time.setTextColor(Color.BLACK);

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				// ��������ɹ�
				case Constant.CONFERENCE_CREATE_SECCESS:
					Toast.makeText(MeetingAdd.this, "��������ɹ�", Toast.LENGTH_LONG).show();
					MeetingAdd.this.finish();
					break;
				// ��������ʧ��
				case Constant.CONFERENCE_CREATE_FAIL:
					String errorCode = ((NormalServerResponse) msg.obj).getEc();
					showAlterDialog("��¼ʧ��", Utils.getErrorMsg(errorCode),
							R.drawable.login_error_icon, "ȷ��", null);
					break;
				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler, Constant.CONFERENCE_CREATE_SECCESS,
				Contants.METHOD_CONFERENCE_CREATE);
		MessageHandlerManager.getInstance().register(handler, Constant.CONFERENCE_CREATE_FAIL,
				Contants.METHOD_CONFERENCE_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MessageHandlerManager.getInstance().unregister(Constant.CONFERENCE_CREATE_SECCESS,
				Contants.METHOD_CONFERENCE_CREATE);
		MessageHandlerManager.getInstance().unregister(Constant.CONFERENCE_CREATE_FAIL,
				Contants.METHOD_CONFERENCE_CREATE);
	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("�������");
	}

	// �Ҳౣ�水ť���
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem save = menu.add(0, 1, 0, "����");
		save.setIcon(R.drawable.ic_action_save);
		save.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	// �˵�ע��
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:// ���������ҳ
			finish();
			break;
		case 1: // �Ҽ��������
			createMeeting();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void createMeeting() {

		String _title = meeting_title.getText().toString();

		int _type; // 1-�ֻ�����ʱ���飻3-�ֻ�����ԤԼ����
		int _status;// ����״̬��1-��������У�2-���������3-ԤԼ�У��ȴ���ʼ��
		if (type1_select.isChecked()) { // ѡ��ԤԼ
			_type = 3;
			_status = 3;
		} else { // ѡ�м�ʱ
			_type = 1;
			_status = 2;
		}
		String _createTime = System.currentTimeMillis() + "";// ����ʱ��
		String _reservedTime = meeting_time.getText().toString();// ԤԼʱ��

		List<ConferenceUpdateQueryResponseRids> rids = new ArrayList<ConferenceUpdateQueryResponseRids>();

		for (int i = 0; i < participator.size(); i++) {
			rids.add(new ConferenceUpdateQueryResponseRids(participator.get(i).getId().substring(1),
					"2"));
		}
		for (int i = 0; i < speaker.size(); i++) {
			rids.add(new ConferenceUpdateQueryResponseRids(speaker.get(i).getId().substring(1),
					"1"));
		}

		// ������
		if (_title.isEmpty() || participator.size() == 0 || speaker.size() == 0
				|| (_type == 3 && _reservedTime.isEmpty())) {
			new AlertDialog.Builder(MeetingAdd.this).setTitle("�޷��������").setMessage("�����¼������Ƿ���д����")
					.setPositiveButton("ȷ��", null).create().show();
			return;
		}

		webRequestManager.createConference(_title, getUserId(),
				Utils.parseDateInFormat(_reservedTime), "4", "", "", rids);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case LocalConstant.MEETING_PARTICIPATOR_SELECT_REQUEST_CODE:
			if (resultCode == RESULT_OK) {

				List<Node> selectedPeople = (List<Node>) data.getExtras().get("data");
				participator = selectedPeople;
				String names = "";
				for (Node item : selectedPeople) {
					names += personDao.getPersonInfo(item.getId().substring(1)).getN() + "/";
				}
				meeting_participator.setText(names);
			}

			break;

		case LocalConstant.MEETING_SPEAKER_SELECT_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				List<Node> selectedPeople = (List<Node>) data.getExtras().get("data");
				speaker = selectedPeople;
				String names = "";
				for (Node item : selectedPeople) {
					names += personDao.getPersonInfo(item.getId().substring(1)).getN() + "/";
				}
				meeting_speaker.setText(names);
			}

			break;

		default:
			break;
		}
	}

	private void showDateTimePicker() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		// ��Ӵ�С���·ݲ�����ת��Ϊlist,����֮����ж�
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };

		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);
		dialog = new Dialog(this);
		dialog.setTitle("��ѡ��������ʱ��");
		// �ҵ�dialog�Ĳ����ļ�
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.date_time_select, null);
		// ��
		final WheelView wv_year = (WheelView) view.findViewById(R.id.year);
		wv_year.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));// ����"��"����ʾ����
		wv_year.setCyclic(true);// ��ѭ������
		wv_year.setLabel("��");// �������
		wv_year.setCurrentItem(year - START_YEAR);// ��ʼ��ʱ��ʾ������
		// ��
		final WheelView wv_month = (WheelView) view.findViewById(R.id.month);
		wv_month.setAdapter(new NumericWheelAdapter(1, 12));
		wv_month.setCyclic(true);
		wv_month.setLabel("��");
		wv_month.setCurrentItem(month);
		// ��
		final WheelView wv_day = (WheelView) view.findViewById(R.id.day);
		wv_day.setCyclic(true);
		// �жϴ�С�¼��Ƿ�����,����ȷ��"��"������
		if (list_big.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 31));
		} else if (list_little.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 30));
		} else {
			// ����
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				wv_day.setAdapter(new NumericWheelAdapter(1, 29));
			else
				wv_day.setAdapter(new NumericWheelAdapter(1, 28));
		}
		wv_day.setLabel("��");
		wv_day.setCurrentItem(day - 1);
		// ʱ
		final WheelView wv_hours = (WheelView) view.findViewById(R.id.hour);
		wv_hours.setAdapter(new NumericWheelAdapter(0, 23));
		wv_hours.setCyclic(true);
		wv_hours.setCurrentItem(hour);

		// ��
		final WheelView wv_mins = (WheelView) view.findViewById(R.id.mins);
		wv_mins.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
		wv_mins.setCyclic(true);
		wv_mins.setCurrentItem(minute);

		// ���"��"����
		OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int year_num = newValue + START_YEAR;
				// �жϴ�С�¼��Ƿ�����,����ȷ��"��"������
				if (list_big.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if ((year_num % 4 == 0 && year_num % 100 != 0) || year_num % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		// ���"��"����
		OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int month_num = newValue + 1;
				// �жϴ�С�¼��Ƿ�����,����ȷ��"��"������
				if (list_big.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if (((wv_year.getCurrentItem() + START_YEAR) % 4 == 0
							&& (wv_year.getCurrentItem() + START_YEAR) % 100 != 0)
							|| (wv_year.getCurrentItem() + START_YEAR) % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		wv_year.addChangingListener(wheelListener_year);
		wv_month.addChangingListener(wheelListener_month);
		// ������Ļ�ܶ���ָ��ѡ��������Ĵ�С
		int textSize = 0;
		textSize = 15;
		wv_day.TEXT_SIZE = textSize;
		wv_hours.TEXT_SIZE = textSize;
		wv_mins.TEXT_SIZE = textSize;
		wv_month.TEXT_SIZE = textSize;
		wv_year.TEXT_SIZE = textSize;
		Button btn_sure = (Button) view.findViewById(R.id.btn_datetime_sure);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_datetime_cancel);
		// ȷ����ť
		btn_sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ����Ǹ���,����ʾΪ"02"����ʽ
				String parten = "00";
				DecimalFormat decimal = new DecimalFormat(parten);

				// 2014-6-12 ѡ��ʱ���жϣ��Ƿ�С�ڵ�ǰʱ�䣡

				String _nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")// ����ʱ��
						.format(new Date(System.currentTimeMillis()));

				String currentSelectTime = (wv_year.getCurrentItem() + START_YEAR) + "-"
						+ decimal.format((wv_month.getCurrentItem() + 1)) + "-"
						+ decimal.format((wv_day.getCurrentItem() + 1)) + " "
						+ decimal.format(wv_hours.getCurrentItem()) + ":"
						+ decimal.format(wv_mins.getCurrentItem()) + ":00";

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date nowTime = new Date();
				Date selectTime = new Date();
				try {
					nowTime = df.parse(_nowTime);
					selectTime = df.parse(currentSelectTime);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				long mins = (selectTime.getTime() - nowTime.getTime()) / 6000;
				if (mins < 3) {
					Toast.makeText(MeetingAdd.this, "ѡ��ʱ��С�ڵ�ǰʱ�䣬������ѡ��", Toast.LENGTH_SHORT).show();
				} else {
					// �������ڵ���ʾ
					meeting_time.setText(currentSelectTime);
					dialog.dismiss();
				}

			}
		});
		// ȡ��
		btn_cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		dialog.setContentView(view);
		dialog.show();
	}

}
