package com.nercms;

import com.google.gson.Gson;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.model.UpdateResponse;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.Contants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.mqtt.MqttResponse;
import android.wxapp.service.util.MySharedPreference;
import nercms.schedule.R;
import nercms.schedule.activity.NewTask;
import nercms.schedule.activity.PlanAdd;

/*
 * 使用方法：
 * 1、创建对象：client = new Push();
 * 2、初始化：client.init("testClient", "test.mosquitto.org", 1883);
 * 3、设置回调函数：Callbacks mCallbacks = new Callbacks(); cleint.setCallbacks(mCallbacks);
 * 4、连接服务器：cleint.login();
 * 5、订阅/发布消息：cleint.addTag("tag1", 1);  cleint.pushMsgToTag("tag1", String.valueOf(pubCount), 1);
 * 		
 * */

public class Push {

	// ///topic：personid
	// ///clientid:m_personid
	// ///message:{type:int,id:int}

	private final String TAG = getClass().getName();

	public static String SERVER_URL = Contants.MQTT_SERVER;
	private static final int PORT = Integer.parseInt(Contants.MQTT_PORT);
	private final String TOPIC_HEADER = "nercms/schedule/";
	public static String PERSON_ID;// personid
	private final int QOS = 1;
	// Qos 0: 至多一次,消息发布完全依赖底层网络,会发生消息丢失或重复;
	// Qos 1: 至少一次,确保消息到达,但消息重复可能会发生;
	// Qos 2: 只有一次, 确保消息到且只到达一次.

	private volatile static Push _unique_instance = null;

	Context c;

	public static Push get_instance(Context c) {
		if (null == _unique_instance) {
			synchronized (Push.class) {
				if (null == _unique_instance) {
					_unique_instance = new Push(c);
				}
			}
		}
		return _unique_instance;
	}

	MqttResponse response;

	public void ini() {
		Log.e("mqtt ini", SERVER_URL + "/" + PORT);
		// ini handler
		iniHandler();
		// ini uid
		PERSON_ID = getUserId();
		// clientid为m_开头
		init("m_" + PERSON_ID, SERVER_URL, PORT);
		setCallbacks(new ICallBacks() {

			@Override
			public int onMessageArrivedFunc(String client_id, String tag, int msg_len, String msg,
					int msg_qos, int duplicate_count) {
				Log.v("onMessageArrivedFunc...", msg);
				// 进行到达消息的处理
				try {
					WebRequestManager manager = new WebRequestManager(AppApplication.getInstance(),
							c);
					Gson gson = new Gson();
					response = gson.fromJson(msg, MqttResponse.class);

					if (response != null) {
						// 进行数据更新
						manager.loginUpdate(c);
					}
				} catch (Exception e) {
					Log.e(TAG, "response.getType() 解析错误");
					e.printStackTrace();
				}
				return 0;
			}

			@Override
			public void onDeliveryCompleteFunc(String client_id, int tocken) {
				Log.v(TAG, "onDeliveryCompleteFunc ");
			}

			@Override
			public void onConnectionLostFunc(String client_id, String cause) {
				Log.v(TAG, "onConnectionLostFunc \t" + cause);
			}
		});
		login();
		// sub
		addTag(TOPIC_HEADER + "m_" + PERSON_ID, QOS);
	}

	Handler handler;

	private void iniHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				Log.e("handleMessage>>>>>>>>>>>", msg.toString() + "....." + msg.what);
				Class target = null;
				Bundle b = null;
				b = new Bundle();
				String content = "您有新的";

				switch (msg.what) {
				case Constants.LOGIN_UPDATE_SUCCESS:
					if (response != null) {
						if (response.getType().equals("1")) {
							// 开始和结束任务推送给领导
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							target = PlanAdd.class;
							content += "任务";
						} else if (response.getType().equals("2")) {
							// 新建指令时进行推送给任务负责人
							target = NewTask.class;
							b.putString("taskInsId", response.getId());
							content += "指令";
						} else if (response.getType().equals("3")) {
							// 上传附件后，推送给应上岗到位领导，如果没有就不推送
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							target = PlanAdd.class;
							content += "任务附件";
						}
					}
					break;
				case Constants.LOGIN_UPDATE_FAIL:
				case Constants.LOGIN_UPDATE_SAVE_FAIL:
					break;
				}

