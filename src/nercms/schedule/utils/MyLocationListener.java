package nercms.schedule.utils;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

public class MyLocationListener implements BDLocationListener {

	public MyLocationListener(ReceiveGPS listener) {
//		System.out.println("�����˼���");
		this.listener = listener;
	}
	
	public MyLocationListener() {
	}

	public interface ReceiveGPS {
		public void onReceiveGPS(MyGPS gps);
	}

	ReceiveGPS listener;

//	public void setReceiveGPSListener(ReceiveGPS listener) {
//		this.listener = listener;
//	}

	private MyGPS mGPS;

	@Override
	public void onReceiveLocation(BDLocation location) {
		if (location == null)
			return;

		if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// ���綨λ���
			String time = location.getTime();// ��ȡʱ��
			double longitude = location.getLongitude();// ��ȡ����
			double latitude = location.getLatitude();// ��ȡγ��
			float radius = location.getRadius();// ��ȡ��λ�ľ���
			double altitude = location.getAltitude();// ��ȡ�߶�
			float speed = location.getSpeed();// ��ȡ�ٶ�
			String coorType = location.getCoorType();// ��ȡ���õ�����ϵ

			mGPS = new MyGPS(time, longitude, latitude, radius, altitude,
					speed, coorType);
			
			System.out.println(mGPS.toString());

			if (listener != null) {
				listener.onReceiveGPS(mGPS);
				System.out.println("�ص��˼���");
			}

//			Log.e("TAG", "���綨λ");
		}
	}

}
