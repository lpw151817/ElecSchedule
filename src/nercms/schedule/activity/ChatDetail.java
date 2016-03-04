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
 * @description ������Ϣ;���ͷ����ת����ϵ������
 * 
 */
public class ChatDetail extends BaseActivity implements OnClickListener {

	private Button mBtnSend;// ���Ͱ�ť
	private EditText mEditTextContent;// ��Ϣ�༭��
	private ListView mListView;

	// 2014-5-27 WeiHao
	private WebRequestManager webRequestManager;

	private String taskID;
	private String userID;// ����ID
	private String personID; // �Է���ID
	private String personName;// �Է�����

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

	// // ��ڣ�1-��Ϣ��2-����
	// private int entranceType;

	// 2014-6-4 WeiHao
	private Button addAttachBtn;
	private LinearLayout attachmentLayout;
	private ImageView pictruePickIv;
	private ImageView cameraIv;
	private ImageView videoIv;

	// �����洢·��
	private String imagePath;
	private String videoPath;

	private String feedbackID;
	private FeedbackAttachModel fbAttach;
	private FeedbackModel fb = null;

	// 2014-7-30 WeiHao
	// Ⱥ��Ϣ��־��0-������Ϣ��1-Ⱥ��Ϣ
	private int isGroup = 0;
	private GroupDao groupDao;

