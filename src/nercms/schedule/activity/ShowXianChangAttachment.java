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
	// 1:��ҵ�ֳ���2�������ֳ���3�������ֳ�
	int enterType;
	String tid;

	private ImageView mImage;
	private TextView mTime;// ��ʾʱ��
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
	private String from;// �ж�ʱ���ĸ�������ת������

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showxianchangsiattachment);

		enterType = getIntent().getIntExtra("enterType", -1);
		tid = getIntent().getStringExtra("tid");

		iniActionBar(true, null, "ȷ���ύ����");

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
			System.out.println("��XianChangeSi ���ݹ�����position : " + mPosition);

			if (type.equals("image")) {
				imagePath = mMap.get("image");
				// ͼƬ̫��imageview��ʾ��ʱ�������ڴ������������Ҫ��ԭͼ����ѹ��
				String thumbnailUri = Utils.getThumbnailDir();
				// ��ȡ����ͼ,����ԭͼ��������ͼ, mImagePath��ԭͼ�ĵ�ַ
				Utils.getThumbnail(imagePath, thumbnailUri);

				Bitmap bitmap = BitmapFactory.decodeFile(thumbnailUri);
				// ��ת
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

				// ͼƬ̫��imageview��ʾ��ʱ�������ڴ������������Ҫ��ԭͼ����ѹ��
				String thumbnailUri = Utils.getThumbnailDir();
				// ��ȡ����ͼ,����ԭͼ��������ͼ, mImagePath��ԭͼ�ĵ�ַ
				Utils.getThumbnail(filePath, thumbnailUri);

				Bitmap bitmap = BitmapFactory.decodeFile(thumbnailUri);
				// ��ת
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

		locate();// ��ʼ��λ

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
				Toast.makeText(getApplicationContext(), "�����磬�޷���ȡGPS", Toast.LENGTH_SHORT).show();
				return;
			}
			Intent intent = new Intent();
			if (from.equals("XianChangSi")) {

				final Intent mintent = new Intent(ShowXianChangAttachment.this, XianChangAdd.class);

				mintent.putExtra("enterType", enterType);
				mintent.putExtra("tid", tid);

				// postion����intent�У�ʱ�䣬gps,url������map���ˣ�����һ��������������Ϣ��
				mintent.putExtra("position", mPosition);
				mintent.putExtra("from", "ShwoXianChangAttachment");

				Map<String, Object> mMap = new HashMap<String, Object>();
				if (type.equals("image")) {// ���ݸ�����·��

					mMap.put("path", imagePath);

				} else if (type.equals("audio")) {

					mMap.put("path", audioPath);

				} else if (type.equals("video")) {

					mMap.put("path", videoPath);

				}

				mMap.put("time", mCurrentTime);// ���ݸ�����ʱ���
				mMap.put("index", 0 + "");// ��һ���ļ����±���0
				mMap.put("gps", mGPS);
				mintent.putExtra("path", (Serializable) mMap);// intent�д���position��mMap
				startActivity(mintent);
				finish();
				return;
			} else if (from.equals("XianChangUpload")) {

				// ������Ϣ
				Map<String, Object> mMap1 = new HashMap<String, Object>();
				mMap1.put("gps", mGPS);
				mMap1.put("path", filePath);
				mMap1.put("time", mCurrentTime);// ���ݸ�����ʱ��
				mMap1.put("index", index);

				// ����filetype��Ŀ������xianchangupload��������seletimage����captureImage������Ӧ�Ĳ���
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
			setResult(RESULT_OK, intent);// ����XianChangUpload
			finish();

			break;

		case R.id.bt_cancel:
			if (from.equals("XianChangSi")) {
				if (type.equals("image")) {// ���ݸ�����·��
					Utils.deleteMedia(imagePath);
				} else if (type.equals("audio")) {
					Utils.deleteMedia(audioPath);
				}
			} else if (from.equals("XianChangUpload")) {
				// ��ѡ�񸽼���ʱ�򣬴������ѡ��ͼƬ�����ȡ����ť�ǲ�ɾ��ԭͼƬ��
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
				// ���������Ƶ
				Intent videoIntent = new Intent(ShowXianChangAttachment.this, PlayVideo.class);
				videoIntent.putExtra("path", videoPath);
				startActivity(videoIntent);
			}
			break;

		default:
			break;
		}
	}

	// ��λ
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener = null;

	MyGPS mGPS = new MyGPS();
	private String index;

	// ��λ
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
		mLocationClient = new LocationClient(getApplicationContext()); // ����LocationClient��
		mLocationClient.registerLocationListener(myListener); // ע���������
		initLocation();
		mLocationClient.start();// ��ʼ��λ
	}

	public void initLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);// ��ѡ��Ĭ�ϸ߾��ȣ����ö�λģʽ���߾��ȣ��͹��ģ����豸
		option.setCoorType("bd09ll");// ��ѡ��Ĭ��gcj02�����÷��صĶ�λ�������ϵ
		// int span = 1000;
		// option.setScanSpan(span);//
		// ��ѡ��Ĭ��0��������λһ�Σ����÷���λ����ļ����Ҫ���ڵ���1000ms������Ч��
		option.setScanSpan(0);
		option.setIsNeedAddress(true);// ��ѡ�������Ƿ���Ҫ��ַ��Ϣ��Ĭ�ϲ���Ҫ
		option.setOpenGps(true);// ��ѡ��Ĭ��false,�����Ƿ�ʹ��gps
		option.setLocationNotify(true);// ��ѡ��Ĭ��false�������Ƿ�gps��Чʱ����1S1��Ƶ�����GPS���
		option.setIsNeedLocationDescribe(true);// ��ѡ��Ĭ��false�������Ƿ���Ҫλ�����廯�����������BDLocation.getLocationDescribe��õ�����������ڡ��ڱ����찲�Ÿ�����
		option.setIgnoreKillProcess(false);// ��ѡ��Ĭ��false����λSDK�ڲ���һ��SERVICE�����ŵ��˶������̣������Ƿ���stop��ʱ��ɱ��������̣�Ĭ��ɱ��
		option.SetIgnoreCacheException(false);// ��ѡ��Ĭ��false�������Ƿ��ռ�CRASH��Ϣ��Ĭ���ռ�
		option.setEnableSimulateGps(false);// ��ѡ��Ĭ��false�������Ƿ���Ҫ����gps��������Ĭ����Ҫ
		mLocationClient.setLocOption(option);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mLocationClient.stop();// ֹͣ��λ
	}

	private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// ��ȡ��Ƶ������ͼ
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);

		System.out.println("w" + bitmap.getWidth());
		System.out.println("h" + bitmap.getHeight());
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
}
