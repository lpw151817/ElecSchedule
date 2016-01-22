package nercms.schedule.activity;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.wxapp.service.elec.dao.TaskInsDao;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;
import nercms.schedule.R.menu;
import nercms.schedule.adapter.ZhilingAdapter;

public class ZhilingActivity extends BaseActivity {

	ListView listview;
	ZhilingAdapter adapter;
	TaskInsDao dao;
	List<tb_task_instructions> data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zhiling);

		iniActionBar(true, null, "¡Ÿ ±÷∏¡Ó");

		dao = new TaskInsDao(this);
		data = dao.getAllIns();

		listview = (ListView) findViewById(R.id.listview);
		adapter = new ZhilingAdapter(this, data);
		listview.setAdapter(adapter);

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(ZhilingActivity.this, NewTask.class);
				intent.putExtra("taskInsId", data.get(position).getId());
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
