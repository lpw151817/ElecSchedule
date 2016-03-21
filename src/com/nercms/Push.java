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

	public static String SERVER_URL = Contants.MQTT_SERVER;
	private static final int PORT = Integer.parseInt(Contants.MQTT_PORT);
	private final String TOPIC_HEADER = "nercms/schedule/";
	public static String PERSON_ID;// personid
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

	MqttResponse response;

	public void ini() {
		Log.e("mqtt ini", SERVER_URL + "/" + PORT);
		// ini handler
		iniHandler();
		// ini uid
		PERSON_ID = getUserId();
		// clientidΪm_��ͷ
		init("m_" + PERSON_ID, SERVER_URL, PORT);
		setCallbacks(new ICallBacks() {

			@Override
			public int onMessageArrivedFunc(String client_id, String tag, int msg_len, String msg,
					int msg_qos, int duplicate_count) {
				Log.v("onMessageArrivedFunc...", msg);
				// ���е�����Ϣ�Ĵ���
				try {
					WebRequestManager manager = new WebRequestManager(AppApplication.getInstance(),
							c);
					Gson gson = new Gson();
					response = gson.fromJson(msg, MqttResponse.class);

					if (response != null) {
						// �������ݸ���
						manager.loginUpdate(c);
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
				Log.e("handleMessage>>>>>>>>>>>", msg.toString() + "....." + msg.what);
				Class target = null;
				Bundle b = null;
				b = new Bundle();
				String content = "�����µ�";

				switch (msg.what) {
				case Constants.LOGIN_UPDATE_SUCCESS:
					if (response != null) {
						if (response.getType().equals("1")) {
							// ��ʼ�ͽ����������͸��쵼
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							target = PlanAdd.class;
							content += "����";
						} else if (response.getType().equals("2")) {
							// �½�ָ��ʱ�������͸���������
							target = NewTask.class;
							b.putString("taskInsId", response.getId());
							content += "ָ��";
						} else if (response.getType().equals("3")) {
							// �ϴ����������͸�Ӧ�ϸڵ�λ�쵼�����û�оͲ�����
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							target = PlanAdd.class;
							content += "���񸽼�";
						}
					}
					break;
				case Constants.LOGIN_UPDATE_FAIL:
				case Constants.LOGIN_UPDATE_SAVE_FAIL:
					break;
				}

				// if (target != null)
				showNotification(Push.this.c, target, b, content, "����ϵͳ", content);
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
