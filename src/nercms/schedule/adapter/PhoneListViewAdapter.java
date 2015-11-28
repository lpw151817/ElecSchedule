package nercms.schedule.adapter;

import java.util.ArrayList;

import nercms.schedule.R;
import nercms.schedule.activity.PhoneDetail;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.CallLog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.dao.PhoneDao;
import android.wxapp.service.model.PhoneModel;
import android.wxapp.service.util.MySharedPreference;

public class PhoneListViewAdapter extends BaseAdapter {

	private DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao;
	private PhoneDao phoneDao;

	private Intent intent;
	private Context context;
	private ArrayList<PhoneModel> phoneList;

	private String userID = null; // 本人ID
	private int phoneInOut; // 电话出入标示：0-呼出，1-呼入

	public PhoneListViewAdapter(Context context, ArrayList<PhoneModel> phoneList) {
		this.context = context;
		this.phoneList = phoneList;
		this.personDao = daoFactory.getPersonDao(context);
		this.phoneDao = daoFactory.getPhoneDao(context);

		// 设置用户ID
		userID = MySharedPreference
				.get(context, MySharedPreference.USER_ID, "");
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return phoneList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return phoneList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@SuppressLint("NewApi")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {

		Holder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.phone_fragment_item, null);
			holder = new Holder();
			holder.nameText = (TextView) convertView
					.findViewById(R.id.phone_name_tv);
			holder.startTimeText = (TextView) convertView
					.findViewById(R.id.phone_start_time_tv);
			holder.isAnsweredText = (TextView) convertView
					.findViewById(R.id.phone_is_answered_tv);
			holder.durationText = (TextView) convertView
					.findViewById(R.id.phone_duration_tv);
			holder.statusImage = (ImageView) convertView
					.findViewById(R.id.phone_status_iv);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		final PhoneModel phone = phoneList.get(position);

		// 电话类型判断
		if (phone.getType() == 1) { // IP电话

			final String objectID; // 电话对象的ID

			if (userID.equals(String.valueOf(phone.getCallerID()))) { // 本人为呼叫者
				objectID = Integer.toString(phone.getCalleeID()); // 电话对象置为被呼叫者
				phoneInOut = 0; // 为呼出电话
			} else {// 本人为被呼叫者
				objectID = Integer.toString(phone.getCallerID()); // 电话对象置为呼叫者
				phoneInOut = 1; // 为呼入电话
			}

			// 电话对象姓名
			String name = personDao.getSSMByID(objectID).getName();
			holder.nameText.setText(name + " (IP电话)");
			// 呼叫开始时间
			holder.startTimeText.setText(phone.getStartTime());
			// 呼叫状态图标
			if (phoneInOut == 1) { // 呼入
				// holder.statusImage.setBackground(context.getResources()
				// .getDrawable(R.drawable.phone_in));
				holder.statusImage.setImageResource(R.drawable.phone_in);
			} else {

				holder.statusImage.setImageResource(R.drawable.phone_out);
			}

			// 呼叫是否接通
			if (phone.getIsAnswered() == 1) {
				holder.isAnsweredText.setText("已接通");
				holder.durationText.setText(phone.getDuration());
			} else { // 未接通
				holder.isAnsweredText.setText("未接通");
				holder.durationText.setVisibility(View.INVISIBLE);
				// 如果是呼入电话未接通，则图标显示phoneMissed未接
				if (phoneInOut == 1) {
					holder.statusImage
							.setImageResource(R.drawable.phone_missed);
				}
			}

			// 点击跳转到电话详情界面
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					intent = new Intent(context, PhoneDetail.class);
					intent.putExtra("phone_id", phone.getPhoneID());
					intent.putExtra("object_id", objectID);
					context.startActivity(intent);

				}
			});

			convertView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View arg0) {
					new AlertDialog.Builder(context)
							.setTitle("删除")
							.setMessage("确定删除该IP通话记录？")
							.setPositiveButton("删除",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											phoneDao.deletePhoneByID(phone
													.getPhoneID());
											phoneList.remove(position);
											PhoneListViewAdapter.this
													.notifyDataSetChanged();
										}
									}).setNegativeButton("取消", null).create()
							.show();
					return false;
				}
			});

		} else { // 本地电话

			final String nameORnumber; // 本机存储的名字，或者电话号码
			if (phone.getCallerName() == null
					|| phone.getCallerName().equalsIgnoreCase("")) {
				nameORnumber = phone.getEndTime();
				holder.nameText.setText(phone.getEndTime()); // 本地通讯录没有名字，设置显示电话号码
			} else {
				nameORnumber = phone.getCallerName();
				holder.nameText.setText(phone.getCallerName() + " ("
						+ phone.getEndTime() + ")");
			}

			holder.startTimeText.setText(phone.getStartTime());

			if (phone.getCallType() == 1) {
				holder.statusImage.setImageResource(R.drawable.phone_in);
			} else if (phone.getCallType() == 2) {
				holder.statusImage.setImageResource(R.drawable.phone_out);
			} else if (phone.getCallType() == 3) {
				holder.statusImage.setImageResource(R.drawable.phone_missed);
			}

			holder.durationText.setText(phone.getDuration());

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					new AlertDialog.Builder(context)
							.setTitle("呼叫 " + nameORnumber + " ?")
							.setPositiveButton("呼叫",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {
											Intent intent = new Intent(
													Intent.ACTION_CALL,
													Uri.parse("tel:"
															+ phone.getEndTime()));
											context.startActivity(intent);
										}
									}).setNegativeButton("取消", null).create()
							.show();
				}
			});

			convertView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View arg0) {
					new AlertDialog.Builder(context)
							.setTitle("删除")
							.setMessage("确定删除该本地通话记录？")
							.setPositiveButton("删除",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface arg0, int arg1) {

											context.getContentResolver()
													.delete(CallLog.Calls.CONTENT_URI,
															"_id=?",
															new String[] { phone
																	.getPhoneID()
																	+ "" });

											phoneList.remove(position);
											PhoneListViewAdapter.this
													.notifyDataSetChanged();
										}
									}).setNegativeButton("取消", null).create()
							.show();
					return false;
				}
			});

		}

		return convertView;
	}

	class Holder {
		TextView nameText, startTimeText, isAnsweredText, durationText;
		ImageView statusImage;
	}

}
