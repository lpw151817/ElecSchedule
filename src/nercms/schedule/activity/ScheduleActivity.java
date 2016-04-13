package nercms.schedule.activity;

import java.util.ArrayList;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.utils.AttachmentUpload;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.wxapp.service.elec.dao.Org;
import android.wxapp.service.elec.model.UploadTaskAttachmentResponse;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.Contants;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.util.Constant;

import com.nercms.schedule.misc.GD;
import com.nercms.schedule.misc.GID;
import com.nercms.schedule.ui.MediaInstance;
import com.nercms.schedule.ui.OnMsgCallback;

public class ScheduleActivity extends BaseActivity implements OnClickListener, OnMsgCallback {

//	private boolean isScheduleOpen = false;

	protected static final int UPLOAD = 0;

	private int pingMax = 5000;

	public static String server_ip_wan = Contants.SCHEDULE_SERVER_WAN;// 调度服务器IP
	public static String server_ip_lan = Contants.SCHEDULE_SERVER_LAN;

	// private String server_ip = "172.16.25.178";//调度服务器IP
	// private String server_ip = "192.168.2.150";// 调度服务器IP
	// private String server_ip = "192.168.3.2";//调度服务器IP
	private int server_port = Contants.SCHEDULE_PORT;// 调度服务器通信端口
	private String self_id;// 本机注册ID
	private String encrypt_info = "JEO!FGL#GGG)GG$G$HIG((^&%$FJEF";
	// private String remote_id1 = "222";// "4294967295";//被叫终端ID，可有多个
	// // private String remote_id2 = "333";
	// private String remote_id2 = "4294967295";
	// private String video_source = remote_id2;// 视频源ID

	private Button bt1, bt2, bt3, bt4;

	SurfaceView video_render_view;
	SurfaceView video_capture_view;

	// 选择需要调度的人
	private List<Org> selectedPeople;
	// 视频源
	private String videoId;

