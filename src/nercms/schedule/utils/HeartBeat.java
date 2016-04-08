package nercms.schedule.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.model.HeartBeatResponse;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;

public class HeartBeat {
	public static Object _exit_flag = new Object();

	private static ScheduledExecutorService _network_status_service = null;

	private static long heart_beat_period = 60000L;
	private static WebRequestManager manager;

	public static void start(final Context context) {
		if (manager == null)
			manager = new WebRequestManager(AppApplication.getInstance(), context);

		registHandler();

		Thread network_status_thread = new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						// 调用心跳http接口
						manager.heartBeat(context);

						Thread.sleep(heart_beat_period);
					}

				} catch (Exception e) {
					e.printStackTrace();
					MessageHandlerManager.getInstance().sendMessage(Constants.HEART_BEAT_FAIL,
							HeartBeatResponse.class.getName());
				}
			}
		};

		_network_status_service = Executors.newScheduledThreadPool(1);
		_network_status_service.scheduleAtFixedRate(network_status_thread, 100, 100,
				TimeUnit.MILLISECONDS);
	}

	public static void stop(Context context) {
		_network_status_service.shutdownNow();
	}

	private static Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case Constants.HEART_BEAT_SUCCESS:
				// 心跳成功
				Log.v("HeartBeat", "success!");
				break;

			case Constants.HEART_BEAT_FAIL:
				// 心跳失败
				Log.v("HeartBeat", "fail!");
				break;
			}
		};
	};

	private static void registHandler() {
		MessageHandlerManager.getInstance().register(handler, Constants.HEART_BEAT_SUCCESS,
				HeartBeatResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.HEART_BEAT_FAIL,
				HeartBeatResponse.class.getName());
	}
}
