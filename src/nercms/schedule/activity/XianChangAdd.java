package nercms.schedule.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import nercms.schedule.R;
import nercms.schedule.adapter.XianChangAddAdapter;

public class XianChangAdd extends BaseActivity {
	// TODO 测试数据
	int enterType = 1;

	ListView mListView;
	Button bt_jieshurenwu;
	TextView tv_time;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_add);

		switch (enterType) {
		case 1:
			getSupportActionBar().setTitle("作业现场列表");
			break;
		case 2:
			getSupportActionBar().setTitle("操作现场列表");
			break;
		case 3:
			getSupportActionBar().setTitle("故障紧急抢修现场列表");
			break;
		}

		bt_jieshurenwu = (Button) findViewById(R.id.jieshurenwu);
		bt_jieshurenwu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
		tv_time = (TextView) findViewById(R.id.time);

		mListView = (ListView) findViewById(R.id.listview);
		mListView.setAdapter(new XianChangAddAdapter(this, enterType));

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(XianChangAdd.this, XianChangUpload.class);
				XianChangAdd.this.startActivityForResult(intent, 100);
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

}
