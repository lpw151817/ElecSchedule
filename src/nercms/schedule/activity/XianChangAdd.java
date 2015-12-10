package nercms.schedule.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;
import nercms.schedule.R.menu;
import nercms.schedule.adapter.XianChangAddAdapter;

public class XianChangAdd extends BaseActivity {

	ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_add);

		mListView = (ListView) findViewById(R.id.listview);
		int[] hasContent = { 1, 1, 1, 1, 0, 0 };
		mListView.setAdapter(new XianChangAddAdapter(this, 1, hasContent));

	}

}
