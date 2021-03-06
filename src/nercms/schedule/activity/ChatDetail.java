package nercms.schedule.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.baidu.location.b.e;
import com.example.recordtest.AudioRecorder;
import com.example.recordtest.RecordButton;
import com.example.recordtest.RecordButton.RecordListener;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.ContactsContract.CommonDataKinds.Event;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.GroupDao;
import android.wxapp.service.dao.MessageDao;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.elec.dao.TaskInsDao;
import android.wxapp.service.elec.model.CreateInsResponse;
import android.wxapp.service.elec.model.NormalServerResponse;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.message.ReceiveMessageResponse;
import android.wxapp.service.model.FeedbackAttachModel;
import android.wxapp.service.model.FeedbackModel;
import android.wxapp.service.model.MessageModel;
import android.wxapp.service.request.Contants;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.DownloadMutex;
import android.wxapp.service.util.HttpUploadTask;
import nercms.schedule.R;
import nercms.schedule.adapter.FeedbackListAdapter;
//fym import nercms.schedule.adapter.MessageListAdapter;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;

/**
 * 
 * @author jiaocuina@gmail.com
 * @version 2014-03-02
 * @description 发起消息;点击头像跳转到联系人详情
 * 
 */
public class ChatDetail extends BaseActivity implements OnClickListener {

	private static final int UPDATE_LIST = 151;
	private Button mBtnSend;// 发送按钮
	private Button mBtnChange;//语音文字切换键
	private boolean isVoice = false;
	// private Button mBtnVoice;
	private RecordButton mBtnVoice;
	private EditText mEditTextContent;// 消息编辑域
	private ListView mListView;

	// 2014-5-27 WeiHao
	private WebRequestManager webRequestManager;

	private String taskID;

	private Handler handler;
	private FeedbackListAdapter fbAdapter = null;
	private List<tb_task_instructions> fbList = new ArrayList<tb_task_instructions>();
	private List<tb_task_instructions> newList = new ArrayList<tb_task_instructions>();
	//fym private List<tb_task_instructions> tempList = new ArrayList<tb_task_instructions>();

	private String msgID;
	private PersonDao personDao;
	private TaskInsDao msgDao;

	// // 入口：1-消息；2-反馈
	// private int entranceType;

	// 2014-6-4 WeiHao
	private Button addAttachBtn;//增加附件按钮
	private LinearLayout attachmentLayout;
	private ImageView pictruePickIv;
	private ImageView cameraIv;
	private ImageView videoIv;

	// 附件存储路径
	private String imagePath;
	private String videoPath;

	private FeedbackAttachModel fbAttach;
	private FeedbackModel fb = null;

	// 2014-7-30 WeiHao
	// 群消息标志：0-个人消息；1-群消息
	private int isGroup = 0;
	private GroupDao groupDao;

	// 2041-8-9
	// 任务状态：2-已完成任务，限制反馈编辑发送，仅供查看
	private int taskStatus;
	private LinearLayout operationLayout;
	private int delayedTime = 1000;// 1s

