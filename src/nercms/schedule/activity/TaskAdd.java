package nercms.schedule.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.dateSelect.NumericWheelAdapter;
import nercms.schedule.dateSelect.OnWheelChangedListener;
import nercms.schedule.dateSelect.WheelView;
import nercms.schedule.layout.RoundAngleImageView;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
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
import android.graphics.Bitmap.Config;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.AffairDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.affair.CreateTaskRequestAttachment;
import android.wxapp.service.jerry.model.affair.CreateTaskRequestIds;
import android.wxapp.service.jerry.model.affair.QueryAffairInfoResponse;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.jerry.model.person.Org;
import android.wxapp.service.model.AffairAttachModel;
import android.wxapp.service.model.AffairModel;
import android.wxapp.service.model.PersonOnDutyModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.thread.SaveAffairThread;
import android.wxapp.service.thread.ThreadManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.HttpDownloadTask;
import android.wxapp.service.util.HttpUploadTask;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.imooc.treeview.utils.Node;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class TaskAdd extends BaseActivity {

	private static final String TAG = "TaskAddActivity";

	List<Node> selectedPerson;

	// �����������
	private WebRequestManager webRequestManager;
	// ���������������
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	// ��Ա��������������
	private PersonDao personDao;
	// ���û�ID
	private String userID;

	private Handler handler;

	// �ı���ؼ�
	private EditText etTitle; // ��������
	private EditText etSponsor; // ������
	private EditText etPod; // ������
	private EditText etEndTime; // ��ֹʱ��
	private EditText etContent; // ��������
	private ImageButton btnPodPicker; // ������ѡȡ��ť
	private List<Node> lsSelectedPod;
	private List<Node> lsSelectedReceiver;
	private EditText etReceiver;
	private ImageButton btnReceiverPicker;

	private LinearLayout attachPickLayout;

	private TextView tvUploadStatus;

	// ʱ��ؼ����
	private ImageButton btn_calendar;
	private Dialog dialog;
	private static int START_YEAR = 2010, END_YEAR = 2020;

	// ������Ӱ�ť
	private ImageButton btnAttachPicker;
	private AffairModel affairModel;
	private PersonOnDutyModel personModel;
	private boolean isAllAttachmentUpload = false;

	// ��ʾ��ͼ�Ի���
	private Dialog imageDialog;
	// ý���ļ�����
	private final int TYPE_IMAGE = 1;
	private final int TYPE_VIDEO = 2;
	// ͼƬ��ʾ�Ŀ��
	private final int IMG_WIDTH = 130;
	private final int IMG_HEIGHT = 130;
	// ÿ����ʾ����ͼ��Ŀ
	private final int NUMPERROW = 3;

	// ��������ͼչʾLayout
	private LinearLayout showAttachLayout;
	// ������ʾ��ͼƬ�ļ���
	private ArrayList<Bitmap> bitmapList = new ArrayList<Bitmap>();
	// �����洢·��
	private String imagePath;
	private String videoPath;
	// ������
	private ArrayList<TaskAdd.Media> mediaList = new ArrayList<TaskAdd.Media>();
	// �������
	private int mediaIndex = 0;
	// ͼƬ����
	private nercms.schedule.layout.FixedGridLayout imageContainer;

	// ����mediaIndex��media����ͼ��ַ��ӳ��
	private HashMap<Integer, String> index_path_Map = new HashMap<Integer, String>();
	// ����mediaIndex��media����ͼ��ַ��ӳ��
	private HashMap<Integer, String> index_originalPath_Map = new HashMap<Integer, String>();

	// ����ID����onCreate()�н��г�ʼ��
	private String taskID;

	// �����ϴ��ɹ�������
	private int successCounter = 0;

	// ����mediaIndex��layout��ַ��ӳ��
	// private HashMap<Integer, RelativeLayout> index_layout_Map = new
	// HashMap<Integer, RelativeLayout>();
	QueryAffairInfoResponse data;
	DisplayImageOptions options;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_add);

		data = (QueryAffairInfoResponse) getIntent().getSerializableExtra("data");

		webRequestManager = new WebRequestManager(AppApplication.getInstance(), this);

		// ׼���û���Ϣ
		userID = MySharedPreference.get(TaskAdd.this, MySharedPreference.USER_ID, "");
		personDao = daoFactory.getPersonDao(TaskAdd.this);

		// ��ʼ��ActionBar
		initActionBar();

		// ��ʼ��Handler
		initHandler();

		// ��������ID
		taskID = Utils.produceTaskID(userID);

		// �ؼ���ʼ��
		etTitle = (EditText) findViewById(R.id.task_title);
		etSponsor = (EditText) findViewById(R.id.task_starter);
		etPod = (EditText) findViewById(R.id.task_participator);
		etEndTime = (EditText) findViewById(R.id.task_deadline);
		etEndTime.setEnabled(false);// ʱ�䲻�ɱ༭
		etContent = (EditText) findViewById(R.id.task_content);
		btnPodPicker = (ImageButton) findViewById(R.id.btn_pod_picker);
		btn_calendar = (ImageButton) findViewById(R.id.select_time);
		etReceiver = (EditText) findViewById(R.id.task_receiver_et);
		btnReceiverPicker = (ImageButton) findViewById(R.id.btn_receiver_picker);

		tvUploadStatus = (TextView) findViewById(R.id.upload_status_textview);

		// ��������ʾ
		etSponsor.setEnabled(false);
		etSponsor.setText(personDao.getPersonInfo(userID).getN());

		// ������ѡ��
		btnReceiverPicker.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(TaskAdd.this, ContactSelect.class);
				intent.putExtra("entrance_flag", 1);
				intent.putExtra("type", 2);
				intent.putExtra("pod", (Serializable) lsSelectedPod);
				intent.putExtra("receiver", (Serializable) lsSelectedReceiver);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivityForResult(intent, LocalConstant.TASK_POD_SELECT_REQUEST_CODE);
			}
		});

		// TODO ������ѡ��
		btnPodPicker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(TaskAdd.this, ContactSelect.class);
				intent.putExtra("entrance_flag", 1);
				intent.putExtra("type", 1);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent.putExtra("pod", (Serializable) lsSelectedPod);
				intent.putExtra("receiver", (Serializable) lsSelectedReceiver);
				startActivityForResult(intent, LocalConstant.TASK_POD_SELECT_REQUEST_CODE);
			}
		});

		btn_calendar.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showDateTimePicker();
			}
		});
		// *****************************************

		// attachment = (EditText)findViewById(R.id.task_attachment);

		// 2014-5-16 WeiHao ���� �����ϴ���ť
		btnAttachPicker = (ImageButton) findViewById(R.id.select_attach);
		btnAttachPicker.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				initAttachPickBtn();
			}
		});

		// ��������ͼչʾLayout��Ĭ�ϲ��ɼ�
		showAttachLayout = (LinearLayout) findViewById(R.id.showAttathLayout);
		showAttachLayout.setVisibility(View.GONE);

		// ͼƬ������ʼ��
		imageContainer = (nercms.schedule.layout.FixedGridLayout) findViewById(
				R.id.attachContainer);
		imageContainer.setCellWidth(IMG_WIDTH);
		imageContainer.setCellHeight(IMG_HEIGHT);
		imageContainer.setNumPerRow(NUMPERROW);

		attachPickLayout = (LinearLayout) findViewById(R.id.task_add_attach_pick_ll);
		attachPickLayout.setVisibility(View.VISIBLE);

		// �������ʷ���ݣ���˵�������޸�������Ϣ�ģ����Լ���ԭ����������Ϣ
		if (data != null) {
			loadHistory();
		}

	}

	private void loadHistory() {

		etTitle.setText(data.getTopic());
		String sPod = "";
		for (CreateTaskRequestIds item : data.getPod()) {
			sPod += (personDao.getPersonInfo(item.getRid()).getN() + "/");
		}
		etPod.setText(sPod);
		String sReceiver = "";
		for (CreateTaskRequestIds item : data.getRids()) {
			sReceiver += (personDao.getPersonInfo(item.getRid()).getN() + "/");
		}
		etReceiver.setText(sReceiver);
		etEndTime.setText(Utils.formatDateMs(data.getEt()));
		etContent.setText(data.getD());
		// ����и����Ļ�
		if (data.getAtt() != null && data.getAtt().size() > 0) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			String path = sdcardDir.getPath() + "/nercms-Schedule/Attachments/";
			String videoThumbnailDir = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail/";
			String mediaName;
			int mediaType;
			// ������ʾͼƬ�����ļ���
			ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();
			options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.no_picture) // ����ͼƬ�������ڼ���ʾ��ͼƬ
					.showImageForEmptyUri(R.drawable.no_picture)// ����ͼƬUriΪ�ջ��Ǵ����ʱ����ʾ��ͼƬ
					.showImageOnFail(R.drawable.no_picture) // ����ͼƬ����/��������д���ʱ����ʾ��ͼƬ
					.cacheInMemory(true)// �������ص�ͼƬ�Ƿ񻺴����ڴ���
					.cacheOnDisc(true)// �������ص�ͼƬ�Ƿ񻺴���SD����
					.considerExifParams(true) // �Ƿ���JPEGͼ��EXIF��������ת����ת��
					.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// ����ͼƬ����εı��뷽ʽ��ʾ
					.bitmapConfig(Bitmap.Config.RGB_565)// ����ͼƬ�Ľ�������//
					// .delayBeforeLoading(int delayInMillis)//int
					// delayInMillisΪ�����õ�����ǰ���ӳ�ʱ��
					// ����ͼƬ���뻺��ǰ����bitmap��������
					// .preProcessor(BitmapProcessor preProcessor)
					.resetViewBeforeLoading(true)// ����ͼƬ������ǰ�Ƿ����ã���λ
					.displayer(new RoundedBitmapDisplayer(20))// �Ƿ�����ΪԲ�ǣ�����Ϊ����
					.displayer(new FadeInBitmapDisplayer(100))// �Ƿ�ͼƬ���غú���Ķ���ʱ��
					.build();// �������;

			// ����ÿһ��������������ʾ����
			for (CreateTaskRequestAttachment item : data.getAtt()) {

				mediaName = item.getU();
				mediaName = mediaName.substring(mediaName.lastIndexOf("/") + 1);
				mediaType = Integer.parseInt(item.getAt());
				final String mediaPath = path.toString() + mediaName;
				if (mediaPath != null && !mediaPath.equalsIgnoreCase("")) {

					// 2014-6-24 WeiHao �������첽ͼƬ���أ�����
					final ImageView imageView = new RoundAngleImageView(this);
					// ��ͼƬ���븽����ʾͼƬ�ļ���
					imageView.setTag(mediaName);
					imageViewList.add(imageView);
					addImage(imageView, mediaPath, false);
					if (mediaType == Utils.MEDIA_TYPE_IMAGE) {

						// ����ͼƬ����ͼ����¼�
						imageView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setDataAndType(Uri.parse("file://" + mediaPath), "image/*");
								startActivity(intent);
							}
						});

						if (!new File(mediaPath).exists()) {
							String downUrl = android.wxapp.service.elec.request.Contants.HFS_URL + File.separator
									+ mediaName;
							// ��������ͼƬ
							ImageRequest imageRequest = new ImageRequest(downUrl,
									new Response.Listener<Bitmap>() {

										@Override
										public void onResponse(Bitmap response) {
											// ͼƬ���浽����
											Utils.saveBitmap(response, mediaPath);
											addImage(imageView, mediaPath, true);
										}
									}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
										@Override
										public void onErrorResponse(VolleyError error) {
										}
									});
							// �����������
							AppApplication.getInstance().myQueue.add(imageRequest);
						} else {
							addImage(imageView, mediaPath, true);
						}
					} else if (mediaType == Utils.MEDIA_TYPE_VIDEO) {
						// ������Ƶ����ͼ����¼�
						imageView.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View arg0) {
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent.setDataAndType(Uri.parse("file://" + mediaPath), "video/*");
								startActivity(intent);
							}
						});
						if (!new File(mediaPath).exists()) {
							String downUrl = LocalConstant.FILE_SERVER_ATTACH_URL + File.separator
									+ mediaName;
							new HttpDownloadTask(TaskAdd.this).execute(downUrl,
									"/nercms-Schedule/Attachments/", mediaName);
						} else {

							// �ж���Ƶ����ͼ�Ƿ���ڣ�����������������ͼ
							String thumbnailPath = videoThumbnailDir
									+ mediaName.substring(0, mediaName.indexOf(".")) + ".jpg";
							if (!new File(thumbnailPath).exists()) {
								Utils.saveBitmap(ThumbnailUtils.createVideoThumbnail(mediaPath,
										Thumbnails.MINI_KIND), thumbnailPath);
							}
							addImage(imageView, thumbnailPath, true);
						}
					}
				}
			}
		}
	}

	private void addImage(final ImageView imageView, final String path, boolean isDone) {
		// ����ͼƬ��ImageView
		// final ImageView imageView = new RoundAngleImageView(this);
		imageView.setPadding(2, 2, 2, 2);
		// ΪimageView���ñ�־���Ա�������ɺ����
		// imageView.setTag(path);
		// ��ʼ��ͼƬ��ʾ����
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(IMG_WIDTH,
				IMG_HEIGHT);
		layoutParams.setMargins(0, 10, 0, 10);
		// ����ͼƬ��ʾ��ʽ
		imageView.setLayoutParams(layoutParams);
		imageView.setScaleType(ScaleType.FIT_XY);

		if (isDone) {
			// ΪͼƬ���õ����¼�
			// imageView.setOnClickListener(new OnClickListener() {
			// public void onClick(View v) {
			// // �����¼������󣬵����¼�ʧЧ
			// // imageView.setEnabled(false);
			// // �����ʾ��ͼ
			// // showImageDialog(imageView, path);
			//
			// }
			// });
			// �첽���ر���ͼƬ
			com.nostra13.universalimageloader.core.ImageLoader.getInstance()
					.displayImage("file://" + path, imageView, options);
		} else {
			// ��ͼƬ�����ͼƬ�б�
			imageContainer.addView(imageView);
			// ���ñ���Ϊ��ɫ
			imageView.setBackgroundResource(R.drawable.no_picture);
		}

	}

	// 2014-5-23 WeiHao
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		if (data != null)
			getSupportActionBar().setTitle("�޸�����");
		else
			getSupportActionBar().setTitle("����������");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// ������ť
		MenuItem save = menu.add(0, 1, 0, "����");
		save.setIcon(R.drawable.ic_action_save);
		save.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// �ж��Ƿ�����������
			backDetect();
			break;
		case 1: // ���棨���ͣ�����
			String title = etTitle.getText().toString();
			String taskContent = etContent.getText().toString();
			String pod = etPod.getText().toString();
			String r = etReceiver.getText().toString();
			String time = etEndTime.getText().toString();
			if (title.isEmpty() || taskContent.isEmpty() || pod.isEmpty() || r.isEmpty()
					|| time.isEmpty()) {
				new AlertDialog.Builder(TaskAdd.this).setTitle("����д������Ϣ")
						.setPositiveButton("ȷ��", null).show();
			} else {
				// ������޸�affair�Ļ�
				if (data != null) {
					// �����޸Ľӿ�
					if (mediaList.size() == 0)
						modifyTask();
					else
						attachmentUploadRequest();
				} else {
					if (mediaList.size() == 0)
						createTask();
					else {
						// �����ϴ�����
						attachmentUploadRequest();
					}
				}

			}

			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	// 2015-5-23 WeiHao ���ؼ�ⷽ��
	private void backDetect() {
		String title = etTitle.getText().toString();
		String podID = etPod.getText().toString();
		String taskContent = etContent.getText().toString();
		if (!title.isEmpty() && String.valueOf(podID).isEmpty() && taskContent.isEmpty()
				&& mediaIndex == 0) {
			new AlertDialog.Builder(TaskAdd.this).setTitle("ȷ���˳�")
					.setMessage("��������ݻ�����Ӹ������ᶪʧ\nȷ���˳�������?")
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// ɾ������
							if (mediaIndex != 0) {
								for (int i = 0; i < mediaIndex; i++) {
								}
							}
							TaskAdd.this.finish();
						}
					}).setNegativeButton("ȡ��", null).create().show();
		} else {
			this.finish();
		}
	}

	// QueryAffairInfoResponse tempSave;

	// 2014-5-19 WeiHao ���������񷽷�
	private void createTask() {
		showProgressDialog("loading...");
		String title = etTitle.getText().toString();
		// PersonOnDutyModel pod = new PersonOnDutyModel(taskID, getUserId());
		String endTime = etEndTime.getText().toString();
		String taskContent = etContent.getText().toString();

		if (lsSelectedPod == null || lsSelectedReceiver == null)
			return;

		List<String> tempReiceverIds = new ArrayList<String>();
		List<CreateTaskRequestIds> tempRids = new ArrayList<CreateTaskRequestIds>();
		for (Node item : lsSelectedReceiver) {
			String tempId = item.getId();
			tempReiceverIds.add(tempId.substring(1, tempId.length()));
			tempRids.add(new CreateTaskRequestIds(tempId.substring(1, tempId.length())));
		}

		List<String> tempPodIds = new ArrayList<String>();
		List<CreateTaskRequestIds> tempPods = new ArrayList<CreateTaskRequestIds>();
		for (Node item : lsSelectedPod) {
			String tempId = item.getId();
			tempPodIds.add(tempId.substring(1, tempId.length()));
			tempPods.add(new CreateTaskRequestIds(tempId.substring(1, tempId.length())));
		}

		List<String> tempAttachmentTypes = new ArrayList<String>();
		List<String> tempAttachmentUrls = new ArrayList<String>();
		List<CreateTaskRequestAttachment> tempAttachments = new ArrayList<CreateTaskRequestAttachment>();
		for (Media item : mediaList) {
			tempAttachmentTypes.add(item.getMediaType() + "");
			tempAttachmentUrls.add(
					android.wxapp.service.elec.request.Contants.HFS_URL + File.separator + item.getMediaName());
			tempAttachments.add(
					new CreateTaskRequestAttachment(item.getMediaType() + "", item.getMediaUrl()));
		}
		String tempNow = System.currentTimeMillis() + "";
		// ������������
		webRequestManager.sendAffair("1", taskContent, title, tempNow,
				Utils.parseDateInFormat(endTime), "", "1", tempNow, "", tempAttachmentTypes,
				tempAttachmentUrls, tempReiceverIds, tempPodIds);

		// tempSave = new QueryAffairInfoResponse("", "", "1", getUserId(),
		// taskContent, title, tempNow,
		// Utils.parseDateInFormat(endTime), tempNow, "1", tempNow, "",
		// tempAttachments,
		// tempRids, tempPods);
	}

	private void modifyTask() {
		showProgressDialog("loading...");

		List<CreateTaskRequestIds> tempPods = new ArrayList<CreateTaskRequestIds>();

		// ��ȡ�޸ĺ�ĸ�����
		if (lsSelectedPod != null && lsSelectedPod.size() > 0) {
			for (Node item : lsSelectedPod) {
				tempPods.add(new CreateTaskRequestIds(item.getId().substring(1)));
			}
		}

		// ��ȡ�޸ĺ�ĳ�����
		List<CreateTaskRequestIds> tempRids = new ArrayList<CreateTaskRequestIds>();
		if (lsSelectedReceiver != null && lsSelectedReceiver.size() > 0) {
			for (Node item : lsSelectedReceiver) {
				tempRids.add(new CreateTaskRequestIds(item.getId().substring(1)));
			}
		}

		// ��ȡ�޸ĺ�ĸ����б�
		List<CreateTaskRequestAttachment> tempAtts = new ArrayList<CreateTaskRequestAttachment>();
		if (mediaList != null && mediaList.size() > 0) {
			List<String> tempAttachmentTypes = new ArrayList<String>();
			List<String> tempAttachmentUrls = new ArrayList<String>();
			for (Media item : mediaList) {
				tempAttachmentTypes.add(item.getMediaType() + "");
				tempAttachmentUrls.add(android.wxapp.service.elec.request.Contants.HFS_URL + File.separator
						+ item.getMediaName());
				// TODO �����ԣ���url��ʾ����ʲô
				tempAtts.add(new CreateTaskRequestAttachment(item.getMediaType() + "",
						item.getMediaUrl()));
			}
		}

		webRequestManager.modifyAffair(data.getAid(), tempPods, tempRids,
				etContent.getText().toString(), etTitle.getText().toString(),
				Utils.parseDateInFormat(etEndTime.getText().toString()), tempAtts);
	}

	private void attachmentUploadRequest() {
		AffairAttachModel attach;
		TaskAdd.Media media;
		for (int i = 0; i < mediaIndex; i++) {
			media = mediaList.get(i);
			attach = new AffairAttachModel(taskID, media.getMediaType(), media.getMediaName());

			String mediaPath = media.getMediaUrl();// ý���ļ��ı���·�����û������ϴ�ʱ
			String uploadUrl = android.wxapp.service.elec.request.Contants.HFS_URL;
			// �����ϴ�
			new HttpUploadTask(tvUploadStatus, this,null).execute(mediaPath, uploadUrl);
		}
	}

	// 2014-5-21 WeiHao
	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.CREATE_AFFAIR_REQUEST_SUCCESS:
					// ���񱣴浽�������ݿ�
					// if (tempSave != null) {
					// new SaveAffairThread(TaskAdd.this, tempSave).run();
					dismissProgressDialog();
					Utils.showShortToast(TaskAdd.this, "�½�����ɹ�");
					TaskAdd.this.finish();
					// } else {
					// Utils.showShortToast(TaskAdd.this, "�½�����ʧ��");
					// }
					break;
				case Constant.CREATE_AFFAIR_REQUEST_FAIL:
					new AlertDialog.Builder(TaskAdd.this).setTitle("�½�����ʧ��").setMessage("�Ƿ����·���?")
							.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							createTask();
						}
					}).setNegativeButton("ȡ��", null).create().show();

					NormalServerResponse failedMsg = (NormalServerResponse) msg.obj;
					Log.e("TaskAdd", "�½�����ʧ�ܣ�" + failedMsg.getEc());

					break;
				case Constant.FILE_UPLOAD_SUCCESS:
					successCounter++;
					if (successCounter == mediaIndex) {
						// ����ȫ���ϴ��ɹ��������������񵽷�����
						if (data == null)
							createTask();
						else
							modifyTask();
					}
					break;
				case Constant.FILE_UPLOAD_FAIL:
					successCounter = 0; // ����������
					Utils.showShortToast(TaskAdd.this, "�����ϴ�ʧ�ܣ������޷�����\n�������������Ƿ�����");
					break;
				case Constant.UPDATE_TASK_INFO_SUCCESS:
					dismissProgressDialog();
					Utils.showShortToast(TaskAdd.this, "�޸ĳɹ�");
					Intent intent = new Intent();
					intent.putExtra("aid", data.getAid());
					setResult(RESULT_OK, intent);
					TaskAdd.this.finish();
					break;
				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler,
				Constant.CREATE_AFFAIR_REQUEST_SUCCESS, Contants.METHOD_AFFAIRS_ADDAFFAIR);
		MessageHandlerManager.getInstance().register(handler, Constant.CREATE_AFFAIR_REQUEST_FAIL,
				Contants.METHOD_AFFAIRS_ADDAFFAIR);
		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_FAIL, "TaskAdd");
		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_SUCCESS,
				"TaskAdd");
		MessageHandlerManager.getInstance().register(handler, Constant.UPDATE_TASK_INFO_SUCCESS,
				Contants.METHOD_AFFAIRS_UPDATE_INFO);
	}

	// 2014-5-16 WeiHao ���� ���ش���
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// ����ͼ��ַ
		String thumbnailUri;
		// ý���ļ���ַ
		String originalUri;

		switch (requestCode) {
		// ����
		case LocalConstant.CAPTURE_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK) {

				// ����ͼ��ַ
				thumbnailUri = Utils.getAttachThumbnailDir();
				if (data == null) {
					File file = new File(imagePath);
					if (file.exists())
						originalUri = imagePath;
					else {
						originalUri = "";
					}
				} else {
					originalUri = imagePath;
				}
				// ��ȡ����ͼ
				Utils.getThumbnail(originalUri, thumbnailUri);

				// �ж��ļ��Ƿ����,������ֱ������
				File file = new File(thumbnailUri);
				if (!file.exists()) {
					return;
				}

				String imageName = originalUri.substring(originalUri.lastIndexOf("/") + 1);
				mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE, imageName, originalUri));
				// ��ʾ������Ƭ
				Uri uri = Uri.fromFile(file);
				int mediaID = mediaIndex++;
				loadMedia(imageContainer, mediaID, getThumbnailFromUri(uri), uri, TYPE_IMAGE);
				// �洢mediaId��imageOriginPath��ӳ��
				index_originalPath_Map.put(mediaID, originalUri);
				// �洢mediaId��thumbnailUri��ӳ��
				index_path_Map.put(mediaID, thumbnailUri);

			}

			break;
		// ¼��
		case LocalConstant.CAPTURE_VIDEO_REQUEST_CODE:
			originalUri = videoPath;
			// �ж��ļ��Ƿ����,������ֱ������
			File file = new File(originalUri);
			if (!file.exists()) {
				return;
			}

			// ������Ƶ����ͼ
			Bitmap videoThumbnailBitmap = ThumbnailUtils.createVideoThumbnail(originalUri,
					Thumbnails.FULL_SCREEN_KIND);

			String videoName = originalUri.substring(originalUri.lastIndexOf("/") + 1);
			// ����Ƶ�����ý���ļ�����
			mediaList.add(new Media(Utils.MEDIA_TYPE_VIDEO, videoName, originalUri));
			// ��ʾ��¼����Ƶ
			Uri uri = Uri.fromFile(file);
			int mediaID = mediaIndex++;
			loadMedia(imageContainer, mediaID, videoThumbnailBitmap, uri, TYPE_VIDEO);
			// �洢mediaID��originPath��ӳ��
			index_originalPath_Map.put(mediaID, originalUri);

			break;
		// ѡ��ͼƬ
		case LocalConstant.SELECT_IMAGE_REQUEST_CODE:

			thumbnailUri = Utils.getAttachThumbnailDir();

			// ����˷��ؼ������󷵻�Ϊ��,����
			if (data == null) {
				originalUri = "";
			} else {
				// ���ͼƬ��uri �ж��Ƿ���Ҫ����·��
				Uri selectedUri = data.getData();
				originalUri = selectedUri.getPath();

				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedUri, proj, null, null, null);
				// ����û�ѡ���ͼƬ������ֵ
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				// ����������ֵ��ȡͼƬ·��
				originalUri = cursor.getString(columnIndex);
				cursor.close();

				if (!(originalUri.endsWith("jpg") || originalUri.endsWith("gif")
						|| originalUri.endsWith("bmp") || originalUri.endsWith("png"))) {
					originalUri = "";
					Utils.showShortToast(TaskAdd.this, "����ͼƬ");
					return;
				}
				imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, taskID, TaskAdd.this);

				// ��ͼƬ����������Ŀ¼��
				File fromFile = new File(originalUri);
				File toFile = new File(imagePath);
				Utils.copyFile(fromFile, toFile, true);

				// ��������ͼ
				Utils.getThumbnail(imagePath, thumbnailUri);

				// �ж��ļ��Ƿ����,������ֱ������
				File thumbFile = new File(thumbnailUri);
				if (!thumbFile.exists()) {
					return;
				}

				String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
				mediaList.add(new Media(Utils.MEDIA_TYPE_IMAGE, imageName, imagePath));
				// ��ʾ��ѡ����Ƭ
				Uri thumbUri = Uri.fromFile(thumbFile);
				int mediaID1 = mediaIndex++;
				loadMedia(imageContainer, mediaID1, getThumbnailFromUri(thumbUri), thumbUri,
						TYPE_IMAGE);
				// �洢mediaId��imageOriginPath��ӳ��
				index_originalPath_Map.put(mediaID1, originalUri);
				// �洢mediaId��thumbnailUri��ӳ��
				index_path_Map.put(mediaID1, thumbnailUri);
			}

			break;
		// ��Աѡ��
		case LocalConstant.TASK_POD_SELECT_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				int type = data.getExtras().getInt("type");
				List<Node> selectedPerson = (List<Node>) data.getSerializableExtra("data");
				String name = "";
				if (selectedPerson == null || selectedPerson.size() == 0) {
					showLongToast("δѡ���κ���");
				}
				if (selectedPerson != null && selectedPerson.size() > 0) {
					for (Node node : selectedPerson) {
						name += node.getName() + "/";
					}
				}
				// pod
				if (type == 1) {
					etPod.setText(name);
					lsSelectedPod = selectedPerson;
				}
				// receiver
				else if (type == 2) {
					etReceiver.setText(name);
					lsSelectedReceiver = selectedPerson;
				}
			}
			break;

		default:
			break;
		}
	}

	/**
	 * �����ϴ���ť��Ӧ�¼�����
	 */
	private void initAttachPickBtn() {
		successCounter = 0;
		AlertDialog.Builder builder = new AlertDialog.Builder(TaskAdd.this);
		builder.setTitle("ѡ�񸽼�����").setItems(new String[] { "ͼ��", "����"/* , "����" */ },
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int which) {
						switch (which) {
						case 0:
							Utils.showShortToast(TaskAdd.this, "ͼ��");
							Intent getAlbum = new Intent(Intent.ACTION_PICK,
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
							// ����Pictures����Type�趨Ϊimage
							getAlbum.setType("image/*");
							// getAlbum.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(getAlbum,
									LocalConstant.SELECT_IMAGE_REQUEST_CODE);
							break;
						case 1:
							Utils.showShortToast(TaskAdd.this, "����");
							// ����
							Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
							imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, taskID,
									TaskAdd.this);
							Uri imageUri = Uri.fromFile(new File(imagePath));
							// ָ����Ƭ����·��
							imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
							startActivityForResult(imageIntent,
									LocalConstant.CAPTURE_IMAGE_REQUEST_CODE);
							break;
						case 2:
							Utils.showShortToast(TaskAdd.this, "����");
							// ����
							Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
							videoPath = Utils.produceAttachDir(Utils.MEDIA_TYPE_VIDEO, taskID,
									TaskAdd.this);
							Uri videoUri = Uri.fromFile(new File(videoPath));
							// ָ����Ƶ�洢·��
							videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
							// ָ����Ƶ��ʱ�����ƣ�30s��
							videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30000);
							startActivityForResult(videoIntent,
									LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);

							break;

						default:
							break;
						}

					}
				});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	/**
	 * ����ý�壨ͼƬ|��Ƶ��
	 * 
	 * @param viewContainer
	 * @param thumbnail
	 * @param uri
	 * @param MediaType
	 */
	public void loadMedia(nercms.schedule.layout.FixedGridLayout viewContainer, int mediaId,
			Bitmap thumbnail, final Uri uri, final int MediaType) {
		// WeiHao �������չʾ���ֲ��ɼ�����δ�ɼ�
		if (showAttachLayout.getVisibility() == View.GONE) {
			showAttachLayout.setVisibility(View.VISIBLE);
		}
		// ��ͼƬ����ͼ��ӵ�����ͼ�б������½���ɺ����
		bitmapList.add(thumbnail);
		final ImageView imageView = CreateImgView(this, thumbnail, IMG_WIDTH, IMG_HEIGHT);
		setImageviewListener(uri, imageView, MediaType);

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
		// com.nercms.workoa.layout.FixedGridLayout.LayoutParams params = new
		// com.nercms.workoa.layout.FixedGridLayout.LayoutParams(width,
		// height);
		rl.setLayoutParams(
				new nercms.schedule.layout.FixedGridLayout.LayoutParams(IMG_WIDTH, IMG_HEIGHT));
		rl.setPadding(2, 2, 2, 2);
		// rl.setBackgroundResource(color.white);
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
		nercms.schedule.layout.FixedGridLayout.LayoutParams params = new nercms.schedule.layout.FixedGridLayout.LayoutParams(
				width, height);
		imageView.setLayoutParams(params);
		imageView.setScaleType(ScaleType.CENTER_CROP);
		// imageView.setPadding(5, 5, 5, 5);
		// imageView.setBackgroundColor(R.drawable.imageview_background);
		// imageView.setBackgroundResource(R.drawable.imageview_background);
		return imageView;
	}

	/**
	 * ������صļ����¼�
	 * 
	 * @param uri
	 * @param imageView
	 */
	public void setImageviewListener(final Uri uri, final ImageView imageView,
			final int MediaType) {
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
				imageView.setEnabled(false);
				switch (MediaType) {
				case TYPE_IMAGE:
					// �����ʾ��ͼ
					showImageDialog(imageView, uri);
					break;
				case TYPE_VIDEO:
					// ���������Ƶ
					Intent mIntent = new Intent(Intent.ACTION_VIEW);
					mIntent.setDataAndType(uri, "video/mp4");
					startActivity(mIntent);
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

	/**
	 * ɾ��ͼƬ����Ƶ�Ի���
	 * 
	 * @param rl
	 *            ɾ��ͼƬ����Ƶ���ڵĲ���
	 * @param MediaType
	 *            ý���ļ����� 1-ͼƬ 2-��Ƶ
	 */
	private void showDeleteMediaDialog(final RelativeLayout rl, final int MediaType) {
		AlertDialog.Builder builder = new Builder(this);
		// ���ñ���
		builder.setTitle("��ʾ");
		// ������ʾ����
		builder.setMessage("ȷ��ɾ��" + (MediaType == TYPE_IMAGE ? "ͼƬ" : "��Ƶ") + "��");
		// ����ȷ����ť
		builder.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (MediaType == TYPE_IMAGE) {
					// ��ͼƬ��ͼƬ�б���ɾ��
					imageContainer.removeView(rl);
				} else if (MediaType == TYPE_VIDEO) {
					// ����Ƶ����Ƶ�б���ɾ��
					imageContainer.removeView(rl);
				}
				// ���ֻ��е�ý���ļ�ɾ��
				Integer index = rl.getId();
				// ɾ������ͼ
				if (index_path_Map.containsKey(index)) {
					String path = index_path_Map.get(index);
					index_path_Map.remove(index);
					Utils.deleteMedia(path);
				}
				// ɾ��ԭý���ļ����£�
				if (index_originalPath_Map.containsKey(index)) {
					String path = index_originalPath_Map.get(index);
					index_originalPath_Map.remove(index);
					Utils.deleteMedia(path);
				}

				// WeiHao ý���ļ�������������ж��Ƿ���ɾ�ո�����������޸��������ظÿؼ�
				if (--mediaIndex == 0) {
					showAttachLayout.setVisibility(View.GONE);
				}

				// WeiHao ��ý���б��Ƴ����Ƴ��ø���
				mediaList.remove(index);

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

	// ------------------------------------------------------------------------------------

	// ѡ���ֹʱ�� �������ս����ж�
	private void showDateTimePicker() {
		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DATE);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		// ��Ӵ�С���·ݲ�����ת��Ϊlist,����֮����ж�
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };
		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);
		dialog = new Dialog(this);
		dialog.setTitle("��ѡ��������ʱ��");
		// �ҵ�dialog�Ĳ����ļ�
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.date_time_select, null);
		// ��
		final WheelView wv_year = (WheelView) view.findViewById(R.id.year);
		wv_year.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));// ����"��"����ʾ����
		wv_year.setCyclic(true);// ��ѭ������
		wv_year.setLabel("��");// �������
		wv_year.setCurrentItem(year - START_YEAR);// ��ʼ��ʱ��ʾ������
		// ��
		final WheelView wv_month = (WheelView) view.findViewById(R.id.month);
		wv_month.setAdapter(new NumericWheelAdapter(1, 12));
		wv_month.setCyclic(true);
		wv_month.setLabel("��");
		wv_month.setCurrentItem(month);
		// ��
		final WheelView wv_day = (WheelView) view.findViewById(R.id.day);
		wv_day.setCyclic(true);
		// �жϴ�С�¼��Ƿ�����,����ȷ��"��"������
		if (list_big.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 31));
		} else if (list_little.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 30));
		} else {
			// ����
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				wv_day.setAdapter(new NumericWheelAdapter(1, 29));
			else
				wv_day.setAdapter(new NumericWheelAdapter(1, 28));
		}
		wv_day.setLabel("��");
		wv_day.setCurrentItem(day - 1);
		// ʱ
		final WheelView wv_hours = (WheelView) view.findViewById(R.id.hour);
		wv_hours.setAdapter(new NumericWheelAdapter(0, 23));
		wv_hours.setCyclic(true);
		wv_hours.setCurrentItem(hour);
		// ��
		final WheelView wv_mins = (WheelView) view.findViewById(R.id.mins);
		wv_mins.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
		wv_mins.setCyclic(true);
		wv_mins.setCurrentItem(minute);
		// ���"��"����
		OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int year_num = newValue + START_YEAR;
				// �жϴ�С�¼��Ƿ�����,����ȷ��"��"������
				if (list_big.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(wv_month.getCurrentItem() + 1))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if ((year_num % 4 == 0 && year_num % 100 != 0) || year_num % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		// ���"��"����
		OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int month_num = newValue + 1;
				// �жϴ�С�¼��Ƿ�����,����ȷ��"��"������
				if (list_big.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 31));
				} else if (list_little.contains(String.valueOf(month_num))) {
					wv_day.setAdapter(new NumericWheelAdapter(1, 30));
				} else {
					if (((wv_year.getCurrentItem() + START_YEAR) % 4 == 0
							&& (wv_year.getCurrentItem() + START_YEAR) % 100 != 0)
							|| (wv_year.getCurrentItem() + START_YEAR) % 400 == 0)
						wv_day.setAdapter(new NumericWheelAdapter(1, 29));
					else
						wv_day.setAdapter(new NumericWheelAdapter(1, 28));
				}
			}
		};
		wv_year.addChangingListener(wheelListener_year);
		wv_month.addChangingListener(wheelListener_month);
		// ������Ļ�ܶ���ָ��ѡ��������Ĵ�С
		int textSize = 0;

		textSize = 15;

		wv_day.TEXT_SIZE = textSize;
		wv_hours.TEXT_SIZE = textSize;
		wv_mins.TEXT_SIZE = textSize;
		wv_month.TEXT_SIZE = textSize;
		wv_year.TEXT_SIZE = textSize;

		Button btn_sure = (Button) view.findViewById(R.id.btn_datetime_sure);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_datetime_cancel);
		// ȷ����ť
		btn_sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// ����Ǹ���,����ʾΪ"02"����ʽ
				String parten = "00";
				DecimalFormat decimal = new DecimalFormat(parten);
				// �������ڵ���ʾ
				String _nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")// ����ʱ��
						.format(new Date(System.currentTimeMillis()));

				String currentSelectTime = (wv_year.getCurrentItem() + START_YEAR) + "-"
						+ decimal.format((wv_month.getCurrentItem() + 1)) + "-"
						+ decimal.format((wv_day.getCurrentItem() + 1)) + " "
						+ decimal.format(wv_hours.getCurrentItem()) + ":"
						+ decimal.format(wv_mins.getCurrentItem()) + ":00";

				DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				Date nowTime = new Date();
				Date selectTime = new Date();
				try {
					nowTime = df.parse(_nowTime);
					selectTime = df.parse(currentSelectTime);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				long mins = (selectTime.getTime() - nowTime.getTime()) / 6000;
				if (mins < 3) {
					Utils.showShortToast(TaskAdd.this, "ѡ��ʱ��С�ڵ�ǰʱ�䣬������ѡ��");
				} else {
					etEndTime.setText(currentSelectTime);
					dialog.dismiss();
				}
			}
		});
		// ȡ��
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		// ����dialog�Ĳ���,����ʾ
		dialog.setContentView(view);
		dialog.show();
	}

	// 2014-5-16 WeiHao ����
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

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			backDetect();
		}
		return false;
	}

	@Override
	protected void onDestroy() {
//		// ע��handler
//		MessageHandlerManager.getInstance().unregister(Constant.CREATE_AFFAIR_REQUEST_SUCCESS,
//				Contants.METHOD_AFFAIRS_ADDAFFAIR);
//		MessageHandlerManager.getInstance().unregister(Constant.CREATE_AFFAIR_REQUEST_FAIL,
//				Contants.METHOD_AFFAIRS_ADDAFFAIR);
//		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_FAIL, "TaskAdd");
//		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_SUCCESS, "TaskAdd");

		System.out.println("TaskAdd OnDestroy");
		// ����ͼƬ�ڴ�
		for (int i = 0; i < bitmapList.size(); i++) {
			bitmapList.get(i).recycle();
			if (bitmapList.get(i) != null)
				bitmapList.get(i).recycle();
		}

		super.onDestroy();
	}

}
