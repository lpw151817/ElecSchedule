package nercms.schedule.activity;

import java.io.File;
import java.text.BreakIterator;

import org.acra.collector.CrashReportData;

import nercms.schedule.R;
import nercms.schedule.service.UpdateService;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.MyLog;
import nercms.schedule.utils.Utils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.text.TextUtils.EllipsizeCallback;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.DatabaseHelper;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.jerry.model.person.GetOrgCodePersonResponse;
import android.wxapp.service.jerry.model.person.GetOrgCodeResponse;
import android.wxapp.service.jerry.model.person.GetPersonInfoResponse;
import android.wxapp.service.jerry.model.person.LoginResponse;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.thread.SaveAffairThread;
import android.wxapp.service.thread.SaveAffairUpdateThread;
import android.wxapp.service.thread.SaveConferenceThread;
import android.wxapp.service.thread.SaveMessageUpdateThread;
import android.wxapp.service.thread.SaveOrgCodePersonThread;
import android.wxapp.service.thread.SaveOrgCodeThread;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nercms.ICallBacks;
import com.nercms.Push;

/**
 * @author jiaocuina@gmail.com
 * @date 2014-03-02
 * @version V1.0
 * @description 输入用户名密码 登录系统
 *
 * @version V1.1
 * @author WEIHAO
 * @date 2014-6-25
 * @new 用户登录一系列的逻辑修正； 加载进度条显示，联系人下载保存完成后跳转
 * @version V1.3
 * @new 登录成功后，注册MQTT
 */
public class Login extends BaseActivity {

	private EditText etUserName; // 用户名编辑框
	private EditText etPassword; // 密码编辑框
	// String un = "fm";
	// String pwd = "123456";

	private Button btnLogin;// 登录按钮

	private String inputUserName = null;
	private String inputPassword = null;

	private Handler handler;

	private WebRequestManager webRequestManager;

	private String TAG = "Login";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		// this is test
		// // 用户测试使用，直接跳过本Activity
		// startActivity(Main.class);
		// return;

		Log.v("Login", "Login onCreate");
		webRequestManager = new WebRequestManager(AppApplication.getInstance(), Login.this);

		initActionBar();

		etUserName = (EditText) findViewById(R.id.login_user_edit);
		etPassword = (EditText) findViewById(R.id.login_passwd_edit);
		// etUserName.setText(un);
		// etPassword.setText(pwd);

