package nercms.schedule.utils;

import android.util.Log;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;

public class MyLocationListener implements BDLocationListener {

	public MyLocationListener(ReceiveGPS listener) {
//		System.out.println("创建了几次");
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

		if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
			String time = location.getTime();// 获取时戳
			double longitude = location.getLongitude();// 获取经度
			double latitude = location.getLatitude();// 获取纬度
			float radius = location.getRadius();// 获取定位的精度
			double altitude = location.getAltitude();// 获取高度
			float speed = location.getSpeed();// 获取速度
			String coorType = location.getCoorType();// 获取采用的坐标系

			mGPS = new MyGPS(time, longitude, latitude, radius, altitude,
					speed, coorType);
			
			System.out.println(mGPS.toString());

			if (listener != null) {
				listener.onReceiveGPS(mGPS);
				System.out.println("回调了集采");
			}

//			Log.e("TAG", "网络定位");
		}
	}

}
