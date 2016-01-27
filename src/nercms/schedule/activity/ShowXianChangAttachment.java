package nercms.schedule.activity;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.actionbarsherlock.view.MenuItem;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

import nercms.schedule.R;
import nercms.schedule.utils.LocationAttachment;
import nercms.schedule.utils.MyGPS;
import nercms.schedule.utils.MyLocationListener;
import nercms.schedule.utils.MyLocationListener.ReceiveGPS;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.PlayVideo;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowXianChangAttachment extends BaseActivity implements OnClickListener {
	// 1:作业现场，2：操作现场，3：故障现场
	int enterType;
	String tid;

	private ImageView mImage;
	private TextView mTime;// 显示时间
	private Button mOk;
	private Button mCancel;
	private String imagePath;
	private int mPosition;
	private String type;
	private String audioPath;
	private String videoPath;
	private String filePath;
	private String mCurrentTime;

	private Map<Integer, Map<String, String>> mContent;
	private Map<String, String> mMap;
	private String from;// 判断时从哪个界面跳转过来的

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showxianchangsiattachment);

		enterType = getIntent().getIntExtra("enterType", -1);
		tid = getIntent().getStringExtra("tid");

		iniActionBar(true, null, "确认提交附件");

		mImage = (ImageView) findViewById(R.id.iv_attach);
		mTime = (TextView) findViewById(R.id.tv_time);
		mOk = (Button) findViewById(R.id.bt_ok);
		mCancel = (Button) findViewById(R.id.bt_cancel);

		mContent = (Map<Integer, Map<String, String>>) getIntent().getSerializableExtra("address");

		mMap = mContent.get(0);
		from = mMap.get("from");
		index = mMap.get("index");

		if (from.equals("XianChangSi")) {
			type = mMap.get("type");
			mPosition = Integer.parseInt(mMap.get("position"));
			System.out.println("从XianChangeSi 传递过来的position : " + mPosition);

			if (type.equals("image")) {
				imagePath = mMap.get("image");
				// 图片太大，imageview显示的时候容易内存溢出，所以需要将原图进行压缩
				String thumbnailUri = Utils.getThumbnailDir();
				// 获取缩略图,根据原图创建缩略图, mImagePath是原图的地址
				Utils.getThumbnail(imagePath, thumbnailUri);

				Bitmap bitmap = BitmapFactory.decodeFile(thumbnailUri);
				// 旋转
				int degree = Utils.readPictureDegree(imagePath);
				bitmap = Utils.rotateBitmap(bitmap, degree);
				mImage.setImageBitmap(bitmap);
			} else if (type.equals("audio")) {
				// audioPath = bundle.getString("audiopath");
				audioPath = mMap.get("audiopath");
				System.out.println("showXianChangActivity: " + audioPath);
				mImage.setImageResource(R.drawable.record);
				mImage.setOnClickListener(this);
			} else if (type.equals("video")) {

				// audioPath = bundle.getString("audiopath");
				videoPath = mMap.get("videopath");
				System.out.println("showXianChangActivity: " + videoPath);
				Bitmap videoThumbnailBitmap = getVideoThumbnail(videoPath, 400, 400,
						MediaStore.Images.Thumbnails.MINI_KIND);
				mImage.setImageBitmap(videoThumbnailBitmap);
				mImage.setOnClickListener(this);
			}
		} else if (from.equals("XianChangUpload")) {
			type = mMap.get("type");

			if (type.equals("selectImage") || type.equals("captureImage")) {
				filePath = mMap.get("path");

				// 图片太大，imageview显示的时候容易内存溢出，所以需要将原图进行压缩
				String thumbnailUri = Utils.getThumbnailDir();
				// 获取缩略图,根据原图创建缩略图, mImagePath是原图的地址
				Utils.getThumbnail(filePath, thumbnailUri);

				Bitmap bitmap = BitmapFactory.decodeFile(thumbnailUri);
				// 旋转
				int degree = Utils.readPictureDegree(filePath);
				bitmap = Utils.rotateBitmap(bitmap, degree);
				mImage.setImageBitmap(bitmap);
			} else if (type.equals("audio")) {
				filePath = mMap.get("path");
				mImage.setImageResource(R.drawable.record);
				mImage.setOnClickListener(this);
			} else if (type.equals("video")) {
				filePath = mMap.get("path");

				Bitmap videoThumbnailBitmap = getVideoThumbnail(filePath, 400, 400,
						MediaStore.Images.Thumbnails.MINI_KIND);
				mImage.setImageBitmap(videoThumbnailBitmap);

			}
		}

		mCurrentTime = Utils.formatDateMs(System.currentTimeMillis());

		mTime.setText(Utils.formatDateMs(System.currentTimeMillis()));

		mOk.setOnClickListener(this);
		mCancel.setOnClickListener(this);

		locate();// 开始定位

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.bt_ok:
			if (!Utils.isNetworkAvailable(getApplicationContext()) || mGPS == null) {
				Toast.makeText(getApplicationContext(), "无网络，无法获取GPS", Toast.LENGTH_SHORT).show();
				return;
			}
			Intent intent = new Intent();
			if (from.equals("XianChangSi")) {

				final Intent mintent = new Intent(ShowXianChangAttachment.this, XianChangAdd.class);

				mintent.putExtra("enterType", enterType);
				mintent.putExtra("tid", tid);

				// postion存在intent中，时间，gps,url都存在map中了（代表一个附件的完整信息）
				mintent.putExtra("position", mPosition);
				mintent.putExtra("from", "ShwoXianChangAttachment");

				Map<String, Object> mMap = new HashMap<String, Object>();
				if (type.equals("image")) {// 传递附件的路径

					mMap.put("path", imagePath);

				} else if (type.equals("audio")) {

					mMap.put("path", audioPath);

				} else if (type.equals("video")) {

					mMap.put("path", videoPath);

				}

				mMap.put("time", mCurrentTime);// 传递附件的时间戳
				mMap.put("index", 0 + "");// 第一个文件的下标是0
				mMap.put("gps", mGPS);
				mintent.putExtra("path", (Serializable) mMap);// intent中存了position和mMap
				startActivity(mintent);
				finish();
				return;
			} else if (from.equals("XianChangUpload")) {

				// 附件信息
				Map<String, Object> mMap1 = new HashMap<String, Object>();
				mMap1.put("gps", mGPS);
				mMap1.put("path", filePath);
				mMap1.put("time", mCurrentTime);// 传递附件的时间
				mMap1.put("index", index);

				// 传递filetype的目的是在xianchangupload中区分是seletimage还是captureImage来做相应的操作
				if (type.equals("selectImage")) {

					intent.putExtra("fileType", "selectImage");
				} else if (type.equals("captureImage")) {

					intent.putExtra("fileType", "captureImage");

				} else if (type.equals("audio")) {

					intent.putExtra("fileType", "audio");

				} else if (type.equals("video")) {
					intent.putExtra("fileType", "video");
				}
				intent.putExtra("path", (Serializable) mMap1);

			}
			setResult(RESULT_OK, intent);// 跳到XianChangUpload
			finish();

			break;

		case R.id.bt_cancel:
			if (from.equals("XianChangSi")) {
				if (type.equals("image")) {// 传递附件的路径
					Utils.deleteMedia(imagePath);
				} else if (type.equals("audio")) {
					Utils.deleteMedia(audioPath);
				}
			} else if (from.equals("XianChangUpload")) {
				// 在选择附件的时候，从相册中选择图片，点击取消按钮是不删除原图片的
				if (type.equals("captureImage") || type.equals("audio") || type.equals("video")) {
					Utils.deleteMedia(filePath);
				}
			}

			finish();
			break;

		case R.id.iv_attach:
			if (type.equals("audio")) {
				File file = new File(audioPath);
				Uri uri = Uri.fromFile(file);
				Intent intent1 = new Intent(Intent.ACTION_MAIN);
				intent1.setAction(Intent.ACTION_DEFAULT);
				intent1.setDataAndType(uri, "audio/*");
				startActivity(intent1);
			} else if (type.equals("video")) {
				// 点击播放视频
				Intent videoIntent = new Intent(ShowXianChangAttachment.this, PlayVideo.class);
				videoIntent.putExtra("path", videoPath);
				startActivity(videoIntent);
			}
			break;

		default:
			break;
		}
	}

	// 定位
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = null;

	MyGPS mGPS = new MyGPS();
	private String index;

	// 定位
	public void locate() {

		myListener = new MyLocationListener(new ReceiveGPS() {

			@Override
			public void onReceiveGPS(MyGPS gps) {
				// System.out
				// .println("ShowXianchangAttachment onReceiveGps : "
				// + gps.toString());
				mGPS = gps;
			}
		});
		mLocationClient = new LocationClient(getApplicationContext()); // 声明LocationClient类
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
	protected void onDestroy() {
		super.onDestroy();
		mLocationClient.stop();// 停止定位
	}

	private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);

		System.out.println("w" + bitmap.getWidth());
		System.out.println("h" + bitmap.getHeight());
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
}
