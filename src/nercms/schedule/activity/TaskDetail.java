package nercms.schedule.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.dateSelect.NumericWheelAdapter;
import nercms.schedule.dateSelect.OnWheelChangedListener;
import nercms.schedule.dateSelect.WheelView;
import nercms.schedule.layout.FixedGridLayout;
import nercms.schedule.layout.RoundAngleImageView;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import android.R.anim;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore.Images.Thumbnails;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.AffairDao;
import android.wxapp.service.dao.AttachmentDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.dao.PersonOnDutyDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.affair.CreateTaskRequestAttachment;
import android.wxapp.service.jerry.model.affair.CreateTaskRequestIds;
import android.wxapp.service.jerry.model.affair.QueryAffairInfoResponse;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.jerry.model.person.GetPersonInfoResponse;
import android.wxapp.service.model.AffairAttachModel;
import android.wxapp.service.model.AffairModel;
import android.wxapp.service.model.PersonOnDutyModel;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.HttpDownloadTask;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class TaskDetail extends BaseActivity {

	// ȫ�ֱ���
	private WebRequestManager webRequestManager;
	private DAOFactory daoFactory = DAOFactory.getInstance();
	private AffairDao affairDao;
	private PersonDao personDao;
	private PersonOnDutyDao podDao;
	private AttachmentDao attachDao;

	private Handler handler;

	private QueryAffairInfoResponse task;
	// private PersonOnDutyModel pod;

	// �ؼ�
	private EditText task_title;// ��������
	private EditText starter;// ������
	private EditText participator;// ������
	private EditText personOnDuty;// ������
	private ImageButton btn_calendar;
	private EditText end_time; // ��ֹʱ�䣨���޸ģ�
	private EditText content;// ��������
	private LinearLayout attachLayout;
	private FixedGridLayout mediaContainer;

	// ʱ��ؼ����
	private Dialog dialog;
	private static int START_YEAR = 2010, END_YEAR = 2020;

	// ͼƬ��ʾ�Ŀ��
	private final int IMG_WIDTH = 130;
	private final int IMG_HEIGHT = 130;
	// ÿ����ʾ����ͼ��Ŀ
	private final int NUMPERROW = 3;
	// ��ʾ��ͼ�Ի���
	Dialog imageDialog;
	// ͼƬ��ʾ����
	private LinearLayout.LayoutParams layoutParams = null;
	// ������ʾͼƬ�����ļ���
	private ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();
	// ������
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();

	private String path;

	// ������ͣ�1-��������2-��������
	private int entranceType = -1;
	// ���״̬�� 1-�����У�δ��ɣ���2-����ɣ�3-���ӳ�
	private int entranceStatus = -1;

	// ����ID
	private String taskID;
	// ����������
	private String podName = "";
	// ����������
	private String pName = "";
	// ����������
	private String sponsorName;
	// ����ID
	private String userID;
	// ���񸽼��б�
	// private ArrayList<AffairAttachModel> taskAttackList;
	private List<CreateTaskRequestAttachment> taskAttackList;

	DisplayImageOptions options;

	private String videoThumbnailDir;

	private MenuItem fbItem;
	private SubMenu subMenu;
	private MenuItem menuItem;
	// private boolean isMySponse = false;
	private boolean isDoing = false;

	// ʱ��ѡ��ؼ���ѡ��ʱ���ȫ�ֱ���
	private String currentSelectTime;
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_detail);

		Log.v("", "onCreate");

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
				.build();// �������

		userID = MySharedPreference.get(TaskDetail.this, MySharedPreference.USER_ID, "");
		webRequestManager = new WebRequestManager(AppApplication.getInstance(), TaskDetail.this);

		// ����������洫�����������ͺ�����״̬��ʼ����ڱ���������ID
		// ������ͣ�1-��������2-��������
		entranceType = getIntent().getIntExtra("type", -1);
		// ���״̬�� 1-�����У�δ��ɣ���2-����ɣ�3-���ӳ�
		entranceStatus = getIntent().getIntExtra("status", -1);
		taskID = getIntent().getExtras().getString("id");

		// ׼������
		initData();
		initActionBar();
		// ��ʼ���ؼ�
		initView();

		// TODO
		initHandler();

	}

	private void initActionBar() {
		Log.v("", "initActionBar");
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("��������");
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.v("", "onPrepareOptionsMenu");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.v("", "onCreateOptionsMenu");
		// ������ť
		// 2014-8-9 ע��
		// �޸��߼�������״̬���������ʾ������ť
		// 1-�����У��ɷ��ͷ������ɲ���
		// 2-����ɣ�ֻ�ܲ鿴����
		// 3-�����󣺿ɷ��ͷ������ɲ���
		// if (entranceStatus == 1) {

		fbItem = menu.add(0, 1, 0, "����");
		fbItem.setIcon(R.drawable.ic_action_chat);
		fbItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		if (entranceType == 1 && entranceStatus != 2) {
			// ������ť
			subMenu = menu.addSubMenu("����");
			// subMenu.add(0, 2, 0,
			// "��ֹʱ���޸�").setIcon(R.drawable.ofm_task_modify);
			subMenu.add(0, 3, 0, "���������").setIcon(R.drawable.ofm_task_end);
			if (entranceType == 1 && entranceStatus == 1)
				subMenu.add(0, 4, 0, "�޸�������Ϣ").setIcon(android.R.drawable.ic_menu_edit);

			menuItem = subMenu.getItem();
			menuItem.setIcon(R.drawable.ic_action_overflow);
			menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}
		// }
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		case 1:
			// ��ת����������
			Intent intent = new Intent(TaskDetail.this, ChatDetail.class);
			intent.putExtra("entrance_type", 2); // ��Ϣ������ڣ�����
			intent.putExtra("task_id", taskID);
			intent.putExtra("task_status", entranceStatus);
			startActivity(intent);
			break;
		case 2:
			// ����ʱ��ѡ����
			showDateTimePicker();
			break;
		case 3:
			new AlertDialog.Builder(TaskDetail.this).setTitle("�������").setMessage("ȷ������������Ϊ���?")
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// ������������
							webRequestManager.endAffair(taskID + "");

						}
					}).setNegativeButton("ȡ��", null).create().show();
			break;
		// �޸�������Ϣ
		case 4:
			Intent intent2 = new Intent(TaskDetail.this, TaskAdd.class);
			intent2.putExtra("data", affairDao.getAffairInfoByAid(taskID));
			TaskDetail.this.startActivityForResult(intent2, REQUESTCODE);
			break;

		default:
			break;
		}
		return true;
	}

	private final int REQUESTCODE = 100;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case REQUESTCODE:
				// ׼������
				initData();
				// ��ʼ���ؼ�
				initView();
				break;

			default:
				break;
			}
		}
	}

	private void initData() {
		Log.v("", "initData");

		affairDao = daoFactory.getAffairDao(TaskDetail.this);
		task = affairDao.getAffairInfoByAid(taskID);
		// �޸�����Ϊ�Ѷ�
		affairDao.updateAffairIsRead(taskID);

		personDao = daoFactory.getPersonDao(TaskDetail.this);
		Map<String, List<CreateTaskRequestIds>> ids = affairDao.getPersonIdByAffairId(taskID);
		// ����������
		List<CreateTaskRequestIds> pods = ids.get("1");
		for (CreateTaskRequestIds createTaskRequestIds : pods) {
			GetPersonInfoResponse temp = personDao.getPersonInfo(createTaskRequestIds.getRid());
			if (temp != null)
				podName += personDao.getPersonInfo(createTaskRequestIds.getRid()).getN() + "/";
			else
				continue;
		}
		// ����������
		List<CreateTaskRequestIds> rids = ids.get("2");
		// �Գ��������ݽ������ɲ���ʾ
		for (CreateTaskRequestIds createTaskRequestIds : rids) {
			GetPersonInfoResponse temp = personDao.getPersonInfo(createTaskRequestIds.getRid());
			if (temp != null)
				pName += personDao.getPersonInfo(createTaskRequestIds.getRid()).getN() + "/";
			else
				continue;
		}
		sponsorName = personDao.getPersonInfo(task.getSid()).getN();
		taskAttackList = task.getAtt();
		// �ж��Ƿ�������ɵ����������������
		if (task.getCt() != null && !task.getCt().isEmpty()) {
			// ���ط����Ͳ�����ť�������鿴
			isDoing = true;
		}

	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.END_TASK_REQUEST_SUCCESS:
					Utils.showShortToast(TaskDetail.this, "�����ѱ��Ϊ�����");
					TaskDetail.this.finish();
					break;
				case Constant.FILE_DOWNLOAD_SUCCESS:
					// updateMedia(path.toString() + (String) msg.obj);
					// ������Ƶ��������ͼ��ʾ
					Log.v("initHandler", "�յ����������Ϣ��׼��������Ƶ����ͼ");
					updateVideo((String) msg.obj);
					break;
				case Constant.MODIFY_TASK_REQUEST_SUCCESS:
					end_time.setText(currentSelectTime);
					end_time.setTextColor(getResources().getColor(R.color.red));
					Utils.showShortToast(TaskDetail.this, "�����ֹʱ�����޸�");
					break;
				case Constant.QUERY_TASK_INFO_REQUEST_FAIL:
					dismissProgressDialog();
					showLongToast("������룺" + ((NormalServerResponse) msg.obj).getEc());
					Log.e(getClass().getName(), "������룺" + ((NormalServerResponse) msg.obj).getEc());
					break;
				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler, Constant.FILE_DOWNLOAD_SUCCESS,
				"TaskDetail");
		MessageHandlerManager.getInstance().register(handler, Constant.END_TASK_REQUEST_SUCCESS,
				"TaskDetail");
		MessageHandlerManager.getInstance().register(handler, Constant.MODIFY_TASK_REQUEST_SUCCESS,
				"TaskDetail");
		MessageHandlerManager.getInstance().register(handler, Constant.END_TASK_REQUEST_SUCCESS,
				Contants.METHOD_AFFAIRS_END_TASK);
		MessageHandlerManager.getInstance().register(handler,
				Constant.QUERY_TASK_INFO_REQUEST_SUCCESS, Contants.METHOD_AFFAIRS_QUERY_INFO);
		MessageHandlerManager.getInstance().register(handler, Constant.QUERY_TASK_INFO_REQUEST_FAIL,
				Contants.METHOD_AFFAIRS_QUERY_INFO);
	}

	private void initView() {
		task_title = (EditText) findViewById(R.id.td_title);
		task_title.setText(task.getTopic());
		starter = (EditText) findViewById(R.id.td_starter);
		starter.setText(sponsorName);
		personOnDuty = (EditText) findViewById(R.id.personOnDuty);
		personOnDuty.setText(podName);
		participator = (EditText) findViewById(R.id.td_participator);
		participator.setText(pName);
		end_time = (EditText) findViewById(R.id.td_deadline);
		end_time.setText(Utils.formatDateMs(task.getEt()));
		btn_calendar = (ImageButton) findViewById(R.id.td_btn_deadline);
		btn_calendar.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TaskAdd.showDateTimePicker();
			}
		});
		btn_calendar.setVisibility(View.GONE);

		content = (EditText) findViewById(R.id.td_content);
		content.setText(task.getD());

		attachLayout = (LinearLayout) findViewById(R.id.td_showAttathLayout);
		if (taskAttackList.size() == 0) {
			attachLayout.setVisibility(View.GONE);
			return;
		}
		attachLayout.setVisibility(View.VISIBLE);
		mediaContainer = (FixedGridLayout) findViewById(R.id.td_attachContainer);
		mediaContainer.setCellWidth(IMG_WIDTH);
		mediaContainer.setCellHeight(IMG_HEIGHT);
		mediaContainer.setNumPerRow(NUMPERROW);

		// ��ʼ��ͼƬ��ʾ����
		layoutParams = new LinearLayout.LayoutParams(IMG_WIDTH, IMG_HEIGHT);
		layoutParams.setMargins(0, 10, 0, 10);

		File sdcardDir = Environment.getExternalStorageDirectory();
		path = sdcardDir.getPath() + "/nercms-Schedule/Attachments/";
		videoThumbnailDir = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail/";

		String mediaName;
		int mediaType;

		for (int i = 0; i < taskAttackList.size(); i++) {
			mediaName = taskAttackList.get(i).getU();
			mediaName = mediaName.substring(mediaName.lastIndexOf("/") + 1);
			mediaType = Integer.parseInt(taskAttackList.get(i).getAt());
			final String mediaPath = path.toString() + mediaName;
			if (mediaPath != null && !mediaPath.equalsIgnoreCase("")) {

				// 2014-6-24 WeiHao ע�ͣ������µ�ͼƬ����ģʽ

				// if (mediaType == Utils.MEDIA_TYPE_IMAGE) {
				// addImage(mediaPath);
				// } else if (mediaType == Utils.MEDIA_TYPE_VIDEO) {
				// addVideo(mediaPath);
				// }
				//
				// // �ж��ļ��Ƿ���ڣ��������������ļ�����������
				// if (!new File(mediaPath).exists()) {
				// String downUrl = LocalConstant.FILE_SERVER_ATTACH_URL
				// + File.separator + mediaName;
				// new HttpDownloadTask(TaskDetail.this).execute(downUrl,
				// "/nercms-Schedule/Attachments/", mediaName);
				// }

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
						new HttpDownloadTask(TaskDetail.this).execute(downUrl,
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

	// 2014-6-27 WeiHao
	/** ������ɺ󣬸�����Ƶ��ʾ */
	private void updateVideo(final String mediaName) {
		for (ImageView imageView : imageViewList) {
			if (imageView.getTag() != null) {
				// if (imageView.getTag().equals((Object) path)) {
				// // ��ȡ��Ƶ����ͼ
				// Bitmap pic = ThumbnailUtils.createVideoThumbnail(path,
				// Thumbnails.MICRO_KIND);
				// if (pic != null) {
				// // ��ͼƬ����ͼ��ӵ�����ͼ�б������½���ɺ����
				// imageCache.put(path, new SoftReference<Bitmap>(pic));
				// // ��������ͼ
				// imageView.setImageBitmap(pic);
				// }
				// }

				if (imageView.getTag().equals(mediaName)) {

					// �ж���Ƶ����ͼ�Ƿ���ڣ�����������������ͼ
					String thumbnailPath = videoThumbnailDir
							+ mediaName.substring(0, mediaName.indexOf(".")) + ".jpg";
					Log.v("updateVideo", "������Ƶ����ͼ");
					Utils.saveBitmap(ThumbnailUtils.createVideoThumbnail(
							Environment.getExternalStorageDirectory().getPath()
									+ "/nercms-Schedule/Attachments/" + mediaName,
							Thumbnails.MINI_KIND), thumbnailPath);

					addImage(imageView, thumbnailPath, true);
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
			mediaContainer.addView(imageView);
			// ���ñ���Ϊ��ɫ
			imageView.setBackgroundResource(R.drawable.no_picture);
		}

	}

	/** ��ʾ��ͼ�Ի��� */
	private void showImageDialog(final ImageView imageView, final String path) {
		// ��ȡ�Ի��򲼾ֲ�ʵ����
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.image_dialog, null);
		// ����Ի���
		imageDialog = new Dialog(this, R.style.imageDialog);
		imageDialog.setContentView(view);
		// ���ͼƬ
		ImageView dialogImageView = (ImageView) view.findViewById(R.id.imageImageView);
		// ��ȡͼƬ
		try {
			final Bitmap pic = BitmapFactory.decodeFile(path);
			// ��ͼƬ����ͼ���ص�ImageView
			dialogImageView.setImageBitmap(pic);
			// ΪͼƬ���õ����¼�
			dialogImageView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					imageDialog.dismiss();
					imageDialog = null;
					// ����ͼ�����¼��ָ�
					imageView.setEnabled(true);
					// ͼƬ����
					pic.recycle();
				}
			});
			// ��ʾ�Ի���
			imageDialog.show();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** ͨ��ͼƬ��·����ȡͼƬ������ͼ */
	public Bitmap getThumbnailFromPath(final String path) {
		// ͼƬ������
		InputStream input = null;
		try {
			input = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// ͼƬ����ѡ��
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;
		// ��ȡͼƬ����ͼ������
		return BitmapFactory.decodeStream(input, null, opts);
	}

	/**
	 * ����ͼƬ�ڴ�
	 */
	private void freeBitmap() {
		if (imageCache.isEmpty()) {
			return;
		}

		for (SoftReference<Bitmap> bitmap : imageCache.values()) {
			if (bitmap.get() != null && !bitmap.get().isRecycled()) {
				bitmap.get().recycle();
				bitmap = null;
			}
		}
		imageCache.clear();
		imageCache = null;
		System.gc();
	}

	@Override
	protected void onDestroy() {
//		MessageHandlerManager.getInstance().unregister(Constant.FILE_DOWNLOAD_SUCCESS,
//				"TaskDetail");
//		MessageHandlerManager.getInstance().unregister(Constant.END_TASK_REQUEST_SUCCESS,
//				"TaskDetail");
//		MessageHandlerManager.getInstance().unregister(Constant.MODIFY_TASK_REQUEST_SUCCESS,
//				"TaskDetail");
//		MessageHandlerManager.getInstance().unregister(Constant.END_TASK_REQUEST_SUCCESS,
//				Contants.METHOD_AFFAIRS_END_TASK);
//		MessageHandlerManager.getInstance().unregister(Constant.QUERY_TASK_INFO_REQUEST_SUCCESS,
//				Contants.METHOD_AFFAIRS_QUERY_INFO);
//		MessageHandlerManager.getInstance().unregister(Constant.QUERY_TASK_INFO_REQUEST_FAIL,
//				Contants.METHOD_AFFAIRS_QUERY_INFO);
		freeBitmap();
		super.onDestroy();
	}

	// ѡ���ֹʱ�� �������ս����ж�
	private void showDateTimePicker() {
		String originalEndTime = task.getEt();
		int year = Integer.parseInt(originalEndTime.substring(0, 4));
		int month = Integer.parseInt(originalEndTime.substring(5, 7));
		int day = Integer.parseInt(originalEndTime.substring(8, 10));
		int hour = Integer.parseInt(originalEndTime.substring(11, 13));
		int minute = Integer.parseInt(originalEndTime.substring(14, 16));

		// Calendar calendar = Calendar.getInstance();
		// int year = calendar.get(Calendar.YEAR);
		// int month = calendar.get(Calendar.MONTH);
		// int day = calendar.get(Calendar.DATE);
		// int hour = calendar.get(Calendar.HOUR_OF_DAY);
		// int minute = calendar.get(Calendar.MINUTE);
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
		wv_month.setCurrentItem(month - 1);
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

				currentSelectTime = (wv_year.getCurrentItem() + START_YEAR) + "-"
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
					Toast.makeText(TaskDetail.this, "ѡ��ʱ��С�ڵ�ǰʱ�䣬������ѡ��", Toast.LENGTH_SHORT).show();
				} else {
					dialog.dismiss();
					new AlertDialog.Builder(TaskDetail.this).setTitle("�޸�����")
							.setMessage("ȷ��������Ľ�ֹʱ��� " + end_time.getText().toString() + " �޸�Ϊ "
									+ currentSelectTime + " ��")
							.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {

							// ������������޸�����
							// //////////TODO
							webRequestManager.modifyAffairEndTime("", taskID, currentSelectTime);
						}

					}).setNegativeButton("ȡ��", null).create().show();

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

}
