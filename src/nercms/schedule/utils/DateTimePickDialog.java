package nercms.schedule.utils;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import nercms.schedule.R;

/**
 * 日期时间选择控件 使用方法： private EditText inputDate;//需要设置的日期时间文本编辑框 private String
 * initDateTime="2012年9月3日 14:44",//初始日期时间值 在点击事件中使用：
 * inputDate.setOnClickListener(new OnClickListener() {
 * 
 * @Override public void onClick(View v) { DateTimePickDialogUtil
 *           dateTimePicKDialog=new
 *           DateTimePickDialogUtil(SinvestigateActivity.this,initDateTime);
 *           dateTimePicKDialog.dateTimePicKDialog(inputDate); } });
 * 
 */
public class DateTimePickDialog implements OnDateChangedListener, OnTimeChangedListener {
	private DatePicker datePicker;
	private TimePicker timePicker;
	private AlertDialog ad;
	private String dateTime;
	private String initDateTime;
	private Activity activity;

	private DateTimePickDialogListener listener;
	// 标记是否为开始任务
	private boolean isBegin;
	private String beginTime;

	/**
	 * 日期时间弹出选择框构造函数
	 * 
	 * @param activity
	 *            ：调用的父activity
	 * @param initDateTime
	 *            初始日期时间值，作为弹出窗口的标题和日期时间初始值
	 */
	public DateTimePickDialog(Activity activity, String initDateTime,
			DateTimePickDialogListener listener) {
		this.activity = activity;
		this.initDateTime = initDateTime;
		this.isBegin = true;
		this.listener = listener;
	}

	public DateTimePickDialog(Activity activity, String initDateTime, String beginTime,
			DateTimePickDialogListener listener) {
		this.activity = activity;
		this.initDateTime = initDateTime;
		this.isBegin = false;
		this.beginTime = beginTime;
		this.listener = listener;
	}

	public void init(DatePicker datePicker, TimePicker timePicker) {
		Calendar calendar = Calendar.getInstance();
		if (!(null == initDateTime || "".equals(initDateTime))) {
			calendar = this.getCalendarByInintData(initDateTime);
		}

		datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
				calendar.get(Calendar.DAY_OF_MONTH), this);
		timePicker.setCurrentHour(calendar.get(Calendar.HOUR_OF_DAY));
		timePicker.setCurrentMinute(calendar.get(Calendar.MINUTE));
	}

	/**
	 * 弹出日期时间选择框方法
	 * 
	 * @param inputDate
	 *            :为需要设置的日期时间文本编辑框
	 * @return
	 */
	public AlertDialog dateTimePicKDialog(final EditText inputDate) {
		LinearLayout dateTimeLayout = (LinearLayout) activity.getLayoutInflater()
				.inflate(R.layout.common_datetime, null);

		datePicker = (DatePicker) dateTimeLayout.findViewById(R.id.datepicker);
		datePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
		timePicker = (TimePicker) dateTimeLayout.findViewById(R.id.timepicker);
		timePicker.setDescendantFocusability(TimePicker.FOCUS_BLOCK_DESCENDANTS);
		timePicker.setIs24HourView(true);

		init(datePicker, timePicker);
		timePicker.setIs24HourView(true);
		timePicker.setOnTimeChangedListener(this);

		ad = new AlertDialog.Builder(activity).setView(dateTimeLayout)
				.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (Utils.parseDateInFormat1(dateTime).before(
								// 过滤秒，将比较级别扩大到分钟
								new Date(System.currentTimeMillis() / (1000 * 60) * (1000 * 60)))) {
							Toast.makeText(activity, "选择时间小于当前时间，请重新选择", Toast.LENGTH_SHORT).show();
						} else {
							if (isBegin)
								listener.onDateTimeSelected(dateTime, isBegin);
							else {
								if (Utils.parseDateInFormat1(beginTime)
										.before(Utils.parseDateInFormat1(dateTime))) {
									listener.onDateTimeSelected(dateTime, isBegin);
								} else {
									Toast.makeText(activity, "计划结束时间应迟于计划开始时间，请重新选择",
											Toast.LENGTH_SHORT).show();
								}
							}
						}
					}
				}).show();

		onDateChanged(null, 0, 0, 0);
		return ad;
	}

	public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
		onDateChanged(null, 0, 0, 0);
	}

	public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		// 获得日历实例
		Calendar calendar = Calendar.getInstance();

		calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
				timePicker.getCurrentHour(), timePicker.getCurrentMinute());
		dateTime = Utils.formatDateMs(calendar.getTime().getTime());
	}

	/**
	 * 实现将初始日期时间2012年07月02日 16:45 拆分成年 月 日 时 分 秒,并赋值给calendar
	 * 
	 * @param initDateTime
	 *            初始日期时间值 字符串型
	 * @return Calendar
	 */
	private Calendar getCalendarByInintData(String initDateTime) {
		Calendar calendar = Calendar.getInstance();

		Date date = Utils.parseDateInFormat1(initDateTime);
		if (date != null) {
			calendar.set(date.getYear() + 1900, date.getMonth(), date.getDate(), date.getHours(),
					date.getMinutes());
		}
		return calendar;
	}

}
