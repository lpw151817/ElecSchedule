package nercms.schedule.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.actionbarsherlock.view.MenuItem;
import com.imooc.treeview.utils.Node;

import nercms.schedule.R;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.FixedGridLayout;
import nercms.schedule.view.PlayVideo;
import nercms.schedule.view.RecordButton;
import nercms.schedule.view.RoundAngleImageView;
import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.dao.TaskInsDao;
import android.wxapp.service.elec.model.CreateInsResponse;
import android.wxapp.service.elec.model.bean.Attachments;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.Contants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.HttpUploadTask;

/**
 * @author chen
 *
 */
public class NewTask extends BaseActivity {

	String tid;
	TaskInsDao dao;

	public static final int TYPE_IMAGE = 1;
	public static final int TYPE_VIDEO = 2;
	public static final int TYPE_AUDIO = 3;
	public static final int TYPE_SELECT_IMAGE = 4;
	public static final int TYPE_PEOPLE_SELECT = 5;
	RecordButton mRecord;
	ImageButton mVideo;

	// video
	public static String fileFolder = Environment.getExternalStorageDirectory().getPath()
			+ "/TestRecord";

	private String fileName;
	private String audioFileName;

	private String videopath;

	private String audiopath;

	private String mImagePath;

	// 附件缩略图展示Layout
	private LinearLayout showAttachLayout;
	// 图片容器
	private FixedGridLayout imageContainer;

	// 图片显示的宽高
	private static int IMG_WIDTH = 130;
	private static int IMG_HEIGHT = 130;
	// 每行显示缩略图数目
	private final int NUMPERROW = 3;
	// 附件编号
	private int mediaIndex = 0;

	// 返回码
	public static final int CAPTURE_AUDIO_REQUEST_CODE = 101;
	public static final int CAPTURE_VIDEO_REQUEST_CODE = 102;
	public static final int SELECT_IMAGE_REQUEST_CODE = 103;
	public static final int CAPTURE_IMAGE_REQUEST_CODE = 104;

	WebRequestManager manager;

	// 保存mediaIndex与media缩略图地址的映射,
	// 存放每一个媒体文件的地址
	private HashMap<Integer, String> index_originalPath_Map = new HashMap<Integer, String>();

	// 保存mediaIndex与media缩略图地址的映射
	private HashMap<Integer, String> index_path_Map = new HashMap<Integer, String>();

	// 附件存储路径
	private String imagePath;

	// 显示大图对话框
	private Dialog imageDialog;

	// 发送按钮
	private Button mSend;

	// 附件集
	private ArrayList<NewTask.Media> mediaList = new ArrayList<NewTask.Media>();

	// 上传进度
	private TextView mProgress;

	private Handler handler;

	// 上传的附件名称列表
	private List<String> fileNameList;

	private int count;// 上传附件的个数

	// private ImageButton mReceiver;//接收人按钮
	private EditText mReceiverInput;// 接受人输入框
	private EditText mContentInput;// 主要接受内容

