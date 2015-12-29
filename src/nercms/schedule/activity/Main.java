package nercms.schedule.activity;

import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import nercms.schedule.R;
import nercms.schedule.adapter.MainPagerAdapter;

public class Main extends BaseActivity {

	ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		iniActionBar(null, "ÈÎÎñ");

		listview = (ListView) findViewById(R.id.listView1);
		listview.setAdapter(new MainPagerAdapter(this));

		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(Main.this, TaskList.class);
				intent.putExtra("enterType", position + 1);
				Main.this.startActivity(intent);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
