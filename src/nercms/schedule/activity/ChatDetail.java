package nercms.schedule.activity;

import java.io.File;
import java.security.acl.Group;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.adapter.FeedbackListAdapter;
import nercms.schedule.adapter.MessageListAdapter;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
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
import android.wxapp.service.dao.AttachmentDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.FeedbackDao;
import android.wxapp.service.dao.GroupDao;
import android.wxapp.service.dao.MessageDao;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.message.QueryContactPersonMessageResponseIds;
import android.wxapp.service.jerry.model.message.ReceiveMessageResponse;
import android.wxapp.service.model.FeedbackAttachModel;
import android.wxapp.service.model.FeedbackModel;
import android.wxapp.service.model.MessageModel;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.thread.SaveMessageThread;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.HttpUploadTask;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.imooc.treeview.utils.Node;

/**
 * 
 * @author jiaocuina@gmail.com
 * @version 2014-03-02
 * @description 发起消息;点击头像跳转到联系人详情
 * 
 */
public class ChatDetail extends BaseActivity implements OnClickListener {

	private Button mBtnSend;// 发送按钮
	private EditText mEditTextContent;// 消息编辑域
	private ListView mListView;

	// 2014-5-27 WeiHao
	private WebRequestManager webRequestManager;

	private String taskID;
	private String userID;// 本人ID
	private String personID; // 对方的ID
	private String personName;// 对方姓名

	private Handler handler;
	private DAOFactory daoFactory = DAOFactory.getInstance();
	private FeedbackListAdapter fbAdapter = null;
	// private ArrayList<FeedbackModel> fbList = new ArrayList<FeedbackModel>();
	private List<ReceiveMessageResponse> fbList = new ArrayList<ReceiveMessageResponse>();

	private String msgID;
	private MessageDao msgDao;
	private PersonDao personDao;
	private MessageListAdapter msgAdapter = null;
	private MessageModel msg = null;
	private List<ReceiveMessageResponse> msgList;

	// 入口：1-消息；2-反馈
	private int entranceType;

	// 2014-6-4 WeiHao
	private Button addAttachBtn;
	private LinearLayout attachmentLayout;
	private ImageView pictruePickIv;
	private ImageView cameraIv;
	private ImageView videoIv;

	// 附件存储路径
	private String imagePath;
	private String videoPath;

	private String feedbackID;
	private FeedbackAttachModel fbAttach;
	private FeedbackModel fb = null;

	// 2014-7-30 WeiHao
	// 群消息标志：0-个人消息；1-群消息
	private int isGroup = 0;
	private GroupDao groupDao;

	// 2041-8-9
	// 任务状态：2-已完成任务，限制反馈编辑发送，仅供查看
	private int taskStatus;
	private RelativeLayout operationLayout;