				// if (target != null)
				showNotification(Push.this.c, target, b, content, "调度系统", content);
			}
		};

		MessageHandlerManager.getInstance().register(handler, Constants.LOGIN_UPDATE_SUCCESS,
				UpdateResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.LOGIN_UPDATE_SAVE_FAIL,
				UpdateResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.LOGIN_UPDATE_FAIL,
				UpdateResponse.class.getName());
	}

	protected String getUserId() {
		return MySharedPreference.get(c, MySharedPreference.USER_ID, null);
	}

	private Push(Context c) {
		this.c = c;
	}

	public void release() {
		Log.e("mqtt release", "release()");
		Log.e("mqtt connect state", state() + "");
		if (state() == 2) {
			// 已经连上服务器了
			logout();
			_unique_instance = null;
		}
	}

	static {
		try {
			System.loadLibrary("push"); // call .so
		} catch (UnsatisfiedLinkError e) {
			System.out.println("load lib push failed.");
		}
	}

	/**
	 * 初始化客户端 client_id：客户端的ID server_ip：服务端IP server_port:服务端端口号
	 * 
	 * @return 成功返回0，失败返回-1
	 */
	public native int init(String client_id, String server_ip, int server_port);

	/*
	 * 设置回调函数 callbacks：回调函数
	 */
	public native void setCallbacks(ICallBacks callbacks);

	/**
	 * 连接登录到服务器
	 * 
	 * @return 成功返回0，失败返回-1
	 * 
	 */
	public native int login();

	/**
	 * 订阅消息 tag:消息主题 qos:消息的qos
	 * 
	 * @param qos
	 *            Qos 0: 至多一次,消息发布完全依赖底层网络,会发生消息丢失或重复; Qos 1:
	 *            至少一次,确保消息到达,但消息重复可能会发生; Qos 2: 只有一次, 确保消息到且只到达一次.
	 * @return 成功返回0，失败返回-1
	 */
	public native int addTag(String tag, int qos);

	/**
	 * 取消订阅消息 tag:消息主题
	 * 
	 * @return 成功返回0，失败返回-1
	 */
	public native int removeTag(String tag);

	/**
	 * 客户端当前的连接状态
	 * 
	 * @return 返回2，表示已经连接到服务器 返回1，表示已创建客户端对象，还未连接到服务器
	 */
	public native int state();

	/**
	 * 发布消息 tag:消息主题 msg：消息内容 qos:消息的qos
	 * 
	 * @param qos
	 *            Qos 0: 至多一次,消息发布完全依赖底层网络,会发生消息丢失或重复; Qos 1:
	 *            至少一次,确保消息到达,但消息重复可能会发生; Qos 2: 只有一次, 确保消息到且只到达一次.
	 * @return 成功返回0，失败返回-1
	 */
	public native int pushMsgToTag(String tag, String msg, int qos);

	/**
	 * 断开连接
	 *
	 * @return 成功返回0，失败返回-1
	 */
	private native int logout();

	private long self_ptr;

	private String _client_id;

	private String _server_url;

	private void showNotification(Context c, Class<?> target, Bundle b, String trick, String title,
			String content) {
		NotificationManager nm = (NotificationManager) c
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.ic_launcher, trick,
				System.currentTimeMillis());
		n.flags = Notification.FLAG_AUTO_CANCEL;
		n.defaults = Notification.DEFAULT_ALL;
		long[] vibrate = { 0, 100, 200, 300 };
		n.vibrate = vibrate;
		Intent i = new Intent(c, target);
		if (b != null)
			i.putExtras(b);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		// PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(c, R.string.app_name, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		n.setLatestEventInfo(c, title, content, contentIntent);
		nm.notify(R.string.app_name, n);
	}
}
