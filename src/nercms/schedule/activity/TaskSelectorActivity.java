package nercms.schedule.activity;

import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.dao.TaskInsDao;
import android.wxapp.service.elec.model.bean.table.tb_task_info;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;

public class TaskSelectorActivity extends BaseActivity implements OnClickListener {

	PlanTaskDao dao;
	tb_task_info info;
	String tid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_task_selector);

		dao = new PlanTaskDao(this);

		tid = getIntent().getStringExtra("tid");
		info = dao.getPlanTask(tid);
		iniActionBar(true, null, info.getName());

		findViewById(R.id.shangchuanxianchangxinxi).setOnClickListener(this);
		findViewById(R.id.jiaohuxinxi).setOnClickListener(this);
		findViewById(R.id.renwuxiangqing).setOnClickListener(this);
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
			
			break;
		case R.id.renwuxiangqing:
			intent.setClass(TaskSelectorActivity.this, PlanAdd.class);
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
