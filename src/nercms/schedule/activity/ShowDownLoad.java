package nercms.schedule.activity;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.FixedGridLayout;
import nercms.schedule.view.PlayVideo;
import nercms.schedule.view.RoundAngleImageView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.HttpDownloadTask;

public class ShowDownLoad extends Activity {
	// 图片显示的宽高
	private static int IMG_WIDTH = 130;
	private static int IMG_HEIGHT = 130;

	// 附件缩略图展示Layout
	private LinearLayout showAttachLayout;
	// 图片容器
	private FixedGridLayout imageContainer;

	// 每行显示缩略图数目
	private final int NUMPERROW = 3;

	// 附件编号
	private int mediaIndex = 0;

	// 附件集
	private ArrayList<ShowDownLoad.Media> mediaList = new ArrayList<ShowDownLoad.Media>();

	// 保存mediaIndex与media缩略图地址的映射,
	// 存放每一个媒体文件的地址
	private HashMap<Integer, String> index_originalPath_Map = new HashMap<Integer, String>();

	// 保存mediaIndex与media缩略图地址的映射
	private HashMap<Integer, String> index_path_Map = new HashMap<Integer, String>();

	private static final int TYPE_IMAGE = 1;
	private static final int TYPE_VIDEO = 2;
	private static final int TYPE_AUDIO = 3;
	private static final int TYPE_SELECT_IMAGE = 4;

	// 显示大图对话框
	private Dialog imageDialog;

	private String mVideoPath;

	private Handler handler;

