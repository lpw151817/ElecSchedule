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
import android.wxapp.service.util.HttpUploadTask;
import nercms.schedule.R;
import nercms.schedule.adapter.FeedbackListAdapter;
import nercms.schedule.adapter.MessageListAdapter;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;

/**
 * 
 * @author jiaocuina@gmail.com
 * @version 2014-03-02
 * @description ������Ϣ;���ͷ����ת����ϵ������
 * 
 */
public class ChatDetail extends BaseActivity implements OnClickListener {

	private static final int UPDATE_LIST = 151;
	private Button mBtnSend;// ���Ͱ�ť
	private Button mBtnChange;
	private boolean isVoice = false;
	private Button mBtnVoice;
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
	private List<tb_task_instructions> fbList = new ArrayList<tb_task_instructions>();
	private List<tb_task_instructions> newList = new ArrayList<tb_task_instructions>();
	private List<tb_task_instructions> tempList = new ArrayList<tb_task_instructions>();

	private String msgID;
	private PersonDao personDao;
	private TaskInsDao msgDao;

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

	private FeedbackAttachModel fbAttach;
	private FeedbackModel fb = null;

	// 2014-7-30 WeiHao
	// Ⱥ��Ϣ��־��0-������Ϣ��1-Ⱥ��Ϣ
	private int isGroup = 0;
	private GroupDao groupDao;

