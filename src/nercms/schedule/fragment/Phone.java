package nercms.schedule.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import nercms.schedule.R;
import nercms.schedule.adapter.PhoneListViewAdapter;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.CallLog.Calls;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PhoneDao;
import android.wxapp.service.model.PhoneModel;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockFragment;

public class Phone extends SherlockFragment {

	private DAOFactory daoFactory = DAOFactory.getInstance();
	private PhoneListViewAdapter phoneAdapter;
	private ListView phoneListView;
	private PhoneDao phoneDao;
	private String userID;

	public static Phone newInstance() {
		Phone phoneFragment = new Phone();
		return phoneFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.phone_fragment, null);

		userID = MySharedPreference.get(getActivity(),
				MySharedPreference.USER_ID, "");
		phoneListView = (ListView) v.findViewById(R.id.phone_list);

		initData();

		return v;
	}

	private void initData() {
		// phoneDao = daoFactory.getPhoneDao(getActivity());
		// ArrayList<PhoneModel> phoneList = phoneDao.getPhoneListByID(userID);
		// // 2014-6-13 WeiHao ����ͨ����¼�Ļ�ȡ
		// Cursor cursor = getActivity().getContentResolver().query(
		// CallLog.Calls.CONTENT_URI, null, null, null, null);
		// PhoneModel localCall;
		// if (cursor.moveToFirst()) {
		// do {
		//
		// String id = String.valueOf(cursor.getInt(cursor
		// .getColumnIndexOrThrow(Calls._ID)));
		// String number = cursor.getString(cursor
		// .getColumnIndex(Calls.NUMBER));
		//
		// String name = cursor.getString(cursor
		// .getColumnIndexOrThrow(Calls.CACHED_NAME));
		// SimpleDateFormat sfd = new SimpleDateFormat(
		// "yyyy-MM-dd HH:mm:ss");
		// Date date = new Date(Long.parseLong(cursor.getString(cursor
		// .getColumnIndexOrThrow(Calls.DATE))));
		// String time = sfd.format(date);
		// String duration = cursor.getString(cursor
		// .getColumnIndexOrThrow(Calls.DURATION));
		//
		// int callType;
		// switch (Integer.parseInt(cursor.getString(cursor
		// .getColumnIndex(Calls.TYPE)))) {
		// case Calls.INCOMING_TYPE:
		// callType = 1;
		// break;
		// case Calls.OUTGOING_TYPE:
		// callType = 2;
		// break;
		// case Calls.MISSED_TYPE:
		// callType = 3;
		// break;
		// default:
		// callType = 4;
		// break;
		// }
		//
		// // 2014-7-11
		// // ���ص绰��PhoneID����ΪCalls._ID
		// // ���ص绰��EndTime����ΪCalls.NUMBER
		//
		// localCall = new PhoneModel(id, 2, 0, 0, time, 0, number,
		// duration,
		// 0);
		// localCall.setCallType(callType);
		// localCall.setCallerName(name);
		// // �������в��뵽�绰������
		// phoneList.add(localCall);
		//
		// } while (cursor.moveToNext());
		// }
		//
		// // �Բ����ĵ绰�б����°���ʱ������
		// // ��������Ϣ�б��շ���ʱ�������������
		// for (int i = 0; i < phoneList.size(); i++) {
		// Collections.sort(phoneList, new Comparator<PhoneModel>() {
		//
		// @Override
		// public int compare(PhoneModel p1, PhoneModel p2) {
		// return p2.getStartTime().compareTo(p1.getStartTime());
		// }
		//
		// });
		// }
		//
		// phoneAdapter = new PhoneListViewAdapter(getActivity(), phoneList);
		// phoneListView.setAdapter(phoneAdapter);

	}

	@Override
	public void onResume() {
		super.onResume();
		initData();
	}

}