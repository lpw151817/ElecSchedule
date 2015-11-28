package nercms.schedule.fragment;

import java.util.ArrayList;

import nercms.schedule.R;
import nercms.schedule.adapter.MeetingListViewAdapter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.wxapp.service.dao.ConferenceDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.model.ConferenceModel;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockFragment;

public class Meeting extends SherlockFragment {

	private MeetingListViewAdapter meetingAdapter;
	private ListView meetingListView;
	private String userID;

	public static Meeting newInstance() {
		Meeting meetingFragment = new Meeting();
		return meetingFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.meeting_fragment, null);

		userID = MySharedPreference.get(getActivity(), MySharedPreference.USER_ID, "");

		meetingListView = (ListView) v.findViewById(R.id.meeting_list);

		initData();

		return v;
	}

	private void initData() {
		meetingAdapter = new MeetingListViewAdapter(getActivity(), userID);
		meetingListView.setAdapter(meetingAdapter);
	}

}
