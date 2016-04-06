package nercms.schedule.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ListView;
import android.wxapp.service.elec.dao.Org;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.model.bean.table.TB_SYS_Person;
import nercms.schedule.R;
import nercms.schedule.adapter.SchedulePersonAdapter;

public class SchedulePersonActivity extends BaseActivity {

	ListView listView;

	OrgDao dao;
	List<Org> data = new ArrayList<Org>();
	SchedulePersonAdapter adapter;
	// �����������ֵ
	private final int MAX_USER = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule_person);

		iniActionBar(true, null, "������Ա����");
		listView = (ListView) findViewById(R.id.listview);

		dao = new OrgDao(this);
		data.add(new Org("p" + getUserId(), "", dao.getPerson(getUserId()).getName()));
		if (isAdmin())
			data.addAll(dao.convert(dao.getPersons("0")));
		else
			data.addAll(dao.convert(dao.getPersons("1")));

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
			String videoSource = adapter.getSelectedVideo();
			if (TextUtils.isEmpty(videoSource)) {
				showShortToast("��ѡ����ƵԴ");
			} else if (adapter.getSelectedPeople().size() < 2) {
				showShortToast("��ѡ�����û�");
			} else if (adapter.getSelectedPeople().size() > MAX_USER) {
				showShortToast("���ֻ��ѡ��" + MAX_USER + "���û�");
			} else {
				if (adapter.getSelectedPeople()
						.contains(new Org(adapter.getSelectedVideo(), "", ""))) {
//					Intent intent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putSerializable("people", (Serializable) adapter.getSelectedPeople());
					bundle.putString("videoId", videoSource.substring(1));
					bundle.putInt("tag", 999);
//					intent.putExtras(bundle);
//					setResult(RESULT_OK, intent);
					ScheduleActivity.wakeUp(getApplicationContext(), bundle);
					finish();
				} else {
					showShortToast("��ѡ���ѹ�ѡ������Ϊ��ƵԴ");
				}
			}
			break;
		}

		return super.onOptionsItemSelected(item);
	}
}
