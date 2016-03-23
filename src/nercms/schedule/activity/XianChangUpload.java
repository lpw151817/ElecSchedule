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

	// ��������ͼչʾLayout
	private LinearLayout showAttachLayout;
	// ͼƬ����
	private FixedGridLayout imageContainer;

	// ͼƬ��ʾ�Ŀ��
	private static int IMG_WIDTH = 130;
	private static int IMG_HEIGHT = 130;
	// ÿ����ʾ����ͼ��Ŀ
	private final int NUMPERROW = 3;
	private String mFilePath;// ���ݹ����ĸ���·��
	private String mFileName;
	// ��ʾ��ͼ�Ի���
	private Dialog imageDialog;

	// �������,
	private int mediaIndex = 0;

	// ����mediaIndex��media����ͼ��ַ��ӳ��,
	// ���ÿһ��ý���ļ��ĵ�ַ
	private HashMap<Integer, String> index_originalPath_Map = new HashMap<Integer, String>();

	// // ����mediaIndex��media����ͼ��ַ��ӳ��
	private HashMap<Integer, String> index_path_Map = new HashMap<Integer, String>();

	// ������,�����ϴ�����
	private ArrayList<XianChangUpload.Media> mediaList = new ArrayList<XianChangUpload.Media>();

	List<Map<String, Object>> mUrl = new ArrayList<Map<String, Object>>();

	List<Map<String, Object>> mUploadUrl = new ArrayList<Map<String, Object>>();// ��������Ѿ��ϴ��ĸ���

	List<Map<String, Object>> mUnUploadUrl = new ArrayList<Map<String, Object>>();// �������δ�ϴ��ĸ���

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

		// ��������ͼչʾLayout��Ĭ�ϲ��ɼ�
		showAttachLayout = (LinearLayout) findViewById(R.id.showAttathLayout);
		showAttachLayout.setVisibility(View.GONE);

		// ͼƬ������ʼ��
		imageContainer = (FixedGridLayout) findViewById(R.id.attachContainer);
		imageContainer.setCellWidth(IMG_WIDTH);
		imageContainer.setCellHeight(IMG_HEIGHT);
		imageContainer.setNumPerRow(NUMPERROW);

		int mIndex = getIntent().getIntExtra("myMediaIndex", -1);
		System.out.println("mIndex: " + mIndex);

		mUploadUrl = (List<Map<String, Object>>) getIntent().getSerializableExtra("mUploadUrl");

		// ����upload������˳��1.��showxianchangattachment=>xianchangAdd=>upload,��������Ϊ0
		// 2.xianchanadd=>upload(���actionbar��home)=>xianchangadd=>upload,����mediaIndex�����ϴε�ֵ
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
		case R.id.select:// ѡ�񸽼�
			int[] isRadio;
			int[] isPhoto;
			int[] isVideo;
			int position = getIntent().getIntExtra("position", -1);
			List<String> name = new ArrayList<String>();

			// �����̺߳���ҵ�ֳ���ÿ����Ŀ�еĸ�����ť�Ĳ����ǲ�һ����
			if (Main.pageType == 0) {

				isRadio = c.getResources().getIntArray(R.array.zuoyexianchang_si_radio);
				isPhoto = c.getResources().getIntArray(R.array.zuoyexianchang_si_photo);
				isVideo = c.getResources().getIntArray(R.array.zuoyexianchang_si_video);

				if (isPhoto[position] == 1) {
					name.add("����");
				}

				if (isRadio[position] == 1) {
					name.add("¼��");
				}

			} else if (Main.pageType == 1) {
				isRadio = c.getResources().getIntArray(R.array.caozuoxianchang_si_radio);
				isPhoto = c.getResources().getIntArray(R.array.caozuoxianchang_si_photo);
				isVideo = c.getResources().getIntArray(R.array.caozuoxianchang_si_video);

				if (isPhoto[position] == 1) {
					name.add("����");
				}

				if (isRadio[position] == 1) {
					name.add("¼��");
				}

				if (isVideo[position] == 1) {
					name.add("����");
				}
			}

			else if (Main.pageType == 2) {
				isRadio = c.getResources().getIntArray(R.array.guzhangjinji_si_radio);
				isPhoto = c.getResources().getIntArray(R.array.guzhangjinji_si_photo);
				isVideo = c.getResources().getIntArray(R.array.guzhangjinji_si_video);

				if (isPhoto[position] == 1) {
					name.add("����");
				}

				if (isRadio[position] == 1) {
					name.add("¼��");
				}

				if (isVideo[position] == 1) {
					name.add("����");
				}
			}

			// ��Ҫ���Ǹ��ݲ�ͬ��ҳ��Ͳ�ͬ����Ŀ�������ĸ����Ի�������ݲ�һ�����е�ֻ��Ҫ¼�����е�ֻ��Ҫ���񡣡���

			int size = name.size();
			final String[] array = (String[]) name.toArray(new String[size]);

			AlertDialog.Builder builder = new AlertDialog.Builder(XianChangUpload.this);
			builder.setTitle("ѡ�񸽼�����").setItems(array, new DialogInterface.OnClickListener() {

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
			// �ϴ�mUrl����ĸ�������Ҫ�����Ѿ��ϴ��˵Ĳ����ظ��ϴ�
			int fileCount = getFileCount();
			if (fileCount < 1) {
				showShortToast("��ѡ�񸽼�");
			} else {
				attachmentUploadRequest();// �ϴ�����
			}
			break;
		}
	}

	private void attachmentUploadRequest() {
		if (!Utils.isNetworkAvailable(XianChangUpload.this)) {
			Toast.makeText(XianChangUpload.this, "���粻����", Toast.LENGTH_SHORT).show();
			return;
		}
		String uploadUrl = android.wxapp.service.elec.request.Contants.HFS_URL;
		if (getFileCount() < 1) {
			showLongToast("��ѡ�񸽼��ϴ�");
		} else {

			// �����ظ��ϴ�
			mUnUploadUrl.clear();
			// TODO ��mUrl-mUploadList = mUnploadList

			List<String> mUrlPath = new ArrayList<String>();
			List<String> mUrlUploadPath = new ArrayList<String>();
			List<String> mUrlUnUploadPath = new ArrayList<String>();

			// ��ӡ�����������
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
			// �ϴ�
			for (Map<String, Object> map : mUnUploadUrl) {
				if (map != null) {
					if (map.get("path") != null) {
						Log.e("TAG", "�ϴ���·�� : " + map.get("path"));
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
		if (name.equals("����")) {

			// Utilss.showShortToast(MainActivity.this,"����");
			// ����
			Intent cameraintent = new Intent();
			// ָ������ϵͳ�����Action
			cameraintent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			cameraintent.addCategory(Intent.CATEGORY_DEFAULT);

			mImagePath = NewTask.fileFolder + File.separator + getFileDate() + ".jpg";
			// �����ļ���ַ�����ļ�
			File imagefile = new File(mImagePath);
			if (imagefile.exists()) {
				imagefile.delete();
			}
			// ���ļ���ַת����Uri��ʽ
			Uri imageUri = Uri.fromFile(imagefile);
			// ����ϵͳ���������Ƭ��ɺ�ͼƬ�ļ��Ĵ�ŵ�ַ
			cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(cameraintent, LocalConstant.CAPTURE_IMAGE_REQUEST_CODE);
		} else if (name.equals("¼��")) {
			Intent recordIntent = new Intent(XianChangUpload.this, RecordActivity.class);
			startActivityForResult(recordIntent, LocalConstant.CAPTURE_AUDIO_REQUEST_CODE);
		} else if (name.equals("����")) {
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
			System.out.println("�Ǵ�home����ȥ��ô");
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
			builder.setMessage("����δ�ϴ����Ƿ�����ϴ�");
			builder.setCancelable(false);
			builder.setPositiveButton("��", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
			builder.setNegativeButton("��", new DialogInterface.OnClickListener() {

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
					data.putExtra("mediaIndex", mediaIndex);// �Ѹ������±�ֵ����ȥ���´ν�����ʱ��mediaIndex���ϴε�λ�ÿ�ʼ����
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
			data.putExtra("mediaIndex", mediaIndex);// �Ѹ������±�ֵ����ȥ���´ν�����ʱ��mediaIndex���ϴε�λ�ÿ�ʼ����
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

		// ��ӡ�����������
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
					// ��ȡ����ͼ,����ԭͼ��������ͼ, mImagePath��ԭͼ�ĵ�ַ
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
					mMap.put("index", mediaID1 + "");// ���ݸ������±�

					Map<Integer, Map<String, String>> mContent = new HashMap<Integer, Map<String, String>>();
					mContent.put(0, mMap);
					intent.putExtra("address", (Serializable) mContent);
					startActivityForResult(intent, 321);

					// mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE,
					// selectImageName, selectimagepath));
					// // �洢mediaId��imageOriginPath��ӳ��
					// index_originalPath_Map.put(mediaID1, selectimagepath);
					// loadMedia(imageContainer, mediaID1,
					// getThumbnailFromUri(uri1), uri1,
					// NewTask.TYPE_SELECT_IMAGE);
					// // �洢mediaId��thumbnailUri��ӳ��
					// index_path_Map.put(mediaID1, selectThumbnailUri);

				}
			}
			break;

		case 321:

			if (resultCode == RESULT_OK) {
				if (data != null) {

					Map<String, Object> mMa = (Map<String, Object>) data
							.getSerializableExtra("path");
					System.out.println("���ݹ�����gps��Ϣ�� " + mMa.get("gps"));
					String index = (String) mMa.get("index");
					mUrl.add(mMa);// ����Ϣ��ӽ�ȥ

					String fileType = (String) data.getStringExtra("fileType");

					// ѡ��ͼƬ�������ȡ����ͼƬ����ʾ��ɾ�������ǲ�һ����
					if (fileType.equals("selectImage")) {
						int mediaID1 = Integer.valueOf(index);
						mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE, selectImageName,
								selectimagepath));
						index_originalPath_Map.put(mediaID1, selectimagepath);
						System.out.println("mediaID1 : " + mediaID1);

						loadMedia(imageContainer, mediaID1, getThumbnailFromUri(uri1), uri1,
								NewTask.TYPE_SELECT_IMAGE);
						// �洢mediaId��thumbnailUri��ӳ��
						index_path_Map.put(mediaID1, selectThumbnailUri);
					} else if (fileType.equals("captureImage")) {
						int mediaID2 = Integer.valueOf(index);

						mediaList.add(
								new Media(Utils.MEDIA_TYPE_IMAGE, captureImageName, mImagePath));
						// �洢mediaId��imageOriginPath��ӳ��
						index_originalPath_Map.put(mediaID2, mImagePath);
						System.out.println("mediaID2 : " + mediaID2);
						loadMedia(imageContainer, mediaID2, getThumbnailFromUri(uri2), uri2,
								NewTask.TYPE_IMAGE);
						// �洢mediaId��thumbnailUri��ӳ��
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
						// �洢�ļ���·��
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
				// ��ȡ����ͼ,����ԭͼ��������ͼ, mImagePath��ԭͼ�ĵ�ַ
				Utils.getThumbnail(mImagePath, thumbnailUri);

				// // ����ͼƬ����bitmap����
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
				mMap.put("index", mediaID2 + "");// ���ݸ������±�

				Map<Integer, Map<String, String>> mContent = new HashMap<Integer, Map<String, String>>();
				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable) mContent);
				startActivityForResult(intent, 321);

				// mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE,
				// captureImageName, mImagePath));
				// // �洢mediaId��imageOriginPath��ӳ��
				// index_originalPath_Map.put(mediaID2, mImagePath);
				//
				// loadMedia(imageContainer, mediaID2,
				// getThumbnailFromUri(uri2),
				// uri2, NewTask.TYPE_IMAGE);
				// // �洢mediaId��thumbnailUri��ӳ��
				// index_path_Map.put(mediaID2, thumbnailUri);
			}

			break;

		case LocalConstant.CAPTURE_AUDIO_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				System.out.println("�������سɹ�");
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
				mMap.put("index", mediaID + "");// ���ݸ������±�

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
				Log.i("TAG", "������ɣ�resultCode=" + requestCode);

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
				mMap.put("index", mediaID4 + "");// ���ݸ������±�

				Map<Integer, Map<String, String>> mContent = new HashMap<Integer, Map<String, String>>();
				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable) mContent);
				startActivityForResult(intent, 321);

				// mediaList.add(new Media(Utils.MEDIA_TYPE_VIDEO, videoName,
				// videopath));
				// // �洢�ļ���·��
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
						if (mfile.exists()) {// ��Ե��Ǵ����������ظ�����ʱ��û��������ɣ��͵�����룬����ʾ���˸���
							// mFilePath = (String)
							// getIntent().getCharSequenceExtra("path");
							mFileName = mFilePath
									.substring(mFilePath.lastIndexOf(File.separator) + 1);

							if (mFileName.contains(".jpg") || mFileName.contains(".png")
									|| mFileName.contains(".jpeg")) {// ����������ͼƬ

								String thumbnailUri = Utils.getThumbnailDir();
								// ��ȡ����ͼ,����ԭͼ��������ͼ, mImagePath��ԭͼ�ĵ�ַ
								Utils.getThumbnail(mFilePath, thumbnailUri);
								File file2 = new File(thumbnailUri);
								Uri uri2 = Uri.fromFile(file2);

								// �Ӹ����л�ȡ�����±�
								int mediaID2 = Integer.valueOf((String) map.get("index"));// ��һ�ν���

								// �洢mediaId��imageOriginPath��ӳ��
								index_originalPath_Map.put(mediaID2, mFilePath);
								loadMedia(imageContainer, mediaID2, getThumbnailFromUri(uri2), uri2,
										NewTask.TYPE_IMAGE);

								// �洢mediaId��thumbnailUri��ӳ��
								index_path_Map.put(mediaID2, thumbnailUri);
							} else if (mFileName.contains(".mp3") || mFileName.contains(".amr")) {// ���ݹ���������Ƶ

								// ����ͼƬ����bitmap����
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
								// mediaID = mediaIndex++;;//��һ�ν���
								// }

								index_originalPath_Map.put(mediaID, mFilePath);
								loadMedia(imageContainer, mediaID, AudioThumbnailBitmap, uri,
										NewTask.TYPE_AUDIO);
							} else if (mFileName.contains(".mp4")) {
								File file = new File(mFilePath);

								Bitmap videoThumbnailBitmap = getVideoThumbnail(mFilePath, 400, 400,
										MediaStore.Images.Thumbnails.MINI_KIND);

								// ��ʾ��¼����Ƶ
								Uri uri = Uri.fromFile(file);
								int mediaID = Integer.valueOf((String) map.get("index"));
								// if (isOk2){
								// mediaID ;
								// } else {
								// mediaID = mediaIndex++;;//��һ�ν���
								// }

								mediaList.add(
										new Media(Utils.MEDIA_TYPE_VIDEO, mFileName, mFilePath));
								// �洢�ļ���·��
								index_originalPath_Map.put(mediaID, mFilePath);
								loadMedia(imageContainer, mediaID, videoThumbnailBitmap, uri,
										NewTask.TYPE_VIDEO);
							}

						} else {
							Toast.makeText(this, "������������", Toast.LENGTH_SHORT).show();
						}

					}
				}

			}
		}

	}

	public void loadMedia(FixedGridLayout viewContainer, int mediaId, Bitmap thumbnail,
			final Uri uri, final int MediaType) {

		// WeiHao �������չʾ���ֲ��ɼ�����δ�ɼ�
		if (showAttachLayout.getVisibility() == View.GONE) {
			showAttachLayout.setVisibility(View.VISIBLE);
		}
		// ��ͼƬ����ͼ��ӵ�����ͼ�б������½���ɺ����
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
				// ��ʾɾ���Ի���
				showDeleteMediaDialog(r1, MediaType);
			}
		});

		// ��ͼƬ�����ͼƬ�б�
		viewContainer.addView(r1);
		if (isAdmin())
			deleteBtn.setVisibility(View.GONE);

	}

	/**
	 * ����ͼ�ؼ�
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
		// imgview λ�ڸ� View �Ķ������ڸ� View �о���
		lp1.topMargin = 15;
		lp1.rightMargin = 15;
		rl.addView(imgview, lp1);

		RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(30, 30);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		lp2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		// btn1 λ�ڸ� View �Ķ������ڸ� View ��ˮƽ����
		rl.addView(btn, lp2);

		// WeiHao ý�������жϣ���ʾˮӡ
		// ...
		// Bitmap wm;
		// ImageView waterMark;
		TextView waterMark = new TextView(this);
		waterMark.setTextSize(12);
		waterMark.setBackgroundColor(getResources().getColor(R.color.darkgrey));
		waterMark.setTextColor(getResources().getColor(R.color.white));
		RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		// waterMarkλ�ڸ�View������
		lp3.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		lp3.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		lp3.leftMargin = 10;
		lp3.bottomMargin = 25; // �ײ������������ã���ʱδ�ҵ�ԭ��

		if (mediaType == NewTask.TYPE_IMAGE) {
			// wm = BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.image_preview);
			// waterMark = CreateImgView(this, wm, 30, 30);

			waterMark.setText("ͼƬ");

			rl.addView(waterMark, lp3);
		} else if (mediaType == NewTask.TYPE_VIDEO) {
			// wm = BitmapFactory.decodeResource(this.getResources(),
			// R.drawable.video_play);
			// waterMark = CreateImgView(this, wm, 50, 50);

			waterMark.setText("��Ƶ");

			rl.addView(waterMark, lp3);
		} else if (mediaType == NewTask.TYPE_AUDIO) {

			waterMark.setText("��Ƶ");

			rl.addView(waterMark, lp3);
		} else if (mediaType == NewTask.TYPE_SELECT_IMAGE) {

			waterMark.setText("ͼƬ");
			rl.addView(waterMark, lp3);
		}
		return rl;
	}

	/**
	 * ����ImageView
	 * 
	 * @param context
	 * @param id
	 * @param pic
	 * @param width
	 * @param height
	 * @return
	 */
	public ImageView CreateImgView(Context context, Bitmap pic, int width, int height) {
		// ����ͼƬ��ImageView
		ImageView imageView = new RoundAngleImageView(context);
		// ��ͼƬ����ͼ���ص�ImageView
		imageView.setImageBitmap(pic);
		// // ΪͼƬ���ñ��
		// imageView.setId(mediaId);
		// ����ͼƬ��ʾ��ʽ
		FixedGridLayout.LayoutParams params = new FixedGridLayout.LayoutParams(width, height);
		imageView.setLayoutParams(params);
		imageView.setScaleType(ScaleType.CENTER_CROP);
		return imageView;
	}

	/**
	 * ������صļ����¼�
	 * 
	 * @param uri
	 * @param imageView
	 */
	public void setImageviewListener(final Uri uri, final ImageView imageView, final int MediaType,
			final int MediaId) {
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
				case NewTask.TYPE_IMAGE:
					// �����ʾ��ͼ
					showImageDialog(imageView, uri);
					break;

				case NewTask.TYPE_SELECT_IMAGE:
					// �����ʾ��ͼ
					showImageDialog(imageView, uri);
					break;
				case NewTask.TYPE_VIDEO:
					// ���������Ƶ
					Intent intent = new Intent(XianChangUpload.this, PlayVideo.class);
					intent.putExtra("path", index_originalPath_Map.get(MediaId));
					startActivity(intent);
					break;

				case NewTask.TYPE_AUDIO:
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
		ImageView dialogImageView = (ImageView) view.findViewById(R.id.imageImageView);
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

	private void showDeleteMediaDialog(final RelativeLayout rl, final int MediaType) {
		String text = "";

		if (MediaType == NewTask.TYPE_AUDIO) {
			text = "��Ƶ";
		} else if (MediaType == NewTask.TYPE_VIDEO) {
			text = "��Ƶ";
		} else if (MediaType == NewTask.TYPE_IMAGE) {
			text = "ͼƬ";
		} else if (MediaType == NewTask.TYPE_SELECT_IMAGE) {
			text = "ͼƬ";
		}

		AlertDialog.Builder builder = new Builder(this);
		// ���ñ���
		builder.setTitle("��ʾ");
		// ������ʾ����
		builder.setMessage("ȷ��ɾ��" + text + "��");
		// ����ȷ����ť
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				// ��ͼ��ѡ���ͼƬ���ܱ�ɾ����������Ҫ��ɾ��

				if (MediaType == NewTask.TYPE_AUDIO) {
					// ��ͼƬ��ͼƬ�б���ɾ��
					imageContainer.removeView(rl);

					// ���ֻ��е�ý���ļ�ɾ��
					Integer index = rl.getId();

					System.out.println("Ӧ���Ƴ����±�index : " + index);
					System.out.println("�Ӹ����б����Ƴ�ǰ�� " + mUrl.toString());
					// mUrl.remove(index.intValue());//�����б����Ƴ����´δ�List�н����ʱ��Ͳ�����ʾ��
					mUrl.set(index.intValue(), null);
					System.out.println("�Ӹ����б����Ƴ��� " + mUrl.toString());

					// ɾ��ԭý���ļ����£�
					if (index_originalPath_Map.containsKey(index)) {
						String path = index_originalPath_Map.get(index);
						index_originalPath_Map.remove(index);
						Utils.deleteMedia(path);
					}
				} else if (MediaType == NewTask.TYPE_VIDEO) {
					// ����Ƶ����Ƶ�б���ɾ��
					imageContainer.removeView(rl);

					// ���ֻ��е�ý���ļ�ɾ��
					Integer index = rl.getId();

					System.out.println("Ӧ���Ƴ����±�index : " + index);
					System.out.println("�Ӹ����б����Ƴ�ǰ�� " + mUrl.toString());
					mUrl.set(index.intValue(), null);
					System.out.println("�Ӹ����б����Ƴ��� " + mUrl.toString());

					// ɾ��ԭý���ļ����£�
					if (index_originalPath_Map.containsKey(index)) {
						String path = index_originalPath_Map.get(index);
						index_originalPath_Map.remove(index);
						Utils.deleteMedia(path);
					}
				} else if (MediaType == NewTask.TYPE_IMAGE) {
					// ��ͼƬ��ͼƬ�б���ɾ��
					imageContainer.removeView(rl);
					// ���ֻ��е�ý���ļ�ɾ��
					Integer index = rl.getId();

					System.out.println("Ӧ���Ƴ����±�index : " + index);
					System.out.println("�Ӹ����б����Ƴ�ǰ�� " + mUrl.toString());
					mUrl.set(index.intValue(), null);

					System.out.println("�Ӹ����б����Ƴ��� " + mUrl.toString());

					// ɾ��ԭý���ļ����£�
					// if (index_originalPath_Map.containsKey(index)) {
					// String path = index_originalPath_Map.get(index);
					// // System.out.println(path);
					// index_originalPath_Map.remove(index);
					// Utils.deleteMedia(path);
					// }

					// ɾ������ͼ
					if (index_path_Map.containsKey(index)) {
						String path = index_path_Map.get(index);
						index_path_Map.remove(index);
						Utils.deleteMedia(path);
					}
				} else if (MediaType == NewTask.TYPE_SELECT_IMAGE) {
					// ��ͼƬ��ͼƬ�б���ɾ��
					imageContainer.removeView(rl);

					// System.out.println("ɾ������ͼ");
					// ���ֻ��е�ý���ļ�ɾ��
					Integer index = rl.getId();

					System.out.println("Ӧ���Ƴ����±�index : " + index);
					System.out.println("�Ӹ����б����Ƴ�ǰ�� " + mUrl.toString());
					mUrl.set(index.intValue(), null);
					System.out.println("�Ӹ����б����Ƴ��� " + mUrl.toString());

					// ɾ������ͼ
					if (index_path_Map.containsKey(index)) {
						String path = index_path_Map.get(index);
						index_path_Map.remove(index);
						Utils.deleteMedia(path);
					}
				}

				// ���ֻ��е�ý���ļ�ɾ��
				// Integer index = rl.getId();

				// WeiHao ý���ļ�������������ж��Ƿ���ɾ�ո�����������޸��������ظÿؼ�
				// if (--mediaIndex == 0) {
				// showAttachLayout.setVisibility(View.GONE);
				// }

				// �Ի���ر�
				dialog.dismiss();
			}
		});

		// ����ȡ����ť
		builder.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				// �Ի���ر�
				dialog.dismiss();
				// ����ͼ�����¼��ָ�
				rl.setEnabled(true);
			}
		});
		// ��ʾ�Ի���
		builder.create().show();
	}

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

	protected String getFileDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
		Date date = new Date(System.currentTimeMillis());
		String file = format.format(date);
		return file;
	}

	private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// ��ȡ��Ƶ������ͼ
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);

		if (bitmap == null) {
			return null;
		}

		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	Handler handler;

	private int mUnUploadFileCount = 0;// ��¼Ϊ�ϴ��ĸ�����Ŀ

	private int position;

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.FILE_UPLOAD_SUCCESS:// �����еĸ������ϴ�����֮��finish��ǰҳ��

					mUnUploadFileCount--;
					Log.i("TAG", "count : " + mUnUploadFileCount);
					if (mUnUploadFileCount == 0) {

						Toast.makeText(XianChangUpload.this, "�ϴ��ɹ�", Toast.LENGTH_SHORT).show();
						// ����http�ӿ�
						List<TaskAttachment> attachment = new ArrayList<TaskAttachment>();
						// for (int i = 0; i < mUnUploadUrl.size(); i++) {
						StringBuilder standard = new StringBuilder("standard");
						// ��ҵ�ֳ�
						if (enterType == 1) {
							switch (position) {
							// ����Ʊ
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
						// �����ֳ�
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
						// ��������
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
								// �����޸�
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
					Toast.makeText(XianChangUpload.this, "�ϴ�ʧ��", Toast.LENGTH_SHORT).show();
					break;

				case Constants.UPLOAD_TASK_ATT_SUCCESS:
					break;
				case Constants.END_TASK_SUCCESS:
					showLongToast("�����ѽ���");
					break;

				case Constants.UPLOAD_TASK_ATT_SAVE_FAIL:
				case Constants.UPLOAD_TASK_ATT_FAIL:
				case Constants.END_TASK_FAIL:
				case Constants.END_TASK_SAVE_FAIL:
					if (msg.obj != null) {
						showAlterDialog("�ϴ�ʧ��", ((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "ȷ��", null);
					} else {
						showAlterDialog("�ϴ�ʧ��", "�����Ƿ����������������", R.drawable.login_error_icon, "ȷ��",
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