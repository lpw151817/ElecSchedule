package nercms.schedule.activity;

import com.Generate_md5;
import com.actionbarsherlock.view.MenuItem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.model.DeleteTaskResponse;
import android.wxapp.service.elec.model.LoginResponse;
import android.wxapp.service.elec.model.NormalServerResponse;
import android.wxapp.service.elec.model.StartTaskResponse;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.util.MySharedPreference;
import nercms.schedule.R;
import nercms.schedule.adapter.MeiRiJiHuaAdapter;
import nercms.schedule.utils.MyLog;
import nercms.schedule.utils.Utils;

public class MeiRiJiHua extends BaseActivity
		implements nercms.schedule.adapter.MeiRiJiHuaAdapter.OnItemDeleteButtonClick {
	int enterType;
	String tid;
	ListView mListView;
	MeiRiJiHuaAdapter adapter;
	Button bt_kaishirenwu;
	PlanTaskDao dao;

	TextView tv_time;

	WebRequestManager webManager;
	Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mei_ri_ji_hua);

		iniActionBar(true, null, null);

		webManager = new WebRequestManager(AppApplication.getInstance(), this);
		iniHandler();

		enterType = getIntent().getIntExtra("enterType", 1);
		tid = getIntent().getStringExtra("tid");

		dao = new PlanTaskDao(this);

		mListView = (ListView) findViewById(R.id.listView1);
		tv_time = (TextView) findViewById(R.id.time);
		tv_time.setText(Utils.formatDateMs(System.currentTimeMillis()));
		adapter = new MeiRiJiHuaAdapter(this, dao.getAllPlanTask(enterType, 3), dao, this);
		mListView.setAdapter(adapter);

		bt_kaishirenwu = (Button) findViewById(R.id.kaishirenwu_bt);
		bt_kaishirenwu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				webManager.startTask(MeiRiJiHua.this, tid, System.currentTimeMillis() + "");
			}
		});
	}

	private void iniHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Constants.START_TASK_SUCCESS:
					// 界面跳转
					showLongToast("任务开始成功");
					Intent intent = new Intent(MeiRiJiHua.this, XianChangSi.class);
					intent.putExtra("enterType", enterType);
					intent.putExtra("tid", tid);
					MeiRiJiHua.this.startActivity(intent);
					break;

				case Constants.START_TASK_FAIL:
				case Constants.START_TASK_SAVE_FAIL:
				case Constants.DELETE_TASK_FAIL:
				case Constants.DELETE_TASK_SAVE_FAIL:
					if (msg.obj != null) {
						showAlterDialog("失败", ((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "确定", null);
					} else {
						showAlterDialog("失败", "请检查是否与服务器连接正常", R.drawable.login_error_icon, "确定",
								null);
					}
					break;

				case Constants.DELETE_TASK_SUCCESS:
					Toast.makeText(MeiRiJiHua.this, "删除成功", Toast.LENGTH_LONG).show();
					adapter.getData().remove(position);
					adapter.notifyDataSetChanged();
					break;

				}
			}

		};

		MessageHandlerManager.getInstance().register(handler, Constants.START_TASK_SUCCESS,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.START_TASK_FAIL,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.START_TASK_SAVE_FAIL,
				StartTaskResponse.class.getName());

		MessageHandlerManager.getInstance().register(handler, Constants.DELETE_TASK_SUCCESS,
				DeleteTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.DELETE_TASK_FAIL,
				DeleteTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.DELETE_TASK_SAVE_FAIL,
				DeleteTaskResponse.class.getName());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MessageHandlerManager.getInstance().unregister(Constants.START_TASK_SUCCESS,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.START_TASK_FAIL,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.START_TASK_SAVE_FAIL,
				StartTaskResponse.class.getName());

		MessageHandlerManager.getInstance().unregister(Constants.DELETE_TASK_SUCCESS,
				DeleteTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.DELETE_TASK_FAIL,
				DeleteTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.DELETE_TASK_SAVE_FAIL,
				DeleteTaskResponse.class.getName());

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

	int position = -1;

	@Override
	public void onClick(int position) {
		webManager.deleteTask(MeiRiJiHua.this, adapter.getData().get(position).getId());
		this.position = position;
	}
}
