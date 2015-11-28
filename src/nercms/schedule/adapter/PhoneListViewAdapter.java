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

	private String userID = null; // ����ID
	private int phoneInOut; // �绰�����ʾ��0-������1-����

	public PhoneListViewAdapter(Context context, ArrayList<PhoneModel> phoneList) {
		this.context = context;
		this.phoneList = phoneList;
		this.personDao = daoFactory.getPersonDao(context);
		this.phoneDao = daoFactory.getPhoneDao(context);

		// �����û�ID
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

		// �绰�����ж�
		if (phone.getType() == 1) { // IP�绰

			final String objectID; // �绰�����ID

			if (userID.equals(String.valueOf(phone.getCallerID()))) { // ����Ϊ������
				objectID = Integer.toString(phone.getCalleeID()); // �绰������Ϊ��������
				phoneInOut = 0; // Ϊ�����绰
			} else {// ����Ϊ��������
				objectID = Integer.toString(phone.getCallerID()); // �绰������Ϊ������
				phoneInOut = 1; // Ϊ����绰
			}

			// �绰��������
			String name = personDao.getSSMByID(objectID).getName();
			holder.nameText.setText(name + " (IP�绰)");
			// ���п�ʼʱ��
			holder.startTimeText.setText(phone.getStartTime());
			// ����״̬ͼ��
			if (phoneInOut == 1) { // ����
				// holder.statusImage.setBackground(context.getResources()
				// .getDrawable(R.drawable.phone_in));
				holder.statusImage.setImageResource(R.drawable.phone_in);
			} else {

				holder.statusImage.setImageResource(R.drawable.phone_out);
			}

			// �����Ƿ��ͨ
			if (phone.getIsAnswered() == 1) {
				holder.isAnsweredText.setText("�ѽ�ͨ");
				holder.durationText.setText(phone.getDuration());
			} else { // δ��ͨ
				holder.isAnsweredText.setText("δ��ͨ");
				holder.durationText.setVisibility(View.INVISIBLE);
				// ����Ǻ���绰δ��ͨ����ͼ����ʾphoneMissedδ��
				if (phoneInOut == 1) {
					holder.statusImage
							.setImageResource(R.drawable.phone_missed);
				}
			}

			// �����ת���绰�������
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
							.setTitle("ɾ��")
							.setMessage("ȷ��ɾ����IPͨ����¼��")
							.setPositiveButton("ɾ��",
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
									}).setNegativeButton("ȡ��", null).create()
							.show();
					return false;
				}
			});

		} else { // ���ص绰

			final String nameORnumber; // �����洢�����֣����ߵ绰����
			if (phone.getCallerName() == null
					|| phone.getCallerName().equalsIgnoreCase("")) {
				nameORnumber = phone.getEndTime();
				holder.nameText.setText(phone.getEndTime()); // ����ͨѶ¼û�����֣�������ʾ�绰����
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
							.setTitle("���� " + nameORnumber + " ?")
							.setPositiveButton("����",
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
									}).setNegativeButton("ȡ��", null).create()
							.show();
				}
			});

			convertView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View arg0) {
					new AlertDialog.Builder(context)
							.setTitle("ɾ��")
							.setMessage("ȷ��ɾ���ñ���ͨ����¼��")
							.setPositiveButton("ɾ��",
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
									}).setNegativeButton("ȡ��", null).create()
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