	// 2041-8-9
	// ����״̬��2-������������Ʒ����༭���ͣ������鿴
	private int taskStatus;
	private RelativeLayout operationLayout;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_detail);
		this.personDao = new PersonDao(this);
		this.groupDao = new GroupDao(this);

		// ����activityʱ���Զ����������
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		iniActionBar(true, null, "����");
		initView();
		initHandler();

		// 2014-5-27 WeiHao

		webRequestManager = new WebRequestManager(AppApplication.getInstance(), ChatDetail.this);

		userID = getUserId();

		taskID = getIntent().getExtras().getString("task_id");
		taskStatus = getIntent().getExtras().getInt("task_status", -1);
		initData();

		if (taskStatus == 2) {
			operationLayout.setVisibility(View.GONE);
			getSupportActionBar().setTitle("�鿴����");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
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
	 * ��ʼ����ͼ
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
	 * ��ʼ��Ҫ��ʾ������
	 */
	public void initData() {

		if (this.msgDao == null)
			this.msgDao = new MessageDao(this);

		fbList = this.msgDao.getFeedback(taskID);

		fbAdapter = new FeedbackListAdapter(ChatDetail.this, fbList);
		mListView.setAdapter(fbAdapter);
		mListView.setSelection(mListView.getCount() - 1);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_send:
			if (mEditTextContent.getText().toString().length() > 0) {
				sendFeedback();
			} else {
				showShortToast("����д��Ϣ����");
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
			Toast.makeText(ChatDetail.this, "ͼ��", Toast.LENGTH_SHORT).show();
			Intent getAlbum = new Intent();
			// ����Pictures����Type�趨Ϊimage
			getAlbum.setType("image/*");
			getAlbum.setAction(Intent.ACTION_GET_CONTENT);

			feedbackID = Utils.produceFeedbackID(String.valueOf(userID));
			imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, feedbackID, ChatDetail.this);
			startActivityForResult(getAlbum, LocalConstant.SELECT_IMAGE_REQUEST_CODE);

			break;

		case R.id.camera_imageView:
			Toast.makeText(ChatDetail.this, "����", Toast.LENGTH_SHORT).show();
			// ����
			Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			feedbackID = Utils.produceFeedbackID(String.valueOf(userID));
			imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, feedbackID, ChatDetail.this);

			Uri imageUri = Uri.fromFile(new File(imagePath));
			// ָ����Ƭ����·��
			imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
			startActivityForResult(imageIntent, LocalConstant.CAPTURE_IMAGE_REQUEST_CODE);

			break;

		case R.id.video_imageView:

			Toast.makeText(ChatDetail.this, "����", Toast.LENGTH_SHORT).show();
			// ����
			Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			feedbackID = Utils.produceFeedbackID(String.valueOf(userID));
			videoPath = Utils.produceAttachDir(Utils.MEDIA_TYPE_VIDEO, feedbackID, ChatDetail.this);

			Uri videoUri = Uri.fromFile(new File(videoPath));
			// ָ����Ƶ�洢·��
			videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
			// ָ����Ƶ��ʱ�����ƣ�30s��
			videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 30000);
			startActivityForResult(videoIntent, LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);
			break;

		}
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// ����ͼ��ַ
		// String thumbnailUri;
		// ý���ļ���ַ
		String originalUri;

		String uploadUrl = android.wxapp.service.elec.request.Contants.HFS_URL;

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

				String feedbackTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date(System.currentTimeMillis()));

				fb = new FeedbackModel(feedbackID, taskID, Integer.parseInt(userID), feedbackTime,
						"", Constant.READ);
				String imageName = originalUri.substring(originalUri.lastIndexOf("/") + 1);
				fbAttach = new FeedbackAttachModel(feedbackID, LocalConstant.IAMGE_TYPE, imageName);
				fb.setAttachment(fbAttach);

				// �����ϴ�
				new HttpUploadTask(null, this).execute(originalUri, uploadUrl);
			}

			break;
		case LocalConstant.CAPTURE_VIDEO_REQUEST_CODE:

			if (resultCode == RESULT_OK) {
				originalUri = videoPath;
				// �ж��ļ��Ƿ����,������ֱ������
				File file = new File(originalUri);
				if (!file.exists()) {
					return;
				}
				String feedbackTime1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date(System.currentTimeMillis()));

				fb = new FeedbackModel(feedbackID, taskID, Integer.parseInt(userID), feedbackTime1,
						null, Constant.READ);
				String videoName = originalUri.substring(originalUri.lastIndexOf("/") + 1);
				fbAttach = new FeedbackAttachModel(feedbackID, LocalConstant.VIDEO_TYPE, videoName);
				fb.setAttachment(fbAttach);
				// �����ϴ�
				new HttpUploadTask(null, this).execute(originalUri, uploadUrl);
			}

			break;
		case LocalConstant.SELECT_IMAGE_REQUEST_CODE:
			String selectedPath;
			// ����˷��ؼ������󷵻�Ϊ��,����
			if (data == null) {
				originalUri = "";
			} else {
				// ���ͼƬ��uri �ж��Ƿ���Ҫ����·��
				Uri selectedUri = data.getData();
				selectedPath = selectedUri.getPath();

				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = getContentResolver().query(selectedUri, proj, null, null, null);
				// ����û�ѡ���ͼƬ������ֵ
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				// ����������ֵ��ȡͼƬ·��
				selectedPath = cursor.getString(columnIndex);
				cursor.close();

				if (!(selectedPath.endsWith("jpg") || selectedPath.endsWith("gif")
						|| selectedPath.endsWith("bmp") || selectedPath.endsWith("png"))) {
					originalUri = "";
					Toast.makeText(ChatDetail.this, "����ͼƬ", Toast.LENGTH_SHORT).show();
					return;
				}
				imagePath = Utils.produceAttachDir(Utils.MEDIA_TYPE_IMAGE, taskID, ChatDetail.this);

				// ��ͼƬ����������Ŀ¼��
				File fromFile = new File(selectedPath);
				File toFile = new File(imagePath);
				Utils.copyFile(fromFile, toFile, true);

				// ���¶�ȫ�ֱ������и�ֵ
				// �յ������ϴ��ɹ�message������Handler�д����ٷ�����Ϣ���߷������ݵ����������Լ����浽����

				String feedbackTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.format(new Date(System.currentTimeMillis()));

				fb = new FeedbackModel(feedbackID, taskID, Integer.parseInt(userID), feedbackTime,
						"", Constant.UNREAD);
				String imageName = imagePath.substring(imagePath.lastIndexOf("/") + 1);
				fbAttach = new FeedbackAttachModel(feedbackID, LocalConstant.IAMGE_TYPE, imageName);
				fb.setAttachment(fbAttach);
				// �����ϴ�
				new HttpUploadTask(null, this).execute(imagePath, uploadUrl);
			}

			break;

		default:
			break;
		}
	}

	// ���ͷ���
	private void sendFeedback() {
		String contString = mEditTextContent.getText().toString();
		if (!contString.isEmpty()) { // �ı�����
			feedbackID = Utils.produceFeedbackID(String.valueOf(userID));

			tempMsg = new ReceiveMessageResponse("", feedbackID, "4", userID, taskID,
					System.currentTimeMillis() + "", contString, "1", "", "", "");

			// ���͵�������
			sendFb(tempMsg);

		} else { // ����Ϣ������ʾ
			new AlertDialog.Builder(ChatDetail.this).setTitle("���ܷ��Ϳհ׷���")
					.setPositiveButton("ȷ��", null).create().show();
		}
	}

	private void sendFb(ReceiveMessageResponse data) {
		webRequestManager.sendFeedback(data.getSid(), data.getRid(), data.getSt(), data.getC(),
				data.getAt(), data.getAu(), data.getUt());
	}

	ReceiveMessageResponse tempMsg;

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message handlerMsg) {

				switch (handlerMsg.what) {

				case Constant.FILE_UPLOAD_SUCCESS:

					break;
				// 2014-6-9 ���յ���Ϣ�����̵߳�֪ͨ��ֱ��ˢ�¶Ի�����
				case Constant.SAVE_MESSAGE_SUCCESS:
					// ȡ����Ϣ��
					Log.v("ChatDetail", "��Ϣ������ɣ�ˢ�½���");
					MessageModel newMsg = (MessageModel) handlerMsg.obj;
					// msgList.add(newMsg);
					// �������ݿ⣬��ʾ����Ϣ�Ѷ�
					msgDao.updateMessageIsRead(newMsg.getMessageID());

					msgAdapter.notifyDataSetChanged();
					mListView.setSelection(mListView.getCount() - 1);
					break;
				case Constant.SEND_MESSAGE_REQUEST_SUCCESS:
					// ˢ����ʾ
					hideInput();
					msgList.add(tempMsg);
					msgAdapter.notifyDataSetChanged();
					mEditTextContent.setText("");
					mListView.setSelection(mListView.getCount() - 1);
					Toast.makeText(ChatDetail.this, "������Ϣ�ɹ�", Toast.LENGTH_SHORT).show();

					break;
				case Constant.SEND_FEEDBACK_REQUEST_SUCCESS:
					hideInput();
					fbList.add(tempMsg);
					fbAdapter.notifyDataSetChanged();
					mEditTextContent.setText("");
					mListView.setSelection(mListView.getCount() - 1);
					Toast.makeText(ChatDetail.this, "���ͷ����ɹ�", Toast.LENGTH_SHORT).show();
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
	 * ���������
	 */
	private void hideInput() {
		// �����ʧȥ���㣨ע�⣬��ʼʱ��ֻ���������Ի�ȡ���㣬���Դ˷�����Ч��
		// ����xml�ļ��н�����TextView��focusable��Ϊtrue���У���
		// �ο�http://blog.sina.com.cn/s/blog_4e1e357d01012mkx.html
		mEditTextContent.clearFocus();
		// ���������
		InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(
				Context.INPUT_METHOD_SERVICE);
		mInputMethodManager.hideSoftInputFromWindow(mEditTextContent.getWindowToken(), 0);
	}

	@Override
	protected void onDestroy() {
		// ע��handler
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
		// ����ͼƬ�ڴ�
		if (fbAdapter != null) {
			fbAdapter.freeBitmap();
		}
		if (msgAdapter != null) {
			msgAdapter.freeBitmap();
		}

		super.onDestroy();
	}

}