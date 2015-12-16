package nercms.schedule.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;
import nercms.schedule.adapter.MeiRiJiHuaAdapter;

public class MeiRiJiHua extends Activity {

	ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mei_ri_ji_hua);

		mListView = (ListView) findViewById(R.id.listView1);
		mListView.setAdapter(new MeiRiJiHuaAdapter(this));
	}

}
