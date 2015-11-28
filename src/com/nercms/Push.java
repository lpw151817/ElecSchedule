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
 * ʹ�÷�����
 * 1����������client = new Push();
 * 2����ʼ����client.init("testClient", "test.mosquitto.org", 1883);
 * 3�����ûص�������Callbacks mCallbacks = new Callbacks(); cleint.setCallbacks(mCallbacks);
 * 4�����ӷ�������cleint.login();
 * 5������/������Ϣ��cleint.addTag("tag1", 1);  cleint.pushMsgToTag("tag1", String.valueOf(pubCount), 1);
 * 		
 * */

public class Push {

	// ///topic��personid
	// ///clientid:m_personid
	// ///message:{type:int,id:int}

	private final String TAG = getClass().getName();

	public static String SERVER_URL = Contants.SERVER;
	private static final int PORT = 1883;
	private final String TOPIC_HEADER = "nercms/schedule/";
	public static String PERSON_ID = "";// personid
	private final int QOS = 1;
	// Qos 0: ����һ��,��Ϣ������ȫ�����ײ�����,�ᷢ����Ϣ��ʧ���ظ�;
	// Qos 1: ����һ��,ȷ����Ϣ����,����Ϣ�ظ����ܻᷢ��;
	// Qos 2: ֻ��һ��, ȷ����Ϣ����ֻ����һ��.

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
		// clientidΪm_��ͷ
		init("m_" + PERSON_ID, SERVER_URL, PORT);
		setCallbacks(new ICallBacks() {

			@Override
			public int onMessageArrivedFunc(String client_id, String tag, int msg_len, String msg,
					int msg_qos, int duplicate_count) {
				Log.v(TAG, msg);
				// TODO ���е�����Ϣ�Ĵ���
				try {
					WebRequestManager manager = new WebRequestManager(AppApplication.getInstance(),
							c);

					Gson gson = new Gson();
					MqttResponse response = gson.fromJson(msg, MqttResponse.class);

					if (response != null) {
						switch (Integer.parseInt(response.getType())) {
						// ����
						case 1:
							AffairDao affairDao = new AffairDao(c);
							QueryAffairInfoResponse affair = affairDao
									.getAffairInfoByAid(response.getId());
							// ������ڣ���ɾ��������
							if (affair != null) {
								affairDao.deleteAffair(response.getId());
							}
							manager.getAffair(response.getId());
							break;
						// ����
						case 2:
							ConferenceDao conferenceDao = new ConferenceDao(c);
							ConferenceUpdateQueryResponseItem conference = conferenceDao
									.getConferenceByCid(response.getId());
							// ������ڣ���ɾ��������
							if (conference != null) {
								conferenceDao.deleteConferenceByID(response.getId());
							}
							manager.getConference(response.getId());
							break;
						// ������Ϣ
						case 3:
							manager.getMessage(response.getId());
							break;
						// Ⱥ����Ϣ
						case 4:
							manager.getMessage(response.getId());
							break;
						// ������
						case 5:
							manager.getMessage(response.getId());
							break;

						}

					}
				} catch (Exception e) {
					Log.e(TAG, "response.getType() ��������");
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
				String content = "�����µ�";

				switch (msg.what) {
				case Constant.QUERY_TASK_INFO_REQUEST_SUCCESS:
					QueryAffairInfoResponse info = (QueryAffairInfoResponse) msg.obj;
					int entranceStatus;
					int entranceType;
					// ������ͣ�1-��������2-��������
					if (info.getPod().contains(new CreateTaskRequestIds(getUserId()))) {
						entranceType = 1;
					} else
						entranceType = 2;

					// ��û�����ʱ��
					if (info.getCt() == null) {
						// ����ʱ��С�ڵ�ǰʱ��,�����Ѿ��ӳ�
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
					content += "����";
					break;
				case Constant.CONFERENCE_QUERY_SECCUESS:
					ConferenceQueryResponse r = (ConferenceQueryResponse) msg.obj;
					target = MeetingDetail.class;
					b.putString("conference_id", r.getCid());
					content += "����";
					break;
				case Constant.QUERY_MESSAGE_INFO_REQUEST_SUCCESS:
					ReceiveMessageResponse response = (ReceiveMessageResponse) msg.obj;
					target = ChatDetail.class;

					// ������Ϣ
					if (response.getT().equals("0")) {
						content += "��Ϣ";
						// ������յ����ͣ�����Ҫ��ѯ���Ǳ��û��뷢���ߵ������¼�������ǽ�����
						b.putInt("selected_id", Integer.parseInt(response.getSid()));
						b.putString("selected_name",
								new PersonDao(c).getPersonInfo(response.getSid()).getN());
						b.putBoolean("isGroup", false);
						b.putInt("entrance_type", 1);
					}
					// Ⱥ����Ϣ
					else if (response.getT().equals("1") || response.getT().equals("2")) {
						content += "��Ϣ";
						// ������յ����ͣ��������Ϊgroup��id
						b.putInt("selected_id", Integer.parseInt(response.getRid()));
						b.putString("selected_name",
								new GroupDao(c).queryGroupById(response.getRid()).getN());
						b.putBoolean("isGroup", true);
						b.putInt("entrance_type", 1);
					}
					// ����
					else if (response.getT().equals("4")) {
						b.putInt("entrance_type", 2);
						content += "����";
						// ������ΪtaskId
						b.putString("task_id", response.getRid());
						QueryAffairInfoResponse affairInfoResponse = new AffairDao(c)
								.getAffairInfoByAid(response.getRid());
						// task_status: 1-�����У�δ��ɣ���2-����ɣ�3-���ӳ�
						// ������ڽ���ʱ��,֤���Ѿ����
						if (!TextUtils.isEmpty(affairInfoResponse.getCt())) {
							b.putInt("task_status", 2);
						}
						// �����ڽ���ʱ��
						else {
							// �򵥵㣬��Ϊ-1
							b.putInt("task_status", -1);
						}
					}
					break;
				}

				showNotification(Push.this.c, target, b, content, "����ϵͳ", content);
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
			// �Ѿ����Ϸ�������
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
	 * ��ʼ���ͻ��� client_id���ͻ��˵�ID server_ip�������IP server_port:����˶˿ں�
	 * 
	 * @return �ɹ�����0��ʧ�ܷ���-1
	 */
	public native int init(String client_id, String server_ip, int server_port);

	/*
	 * ���ûص����� callbacks���ص�����
	 */
	public native void setCallbacks(ICallBacks callbacks);

	/**
	 * ���ӵ�¼��������
	 * 
	 * @return �ɹ�����0��ʧ�ܷ���-1
	 * 
	 */
	public native int login();

	/**
	 * ������Ϣ tag:��Ϣ���� qos:��Ϣ��qos
	 * 
	 * @param qos
	 *            Qos 0: ����һ��,��Ϣ������ȫ�����ײ�����,�ᷢ����Ϣ��ʧ���ظ�; Qos 1:
	 *            ����һ��,ȷ����Ϣ����,����Ϣ�ظ����ܻᷢ��; Qos 2: ֻ��һ��, ȷ����Ϣ����ֻ����һ��.
	 * @return �ɹ�����0��ʧ�ܷ���-1
	 */
	public native int addTag(String tag, int qos);

	/**
	 * ȡ��������Ϣ tag:��Ϣ����
	 * 
	 * @return �ɹ�����0��ʧ�ܷ���-1
	 */
	public native int removeTag(String tag);

	/**
	 * �ͻ��˵�ǰ������״̬
	 * 
	 * @return ����2����ʾ�Ѿ����ӵ������� ����1����ʾ�Ѵ����ͻ��˶��󣬻�δ���ӵ�������
	 */
	public native int state();

	/**
	 * ������Ϣ tag:��Ϣ���� msg����Ϣ���� qos:��Ϣ��qos
	 * 
	 * @param qos
	 *            Qos 0: ����һ��,��Ϣ������ȫ�����ײ�����,�ᷢ����Ϣ��ʧ���ظ�; Qos 1:
	 *            ����һ��,ȷ����Ϣ����,����Ϣ�ظ����ܻᷢ��; Qos 2: ֻ��һ��, ȷ����Ϣ����ֻ����һ��.
	 * @return �ɹ�����0��ʧ�ܷ���-1
	 */
	public native int pushMsgToTag(String tag, String msg, int qos);

	/**
	 * �Ͽ�����
	 *
	 * @return �ɹ�����0��ʧ�ܷ���-1
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
