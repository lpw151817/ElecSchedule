package nercms.schedule.activity;

import com.actionbarsherlock.view.MenuItem;
import com.baidu.location.b.h;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.dao.TaskInsDao;
import android.wxapp.service.elec.model.StartTaskResponse;
import android.wxapp.service.elec.model.bean.table.tb_task_info;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;

public class TaskSelectorActivity extends BaseActivity implements OnClickListener {

	PlanTaskDao dao;
	tb_task_info info;
	String tid;

	WebRequestManager manager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_selector);

		tid = getIntent().getStringExtra("tid");
		dao = new PlanTaskDao(this);
		info = dao.getPlanTask(tid);

		if (!isAdmin()) {
			if (TextUtils.isEmpty(info.getStart_time())) {
				manager = new WebRequestManager(AppApplication.getInstance(), this);
				iniHandler();
				manager.startTask(this, tid, System.currentTimeMillis() + "");
			}
		}

		iniActionBar(true, null, info.getName());

		if (isAdmin())
			((TextView) findViewById(R.id.rwxq)).setText("查看现场信息");

		findViewById(R.id.shangchuanxianchangxinxi).setOnClickListener(this);
		findViewById(R.id.jiaohuxinxi).setOnClickListener(this);
		findViewById(R.id.renwuxiangqing).setOnClickListener(this);
	}

	Handler handler;

	private void iniHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Constants.START_TASK_SUCCESS:
					showLog_e("任务开始成功");
					break;

				case Constants.START_TASK_SAVE_FAIL:
				case Constants.START_TASK_FAIL:
					showLog_e("任务开始失败");
					break;
				}
			}
		};
		MessageHandlerManager.getInstance().register(handler, Constants.START_TASK_SUCCESS,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.START_TASK_SAVE_FAIL,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.START_TASK_FAIL,
				StartTaskResponse.class.getName());
	}

	@Override
	protected void onDestroy() {
		MessageHandlerManager.getInstance().unregister(Constants.START_TASK_SUCCESS,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.START_TASK_SAVE_FAIL,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.START_TASK_FAIL,
				StartTaskResponse.class.getName());
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.shangchuanxianchangxinxi:
			intent.setClass(TaskSelectorActivity.this, XianChangAdd.class);
			intent.putExtra("tid", tid);
			if (info.getCategory().equals("category01")) {
				intent.putExtra("enterType", 1);
			} else if (info.getCategory().equals("category02")) {
				intent.putExtra("enterType", 2);
			} else if (info.getCategory().equals("category03")) {
				intent.putExtra("enterType", 3);
			}
			intent.putExtra("isContinueTask", true);
			break;
		case R.id.jiaohuxinxi:
			intent.setClass(TaskSelectorActivity.this, ChatDetail.class);
			intent.putExtra("task_id", tid);
			if (info != null) {
				if (TextUtils.isEmpty(info.getEnd_time())) {
					intent.putExtra("task_status", 1);
				} else {
					intent.putExtra("task_status", 2);
				}
			}
			break;
		case R.id.renwuxiangqing:
			// intent.setClass(TaskSelectorActivity.this, PlanAdd.class);
			intent.setClass(TaskSelectorActivity.this, DailyTaskDetail.class);
			intent.putExtra("enterType", 0);
			intent.putExtra("tid", tid);
			break;
		}
		TaskSelectorActivity.this.startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
