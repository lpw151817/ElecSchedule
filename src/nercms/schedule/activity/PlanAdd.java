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

import com.actionbarsherlock.view.MenuItem;
import com.imooc.treeview.utils.Node;
import com.nercms.Push;

import android.R.integer;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.model.CreatePlanTaskResponse;
import android.wxapp.service.elec.model.LoginResponse;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.util.MySharedPreference;
import nercms.schedule.R;
import nercms.schedule.dateSelect.NumericWheelAdapter;
import nercms.schedule.dateSelect.OnWheelChangedListener;
import nercms.schedule.dateSelect.WheelView;
import nercms.schedule.utils.MyLog;
import nercms.schedule.utils.Utils;

public class PlanAdd extends BaseActivity implements OnClickListener {

	private Handler handler;

	RadioGroup tq_rg, lb_rg, tsxq_rg, ssdd_rg, tdlx_rg;
	RadioButton tx_q, tq_yin, tq_yu, lb_zyxc, lb_czxc, lb_gzqxxc, tsxq_t, tsxq_w, ssdd_s, ssdd_d,
			ssdd_p, ssdd_x, ssdd_qt, tdlx_lstd, tdlx_jhtd, tdlx_qt;
	EditText xmmc, tdfw, tdyxqy, zygznr, gzfzr, jhkssj, jhjssj, ysgdwld, sc, ssdw, rs, bz;
	ImageButton jhkssj_bt, jhjssj_bt, gzfzr_bt, ysgdwld_bt, ssdw_bt;
	Button qrtj;
	CheckBox sfxydb, sftd;

	WebRequestManager webRequest;
	int enterType;

