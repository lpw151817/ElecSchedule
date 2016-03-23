package nercms.schedule.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.MyGPS;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.FixedGridLayout;
import nercms.schedule.view.PlayVideo;
import nercms.schedule.view.RoundAngleImageView;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.model.StartTaskResponse;
import android.wxapp.service.elec.model.UploadTaskAttachmentResponse;
import android.wxapp.service.elec.model.bean.Attachments;
import android.wxapp.service.elec.model.bean.GPS;
import android.wxapp.service.elec.model.bean.TaskAttachment;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.HttpUploadTask;

/*
 * 
 */

public class XianChangUpload extends BaseActivity implements OnClickListener {

	private String videopath;

	private String audiopath;

	private String mImagePath;

	private String audioFileName;

	Button bt_select, bt_shangchuangAttach;
	// Button bt_upload;

	// 附件缩略图展示Layout
	private LinearLayout showAttachLayout;
	// 图片容器
	private FixedGridLayout imageContainer;

	// 图片显示的宽高
	private static int IMG_WIDTH = 130;
	private static int IMG_HEIGHT = 130;
	// 每行显示缩略图数目
	private final int NUMPERROW = 3;
	private String mFilePath;// 传递过来的附件路径
	private String mFileName;
	// 显示大图对话框
	private Dialog imageDialog;

	// 附件编号,
	private int mediaIndex = 0;

	// 保存mediaIndex与media缩略图地址的映射,
	// 存放每一个媒体文件的地址
	private HashMap<Integer, String> index_originalPath_Map = new HashMap<Integer, String>();

	// // 保存mediaIndex与media缩略图地址的映射
	private HashMap<Integer, String> index_path_Map = new HashMap<Integer, String>();

	// 附件集,用来上传附件
	private ArrayList<XianChangUpload.Media> mediaList = new ArrayList<XianChangUpload.Media>();

	List<Map<String, Object>> mUrl = new ArrayList<Map<String, Object>>();

	List<Map<String, Object>> mUploadUrl = new ArrayList<Map<String, Object>>();// 用来存放已经上传的附件

	List<Map<String, Object>> mUnUploadUrl = new ArrayList<Map<String, Object>>();// 用来存放未上传的附件

	private String fileName;

	boolean isOk = false;

	private int mediaID1;

	private String selectimagepath;

	private Uri uri1;

	private String selectThumbnailUri;

	private int mediaID2;

	private Uri uri2;

	private String thumbnailUri;

	private String captureImageName;

	private String selectImageName;

	private int mediaID;

	private String captureAudioName;

	private Bitmap audioThumbnailBitmap;

	private Uri uri;

	private String videoName;

	private Uri uri4;

	private int mediaID4;

	private Bitmap videoThumbnailBitmap;

	private Context c;

	String tid;

