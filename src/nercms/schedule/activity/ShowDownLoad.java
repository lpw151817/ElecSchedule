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
	// ͼƬ��ʾ�Ŀ��
	private static int IMG_WIDTH = 130;
	private static int IMG_HEIGHT = 130;

	// ��������ͼչʾLayout
	private LinearLayout showAttachLayout;
	// ͼƬ����
	private FixedGridLayout imageContainer;

	// ÿ����ʾ����ͼ��Ŀ
	private final int NUMPERROW = 3;

	// �������
	private int mediaIndex = 0;

	// ������
	private ArrayList<ShowDownLoad.Media> mediaList = new ArrayList<ShowDownLoad.Media>();

	// ����mediaIndex��media����ͼ��ַ��ӳ��,
	// ���ÿһ��ý���ļ��ĵ�ַ
	private HashMap<Integer, String> index_originalPath_Map = new HashMap<Integer, String>();

	// ����mediaIndex��media����ͼ��ַ��ӳ��
	private HashMap<Integer, String> index_path_Map = new HashMap<Integer, String>();

	private static final int TYPE_IMAGE = 1;
	private static final int TYPE_VIDEO = 2;
	private static final int TYPE_AUDIO = 3;
	private static final int TYPE_SELECT_IMAGE = 4;

	// ��ʾ��ͼ�Ի���
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

		// ��������ͼչʾLayout��Ĭ�ϲ��ɼ�
		showAttachLayout = (LinearLayout) findViewById(R.id.showDownloadAttathLayout);
		showAttachLayout.setVisibility(View.GONE);

		// ͼƬ������ʼ��
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
		// ����ͼƬ����bitmap����
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
		// ����ͼ��ַ
		String thumbnailUri = Utils.getThumbnailDir();
		// ��ȡ����ͼ,����ԭͼ��������ͼ, mImagePath��ԭͼ�ĵ�ַ
		Utils.getThumbnail(mImagePath, thumbnailUri);

		// // ����ͼƬ����bitmap����
		// Bitmap imageThumbnailBitmap =
		// BitmapFactory.decodeFile(mImagePath);
		File file2 = new File(thumbnailUri);
		
		String captureImageName = mImagePath.substring(mImagePath.lastIndexOf(File.separator)+1);
		System.out.println("captureImageName: "+captureImageName);
		mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE, captureImageName, mImagePath));

		Uri uri2 = Uri.fromFile(file2);
		int mediaID2 = mediaIndex++;
		// �洢mediaId��imageOriginPath��ӳ��
		index_originalPath_Map.put(mediaID2, mImagePath);

		loadMedia(imageContainer, mediaID2, getThumbnailFromUri(uri2),
				uri2, TYPE_IMAGE);

		// �洢mediaId��thumbnailUri��ӳ��
		index_path_Map.put(mediaID2, thumbnailUri);
	}
	
	/** ͨ��ͼƬ��Uri��ȡͼƬ������ͼ */
	public Bitmap getThumbnailFromUri(final Uri uri) {
		ContentResolver cr = this.getContentResolver();
		// ͼƬ������
		InputStream input = null;
		try {
			input = cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// ͼƬ����ѡ��
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;
		// ��ȡͼƬ����ͼ������
		return BitmapFactory.decodeStream(input, null, opts);
	}


	public void loadVideo(String videopath) {
		File file = new File(videopath);

		mVideoPath = videopath;

		Bitmap videoThumbnailBitmap = getVideoThumbnail(videopath, 400, 400,
				MediaStore.Images.Thumbnails.MINI_KIND);

		// ��ʾ��¼����Ƶ
		Uri uri = Uri.fromFile(file);
		int mediaID = mediaIndex++;

		String videoName = videopath.substring(videopath
				.lastIndexOf(File.separator) + 1);
		System.out.println("videoName: " + videoName);
		mediaList.add(new Media(Utils.MEDIA_TYPE_VIDEO, videoName, videopath));
		// �洢�ļ���·��
		index_originalPath_Map.put(mediaID, videopath);
		loadMedia(imageContainer, mediaID, videoThumbnailBitmap, uri,
				TYPE_VIDEO);
	}

	// ��û������ͻ�ȡ��bitmap
	private Bitmap getVideoThumbnail(String videoPath, int width, int height,
			int kind) {
		Bitmap bitmap = null;
		// ��ȡ��Ƶ������ͼ
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);

		Log.e("TAG", "getVideoThumbnail  : " + bitmap);

		Log.e("TAG", "getVideoThumbnail  : " + videoPath);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}


	// ý����
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

		// WeiHao �������չʾ���ֲ��ɼ�����δ�ɼ�
		if (showAttachLayout.getVisibility() == View.GONE) {
			showAttachLayout.setVisibility(View.VISIBLE);
		}
		// ��ͼƬ����ͼ��ӵ�����ͼ�б������½���ɺ����
		// bitmapList.add(thumbnail);
		final ImageView imageView = CreateImgView(this, thumbnail, IMG_WIDTH,
				IMG_HEIGHT);
		setImageviewListener(uri, imageView, MediaType, mediaId);


		final RelativeLayout r1 = WrapImgView(MediaType, imageView);
		r1.setId(mediaId);

		// ��ͼƬ�����ͼƬ�б�
		viewContainer.addView(r1);

	}

	public ImageView CreateImgView(Context context, Bitmap pic, int width,
			int height) {
		// ����ͼƬ��ImageView
		ImageView imageView = new RoundAngleImageView(context);
		// ��ͼƬ����ͼ���ص�ImageView
		imageView.setImageBitmap(pic);
		// // ΪͼƬ���ñ��
		// imageView.setId(mediaId);
		// ����ͼƬ��ʾ��ʽ
		FixedGridLayout.LayoutParams params = new FixedGridLayout.LayoutParams(
				width, height);
		imageView.setLayoutParams(params);
		imageView.setScaleType(ScaleType.CENTER_CROP);
		return imageView;
	}

	public void setImageviewListener(final Uri uri, final ImageView imageView,
			final int MediaType, final int MediaId) {
		// ΪͼƬ���ô����¼�
		imageView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				// ����ʱ���͸��
				if (event.getAction() == MotionEvent.ACTION_DOWN)
					imageView.setAlpha(50);
				// ̧��ʱ�ָ�͸����
				else if (event.getAction() == MotionEvent.ACTION_UP)
					imageView.setAlpha(255);
				return false;
			}
		});
		// ΪͼƬ���õ����¼�
		imageView.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// �����¼������󣬵����¼�ʧЧ
				// imageView.setEnabled(false);
				switch (MediaType) {
				case TYPE_IMAGE:
					// �����ʾ��ͼ
					showImageDialog(imageView, uri);
					break;

				case TYPE_SELECT_IMAGE:
					// �����ʾ��ͼ
					showImageDialog(imageView, uri);
					break;
				case TYPE_VIDEO:
					// ���������Ƶ
					Intent intent = new Intent(ShowDownLoad.this,
							PlayVideo.class);
					intent.putExtra("path", mVideoPath);
					startActivity(intent);
					break;

				case TYPE_AUDIO:
					// �����Ƶ
					Log.e("TAG", "������Ƶ");

					// ����mediaId����ȡ�������Ƶ��·��
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

	/** ��ʾ��ͼ�Ի��� */
	private void showImageDialog(final ImageView imageView, final Uri uri) {
		// ��ȡ�Ի��򲼾ֲ�ʵ����
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.image_dialog, null);
		// ����Ի���
		imageDialog = new Dialog(this, R.style.imageDialog);
		imageDialog.setContentView(view);
		// ���ͼƬ
		ImageView dialogImageView = (ImageView) view
				.findViewById(R.id.imageImageView);
		// ��ȡͼƬ
		final Bitmap pic = getBitmapFromUri(uri);
		// ��ͼƬ����ͼ���ص�ImageView
		dialogImageView.setImageBitmap(pic);
		// ΪͼƬ���õ����¼�
		dialogImageView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				imageDialog.dismiss();
				imageDialog = null;
				// ����ͼ�����¼��ָ�
				imageView.setEnabled(true);
				// ����ͼƬ
				pic.recycle();
			}
		});
		// ��ʾ�Ի���
		imageDialog.show();
	}

	/** ͨ��ͼƬ��Uri��ȡͼƬ */
	public Bitmap getBitmapFromUri(final Uri uri) {
		ContentResolver cr = this.getContentResolver();
		// ͼƬ������
		InputStream input = null;
		try {
			input = cr.openInputStream(uri);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// ��ȡͼƬ����ͼ������
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
		// imgview λ�ڸ� View �Ķ������ڸ� View �о���
		lp1.topMargin = 15;
		lp1.rightMargin = 15;
		rl.addView(imgview, lp1);

		RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(30,
				30);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		// btn1 λ�ڸ� View �Ķ������ڸ� View ��ˮƽ����

		// WeiHao ý�������жϣ���ʾˮӡ
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
		// waterMarkλ�ڸ�View������
		lp3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp3.leftMargin = 10;
		lp3.bottomMargin = 25; // �ײ������������ã���ʱδ�ҵ�ԭ��

		if (mediaType == TYPE_IMAGE) {
			// wm = BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.image_preview);
			// waterMark = CreateImgView(this, wm, 30, 30);

			waterMark.setText("ͼƬ");

			rl.addView(waterMark, lp3);
		} else if (mediaType == TYPE_VIDEO) {
			// wm = BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.video_play);
			// waterMark = CreateImgView(this, wm, 50, 50);

			waterMark.setText("��Ƶ");

			rl.addView(waterMark, lp3);
		} else if (mediaType == TYPE_AUDIO) {

			waterMark.setText("��Ƶ");

			rl.addView(waterMark, lp3);
		} else if (mediaType == TYPE_SELECT_IMAGE) {

			waterMark.setText("ͼƬ");
			rl.addView(waterMark, lp3);
		}

		return rl;
	}


	/**
	 * ɾ��ý���ļ�
	 */
	public static boolean deleteMedia(String path) {
		// ��ȡ�ļ����жϴ������
		File file = new File(path);
		if (!file.exists()) {
			return true;
		}
		// ɾ���ļ�
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
