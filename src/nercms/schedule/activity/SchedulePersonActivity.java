package nercms.schedule.activity;

import java.io.Serializable;
import java.util.List;

import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.wxapp.service.elec.dao.Org;
import android.wxapp.service.elec.dao.OrgDao;
import nercms.schedule.R;
import nercms.schedule.adapter.SchedulePersonAdapter;

public class SchedulePersonActivity extends BaseActivity {

	ListView listView;

	OrgDao dao;
	List<Org> data;
	SchedulePersonAdapter adapter;

	private final int MAX_USER = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule_person);

		iniActionBar(true, null, "������Ա����");
		listView = (ListView) findViewById(R.id.listview);

		dao = new OrgDao(this);
		data = dao.getAllPersons();
		adapter = new SchedulePersonAdapter(this, data);
		listView.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		// ȷ����ť
		MenuItem select_ok = menu.add(0, 1, 0, "ȷ��");
		select_ok.setTitle("ȷ��");
		select_ok.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			setResult(RESULT_CANCELED);
			break;
		case 1:
			int position = adapter.getSelectedVideo();
			if (position == -1) {
				showLongToast("��ѡ����ƵԴ");
			} else if (adapter.getSelectedPeople().size() < 2) {
				showLongToast("��ѡ�����û�");
			} else if (adapter.getSelectedPeople().size() > MAX_USER) {
				showLongToast("���ֻ��ѡ��" + MAX_USER + "���û�");
			} else {
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putSerializable("people", (Serializable) adapter.getSelectedPeople());
				String videoId = ((Org) adapter.getItem(position)).getId().substring(1);
				bundle.putString("videoId", videoId);
				intent.putExtras(bundle);
				setResult(RESULT_OK, intent);
				finish();
			}
			break;
		}

		return super.onOptionsItemSelected(item);
	}
}
