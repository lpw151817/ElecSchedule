package nercms.schedule.activity;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.actionbarsherlock.view.MenuItem;
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
import android.wxapp.service.elec.model.LoginResponse;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
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
	RadioButton tx_q, tq_yin, tq_yu, lb_jhgz, lb_lsgz, lb_gzqx, lb_qt, tsxq_t, tsxq_w, ssdd_s,
			ssdd_d, ssdd_p, ssdd_x, ssdd_qt, tdlx_lstd, tdlx_jhtd, tdlx_qt;
	EditText xmmc, tdfw, tdyxqy, zygznr, gzfzr, jhkssj, jhjssj, ysgdwld, sc, ssdw, rs, bz;
	ImageButton jhkssj_bt, jhjssj_bt, gzfzr_bt, ysgdwld_bt, ssdw_bt;
	Button qrtj;
	CheckBox sfxydb, sftd;

	WebRequestManager webRequest;
	int enterType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_plan_add);

		enterType = getIntent().getIntExtra("enterType", 1);

		// 初始化ActionBar
		initActionBar();
		initView();

		webRequest = new WebRequestManager(AppApplication.getInstance(), PlanAdd.this);

		initHandler();
	}

	private void startActivity() {
		Intent intent = new Intent(this, MeiRiJiHua.class);
		intent.putExtra("enterType", enterType);
		startActivity(intent);
	}

	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constants.LOGIN_SUCCESS:

					break;

				default:
					Log.e(PlanAdd.class.getName(), msg.what + "<<<<未处理");
					break;
				}

			}
		};
		// 注册Handler
		registHandler();
	}

	private void registHandler() {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// TODO unregistHandler
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
		lb_jhgz = (RadioButton) findViewById(R.id.jihuagongzuo);
		lb_lsgz = (RadioButton) findViewById(R.id.linshigongzuo);
		lb_gzqx = (RadioButton) findViewById(R.id.guzhangqiangxiu);
		lb_qt = (RadioButton) findViewById(R.id.leibie_qita);
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
		sc = (EditText) findViewById(R.id.sancuo);
		ssdw = (EditText) findViewById(R.id.shishidanwei);
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
	 * 检查必填项是否填完
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
				&& !TextUtils.isEmpty(rs.getText().toString()) && (lb_gzqx.isChecked()
						|| lb_jhgz.isChecked() || lb_lsgz.isChecked() || lb_qt.isChecked());
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
				String weather = null;
				if (tq_yin.isChecked()) {
					weather = "阴";
				} else if (tq_yu.isChecked()) {
					weather = "雨";
				} else if (tx_q.isChecked()) {
					weather = "晴";
				}

				String category = null;
				if (lb_gzqx.isChecked()) {
					category = "故障抢修";
				} else if (lb_jhgz.isChecked()) {
					category = "计划工作";
				} else if (lb_lsgz.isChecked()) {
					category = "临时工作";
				} else if (lb_qt.isChecked()) {
					category = "其他";
				}

				String special = null;
				if (tsxq_t.isChecked()) {
					special = "T";
				} else if (tsxq_w.isChecked()) {
					special = "无";
				}

				String domain = null;
				if (ssdd_d.isChecked()) {
					domain = "地";
				} else if (ssdd_p.isChecked()) {
					domain = "配";
				} else if (ssdd_qt.isChecked()) {
					domain = "其他";
				} else if (ssdd_s.isChecked()) {
					domain = "省";
				} else if (ssdd_x.isChecked()) {
					domain = "县";
				}

				String cut_type = null;
				if (tdlx_jhtd.isChecked()) {
					cut_type = "计划停电";
				} else if (tdlx_lstd.isChecked()) {
					cut_type = "临时停电";
				} else if (tdlx_qt.isChecked()) {
					cut_type = "其他";
				}

				// TODO 发送网络请求
				webRequest.createPlanTask(PlanAdd.this, weather, xmmc.getText().toString(),
						tdfw.getText().toString(), tdyxqy.getText().toString(),
						zygznr.getText().toString(), gzfzr.getText().toString(),
						jhkssj.getText().toString(), jhjssj.getText().toString(), category,
						sfxydb.isChecked(), special, ysgdwld.getText().toString(),
						sc.getText().toString(), domain, sftd.isChecked(), cut_type,
						ssdw.getText().toString(), rs.getText().toString(),
						bz.getText().toString());
			} else {
				showAlterDialog("错误", "请确认必填项填写完整!", R.drawable.login_error_icon, "确定", null);
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

	// 时间控件相关
	private ImageButton btn_calendar;
	private Dialog dialog;
	private static int START_YEAR = 2010, END_YEAR = 2020;

	// 选择截止时间 对年月日进行判断
	private void showDateTimePicker(final EditText editText) {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		// 添加大小月月份并将其转换为list,方便之后的判断
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };
		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);
		dialog = new Dialog(this);
		dialog.setTitle("请选择日期与时间");
		// 找到dialog的布局文件
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.date_time_select, null);
		// 年
		final WheelView wv_year = (WheelView) view.findViewById(R.id.year);
		wv_year.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));// 设置"年"的显示数据
		wv_year.setCyclic(true);// 可循环滚动
		wv_year.setLabel("年");// 添加文字
		wv_year.setCurrentItem(year - START_YEAR);// 初始化时显示的数据
		// 月
		final WheelView wv_month = (WheelView) view.findViewById(R.id.month);
		wv_month.setAdapter(new NumericWheelAdapter(1, 12));
		wv_month.setCyclic(true);
		wv_month.setLabel("月");
		wv_month.setCurrentItem(month);
		// 日
		final WheelView wv_day = (WheelView) view.findViewById(R.id.day);
		wv_day.setCyclic(true);
		// 判断大小月及是否闰年,用来确定"日"的数据
		if (list_big.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 31));
		} else if (list_little.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 30));
		} else {
			// 闰年
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				wv_day.setAdapter(new NumericWheelAdapter(1, 29));
			else
				wv_day.setAdapter(new NumericWheelAdapter(1, 28));
		}
		wv_day.setLabel("日");
		wv_day.setCurrentItem(day - 1);
		// 时
		final WheelView wv_hours = (WheelView) view.findViewById(R.id.hour);
		wv_hours.setAdapter(new NumericWheelAdapter(0, 23));
		wv_hours.setCyclic(true);
		wv_hours.setCurrentItem(hour);
		// 分
		final WheelView wv_mins = (WheelView) view.findViewById(R.id.mins);
		wv_mins.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
		wv_mins.setCyclic(true);
		wv_mins.setCurrentItem(minute);
		// 添加"年"监听
		OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int year_num = newValue + START_YEAR;
				// 判断大小月及是否闰年,用来确定"日"的数据
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
		// 添加"月"监听
		OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int month_num = newValue + 1;
				// 判断大小月及是否闰年,用来确定"日"的数据
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
		// 根据屏幕密度来指定选择器字体的大小
		int textSize = 0;

		textSize = 15;

		wv_day.TEXT_SIZE = textSize;
		wv_hours.TEXT_SIZE = textSize;
		wv_mins.TEXT_SIZE = textSize;
		wv_month.TEXT_SIZE = textSize;
		wv_year.TEXT_SIZE = textSize;

		Button btn_sure = (Button) view.findViewById(R.id.btn_datetime_sure);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_datetime_cancel);
		// 确定按钮
		btn_sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 如果是个数,则显示为"02"的样式
				String parten = "00";
				DecimalFormat decimal = new DecimalFormat(parten);
				// 设置日期的显示
				String _nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")// 创建时间
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
					Utils.showShortToast(PlanAdd.this, "选择时间小于当前时间，请重新选择");
				} else {
					editText.setText(currentSelectTime);
					dialog.dismiss();
				}
			}
		});
		// 取消
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		// 设置dialog的布局,并显示
		dialog.setContentView(view);
		dialog.show();
	}
}
