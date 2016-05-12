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
 * ����ʱ��ѡ��ؼ� ʹ�÷����� private EditText inputDate;//��Ҫ���õ�����ʱ���ı��༭�� private String
 * initDateTime="2012��9��3�� 14:44",//��ʼ����ʱ��ֵ �ڵ���¼���ʹ�ã�
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
	// ����Ƿ�Ϊ��ʼ����
	private boolean isBegin;
	private String beginTime;

	/**
	 * ����ʱ�䵯��ѡ����캯��
	 * 
	 * @param activity
	 *            �����õĸ�activity
	 * @param initDateTime
	 *            ��ʼ����ʱ��ֵ����Ϊ�������ڵı��������ʱ���ʼֵ
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
	 * ��������ʱ��ѡ��򷽷�
	 * 
	 * @param inputDate
	 *            :Ϊ��Ҫ���õ�����ʱ���ı��༭��
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
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (Utils.parseDateInFormat1(dateTime).before(
								// �����룬���Ƚϼ������󵽷���
								new Date(System.currentTimeMillis() / (1000 * 60) * (1000 * 60)))) {
							Toast.makeText(activity, "ѡ��ʱ��С�ڵ�ǰʱ�䣬������ѡ��", Toast.LENGTH_SHORT).show();
						} else {
							if (isBegin)
								listener.onDateTimeSelected(dateTime, isBegin);
							else {
								if (Utils.parseDateInFormat1(beginTime)
										.before(Utils.parseDateInFormat1(dateTime))) {
									listener.onDateTimeSelected(dateTime, isBegin);
								} else {
									Toast.makeText(activity, "�ƻ�����ʱ��Ӧ���ڼƻ���ʼʱ�䣬������ѡ��",
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
		// �������ʵ��
		Calendar calendar = Calendar.getInstance();

		calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
				timePicker.getCurrentHour(), timePicker.getCurrentMinute());
		dateTime = Utils.formatDateMs(calendar.getTime().getTime());
	}

	/**
	 * ʵ�ֽ���ʼ����ʱ��2012��07��02�� 16:45 ��ֳ��� �� �� ʱ �� ��,����ֵ��calendar
	 * 
	 * @param initDateTime
	 *            ��ʼ����ʱ��ֵ �ַ�����
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