	private String fileFolder = Environment.getExternalStorageDirectory()
			.getPath() + "/nercms-Schedule/DownloadAttachments/";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.showdownload);

		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();
		int height = wm.getDefaultDisplay().getHeight();

		IMG_WIDTH = (width - 65) / 3;
		IMG_HEIGHT = IMG_WIDTH;

		// 附件缩略图展示Layout，默认不可见
		showAttachLayout = (LinearLayout) findViewById(R.id.showDownloadAttathLayout);
		showAttachLayout.setVisibility(View.GONE);

		// 图片容器初始化
		imageContainer = (FixedGridLayout) findViewById(R.id.downloadAttachContainer);
		imageContainer.setCellWidth(IMG_WIDTH);
		imageContainer.setCellHeight(IMG_HEIGHT);
		imageContainer.setNumPerRow(NUMPERROW);

		initHandler();

		File file = new File(fileFolder);
		if (!file.exists()) {
			file.mkdirs();
			Log.e("TAG", "THE FILE IS EXIST");
		}

		Intent intent = getIntent();
		List<String> fileName = (List<String>) intent
				.getSerializableExtra("name");
		for (String mediaName : fileName) {

			String downUrl = LocalConstant.FILE_SERVER_ATTACH_URL
					+ File.separator + mediaName;
			
			Log.e("TAG", "fileFolder : " + fileFolder);
			new HttpDownloadTask(ShowDownLoad.this).execute(downUrl,
					"/nercms-Schedule/DownloadAttachments/", mediaName);


		}
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {

		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.FILE_DOWNLOAD_SUCCESS:
					String mediaName = (String) msg.obj;
					Log.e("TAG", "case Constant.FILE_DOWNLOAD_SUCCESS: "
							+ mediaName);
					System.out.println("case Constant.FILE_DOWNLOAD_SUCCESS: "
							+ mediaName);
					if (mediaName.contains(".3gp")) {
						loadVideo(fileFolder + mediaName);
					} else if (mediaName.contains(".jpg")){
						loadImage(fileFolder + mediaName);
					} else if (mediaName.contains(".amr")){
						loadAudio(fileFolder + mediaName);
					}
					break;
				case Constant.FILE_DOWNLOAD_FAIL:
					Log.e("TAG", "case Constant.FILE_DOWNLOAD_SUCCESS: ");
					break;
				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler,
				Constant.FILE_DOWNLOAD_FAIL, "ShowDownLoad");
		MessageHandlerManager.getInstance().register(handler,
				Constant.FILE_DOWNLOAD_SUCCESS, "ShowDownLoad");
	}
	
	private void loadAudio(String audiopath){
		// 根据图片生成bitmap对象
		Bitmap AudioThumbnailBitmap = BitmapFactory.decodeResource(
				getResources(), R.drawable.record);

		
		File file = new File(audiopath);
		
		String captureAudioName = audiopath.substring(audiopath.lastIndexOf(File.separator)+1);
		mediaList.add(new Media(Utils.MEDIA_TYPE_AUDIO, captureAudioName, audiopath));

		Uri uri = Uri.fromFile(file);
		int mediaID = mediaIndex++;
		index_originalPath_Map.put(mediaID, audiopath);
		loadMedia(imageContainer, mediaID, AudioThumbnailBitmap, uri,
				TYPE_AUDIO);
	}
	
	public void loadImage(String mImagePath){
		// 缩略图地址
		String thumbnailUri = Utils.getThumbnailDir();
		// 获取缩略图,根据原图创建缩略图, mImagePath是原图的地址
		Utils.getThumbnail(mImagePath, thumbnailUri);

		// // 根据图片生成bitmap对象
		// Bitmap imageThumbnailBitmap =
		// BitmapFactory.decodeFile(mImagePath);
		File file2 = new File(thumbnailUri);
		
		String captureImageName = mImagePath.substring(mImagePath.lastIndexOf(File.separator)+1);
		System.out.println("captureImageName: "+captureImageName);
		mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE, captureImageName, mImagePath));

		Uri uri2 = Uri.fromFile(file2);
		int mediaID2 = mediaIndex++;
		// 存储mediaId与imageOriginPath的映射
		index_originalPath_Map.put(mediaID2, mImagePath);

		loadMedia(imageContainer, mediaID2, getThumbnailFromUri(uri2),
				uri2, TYPE_IMAGE);

		// 存储mediaId与thumbnailUri的映射
		index_path_Map.put(mediaID2, thumbnailUri);
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


	public void loadVideo(String videopath) {
		File file = new File(videopath);

		mVideoPath = videopath;

		Bitmap videoThumbnailBitmap = getVideoThumbnail(videopath, 400, 400,
				MediaStore.Images.Thumbnails.MINI_KIND);

		// 显示所录制视频
		Uri uri = Uri.fromFile(file);
		int mediaID = mediaIndex++;

		String videoName = videopath.substring(videopath
				.lastIndexOf(File.separator) + 1);
		System.out.println("videoName: " + videoName);
		mediaList.add(new Media(Utils.MEDIA_TYPE_VIDEO, videoName, videopath));
		// 存储文件的路径
		index_originalPath_Map.put(mediaID, videopath);
		loadMedia(imageContainer, mediaID, videoThumbnailBitmap, uri,
				TYPE_VIDEO);
	}

	// 还没下载完就获取了bitmap
	private Bitmap getVideoThumbnail(String videoPath, int width, int height,
			int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);

		Log.e("TAG", "getVideoThumbnail  : " + bitmap);

		Log.e("TAG", "getVideoThumbnail  : " + videoPath);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
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

	public void loadMedia(FixedGridLayout viewContainer, int mediaId,
			Bitmap thumbnail, final Uri uri, final int MediaType) {

		System.out.println("index_originalPath_Map:"
				+ index_originalPath_Map.toString());

		// WeiHao 如果附件展示布局不可见，置未可见
		if (showAttachLayout.getVisibility() == View.GONE) {
			showAttachLayout.setVisibility(View.VISIBLE);
		}
		// 将图片缩略图添加到缩略图列表，便于新建完成后回收
		// bitmapList.add(thumbnail);
		final ImageView imageView = CreateImgView(this, thumbnail, IMG_WIDTH,
				IMG_HEIGHT);
		setImageviewListener(uri, imageView, MediaType, mediaId);


		final RelativeLayout r1 = WrapImgView(MediaType, imageView);
		r1.setId(mediaId);

		// 将图片添加入图片列表
		viewContainer.addView(r1);

	}

	public ImageView CreateImgView(Context context, Bitmap pic, int width,
			int height) {
		// 加载图片的ImageView
		ImageView imageView = new RoundAngleImageView(context);
		// 将图片缩略图加载到ImageView
		imageView.setImageBitmap(pic);
		// // 为图片设置编号
		// imageView.setId(mediaId);
		// 设置图片显示格式
		FixedGridLayout.LayoutParams params = new FixedGridLayout.LayoutParams(
				width, height);
		imageView.setLayoutParams(params);
		imageView.setScaleType(ScaleType.CENTER_CROP);
		return imageView;
	}

	public void setImageviewListener(final Uri uri, final ImageView imageView,
			final int MediaType, final int MediaId) {
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
					Intent intent = new Intent(ShowDownLoad.this,
							PlayVideo.class);
					intent.putExtra("path", mVideoPath);
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
		ImageView dialogImageView = (ImageView) view
				.findViewById(R.id.imageImageView);
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

	public RelativeLayout WrapImgView(int mediaType, ImageView imgview
			) {
		RelativeLayout rl = new RelativeLayout(this);
		rl.setLayoutParams(new FixedGridLayout.LayoutParams(IMG_WIDTH,
				IMG_HEIGHT));
		rl.setPadding(2, 2, 2, 2);
		RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		lp1.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp1.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
		// imgview 位于父 View 的顶部，在父 View 中居左
		lp1.topMargin = 15;
		lp1.rightMargin = 15;
		rl.addView(imgview, lp1);

		RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(30,
				30);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		// btn1 位于父 View 的顶部，在父 View 中水平居右

		// WeiHao 媒体类型判断，显示水印
		// ...
		// Bitmap wm;
		// ImageView waterMark;
		TextView waterMark = new TextView(this);
		waterMark.setTextSize(12);
		waterMark.setBackgroundColor(getResources().getColor(R.color.darkgrey));
		waterMark.setTextColor(getResources().getColor(R.color.white));
		RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
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
	
//	@Override
//	public void onBackPressed() {
//		finish();
//		super.onBackPressed();
//	}

}
