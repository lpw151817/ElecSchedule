package nercms.schedule.activity;

import java.util.Date;
import java.util.List;

import com.actionbarsherlock.view.MenuItem;
import com.android.volley.NetworkError;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.model.bean.table.tb_task_info;
import nercms.schedule.R;
import nercms.schedule.adapter.XianChangAddAdapter;
import nercms.schedule.adapter.XianchangAdapter;
import nercms.schedule.utils.Utils;

/**
 * 6.10 FINAL
 * 
 * @author JerryLiu
 * 
 */
public class TaskList extends BaseActivity {

	ListView listView;
	XianchangAdapter adapter;
	Button bt_rjhlr;
	// ͳ�ƽ������ 4 ��ʾ���� 5��ʾ����
	int enterType;
	// status 0,�µ�����1,ִ���У�2,����3,��ɣ�4,ȡ������ null�򲻲�ѯ���ֶ�
	String status;

	PlanTaskDao planTaskDao;
	List<tb_task_info> data;

	Button bt_qianyitian, bt_houyitian;
	TextView tv_jintian;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_list);

		bt_houyitian = (Button) findViewById(R.id.houyitian);
		bt_houyitian.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String tmp = tv_jintian.getText().toString() + " 00:00:00";

				Date today = Utils.parseDateInFormat1(tmp);
				Date tomorrow = new Date(today.getYear(), today.getMonth(), today.getDate() + 1);
				Date theDayAfterTomorrow = new Date(today.getYear(), today.getMonth(),
						today.getDate() + 2);
				if (isAdmin() == PERSON_TYPE.XIANCHANG)
					data = planTaskDao.getPlanTasks(enterType, 3, getUserId(), status,
							tomorrow.getTime() + "", theDayAfterTomorrow.getTime() + "");
				else
					data = planTaskDao.getPlanTasks(enterType, 3, null, status,
							tomorrow.getTime() + "", theDayAfterTomorrow.getTime() + "");
				tv_jintian.setText(Utils.formatDateMs(tomorrow.getTime()).split(" ")[0]);

				adapter.updateData(data);
			}
		});
		bt_qianyitian = (Button) findViewById(R.id.qianyitian);
		bt_qianyitian.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String tmp = tv_jintian.getText().toString() + " 00:00:00";

				Date today = Utils.parseDateInFormat1(tmp);
				Date yesterday = new Date(today.getYear(), today.getMonth(), today.getDate() - 1);
				if (isAdmin() == PERSON_TYPE.XIANCHANG)
					data = planTaskDao.getPlanTasks(enterType, 3, getUserId(), status,
							yesterday.getTime() + "", today.getTime() + "");
				else
					data = planTaskDao.getPlanTasks(enterType, 3, null, status,
							yesterday.getTime() + "", today.getTime() + "");
				tv_jintian.setText(Utils.formatDateMs(yesterday.getTime()).split(" ")[0]);

				adapter.updateData(data);
			}
		});

		tv_jintian = (TextView) findViewById(R.id.jintian);
		tv_jintian.setText(Utils.formatDateMs(System.currentTimeMillis()).split(" ")[0]);

		enterType = getIntent().getIntExtra("enterType", 1);
		status = getIntent().getStringExtra("statue");

		switch (enterType) {
		case 1:
			iniActionBar(true, null, "��ҵ�ֳ�");
			break;
		case 2:
			iniActionBar(true, null, "�����ֳ�");
			break;
		case 3:
			iniActionBar(true, null, "���Ͻ��������ֳ�");
			break;
		case 4:
			StringBuilder sb = new StringBuilder();
			// status 0,�µ�����1,ִ���У�2,����3,��ɣ�4,ȡ������ null�򲻲�ѯ���ֶ�
			switch (Integer.parseInt(status)) {
			case 0:
				sb.append("δִ�е�����");
				break;
			case 1:
				sb.append("����ִ�е�����");
				break;
			case 2:
				sb.append("������ɵ�����");
				break;
			case 3:
				sb.append("������ɵ�����");
				break;
			case 4:
				sb.append("ȡ��������");
				break;
			}
			sb.append(" - ����");
			iniActionBar(true, null, sb.toString());
			break;
		case 5:
			StringBuilder sb1 = new StringBuilder();
			// status 0,�µ�����1,ִ���У�2,����3,��ɣ�4,ȡ������ null�򲻲�ѯ���ֶ�
			switch (Integer.parseInt(status)) {
			case 0:
				sb1.append("δִ�е�����");
				break;
			case 1:
				sb1.append("����ִ�е�����");
				break;
			case 2:
				sb1.append("������ɵ�����");
				break;
			case 3:
				sb1.append("������ɵ�����");
				break;
			case 4:
				sb1.append("ȡ��������");
				break;
			}
			sb1.append(" - ����");
			iniActionBar(true, null, sb1.toString());
			break;
		}

		planTaskDao = new PlanTaskDao(this);

		bt_rjhlr = (Button) findViewById(R.id.rijihualuru);
		listView = (ListView) findViewById(R.id.task_list);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// ����ǰ��¼�û��ǹ���Ա������¼��ƻ�
		if (isAdmin() == PERSON_TYPE.XIANCHANG) {
			bt_rjhlr.setVisibility(View.GONE);
			if (enterType < 4) {
				// data = planTaskDao.getPlanTasks(enterType, 3, getUserId(),
				// status);

				// ��ʾ����ĵ�ַ
				String tmp = tv_jintian.getText().toString() + " 00:00:00";
				Date today = Utils.parseDateInFormat1(tmp);
				Date tomorrow = new Date(today.getYear(), today.getMonth(), today.getDate() + 1);

				data = planTaskDao.getPlanTasks(enterType, 3, getUserId(), status,
						today.getTime() + "", tomorrow.getTime() + "");

			} else {

				// ʱ��ѡ��������
				findViewById(R.id.top).setVisibility(View.GONE);

				Date tmp = new Date(System.currentTimeMillis());
				Date today = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate() + 1);
				Date yesterday = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate());
				Date theDayBeforeYesterday = new Date(tmp.getYear(), tmp.getMonth(),
						tmp.getDate() - 1);
				// 4 ��ʾ���� 5��ʾ����
				if (enterType == 4)
					data = planTaskDao.getPlanTasks(enterType, 3, getUserId(), status,
							yesterday.getTime() + "", today.getTime() + "");
				else if (enterType == 5)
					data = planTaskDao.getPlanTasks(enterType, 3, getUserId(), status,
							theDayBeforeYesterday.getTime() + "", yesterday.getTime() + "");
			}
		} else {
			if (enterType < 4) {
				// data = planTaskDao.getPlanTasks(enterType, 3, null, status);

				// ��ʾ����ĵ�ַ
				String tmp = tv_jintian.getText().toString() + " 00:00:00";
				Date today = Utils.parseDateInFormat1(tmp);
				Date tomorrow = new Date(today.getYear(), today.getMonth(), today.getDate() + 1);

				data = planTaskDao.getPlanTasks(enterType, 3, null, status, today.getTime() + "",
						tomorrow.getTime() + "");
			} else {
				// ʱ��ѡ��������
				findViewById(R.id.top).setVisibility(View.GONE);

				Date tmp = new Date(System.currentTimeMillis());
				Date today = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate() + 1);
				Date yesterday = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate());
				Date theDayBeforeYesterday = new Date(tmp.getYear(), tmp.getMonth(),
						tmp.getDate() - 1);
				// 4 ��ʾ���� 5��ʾ����
				if (enterType == 4)
					data = planTaskDao.getPlanTasks(enterType, 3, null, status,
							yesterday.getTime() + "", today.getTime() + "");
				else if (enterType == 5)
					data = planTaskDao.getPlanTasks(enterType, 3, null, status,
							theDayBeforeYesterday.getTime() + "", yesterday.getTime() + "");
			}

			bt_rjhlr.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(TaskList.this, PlanAdd.class);
					intent.putExtra("enterType", enterType);
					intent.putExtra("statue", status);
					TaskList.this.startActivity(intent);
				}
			});
		}

		adapter = new XianchangAdapter(this, enterType, data);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// ����ת
				Intent intent = new Intent(TaskList.this, TaskSelectorActivity.class);
				intent.putExtra("enterType", 0);
				intent.putExtra("tid", data.get(position).getId());
				TaskList.this.startActivity(intent);

				// // item����¼������ڲ�ѯ��������
				// Intent intent = new Intent(TaskList.this, PlanAdd.class);
				// intent.putExtra("enterType", 0);
				// intent.putExtra("tid", data.get(position).getId());
				// TaskList.this.startActivity(intent);
			}
		});
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}