	List<Node> selectedPerson;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_detail);
		this.personDao = new PersonDao(this);
		this.groupDao = new GroupDao(this);
		entranceType = getIntent().getExtras().getInt("entrance_type");

		// 启动activity时不自动弹出软键盘
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		initActionBar();
		initView();
		initHandler();

		// 2014-5-27 WeiHao

		webRequestManager = new WebRequestManager(AppApplication.getInstance(), ChatDetail.this);

		userID = MySharedPreference.get(ChatDetail.this, MySharedPreference.USER_ID, "");

		if (entranceType == 2) { // 判断为反馈
			taskID = getIntent().getExtras().getString("task_id");
			taskStatus = getIntent().getExtras().getInt("task_status", -1);
		} else if (entranceType == 1) {// 判断为消息

			selectedPerson = (List<Node>) getIntent().getExtras().get("data");
			if (selectedPerson != null) {
				if (selectedPerson.size() == 1) {
					personID = selectedPerson.get(0).getId().substring(1);
					// 群聊
					if (selectedPerson.get(0).getId().startsWith("o")
							|| selectedPerson.get(0).getId().startsWith("g")) {
						isGroup = 1;
						// 基本群组
						if (selectedPerson.get(0).getId().startsWith("o")) {
							personName = personDao.getOrgDes(personID);
						}
						// 非基本群组
						else if (selectedPerson.get(0).getId().startsWith("g")) {
							// personName 赋值
							personName = groupDao.queryGroupById(personID).getN();
						}
					}
					// 私聊
					else if (selectedPerson.get(0).getId().startsWith("p")) {
						isGroup = 0;
						personName = personDao.getPersonInfo(personID).getN();
					}
				}
				// 标志是群消息
				else if (selectedPerson.size() > 1) {
					isGroup = 1;
				}
			} else {
				personID = getIntent().getExtras().getInt("selected_id") + "";
				personName = getIntent().getExtras().getString("selected_name");
				if (getIntent().getExtras().getBoolean("isGroup", false)) {
					isGroup = 1;
				}
			}
		}
		initData();

		if (taskStatus == 2) {
			operationLayout.setVisibility(View.GONE);
			getSupportActionBar().setTitle("查看反馈");
		}
	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("聊天");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// 消息右侧按钮 显示个人详情
		// 反馈时右侧无图标
		if (entranceType == 1) {
			MenuItem contact = menu.add(0, 1, 0, "联系人");
			contact.setIcon(R.drawable.ic_action_person);
			contact.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		}

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// 返回时将所有与该对象的消息标记为已读
			if (entranceType == 1) {
				// msgDao.updateMessageIsRead(userID, String.valueOf(personID));
			}
			this.finish();
			break;
		case 1:
			if (entranceType == 1) {
				Intent intent = new Intent(ChatDetail.this, ContactDetail.class);

				// 2014-7-30 WeiHao 修改
				intent.putExtra("IS_GROUP", isGroup);
				intent.putExtra("CONTACT_ID", selectedPerson.get(0).getId());
				startActivity(intent);
			}
			break;

		default:
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
		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

		operationLayout = (RelativeLayout) findViewById(R.id.rl_bottom);
	}

	/*
	 * 初始化要显示的数据
	 */
	public void initData() {
		getSupportActionBar().setTitle(personName);
		if (entranceType == 2) { // 反馈
			getSupportActionBar().setTitle("任务反馈");

			if (this.msgDao == null)
				this.msgDao = new MessageDao(this);

			fbList = this.msgDao.getFeedback(taskID);

			// fbDao = daoFactory.getFeedbackDao(ChatDetail.this);
			// attachDao = daoFactory.getAttachmentDao(ChatDetail.this);
			// fbList = fbDao.getFeedbackByAffairID(taskID);
			// 查询附件
			// ArrayList<FeedbackAttachModel> fbAttachList = new
			// ArrayList<FeedbackAttachModel>(0);
			// for (int i = 0; i < fbList.size(); i++) {
			// FeedbackModel tempFb = fbList.get(i);
			// fbAttachList =
			// attachDao.getAttachByFeedbackID(tempFb.getFeedbackID());
			// if (fbAttachList.size() > 0) {
			// for (int j = 0; j < fbAttachList.size(); j++) {
			// tempFb.setAttachment(fbAttachList.get(j));
			// fbList.set(i, tempFb);
			// }
			// }
			// }

			fbAdapter = new FeedbackListAdapter(ChatDetail.this, fbList);
			mListView.setAdapter(fbAdapter);
			mListView.setSelection(mListView.getCount() - 1);
		} else { // 消息
			msgDao = daoFactory.getMessageDao(ChatDetail.this);
			if (isGroup == 0) {
				msgList = msgDao.getMessageBySidAndRid(userID, personID, "0");

			} else {
				String type;
				if (selectedPerson.get(0).getId().startsWith("o"))
					type = "1";
				else {
					type = "2";
				}
				msgList = msgDao.getMessageBySidAndRid(userID, personID, type);
			}
			msgAdapter = new MessageListAdapter(ChatDetail.this, msgList);
			mListView.setAdapter(msgAdapter);
			mListView.setSelection(mListView.getCount() - 1);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:
			if (mEditTextContent.getText().toString().length() > 0) {
				if (entranceType == 1) {
					sendMessage();
				} else {
					sendFeedback();
				}
			} else {
				showShortToast("请填写消息内容");
			}
			break;

		case R.id.btn_chat_add_attach:
			if (attachmentLayout.getVisibility() == View.VISIBLE) {
				attachmentLayout.setVisibility(View.GONE);
			} else {
				attachmentLayout.setVisibility(View.VISIBLE);
				hideInput();
			}
			break;

		case R.id.picture_imageView:
			Toast.makeText(ChatDetail.this, "图库", Toast.LENGTH_SHORT).show();
			Intent getAlbum = new Intent();
			// 开启Pictures画面Type设定为image
			getAlbum.setType("image/*");
			getAlbum.setAction(Intent.ACTION_GET_CONTENT);

			if (entranceType == 1) {
				msgID = Utils.produceMessageID(userID);
				imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, msgID, ChatDetail.this);
			} else {
				feedbackID = Utils.produceFeedbackID(String.valueOf(userID));
				imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, feedbackID,
						ChatDetail.this);
			}
			startActivityForResult(getAlbum, LocalConstant.SELECT_IMAGE_REQUEST_CODE);

			break;

		case R.id.camera_imageView:
			Toast.makeText(ChatDetail.this, "拍照", Toast.LENGTH_SHORT).show();
			// 拍照
			Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			if (entranceType == 1) {
				msgID = Utils.produceMessageID(userID);
				imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, msgID, ChatDetail.this);
			} else {
				feedbackID = Utils.produceFeedbackID(String.valueOf(userID));
				imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, feedbackID,
						ChatDetail.this);
			}

			Uri imageUri = Uri.fromFile(new File(imagePath));
			// 指定照片储存路径
			imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(imageIntent, LocalConstant.CAPTURE_IMAGE_REQUEST_CODE);

			break;

		case R.id.video_imageView:

			Toast.makeText(ChatDetail.this, "摄像", Toast.LENGTH_SHORT).show();
			// 摄像
			Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			if (entranceType == 1) {
				msgID = Utils.produceMessageID(userID);
				videoPath = Utils.produceAttachDir(Utils.MEDIA_TYPE_VIDEO, msgID, ChatDetail.this);
			} else {
				feedbackID = Utils.produceFeedbackID(String.valueOf(userID));
				videoPath = Utils.produceAttachDir(Utils.MEDIA_TYPE_VIDEO, feedbackID,
						ChatDetail.this);
			}

			Uri videoUri = Uri.fromFile(new File(videoPath));
			// 指定视频存储路径
			videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
			// 指定视频的时长限制（30s）
			videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30000);
			startActivityForResult(videoIntent, LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);
			break;

		}
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// 缩略图地址
		// String thumbnailUri;
		// 媒体文件地址
		String originalUri;

		String uploadUrl = LocalConstant.FILE_SERVER_ATTACH_URL;

		switch (requestCode) {
		case LocalConstant.CAPTURE_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
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

				// 以下对全局变量进行赋值
				// 收到附件上传成功的handler通知后再在Handler中处理，再发送消息或者反馈内容到服务器，以及保存到本地
				if (entranceType == 1) { // 消息（附件）
					String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date(System.currentTimeMillis()));
					String imageName = originalUri.substring(originalUri.lastIndexOf("/") + 1);
					msg = new MessageModel(msgID, Integer.parseInt(userID),
							Integer.parseInt(personID), sendTime, null, LocalConstant.IAMGE_TYPE,
							imageName, isGroup, Constant.READ);

				} else { // 反馈（附件）
					String feedbackTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date(System.currentTimeMillis()));

					fb = new FeedbackModel(feedbackID, taskID, Integer.parseInt(userID),
							feedbackTime, "", Constant.READ);
					String imageName = originalUri.substring(originalUri.lastIndexOf("/") + 1);
					fbAttach = new FeedbackAttachModel(feedbackID, LocalConstant.IAMGE_TYPE,
							imageName);
					fb.setAttachment(fbAttach);
				}

				// 开启上传
				new HttpUploadTask(null, this).execute(originalUri, uploadUrl);
			}

			break;
		case LocalConstant.CAPTURE_VIDEO_REQUEST_CODE:

			if (resultCode == RESULT_OK) {
				originalUri = videoPath;
				// 判断文件是否存在,不存在直接跳过
				File file = new File(originalUri);
				if (!file.exists()) {
					return;
				}

				// 以下对全局变量进行赋值
				// 收到附件上传成功message后再在Handler中处理，再发送消息或者反馈内容到服务器，以及保存到本地

				if (entranceType == 1) { // 消息（附件）
					String sendTime1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date(System.currentTimeMillis()));
					String videoName = originalUri.substring(originalUri.lastIndexOf("/") + 1);
					msg = new MessageModel(msgID, Integer.parseInt(userID),
							Integer.parseInt(personID), sendTime1, null, LocalConstant.VIDEO_TYPE,
							videoName, isGroup, Constant.READ);
				} else {
					String feedbackTime1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date(System.currentTimeMillis()));

					fb = new FeedbackModel(feedbackID, taskID, Integer.parseInt(userID),
							feedbackTime1, null, Constant.READ);
					String videoName = originalUri.substring(originalUri.lastIndexOf("/") + 1);
					fbAttach = new FeedbackAttachModel(feedbackID, LocalConstant.VIDEO_TYPE,
							videoName);
					fb.setAttachment(fbAttach);
				}
				// 开启上传
				new HttpUploadTask(null, this).execute(originalUri, uploadUrl);
			}

			break;
		case LocalConstant.SELECT_IMAGE_REQUEST_CODE:
			String selectedPath;
			// 点击了返回键，对象返回为空,跳过
			if (data == null) {
				originalUri = "";
			} else {
				// 获得图片的uri 判断是否需要索引路径
				Uri selectedUri = data.getData();
				selectedPath = selectedUri.getPath();

				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedUri, proj, null, null, null);
				// 获得用户选择的图片的索引值
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				// 最后根据索引值获取图片路径
				selectedPath = cursor.getString(columnIndex);
				cursor.close();

				if (!(selectedPath.endsWith("jpg") || selectedPath.endsWith("gif")
						|| selectedPath.endsWith("bmp") || selectedPath.endsWith("png"))) {
					originalUri = "";
					Toast.makeText(ChatDetail.this, "不是图片", Toast.LENGTH_SHORT).show();
					return;
				}
				imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, taskID, ChatDetail.this);

				// 将图片拷贝到附件目录下
				File fromFile = new File(selectedPath);
				File toFile = new File(imagePath);
				Utils.copyFile(fromFile, toFile, true);

				// 以下对全局变量进行赋值
				// 收到附件上传成功message后再在Handler中处理，再发送消息或者反馈内容到服务器，以及保存到本地

				if (entranceType == 1) { // 消息（附件）
					String sendTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date(System.currentTimeMillis()));
					String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
					msg = new MessageModel(msgID, Integer.parseInt(userID),
							Integer.parseInt(personID), sendTime, null, LocalConstant.IAMGE_TYPE,
							imageName, isGroup, Constant.READ);
				} else {
					String feedbackTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
							.format(new Date(System.currentTimeMillis()));

					fb = new FeedbackModel(feedbackID, taskID, Integer.parseInt(userID),
							feedbackTime, "", Constant.UNREAD);
					String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
					fbAttach = new FeedbackAttachModel(feedbackID, LocalConstant.IAMGE_TYPE,
							imageName);
					fb.setAttachment(fbAttach);
				}
				// 开启上传
				new HttpUploadTask(null, this).execute(imagePath, uploadUrl);
			}

			break;

		default:
			break;
		}
	}

	// 发送反馈
	private void sendFeedback() {
		String contString = mEditTextContent.getText().toString();
		if (!contString.isEmpty()) { // 文本反馈
			feedbackID = Utils.produceFeedbackID(String.valueOf(userID));

			tempMsg = new ReceiveMessageResponse("", feedbackID, "4", userID, taskID,
					System.currentTimeMillis() + "", contString, "1", "", "", "");

			// 发送到服务器
			sendFb(tempMsg);

		} else { // 空消息发送提示
			new AlertDialog.Builder(ChatDetail.this).setTitle("不能发送空白反馈")
					.setPositiveButton("确定", null).create().show();
		}
	}

	private void sendFb(ReceiveMessageResponse data) {
		webRequestManager.sendFeedback(data.getSid(), data.getRid(), data.getSt(), data.getC(),
				data.getAt(), data.getAu(), data.getUt());
	}

	private void sendMsg(ReceiveMessageResponse data) {
		webRequestManager.sendMessage(data.getT(), data.getSid(), personID, data.getSt(),
				data.getC(), data.getAt(), data.getAu(), data.getUt());
	}

	ReceiveMessageResponse tempMsg;

	// 发送消息
	private void sendMessage() {
		String contString = mEditTextContent.getText().toString();
		// TODO
		if (isGroup == 0) {
			// 私聊
			tempMsg = new ReceiveMessageResponse("", "", "0", userID, personID,
					System.currentTimeMillis() + "", contString, "1", "", "", "");
		} else if (isGroup == 1) {
			String groupTyep = groupDao.queryGroupById(personID).getT();
			// 非基本群组
			if (groupTyep.equals("2")) {
				tempMsg = new ReceiveMessageResponse("", "", "2", userID, personID,
						System.currentTimeMillis() + "", contString, "1", "", "", "");
			}
			// 基本群组
			else if (groupTyep.equals("1")) {
				tempMsg = new ReceiveMessageResponse("", "", "1", userID, personID,
						System.currentTimeMillis() + "", contString, "1", "", "", "");
			}
		}
		// 发送消息到服务器
		sendMsg(tempMsg);
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message handlerMsg) {

				switch (handlerMsg.what) {

				case Constant.FILE_UPLOAD_SUCCESS:
					// if (entranceType == 1) {
					// if (msg == null) {
					// Toast.makeText(ChatDetail.this, "msg为空,不保存此反馈",
					// Toast.LENGTH_SHORT).show();
					// } else {
					// // // webRequestManager.sendMessage(msg);
					// // sendMsg(msg);
					// // msg.save(ChatDetail.this);
					// //
					// // msgList.add(msg);
					// msgAdapter.notifyDataSetChanged();
					// }
					//
					// } else {
					// // 发送服务器
					// if (fb == null) {
					// Toast.makeText(ChatDetail.this, "fb为空,不保存此反馈",
					// Toast.LENGTH_SHORT).show();
					// } else {
					// // webRequestManager.sendFeedback(fb);
					// sendFb(fb);
					// // 保存本地
					// if (fb.getAttachment() != null) {
					// fb.getAttachment().save(ChatDetail.this);
					// }
					// fb.save(ChatDetail.this);
					// // 刷新列表
					// fbList.add(fb);
					// fbAdapter.notifyDataSetChanged();
					// }
					//
					// }
					//
					// mEditTextContent.setText("");
					// mListView.setSelection(mListView.getCount() - 1);

					break;
				// 2014-6-9 接收到消息保存线程的通知，直接刷新对话界面
				case Constant.SAVE_MESSAGE_SUCCESS:
					// 取出消息体
					Log.v("ChatDetail", "消息保存完成，刷新界面");
					MessageModel newMsg = (MessageModel) handlerMsg.obj;
					// msgList.add(newMsg);
					// 更新数据库，标示该消息已读
					msgDao.updateMessageIsRead(newMsg.getMessageID());

					msgAdapter.notifyDataSetChanged();
					mListView.setSelection(mListView.getCount() - 1);
					break;
				case Constant.SEND_MESSAGE_REQUEST_SUCCESS:
					// 刷新显示
					hideInput();
					msgList.add(tempMsg);
					msgAdapter.notifyDataSetChanged();
					mEditTextContent.setText("");
					mListView.setSelection(mListView.getCount() - 1);
					Toast.makeText(ChatDetail.this, "发送消息成功", Toast.LENGTH_SHORT).show();

					break;
				case Constant.SEND_FEEDBACK_REQUEST_SUCCESS:
					hideInput();
					fbList.add(tempMsg);
					fbAdapter.notifyDataSetChanged();
					mEditTextContent.setText("");
					mListView.setSelection(mListView.getCount() - 1);
					Toast.makeText(ChatDetail.this, "发送反馈成功", Toast.LENGTH_SHORT).show();
					break;
				default:
					break;
				}

			}

		};

		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_SUCCESS,
				"ChatDetail");
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_MESSAGE_SUCCESS,
				"ChatDetail");
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_FEEDBACK_SUCCESS,
				"ChatDetail");
		MessageHandlerManager.getInstance().register(handler, Constant.SEND_MESSAGE_REQUEST_SUCCESS,
				Contants.METHOD_MESSAGE_SEND);
		MessageHandlerManager.getInstance().register(handler,
				Constant.SEND_FEEDBACK_REQUEST_SUCCESS, Contants.METHOD_FEEDBACK_SEND);
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

	@Override
	protected void onDestroy() {
		// 注销handler
		MessageHandlerManager.getInstance().unregister(Constant.SEND_FEEDBACK_REQUEST_SUCCESS,
				"ChatDetail");
		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_SUCCESS, "ChatDetail");
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_MESSAGE_SUCCESS, "ChatDetail");
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_FEEDBACK_SUCCESS,
				"ChatDetail");
		MessageHandlerManager.getInstance().unregister(Constant.SEND_MESSAGE_REQUEST_SUCCESS,
				Contants.METHOD_MESSAGE_SEND);
		MessageHandlerManager.getInstance().unregister(Constant.SEND_FEEDBACK_REQUEST_SUCCESS,
				Contants.METHOD_FEEDBACK_SEND);
		System.out.println("ChatDetail onDestroy");
		// 回收图片内存
		if (fbAdapter != null) {
			fbAdapter.freeBitmap();
		}
		if (msgAdapter != null) {
			msgAdapter.freeBitmap();
		}

		super.onDestroy();
	}

}