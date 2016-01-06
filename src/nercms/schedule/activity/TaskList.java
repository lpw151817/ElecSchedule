package nercms.schedule.activity;

import com.actionbarsherlock.view.MenuItem;
import com.android.volley.NetworkError;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.wxapp.service.elec.dao.PlanTaskDao;
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

		bt_rjhlr = (Button) findViewById(R.id.rijihualuru);

		bt_rjhlr.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(TaskList.this, PlanAdd.class);
				intent.putExtra("enterType", enterType);
				TaskList.this.startActivity(intent);
			}
		});

		planTaskDao = new PlanTaskDao(this);

		listView = (ListView) findViewById(R.id.task_list);
		listView.setAdapter(
				new XianchangAdapter(this, enterType, planTaskDao.getAllPlanTask(enterType, 3)));
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