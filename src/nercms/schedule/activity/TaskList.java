package nercms.schedule.activity;

import java.util.Date;
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
	// 统计界面进入 4 表示今天 5表示昨天
	int enterType;
	// status 0,新的任务；1,执行中；2,延误；3,完成；4,取消任务。 null则不查询此字段
	String status;

	PlanTaskDao planTaskDao;
	List<tb_task_info> data;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_list);

		enterType = getIntent().getIntExtra("enterType", 1);
		status = getIntent().getStringExtra("statue");

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
		case 4:
			StringBuilder sb = new StringBuilder();
			// status 0,新的任务；1,执行中；2,延误；3,完成；4,取消任务。 null则不查询此字段
			switch (Integer.parseInt(status)) {
			case 0:
				sb.append("未执行的任务");
				break;
			case 1:
				sb.append("正在执行的任务");
				break;
			case 2:
				sb.append("延误完成的任务");
				break;
			case 3:
				sb.append("正常完成的任务");
				break;
			case 4:
				sb.append("取消的任务");
				break;
			}
			sb.append(" - 今天");
			iniActionBar(true, null, sb.toString());
			break;
		case 5:
			StringBuilder sb1 = new StringBuilder();
			// status 0,新的任务；1,执行中；2,延误；3,完成；4,取消任务。 null则不查询此字段
			switch (Integer.parseInt(status)) {
			case 0:
				sb1.append("未执行的任务");
				break;
			case 1:
				sb1.append("正在执行的任务");
				break;
			case 2:
				sb1.append("延误完成的任务");
				break;
			case 3:
				sb1.append("正常完成的任务");
				break;
			case 4:
				sb1.append("取消的任务");
				break;
			}
			sb1.append(" - 昨天");
			iniActionBar(true, null, sb1.toString());
			break;
		}

		planTaskDao = new PlanTaskDao(this);

		bt_rjhlr = (Button) findViewById(R.id.rijihualuru);
		listView = (ListView) findViewById(R.id.task_list);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// 当当前登录用户非管理员，则不能录入计划
		if (isAdmin() == PERSON_TYPE.XIANCHANG) {
			bt_rjhlr.setVisibility(View.GONE);
			if (enterType < 4)
				data = planTaskDao.getPlanTasks(enterType, 3, getUserId(), status);
			else {
				Date tmp = new Date(System.currentTimeMillis());
				Date today = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate() + 1);
				Date yesterday = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate());
				Date theDayBeforeYesterday = new Date(tmp.getYear(), tmp.getMonth(),
						tmp.getDate() - 1);
				// 4 表示今天 5表示昨天
				if (enterType == 4)
					data = planTaskDao.getPlanTasks(enterType, 3, getUserId(), status,
							yesterday.getTime() + "", today.getTime() + "");
				else if (enterType == 5)
					data = planTaskDao.getPlanTasks(enterType, 3, getUserId(), status,
							theDayBeforeYesterday.getTime() + "", yesterday.getTime() + "");
			}
		} else {

			if (enterType < 4)
				data = planTaskDao.getPlanTasks(enterType, 3, null, status);
			else {
				Date tmp = new Date(System.currentTimeMillis());
				Date today = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate() + 1);
				Date yesterday = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate());
				Date theDayBeforeYesterday = new Date(tmp.getYear(), tmp.getMonth(),
						tmp.getDate() - 1);
				// 4 表示今天 5表示昨天
				if (enterType == 4)
					data = planTaskDao.getPlanTasks(enterType, 3, null, status,
							yesterday.getTime() + "", today.getTime() + "");
				else if (enterType == 5)
					data = planTaskDao.getPlanTasks(enterType, 3, null, status,
							theDayBeforeYesterday.getTime() + "", yesterday.getTime() + "");
			}

			bt_rjhlr.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TaskList.this, PlanAdd.class);
					intent.putExtra("enterType", enterType);
					intent.putExtra("statue", status);
					TaskList.this.startActivity(intent);
				}
			});
		}

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