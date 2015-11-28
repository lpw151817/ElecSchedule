package nercms.schedule.service;

import java.util.Timer;
import java.util.TimerTask;

import nercms.schedule.R;
import nercms.schedule.activity.Login;
import nercms.schedule.activity.Main;
import nercms.schedule.utils.MyLog;
import nercms.schedule.utils.Utils;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.wxapp.service.AppApplication;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.thread.SaveConferenceThread;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

public class UpdateService extends Service {
	private Timer mTimer;
	private TimerTask mTask;
	private Handler mHandler;
	private String TAG = "UpdateService";
	private WebRequestManager webRequestManager;
	// 按照一小时一次进行请求
	// private final long TASK_PERIOD = 1000 * 60 * 60;
	private final long TASK_PERIOD = 1000 * 10;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		Log.e(TAG, "OnCreate");
		super.onCreate();
		// 初始化各种变量
		ini();
		// 执行Timer
		this.mTimer.schedule(mTask, 0, TASK_PERIOD);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	private void ini() {
		if (this.webRequestManager == null)
			webRequestManager = new WebRequestManager(AppApplication.getInstance(),
					getApplicationContext());
		if (this.mTask == null) {
			this.mTask = new TimerTask() {
				@Override
				public void run() {
					Log.e(TAG, "start update!");
					// 获取组织相关信息并存入数据库
					webRequestManager.getOrgCodeUpdate();
					webRequestManager.getOrgPersonUpdate();
					// 获取事务更新
					webRequestManager.getAffairUpdate("1");
					// 获取消息更新
					webRequestManager.getMessageUpdate("1");
					// 更新会议
					webRequestManager.updateConference("1");
					// group
					webRequestManager.getGroupUpdateRequest("1");
					// gps
					webRequestManager.getGpsUpdateRequest("1");
				}
			};
		}
		if (this.mTimer == null) {
			this.mTimer = new Timer();
		}

		iniHandler();
	}

	private void iniHandler() {
		this.mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// 网络请求的回调
				switch (msg.what) {
				// 保存orgcode失败
				case Constant.SAVE_ORG_CODE_FAIL:
					MyLog.e(TAG, "保存orgcode失败");
					break;
				// 保存orgcode成功
				case Constant.SAVE_ORG_CODE_SUCCESS:
					MyLog.e(TAG, "保存orgcode成功");
					break;
				// 获取组织结点失败
				case Constant.QUERY_ORG_NODE_REQUEST_FAIL:
					MyLog.e(TAG, "获取结点失败");
					break;
				// 存储orgperson成功
				case Constant.SAVE_ORG_PERSON_SUCCESS:
					MyLog.e(TAG, "保存orgperson成功");
					break;
				// 存储orgperson失败
				case Constant.SAVE_ORG_PERSON_FAIL:
					MyLog.e(TAG, "保存orgperson失败");
					break;
				// 获取orgperson失败
				case Constant.QUERY_ORG_PERSON_REQUEST_FAIL:
					Log.e(TAG, "获取orgperson失败");
					break;
				// 保存联系人完成
				case Constant.SAVE_ALL_PERSON_SUCCESS:
					break;
				case Constant.UPDATE_TASK_LIST_REQUEST_FAIL:
					Log.e(TAG, "获取TaskList失败");
					break;
				case Constant.UPDATE_MESSAGE_REQUEST_FAIL:
					Log.e(TAG, "获取Message失败");
					break;
				default:
					break;
				}
			}

		};

		// handler的注册
		MessageHandlerManager.getInstance().register(mHandler, Constant.QUERY_ORG_NODE_REQUEST_FAIL,
				Contants.METHOD_PERSON_GET_ORG_CODE);
		MessageHandlerManager.getInstance().register(mHandler, Constant.QUERY_ORG_PERSON_REQUEST_FAIL,
				Contants.METHOD_PERSON_GET_ORG_PERSON);
		MessageHandlerManager.getInstance().register(mHandler, Constant.UPDATE_TASK_LIST_REQUEST_FAIL,
				Contants.METHOD_AFFAIRS_UPDATE_LIST);
		MessageHandlerManager.getInstance().register(mHandler, Constant.UPDATE_MESSAGE_REQUEST_FAIL,
				Contants.METHOD_MESSAGE_UPDATE);
		MessageHandlerManager.getInstance().register(mHandler, Constant.CONFERENCE_SAVE_SUCCESS,
				SaveConferenceThread.TAG);
		MessageHandlerManager.getInstance().register(mHandler, Constant.CONFERENCE_SAVE_FAIL,
				SaveConferenceThread.TAG);
		MessageHandlerManager.getInstance().register(mHandler, Constant.GROUP_SAVE_SECCESS,
				Contants.METHOD_GROUP_UPDATE);
		MessageHandlerManager.getInstance().register(mHandler, Constant.GROUP_SAVE_FAIL,
				Contants.METHOD_GROUP_UPDATE);
		MessageHandlerManager.getInstance().register(mHandler, Constant.SAVE_GPS_SECCESS,
				Contants.METHOD_GPS_UPDAET);
		MessageHandlerManager.getInstance().register(mHandler, Constant.SAVE_GPS_FAIL,
				Contants.METHOD_GPS_UPDAET);
		MessageHandlerManager.getInstance().register(mHandler, Constant.QUERY_GPSS_REQUEST_FAIL,
				Contants.METHOD_GPS_UPDAET);
	}

	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		super.onDestroy();

		this.mTask.cancel();
		this.mTask = null;
		this.mTimer.cancel();
		this.mTimer = null;

		// handler的销毁
		MessageHandlerManager.getInstance().unregister(Constant.QUERY_ORG_NODE_REQUEST_FAIL,
				Contants.METHOD_PERSON_GET_ORG_CODE);
		MessageHandlerManager.getInstance().unregister(Constant.QUERY_ORG_PERSON_REQUEST_FAIL,
				Contants.METHOD_PERSON_GET_ORG_PERSON);
		MessageHandlerManager.getInstance().unregister(Constant.UPDATE_TASK_LIST_REQUEST_FAIL,
				Contants.METHOD_AFFAIRS_UPDATE_LIST);
		MessageHandlerManager.getInstance().unregister(Constant.UPDATE_MESSAGE_REQUEST_FAIL,
				Contants.METHOD_MESSAGE_UPDATE);
		MessageHandlerManager.getInstance().unregister(Constant.CONFERENCE_SAVE_SUCCESS,
				SaveConferenceThread.TAG);
		MessageHandlerManager.getInstance().unregister(Constant.CONFERENCE_SAVE_FAIL,
				SaveConferenceThread.TAG);
		MessageHandlerManager.getInstance().unregister(Constant.GROUP_SAVE_SECCESS,
				Contants.METHOD_GROUP_UPDATE);
		MessageHandlerManager.getInstance().unregister(Constant.GROUP_SAVE_FAIL,
				Contants.METHOD_GROUP_UPDATE);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_GPS_SECCESS,
				Contants.METHOD_GPS_UPDAET);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_GPS_FAIL,
				Contants.METHOD_GPS_UPDAET);
		MessageHandlerManager.getInstance().unregister(Constant.QUERY_GPSS_REQUEST_FAIL,
				Contants.METHOD_GPS_UPDAET);
	}
}
