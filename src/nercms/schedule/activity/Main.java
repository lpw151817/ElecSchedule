package nercms.schedule.activity;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

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

		iniActionBar(true, R.drawable.iconfont_xinxi_max, "任务");

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
			Intent intent = new Intent(Main.this, Login.class);
			Main.this.startActivity(intent);
			Main.this.finish();
			break;
		case -1:
			showShortToast("指令");
			break;
		case 1:
			showShortToast("指挥调度");
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// 搜索按钮 隐藏
		MenuItem search = menu.add(0, -1, 0, "search");
		search.setIcon(R.drawable.actionbar_icon);
		search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		// 子菜单 ：发起任务，消息，会议，视频直播，智能调度，添加客户
		SubMenu addMenu = menu.addSubMenu("add item");
		addMenu.add(0, 1, 0, "指挥调度").setIcon(R.drawable.btn_tips);

		MenuItem addItem = addMenu.getItem();
		addItem.setIcon(R.drawable.ic_action_overflow);
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);// 总是作为Action项显示
		return super.onCreateOptionsMenu(menu);
	}
}