	List<Node> receiverList = new ArrayList<Node>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_task);

		iniActionBar(true, null, null);

		dao = new TaskInsDao(this);

		manager = new WebRequestManager(AppApplication.getInstance(), this);

		WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();

		IMG_WIDTH = (width - 65) / 3;
		IMG_HEIGHT = IMG_WIDTH;

		// mRecord = (RecordButton) findViewById(R.id.btn_record);
		mVideo = (ImageButton) findViewById(R.id.fujian_bt);
		// mReceiver = (ImageButton) findViewById(R.id.jieshouren_bt);
		mContentInput = (EditText) findViewById(R.id.zhuyaogongzuoneirong);
		mReceiverInput = (EditText) findViewById(R.id.jieshouren_et);

		// 需要从上一个界面中传入
		tid = getIntent().getExtras().getString("tid");
		if (!TextUtils.isEmpty(tid)) {
			tb_task_instructions data = dao.getTaskIns(tid);
			mContentInput.setText(data.getContent());
			Utils.setEditTextUnEditable(mContentInput);
			mReceiverInput.setText(new OrgDao(this).getPerson(data.getSend_id()).getName());
			Utils.setEditTextUnEditable(mReceiverInput);
			// TODO 旧任务显示附件信息

		}
		// else {
		// mReceiverInput.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// if (event.getAction() == MotionEvent.ACTION_UP) {
		// Intent intent = new Intent(NewTask.this, ContactSelect.class);
		// intent.putExtra("entrance_flag", 2);
		// intent.putExtra("pod", (Serializable) receiverList);
		// NewTask.this.startActivityForResult(intent, TYPE_PEOPLE_SELECT);
		// }
		// return true;
		// }
		// });
		// }

		// 附件缩略图展示Layout，默认不可见
		showAttachLayout = (LinearLayout) findViewById(R.id.showAttathLayout);
		showAttachLayout.setVisibility(View.GONE);

		// 图片容器初始化
		imageContainer = (FixedGridLayout) findViewById(R.id.attachContainer);
		imageContainer.setCellWidth(IMG_WIDTH);
		imageContainer.setCellHeight(IMG_HEIGHT);
		imageContainer.setNumPerRow(NUMPERROW);

		mVideo.setOnClickListener(click);

		System.out.println("width:" + width);
		System.out.println("height:" + height);

		mSend = (Button) findViewById(R.id.fasong);// 点击发送按钮上传附件
		mProgress = (TextView) findViewById(R.id.upload_status_textview);
		mSend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {

				boolean isReceiver = TextUtils.isEmpty(mReceiverInput.getText());
				boolean isInput = TextUtils.isEmpty(mContentInput.getText());
				boolean iscount = (mediaIndex == 0);
				boolean istrue = !(TextUtils.isEmpty(mReceiverInput.getText())
						&& TextUtils.isEmpty(mContentInput.getText()) && (count == 0));

				System.out.println(isReceiver + " " + isInput + " " + iscount + " " + istrue
						+ "mediaIndex : " + mediaIndex);

				// 接收人，具体内容和附件的数目都不能为空
				if ((!(TextUtils.isEmpty(mReceiverInput.getText()))
						&& (!(TextUtils.isEmpty(mContentInput.getText()))) && (mediaIndex != 0))) {
					attachmentUploadRequest();
				} else {
					Toast.makeText(NewTask.this, "内容不能为空", Toast.LENGTH_SHORT).show();
				}

				// attachmentUploadRequest();
			}
		});

		// //点击接受人的点击事件
		// mReceiver.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		//
		// }
		// });

		initHandler();

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

	/**
	 * 附件上传
	 */
	private void attachmentUploadRequest() {
		if (!isNetworkAvailable(NewTask.this)) {
			Toast.makeText(NewTask.this, "网络不可用", Toast.LENGTH_SHORT).show();
			return;
		}

		fileNameList = new ArrayList<String>();
		String fileName = null;// 上传的文件名

		count = mediaIndex;

		NewTask.Media media;
		for (int i = 0; i < mediaIndex; i++) {
			media = mediaList.get(i);

			String mediaPath = media.getMediaUrl();// 媒体文件的本地路径，用户附件上传时
			String uploadUrl = Contants.HFS_URL;

			fileName = path2FileName(mediaPath);
			fileNameList.add(fileName);

			System.out.println("mediaPath : " + mediaPath);
			// 开启上传
			new HttpUploadTask(mProgress, this).execute(mediaPath, uploadUrl);
		}
		//
		// System.out.println("filename :"+fileNameList.toString());
		//

	}

	private String path2FileName(String path) {
		return path.substring(path.lastIndexOf(File.separator) + 1);
	}

	// 2014-5-21 WeiHao
	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.FILE_UPLOAD_SUCCESS:// 上传成功

					// count--;
					// Log.i("TAG", "count : " + count);
					// if (count == 0){
					//
					// Intent intent = new Intent(NewTask.this,
					// ShowDownLoad.class);
					// intent.putExtra("name", (Serializable) fileNameList);
					// startActivity(intent);
					// finish();
					// }

					// http接口请求
					String server = android.wxapp.service.elec.request.Contants.HFS_URL;
					List<Attachments> attachments = new ArrayList<Attachments>();
					for (Media item : mediaList) {
						String md5 = Utils.getFileMD5(new File(item.getMediaUrl()));
						attachments.add(new Attachments(item.getMediaType() + "",
								server + File.separator + path2FileName(item.getMediaUrl()),
								System.currentTimeMillis() + "", null, md5));

					}
					manager.createInsRequest(NewTask.this, receiverList, tid,
							mContentInput.getText().toString(), attachments);
					break;
				case Constant.FILE_UPLOAD_FAIL:
					showLongToast("附件上传失败");
					break;
				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_FAIL, "NewTask");
		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_SUCCESS,
				"NewTask");

		MessageHandlerManager.getInstance().register(handler, Constants.CREATE_INS_SUCCESS,
				CreateInsResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.CREATE_INS_SAVE_FAIL,
				CreateInsResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.CREATE_INS_FAIL,
				CreateInsResponse.class.getName());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_FAIL, "NewTask");
		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_SUCCESS, "NewTask");

		MessageHandlerManager.getInstance().unregister(Constants.CREATE_INS_SUCCESS,
				CreateInsResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.CREATE_INS_SAVE_FAIL,
				CreateInsResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.CREATE_INS_FAIL,
				CreateInsResponse.class.getName());

	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private View.OnClickListener click = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			initAttachPickBtn();
		}
	};

	/**
	 * 附件上传按钮响应事件方法
	 */
	private void initAttachPickBtn() {
		AlertDialog.Builder builder = new AlertDialog.Builder(NewTask.this);
		builder.setTitle("选择附件类型").setItems(new String[] { "图库", "拍照", "摄像", "录音" },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int which) {
						switch (which) {
						case 0:
							// Utilss.showShortToast(MainActivity.this, "图库");
							Intent getAlbum = new Intent(Intent.ACTION_PICK,
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							// 开启Pictures画面Type设定为image
							getAlbum.setType("image/*");
							// getAlbum.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(getAlbum, SELECT_IMAGE_REQUEST_CODE);
							break;

						case 1:
							// Utilss.showShortToast(MainActivity.this,"拍照");
							// 拍照
							Intent cameraintent = new Intent();
							// 指定开启系统相机的Action
							cameraintent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
							cameraintent.addCategory(Intent.CATEGORY_DEFAULT);

							mImagePath = fileFolder + File.separator + getFileDate() + ".jpg";
							// 根据文件地址创建文件
							File imagefile = new File(mImagePath);
							if (imagefile.exists()) {
								imagefile.delete();
							}
							// 把文件地址转换成Uri格式
							Uri imageUri = Uri.fromFile(imagefile);
							// 设置系统相机拍摄照片完成后图片文件的存放地址
							cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
							startActivityForResult(cameraintent, CAPTURE_IMAGE_REQUEST_CODE);
							break;

						case 2:
							Intent intent = new Intent();
							intent.setAction("android.media.action.VIDEO_CAPTURE");
							intent.addCategory("android.intent.category.DEFAULT");

							fileName = getFileDate();
							videopath = fileFolder + "/" + fileName + ".mp4";
							File file = new File(videopath);
							if (file.exists()) {
								file.delete();
							}
							Uri uri = Uri.fromFile(file);
							intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
							startActivityForResult(intent, CAPTURE_VIDEO_REQUEST_CODE);

							break;

						case 3:
							Intent recordIntent = new Intent(NewTask.this, RecordActivity.class);
							startActivityForResult(recordIntent, CAPTURE_AUDIO_REQUEST_CODE);

							break;

						default:
							break;
						}

					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// 媒体文件地址
		String originalUri;

		switch (requestCode) {
		case CAPTURE_VIDEO_REQUEST_CODE:

			if (resultCode == RESULT_OK) {
				Log.i("TAG", "拍摄完成，resultCode=" + requestCode);

				// MICRO_KIND和MINI_KIND 分别为微型和迷你两种缩略模式，前者的分辨率更低
				// videoThumbnail.setImageBitmap(getVideoThumbnail(path, 400,
				// 400,
				// MediaStore.Images.Thumbnails.MINI_KIND));

				// String videoName =
				// videopath.substring(videopath.lastIndexOf("/")
				// + 1);

				File file = new File(videopath);

				Bitmap videoThumbnailBitmap = getVideoThumbnail(videopath, 400, 400,
						MediaStore.Images.Thumbnails.MINI_KIND);

				// 显示所录制视频
				Uri uri = Uri.fromFile(file);
				int mediaID = mediaIndex++;

				String videoName = videopath.substring(videopath.lastIndexOf(File.separator) + 1);
				System.out.println("videoName: " + videoName);
				mediaList.add(new Media(Utils.MEDIA_TYPE_VIDEO, videoName, videopath));
				// 存储文件的路径
				index_originalPath_Map.put(mediaID, videopath);
				loadMedia(imageContainer, mediaID, videoThumbnailBitmap, uri, TYPE_VIDEO);
			}

			break;

		case SELECT_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				if (data != null) {
					Uri thumbUri = data.getData();
					String selectimagepath = uri2filePath(thumbUri);

					// 缩略图地址
					String selectThumbnailUri = Utils.getThumbnailDir();
					// 获取缩略图,根据原图创建缩略图, mImagePath是原图的地址
					Utils.getThumbnail(selectimagepath, selectThumbnailUri);
					// Utils.getBitMapFromRes(selectimagepath,
					// selectThumbnailUri);

					File file1 = new File(selectThumbnailUri);

					String selectImageName = selectimagepath
							.substring(selectimagepath.lastIndexOf(File.separator) + 1);
					System.out.println("selectImageName: " + selectImageName);
					mediaList.add(
							new Media(Utils.MEDIA_TYPE_IMAGE, selectImageName, selectimagepath));

					// 显示所录制视频
					Uri uri1 = Uri.fromFile(file1);
					int mediaID1 = mediaIndex++;
					// 存储mediaId与imageOriginPath的映射
					index_originalPath_Map.put(mediaID1, selectimagepath);

					loadMedia(imageContainer, mediaID1, getThumbnailFromUri(uri1), uri1,
							TYPE_SELECT_IMAGE);

					// 存储mediaId与thumbnailUri的映射
					index_path_Map.put(mediaID1, selectThumbnailUri);

				}

			}

			break;

		case CAPTURE_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				// 缩略图地址
				String thumbnailUri = Utils.getThumbnailDir();
				// 获取缩略图,根据原图创建缩略图, mImagePath是原图的地址
				Utils.getThumbnail(mImagePath, thumbnailUri);

				// // 根据图片生成bitmap对象
				// Bitmap imageThumbnailBitmap =
				// BitmapFactory.decodeFile(mImagePath);
				File file2 = new File(thumbnailUri);

				String captureImageName = mImagePath
						.substring(mImagePath.lastIndexOf(File.separator) + 1);
				System.out.println("captureImageName: " + captureImageName);
				mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE, captureImageName, mImagePath));

				Uri uri2 = Uri.fromFile(file2);
				int mediaID2 = mediaIndex++;
				// 存储mediaId与imageOriginPath的映射
				index_originalPath_Map.put(mediaID2, mImagePath);

				loadMedia(imageContainer, mediaID2, getThumbnailFromUri(uri2), uri2, TYPE_IMAGE);

				// 存储mediaId与thumbnailUri的映射
				index_path_Map.put(mediaID2, thumbnailUri);
			}

			break;

		case CAPTURE_AUDIO_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				System.out.println("声音返回成功");
				// 根据图片生成bitmap对象
				Bitmap AudioThumbnailBitmap = BitmapFactory.decodeResource(getResources(),
						R.drawable.record);
				audioFileName = getFileDate();
				audiopath = data.getStringExtra("path");

				System.out.println("audiopath: " + audiopath);
				File file = new File(audiopath);

				String captureAudioName = audiopath
						.substring(audiopath.lastIndexOf(File.separator) + 1);
				System.out.println("captureAudioName: " + captureAudioName);
				mediaList.add(new Media(Utils.MEDIA_TYPE_AUDIO, captureAudioName, audiopath));

				Uri uri = Uri.fromFile(file);
				int mediaID = mediaIndex++;
				index_originalPath_Map.put(mediaID, audiopath);
				loadMedia(imageContainer, mediaID, AudioThumbnailBitmap, uri, TYPE_AUDIO);
			}

			break;
		// 选择接收人
		case TYPE_PEOPLE_SELECT:
			if (resultCode == RESULT_OK) {
				receiverList = (List<Node>) data.getSerializableExtra("data");
				StringBuilder builder = new StringBuilder();
				for (Node i : receiverList) {
					builder.append(i.getName() + "/");
				}
				mReceiverInput.setText(builder.toString());
			}
			break;
		}

	}

	/** 通过图片的Uri获取图片的缩略图 */
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

	private String uri2filePath(Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, proj, null, null, null);
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(index);
		return path;
	}

	/**
	 * 加载媒体（图片|视频）
	 * 
	 * @param viewContainer
	 * @param thumbnail
	 * @param uri
	 * @param MediaType
	 */
	public void loadMedia(FixedGridLayout viewContainer, int mediaId, Bitmap thumbnail,
			final Uri uri, final int MediaType) {

		System.out.println("index_originalPath_Map:" + index_originalPath_Map.toString());

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

		deleteBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// 显示删除对话框
				showDeleteMediaDialog(r1, MediaType);
			}
		});

		// 将图片添加入图片列表
		viewContainer.addView(r1);

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

		if (mediaType == TYPE_IMAGE) {
			// wm = BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.image_preview);
			// waterMark = CreateImgView(this, wm, 30, 30);

			waterMark.setText("图片");

			rl.addView(waterMark, lp3);
		} else if (mediaType == TYPE_VIDEO) {
			// wm = BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.video_play);
			// waterMark = CreateImgView(this, wm, 50, 50);

			waterMark.setText("视频");

			rl.addView(waterMark, lp3);
		} else if (mediaType == TYPE_AUDIO) {

			waterMark.setText("音频");

			rl.addView(waterMark, lp3);
		} else if (mediaType == TYPE_SELECT_IMAGE) {

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
				case TYPE_IMAGE:
					// 点击显示大图
					showImageDialog(imageView, uri);
					break;

				case TYPE_SELECT_IMAGE:
					// 点击显示大图
					showImageDialog(imageView, uri);
					break;
				case TYPE_VIDEO:

					// 点击播放视频
					Intent intent = new Intent(NewTask.this, PlayVideo.class);
					intent.putExtra("path", videopath);
					startActivity(intent);
					break;

				case TYPE_AUDIO:
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

	/** 通过图片的Uri获取图片 */
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

	/**
	 * 删除图片或视频对话框
	 * 
	 * @param rl
	 *            删除图片或视频所在的布局
	 * @param MediaType
	 *            媒体文件类型 1-图片 2-视频
	 */
	private void showDeleteMediaDialog(final RelativeLayout rl, final int MediaType) {
		String text = "";

		if (MediaType == TYPE_AUDIO) {
			text = "音频";
		} else if (MediaType == TYPE_VIDEO) {
			text = "视频";
		} else if (MediaType == TYPE_IMAGE) {
			text = "图片";
		} else if (MediaType == TYPE_SELECT_IMAGE) {
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

				if (MediaType == TYPE_AUDIO) {
					// 将图片从图片列表中删除
					imageContainer.removeView(rl);

					// 将手机中的媒体文件删除
					Integer index = rl.getId();

					// 删除原媒体文件（新）
					if (index_originalPath_Map.containsKey(index)) {
						String path = index_originalPath_Map.get(index);
						index_originalPath_Map.remove(index);
						deleteMedia(path);
					}
				} else if (MediaType == TYPE_VIDEO) {
					// 将视频从视频列表中删除
					imageContainer.removeView(rl);

					// 将手机中的媒体文件删除
					Integer index = rl.getId();

					// 删除原媒体文件（新）
					if (index_originalPath_Map.containsKey(index)) {
						String path = index_originalPath_Map.get(index);
						index_originalPath_Map.remove(index);
						deleteMedia(path);
					}
				} else if (MediaType == TYPE_IMAGE) {
					// 将图片从图片列表中删除
					imageContainer.removeView(rl);
					// 将手机中的媒体文件删除
					Integer index = rl.getId();

					// 删除原媒体文件（新）
					if (index_originalPath_Map.containsKey(index)) {
						String path = index_originalPath_Map.get(index);
						// System.out.println(path);
						index_originalPath_Map.remove(index);
						deleteMedia(path);
					}

					// 删除缩略图
					if (index_path_Map.containsKey(index)) {
						String path = index_path_Map.get(index);
						index_path_Map.remove(index);
						deleteMedia(path);
					}
				} else if (MediaType == TYPE_SELECT_IMAGE) {
					// 将图片从图片列表中删除
					imageContainer.removeView(rl);

					// System.out.println("删除缩略图");
					// 将手机中的媒体文件删除
					Integer index = rl.getId();
					// 删除缩略图
					if (index_path_Map.containsKey(index)) {
						String path = index_path_Map.get(index);
						index_path_Map.remove(index);
						deleteMedia(path);
					}
				}

				// 将手机中的媒体文件删除
				// Integer index = rl.getId();

				// WeiHao 媒体文件编号缩减，并判断是否已删空附件，如果已无附件，隐藏该控件
				if (--mediaIndex == 0) {
					showAttachLayout.setVisibility(View.GONE);
				}

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

	protected String getFileDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
		Date date = new Date(System.currentTimeMillis());
		String file = format.format(date);
		return file;
	}

	/**
	 * 删除媒体文件
	 */
	public static boolean deleteMedia(String path) {
		// 获取文件并判断存在与否
		File file = new File(path);
		if (!file.exists()) {
			return true;
		}
		// 删除文件
		if (file.delete()) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onBackPressed() {

		Utils.delAllFile(fileFolder);
		super.onBackPressed();
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

}
