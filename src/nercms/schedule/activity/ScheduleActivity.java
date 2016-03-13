package nercms.schedule.activity;

import java.util.List;

import com.nercms.schedule.misc.GID;
import com.nercms.schedule.ui.MediaInstance;
import com.nercms.schedule.ui.OnMsgCallback;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.wxapp.service.elec.dao.Org;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;

public class ScheduleActivity extends BaseActivity implements OnClickListener, OnMsgCallback {

	// private String server_ip = "120.26.78.7";//调度服务器IP
	// private String server_ip = "172.16.25.178";//调度服务器IP
	private String server_ip = "192.168.2.150";// 调度服务器IP
	// private String server_ip = "192.168.3.2";//调度服务器IP
	private int server_port = 5060;// 调度服务器通信端口
	private String self_id = "111";// 本机注册ID
	private String encrypt_info = "JEO!FGL#GGG)GG$G$HIG((^&%$FJEF";
	private String remote_id1 = "222";// "4294967295";//被叫终端ID，可有多个
	// private String remote_id2 = "333";
	private String remote_id2 = "4294967295";
	private String video_source = remote_id2;// 视频源ID

	private Button bt1, bt2, bt3;
	private SurfaceView surfaceView;
	// 选择需要调度的人
	private List<Org> selectedPeople;
	// 视频源
	private String videoId;

	private Handler handler = new Handler() {
		// 回调处理
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case GID.MSG_INCOMING_CALL:
				Toast.makeText(ScheduleActivity.this, "收到调度邀请 " + (String) (msg.obj),
						Toast.LENGTH_SHORT).show();
				break;

			case GID.MSG_HANG_UP:
				Toast.makeText(ScheduleActivity.this, "调度结束", Toast.LENGTH_SHORT).show();
				break;

			case GID.MSG_RECV_CANCEL:
				Toast.makeText(ScheduleActivity.this, "主叫方放弃调度", Toast.LENGTH_SHORT).show();
				break;

			default:
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);

		iniActionBar(true, null, "指挥调度");

		surfaceView = (SurfaceView) findViewById(R.id.videorenderview);
		MediaInstance.instance().api_start(getApplicationContext(), server_ip, server_port, self_id,
				encrypt_info);
		// MediaInstance.instance().api_start(this, server_ip,
		// server_port,self_id, encrypt_info);
		MediaInstance.instance().api_set_msg_callback(this);
		MediaInstance.instance().api_set_video_view(surfaceView);// layout_inflater.inflate(R.layout.videorender,null));

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
			// TODO 设置
			break;
		case R.id.button2:
			// 调度
			Intent intent = new Intent(ScheduleActivity.this, SchedulePersonActivity.class);
			ScheduleActivity.this.startActivityForResult(intent, 999);
			break;
		case R.id.button3:
			// 退出
			finish();
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

	@Override
	public void on_msg_callback(int arg0, String arg1) {
		handler.sendMessage(handler.obtainMessage(arg0, arg1));
	}

	@Override
	public void finish() {
		super.finish();
		MediaInstance.instance().api_shutdown();
	}
}
