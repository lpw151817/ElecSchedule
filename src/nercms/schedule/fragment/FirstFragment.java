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
import android.widget.TextView;
import android.wxapp.service.AppApplication;


public class FirstFragment extends Fragment implements OnClickListener {

	private View view;
	Context context;
	int mcount = 0;
	private Button bt_delayedTask;
	private Button bt_performingTask;
	private Button bt_completedTask;
	private Button bt_cancelledTask;
	private Button mNewTask;
	private com.jauker.widget.BadgeView badge;

	public FirstFragment(Context context, int count) {
		this.context = context;
		mcount = count;
	}

	public FirstFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.tab01, container, false);

		mNewTask = (Button) view.findViewById(R.id.tab01_bt_newtask);
		bt_delayedTask = (Button) view.findViewById(R.id.tab01_bt_delaytask);
		bt_performingTask = (Button) view.findViewById(R.id.tab01_bt_performtask);
		bt_completedTask = (Button) view.findViewById(R.id.tab01_bt_finishedtask);
		bt_cancelledTask = (Button) view.findViewById(R.id.tab01_bt_cancelledtask);
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.newtasklayout);
//		badge = new com.jauker.widget.BadgeView(context);
		badge = new com.jauker.widget.BadgeView(AppApplication.getInstance().getApplicationContext());
		
		badge.setText(mcount + "");
		badge.setTargetView(mNewTask);

		mNewTask.setOnClickListener(this);
		bt_delayedTask.setOnClickListener(this);
		bt_performingTask.setOnClickListener(this);
		bt_completedTask.setOnClickListener(this);
		bt_cancelledTask.setOnClickListener(this);
		return view;
	}
	
	public com.jauker.widget.BadgeView getBadgeView1(){
		return badge;
	}

	@Override
	public void onResume() {
		super.onResume();
		MainContent activity = (MainContent) getActivity();
		badge.setText(activity.zuoyecount + "");
	}

	@Override
	public void onClick(View v) {

		Intent intent = new Intent(context, TaskList.class);
		// 0,新的任务；1,执行中；2,延误；3,完成；4,取消任务。 null则不查询此字段
		switch (v.getId()) {
		case R.id.tab01_bt_newtask:
			intent.putExtra("enterType", 1);
			intent.putExtra("statue", "0");
			startActivity(intent);
			break;

		case R.id.tab01_bt_delaytask:
			intent.putExtra("enterType", 1);
			intent.putExtra("statue", "2");
			startActivity(intent);
			break;

		case R.id.tab01_bt_performtask:
			intent.putExtra("enterType", 1);
			intent.putExtra("statue", "1");
			startActivity(intent);
			break;

		case R.id.tab01_bt_finishedtask:
			intent.putExtra("enterType", 1);
			intent.putExtra("statue", "3");
			startActivity(intent);
			break;

		case R.id.tab01_bt_cancelledtask:
			intent.putExtra("enterType", 1);
			intent.putExtra("statue", "4");
			startActivity(intent);
			break;

		default:
			break;
		}

	}

}