	List<Node> gzfzrList;
	List<Node> ysgdwldList;
	Node orgs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan_add);

		enterType = getIntent().getIntExtra("enterType", 1);
		gzfzrList = new ArrayList<Node>();
		ysgdwldList = new ArrayList<Node>();

		// ��ʼ��ActionBar
		initActionBar();
		initView();

		webRequest = new WebRequestManager(AppApplication.getInstance(), PlanAdd.this);

		initHandler();
	}

	private void startActivity(String tid) {
		Intent intent = new Intent(this, MeiRiJiHua.class);
		intent.putExtra("enterType", enterType);
		intent.putExtra("tid", tid);
		startActivity(intent);
	}

	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constants.CREATE_TASK_SUCCESS:
					// �����ɹ�
					showLongToast("�����ɹ�");
					CreatePlanTaskResponse r = (CreatePlanTaskResponse) msg.obj;
					startActivity(r.getTid());
					break;
				case Constants.CREATE_TASK_FAIL:
				case Constants.CREATE_TASK_SAVE_FAIL:
					dismissProgressDialog();
					if (msg.obj != null) {
						showAlterDialog("����ʧ��", ((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "ȷ��", null);
					} else {
						showAlterDialog("����ʧ��", "�����Ƿ����������������", R.drawable.login_error_icon, "ȷ��",
								null);
					}
					break;

				default:
					Log.e(PlanAdd.class.getName(), msg.what + "<<<<δ����");
					break;
				}

			}
		};
		// ע��Handler
		registHandler();
	}

	private void registHandler() {
		MessageHandlerManager.getInstance().register(handler, Constants.CREATE_TASK_SUCCESS,
				CreatePlanTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.CREATE_TASK_FAIL,
				CreatePlanTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.CREATE_TASK_SAVE_FAIL,
				CreatePlanTaskResponse.class.getName());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MessageHandlerManager.getInstance().unregister(Constants.CREATE_TASK_SUCCESS,
				CreatePlanTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.CREATE_TASK_FAIL,
				CreatePlanTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.CREATE_TASK_SAVE_FAIL,
				CreatePlanTaskResponse.class.getName());
	}

	private void initView() {
		tq_rg = (RadioGroup) findViewById(R.id.radioGroup1);
		lb_rg = (RadioGroup) findViewById(R.id.radioGroup2);
		tsxq_rg = (RadioGroup) findViewById(R.id.radioGroup3);
		ssdd_rg = (RadioGroup) findViewById(R.id.radioGroup4);
		tdlx_rg = (RadioGroup) findViewById(R.id.radioGroup5);

		tx_q = (RadioButton) findViewById(R.id.sun);
		tq_yin = (RadioButton) findViewById(R.id.cloudy);
		tq_yu = (RadioButton) findViewById(R.id.rain);
		lb_zyxc = (RadioButton) findViewById(R.id.xianchagnzuoye);
		lb_czxc = (RadioButton) findViewById(R.id.caozuoxianchang);
		lb_gzqxxc = (RadioButton) findViewById(R.id.guzhangqiangxiuxianchang);
		tsxq_t = (RadioButton) findViewById(R.id.teshuxuqiu1);
		tsxq_w = (RadioButton) findViewById(R.id.teshuxuqiu2);
		ssdd_s = (RadioButton) findViewById(R.id.suoshudiaodu1);
		ssdd_d = (RadioButton) findViewById(R.id.suoshudiaodu2);
		ssdd_p = (RadioButton) findViewById(R.id.suoshudiaodu3);
		ssdd_x = (RadioButton) findViewById(R.id.suoshudiaodu4);
		ssdd_qt = (RadioButton) findViewById(R.id.suoshudiaodu5);
		tdlx_lstd = (RadioButton) findViewById(R.id.linshitingdian);
		tdlx_jhtd = (RadioButton) findViewById(R.id.jihuatingdian);
		tdlx_qt = (RadioButton) findViewById(R.id.tingdian_qita);

		xmmc = (EditText) findViewById(R.id.xiangmumingcheng);
		tdfw = (EditText) findViewById(R.id.tingdianfanwei);
		tdyxqy = (EditText) findViewById(R.id.tingdianyingxiangquyu);
		zygznr = (EditText) findViewById(R.id.zhuyaogongzuoneirong);
		gzfzr = (EditText) findViewById(R.id.gongzuofuzeren_et);
		gzfzr.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					startPeopleSelect(1);
				}
				return true;
			}
		});
		jhkssj = (EditText) findViewById(R.id.kaishishijian_et);
		jhkssj.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					showDateTimePicker(jhkssj);
				}
				return true;
			}
		});
		jhjssj = (EditText) findViewById(R.id.jieshushijian_et);
		jhjssj.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					showDateTimePicker(jhjssj);
				}
				return true;
			}
		});
		ysgdwld = (EditText) findViewById(R.id.lingdao_et);
		ysgdwld.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					startPeopleSelect(2);
				}
				return true;
			}
		});
		sc = (EditText) findViewById(R.id.sancuo);
		ssdw = (EditText) findViewById(R.id.shishidanwei);
		ssdw.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					Intent intent = new Intent(PlanAdd.this, OrgSelect.class);
					Bundle bundle = new Bundle();
					bundle.putSerializable("pod", (Serializable) orgs);
					intent.putExtras(bundle);
					startActivityForResult(intent, 200);
				}
				return true;
			}
		});
		rs = (EditText) findViewById(R.id.renshu);
		bz = (EditText) findViewById(R.id.beizhu);

		// gzfzr_bt = (ImageButton) findViewById(R.id.gongzuofuzeren_bt);
		// jhkssj_bt = (ImageButton) findViewById(R.id.kaishishijian_bt);
		// jhjssj_bt = (ImageButton) findViewById(R.id.jieshushijian_bt);
		// ysgdwld_bt = (ImageButton) findViewById(R.id.lingdao_bt);
		// ssdw_bt = (ImageButton) findViewById(R.id.shishidanwei_bt);
		qrtj = (Button) findViewById(R.id.tijiao);

		sfxydb = (CheckBox) findViewById(R.id.dengbao);
		sftd = (CheckBox) findViewById(R.id.tingdian);

		// gzfzr_bt.setOnClickListener(this);
		// jhkssj_bt.setOnClickListener(this);
		// jhjssj_bt.setOnClickListener(this);
		// ssdw_bt.setOnClickListener(this);
		// ysgdwld_bt.setOnClickListener(this);
		qrtj.setOnClickListener(this);
	}

	/**
	 * 
	 * @param from
	 *            1:����������, 2���쵼
	 */
	private void startPeopleSelect(int from) {
		Intent intent = new Intent(PlanAdd.this, ContactSelect.class);
		Bundle bundle = new Bundle();
		// ��ʾ��ÿ�ռƻ�����
		bundle.putInt("entrance_flag", 1);
		bundle.putInt("type", from);
		// input bundle
		bundle.putSerializable("pod", (Serializable) gzfzrList);
		bundle.putSerializable("receiver", (Serializable) ysgdwldList);
		intent.putExtras(bundle);
		startActivityForResult(intent, 100);
	}

	/**
	 * ���������Ƿ�����
	 * 
	 * @return
	 */
	private boolean checkMustInput() {
		return !TextUtils.isEmpty(xmmc.getText().toString())
				&& !TextUtils.isEmpty(zygznr.getText().toString())
				&& !TextUtils.isEmpty(gzfzr.getText().toString())
				&& !TextUtils.isEmpty(jhkssj.getText().toString())
				&& !TextUtils.isEmpty(jhjssj.getText().toString())
				&& !TextUtils.isEmpty(ssdw.getText().toString())
				&& !TextUtils.isEmpty(rs.getText().toString())
				&& (lb_zyxc.isChecked() || lb_czxc.isChecked() || lb_gzqxxc.isChecked());
	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.tijiao:
			if (checkMustInput()) {
				StringBuilder weather = new StringBuilder("weather");
				if (tq_yin.isChecked()) {
					weather.append("02");
				} else if (tq_yu.isChecked()) {
					weather.append("03");
				} else if (tx_q.isChecked()) {
					weather.append("01");
				}

				StringBuilder category = new StringBuilder("category");
				if (lb_zyxc.isChecked()) {
					category.append("01");
				} else if (lb_czxc.isChecked()) {
					category.append("02");
				} else if (lb_gzqxxc.isChecked()) {
					category.append("03");
				}

				String special = null;
				if (tsxq_t.isChecked()) {
					special = "T";
				} else if (tsxq_w.isChecked()) {
					special = "��";
				}

				StringBuilder domain = new StringBuilder("domain");
				if (ssdd_d.isChecked()) {
					domain.append("02");
				} else if (ssdd_p.isChecked()) {
					domain.append("03");
				} else if (ssdd_qt.isChecked()) {
					domain.append("05");
				} else if (ssdd_s.isChecked()) {
					domain.append("01");
				} else if (ssdd_x.isChecked()) {
					domain.append("04");
				}

				StringBuilder cut_type = new StringBuilder("cutType");
				if (tdlx_jhtd.isChecked()) {
					cut_type.append("02");
				} else if (tdlx_lstd.isChecked()) {
					cut_type.append("01");
				} else if (tdlx_qt.isChecked()) {
					cut_type.append("03");
				}

				// ������������
				webRequest.createPlanTask(PlanAdd.this, weather.toString(),
						xmmc.getText().toString(), tdfw.getText().toString(),
						tdyxqy.getText().toString(), zygznr.getText().toString(), gzfzrList,
						Utils.parseDateInFormat(jhkssj.getText().toString()),
						Utils.parseDateInFormat(jhjssj.getText().toString()), category.toString(),
						sfxydb.isChecked(), special, ysgdwldList, sc.getText().toString(),
						domain.toString(), sftd.isChecked(), cut_type.toString(), orgs,
						rs.getText().toString(), bz.getText().toString());
			} else {
				showAlterDialog("����", "��ȷ�ϱ�������д����!", R.drawable.login_error_icon, "ȷ��", null);
			}
			break;
		// case R.id.gongzuofuzeren_bt:
		//
		// break;
		// case R.id.kaishishijian_bt:
		//
		// showDateTimePicker(jhkssj);
		// break;
		// case R.id.jieshushijian_bt:
		// showDateTimePicker(jhjssj);
		// break;
		// case R.id.lingdao_bt:
		//
		// break;
		// case R.id.shishidanwei_bt:
		//
		// break;

		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 100:
				int type = data.getIntExtra("type", -1);
				// ����������
				if (type == 1) {
					gzfzrList = (List<Node>) data.getSerializableExtra("data");
					StringBuilder builder = new StringBuilder();
					for (Node i : gzfzrList) {
						builder.append(i.getName() + "/");
					}
					gzfzr.setText(builder.toString());
				}
				// �쵼
				else if (type == 2) {
					ysgdwldList = (List<Node>) data.getSerializableExtra("data");
					StringBuilder builder = new StringBuilder();
					for (Node i : ysgdwldList) {
						builder.append(i.getName() + "/");
					}
					ysgdwld.setText(builder.toString());
				}
				break;
			case 200:
				orgs = (Node) data.getSerializableExtra("data");
				ssdw.setText(orgs.getName());
				break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	////////////////////////////////////////
	////////////////////////////////////////
	////////////////////////////////////////
	///////////// ����Ϊʱ��ؼ�/////////////////
	////////////////////////////////////////
	////////////////////////////////////////
	////////////////////////////////////////
	// ʱ��ؼ����
	private ImageButton btn_calendar;
	private Dialog dialog;
	private static int START_YEAR = 2016, END_YEAR = 2030;

	// ѡ���ֹʱ�� �������ս����ж�
	private void showDateTimePicker(final EditText editText) {
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
				// �������ڵ���ʾ
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
					e.printStackTrace();
				}
				long mins = (selectTime.getTime() - nowTime.getTime()) / 6000;
				if (mins < 3) {
					Utils.showShortToast(PlanAdd.this, "ѡ��ʱ��С�ڵ�ǰʱ�䣬������ѡ��");
				} else {
					editText.setText(currentSelectTime);
					dialog.dismiss();
				}
			}
		});
		// ȡ��
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		// ����dialog�Ĳ���,����ʾ
		dialog.setContentView(view);
		dialog.show();
	}
}
