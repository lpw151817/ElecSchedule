package com.nercms;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.google.gson.Gson;

import android.os.Handler;
import android.util.Log;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.model.UpdateResponse;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.mqtt.MqttResponse;

public class MQTT {
	public static String SERVER_URL = "tcp://test.mosquitto.org:1883";
	public static String CLIENT_ID = MqttClient.generateClientId();// 不可超过23字节

	public final static String SUBSCRIBE_TOPIC_PREFIX = "nercms/schedule/";
	private final int SUBSCRIBE_QOS = 2;
	// Qos 0: 至多一次,消息发布完全依赖底层网络,会发生消息丢失或重复;
	// Qos 1: 至少一次,确保消息到达,但消息重复可能会发生;
	// Qos 2: 只有一次, 确保消息到且只到达一次.

	private final int CONNECTION_TIMEOUT = 10;// seconds
	private final int KEEP_ALIVE_INTERVAL = 45;// seconds
	private final long PUBLISH_TIMEOUT = 10000;// millisecond
	private final long DISCONNECTION_TIMEOUT = 10000;// millisecond

	private volatile static MQTT _unique_instance = null;

	public static MQTT get_instance() {
		if (null == _unique_instance) {
			synchronized (MQTT.class) {
				if (null == _unique_instance) {
					_unique_instance = new MQTT();
				}
			}
		}

		return _unique_instance;
	}

	private MqttClient _client = null;

	private MQTT() {

		////////////////////////////////////////////////////////
		MessageHandlerManager.getInstance().register(handler, Constants.MQTT_UPDATE_SUCCESS,
				UpdateResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.MQTT_UPDATE_FAIL,
				UpdateResponse.class.getName());
				////////////////////////////////////////////////////////

		// 创建MQTT客户端
		try {
			// 创建
			_client = new MqttClient(SERVER_URL, CLIENT_ID, new MemoryPersistence());

			// 设置回调
			MQTTClientCallback callback = new MQTTClientCallback();
			_client.setCallback(callback);

			// 初始化，连接及订阅
			init();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.v("MQTT", "create mqtt client error " + e.toString());
		}
	}

	private final String WILL_TOPIC = "mqtt/errors";
	private final String WILL_MESSAGE = "connection crashed";
	private Thread _check_thread = null;
	private boolean _check_thread_exit_flag = false;

	private void init() {
		connect();

		_check_thread = new Thread() {
			@Override
			public void run() {
				while (false == _check_thread_exit_flag) {
					if (false == _client.isConnected()) {
						disconnect();
						connect();
					}

					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		};
		_check_thread.start();
	}

	// 对外接口
	public void close() {
		disconnect();

		_check_thread_exit_flag = true;

		try {
			_check_thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		_check_thread = null;
	}

	private void connect() {
		synchronized (MQTT.class) {
			if (null == _client)
				return;

			try {
				if (false == _client.isConnected()) {
					// 连接
					MqttConnectOptions options = new MqttConnectOptions();
					// cleanSession为true则客户机建立连接时，将除去客户机的任何旧预订，当客户机断开连接时，会除去客户机在会话期间创建的任何新预订。
					// 为false则客户机创建的任何预订都会被添加至客户机在连接之前就已存在的所有预订，当客户机断开连接时，所有预订仍保持活动状态。
					// cleanSession为true时客户机仅在会话的作用域内创建预订和接收发布
					// cleanSession为false时是持久预订，无论客户端是否连接而其预订保持活动状态。当客户机重连时将接收任何未传递的发布。在它连接之后可修改处于活动状态的预订集。
					// 要更改此属性的设置，必须将客户机断开连接，然后再重新连接客户机。如果您将方式从使用
					// cleanSession=false 更改为
					// cleanSession=true，那么此客户机先前的所有预订以及尚未接收到的任何发布都将被废弃。
					options.setCleanSession(true);
					options.setConnectionTimeout(CONNECTION_TIMEOUT);// 设置连接超时时间
					options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);// 设置心跳间隔
					options.setWill(_client.getTopic(WILL_TOPIC), WILL_MESSAGE.getBytes(), 2, true);
					_client.connect(options);

					options = null;
				}

				if (true == _client.isConnected()) {
					Log.v("MQTT", "mqtt connect " + _client.isConnected());

					_client.subscribe(SUBSCRIBE_TOPIC_PREFIX + CLIENT_ID, SUBSCRIBE_QOS);

					Log.v("MQTT", "mqtt subscribe " + _client.isConnected());
				}
			} catch (MqttSecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void disconnect() {
		synchronized (MQTT.class) {
			if (null == _client)
				return;

			try {
				_client.disconnect(DISCONNECTION_TIMEOUT);
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Log.v("MQTT", "mqtt disconnect " + _client.isConnected());
		}
	}

	// 对外接口
	public boolean publish_message(String message_topic, String content, int QoS) {
		synchronized (MQTT.class) {
			if (null == _client || false == _client.isConnected()) {
				init();
			}

			if (null == _client || false == _client.isConnected()) {
				return false;
			}

			// 发送消息并获取回执
			try {
				MqttTopic topic = _client.getTopic(message_topic);// 创建topic

				// MqttDeliveryToken token = topic.publish(content.getBytes(),
				// QoS, false);
				MqttMessage message = new MqttMessage();
				message.setQos(QoS);
				message.setRetained(false);// 是否在服务器中保存消息体
				message.setPayload(content.getBytes());

				// long t = System.currentTimeMillis();
				MqttDeliveryToken token = topic.publish(message);
				// Log.v("Baidu", "mqtt 1 " + (System.currentTimeMillis() - t));
				while (false == token.isComplete()) {
					token.waitForCompletion(PUBLISH_TIMEOUT);
				}
				// Log.v("Baidu", "mqtt 2 " + (System.currentTimeMillis() - t));

				if (true == token.isComplete()) {
					// Log.v("MQTT", "client(" + _client.getClientId() + ")
					// Publishing " + message.toString() + " on topic " +
					// topic.getName() + " with QoS = " + message.getQos());
				} else {
					disconnect();
					connect();
				}

				return token.isComplete();
			} catch (MqttPersistenceException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				Log.v("MQTT", "mqtt publish error 1: " + e.toString());
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
				Log.v("MQTT", "mqtt publish error 2: " + e.toString());
			}
		}

		return false;
	}

	/////////////////////////////////////////////
	public static Handler handler;

	public class MQTTClientCallback implements MqttCallback {
		@Override
		public void messageArrived(String topic, MqttMessage message) {
			Log.i("MQTT", "Recv message: " + message.toString() + " on topic: " + topic.toString());

			// 进行到达消息的处理
			try {

				WebRequestManager manager = new WebRequestManager(AppApplication.getInstance(),
						AppApplication.getInstance().getContext());
				Gson gson = new Gson();
				MqttResponse response = gson.fromJson(message.toString(), MqttResponse.class);

				if (response != null) {
					// 进行数据更新
					manager.mqttUpdate(AppApplication.getInstance().getContext(), response);
				}

			} catch (Exception e) {
				Log.e("MQTT", "response.getType() 解析错误");
				e.printStackTrace();
			}

		}

		@Override
		public void connectionLost(Throwable cause) {
			Log.v("MQTT",
					"connection lost due to " + cause.getMessage() + ", "
							+ ((MqttException) cause).getReasonCode() + ", "
							+ ((MqttException) cause).getCause());

			disconnect();
			connect();
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken token) {
			// Log.v("MQTT", "delivery complete: " + token.toString());
		}
	}

}
