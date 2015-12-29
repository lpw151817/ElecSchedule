package nercms.schedule.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import nercms.schedule.R;
import nercms.schedule.adapter.MeiRiJiHuaAdapter;

public class MeiRiJiHua extends Activity {
	int enterType;
	ListView mListView;
	Button bt_kaishirenwu;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mei_ri_ji_hua);

		enterType = getIntent().getIntExtra("enterType", 1);

		mListView = (ListView) findViewById(R.id.listView1);
		mListView.setAdapter(new MeiRiJiHuaAdapter(this));

		bt_kaishirenwu = (Button) findViewById(R.id.kaishirenwu_bt);
		bt_kaishirenwu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MeiRiJiHua.this, XianChangSi.class);
				intent.putExtra("enterType", enterType);
				MeiRiJiHua.this.startActivity(intent);
			}
		});
	}

}