	public void onCreate(Bundle savedInstanceState)
	{
		Log.v("Schedule", "ChatDetail onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_detail);
		this.personDao = new PersonDao(this);
		this.groupDao = new GroupDao(this);

		// 启动activity时不自动弹出软键盘
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		iniActionBar(true, null, "交互信息");
		initView();
		initHandler();

		// 2014-5-27 WeiHao

		webRequestManager = new WebRequestManager(AppApplication.getInstance(), ChatDetail.this);

		taskID = getIntent().getExtras().getString("task_id");
		taskStatus = getIntent().getExtras().getInt("task_status", -1);
		initData();

		if (taskStatus == 2)
		{
			//任务结束仍显示交互信息 operationLayout.setVisibility(View.GONE);
		}

		//更新列表，周期delayedTime
		Runnable thread = new Runnable() {

			@Override
			public void run()
			{
				//if(true) return;//fym
				
				if (msgDao == null)
					msgDao = new TaskInsDao(ChatDetail.this);

				//刷新显示已有消息
				//fym tempList = msgDao.getMsg(taskID);
				//Log.v("Schedule", "chat msg " + tempList.size());
				Message msg = new Message();
				msg.obj = msgDao.getMsg(taskID);//fym tempList;
				msg.what = UPDATE_LIST;
				handler.sendMessage(msg);
			}
		};

		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(thread, 100, delayedTime, TimeUnit.MILLISECONDS);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * 初始化视图
	 */
	public void initView() {

		addAttachBtn = (Button) findViewById(R.id.btn_chat_add_attach);
		addAttachBtn.setOnClickListener(this);
		attachmentLayout = (LinearLayout) findViewById(R.id.attachment_linearLayout);
		pictruePickIv = (ImageView) findViewById(R.id.picture_imageView);
		pictruePickIv.setOnClickListener(this);
		cameraIv = (ImageView) findViewById(R.id.camera_imageView);
		cameraIv.setOnClickListener(this);
		videoIv = (ImageView) findViewById(R.id.video_imageView);
		videoIv.setOnClickListener(this);

		mListView = (ListView) findViewById(R.id.listview);
		mBtnSend = (Button) findViewById(R.id.btn_send);
		mBtnSend.setOnClickListener(this);
		mBtnChange = (Button) findViewById(R.id.btn_chat_change);
		mBtnChange.setOnClickListener(this);
		mBtnChange.setBackgroundResource(android.R.drawable.ic_btn_speak_now);
		// mBtnVoice = (Button) findViewById(R.id.btn_chat_voice);
		mBtnVoice = (RecordButton) findViewById(R.id.btn_chat_voice);
		mBtnVoice.setAudioRecord(new AudioRecorder());
		// 录音完成之后的回调
		mBtnVoice.setRecordListener(new RecordListener() {

			@Override
			public void recordEnd(String filename) {
				/*
				 * attachmentType01 图片 attachmentType02 音频 attachmentType03 视频
				 */
				String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
						+ "/nercms-Schedule/DownloadAttachments/" + filename;
				// DB 存储
				//saveInsAndAtt(String planTaskId, String content, String send_id, String sendtime, String type, String attType, String url, String update_time, String md5)
				boolean saveSuccess = new TaskInsDao(ChatDetail.this).saveInsAndAtt(taskID, "",
						getUserId(), System.currentTimeMillis() + "", "1", "attachmentType02",
						filename, System.currentTimeMillis() + "",
						Utils.getFileMD5(new File(filePath)));

				if (saveSuccess)
				{
					Log.v("Schedule", "save audio record");
					// 界面显示
					fbList = msgDao.getMsg(taskID);
					fbAdapter.setFblist(fbList);
					mListView.setSelection(fbList.size() - 1);
				}
				else
				{
					showShortToast("录音保存失败");
				}
			}
		});

		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

		operationLayout = (LinearLayout) findViewById(R.id.rl_bottom);

		// 添加监听器，用于控制发送按钮和附件按钮
		mEditTextContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				// 将附件框消失掉
				if (attachmentLayout.getVisibility() == View.VISIBLE)
					attachmentLayout.setVisibility(View.GONE);

				if (TextUtils.isEmpty(s)) {
					mBtnSend.setVisibility(View.GONE);
					addAttachBtn.setVisibility(View.VISIBLE);
				} else {
					mBtnSend.setVisibility(View.VISIBLE);
					addAttachBtn.setVisibility(View.GONE);
				}
			}
		});
	}

	/*
	 * 初始化要显示的数据
	 */
	public void initData() {

		if (this.msgDao == null)
			this.msgDao = new TaskInsDao(this);

		fbList = this.msgDao.getMsg(taskID);

		fbAdapter = new FeedbackListAdapter(ChatDetail.this, fbList);
		mListView.setAdapter(fbAdapter);
		mListView.setSelection(mListView.getCount() - 1);
	}

	private long time_old = System.currentTimeMillis();

	@Override
	public void onClick(View v) {
		Log.v("Schedule", "chat button: " + v.getId());//fym
		
		switch (v.getId())
		{
		case R.id.btn_chat_change://左侧文字语音切换键
			Log.v("Schedule", "R.id.btn_chat_change");//fym
			// 将附件框消失掉
			if (attachmentLayout.getVisibility() == View.VISIBLE)
				attachmentLayout.setVisibility(View.GONE);

			isVoice = !isVoice;
			if(isVoice)//发语音
			{
				mEditTextContent.setVisibility(View.GONE);//隐藏文本输入框
				mBtnVoice.setVisibility(View.VISIBLE);
				hideInput();
				mEditTextContent.setText("");
				//修改button图标为话筒
				mBtnChange.setBackgroundResource(R.drawable.ic_keyboard);
			}
			else
			{
				mEditTextContent.setVisibility(View.VISIBLE);
				mBtnVoice.setVisibility(View.GONE);
				mEditTextContent.requestFocus();
				mBtnChange.setBackgroundResource(android.R.drawable.ic_btn_speak_now);
			}
			break;
			
		case R.id.btn_send:
			Log.v("Schedule", "R.id.btn_send");//fym
			if (System.currentTimeMillis() - time_old > 1000) {
				if (mEditTextContent.getText().toString().length() > 0) {
					sendFeedback();
				} else {
					showShortToast("请填写消息内容");
				}
			} else {
				showShortToast("连续两条消息不能小于1秒");
			}
			break;

		case R.id.btn_chat_add_attach:
			Log.v("Schedule", "R.id.btn_chat_add_attach");//fym
			if (attachmentLayout.getVisibility() == View.VISIBLE) {
				attachmentLayout.setVisibility(View.GONE);
			} else {
				attachmentLayout.setVisibility(View.VISIBLE);
				hideInput();
			}
			break;

		case R.id.picture_imageView:
			Log.v("Schedule", "R.id.picture_imageView");//fym
			// 选照片
			//////////////// 暂时不做实现
			////////////////////////////////////////////////////////////////////
			Toast.makeText(ChatDetail.this, "图库", Toast.LENGTH_SHORT).show();
			Intent getAlbum = new Intent();
			// 开启Pictures画面Type设定为image
			getAlbum.setType("image/*");
			getAlbum.setAction(Intent.ACTION_GET_CONTENT);

			startActivityForResult(getAlbum, LocalConstant.SELECT_IMAGE_REQUEST_CODE);

			break;

		case R.id.camera_imageView:
			Log.v("Schedule", "R.id.camera_imageView");//fym
			// TODO 拍照
			Toast.makeText(ChatDetail.this, "拍照", Toast.LENGTH_SHORT).show();
			// 拍照
//			Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//			startActivityForResult(imageIntent, LocalConstant.CAPTURE_IMAGE_REQUEST_CODE);
			
			Intent imageIntent = new Intent();
			imageIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
			imageIntent.addCategory(Intent.CATEGORY_DEFAULT);
			
			mImagePath = NewTask.fileFolder + File.separator + Utils.getFileDate()
					+ ".jpg";
			
			// 根据文件地址创建文件
			File imagefile = new File(mImagePath);
			if (!imagefile.getParentFile().exists())
				imagefile.mkdirs();
			if (imagefile.exists()) {
				imagefile.delete();
			}
			// 把文件地址转换成Uri格式
			Uri imageUri = Uri.fromFile(imagefile);
			imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(imageIntent, LocalConstant.CAPTURE_IMAGE_REQUEST_CODE);

			break;

		case R.id.video_imageView:
			Log.v("Schedule", "R.id.video_imageView");//fym
			// TODO 录像
			Toast.makeText(ChatDetail.this, "摄像", Toast.LENGTH_SHORT).show();
			// 摄像
//			Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//			startActivityForResult(videoIntent, LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);
			
			Intent intent = new Intent(ChatDetail.this, Video.class);

			String fileName = Utils.getFileDate();
			videopath2 = NewTask.fileFolder + "/" + fileName + ".mp4";
			File file = new File(videopath2);
			if (file.exists()) {
				file.delete();
			}
			intent.putExtra("videoPath", videopath2);
			intent.putExtra("maxTime", 8);//交互信息录像最长15秒
			startActivityForResult(intent, LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);
			break;

		}
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		Log.v("Schedule", "ChatDetail::onActivityResult");//fym
		super.onActivityResult(requestCode, resultCode, data);
		/*
		 * attachmentType01 图片 attachmentType02 音频 attachmentType03 视频
		 */
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case LocalConstant.CAPTURE_IMAGE_REQUEST_CODE:
				// TODO 拍照回调
				showShortToast("拍照回调");
				String filename = mImagePath.substring(mImagePath.lastIndexOf(File.separator + "") + 1);
				String thumbnailUri = Utils.getThumbnailDir(filename);
				// 获取缩略图,根据原图创建缩略图, mImagePath是原图的地址
				Utils.getThumbnail(mImagePath, thumbnailUri);
				
				String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
						+ "/nercms-Schedule/DownloadAttachments/" + filename;
				// DB 存储
				boolean saveSuccess = new TaskInsDao(ChatDetail.this).saveInsAndAtt(taskID, "",
						getUserId(), System.currentTimeMillis() + "", "1", "attachmentType01",
						filename, System.currentTimeMillis() + "",
						Utils.getFileMD5(new File(filePath)));

				if (saveSuccess) {
					// 界面显示
					fbList = msgDao.getMsg(taskID);
					fbAdapter.setFblist(fbList);
					mListView.setSelection(fbList.size() - 1);
				} else {
					showShortToast("图片保存失败");
				}
				
				break;
			case LocalConstant.CAPTURE_VIDEO_REQUEST_CODE:
				// TODO 录像回调
				showShortToast("录像回调");
				Bitmap videoThumbnailBitmap = getVideoThumbnail(videopath2, 400, 400,
						MediaStore.Images.Thumbnails.MINI_KIND);
				String videoNameContainsSuffix = videopath2.substring(videopath2
						.lastIndexOf(File.separator + "") + 1);//2015.mp4
				String videoName = videoNameContainsSuffix.replace("mp4", "jpg");
				String uri = NewTask.fileThumbnail + videoName;
				Utils.saveBitmap(videoThumbnailBitmap, uri);
				
//				String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()
//						+ "/nercms-Schedule/DownloadAttachments/" + filename;
				// DB 存储
				boolean saveSuccess1 = new TaskInsDao(ChatDetail.this).saveInsAndAtt(taskID, "",
						getUserId(), System.currentTimeMillis() + "", "1", "attachmentType03",
						videoNameContainsSuffix, System.currentTimeMillis() + "",
						Utils.getFileMD5(new File(videopath2)));

				if (saveSuccess1) {
					// 界面显示
					fbList = msgDao.getMsg(taskID);
					fbAdapter.setFblist(fbList);
					mListView.setSelection(fbList.size() - 1);
				} else {
					showShortToast("录像保存失败");
				}
				
				break;
			case LocalConstant.SELECT_IMAGE_REQUEST_CODE:
				// TODO 选照片回调
				showShortToast("选照片回调");
				break;
			}
		}
	}

	// 发送反馈
	private void sendFeedback() {
		String contString = mEditTextContent.getText().toString();
		if (!contString.isEmpty()) { // 文本反馈
			tempMsg = new tb_task_instructions("", taskID, contString, getUserId(),
					System.currentTimeMillis() + "", "1");

			// 发送到服务器
			// uid 为接收人员id
			webRequestManager.createInsRequest(this, msgDao.getMsgReceivers(taskID), tempMsg.getTask_id(), tempMsg.getContent(), null, "1");

		} else { // 空消息发送提示
			new AlertDialog.Builder(ChatDetail.this).setTitle("不能发送空白反馈")
					.setPositiveButton("确定", null).create().show();
		}
	}

	tb_task_instructions tempMsg;
	private ScheduledExecutorService scheduler;
	private String mImagePath;
	private String videopath2;

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				
				//Log.v("Schedule", "ChatDetail msg: " + msg.what);//fym

				switch (msg.what) {

				case Constants.CREATE_INS_SUCCESS:
					time_old = System.currentTimeMillis();
					// 刷新显示
					hideInput();
					fbList.add(tempMsg);
					fbAdapter.notifyDataSetChanged();
					mEditTextContent.setText("");
					mListView.setSelection(mListView.getCount() - 1);
					Toast.makeText(ChatDetail.this, "发送消息成功", Toast.LENGTH_SHORT).show();

					break;
				case Constants.CREATE_INS_SAVE_FAIL:
				case Constants.CREATE_INS_FAIL:
					if (msg.obj != null) {
						showShortToast("发布失败:" + ((NormalServerResponse) msg.obj).getEc());
					} else {
						showShortToast("发布失败,请检查是否与服务器连接正常");
					}
					break;

				case UPDATE_LIST:
					// List<tb_task_instructions> fbList
					newList = (List<tb_task_instructions>) msg.obj;
					
					if(0 == newList.size())
						break;
					
					if (fbList.size() == 0)
					{// 显示列表里面没有消息
						for (tb_task_instructions ins : newList)
						{
							fbList.add(ins);
						}

						Log.v("Baidu", "new list 3");
						fbAdapter.notifyDataSetChanged();
					}
					else
					{
						tb_task_instructions task = fbList.get(fbList.size() - 1);
						String time = task.getSend_time();
						//System.out.println("time : " + time);
						for (tb_task_instructions ins : newList)
						{
							String insTime = ins.getSend_time();

							boolean isUp = isUpdate(time, insTime);
							// 如果time < insTime就更新数据
							if (isUp) {
								if ((!getUserId().equals(ins.getSend_id())))
								{
									fbList.add(ins);
									Log.v("Baidu", "new list 5");
									fbAdapter.notifyDataSetChanged();
									mListView.setSelection(fbList.size() - 1);
								}
							}
						}
					}
					
					//fym
					if(null != fbAdapter)
					{
						if(false == DownloadMutex._download_feedback_tasks.isEmpty())
						{
							Log.v("Temp", "fbAdapter.notifyDataSetChanged()");
							fbAdapter.notifyDataSetChanged();
						}
					}
					break;
				}

			}

		};

		// MessageHandlerManager.getInstance().register(handler,
		// Constant.FILE_UPLOAD_SUCCESS,
		// "ChatDetail");
		// MessageHandlerManager.getInstance().register(handler,
		// Constant.SAVE_MESSAGE_SUCCESS,
		// "ChatDetail");
		// MessageHandlerManager.getInstance().register(handler,
		// Constant.SAVE_FEEDBACK_SUCCESS,
		// "ChatDetail");
		MessageHandlerManager.getInstance().register(handler, Constants.CREATE_INS_SUCCESS,
				CreateInsResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.CREATE_INS_SAVE_FAIL,
				CreateInsResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.CREATE_INS_FAIL,
				CreateInsResponse.class.getName());
	}

	private boolean isUpdate(String time, String insTime) {
		// TODO Auto-generated method stub

		if (time.compareTo(insTime) < 0) {// dat1在dat2之前
			return true;
		} else {
			return false;
		}

	}

	@Override
	protected void onDestroy() {
		// 注销handler
		// MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_SUCCESS,
		// "ChatDetail");
		// MessageHandlerManager.getInstance().unregister(Constant.SAVE_MESSAGE_SUCCESS,
		// "ChatDetail");
		// MessageHandlerManager.getInstance().unregister(Constant.SAVE_FEEDBACK_SUCCESS,
		// "ChatDetail");
		// MessageHandlerManager.getInstance().unregister(Constants.CREATE_INS_SUCCESS,
		// CreateInsResponse.class.getName());
		// MessageHandlerManager.getInstance().unregister(Constants.CREATE_INS_SAVE_FAIL,
		// CreateInsResponse.class.getName());
		// MessageHandlerManager.getInstance().unregister(Constants.CREATE_INS_FAIL,
		// CreateInsResponse.class.getName());

		System.out.println("ChatDetail onDestroy");
		// 回收图片内存
		if (fbAdapter != null) {
			fbAdapter.freeBitmap();
		}

		scheduler.shutdownNow();
		super.onDestroy();
	}

	private String getDate() {
		Calendar c = Calendar.getInstance();

		String year = String.valueOf(c.get(Calendar.YEAR));
		String month = String.valueOf(c.get(Calendar.MONTH));
		String day = String.valueOf(c.get(Calendar.DAY_OF_MONTH) + 1);
		String hour = String.valueOf(c.get(Calendar.HOUR_OF_DAY));
		String mins = String.valueOf(c.get(Calendar.MINUTE));

		StringBuffer sbBuffer = new StringBuffer();
		sbBuffer.append(year + "-" + month + "-" + day + " " + hour + ":" + mins);

		return sbBuffer.toString();
	}

	/**
	 * 隐藏软键盘
	 */
	private void hideInput() {
		// 输入框失去焦点（注意，初始时，只有输入框可以获取焦点，所以此方法无效，
		// 后在xml文件中将标题TextView的focusable设为true可行），
		// 参考http://blog.sina.com.cn/s/blog_4e1e357d01012mkx.html
		mEditTextContent.clearFocus();
		// 隐藏软键盘
		InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(
				Context.INPUT_METHOD_SERVICE);
		mInputMethodManager.hideSoftInputFromWindow(mEditTextContent.getWindowToken(), 0);
	}
	
	private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind)
	{
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);

		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

}