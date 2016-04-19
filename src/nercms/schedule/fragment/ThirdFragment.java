package nercms.schedule.fragment;

import nercms.schedule.R;
import nercms.schedule.activity.MainContent;
import nercms.schedule.activity.TaskList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.wxapp.service.AppApplication;

public class ThirdFragment extends Fragment implements OnClickListener {
	private View view;
	Context context;
	int mcount = 0;
	private Button mNewTask;
	private Button bt_delayedTask;
	private Button bt_performingTask;
	private Button bt_completedTask;
	private Button bt_cancelledTask;
	private com.jauker.widget.BadgeView badge;

	public ThirdFragment(Context context, int count) {
		this.context = context;
		mcount = count;
	}

	public ThirdFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.tab03, container, false);
		mNewTask = (Button) view.findViewById(R.id.tab03_bt_newtask);
		bt_delayedTask = (Button) view.findViewById(R.id.tab03_bt_delaytask);
		bt_performingTask = (Button) view.findViewById(R.id.tab03_bt_performtask);
		bt_completedTask = (Button) view.findViewById(R.id.tab03_bt_finishedtask);
		bt_cancelledTask = (Button) view.findViewById(R.id.tab03_bt_cancelledtask);
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.newtasklayout);

		// badge = new com.jauker.widget.BadgeView(context);
		badge = new com.jauker.widget.BadgeView(
				AppApplication.getInstance().getApplicationContext());
		badge.setText(mcount + "");
		badge.setTargetView(mNewTask);

		mNewTask.setOnClickListener(this);
		bt_delayedTask.setOnClickListener(this);
		bt_performingTask.setOnClickListener(this);
		bt_completedTask.setOnClickListener(this);
		bt_cancelledTask.setOnClickListener(this);
		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		MainContent activity = (MainContent) getActivity();
		badge.setText(activity.qiangxiucount + "");
	}

	public com.jauker.widget.BadgeView getBadgeView1() {
		if (badge == null) {
			badge = new com.jauker.widget.BadgeView(
					AppApplication.getInstance().getApplicationContext());
			badge.setText(mcount + "");
			badge.setTargetView(mNewTask);

		}
		return badge;
	}

	@Override
	public void onClick(View v) {
		if (context == null)
			context = getActivity();
		Intent intent = new Intent(context, TaskList.class);
		// 0,�µ�����1,ִ���У�2,����3,��ɣ�4,ȡ������ null�򲻲�ѯ���ֶ�
		switch (v.getId()) {
		case R.id.tab03_bt_newtask:
			intent.putExtra("enterType", 3);
			intent.putExtra("statue", "0");
			startActivity(intent);
			break;

		case R.id.tab03_bt_delaytask:
			intent.putExtra("enterType", 3);
			intent.putExtra("statue", "2");
			startActivity(intent);
			break;

		case R.id.tab03_bt_performtask:
			intent.putExtra("enterType", 3);
			intent.putExtra("statue", "1");
			startActivity(intent);
			break;

		case R.id.tab03_bt_finishedtask:
			intent.putExtra("enterType", 3);
			intent.putExtra("statue", "3");
			startActivity(intent);
			break;

		case R.id.tab03_bt_cancelledtask:
			intent.putExtra("enterType", 3);
			intent.putExtra("statue", "4");
			startActivity(intent);
			break;

		default:
			break;
		}

	}
}
