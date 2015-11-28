package nercms.schedule.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.util.Log;
import android.wxapp.service.handler.MessageHandlerManager;

public class SDCardService extends Service {

	private static final String TAG = "SdNetService";

	private ConnectivityManager connectivityManager;
	private NetworkInfo info;

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "SD服务启动...");
		// 在IntentFilter中选择你要监听的行为
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_MEDIA_MOUNTED);// sd卡被插入，且已经挂载
		intentFilter.setPriority(1000);// 设置最高优先级
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd卡存在，但还没有挂载
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd卡被移除
		intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd卡作为
															// USB大容量存储被共享，挂载被解除
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd卡已经从sd卡插槽拔出，但是挂载点还没解除
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// 开始扫描
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// 扫描完成
		intentFilter.addDataScheme("file");
		registerReceiver(SdBroadcastRec, intentFilter);// 注册监听函数
		Log.i(TAG, "sd状态改变");

		// 注册网络监听
		IntentFilter netFilter = new IntentFilter();
		netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(netBroadcastRec, netFilter);
	}

	private final BroadcastReceiver SdBroadcastRec = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("android.intent.action.MEDIA_MOUNTED"))// SD卡已经成功挂载
			{
				MessageHandlerManager.getInstance().sendMessage(
						LocalConstant.SD_MOUNTED);
			} else if (action.equals("android.intent.action.MEDIA_REMOVED")// 各种未挂载状态
					|| action
							.equals("android.intent.action.ACTION_MEDIA_UNMOUNTED")
					|| action
							.equals("android.intent.action.ACTION_MEDIA_BAD_REMOVAL")) {
				MessageHandlerManager.getInstance().sendMessage(
						LocalConstant.SD_UNMOUNTED);
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {// 开始扫描
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {// 扫描完成
			} else if (action.equals(Intent.ACTION_MEDIA_SHARED)) {// 扩展介质的挂载被解除
																	// (unmount)。因为它已经作为
																	// USB
																	// 大容量存储被共享
			} else {
			}
		}
	};

	private final BroadcastReceiver netBroadcastRec = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				Log.d(TAG, "网络状态已经改变");
				connectivityManager = (ConnectivityManager)

				getSystemService(Context.CONNECTIVITY_SERVICE);
				info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					String name = info.getTypeName();
					Log.d(TAG, "当前网络名称：" + name);
					MessageHandlerManager.getInstance().sendMessage(
							LocalConstant.NET_AVAILABLE, (Object) name);
				} else {
					Log.d(TAG, "没有可用网络");
					MessageHandlerManager.getInstance().sendMessage(
							LocalConstant.NET_UNAVAILABLE);
				}
			}
		}

	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(SdBroadcastRec);// 取消注册
		unregisterReceiver(netBroadcastRec);
		Log.i(TAG, "停止SdNetService");
	}

}
