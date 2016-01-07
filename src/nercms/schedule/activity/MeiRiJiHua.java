package nercms.schedule.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.wxapp.service.elec.dao.PlanTaskDao;
import nercms.schedule.R;
import nercms.schedule.adapter.MeiRiJiHuaAdapter;
import nercms.schedule.utils.Utils;

public class MeiRiJiHua extends Activity {
	int enterType;
	String tid;
	ListView mListView;
	Button bt_kaishirenwu;
	PlanTaskDao dao;

	TextView tv_time;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mei_ri_ji_hua);
		enterType = getIntent().getIntExtra("enterType", 1);
		tid = getIntent().getStringExtra("tid");

		dao = new PlanTaskDao(this);

		mListView = (ListView) findViewById(R.id.listView1);
		tv_time = (TextView) findViewById(R.id.time);
		tv_time.setText(Utils.formatDateMs(System.currentTimeMillis()));
		mListView.setAdapter(new MeiRiJiHuaAdapter(this, dao.getAllPlanTask(enterType, 3), dao));

		bt_kaishirenwu = (Button) findViewById(R.id.kaishirenwu_bt);
		bt_kaishirenwu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MeiRiJiHua.this, XianChangSi.class);
				intent.putExtra("enterType", enterType);
				intent.putExtra("tid", tid);
				MeiRiJiHua.this.startActivity(intent);
			}
		});
	}

}
