package nercms.schedule.activity;

import java.util.List;

import com.actionbarsherlock.view.MenuItem;
import com.android.volley.NetworkError;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.model.bean.table.tb_task_info;
import nercms.schedule.R;
import nercms.schedule.adapter.XianChangAddAdapter;
import nercms.schedule.adapter.XianchangAdapter;

/**
 * 6.10 FINAL
 * 
 * @author JerryLiu
 *
 */
public class TaskList extends BaseActivity {

	ListView listView;
	Button bt_rjhlr;

	int enterType;

	PlanTaskDao planTaskDao;
	List<tb_task_info> data;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_list);

		enterType = getIntent().getIntExtra("enterType", 1);

		switch (enterType) {
		case 1:
			iniActionBar(true, null, "作业现场");
			break;
		case 2:
			iniActionBar(true, null, "操作现场");
			break;
		case 3:
			iniActionBar(true, null, "故障紧急抢修现场");
			break;
		}

		planTaskDao = new PlanTaskDao(this);

		bt_rjhlr = (Button) findViewById(R.id.rijihualuru);
		// 当当前登录用户非管理员，则不能录入计划
		if (!isAdmin()) {
			bt_rjhlr.setVisibility(View.GONE);
			data = planTaskDao.getPlanTasks(enterType, 3, getUserId(), null);
		} else {
			data = planTaskDao.getPlanTasks(enterType, 3, null, null);
			bt_rjhlr.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TaskList.this, PlanAdd.class);
					intent.putExtra("enterType", enterType);
					TaskList.this.startActivity(intent);
				}
			});

		}

		listView = (ListView) findViewById(R.id.task_list);
		listView.setAdapter(new XianchangAdapter(this, enterType, data));

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 改跳转
				Intent intent = new Intent(TaskList.this, TaskSelectorActivity.class);
				intent.putExtra("enterType", 0);
				intent.putExtra("tid", data.get(position).getId());
				TaskList.this.startActivity(intent);

				// // item点击事件，用于查询任务详情
				// Intent intent = new Intent(TaskList.this, PlanAdd.class);
				// intent.putExtra("enterType", 0);
				// intent.putExtra("tid", data.get(position).getId());
				// TaskList.this.startActivity(intent);
			}
		});
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