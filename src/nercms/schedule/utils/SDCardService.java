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
		Log.i(TAG, "SD��������...");
		// ��IntentFilter��ѡ����Ҫ��������Ϊ
		IntentFilter intentFilter = new IntentFilter(
				Intent.ACTION_MEDIA_MOUNTED);// sd�������룬���Ѿ�����
		intentFilter.setPriority(1000);// ����������ȼ�
		intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);// sd�����ڣ�����û�й���
		intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);// sd�����Ƴ�
		intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);// sd����Ϊ
															// USB�������洢���������ر����
		intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);// sd���Ѿ���sd����۰γ������ǹ��ص㻹û���
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);// ��ʼɨ��
		intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);// ɨ�����
		intentFilter.addDataScheme("file");
		registerReceiver(SdBroadcastRec, intentFilter);// ע���������
		Log.i(TAG, "sd״̬�ı�");

		// ע���������
		IntentFilter netFilter = new IntentFilter();
		netFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(netBroadcastRec, netFilter);
	}

	private final BroadcastReceiver SdBroadcastRec = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("android.intent.action.MEDIA_MOUNTED"))// SD���Ѿ��ɹ�����
			{
				MessageHandlerManager.getInstance().sendMessage(
						LocalConstant.SD_MOUNTED);
			} else if (action.equals("android.intent.action.MEDIA_REMOVED")// ����δ����״̬
					|| action
							.equals("android.intent.action.ACTION_MEDIA_UNMOUNTED")
					|| action
							.equals("android.intent.action.ACTION_MEDIA_BAD_REMOVAL")) {
				MessageHandlerManager.getInstance().sendMessage(
						LocalConstant.SD_UNMOUNTED);
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {// ��ʼɨ��
			} else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {// ɨ�����
			} else if (action.equals(Intent.ACTION_MEDIA_SHARED)) {// ��չ���ʵĹ��ر����
																	// (unmount)����Ϊ���Ѿ���Ϊ
																	// USB
																	// �������洢������
			} else {
			}
		}
	};

	private final BroadcastReceiver netBroadcastRec = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				Log.d(TAG, "����״̬�Ѿ��ı�");
				connectivityManager = (ConnectivityManager)

				getSystemService(Context.CONNECTIVITY_SERVICE);
				info = connectivityManager.getActiveNetworkInfo();
				if (info != null && info.isAvailable()) {
					String name = info.getTypeName();
					Log.d(TAG, "��ǰ�������ƣ�" + name);
					MessageHandlerManager.getInstance().sendMessage(
							LocalConstant.NET_AVAILABLE, (Object) name);
				} else {
					Log.d(TAG, "û�п�������");
					MessageHandlerManager.getInstance().sendMessage(
							LocalConstant.NET_UNAVAILABLE);
				}
			}
		}

	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(SdBroadcastRec);// ȡ��ע��
		unregisterReceiver(netBroadcastRec);
		Log.i(TAG, "ֹͣSdNetService");
	}

}
