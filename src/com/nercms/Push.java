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
import android.text.TextUtils;
import android.util.Log;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.AffairDao;
import android.wxapp.service.dao.ConferenceDao;
import android.wxapp.service.dao.GroupDao;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.affair.CreateTaskRequestIds;
import android.wxapp.service.jerry.model.affair.QueryAffairInfoResponse;
import android.wxapp.service.jerry.model.conference.ConferenceQueryResponse;
import android.wxapp.service.jerry.model.conference.ConferenceUpdateQueryResponseItem;
import android.wxapp.service.jerry.model.message.ReceiveMessageResponse;
import android.wxapp.service.jerry.model.mqtt.MqttResponse;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;
import nercms.schedule.R;
import nercms.schedule.activity.ChatDetail;
import nercms.schedule.activity.MeetingDetail;
import nercms.schedule.activity.TaskDetail;

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

	public static String SERVER_URL = Contants.SERVER;
	private static final int PORT = 1883;
	private final String TOPIC_HEADER = "nercms/schedule/";
	public static String PERSON_ID = "";// personid
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

	public void ini() {
		Log.e("mqtt ini", "ini()");
		// ini handler
		iniHandler();
		// clientid为m_开头
		init("m_" + PERSON_ID, SERVER_URL, PORT);
		setCallbacks(new ICallBacks() {

			@Override
			public int onMessageArrivedFunc(String client_id, String tag, int msg_len, String msg,
					int msg_qos, int duplicate_count) {
				Log.v(TAG, msg);
				// TODO 进行到达消息的处理
				try {
					WebRequestManager manager = new WebRequestManager(AppApplication.getInstance(),
							c);

					Gson gson = new Gson();
					MqttResponse response = gson.fromJson(msg, MqttResponse.class);

					if (response != null) {
						switch (Integer.parseInt(response.getType())) {
						// 事务
						case 1:
							AffairDao affairDao = new AffairDao(c);
							QueryAffairInfoResponse affair = affairDao
									.getAffairInfoByAid(response.getId());
							// 如果存在，则删除旧数据
							if (affair != null) {
								affairDao.deleteAffair(response.getId());
							}
							manager.getAffair(response.getId());
							break;
						// 会议
						case 2:
							ConferenceDao conferenceDao = new ConferenceDao(c);
							ConferenceUpdateQueryResponseItem conference = conferenceDao
									.getConferenceByCid(response.getId());
							// 如果存在，则删除旧数据
							if (conference != null) {
								conferenceDao.deleteConferenceByID(response.getId());
							}
							manager.getConference(response.getId());
							break;
						// 个人消息
						case 3:
							manager.getMessage(response.getId());
							break;
						// 群组消息
						case 4:
							manager.getMessage(response.getId());
							break;
						// 事务反馈
						case 5:
							manager.getMessage(response.getId());
							break;

						}

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
				Class target = null;
				Bundle b = null;
				b = new Bundle();
				String content = "您有新的";

				switch (msg.what) {
				case Constant.QUERY_TASK_INFO_REQUEST_SUCCESS:
					QueryAffairInfoResponse info = (QueryAffairInfoResponse) msg.obj;
					int entranceStatus;
					int entranceType;
					// 入口类型：1-发起任务；2-接收任务
					if (info.getPod().contains(new CreateTaskRequestIds(getUserId()))) {
						entranceType = 1;
					} else
						entranceType = 2;

					// 当没有完成时间
					if (info.getCt() == null) {
						// 结束时间小于当前时间,任务已经延迟
						if (info.getEt().compareTo(System.currentTimeMillis() + "") < 0) {
							entranceStatus = 3;
						} else
							entranceStatus = 1;
					} else {
						entranceStatus = 2;
					}
					b.putInt("type", entranceType);
					b.putInt("status", entranceStatus);
					b.putString("id", info.getAid());
					target = TaskDetail.class;
					content += "事务";
					break;
				case Constant.CONFERENCE_QUERY_SECCUESS:
					ConferenceQueryResponse r = (ConferenceQueryResponse) msg.obj;
					target = MeetingDetail.class;
					b.putString("conference_id", r.getCid());
					content += "会议";
					break;
				case Constant.QUERY_MESSAGE_INFO_REQUEST_SUCCESS:
					ReceiveMessageResponse response = (ReceiveMessageResponse) msg.obj;
					target = ChatDetail.class;

					// 个人消息
					if (response.getT().equals("0")) {
						content += "消息";
						// 如果接收到推送，则需要查询的是本用户与发送者的聊天记录，而不是接受者
						b.putInt("selected_id", Integer.parseInt(response.getSid()));
						b.putString("selected_name",
								new PersonDao(c).getPersonInfo(response.getSid()).getN());
						b.putBoolean("isGroup", false);
						b.putInt("entrance_type", 1);
					}
					// 群组消息
					else if (response.getT().equals("1") || response.getT().equals("2")) {
						content += "消息";
						// 如果接收到推送，则接受者为group的id
						b.putInt("selected_id", Integer.parseInt(response.getRid()));
						b.putString("selected_name",
								new GroupDao(c).queryGroupById(response.getRid()).getN());
						b.putBoolean("isGroup", true);
						b.putInt("entrance_type", 1);
					}
					// 反馈
					else if (response.getT().equals("4")) {
						b.putInt("entrance_type", 2);
						content += "反馈";
						// 接受者为taskId
						b.putString("task_id", response.getRid());
						QueryAffairInfoResponse affairInfoResponse = new AffairDao(c)
								.getAffairInfoByAid(response.getRid());
						// task_status: 1-进行中（未完成）；2-已完成；3-已延迟
						// 如果存在结束时间,证明已经完成
						if (!TextUtils.isEmpty(affairInfoResponse.getCt())) {
							b.putInt("task_status", 2);
						}
						// 不存在结束时间
						else {
							// 简单点，置为-1
							b.putInt("task_status", -1);
						}
					}
					break;
				}

				showNotification(Push.this.c, target, b, content, "调度系统", content);
			}
		};

		MessageHandlerManager.getInstance().register(handler,
				Constant.QUERY_TASK_INFO_REQUEST_SUCCESS, Contants.METHOD_AFFAIRS_QUERY_INFO);
		MessageHandlerManager.getInstance().register(handler, Constant.CONFERENCE_QUERY_SECCUESS,
				Contants.METHOD_CONFERENCE_QUERY);
		MessageHandlerManager.getInstance().register(handler,
				Constant.QUERY_MESSAGE_INFO_REQUEST_SUCCESS, Contants.METHOD_MESSAGE_RECEIVE);
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
