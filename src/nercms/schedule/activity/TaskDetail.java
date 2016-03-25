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

	// 全局变量
	private WebRequestManager webRequestManager;
	private DAOFactory daoFactory = DAOFactory.getInstance();
	private AffairDao affairDao;
	private PersonDao personDao;
	private PersonOnDutyDao podDao;
	private AttachmentDao attachDao;

	private Handler handler;

	private QueryAffairInfoResponse task;
	// private PersonOnDutyModel pod;

	// 控件
	private EditText task_title;// 任务主题
	private EditText starter;// 发起人
	private EditText participator;// 抄送人
	private EditText personOnDuty;// 责任人
	private ImageButton btn_calendar;
	private EditText end_time; // 截止时间（可修改）
	private EditText content;// 任务内容
	private LinearLayout attachLayout;
	private FixedGridLayout mediaContainer;

	// 时间控件相关
	private Dialog dialog;
	private static int START_YEAR = 2010, END_YEAR = 2020;

	// 图片显示的宽高
	private final int IMG_WIDTH = 130;
	private final int IMG_HEIGHT = 130;
	// 每行显示缩略图数目
	private final int NUMPERROW = 3;
	// 显示大图对话框
	Dialog imageDialog;
	// 图片显示布局
	private LinearLayout.LayoutParams layoutParams = null;
	// 附件显示图片容器的集合
	private ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();
	// 软引用
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();

	private String path;

	// 入口类型：1-发送任务；2-接收任务
	private int entranceType = -1;
	// 入口状态： 1-进行中（未完成）；2-已完成；3-已延迟
	private int entranceStatus = -1;

	// 任务ID
	private String taskID;
	// 责任人姓名
	private String podName = "";
	// 抄送人姓名
	private String pName = "";
	// 发起人姓名
	private String sponsorName;
	// 本人ID
	private String userID;
	// 任务附件列表
	// private ArrayList<AffairAttachModel> taskAttackList;
	private List<CreateTaskRequestAttachment> taskAttackList;

	DisplayImageOptions options;

	private String videoThumbnailDir;

	private MenuItem fbItem;
	private SubMenu subMenu;
	private MenuItem menuItem;
	// private boolean isMySponse = false;
	private boolean isDoing = false;

	// 时间选择控件，选中时间的全局变量
	private String currentSelectTime;
	

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_detail);

		Log.v("", "onCreate");

		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.no_picture) // 设置图片在下载期间显示的图片
				.showImageForEmptyUri(R.drawable.no_picture)// 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.no_picture) // 设置图片加载/解码过程中错误时候显示的图片
				.cacheInMemory(true)// 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true)// 设置下载的图片是否缓存在SD卡中
				.considerExifParams(true) // 是否考虑JPEG图像EXIF参数（旋转，翻转）
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
				.bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型//
				// .delayBeforeLoading(int delayInMillis)//int
				// delayInMillis为你设置的下载前的延迟时间
				// 设置图片加入缓存前，对bitmap进行设置
				// .preProcessor(BitmapProcessor preProcessor)
				.resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
				.displayer(new RoundedBitmapDisplayer(20))// 是否设置为圆角，弧度为多少
				.displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
				.build();// 构建完成

		userID = MySharedPreference.get(TaskDetail.this, MySharedPreference.USER_ID, "");
		webRequestManager = new WebRequestManager(AppApplication.getInstance(), TaskDetail.this);

		// 根据任务界面传来的任务类型和任务状态初始化入口变量，任务ID
		// 入口类型：1-发起任务；2-接收任务
		entranceType = getIntent().getIntExtra("type", -1);
		// 入口状态： 1-进行中（未完成）；2-已完成；3-已延迟
		entranceStatus = getIntent().getIntExtra("status", -1);
		taskID = getIntent().getExtras().getString("id");

		// 准备数据
		initData();
		initActionBar();
		// 初始化控件
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
		getSupportActionBar().setTitle("任务详情");
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.v("", "onPrepareOptionsMenu");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.v("", "onCreateOptionsMenu");
		// 反馈按钮
		// 2014-8-9 注释
		// 修改逻辑：三种状态的任务均显示反馈按钮
		// 1-进行中：可发送反馈，可操作
		// 2-已完成：只能查看反馈
		// 3-已延误：可发送反馈，可操作
		// if (entranceStatus == 1) {

		fbItem = menu.add(0, 1, 0, "反馈");
		fbItem.setIcon(R.drawable.ic_action_chat);
		fbItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		if (entranceType == 1 && entranceStatus != 2) {
			// 操作按钮
			subMenu = menu.addSubMenu("操作");
			// subMenu.add(0, 2, 0,
			// "截止时间修改").setIcon(R.drawable.ofm_task_modify);
			subMenu.add(0, 3, 0, "任务置完成").setIcon(R.drawable.ofm_task_end);
			if (entranceType == 1 && entranceStatus == 1)
				subMenu.add(0, 4, 0, "修改任务信息").setIcon(android.R.drawable.ic_menu_edit);

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
			// 跳转到反馈界面
			Intent intent = new Intent(TaskDetail.this, ChatDetail.class);
			intent.putExtra("entrance_type", 2); // 消息界面入口：反馈
			intent.putExtra("task_id", taskID);
			intent.putExtra("task_status", entranceStatus);
			startActivity(intent);
			break;
		case 2:
			// 弹出时间选择器
			showDateTimePicker();
			break;
		case 3:
			new AlertDialog.Builder(TaskDetail.this).setTitle("任务完成").setMessage("确定将该任务置为完成?")
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 发送网络请求
							webRequestManager.endAffair(taskID + "");

						}
					}).setNegativeButton("取消", null).create().show();
			break;
		// 修改任务信息
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
				// 准备数据
				initData();
				// 初始化控件
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
		// 修改任务为已读
		affairDao.updateAffairIsRead(taskID);

		personDao = daoFactory.getPersonDao(TaskDetail.this);
		Map<String, List<CreateTaskRequestIds>> ids = affairDao.getPersonIdByAffairId(taskID);
		// 负责人数据
		List<CreateTaskRequestIds> pods = ids.get("1");
		for (CreateTaskRequestIds createTaskRequestIds : pods) {
			GetPersonInfoResponse temp = personDao.getPersonInfo(createTaskRequestIds.getRid());
			if (temp != null)
				podName += personDao.getPersonInfo(createTaskRequestIds.getRid()).getN() + "/";
			else
				continue;
		}
		// 抄送人数据
		List<CreateTaskRequestIds> rids = ids.get("2");
		// 对抄送人数据进行生成并显示
		for (CreateTaskRequestIds createTaskRequestIds : rids) {
			GetPersonInfoResponse temp = personDao.getPersonInfo(createTaskRequestIds.getRid());
			if (temp != null)
				pName += personDao.getPersonInfo(createTaskRequestIds.getRid()).getN() + "/";
			else
				continue;
		}
		sponsorName = personDao.getPersonInfo(task.getSid()).getN();
		taskAttackList = task.getAtt();
		// 判断是否是已完成的任务和已延误任务
		if (task.getCt() != null && !task.getCt().isEmpty()) {
			// 隐藏反馈和操作按钮，仅供查看
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
					Utils.showShortToast(TaskDetail.this, "任务已标记为已完成");
					TaskDetail.this.finish();
					break;
				case Constant.FILE_DOWNLOAD_SUCCESS:
					// updateMedia(path.toString() + (String) msg.obj);
					// 更新视频附件缩略图显示
					Log.v("initHandler", "收到下载完成消息，准备更新视频缩略图");
					updateVideo((String) msg.obj);
					break;
				case Constant.MODIFY_TASK_REQUEST_SUCCESS:
					end_time.setText(currentSelectTime);
					end_time.setTextColor(getResources().getColor(R.color.red));
					Utils.showShortToast(TaskDetail.this, "任务截止时间已修改");
					break;
				case Constant.QUERY_TASK_INFO_REQUEST_FAIL:
					dismissProgressDialog();
					showLongToast("错误代码：" + ((NormalServerResponse) msg.obj).getEc());
					Log.e(getClass().getName(), "错误代码：" + ((NormalServerResponse) msg.obj).getEc());
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

		// 初始化图片显示布局
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

				// 2014-6-24 WeiHao 注释：改用新的图片加载模式

				// if (mediaType == Utils.MEDIA_TYPE_IMAGE) {
				// addImage(mediaPath);
				// } else if (mediaType == Utils.MEDIA_TYPE_VIDEO) {
				// addVideo(mediaPath);
				// }
				//
				// // 判断文件是否存在，不存在则连接文件服务器下载
				// if (!new File(mediaPath).exists()) {
				// String downUrl = LocalConstant.FILE_SERVER_ATTACH_URL
				// + File.separator + mediaName;
				// new HttpDownloadTask(TaskDetail.this).execute(downUrl,
				// "/nercms-Schedule/Attachments/", mediaName);
				// }

				// 2014-6-24 WeiHao 新增，异步图片下载，加载
				final ImageView imageView = new RoundAngleImageView(this);
				// 将图片加入附件显示图片的集合
				imageView.setTag(mediaName);
				imageViewList.add(imageView);
				addImage(imageView, mediaPath, false);
				if (mediaType == Utils.MEDIA_TYPE_IMAGE) {
					// 设置图片缩略图点击事件
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
						// 请求网络图片
						ImageRequest imageRequest = new ImageRequest(downUrl,
								new Response.Listener<Bitmap>() {

									@Override
									public void onResponse(Bitmap response) {
										// 图片保存到本地
										Utils.saveBitmap(response, mediaPath);
										addImage(imageView, mediaPath, true);
									}
								}, 0, 0, Config.RGB_565, new Response.ErrorListener() {
									@Override
									public void onErrorResponse(VolleyError error) {
									}
								});
						// 加入请求队列
						AppApplication.getInstance().myQueue.add(imageRequest);
					} else {
						addImage(imageView, mediaPath, true);
					}
				} else if (mediaType == Utils.MEDIA_TYPE_VIDEO) {
					// 设置视频缩略图点击事件
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

						// 判断视频缩略图是否存在，不存在则生成缩略图
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
	/** 下载完成后，更新视频显示 */
	private void updateVideo(final String mediaName) {
		for (ImageView imageView : imageViewList) {
			if (imageView.getTag() != null) {
				// if (imageView.getTag().equals((Object) path)) {
				// // 获取视频缩略图
				// Bitmap pic = ThumbnailUtils.createVideoThumbnail(path,
				// Thumbnails.MICRO_KIND);
				// if (pic != null) {
				// // 将图片缩略图添加到缩略图列表，便于新建完成后回收
				// imageCache.put(path, new SoftReference<Bitmap>(pic));
				// // 加载缩略图
				// imageView.setImageBitmap(pic);
				// }
				// }

				if (imageView.getTag().equals(mediaName)) {

					// 判断视频缩略图是否存在，不存在则生成缩略图
					String thumbnailPath = videoThumbnailDir
							+ mediaName.substring(0, mediaName.indexOf(".")) + ".jpg";
					Log.v("updateVideo", "生成视频缩略图");
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
		// 加载图片的ImageView
		// final ImageView imageView = new RoundAngleImageView(this);
		imageView.setPadding(2, 2, 2, 2);
		// 为imageView设置标志，以便下载完成后更新
		// imageView.setTag(path);

		// 设置图片显示格式
		imageView.setLayoutParams(layoutParams);
		imageView.setScaleType(ScaleType.FIT_XY);

		if (isDone) {
			// 为图片设置单击事件
			// imageView.setOnClickListener(new OnClickListener() {
			// public void onClick(View v) {
			// // 单击事件触发后，单击事件失效
			// // imageView.setEnabled(false);
			// // 点击显示大图
			// // showImageDialog(imageView, path);
			//
			// }
			// });
			// 异步加载本地图片
			com.nostra13.universalimageloader.core.ImageLoader.getInstance()
					.displayImage("file://" + path, imageView, options);
		} else {
			// 将图片添加入图片列表
			mediaContainer.addView(imageView);
			// 设置背景为灰色
			imageView.setBackgroundResource(R.drawable.no_picture);
		}

	}

	/** 显示大图对话框 */
	private void showImageDialog(final ImageView imageView, final String path) {
		// 获取对话框布局并实例化
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.image_dialog, null);
		// 构造对话框
		imageDialog = new Dialog(this, R.style.imageDialog);
		imageDialog.setContentView(view);
		// 添加图片
		ImageView dialogImageView = (ImageView) view.findViewById(R.id.imageImageView);
		// 获取图片
		try {
			final Bitmap pic = BitmapFactory.decodeFile(path);
			// 将图片缩略图加载到ImageView
			dialogImageView.setImageBitmap(pic);
			// 为图片设置单击事件
			dialogImageView.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					imageDialog.dismiss();
					imageDialog = null;
					// 缩略图单击事件恢复
					imageView.setEnabled(true);
					// 图片回收
					pic.recycle();
				}
			});
			// 显示对话框
			imageDialog.show();
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** 通过图片的路径获取图片的缩略图 */
	public Bitmap getThumbnailFromPath(final String path) {
		// 图片输入流
		InputStream input = null;
		try {
			input = new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// 图片缩略选项
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 4;
		// 获取图片缩略图并返回
		return BitmapFactory.decodeStream(input, null, opts);
	}

	/**
	 * 回收图片内存
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

	// 选择截止时间 对年月日进行判断
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
		// 添加大小月月份并将其转换为list,方便之后的判断
		String[] months_big = { "1", "3", "5", "7", "8", "10", "12" };
		String[] months_little = { "4", "6", "9", "11" };
		final List<String> list_big = Arrays.asList(months_big);
		final List<String> list_little = Arrays.asList(months_little);
		dialog = new Dialog(this);
		dialog.setTitle("请选择日期与时间");
		// 找到dialog的布局文件
		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.date_time_select, null);
		// 年
		final WheelView wv_year = (WheelView) view.findViewById(R.id.year);
		wv_year.setAdapter(new NumericWheelAdapter(START_YEAR, END_YEAR));// 设置"年"的显示数据
		wv_year.setCyclic(true);// 可循环滚动
		wv_year.setLabel("年");// 添加文字
		wv_year.setCurrentItem(year - START_YEAR);// 初始化时显示的数据
		// 月
		final WheelView wv_month = (WheelView) view.findViewById(R.id.month);
		wv_month.setAdapter(new NumericWheelAdapter(1, 12));
		wv_month.setCyclic(true);
		wv_month.setLabel("月");
		wv_month.setCurrentItem(month - 1);
		// 日
		final WheelView wv_day = (WheelView) view.findViewById(R.id.day);
		wv_day.setCyclic(true);
		// 判断大小月及是否闰年,用来确定"日"的数据
		if (list_big.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 31));
		} else if (list_little.contains(String.valueOf(month + 1))) {
			wv_day.setAdapter(new NumericWheelAdapter(1, 30));
		} else {
			// 闰年
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				wv_day.setAdapter(new NumericWheelAdapter(1, 29));
			else
				wv_day.setAdapter(new NumericWheelAdapter(1, 28));
		}
		wv_day.setLabel("日");
		wv_day.setCurrentItem(day - 1);
		// 时
		final WheelView wv_hours = (WheelView) view.findViewById(R.id.hour);
		wv_hours.setAdapter(new NumericWheelAdapter(0, 23));
		wv_hours.setCyclic(true);
		wv_hours.setCurrentItem(hour);
		// 分
		final WheelView wv_mins = (WheelView) view.findViewById(R.id.mins);
		wv_mins.setAdapter(new NumericWheelAdapter(0, 59, "%02d"));
		wv_mins.setCyclic(true);
		wv_mins.setCurrentItem(minute);
		// 添加"年"监听
		OnWheelChangedListener wheelListener_year = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int year_num = newValue + START_YEAR;
				// 判断大小月及是否闰年,用来确定"日"的数据
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
		// 添加"月"监听
		OnWheelChangedListener wheelListener_month = new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
				int month_num = newValue + 1;
				// 判断大小月及是否闰年,用来确定"日"的数据
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
		// 根据屏幕密度来指定选择器字体的大小
		int textSize = 0;

		textSize = 15;

		wv_day.TEXT_SIZE = textSize;
		wv_hours.TEXT_SIZE = textSize;
		wv_mins.TEXT_SIZE = textSize;
		wv_month.TEXT_SIZE = textSize;
		wv_year.TEXT_SIZE = textSize;

		Button btn_sure = (Button) view.findViewById(R.id.btn_datetime_sure);
		Button btn_cancel = (Button) view.findViewById(R.id.btn_datetime_cancel);
		// 确定按钮
		btn_sure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// 如果是个数,则显示为"02"的样式
				String parten = "00";
				DecimalFormat decimal = new DecimalFormat(parten);
				// 设置日期的显示
				String _nowTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")// 创建时间
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
					Toast.makeText(TaskDetail.this, "选择时间小于当前时间，请重新选择", Toast.LENGTH_SHORT).show();
				} else {
					dialog.dismiss();
					new AlertDialog.Builder(TaskDetail.this).setTitle("修改提醒")
							.setMessage("确定将任务的截止时间从 " + end_time.getText().toString() + " 修改为 "
									+ currentSelectTime + " 吗？")
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {

							// 向服务器发出修改请求
							// //////////TODO
							webRequestManager.modifyAffairEndTime("", taskID, currentSelectTime);
						}

					}).setNegativeButton("取消", null).create().show();

				}
			}
		});
		// 取消
		btn_cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				dialog.dismiss();
			}
		});
		// 设置dialog的布局,并显示
		dialog.setContentView(view);
		dialog.show();
	}

}
