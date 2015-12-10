package nercms.schedule.activity;

import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;
import nercms.schedule.R.menu;
import nercms.schedule.adapter.XianChangSiAdapter;
import nercms.schedule.adapter.XianchangAdapter;

public class XianChangSi extends BaseActivity {

	ListView mListView;
	// 1:��ҵ�ֳ���2�������ֳ���3�������ֳ�
	int type = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_si);

		initActionBar();

		mListView = (ListView) findViewById(R.id.listView1);
		mListView.setAdapter(new XianChangSiAdapter(this, type));
	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		switch (type) {
		case 1:
			getSupportActionBar().setTitle("��ҵ�ֳ�");
			break;
		case 2:
			getSupportActionBar().setTitle("�����ֳ�");
			break;
		case 3:
			getSupportActionBar().setTitle("���Ͻ��������ֳ�");
		}
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
