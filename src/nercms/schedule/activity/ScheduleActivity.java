package nercms.schedule.activity;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.wxapp.service.elec.dao.Org;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;

public class ScheduleActivity extends BaseActivity implements OnClickListener {

	Button bt1, bt2, bt3;

	// ѡ����Ҫ���ȵ���
	List<Org> selectedPeople;
	// ��ƵԴ
	String videoId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);

		iniActionBar(true, null, "ָ�ӵ���");

		bt1 = (Button) findViewById(R.id.button1);
		bt1.setOnClickListener(this);
		bt2 = (Button) findViewById(R.id.button2);
		bt2.setOnClickListener(this);
		bt3 = (Button) findViewById(R.id.button3);
		bt3.setOnClickListener(this);

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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			// TODO ����
			break;
		case R.id.button2:
			// ����
			Intent intent = new Intent(ScheduleActivity.this, SchedulePersonActivity.class);
			ScheduleActivity.this.startActivityForResult(intent, 999);

			break;
		case R.id.button3:
			// TODO �˳�

			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case 999:
				selectedPeople = (List<Org>) data.getSerializableExtra("people");
				for (Org org : selectedPeople) {
					showLog_e(org.getId().substring(1));
				}
				videoId = data.getStringExtra("videoId");
				showLog_e(videoId);
				break;

			}
		}
	}
}