		btnLogin = (Button) findViewById(R.id.login_login_btn);
		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				inputUserName = etUserName.getText().toString().trim();
				inputPassword = etPassword.getText().toString().trim();
				String imsi = Utils.getIMSI(Login.this);
				new android.wxapp.service.elec.request.WebRequestManager(
						AppApplication.getInstance(), Login.this).login(inputUserName,
								inputPassword, imsi);
			}
		});

		// // 默认显示上次登录的用户ID
		// etUserName.setText(MySharedPreference.get(Login.this,
		// MySharedPreference.USER_NAME, ""));
		//
		// // 如果用户名为空，则聚焦用户名，否则聚焦密码
		// if (TextUtils.isEmpty(etUserName.getText().toString())) {
		// etUserName.requestFocus();
		// } else {
		// etPassword.requestFocus();
		// }
		//
		// btnLogin.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View arg0) {
		// MyLog.i(TAG, "登录按钮点击");
		// login_mainschedule();
		// }
		// });
		//
		// // 注册或者重新注册Handler
		// initHandler();
		// Log.v("Login", "OnResume,注册或者重新注册Handler");
	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("登录");

		getSupportActionBar().hide();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// 设置按钮
		MenuItem setting = menu.add(0, 1, 0, "设置");
		setting.setIcon(R.drawable.ofm_setting_icon);
		setting.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		setting.setVisible(false);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case 1:
			startActivity(new Intent(Login.this, Setting.class));
			break;

		default:
			break;
		}
		return true;
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				// 登录成功
				case Constant.LOGIN_REQUEST_SUCCESS:

					// 接收用户ID
					String userID = ((LoginResponse) msg.obj).getUid();
					// 保存用户id
					MySharedPreference.save(Login.this, MySharedPreference.USER_ID, userID);
					// 保存用户的登录名
					MySharedPreference.save(Login.this, MySharedPreference.USER_NAME,
							inputUserName);
					// 保存用户密码
					MySharedPreference.save(Login.this, MySharedPreference.USER_IC, inputPassword);

					// // 新建线程去进行MQTT连接
					// new Thread(new Runnable() {
					// @Override
					// public void run() {
					// Looper.prepare();
					Push.PERSON_ID = getUserId();
					Push.get_instance(Login.this).ini();
					// }
					// }).start();

					// 写一个Timer定时请求服务器是否有更新数据
					getOrgInfoUpdate();
					getAffairUpdate();
					getMessageUpdate();
					// conference的更新操作
					getConferenceUpdate();
					// group的更新
					getGroupUpdate();
					// // TODO gps update
					// getGpsUpdate();

					// 获取个人信息数据
					GetPersonInfoResponse mPersonInfo = DAOFactory.getInstance()
							.getPersonDao(Login.this).getCustomer();
					// 如果没有数据，则先进行网络请求
					if (mPersonInfo == null) {
						webRequestManager.GetPersonInfo(getUserId());
					}

					// 写日志
					MyLog.i(TAG, "用户：" + userID + " 登陆成功");
					break;
				// 登录 失败
				case Constant.LOGIN_REQUEST_FAIL:
					MyLog.i(TAG, "登录失败");
					dismissProgressDialog();
					if (msg.obj != null) {
						String errorCode = ((NormalServerResponse) msg.obj).getEc();
						showAlterDialog("登录失败", Utils.getErrorMsg(errorCode),
								R.drawable.login_error_icon, "确定", null);
					} else {
						showAlterDialog("登录失败", "请检查是否与服务器连接正常", R.drawable.login_error_icon, "确定",
								null);
					}
					break;
				// 保存orgcode失败
				case Constant.SAVE_ORG_CODE_FAIL:
					MyLog.e(TAG, "保存orgcode失败");
					break;
				// 保存orgcode成功
				case Constant.SAVE_ORG_CODE_SUCCESS:
					MyLog.e(TAG, "保存orgcode成功");
					isGetOrgCode = true;
					startInMain();
					break;
				// 获取组织结点失败
				case Constant.QUERY_ORG_NODE_REQUEST_FAIL:
					MyLog.i(TAG, "获取结点失败");
					dismissProgressDialog();
					showAlterDialog("获取结点失败",
							Utils.getErrorMsg(((NormalServerResponse) msg.obj).getEc()),
							R.drawable.login_error_icon);
					break;
				// 存储orgperson成功
				case Constant.SAVE_ORG_PERSON_SUCCESS:
					MyLog.e(TAG, "保存orgperson成功");
					isGetOrgPerson = true;
					startInMain();
					break;
				// 存储orgperson失败
				case Constant.SAVE_ORG_PERSON_FAIL:
					MyLog.e(TAG, "保存orgperson失败");
					break;
				// 获取orgperson失败
				case Constant.QUERY_ORG_PERSON_REQUEST_FAIL:
					showAlterDialog("获取orgperson失败",
							Utils.getErrorMsg(((NormalServerResponse) msg.obj).getEc()),
							R.drawable.login_error_icon);
					break;
				// 保存联系人完成
				case Constant.SAVE_ALL_PERSON_SUCCESS:
					// 5.跳转到主界面
					startInMain();
					break;
				// 保存事务成功
				case Constant.SAVE_TASK_SUCCESS:
					isGetAffair = true;
					startInMain();
					break;
				// 保存消息成功
				case Constant.SAVE_MESSAGE_SUCCESS:
					isGetMessage = true;
					startInMain();
					break;
				case Constant.UPDATE_MESSAGE_REQUEST_FAIL:
					dismissProgressDialog();
					Toast.makeText(Login.this, "Message Update Failed", Toast.LENGTH_LONG).show();
					break;
				case Constant.CONFERENCE_SAVE_SUCCESS:
					isGetConference = true;
					startInMain();
					break;
				case Constant.CONFERENCE_SAVE_FAIL:
					dismissProgressDialog();
					Toast.makeText(Login.this, "Conference Update Failed", Toast.LENGTH_LONG)
							.show();
					break;
				case Constant.GROUP_SAVE_SECCESS:
					isGetGroup = true;
					startInMain();
					break;
				case Constant.GROUP_SAVE_FAIL:
					dismissProgressDialog();
					Toast.makeText(Login.this, "Group Update Failed", Toast.LENGTH_LONG).show();
					break;
				case Constant.SAVE_GPS_SECCESS:
					isGetGps = true;
					startInMain();
					break;
				case Constant.SAVE_GPS_FAIL:
					dismissProgressDialog();
					Toast.makeText(Login.this, "GPS Update Failed", Toast.LENGTH_LONG).show();
				default:
					Log.e("LoginActivity", msg.what + "<<<<未处理");
					break;
				}

			}
		};
		// 注册Handler
		registHandler();
	}

	// TODO 验证需要更改
	private void startInMain() {
		if (isGetAffair && isGetMessage && isGetOrgCode && isGetOrgPerson && isGetConference
				&& isGetGroup && isGetGps) {
			dismissProgressDialog();
			startActivity(new Intent(Login.this, Main.class));
			// 打开定时更新的service
			// // TODO 总是返回所有数据，而不是根据时间来返回
			// startService(new Intent(Login.this, UpdateService.class));
			Login.this.finish();
		}
	}

	boolean isGetOrgCode = false;
	boolean isGetOrgPerson = false;
	boolean isGetAffair = false;
	boolean isGetMessage = false;
	boolean isGetConference = false;
	boolean isGetGroup = false;
	// TODO
	boolean isGetGps = true;

	private void getGpsUpdate() {
		if (MySharedPreference.get(this, MySharedPreference.LAST_UPDATE_GPS_TIMESTAMP,
				null) == null) {
			webRequestManager.getGpsUpdateRequest("1");
		} else {
			isGetGroup = true;
			startInMain();
		}
	}

	private void getGroupUpdate() {
		if (MySharedPreference.get(this, MySharedPreference.LAST_UPDATE_GROUP_TIMESTAMP,
				null) == null)
			webRequestManager.getGroupUpdateRequest("1");
		else {
			isGetGroup = true;
			startInMain();
		}
	}

	// 获取组织相关信息并存入数据库
	private void getOrgInfoUpdate() {
		if (MySharedPreference.get(this, MySharedPreference.LAST_UPDATE_ORGCODE_TIMESTAMP,
				null) == null)
			webRequestManager.getOrgCodeUpdate();
		else {

			isGetOrgCode = true;
			startInMain();
		}
		if (MySharedPreference.get(this, MySharedPreference.LAST_UPDATE_ORGPERSON_TIMESTAMP,
				null) == null)
			webRequestManager.getOrgPersonUpdate();
		else {
			isGetOrgPerson = true;
			startInMain();
		}
	}

	private void getAffairUpdate() {
		if (MySharedPreference.get(this, MySharedPreference.LAST_UPDATE_TASK_TIMESTAMP,
				null) == null) {
			// 获取全部数据
			webRequestManager.getAffairUpdate("1");
		} else {
			isGetAffair = true;
			startInMain();
		}
	}

	private void getMessageUpdate() {
		if (MySharedPreference.get(this, MySharedPreference.LAST_UPDATE_MESSAGE_TIMESTAMP,
				null) == null)
			// 获取全部数据
			webRequestManager.getMessageUpdate("1");
		else {
			isGetMessage = true;
			startInMain();
		}
	}

	private void getConferenceUpdate() {
		if (MySharedPreference.get(this, MySharedPreference.LAST_UPDATE_CONFERENCE_TIMESTAMP,
				null) == null) {
			webRequestManager.updateConference("1");
		} else {
			isGetConference = true;
			startInMain();
		}
	}

	// 判断用户名与密码符合要求则登录成功
	public void login_mainschedule() {

		// 2014-6-24 WeiHao
		// 1.显示进度条
		mProgressDialog.setCancelable(true);
		mProgressDialog.setCanceledOnTouchOutside(false);
		showProgressDialog("正在登录", "努力加载数据中...请稍后~");
		// 隐藏软键盘
		InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(
				Context.INPUT_METHOD_SERVICE);
		mInputMethodManager.hideSoftInputFromWindow(etPassword.getWindowToken(), 0);

		if (!checkInternet()) {
			dismissProgressDialog();
			return;
		}

		if (!checkSDCard()) {
			dismissProgressDialog();
			return;
		}

		inputUserName = etUserName.getText().toString().trim();
		inputPassword = etPassword.getText().toString().trim();
		String imsi = Utils.getIMSI(Login.this);

		if (inputUserName == null || inputUserName.equals("") || inputPassword == null
				|| inputPassword.equals("")) {
			dismissProgressDialog();
			new AlertDialog.Builder(Login.this)
					.setIcon(getResources().getDrawable(R.drawable.login_error_icon))
					.setTitle("登录错误").setMessage("帐号或者密码不能为空，\n请输入后再登录！").create().show();
			return;
		}
		// 请求服务器进行登录验证
		webRequestManager.loginVarification(inputUserName, inputPassword, imsi);
	}

	@Override
	protected void onResume() {

		super.onResume();
	}

	public boolean checkSDCard() {
		// SD卡判断并提示
		if (Utils.isExistSDCard()) {
			if (Utils.getSDFreeSize() < 200) { // 存储空间小于200MB
				Toast.makeText(Login.this, "您的SD卡存储空间不足，请及时清理，避免操作受限！", Toast.LENGTH_SHORT).show();
				return false;
			} else { // SD卡挂载且空间足够，附件目录准备
				// 创建一个文件夹对象，赋值为外部存储器的目录
				File sdcardDir = Environment.getExternalStorageDirectory();
				// 得到一个路径，内容是sdcard的附件路径
				String path1 = sdcardDir.getPath() + "/nercms-Schedule/Attachments";
				File filePath1 = new File(path1);
				// 得到一个路径，内容是sdcard的附件缩略图路径
				String path2 = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail";
				File filePath2 = new File(path2);

				if (!filePath1.exists()) {
					// 若不存在，创建目录
					filePath1.mkdirs();
				}

				if (!filePath2.exists()) {
					filePath2.mkdirs();
				}
				return true;
			}
		} else {
			Toast.makeText(Login.this, "未检测到SD卡，操作将受限！", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private boolean checkInternet() {
		ConnectivityManager connectivityManager;
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {
			String name = info.getTypeName();
			Log.d(TAG, "当前网络名称：" + name);
			// ping应用服务器
			// if (Utils.serverPing()) {
			// return true;
			// } else {
			// Toast.makeText(Login.this, "网络连接不可用", Toast.LENGTH_SHORT)
			// .show();
			// return false;
			// }
			return true;
		} else {
			Toast.makeText(Login.this, "无网络访问", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 点击返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// 如果正在登录，取消登录
			dismissProgressDialog();
			AppApplication.getInstance().myQueue.cancelAll(this);
			Utils.showShortToast(this, "取消登录");
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// 注销Handler
		MessageHandlerManager.getInstance().unregister(Constant.LOGIN_REQUEST_FAIL,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().unregister(Constant.LOGIN_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_ALL_PERSON_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().unregister(Constant.QUERY_ORG_NODE_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_GET_ORG_CODE);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_ORG_CODE_SUCCESS,
				SaveOrgCodeThread.TAG);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_ORG_CODE_FAIL,
				SaveOrgCodeThread.TAG);
		MessageHandlerManager.getInstance().unregister(Constant.QUERY_ORG_NODE_REQUEST_FAIL,
				Contants.METHOD_PERSON_GET_ORG_CODE);

		MessageHandlerManager.getInstance().unregister(Constant.SAVE_ORG_PERSON_SUCCESS,
				SaveOrgCodePersonThread.TAG);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_ORG_PERSON_FAIL,
				SaveOrgCodePersonThread.TAG);
		MessageHandlerManager.getInstance().unregister(Constant.QUERY_ORG_PERSON_REQUEST_FAIL,
				Contants.METHOD_PERSON_GET_ORG_PERSON);
		MessageHandlerManager.getInstance().unregister(Constant.UPDATE_TASK_LIST_REQUEST_SUCCESS,
				Contants.METHOD_AFFAIRS_UPDATE_LIST);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_TASK_SUCCESS,
				SaveAffairUpdateThread.TAG);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_MESSAGE_SUCCESS,
				SaveMessageUpdateThread.TAG);
		MessageHandlerManager.getInstance().unregister(Constant.UPDATE_MESSAGE_REQUEST_FAIL,
				Contants.METHOD_MESSAGE_UPDATE);
		MessageHandlerManager.getInstance().unregister(Constant.CONFERENCE_SAVE_SUCCESS,
				SaveConferenceThread.TAG);
		MessageHandlerManager.getInstance().unregister(Constant.CONFERENCE_SAVE_FAIL,
				SaveConferenceThread.TAG);
		MessageHandlerManager.getInstance().unregister(Constant.GROUP_SAVE_SECCESS,
				Contants.METHOD_GROUP_UPDATE);
		MessageHandlerManager.getInstance().unregister(Constant.GROUP_SAVE_FAIL,
				Contants.METHOD_GROUP_UPDATE);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_GPS_SECCESS,
				Contants.METHOD_GPS_UPDAET);
		MessageHandlerManager.getInstance().unregister(Constant.SAVE_GPS_FAIL,
				Contants.METHOD_GPS_UPDAET);
		MessageHandlerManager.getInstance().unregister(Constant.QUERY_GPSS_REQUEST_FAIL,
				Contants.METHOD_GPS_UPDAET);
		Log.v("Login", "onDestroy,注册Handler");
		super.onDestroy();
	}

	// 注册Handler
	private void registHandler() {
		// 登陆失败
		MessageHandlerManager.getInstance().register(handler, Constant.LOGIN_REQUEST_FAIL,
				Contants.METHOD_PERSON_LOGIN);
		// 登陆成功
		MessageHandlerManager.getInstance().register(handler, Constant.LOGIN_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_ALL_PERSON_SUCCESS,
				Contants.METHOD_PERSON_LOGIN);

		MessageHandlerManager.getInstance().register(handler,
				Constant.QUERY_ORG_NODE_REQUEST_SUCCESS, Contants.METHOD_PERSON_GET_ORG_CODE);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_ORG_CODE_SUCCESS,
				SaveOrgCodeThread.TAG);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_ORG_CODE_FAIL,
				SaveOrgCodeThread.TAG);
		MessageHandlerManager.getInstance().register(handler, Constant.QUERY_ORG_NODE_REQUEST_FAIL,
				Contants.METHOD_PERSON_GET_ORG_CODE);

		MessageHandlerManager.getInstance().register(handler,
				Constant.QUERY_ORG_PERSON_REQUEST_SUCCESS, Contants.METHOD_PERSON_GET_ORG_PERSON);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_ORG_PERSON_SUCCESS,
				SaveOrgCodePersonThread.TAG);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_ORG_PERSON_FAIL,
				SaveOrgCodePersonThread.TAG);
		MessageHandlerManager.getInstance().register(handler,
				Constant.QUERY_ORG_PERSON_REQUEST_FAIL, Contants.METHOD_PERSON_GET_ORG_PERSON);
		MessageHandlerManager.getInstance().register(handler,
				Constant.UPDATE_TASK_LIST_REQUEST_SUCCESS, Contants.METHOD_AFFAIRS_UPDATE_LIST);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_TASK_SUCCESS,
				SaveAffairUpdateThread.TAG);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_MESSAGE_SUCCESS,
				SaveMessageUpdateThread.TAG);
		MessageHandlerManager.getInstance().register(handler, Constant.UPDATE_MESSAGE_REQUEST_FAIL,
				Contants.METHOD_MESSAGE_UPDATE);
		MessageHandlerManager.getInstance().register(handler, Constant.CONFERENCE_SAVE_SUCCESS,
				SaveConferenceThread.TAG);
		MessageHandlerManager.getInstance().register(handler, Constant.CONFERENCE_SAVE_FAIL,
				SaveConferenceThread.TAG);
		MessageHandlerManager.getInstance().register(handler, Constant.GROUP_SAVE_SECCESS,
				Contants.METHOD_GROUP_UPDATE);
		MessageHandlerManager.getInstance().register(handler, Constant.GROUP_SAVE_FAIL,
				Contants.METHOD_GROUP_UPDATE);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_GPS_SECCESS,
				Contants.METHOD_GPS_UPDAET);
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_GPS_FAIL,
				Contants.METHOD_GPS_UPDAET);
		MessageHandlerManager.getInstance().register(handler, Constant.QUERY_GPSS_REQUEST_FAIL,
				Contants.METHOD_GPS_UPDAET);
	}
}