	protected long delayedTime = 10000;// 延时10s
	// private ScheduledExecutorService scheduler = Executors
	// .newScheduledThreadPool(1);
	// WebRequestManager requestManager;
	private Handler handler = new Handler() {

		// 回调处理
		@Override
		public void handleMessage(Message msg) {
			Log.e("Demo", "handleMessage:" + msg.what);

			super.handleMessage(msg);
			switch (msg.what) {
			// 被叫方收到调度请求回调
			case GID.MSG_INCOMING_CALL:
				wakeUp(getApplicationContext(), null);
				// if (surfaceView.getVisibility() == View.GONE)
				// surfaceView.setVisibility(View.VISIBLE);
				Toast.makeText(ScheduleActivity.this, "收到调度邀请 " + (String) (msg.obj),
						Toast.LENGTH_SHORT).show();
				changeVisibility(View.GONE, bt1, bt2, bt4);
				changeVisibility(View.VISIBLE, bt3);
				break;
			// 主叫方挂断，被叫方回调
			case GID.MSG_HANG_UP:
				wakeUp(getApplicationContext(), null);
				changeVisibility(View.VISIBLE, bt1, bt2);
				changeVisibility(View.GONE, bt3, bt4);
				Toast.makeText(ScheduleActivity.this, "调度结束", Toast.LENGTH_SHORT).show();
				onBackPressed();
				break;

			case GID.MSG_RECV_CANCEL:
				wakeUp(getApplicationContext(), null);
				Toast.makeText(ScheduleActivity.this, "主叫方放弃调度", Toast.LENGTH_SHORT).show();
				onBackPressed();
				break;
			case GID.MSG_PING_DELAY:
//				// Toast.makeText(ScheduleActivity.this, "ping server delay: " +
//				// (Integer) (msg.obj),
//				// Toast.LENGTH_SHORT).show();
//				Log.e("Demo", "Ping Msg:" + (Integer) (msg.obj));
//				int ping = (Integer) (msg.obj);
//				if (ping == 0 || ping > pingMax) {
//					// TODO 网络中断的时候停止上传
//					// scheduler.shutdownNow();
//					AttachmentUpload.stop(ScheduleActivity.this);
//					isScheduleOpen = false;
//					Log.d("TAG", "网络中断的时候停止上传");
//				} else {
//					// TODO 网络连接的时候开始上传
//					// 线程不要重复创建
//
//					if (!isScheduleOpen) {
//						Log.d("JAMES", "打开线程");
//
//						AttachmentUpload.start(ScheduleActivity.this, handler);
//						// AttachmentUpload.start(ScheduleActivity.this);
//
//						isScheduleOpen = true;
//					}
//					Log.d("Demo", "网络连接的时候开始上传");
//				}
				break;

			case Constant.FILE_UPLOAD_SUCCESS:

				/*
				 * // Log.e("TAG", "HHH"); tb_task_attachment planTaskAtts2 =
				 * (tb_task_attachment) msg.obj; //
				 * Toast.makeText(ScheduleActivity.this, "上传成功", //
				 * Toast.LENGTH_SHORT).show();
				 * pDao.changeTaskAttachmentStatus(planTaskAtts2.getTask_id(),
				 * "1");
				 * 
				 * 
				 * GpsDao gpsDao = new GpsDao(ScheduleActivity.this);
				 * PlanTaskDao pDao = new PlanTaskDao(ScheduleActivity.this);
				 * 
				 * 
				 * 
				 * 
				 * 
				 * List<Attachments> sublist = new ArrayList<Attachments>();
				 * List<TaskAttachment> attachment = new
				 * ArrayList<TaskAttachment>(); Attachments att = new
				 * Attachments(planTaskAtts2.getType(),
				 * android.wxapp.service.elec.request.Contants.HFS_URL +
				 * File.separator + planTaskAtts2.getUrl(),
				 * planTaskAtts2.getUpload_time(),
				 * gpsDao.getHistory(planTaskAtts2.getHistorygps()),
				 * planTaskAtts2.getMd5()); sublist.add(att); TaskAttachment
				 * item = new TaskAttachment( planTaskAtts2.getStandard(),
				 * sublist); attachment.add(item);
				 * 
				 * tb_task_info planTask = pDao.getPlanTask(planTaskAtts2
				 * .getTask_id());
				 * requestManager.uploadTaskAttachment(ScheduleActivity.this,
				 * planTaskAtts2.getTask_id(), planTask.getCategory() + "",
				 * attachment);
				 * 
				 */
				break;
			case Constant.FILE_UPLOAD_FAIL:
				Toast.makeText(ScheduleActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
				break;

			case Constants.UPLOAD_TASK_ATT_SUCCESS:
				Toast.makeText(ScheduleActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
				break;

			}
		}
	};

	/*
	 * private PlanTaskDao pDao = new PlanTaskDao(ScheduleActivity.this);
	 * 
	 * Runnable command = new Runnable() {
	 * 
	 * @Override public void run() { if (pDao == null) pDao = new
	 * PlanTaskDao(ScheduleActivity.this); List<tb_task_attachment> planTaskAtts
	 * = pDao.getPlanTaskAtts(null, null, "0"); Log.d("TAG",
	 * planTaskAtts.toString());
	 * 
	 * if (planTaskAtts.size() != 0) { registHandler(); for (tb_task_attachment
	 * uploadAtt : planTaskAtts) { String prefix = NewTask.fileFolder; String
	 * fileName = uploadAtt.getUrl(); String uploadUrl =
	 * android.wxapp.service.elec.request.Contants.HFS_URL; new
	 * HttpUploadTask(new TextView(ScheduleActivity.this),
	 * ScheduleActivity.this, uploadAtt).execute(prefix + fileName, uploadUrl);
	 * }
	 * 
	 * 
	 * }
	 * 
	 * } };
	 * 
	 */

	public static void wakeUp(Context c, Bundle b) {
		// 将页面调至前台
		Intent intent = new Intent(c, ScheduleActivity.class);
		if (b != null)
			intent.putExtras(b);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		c.startActivity(intent);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		Log.i("Demo", "MediaDemo::onCreate()");
		iniActionBar(true, null, "视频调度");

		self_id = getUserId();

		// requestManager = new WebRequestManager(AppApplication.getInstance(),
		// this);

		showLog_e(server_ip_wan + ":" + server_port);

		video_render_view = (SurfaceView) findViewById(R.id.videorenderview);
		video_capture_view = (SurfaceView) findViewById(R.id.videocaptureview);

		// surfaceView.setVisibility(View.GONE);

		// 如果内外网ip不等，则设置参数为true
		if (!server_ip_lan.equals(server_ip_wan))
			MediaInstance.instance().api_start(getApplicationContext(), server_ip_wan,
					server_ip_lan, true, server_port, self_id, encrypt_info);
		else
			MediaInstance.instance().api_start(getApplicationContext(), server_ip_wan,
					server_ip_lan, false, server_port, self_id, encrypt_info);
		Log.e("Push", "MediaInstance.instance().api_start");
		MediaInstance.instance().api_set_video_render_scale(2.8f);
		MediaInstance.instance().api_set_msg_callback(this);
		MediaInstance.instance().api_set_video_view(video_render_view, video_capture_view);
		// int ping_delay = MediaInstance.instance()
		// .api_get_ping_delay(ScheduleActivity.server_ip_wan);

		bt1 = (Button) findViewById(R.id.button1);
		bt1.setOnClickListener(this);
		bt2 = (Button) findViewById(R.id.button2);
		bt2.setOnClickListener(this);
		bt3 = (Button) findViewById(R.id.button3);
		bt3.setOnClickListener(this);
		bt3.setVisibility(View.GONE);
		bt4 = (Button) findViewById(R.id.button4);
		bt4.setOnClickListener(this);
		bt4.setVisibility(View.GONE);

		Intent intent = new Intent(getApplicationContext(), MainContent.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		getApplicationContext().startActivity(intent);

		moveTaskToBack(true);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button1:
			// 调度
			Intent intent = new Intent(ScheduleActivity.this, SchedulePersonActivity.class);
			// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			// ScheduleActivity.this.startActivityForResult(intent, 999);
			ScheduleActivity.this.startActivity(intent);
			break;
		case R.id.button2:
			// 呼叫
			if (!TextUtils.isEmpty(videoId) && selectedPeople != null
					&& selectedPeople.size() > 1) {
				// if (surfaceView.getVisibility() == View.GONE)
				// surfaceView.setVisibility(View.VISIBLE);
				changeVisibility(View.GONE, bt1, bt2);
				changeVisibility(View.VISIBLE, bt4);
				ArrayList<String> ids = new ArrayList<String>();
				for (Org org : selectedPeople) {
					ids.add(org.getId().substring(1));
					showLog_e(org.toString());
				}
				showLog_v(videoId);
				MediaInstance.instance().api_start_schedule(ids, videoId);
			} else {
				showLongToast("请设置调度人员");
			}
			break;
		case R.id.button3:
			// 接听
			// if (surfaceView.getVisibility() == View.GONE)
			// surfaceView.setVisibility(View.VISIBLE);
			changeVisibility(View.VISIBLE, bt4);
			changeVisibility(View.GONE, bt1, bt2, bt3);
			MediaInstance.instance().api_accept_schedule_invite();
			break;
		case R.id.button4:
			// 挂断
			// if (surfaceView.getVisibility() == View.GONE)
			// surfaceView.setVisibility(View.VISIBLE);
			changeVisibility(View.GONE, bt4);
			changeVisibility(View.VISIBLE, bt1, bt2);
			MediaInstance.instance().api_shutdown_schedule();
			onBackPressed();
			break;
		}
		refresh_view();
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
	public void on_msg_callback(int arg0, Object arg1) {
		handler.sendMessage(handler.obtainMessage(arg0, arg1));
	}

	@Override
	public void finish() {
		// super.finish();
		// MediaInstance.instance().api_shutdown_schedule();
		// MediaInstance.instance().api_shutdown();
		moveTaskToBack(true); // 设置该activity永不过期，即不执行onDestroy()
	}

	private void changeVisibility(int visable, View... vs) {
		for (View view : vs) {
			view.setVisibility(visable);
		}
	}

	@Override
	protected void onRestart() {
		Log.e("Demo", "MainContent:OnRestart");
		super.onRestart();
	}

	@Override
	protected void onStart() {
		Log.e("Demo", "MainContent:OnStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.i("Demo", "MediaDemo::onResume()");
		super.onResume();

		refresh_view();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.i("Demo", "MediaDemo::onNewIntent()");
		super.onNewIntent(intent);
		if (intent.getIntExtra("tag", -1) == 999) {
			selectedPeople = (List<Org>) intent.getSerializableExtra("people");
			for (Org org : selectedPeople) {
				showLog_e(org.getId().substring(1));
			}
			videoId = intent.getStringExtra("videoId");
			showLog_e(videoId);
		} else if (intent.getIntExtra("tag", -1) == 0) {
			Intent intent1 = new Intent(getApplicationContext(), MainContent.class);
			intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			getApplicationContext().startActivity(intent1);

			moveTaskToBack(true);
		}
	}

	private void refresh_view() {
		video_render_view = (SurfaceView) findViewById(R.id.videorenderview);
		video_capture_view = (SurfaceView) findViewById(R.id.videocaptureview);

		MediaInstance.instance().api_set_msg_callback(this);
		MediaInstance.instance().api_set_video_view(video_render_view, video_capture_view);// layout_inflater.inflate(R.layout.videorender,
																							// null));

		if (true == GD.is_in_schedule()) {
			if (false == GD._i_am_video_source) {
				Log.v("Demo", "not video source");
				video_capture_view.getHolder().setFormat(PixelFormat.TRANSPARENT);
				video_capture_view.setZOrderOnTop(false);
				video_capture_view.setZOrderMediaOverlay(false);
				video_render_view.setZOrderOnTop(true);
				video_render_view.setZOrderMediaOverlay(true);
			} else {
				Log.v("Demo", "video source");
				video_render_view.getHolder().setFormat(PixelFormat.TRANSPARENT);
				video_render_view.setZOrderOnTop(false);
				video_render_view.setZOrderMediaOverlay(false);
				video_capture_view.setZOrderOnTop(true);
				video_capture_view.setZOrderMediaOverlay(true);
			}
		} else {
			video_render_view.setZOrderOnTop(false);
			video_render_view.setZOrderMediaOverlay(false);
			video_capture_view.setZOrderOnTop(true);
			video_capture_view.setZOrderMediaOverlay(true);
		}
	}

	@Override
	public void onBackPressed() {
		Log.i("Demo", "MediaDemo::onBackPressed()");
		if (true == GD.is_in_schedule()) {
			Toast.makeText(ScheduleActivity.this, "请关闭调度后再返回", Toast.LENGTH_SHORT).show();
			return;
		}
		super.onBackPressed();

	}

	private void registHandler() {
		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_SUCCESS,
				getClass().getSimpleName());
		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_FAIL,
				getClass().getSimpleName());
		MessageHandlerManager.getInstance().register(handler, Constants.UPLOAD_TASK_ATT_SUCCESS,
				UploadTaskAttachmentResponse.class.getName());

	}
}
