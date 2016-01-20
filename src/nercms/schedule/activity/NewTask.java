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

	// ��������ͼչʾLayout
	private LinearLayout showAttachLayout;
	// ͼƬ����
	private FixedGridLayout imageContainer;

	// ͼƬ��ʾ�Ŀ��
	private static int IMG_WIDTH = 130;
	private static int IMG_HEIGHT = 130;
	// ÿ����ʾ����ͼ��Ŀ
	private final int NUMPERROW = 3;
	// �������
	private int mediaIndex = 0;

	// ������
	public static final int CAPTURE_AUDIO_REQUEST_CODE = 101;
	public static final int CAPTURE_VIDEO_REQUEST_CODE = 102;
	public static final int SELECT_IMAGE_REQUEST_CODE = 103;
	public static final int CAPTURE_IMAGE_REQUEST_CODE = 104;

	WebRequestManager manager;

	// ����mediaIndex��media����ͼ��ַ��ӳ��,
	// ���ÿһ��ý���ļ��ĵ�ַ
	private HashMap<Integer, String> index_originalPath_Map = new HashMap<Integer, String>();

	// ����mediaIndex��media����ͼ��ַ��ӳ��
	private HashMap<Integer, String> index_path_Map = new HashMap<Integer, String>();

	// �����洢·��
	private String imagePath;

	// ��ʾ��ͼ�Ի���
	private Dialog imageDialog;

	// ���Ͱ�ť
	private Button mSend;

	// ������
	private ArrayList<NewTask.Media> mediaList = new ArrayList<NewTask.Media>();

	// �ϴ�����
	private TextView mProgress;

	private Handler handler;

	// �ϴ��ĸ��������б�
	private List<String> fileNameList;

	private int count;// �ϴ������ĸ���

	// private ImageButton mReceiver;//�����˰�ť
	private EditText mReceiverInput;// �����������
	private EditText mContentInput;// ��Ҫ��������

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

		// ��Ҫ����һ�������д���
		tid = getIntent().getExtras().getString("tid");
		if (!TextUtils.isEmpty(tid)) {
			tb_task_instructions data = dao.getTaskIns(tid);
			mContentInput.setText(data.getContent());
			Utils.setEditTextUnEditable(mContentInput);
			mReceiverInput.setText(new OrgDao(this).getPerson(data.getSend_id()).getName());
			Utils.setEditTextUnEditable(mReceiverInput);
			// TODO ��������ʾ������Ϣ

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

		// ��������ͼչʾLayout��Ĭ�ϲ��ɼ�
		showAttachLayout = (LinearLayout) findViewById(R.id.showAttathLayout);
		showAttachLayout.setVisibility(View.GONE);

		// ͼƬ������ʼ��
		imageContainer = (FixedGridLayout) findViewById(R.id.attachContainer);
		imageContainer.setCellWidth(IMG_WIDTH);
		imageContainer.setCellHeight(IMG_HEIGHT);
		imageContainer.setNumPerRow(NUMPERROW);

		mVideo.setOnClickListener(click);

		System.out.println("width:" + width);
		System.out.println("height:" + height);

		mSend = (Button) findViewById(R.id.fasong);// ������Ͱ�ť�ϴ�����
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

				// �����ˣ��������ݺ͸�������Ŀ������Ϊ��
				if ((!(TextUtils.isEmpty(mReceiverInput.getText()))
						&& (!(TextUtils.isEmpty(mContentInput.getText()))) && (mediaIndex != 0))) {
					attachmentUploadRequest();
				} else {
					Toast.makeText(NewTask.this, "���ݲ���Ϊ��", Toast.LENGTH_SHORT).show();
				}

				// attachmentUploadRequest();
			}
		});

		// //��������˵ĵ���¼�
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
	 * �����ϴ�
	 */
	private void attachmentUploadRequest() {
		if (!isNetworkAvailable(NewTask.this)) {
			Toast.makeText(NewTask.this, "���粻����", Toast.LENGTH_SHORT).show();
			return;
		}

		fileNameList = new ArrayList<String>();
		String fileName = null;// �ϴ����ļ���

		count = mediaIndex;

		NewTask.Media media;
		for (int i = 0; i < mediaIndex; i++) {
			media = mediaList.get(i);

			String mediaPath = media.getMediaUrl();// ý���ļ��ı���·�����û������ϴ�ʱ
			String uploadUrl = Contants.HFS_URL;

			fileName = path2FileName(mediaPath);
			fileNameList.add(fileName);

			System.out.println("mediaPath : " + mediaPath);
			// �����ϴ�
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
				case Constant.FILE_UPLOAD_SUCCESS:// �ϴ��ɹ�

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

					// http�ӿ�����
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
					showLongToast("�����ϴ�ʧ��");
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
	 * �����ϴ���ť��Ӧ�¼�����
	 */
	private void initAttachPickBtn() {
		AlertDialog.Builder builder = new AlertDialog.Builder(NewTask.this);
		builder.setTitle("ѡ�񸽼�����").setItems(new String[] { "ͼ��", "����", "����", "¼��" },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int which) {
						switch (which) {
						case 0:
							// Utilss.showShortToast(MainActivity.this, "ͼ��");
							Intent getAlbum = new Intent(Intent.ACTION_PICK,
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							// ����Pictures����Type�趨Ϊimage
							getAlbum.setType("image/*");
							// getAlbum.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(getAlbum, SELECT_IMAGE_REQUEST_CODE);
							break;

						case 1:
							// Utilss.showShortToast(MainActivity.this,"����");
							// ����
							Intent cameraintent = new Intent();
							// ָ������ϵͳ�����Action
							cameraintent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
							cameraintent.addCategory(Intent.CATEGORY_DEFAULT);

							mImagePath = fileFolder + File.separator + getFileDate() + ".jpg";
							// �����ļ���ַ�����ļ�
							File imagefile = new File(mImagePath);
							if (imagefile.exists()) {
								imagefile.delete();
							}
							// ���ļ���ַת����Uri��ʽ
							Uri imageUri = Uri.fromFile(imagefile);
							// ����ϵͳ���������Ƭ��ɺ�ͼƬ�ļ��Ĵ�ŵ�ַ
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
		// ý���ļ���ַ
		String originalUri;

		switch (requestCode) {
		case CAPTURE_VIDEO_REQUEST_CODE:

			if (resultCode == RESULT_OK) {
				Log.i("TAG", "������ɣ�resultCode=" + requestCode);

				// MICRO_KIND��MINI_KIND �ֱ�Ϊ΢�ͺ�������������ģʽ��ǰ�ߵķֱ��ʸ���
				// videoThumbnail.setImageBitmap(getVideoThumbnail(path, 400,
				// 400,
				// MediaStore.Images.Thumbnails.MINI_KIND));

				// String videoName =
				// videopath.substring(videopath.lastIndexOf("/")
				// + 1);

				File file = new File(videopath);

				Bitmap videoThumbnailBitmap = getVideoThumbnail(videopath, 400, 400,
						MediaStore.Images.Thumbnails.MINI_KIND);

				// ��ʾ��¼����Ƶ
				Uri uri = Uri.fromFile(file);
				int mediaID = mediaIndex++;

				String videoName = videopath.substring(videopath.lastIndexOf(File.separator) + 1);
				System.out.println("videoName: " + videoName);
				mediaList.add(new Media(Utils.MEDIA_TYPE_VIDEO, videoName, videopath));
				// �洢�ļ���·��
				index_originalPath_Map.put(mediaID, videopath);
				loadMedia(imageContainer, mediaID, videoThumbnailBitmap, uri, TYPE_VIDEO);
			}

			break;

		case SELECT_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				if (data != null) {
					Uri thumbUri = data.getData();
					String selectimagepath = uri2filePath(thumbUri);

					// ����ͼ��ַ
					String selectThumbnailUri = Utils.getThumbnailDir();
					// ��ȡ����ͼ,����ԭͼ��������ͼ, mImagePath��ԭͼ�ĵ�ַ
					Utils.getThumbnail(selectimagepath, selectThumbnailUri);
					// Utils.getBitMapFromRes(selectimagepath,
					// selectThumbnailUri);

					File file1 = new File(selectThumbnailUri);

					String selectImageName = selectimagepath
							.substring(selectimagepath.lastIndexOf(File.separator) + 1);
					System.out.println("selectImageName: " + selectImageName);
					mediaList.add(
							new Media(Utils.MEDIA_TYPE_IMAGE, selectImageName, selectimagepath));

					// ��ʾ��¼����Ƶ
					Uri uri1 = Uri.fromFile(file1);
					int mediaID1 = mediaIndex++;
					// �洢mediaId��imageOriginPath��ӳ��
					index_originalPath_Map.put(mediaID1, selectimagepath);

					loadMedia(imageContainer, mediaID1, getThumbnailFromUri(uri1), uri1,
							TYPE_SELECT_IMAGE);

					// �洢mediaId��thumbnailUri��ӳ��
					index_path_Map.put(mediaID1, selectThumbnailUri);

				}

			}

			break;

		case CAPTURE_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				// ����ͼ��ַ
				String thumbnailUri = Utils.getThumbnailDir();
				// ��ȡ����ͼ,����ԭͼ��������ͼ, mImagePath��ԭͼ�ĵ�ַ
				Utils.getThumbnail(mImagePath, thumbnailUri);

				// // ����ͼƬ����bitmap����
				// Bitmap imageThumbnailBitmap =
				// BitmapFactory.decodeFile(mImagePath);
				File file2 = new File(thumbnailUri);

				String captureImageName = mImagePath
						.substring(mImagePath.lastIndexOf(File.separator) + 1);
				System.out.println("captureImageName: " + captureImageName);
				mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE, captureImageName, mImagePath));

				Uri uri2 = Uri.fromFile(file2);
				int mediaID2 = mediaIndex++;
				// �洢mediaId��imageOriginPath��ӳ��
				index_originalPath_Map.put(mediaID2, mImagePath);

				loadMedia(imageContainer, mediaID2, getThumbnailFromUri(uri2), uri2, TYPE_IMAGE);

				// �洢mediaId��thumbnailUri��ӳ��
				index_path_Map.put(mediaID2, thumbnailUri);
			}

			break;

		case CAPTURE_AUDIO_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				System.out.println("�������سɹ�");
				// ����ͼƬ����bitmap����
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
		// ѡ�������
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

	private String uri2filePath(Uri uri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(uri, proj, null, null, null);
		int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(index);
		return path;
	}

	/**
	 * ����ý�壨ͼƬ|��Ƶ��
	 * 
	 * @param viewContainer
	 * @param thumbnail
	 * @param uri
	 * @param MediaType
	 */
	public void loadMedia(FixedGridLayout viewContainer, int mediaId, Bitmap thumbnail,
			final Uri uri, final int MediaType) {

		System.out.println("index_originalPath_Map:" + index_originalPath_Map.toString());

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

		deleteBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// ��ʾɾ���Ի���
				showDeleteMediaDialog(r1, MediaType);
			}
		});

		// ��ͼƬ�����ͼƬ�б�
		viewContainer.addView(r1);

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
					Intent intent = new Intent(NewTask.this, PlayVideo.class);
					intent.putExtra("path", videopath);
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

	/**
	 * ɾ��ͼƬ����Ƶ�Ի���
	 * 
	 * @param rl
	 *            ɾ��ͼƬ����Ƶ���ڵĲ���
	 * @param MediaType
	 *            ý���ļ����� 1-ͼƬ 2-��Ƶ
	 */
	private void showDeleteMediaDialog(final RelativeLayout rl, final int MediaType) {
		String text = "";

		if (MediaType == TYPE_AUDIO) {
			text = "��Ƶ";
		} else if (MediaType == TYPE_VIDEO) {
			text = "��Ƶ";
		} else if (MediaType == TYPE_IMAGE) {
			text = "ͼƬ";
		} else if (MediaType == TYPE_SELECT_IMAGE) {
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

				if (MediaType == TYPE_AUDIO) {
					// ��ͼƬ��ͼƬ�б���ɾ��
					imageContainer.removeView(rl);

					// ���ֻ��е�ý���ļ�ɾ��
					Integer index = rl.getId();

					// ɾ��ԭý���ļ����£�
					if (index_originalPath_Map.containsKey(index)) {
						String path = index_originalPath_Map.get(index);
						index_originalPath_Map.remove(index);
						deleteMedia(path);
					}
				} else if (MediaType == TYPE_VIDEO) {
					// ����Ƶ����Ƶ�б���ɾ��
					imageContainer.removeView(rl);

					// ���ֻ��е�ý���ļ�ɾ��
					Integer index = rl.getId();

					// ɾ��ԭý���ļ����£�
					if (index_originalPath_Map.containsKey(index)) {
						String path = index_originalPath_Map.get(index);
						index_originalPath_Map.remove(index);
						deleteMedia(path);
					}
				} else if (MediaType == TYPE_IMAGE) {
					// ��ͼƬ��ͼƬ�б���ɾ��
					imageContainer.removeView(rl);
					// ���ֻ��е�ý���ļ�ɾ��
					Integer index = rl.getId();

					// ɾ��ԭý���ļ����£�
					if (index_originalPath_Map.containsKey(index)) {
						String path = index_originalPath_Map.get(index);
						// System.out.println(path);
						index_originalPath_Map.remove(index);
						deleteMedia(path);
					}

					// ɾ������ͼ
					if (index_path_Map.containsKey(index)) {
						String path = index_path_Map.get(index);
						index_path_Map.remove(index);
						deleteMedia(path);
					}
				} else if (MediaType == TYPE_SELECT_IMAGE) {
					// ��ͼƬ��ͼƬ�б���ɾ��
					imageContainer.removeView(rl);

					// System.out.println("ɾ������ͼ");
					// ���ֻ��е�ý���ļ�ɾ��
					Integer index = rl.getId();
					// ɾ������ͼ
					if (index_path_Map.containsKey(index)) {
						String path = index_path_Map.get(index);
						index_path_Map.remove(index);
						deleteMedia(path);
					}
				}

				// ���ֻ��е�ý���ļ�ɾ��
				// Integer index = rl.getId();

				// WeiHao ý���ļ�������������ж��Ƿ���ɾ�ո�����������޸��������ظÿؼ�
				if (--mediaIndex == 0) {
					showAttachLayout.setVisibility(View.GONE);
				}

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

	protected String getFileDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
		Date date = new Date(System.currentTimeMillis());
		String file = format.format(date);
		return file;
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

	@Override
	public void onBackPressed() {

		Utils.delAllFile(fileFolder);
		super.onBackPressed();
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

}