	// 2041-8-9
	// ����״̬��2-������������Ʒ����༭���ͣ������鿴
	private int taskStatus;
	private LinearLayout operationLayout;
	private int delayedTime = 1000;// 1s

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.chat_detail);
		this.personDao = new PersonDao(this);
		this.groupDao = new GroupDao(this);

		// ����activityʱ���Զ����������
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		iniActionBar(true, null, "������Ϣ");
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
		}

		Runnable thread = new Runnable() {

			@Override
			public void run() {
				if (msgDao == null)
					msgDao = new TaskInsDao(ChatDetail.this);

				tempList = msgDao.getMsg(taskID);
				Message msg = new Message();
				msg.obj = tempList;
				msg.what = UPDATE_LIST;
				handler.sendMessage(msg);
			}
		};

		scheduler = Executors.newScheduledThreadPool(1);
		scheduler.scheduleAtFixedRate(thread, 100, delayedTime, TimeUnit.MILLISECONDS);

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
		mBtnChange = (Button) findViewById(R.id.btn_chat_change);
		mBtnChange.setOnClickListener(this);
		mBtnVoice = (Button) findViewById(R.id.btn_chat_voice);

		/////////// ���voice��ס˵���¼�
		mBtnVoice.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					// TODO ���µ�ʱ�򴥷�
					mBtnVoice.setText("�ɿ�����");

					break;
				case MotionEvent.ACTION_UP:
					// TODO �ɿ���ʱ�򴥷�
					mBtnVoice.setText("��ס˵��");

					break;
				}

				return false;
			}
		});

		mEditTextContent = (EditText) findViewById(R.id.et_sendmessage);

		operationLayout = (LinearLayout) findViewById(R.id.rl_bottom);

		// ��Ӽ����������ڿ��Ʒ��Ͱ�ť�͸�����ť
		mEditTextContent.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
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
	 * ��ʼ��Ҫ��ʾ������
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
		switch (v.getId()) {
		case R.id.btn_chat_change:
			// ����������ʧ��
			if (attachmentLayout.getVisibility() == View.VISIBLE)
				attachmentLayout.setVisibility(View.GONE);

			isVoice = !isVoice;
			if (isVoice) {
				mEditTextContent.setVisibility(View.GONE);
				mBtnVoice.setVisibility(View.VISIBLE);
				hideInput();
				mEditTextContent.setText("");
				mBtnChange.setBackgroundResource(R.drawable.ic_keyboard);
			} else {
				mEditTextContent.setVisibility(View.VISIBLE);
				mBtnVoice.setVisibility(View.GONE);
				mEditTextContent.requestFocus();
				mBtnChange.setBackgroundResource(android.R.drawable.ic_btn_speak_now);
			}
			break;
		case R.id.btn_send:
			if (System.currentTimeMillis() - time_old > 1000) {
				if (mEditTextContent.getText().toString().length() > 0) {
					sendFeedback();
				} else {
					showShortToast("����д��Ϣ����");
				}
			} else {
				showShortToast("����������Ϣ����С��1��");
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
			// ѡ��Ƭ
			//////////////// ��ʱ����ʵ��
			////////////////////////////////////////////////////////////////////
			Toast.makeText(ChatDetail.this, "ͼ��", Toast.LENGTH_SHORT).show();
			Intent getAlbum = new Intent();
			// ����Pictures����Type�趨Ϊimage
			getAlbum.setType("image/*");
			getAlbum.setAction(Intent.ACTION_GET_CONTENT);

			startActivityForResult(getAlbum, LocalConstant.SELECT_IMAGE_REQUEST_CODE);

			break;

		case R.id.camera_imageView:
			// TODO ����
			Toast.makeText(ChatDetail.this, "����", Toast.LENGTH_SHORT).show();
			// ����
			Intent imageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

			startActivityForResult(imageIntent, LocalConstant.CAPTURE_IMAGE_REQUEST_CODE);

			break;

		case R.id.video_imageView:
			// TODO ¼��
			Toast.makeText(ChatDetail.this, "����", Toast.LENGTH_SHORT).show();
			// ����
			Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
			startActivityForResult(videoIntent, LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);
			break;

		}
	}

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case LocalConstant.CAPTURE_IMAGE_REQUEST_CODE:
				// TODO ���ջص�
				showShortToast("���ջص�");
				break;
			case LocalConstant.CAPTURE_VIDEO_REQUEST_CODE:
				// TODO ¼��ص�
				showShortToast("¼��ص�");
				break;
			case LocalConstant.SELECT_IMAGE_REQUEST_CODE:
				// TODO ѡ��Ƭ�ص�
				showShortToast("ѡ��Ƭ�ص�");
				break;
			}
		}
	}

	// ���ͷ���
	private void sendFeedback() {
		String contString = mEditTextContent.getText().toString();
		if (!contString.isEmpty()) { // �ı�����
			tempMsg = new tb_task_instructions("", taskID, contString, getUserId(),
					System.currentTimeMillis() + "", "1");

			// ���͵�������
			sendFb(tempMsg);

		} else { // ����Ϣ������ʾ
			new AlertDialog.Builder(ChatDetail.this).setTitle("���ܷ��Ϳհ׷���")
					.setPositiveButton("ȷ��", null).create().show();
		}
	}

	private void sendFb(tb_task_instructions data) {
		// uid Ϊ������Աid
		webRequestManager.createInsRequest(this, msgDao.getMsgReceivers(taskID), data.getTask_id(),
				data.getContent(), null, "1");

	}

	tb_task_instructions tempMsg;
	private ScheduledExecutorService scheduler;

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message handlerMsg) {

				switch (handlerMsg.what) {

				case Constants.CREATE_INS_SUCCESS:
					time_old = System.currentTimeMillis();
					// ˢ����ʾ
					hideInput();
					fbList.add(tempMsg);
					fbAdapter.notifyDataSetChanged();
					mEditTextContent.setText("");
					mListView.setSelection(mListView.getCount() - 1);
					Toast.makeText(ChatDetail.this, "������Ϣ�ɹ�", Toast.LENGTH_SHORT).show();

					break;
				case Constants.CREATE_INS_SAVE_FAIL:
				case Constants.CREATE_INS_FAIL:
					if (handlerMsg.obj != null) {
						showShortToast("����ʧ��:" + ((NormalServerResponse) handlerMsg.obj).getEc());
					} else {
						showShortToast("����ʧ��,�����Ƿ����������������");
					}
					break;

				case UPDATE_LIST:
					// List<tb_task_instructions> fbList

					newList = (List<tb_task_instructions>) handlerMsg.obj;

					if (newList.size() != 0) {

						if (fbList.size() == 0) {// ��ʾ�б�����û����Ϣ
							for (tb_task_instructions ins : newList) {
								fbList.add(ins);
							}

							fbAdapter.notifyDataSetChanged();
						} else {
							tb_task_instructions task = fbList.get(fbList.size() - 1);
							String time = task.getSend_time();
							System.out.println("time : " + time);
							for (tb_task_instructions ins : newList) {
								String insTime = ins.getSend_time();

								boolean isUp = isUpdate(time, insTime);
								// ���time < insTime�͸�������
								if (isUp) {
									if ((!getUserId().equals(ins.getSend_id()))) {
										fbList.add(ins);
										fbAdapter.notifyDataSetChanged();
										mListView.setSelection(fbList.size() - 1);
									}
								}
							}
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

		if (time.compareTo(insTime) < 0) {// dat1��dat2֮ǰ
			return true;
		} else {
			return false;
		}

	}

	@Override
	protected void onDestroy() {
		// ע��handler
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
		// ����ͼƬ�ڴ�
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

}