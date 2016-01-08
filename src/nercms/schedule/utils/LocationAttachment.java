package nercms.schedule.utils;

import nercms.schedule.utils.MyLocationListener.ReceiveGPS;
import android.content.Context;
import android.util.Log;

import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

public class LocationAttachment implements ReceiveGPS{
	// 定位
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener;

	private Context mcontext;
//	private ReceiveGPS listener;

	public LocationAttachment(Context context, ReceiveGPS listener) {
		mcontext = context;
//		this.listener = listener;
		this.myListener = new MyLocationListener(listener);
	}

	// 定位
	public void locate() {

		mLocationClient = new LocationClient(mcontext); // 声明LocationClient类
		mLocationClient.registerLocationListener(myListener); // 注册监听函数
		initLocation();
		mLocationClient.start();// 开始定位
	}

	public void initLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// 可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
		option.setCoorType("bd09ll");// 可选，默认gcj02，设置返回的定位结果坐标系
		// int span = 1000;
		// option.setScanSpan(span);//
		// 可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
		option.setScanSpan(0);
		option.setIsNeedAddress(true);// 可选，设置是否需要地址信息，默认不需要
		option.setOpenGps(true);// 可选，默认false,设置是否使用gps
		option.setLocationNotify(true);// 可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
		option.setIsNeedLocationDescribe(true);// 可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
		option.setIgnoreKillProcess(false);// 可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
		option.SetIgnoreCacheException(false);// 可选，默认false，设置是否收集CRASH信息，默认收集
		option.setEnableSimulateGps(false);// 可选，默认false，设置是否需要过滤gps仿真结果，默认需要
		mLocationClient.setLocOption(option);
	}

	@Override
	public void onReceiveGPS(MyGPS gps) {
		//定位成功之后关闭定位服务
		mLocationClient.stop();
		mLocationClient.unRegisterLocationListener(myListener);
	}

}