	WebRequestManager requestManager;
	int enterType;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_upload);

		requestManager = new WebRequestManager(AppApplication.getInstance(), this);

		iniActionBar(true, null, null);

		tid = getIntent().getStringExtra("tid");
		enterType = getIntent().getIntExtra("enterType", -1);
		c = XianChangUpload.this;

		bt_select = (Button) findViewById(R.id.select);
		bt_shangchuangAttach = (Button) findViewById(R.id.shangchuanfujian);
		if (isAdmin() || !TextUtils.isEmpty(new PlanTaskDao(this).getPlanTask(tid).getEnd_time())) {
			bt_select.setVisibility(View.GONE);
			bt_shangchuangAttach.setVisibility(View.GONE);
		}

		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();

		IMG_WIDTH = (width - 100) / 3;
		IMG_HEIGHT = IMG_WIDTH;

		// 附件缩略图展示Layout，默认不可见
		showAttachLayout = (LinearLayout) findViewById(R.id.showAttathLayout);
		showAttachLayout.setVisibility(View.GONE);

		// 图片容器初始化
		imageContainer = (FixedGridLayout) findViewById(R.id.attachContainer);
		imageContainer.setCellWidth(IMG_WIDTH);
		imageContainer.setCellHeight(IMG_HEIGHT);
		imageContainer.setNumPerRow(NUMPERROW);

		int mIndex = getIntent().getIntExtra("myMediaIndex", -1);
		System.out.println("mIndex: " + mIndex);

		mUploadUrl = (List<Map<String, Object>>) getIntent().getSerializableExtra("mUploadUrl");

		// 进入upload有两种顺序：1.从showxianchangattachment=>xianchangAdd=>upload,这种设置为0
		// 2.xianchanadd=>upload(点击actionbar的home)=>xianchangadd=>upload,这种mediaIndex保存上次的值
		// mediaIndex = getIntent().getIntExtra("mediaIndex", mIndex);
		mediaIndex = mIndex;

		loadAttachment();

		bt_select.setOnClickListener(this);
		bt_shangchuangAttach.setOnClickListener(this);

		position = getIntent().getIntExtra("position", -1);
		initHandler();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.select:// 选择附件
			int[] isRadio;
			int[] isPhoto;
			int[] isVideo;
			int position = getIntent().getIntExtra("position", -1);
			List<String> name = new ArrayList<String>();

			// 操作线程和作业现场的每个条目中的附件按钮的操作是不一样的
			if (Main.pageType == 0) {

				isRadio = c.getResources().getIntArray(R.array.zuoyexianchang_si_radio);
				isPhoto = c.getResources().getIntArray(R.array.zuoyexianchang_si_photo);
				isVideo = c.getResources().getIntArray(R.array.zuoyexianchang_si_video);

				if (isPhoto[position] == 1) {
					name.add("拍照");
				}

				if (isRadio[position] == 1) {
					name.add("录音");
				}

			} else if (Main.pageType == 1) {
				isRadio = c.getResources().getIntArray(R.array.caozuoxianchang_si_radio);
				isPhoto = c.getResources().getIntArray(R.array.caozuoxianchang_si_photo);
				isVideo = c.getResources().getIntArray(R.array.caozuoxianchang_si_video);

				if (isPhoto[position] == 1) {
					name.add("拍照");
				}

				if (isRadio[position] == 1) {
					name.add("录音");
				}

				if (isVideo[position] == 1) {
					name.add("摄像");
				}
			}

			else if (Main.pageType == 2) {
				isRadio = c.getResources().getIntArray(R.array.guzhangjinji_si_radio);
				isPhoto = c.getResources().getIntArray(R.array.guzhangjinji_si_photo);
				isVideo = c.getResources().getIntArray(R.array.guzhangjinji_si_video);

				if (isPhoto[position] == 1) {
					name.add("拍照");
				}

				if (isRadio[position] == 1) {
					name.add("录音");
				}

				if (isVideo[position] == 1) {
					name.add("摄像");
				}
			}

			// 主要就是根据不同的页面和不同的条目，弹出的附件对话框的内容不一样，有的只需要录音，有点只需要摄像。。。

			int size = name.size();
			final String[] array = (String[]) name.toArray(new String[size]);

			AlertDialog.Builder builder = new AlertDialog.Builder(XianChangUpload.this);
			builder.setTitle("选择附件类型").setItems(array, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface arg0, int which) {
					switch (which) {

					case 0:

						startAttachment(array[0]);
						break;

					case 1:

						startAttachment(array[1]);
						break;

					case 2:
						startAttachment(array[2]);

						break;

					default:
						break;
					}

				}
			});
			AlertDialog dialog = builder.create();
			dialog.show();

			break;

		case R.id.shangchuanfujian:
			// 上传mUrl里面的附件，还要区分已经上传了的不能重复上传
			int fileCount = getFileCount();
			if (fileCount < 1) {
				showShortToast("请选择附件");
			} else {
				attachmentUploadRequest();// 上传附件
			}
			break;
		}
	}

	private void attachmentUploadRequest() {
		if (!Utils.isNetworkAvailable(XianChangUpload.this)) {
			Toast.makeText(XianChangUpload.this, "网络不可用", Toast.LENGTH_SHORT).show();
			return;
		}
		String uploadUrl = android.wxapp.service.elec.request.Contants.HFS_URL;
		if (getFileCount() < 1) {
			showLongToast("请选择附件上传");
		} else {

			// 避免重复上传
			mUnUploadUrl.clear();
			// TODO 将mUrl-mUploadList = mUnploadList

			List<String> mUrlPath = new ArrayList<String>();
			List<String> mUrlUploadPath = new ArrayList<String>();
			List<String> mUrlUnUploadPath = new ArrayList<String>();

			// 打印出多余的数据
			for (Map<String, Object> ma : mUrl) {
				if (ma != null) {
					mUrlPath.add((String) ma.get("path"));
				}
			}

			for (Map<String, Object> md : mUploadUrl) {
				mUrlUploadPath.add((String) md.get("path"));
			}

			for (String path : mUrlPath) {
				if (!mUrlUploadPath.contains(path)) {
					mUrlUnUploadPath.add(path);
					Log.e("TAG", "-------------------");
					Log.e("TAG", path);

					for (Map<String, Object> ma : mUrl) {
						if (ma != null) {
							String maPath = (String) ma.get("path");
							if (maPath.equals(path)) {
								mUnUploadUrl.add(ma);
							}
						}
					}
				}
			}

			mUnUploadFileCount = mUnUploadUrl.size();
			// 上传
			for (Map<String, Object> map : mUnUploadUrl) {
				if (map != null) {
					if (map.get("path") != null) {
						Log.e("TAG", "上传的路径 : " + map.get("path"));
						mUploadUrl.add(map);
						new HttpUploadTask(new TextView(this), this)
								.execute((String) map.get("path"), uploadUrl);
					}
				}
			}

		}

	}

	private int getFileCount() {
		int fileCount = 0;
		for (int i = 0; i < mUrl.size(); i++) {
			if (mUrl.get(i) != null) {
				fileCount++;
			}
		}
		return fileCount;
	}

	public void startAttachment(String name) {
		if (name.equals("拍照")) {

			// Utilss.showShortToast(MainActivity.this,"拍照");
			// 拍照
			Intent cameraintent = new Intent();
			// 指定开启系统相机的Action
			cameraintent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			cameraintent.addCategory(Intent.CATEGORY_DEFAULT);

			mImagePath = NewTask.fileFolder + File.separator + getFileDate() + ".jpg";
			// 根据文件地址创建文件
			File imagefile = new File(mImagePath);
			if (imagefile.exists()) {
				imagefile.delete();
			}
			// 把文件地址转换成Uri格式
			Uri imageUri = Uri.fromFile(imagefile);
			// 设置系统相机拍摄照片完成后图片文件的存放地址
			cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(cameraintent, LocalConstant.CAPTURE_IMAGE_REQUEST_CODE);
		} else if (name.equals("录音")) {
			Intent recordIntent = new Intent(XianChangUpload.this, RecordActivity.class);
			startActivityForResult(recordIntent, LocalConstant.CAPTURE_AUDIO_REQUEST_CODE);
		} else if (name.equals("摄像")) {
			Intent intent = new Intent();
			intent.setAction("android.media.action.VIDEO_CAPTURE");
			intent.addCategory("android.intent.category.DEFAULT");

			fileName = getFileDate();
			videopath = NewTask.fileFolder + "/" + fileName + ".mp4";
			File file = new File(videopath);
			if (file.exists()) {
				file.delete();
			}
			Uri uri = Uri.fromFile(file);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			startActivityForResult(intent, LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);
		}
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			System.out.println("是从home调回去的么");
			back();
			break;
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	public void onBackPressed() {
		back();
		// super.onBackPressed();
	}

	private void back() {
		if (isShowDialog() == true) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("附件未上传，是否继续上传");
			builder.setCancelable(false);
			builder.setPositiveButton("是", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			builder.setNegativeButton("否", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					for (int i = 0; i < mUrl.size(); i++) {

						if (mUploadUrl.size() == 0) {
							mUrl.set(i, null);
						}

						boolean flag = false;

						for (int j = 0; j < mUploadUrl.size(); j++) {

							if (mUrl.get(i) != null) {
								boolean isContain = mUrl.get(i).get("path")
										.equals(mUploadUrl.get(j).get("path"));
								if (isContain) {
									flag = true;
								}
							}

						}

						if (flag == false) {
							mUrl.set(i, null);
						}
					}

					Intent data = new Intent();
					data.putExtra("url", (Serializable) mUrl);
					System.out.println("home : mUrl " + mUrl.toString());
					data.putExtra("mediaIndex", mediaIndex);// 把附件的下标值传回去，下次进来的时候，mediaIndex从上次的位置开始自增
					data.putExtra("position", getIntent().getIntExtra("position", -1));
					data.putExtra("from", "XianChangUpload");
					data.putExtra("mUploadUrl", (Serializable) mUploadUrl);
					setResult(LocalConstant.SELECT_ATTACHMENT, data);
					finish();
				}
			});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			Intent data = new Intent();
			data.putExtra("url", (Serializable) mUrl);
			System.out.println("home : mUrl " + mUrl.toString());
			data.putExtra("mediaIndex", mediaIndex);// 把附件的下标值传回去，下次进来的时候，mediaIndex从上次的位置开始自增
			data.putExtra("position", getIntent().getIntExtra("position", -1));
			data.putExtra("from", "XianChangUpload");
			data.putExtra("mUploadUrl", (Serializable) mUploadUrl);
			setResult(LocalConstant.SELECT_ATTACHMENT, data);
			finish();
		}
	}

	public boolean isShowDialog() {

		boolean isShow = false;
		// for (int i = 0; i < mUrl.size(); i++) {
		//
		//
		// isShow = false;
		//
		// if (mUploadUrl.size() == 0 ){
		// if (mUrl.get(i)!=null)
		// isShow = true;
		// break;
		// }

		List<String> mUrlPath = new ArrayList<String>();
		List<String> mUrlUploadPath = new ArrayList<String>();

		// 打印出多余的数据
		for (Map<String, Object> ma : mUrl) {
			if (ma != null) {
				mUrlPath.add((String) ma.get("path"));
			}
		}

		for (Map<String, Object> md : mUploadUrl) {
			if (md != null) {
				mUrlUploadPath.add((String) md.get("path"));
			}
		}

		if (mUrlPath.size() != 0) {
			if (mUrlUploadPath.size() == 0) {
				isShow = true;
			} else {
				for (String path : mUrlPath) {
					if (!mUrlUploadPath.contains(path)) {
						isShow = true;
					}
				}

			}
		}

		// }

		return isShow;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case NewTask.SELECT_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				if (data != null) {
					Uri thumbUri = data.getData();
					selectimagepath = uri2filePath(thumbUri);

					selectThumbnailUri = Utils.getThumbnailDir();
					// 获取缩略图,根据原图创建缩略图, mImagePath是原图的地址
					Utils.getThumbnail(selectimagepath, selectThumbnailUri);
					// Utils.getBitMapFromRes(selectimagepath,
					// selectThumbnailUri);

					File file1 = new File(selectThumbnailUri);

					selectImageName = selectimagepath
							.substring(selectimagepath.lastIndexOf(File.separator) + 1);
					System.out.println("selectImageName: " + selectImageName);

					uri1 = Uri.fromFile(file1);

					mediaID1 = mediaIndex++;

					Intent intent = new Intent(XianChangUpload.this, ShowXianChangAttachment.class);
					Map<String, String> mMap = new HashMap<String, String>();
					mMap.put("type", "selectImage");
					mMap.put("path", selectimagepath);
					mMap.put("from", "XianChangUpload");
					mMap.put("index", mediaID1 + "");// 传递附件的下标

					Map<Integer, Map<String, String>> mContent = new HashMap<Integer, Map<String, String>>();
					mContent.put(0, mMap);
					intent.putExtra("address", (Serializable) mContent);
					startActivityForResult(intent, 321);

					// mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE,
					// selectImageName, selectimagepath));
					// // 存储mediaId与imageOriginPath的映射
					// index_originalPath_Map.put(mediaID1, selectimagepath);
					// loadMedia(imageContainer, mediaID1,
					// getThumbnailFromUri(uri1), uri1,
					// NewTask.TYPE_SELECT_IMAGE);
					// // 存储mediaId与thumbnailUri的映射
					// index_path_Map.put(mediaID1, selectThumbnailUri);

				}
			}
			break;

		case 321:

			if (resultCode == RESULT_OK) {
				if (data != null) {

					Map<String, Object> mMa = (Map<String, Object>) data
							.getSerializableExtra("path");
					System.out.println("传递过来的gps信息： " + mMa.get("gps"));
					String index = (String) mMa.get("index");
					mUrl.add(mMa);// 将信息添加进去

					String fileType = (String) data.getStringExtra("fileType");

					// 选择图片和照相获取到的图片的显示和删除方法是不一样的
					if (fileType.equals("selectImage")) {
						int mediaID1 = Integer.valueOf(index);
						mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE, selectImageName,
								selectimagepath));
						index_originalPath_Map.put(mediaID1, selectimagepath);
						System.out.println("mediaID1 : " + mediaID1);

						loadMedia(imageContainer, mediaID1, getThumbnailFromUri(uri1), uri1,
								NewTask.TYPE_SELECT_IMAGE);
						// 存储mediaId与thumbnailUri的映射
						index_path_Map.put(mediaID1, selectThumbnailUri);
					} else if (fileType.equals("captureImage")) {
						int mediaID2 = Integer.valueOf(index);

						mediaList.add(
								new Media(Utils.MEDIA_TYPE_IMAGE, captureImageName, mImagePath));
						// 存储mediaId与imageOriginPath的映射
						index_originalPath_Map.put(mediaID2, mImagePath);
						System.out.println("mediaID2 : " + mediaID2);
						loadMedia(imageContainer, mediaID2, getThumbnailFromUri(uri2), uri2,
								NewTask.TYPE_IMAGE);
						// 存储mediaId与thumbnailUri的映射
						index_path_Map.put(mediaID2, thumbnailUri);
					} else if (fileType.equals("audio")) {

						int mediaID = Integer.valueOf(index);
						System.out.println("mediaID : " + mediaID);

						mediaList.add(
								new Media(Utils.MEDIA_TYPE_AUDIO, captureAudioName, audiopath));
						index_originalPath_Map.put(mediaID, audiopath);
						loadMedia(imageContainer, mediaID, audioThumbnailBitmap, uri,
								NewTask.TYPE_AUDIO);
					} else if (fileType.equals("video")) {
						int mediaID4 = Integer.valueOf(index);

						System.out.println("mediaID4 : " + mediaID4);

						mediaList.add(new Media(Utils.MEDIA_TYPE_VIDEO, videoName, videopath));
						// 存储文件的路径
						index_originalPath_Map.put(mediaID4, videopath);
						loadMedia(imageContainer, mediaID4, videoThumbnailBitmap, uri4,
								NewTask.TYPE_VIDEO);
					}

				}
			}
			break;

		case LocalConstant.CAPTURE_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				thumbnailUri = Utils.getThumbnailDir();
				// 获取缩略图,根据原图创建缩略图, mImagePath是原图的地址
				Utils.getThumbnail(mImagePath, thumbnailUri);

				// // 根据图片生成bitmap对象
				// Bitmap imageThumbnailBitmap =
				// BitmapFactory.decodeFile(mImagePath);
				File file2 = new File(thumbnailUri);

				captureImageName = mImagePath.substring(mImagePath.lastIndexOf(File.separator) + 1);
				System.out.println("captureImageName: " + captureImageName);

				uri2 = Uri.fromFile(file2);
				mediaID2 = mediaIndex++;

				Intent intent = new Intent(XianChangUpload.this, ShowXianChangAttachment.class);
				Map<String, String> mMap = new HashMap<String, String>();
				mMap.put("type", "captureImage");
				mMap.put("path", mImagePath);
				mMap.put("from", "XianChangUpload");
				mMap.put("index", mediaID2 + "");// 传递附件的下标

				Map<Integer, Map<String, String>> mContent = new HashMap<Integer, Map<String, String>>();
				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable) mContent);
				startActivityForResult(intent, 321);

				// mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE,
				// captureImageName, mImagePath));
				// // 存储mediaId与imageOriginPath的映射
				// index_originalPath_Map.put(mediaID2, mImagePath);
				//
				// loadMedia(imageContainer, mediaID2,
				// getThumbnailFromUri(uri2),
				// uri2, NewTask.TYPE_IMAGE);
				// // 存储mediaId与thumbnailUri的映射
				// index_path_Map.put(mediaID2, thumbnailUri);
			}

			break;

		case LocalConstant.CAPTURE_AUDIO_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				System.out.println("声音返回成功");
				audioThumbnailBitmap = BitmapFactory.decodeResource(getResources(),
						R.drawable.record);
				audioFileName = getFileDate();
				audiopath = data.getStringExtra("path");

				System.out.println("audiopath: " + audiopath);
				File file = new File(audiopath);

				captureAudioName = audiopath.substring(audiopath.lastIndexOf(File.separator) + 1);
				System.out.println("captureAudioName: " + captureAudioName);

				mediaID = mediaIndex++;

				Intent intent = new Intent(XianChangUpload.this, ShowXianChangAttachment.class);
				Map<String, String> mMap = new HashMap<String, String>();
				mMap.put("type", "audio");
				mMap.put("path", audiopath);
				mMap.put("from", "XianChangUpload");
				mMap.put("index", mediaID + "");// 传递附件的下标

				Map<Integer, Map<String, String>> mContent = new HashMap<Integer, Map<String, String>>();
				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable) mContent);
				startActivityForResult(intent, 321);

				uri = Uri.fromFile(file);

				// mediaList.add(new Media(Utils.MEDIA_TYPE_AUDIO,
				// captureAudioName, audiopath));
				// index_originalPath_Map.put(mediaID, audiopath);
				// loadMedia(imageContainer, mediaID, audioThumbnailBitmap, uri,
				// NewTask.TYPE_AUDIO);
			}
			break;

		case LocalConstant.CAPTURE_VIDEO_REQUEST_CODE:

			if (resultCode == RESULT_OK) {
				Log.i("TAG", "拍摄完成，resultCode=" + requestCode);

				File file = new File(videopath);

				videoThumbnailBitmap = getVideoThumbnail(videopath, 400, 400,
						MediaStore.Images.Thumbnails.MINI_KIND);

				uri4 = Uri.fromFile(file);
				mediaID4 = mediaIndex++;

				videoName = videopath.substring(videopath.lastIndexOf(File.separator) + 1);
				System.out.println("videoName: " + videoName);

				Intent intent = new Intent(XianChangUpload.this, ShowXianChangAttachment.class);
				Map<String, String> mMap = new HashMap<String, String>();
				mMap.put("type", "video");
				mMap.put("path", videopath);
				mMap.put("from", "XianChangUpload");
				mMap.put("index", mediaID4 + "");// 传递附件的下标

				Map<Integer, Map<String, String>> mContent = new HashMap<Integer, Map<String, String>>();
				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable) mContent);
				startActivityForResult(intent, 321);

				// mediaList.add(new Media(Utils.MEDIA_TYPE_VIDEO, videoName,
				// videopath));
				// // 存储文件的路径
				// index_originalPath_Map.put(mediaID, videopath);
				// loadMedia(imageContainer, mediaID, videoThumbnailBitmap,
				// uri4,
				// NewTask.TYPE_VIDEO);
			}

			break;

		default:
			break;
		}
	}

	private String uri2filePath(Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, proj, null, null, null);
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(index);
		return path;
	}

	public void loadAttachment() {

		if (getIntent() != null) {

			mUrl = (List<Map<String, Object>>) getIntent().getSerializableExtra("url");

			for (Map<String, Object> map : mUrl) {

				// for (String url : map.keySet()) {
				if (map != null) {

					if (map.get("path") != null) {

						System.out.println("XianChangUpload url : " + map.get("path"));

						mFilePath = (String) map.get("path");

						File mfile = new File(mFilePath);
						if (mfile.exists()) {// 针对的是从网络上下载附件的时候，没有下载完成，就点击进入，会显示不了附件
							// mFilePath = (String)
							// getIntent().getCharSequenceExtra("path");
							mFileName = mFilePath
									.substring(mFilePath.lastIndexOf(File.separator) + 1);

							if (mFileName.contains(".jpg") || mFileName.contains(".png")
									|| mFileName.contains(".jpeg")) {// 传过来的是图片

								String thumbnailUri = Utils.getThumbnailDir();
								// 获取缩略图,根据原图创建缩略图, mImagePath是原图的地址
								Utils.getThumbnail(mFilePath, thumbnailUri);
								File file2 = new File(thumbnailUri);
								Uri uri2 = Uri.fromFile(file2);

								// 从附件中获取它的下标
								int mediaID2 = Integer.valueOf((String) map.get("index"));// 第一次进来

								// 存储mediaId与imageOriginPath的映射
								index_originalPath_Map.put(mediaID2, mFilePath);
								loadMedia(imageContainer, mediaID2, getThumbnailFromUri(uri2), uri2,
										NewTask.TYPE_IMAGE);

								// 存储mediaId与thumbnailUri的映射
								index_path_Map.put(mediaID2, thumbnailUri);
							} else if (mFileName.contains(".mp3") || mFileName.contains(".amr")) {// 传递过来的是音频

								// 根据图片生成bitmap对象
								Bitmap AudioThumbnailBitmap = BitmapFactory
										.decodeResource(getResources(), R.drawable.record);

								System.out.println("audiopath: " + mFilePath);
								File file = new File(mFilePath);

								Uri uri = Uri.fromFile(file);
								int mediaID = Integer.valueOf((String) map.get("index"));
								;

								// if (isOk2){
								// mediaID =
								// } else {
								// mediaID = mediaIndex++;;//第一次进来
								// }

								index_originalPath_Map.put(mediaID, mFilePath);
								loadMedia(imageContainer, mediaID, AudioThumbnailBitmap, uri,
										NewTask.TYPE_AUDIO);
							} else if (mFileName.contains(".mp4")) {
								File file = new File(mFilePath);

								Bitmap videoThumbnailBitmap = getVideoThumbnail(mFilePath, 400, 400,
										MediaStore.Images.Thumbnails.MINI_KIND);

								// 显示所录制视频
								Uri uri = Uri.fromFile(file);
								int mediaID = Integer.valueOf((String) map.get("index"));
								// if (isOk2){
								// mediaID ;
								// } else {
								// mediaID = mediaIndex++;;//第一次进来
								// }

								mediaList.add(
										new Media(Utils.MEDIA_TYPE_VIDEO, mFileName, mFilePath));
								// 存储文件的路径
								index_originalPath_Map.put(mediaID, mFilePath);
								loadMedia(imageContainer, mediaID, videoThumbnailBitmap, uri,
										NewTask.TYPE_VIDEO);
							}

						} else {
							Toast.makeText(this, "附件正在下载", Toast.LENGTH_SHORT).show();
						}

					}
				}

			}
		}

	}

	public void loadMedia(FixedGridLayout viewContainer, int mediaId, Bitmap thumbnail,
			final Uri uri, final int MediaType) {

		// WeiHao 如果附件展示布局不可见，置未可见
		if (showAttachLayout.getVisibility() == View.GONE) {
			showAttachLayout.setVisibility(View.VISIBLE);
		}
		// 将图片缩略图添加到缩略图列表，便于新建完成后回收
		// bitmapList.add(thumbnail);
		final ImageView imageView = CreateImgView(this, thumbnail, IMG_WIDTH, IMG_HEIGHT);
		setImageviewListener(uri, imageView, MediaType, mediaId);

		ImageButton deleteBtn = new ImageButton(this);
		deleteBtn.setBackgroundResource(R.drawable.media_delete);

		final RelativeLayout r1 = WrapImgView(MediaType, imageView, deleteBtn);
		r1.setId(mediaId);
		Log.e("TAG", "r1.setMediaId : " + mediaId);

		deleteBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 显示删除对话框
				showDeleteMediaDialog(r1, MediaType);
			}
		});

		// 将图片添加入图片列表
		viewContainer.addView(r1);
		if (isAdmin())
			deleteBtn.setVisibility(View.GONE);

	}

	/**
	 * 缩略图控件
	 * 
	 * @param imgview
	 * @param btn
	 * @author WEIHAO
	 * @since 2014-5-17
	 * @return
	 */
	public RelativeLayout WrapImgView(int mediaType, ImageView imgview, ImageButton btn) {
		RelativeLayout rl = new RelativeLayout(this);
		rl.setLayoutParams(new FixedGridLayout.LayoutParams(IMG_WIDTH, IMG_HEIGHT));
		rl.setPadding(2, 2, 2, 2);
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		// imgview 位于父 View 的顶部，在父 View 中居左
		lp1.topMargin = 15;
		lp1.rightMargin = 15;
		rl.addView(imgview, lp1);

		RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(30, 30);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		// btn1 位于父 View 的顶部，在父 View 中水平居右
		rl.addView(btn, lp2);

		// WeiHao 媒体类型判断，显示水印
		// ...
		// Bitmap wm;
		// ImageView waterMark;
		TextView waterMark = new TextView(this);
		waterMark.setTextSize(12);
		waterMark.setBackgroundColor(getResources().getColor(R.color.darkgrey));
		waterMark.setTextColor(getResources().getColor(R.color.white));
		RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		// waterMark位于父View的左下
		lp3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp3.leftMargin = 10;
		lp3.bottomMargin = 25; // 底部间距好像不起作用，暂时未找到原因

		if (mediaType == NewTask.TYPE_IMAGE) {
			// wm = BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.image_preview);
			// waterMark = CreateImgView(this, wm, 30, 30);

			waterMark.setText("图片");

			rl.addView(waterMark, lp3);
		} else if (mediaType == NewTask.TYPE_VIDEO) {
			// wm = BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.video_play);
			// waterMark = CreateImgView(this, wm, 50, 50);

			waterMark.setText("视频");

			rl.addView(waterMark, lp3);
		} else if (mediaType == NewTask.TYPE_AUDIO) {

			waterMark.setText("音频");

			rl.addView(waterMark, lp3);
		} else if (mediaType == NewTask.TYPE_SELECT_IMAGE) {

			waterMark.setText("图片");
			rl.addView(waterMark, lp3);
		}
		return rl;
	}

	/**
	 * 创建ImageView
	 * 
	 * @param context
	 * @param id
	 * @param pic
	 * @param width
	 * @param height
	 * @return
	 */
	public ImageView CreateImgView(Context context, Bitmap pic, int width, int height) {
		// 加载图片的ImageView
		ImageView imageView = new RoundAngleImageView(context);
		// 将图片缩略图加载到ImageView
		imageView.setImageBitmap(pic);
		// // 为图片设置编号
		// imageView.setId(mediaId);
		// 设置图片显示格式
		FixedGridLayout.LayoutParams params = new FixedGridLayout.LayoutParams(width, height);
		imageView.setLayoutParams(params);
		imageView.setScaleType(ScaleType.CENTER_CROP);
		return imageView;
	}

	/**
	 * 设置相关的监听事件
	 * 
	 * @param uri
	 * @param imageView
	 */
	public void setImageviewListener(final Uri uri, final ImageView imageView, final int MediaType,
			final int MediaId) {
		// 为图片设置触摸事件
		imageView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// 按下时变半透明
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					imageView.setAlpha(50);
				// 抬起时恢复透明度
				else if (event.getAction() == MotionEvent.ACTION_UP)
					imageView.setAlpha(255);
				return false;
			}
		});
		// 为图片设置单击事件
		imageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 单击事件触发后，单击事件失效
				// imageView.setEnabled(false);

				switch (MediaType) {
				case NewTask.TYPE_IMAGE:
					// 点击显示大图
					showImageDialog(imageView, uri);
					break;

				case NewTask.TYPE_SELECT_IMAGE:
					// 点击显示大图
					showImageDialog(imageView, uri);
					break;
				case NewTask.TYPE_VIDEO:
					// 点击播放视频
					Intent intent = new Intent(XianChangUpload.this, PlayVideo.class);
					intent.putExtra("path", index_originalPath_Map.get(MediaId));
					startActivity(intent);
					break;

				case NewTask.TYPE_AUDIO:
					// 点击音频
					Log.e("TAG", "播放音频");

					// 根据mediaId来获取被点击音频的路径
					File file = new File(index_originalPath_Map.get(MediaId));
					Uri uri = Uri.fromFile(file);
					Intent intent1 = new Intent(Intent.ACTION_MAIN);
					intent1.setAction(Intent.ACTION_DEFAULT);
					intent1.setDataAndType(uri, "audio/*");
					startActivity(intent1);

					break;
				default:
					break;
				}

			}
		});

	}

	/** 显示大图对话框 */
	private void showImageDialog(final ImageView imageView, final Uri uri) {
		// 获取对话框布局并实例化
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.image_dialog, null);
		// 构造对话框
		imageDialog = new Dialog(this, R.style.imageDialog);
		imageDialog.setContentView(view);
		// 添加图片
		ImageView dialogImageView = (ImageView) view.findViewById(R.id.imageImageView);
		// 获取图片
		final Bitmap pic = getBitmapFromUri(uri);
		// 将图片缩略图加载到ImageView
		dialogImageView.setImageBitmap(pic);
		// 为图片设置单击事件
		dialogImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				imageDialog.dismiss();
				imageDialog = null;
				// 缩略图单击事件恢复
				imageView.setEnabled(true);
				// 回收图片
				pic.recycle();
			}
		});
		// 显示对话框
		imageDialog.show();
	}

	private void showDeleteMediaDialog(final RelativeLayout rl, final int MediaType) {
		String text = "";

		if (MediaType == NewTask.TYPE_AUDIO) {
			text = "音频";
		} else if (MediaType == NewTask.TYPE_VIDEO) {
			text = "视频";
		} else if (MediaType == NewTask.TYPE_IMAGE) {
			text = "图片";
		} else if (MediaType == NewTask.TYPE_SELECT_IMAGE) {
			text = "图片";
		}

		AlertDialog.Builder builder = new Builder(this);
		// 设置标题
		builder.setTitle("提示");
		// 设置显示内容
		builder.setMessage("确定删除" + text + "？");
		// 设置确定按钮
		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				// 打开图库选择的图片不能被删除，其他的要被删除

				if (MediaType == NewTask.TYPE_AUDIO) {
					// 将图片从图片列表中删除
					imageContainer.removeView(rl);

					// 将手机中的媒体文件删除
					Integer index = rl.getId();

					System.out.println("应该移除的下标index : " + index);
					System.out.println("从附件列表中移除前： " + mUrl.toString());
					// mUrl.remove(index.intValue());//附件列表中移除，下次从List中进入的时候就不会显示了
					mUrl.set(index.intValue(), null);
					System.out.println("从附件列表中移除后： " + mUrl.toString());

					// 删除原媒体文件（新）
					if (index_originalPath_Map.containsKey(index)) {
						String path = index_originalPath_Map.get(index);
						index_originalPath_Map.remove(index);
						Utils.deleteMedia(path);
					}
				} else if (MediaType == NewTask.TYPE_VIDEO) {
					// 将视频从视频列表中删除
					imageContainer.removeView(rl);

					// 将手机中的媒体文件删除
					Integer index = rl.getId();

					System.out.println("应该移除的下标index : " + index);
					System.out.println("从附件列表中移除前： " + mUrl.toString());
					mUrl.set(index.intValue(), null);
					System.out.println("从附件列表中移除后： " + mUrl.toString());

					// 删除原媒体文件（新）
					if (index_originalPath_Map.containsKey(index)) {
						String path = index_originalPath_Map.get(index);
						index_originalPath_Map.remove(index);
						Utils.deleteMedia(path);
					}
				} else if (MediaType == NewTask.TYPE_IMAGE) {
					// 将图片从图片列表中删除
					imageContainer.removeView(rl);
					// 将手机中的媒体文件删除
					Integer index = rl.getId();

					System.out.println("应该移除的下标index : " + index);
					System.out.println("从附件列表中移除前： " + mUrl.toString());
					mUrl.set(index.intValue(), null);

					System.out.println("从附件列表中移除后： " + mUrl.toString());

					// 删除原媒体文件（新）
					// if (index_originalPath_Map.containsKey(index)) {
					// String path = index_originalPath_Map.get(index);
					// // System.out.println(path);
					// index_originalPath_Map.remove(index);
					// Utils.deleteMedia(path);
					// }

					// 删除缩略图
					if (index_path_Map.containsKey(index)) {
						String path = index_path_Map.get(index);
						index_path_Map.remove(index);
						Utils.deleteMedia(path);
					}
				} else if (MediaType == NewTask.TYPE_SELECT_IMAGE) {
					// 将图片从图片列表中删除
					imageContainer.removeView(rl);

					// System.out.println("删除缩略图");
					// 将手机中的媒体文件删除
					Integer index = rl.getId();

					System.out.println("应该移除的下标index : " + index);
					System.out.println("从附件列表中移除前： " + mUrl.toString());
					mUrl.set(index.intValue(), null);
					System.out.println("从附件列表中移除后： " + mUrl.toString());

					// 删除缩略图
					if (index_path_Map.containsKey(index)) {
						String path = index_path_Map.get(index);
						index_path_Map.remove(index);
						Utils.deleteMedia(path);
					}
				}

				// 将手机中的媒体文件删除
				// Integer index = rl.getId();

				// WeiHao 媒体文件编号缩减，并判断是否已删空附件，如果已无附件，隐藏该控件
				// if (--mediaIndex == 0) {
				// showAttachLayout.setVisibility(View.GONE);
				// }

				// 对话框关闭
				dialog.dismiss();
			}
		});

		// 设置取消按钮
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// 对话框关闭
				dialog.dismiss();
				// 缩略图单击事件恢复
				rl.setEnabled(true);
			}
		});
		// 显示对话框
		builder.create().show();
	}

	public Bitmap getBitmapFromUri(final Uri uri) {
		ContentResolver cr = this.getContentResolver();
		// 图片输入流
		InputStream input = null;
		try {
			input = cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// 获取图片缩略图并返回
		return BitmapFactory.decodeStream(input, null, null);
	}

	public Bitmap getThumbnailFromUri(final Uri uri) {
		ContentResolver cr = this.getContentResolver();
		// 图片输入流
		InputStream input = null;
		try {
			input = cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// 图片缩略选项
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;
		// 获取图片缩略图并返回
		return BitmapFactory.decodeStream(input, null, opts);
	}

	// 媒体类
	private class Media {
		private int mediaType;
		private String mediaName;
		private String mediaUrl;

		public Media(int mediaType, String mediaName, String mediaUrl) {
			super();
			this.mediaType = mediaType;
			this.mediaName = mediaName;
			this.mediaUrl = mediaUrl;
		}

		public int getMediaType() {
			return mediaType;
		}

		public void setMediaType(int mediaType) {
			this.mediaType = mediaType;
		}

		public String getMediaName() {
			return mediaName;
		}

		public void setMediaName(String mediaName) {
			this.mediaName = mediaName;
		}

		public String getMediaUrl() {
			return mediaUrl;
		}

		public void setMediaUrl(String mediaUrl) {
			this.mediaUrl = mediaUrl;
		}

	}

	protected String getFileDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
		Date date = new Date(System.currentTimeMillis());
		String file = format.format(date);
		return file;
	}

	private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);

		if (bitmap == null) {
			return null;
		}

		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	Handler handler;

	private int mUnUploadFileCount = 0;// 记录为上传的附件数目

	private int position;

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.FILE_UPLOAD_SUCCESS:// 当所有的附件都上传完了之后finish当前页面

					mUnUploadFileCount--;
					Log.i("TAG", "count : " + mUnUploadFileCount);
					if (mUnUploadFileCount == 0) {

						Toast.makeText(XianChangUpload.this, "上传成功", Toast.LENGTH_SHORT).show();
						// 请求http接口
						List<TaskAttachment> attachment = new ArrayList<TaskAttachment>();
						// for (int i = 0; i < mUnUploadUrl.size(); i++) {
						StringBuilder standard = new StringBuilder("standard");
						// 作业现场
						if (enterType == 1) {
							switch (position) {
							// 工作票
							case 0:
								standard.append("01");
								break;
							case 1:
								standard.append("02");
								break;
							case 2:
								standard.append("03");
								break;
							case 3:
								standard.append("04");
								break;
							case 4:
								standard.append("05");
								break;
							case 5:
								standard.append("06");
								break;
							}
						}
						// 操作现场
						else if (enterType == 2) {
							switch (position) {
							case 0:
								standard.append("07");
								break;
							case 1:
								standard.append("08");
								break;
							case 2:
								standard.append("09");
								break;
							case 3:
								standard.append("10");
								break;
							case 4:
								standard.append("11");
								break;
							}
						}
						// 故障抢修
						else if (enterType == 3) {
							switch (position) {
							case 0:
								standard.append("01");
								break;
							case 1:
								standard.append("02");
								break;
							case 2:
								standard.append("03");
								break;
							case 3:
								standard.append("04");
								break;
							case 4:
								standard.append("05");
								break;
							case 5:
								standard.append("06");
								break;
							}
						}

						List<Attachments> sublist = new ArrayList<Attachments>();
						String server = android.wxapp.service.elec.request.Contants.HFS_URL;
						for (int j = 0; j < mUnUploadUrl.size(); j++) {
							Map<String, Object> attItem = mUnUploadUrl.get(j);

							if (attItem == null) {
								return;
							}

							String filePath = (String) attItem.get("path");
							String type = Utils.judgeFileLeixin(filePath);
							if (type != null) {

								MyGPS myGPS = (MyGPS) attItem.get("gps");
								// 参数修改
								GPS gps = new GPS(getUserId(),
										Utils.formatDateMs(System.currentTimeMillis()),
										myGPS.getLongitude() + "", myGPS.getLatitude() + "", "",
										myGPS.getRadius() + "", myGPS.getAltitude() + "",
										myGPS.getSpeed() + "",
										Utils.formatDateMs(System.currentTimeMillis()),
										myGPS.getCoorType(), "");

								// String md5 = DigestUtils
								// .md5Hex(new FileInputStream(new
								// File(filePath)));
								String md5 = Utils.getFileMD5(new File(filePath));
								Attachments att = new Attachments(type,
										server + File.separator + path2FileName(filePath),
										(String) attItem.get("time"), gps, md5);
								sublist.add(att);

							}
						}
						TaskAttachment item = new TaskAttachment(standard.toString(), sublist);
						attachment.add(item);

						requestManager.uploadTaskAttachment(XianChangUpload.this, tid,
								enterType + "", attachment);

					}

					break;
				case Constant.FILE_UPLOAD_FAIL:
					Toast.makeText(XianChangUpload.this, "上传失败", Toast.LENGTH_SHORT).show();
					break;

				case Constants.UPLOAD_TASK_ATT_SUCCESS:
					break;
				case Constants.END_TASK_SUCCESS:
					showLongToast("任务已结束");
					break;

				case Constants.UPLOAD_TASK_ATT_SAVE_FAIL:
				case Constants.UPLOAD_TASK_ATT_FAIL:
				case Constants.END_TASK_FAIL:
				case Constants.END_TASK_SAVE_FAIL:
					if (msg.obj != null) {
						showAlterDialog("上传失败", ((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "确定", null);
					} else {
						showAlterDialog("上传失败", "请检查是否与服务器连接正常", R.drawable.login_error_icon, "确定",
								null);
					}
					break;
				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_FAIL,
				"XianChangUpload");
		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_SUCCESS,
				"XianChangUpload");

		MessageHandlerManager.getInstance().register(handler, Constants.UPLOAD_TASK_ATT_SUCCESS,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.UPLOAD_TASK_ATT_SAVE_FAIL,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.UPLOAD_TASK_ATT_FAIL,
				UploadTaskAttachmentResponse.class.getName());

		MessageHandlerManager.getInstance().register(handler, Constants.END_TASK_SUCCESS,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.END_TASK_FAIL,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.END_TASK_SAVE_FAIL,
				StartTaskResponse.class.getName());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_FAIL,
				"XianChangUpload");
		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_SUCCESS,
				"XianChangUpload");

		MessageHandlerManager.getInstance().unregister(Constants.UPLOAD_TASK_ATT_SUCCESS,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.UPLOAD_TASK_ATT_SAVE_FAIL,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.UPLOAD_TASK_ATT_FAIL,
				UploadTaskAttachmentResponse.class.getName());

		MessageHandlerManager.getInstance().unregister(Constants.END_TASK_SUCCESS,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.END_TASK_FAIL,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.END_TASK_SAVE_FAIL,
				StartTaskResponse.class.getName());
	}

	private String path2FileName(String path) {
		return path.substring(path.lastIndexOf(File.separator) + 1);
	}

}