package nercms.schedule.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import com.Generate_md5;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.google.gson.Gson;
import com.nercms.MQTT;
import com.nercms.Push;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.dao.DatabaseHelper;
import android.wxapp.service.elec.dao.TaskInsDao;
import android.wxapp.service.elec.model.ConfigBean;
import android.wxapp.service.elec.model.LoginResponse;
import android.wxapp.service.elec.model.NormalServerResponse;
import android.wxapp.service.elec.model.UpdateResponse;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.Contants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.mqtt.MqttResponse;
import android.wxapp.service.util.MySharedPreference;
import nercms.schedule.R;
import nercms.schedule.utils.MyLog;
import nercms.schedule.utils.Utils;

/**
 * 
 * @author J 2015-12-29
 */
public class Login extends BaseActivity {

	private EditText etUserName; // 用户名编辑框
	private EditText etPassword; // 密码编辑框
	// String un = "admin";
	// String pwd = "admin";

	private Button btnLogin;// 登录按钮
	private Button btnUpdate;

	private static String inputUserName = null;
	private static String inputPassword = null;

	private Handler handler;

	private WebRequestManager webRequestManager;

	private String TAG = "Login";

	boolean isMiWen = true;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		// 强制db创建表
		DatabaseHelper dbHelper = DatabaseHelper.getInstance(this);
		dbHelper.onCreate(dbHelper.getWritableDatabase());

		///////////////////////////////////////////////////
		File configFile = new File(Environment.getExternalStorageDirectory().getPath()
				+ "/nercms-Schedule/config.json");
		// 如果配置文件不存在
		if (!configFile.exists()) {
			// 如果是第一次启动，则写配置文件
			if (MySharedPreference.get(this, MySharedPreference.USER_ID, null) == null) {
				FileWriter writer = null;
				try {
					writer = new FileWriter(configFile);
					ConfigBean bean = new ConfigBean(Contants.SERVER, Contants.PORT,
							Contants.HFS_PORT, Contants.MQTT_PORT, Contants.SCHEDULE_SERVER_LAN,
							Contants.SCHEDULE_PORT);
					writer.write(new Gson().toJson(bean));
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						if (writer != null)
							writer.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		// 如果配置文件存在,则读配置文件
		else {
			FileReader reader = null;
			try {
				reader = new FileReader(configFile);
				char[] buff = new char[1024];
				int i = 0;
				StringBuilder sb = new StringBuilder();
				while ((i = reader.read(buff, 0, buff.length)) > 0) {
					sb.append(String.valueOf(buff, 0, i));
				}
				ConfigBean bean = new Gson().fromJson(sb.toString(), ConfigBean.class);
				Contants.PORT = bean.getPORT();
				Contants.SERVER = bean.getSERVER();
				Contants.HFS_PORT = bean.getHFS_PORT();
				Contants.MQTT_PORT = bean.getMQTT_PORT();
				Contants.SCHEDULE_SERVER_LAN = bean.getSCHEDULE_SERVER_LAN();
				Contants.SCHEDULE_PORT = bean.getSCHEDULE_PORT();
				Contants.HFS_SERVER = Contants.SERVER;
				Contants.HFS_URL = "http://" + Contants.HFS_SERVER + ":" + Contants.HFS_PORT
						+ "/ScheduleFileServer";
				Contants.MQTT_SERVER = Contants.SERVER;
				Contants.SCHEDULE_SERVER_WAN = Contants.SERVER;
				Contants.SERVER_URL = "http://" + Contants.SERVER + ":" + Contants.PORT
						+ File.separator;
				Log.v("login", bean.toString());
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		//////////////////////////////////////////////////////

		// // this is test
		// initActionBar();
		// // 用户测试使用，直接跳过本Activity
		// startActivity(MainContent.class);
		// return;

		webRequestManager = new WebRequestManager(AppApplication.getInstance(), Login.this);

		initActionBar();

		etUserName = (EditText) findViewById(R.id.login_user_edit);
		etPassword = (EditText) findViewById(R.id.login_passwd_edit);
		// etUserName.setText(un);
		// etPassword.setText(pwd);

		btnLogin = (Button) findViewById(R.id.login_login_btn);
		btnUpdate = (Button) findViewById(R.id.update);

		btnUpdate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(Contants.HFS_URL + File.separator + "Schedule.apk"));
				startActivity(i);
			}
		});

		// 默认显示上次登录的用户ID
		etUserName.setText(MySharedPreference.get(Login.this, MySharedPreference.USER_NAME, ""));
		etPassword.setText(MySharedPreference.get(Login.this, MySharedPreference.USER_IC, ""));
		// 如果更改了密码框，则标记为明文，否则以密文进行处理
		etPassword.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				isMiWen = false;
			}
		});

		// 如果用户名为空，则聚焦用户名，否则聚焦密码
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			etUserName.requestFocus();
		} else {
			etPassword.requestFocus();
		}

		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MyLog.i(TAG, "登录按钮点击");
				login_mainschedule();
			}
		});

		// 注册或者重新注册Handler
		initHandler();
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
		Log.v("Login", "regist handler");
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				// 登录成功
				case Constants.LOGIN_SUCCESS:

					// Log.v("Login", "handleMessage UserName: " + inputUserName
					// + " "
					// + inputUserName.hashCode());
					// Log.v("Login", "handleMessage UserPsd: " + inputPassword
					// + " "
					// + inputPassword.hashCode());

					// 接收用户ID
					String userID = ((LoginResponse) msg.obj).getUid();
					// 保存用户id
					MySharedPreference.save(Login.this, MySharedPreference.USER_ID, userID);
					// 保存用户的登录名
					MySharedPreference.save(Login.this, MySharedPreference.USER_NAME,
							inputUserName.toLowerCase());
					// 保存用户密码
					MySharedPreference.save(Login.this, MySharedPreference.USER_IC, inputPassword);

					webRequestManager.loginUpdate(Login.this);

					// 写日志
					MyLog.i(TAG, "用户：" + userID + " 登陆成功");
					break;
				// 登录 失败
				case Constants.LOGIN_FAIL:

					MyLog.i(TAG, "登录失败");
					dismissProgressDialog();
					if (msg.obj != null) {
						// String errorCode = ((NormalServerResponse)
						// msg.obj).getEc();
						// showAlterDialog("登录失败", Utils.getErrorMsg(errorCode),
						// R.drawable.login_error_icon, "确定", null);
						showAlterDialog("登录失败", ((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "确定", null);
					} else {
						showAlterDialog("登录失败", "请检查是否与服务器连接正常", R.drawable.login_error_icon, "确定",
								null);
					}
					break;

				case Constants.LOGIN_UPDATE_FAIL:
					dismissProgressDialog();
					if (msg.obj != null) {
						// String errorCode = ((NormalServerResponse)
						// msg.obj).getEc();
						// showAlterDialog("登录失败", Utils.getErrorMsg(errorCode),
						// R.drawable.login_error_icon, "确定", null);
						showAlterDialog("登录失败", ((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "确定", null);

					} else {
						showLongToast("网络中断，请检查是否与服务器连接正常");
						dismissProgressDialog();

						// Intent intent = new Intent(Login.this,
						// MainContent.class);
						// Login.this.startActivity(intent);
						Bundle bundle = new Bundle();
						bundle.putInt("tag", 0);
						ScheduleActivity.wakeUp(Login.this, bundle);
						Login.this.finish();
					}
					break;

				case Constants.LOGIN_UPDATE_SUCCESS:
					dismissProgressDialog();
					showLog_v("更新完成《《《《《《《《《《《《《《《");

					MQTT.SERVER_URL = "tcp://" + Contants.MQTT_SERVER + ":" + Contants.MQTT_PORT;
					MQTT.CLIENT_ID = "m_" + getUserId();
					MQTT.get_instance().publish_message(
							MQTT.SUBSCRIBE_TOPIC_PREFIX + "m_" + getUserId(), "This is test", 1);

					// // MQTT
					// // 新建线程去进行MQTT连接
					// new Thread(new Runnable() {
					// @Override
					// public void run() {
					// Looper.prepare();
					// Push.PERSON_ID = getUserId();
					// Push.get_instance(AppApplication.getInstance()).ini();

					// Push.get_instance(Login.this)
					// .pushMsgToTag("nercms/schedule/m_" + getUserId(),
					// "123123", 1);
					// }
					// }).start();

					// Intent intent = new Intent(Login.this,
					// MainContent.class);
					// Login.this.startActivity(intent);
					Bundle bundle = new Bundle();
					bundle.putInt("tag", 0);
					ScheduleActivity.wakeUp(Login.this, bundle);
					// Login.this.finish();
					break;

				case Constants.LOGIN_UPDATE_SAVE_FAIL:
					dismissProgressDialog();
					showAlterDialog("登录失败", "存储失败", R.drawable.login_error_icon, "确定", null);
					break;
				default:
					Log.e("LoginActivity", msg.what + "<<<<未处理");
					break;

				case Constants.MQTT_UPDATE_SUCCESS:
					Log.v("MQTT", "Login : MQTT_UPDATE_SUCCESS");
					Map<Integer, Object> result = (Map<Integer, Object>) msg.obj;
					MqttResponse response = (MqttResponse) result.get(1);
					Class target = null;
					Bundle b = null;
					b = new Bundle();
					String content = "";
					target = TaskSelectorActivity.class;
					if (response != null) {
						if (response.getType().equals("1")) {
							// 开始和结束任务推送给领导
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							content += "您有新的任务";
						} else if (response.getType().equals("2")) {
							// 新建指令时进行推送给任务负责人
							b.putString("tid", new TaskInsDao(AppApplication.getInstance())
									.getTaskId(response.getId()));
							content += "您有新的消息";
						} else if (response.getType().equals("3")) {
							// 上传附件后，推送给应上岗到位领导，如果没有就不推送
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							content += "您有新的任务附件";
						} else if (response.getType().equals("4")) {
							// 上传附件后，推送给应上岗到位领导，如果没有就不推送
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							content += "任务已开始";
						} else if (response.getType().equals("5")) {
							// 上传附件后，推送给应上岗到位领导，如果没有就不推送
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							content += "任务已结束";
						} else if (response.getType().equals("6")) {
							// 修改后的任务
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							content += "任务已被修改，请查看";
						}
					}

					if (TextUtils.isEmpty(b.getString("tid"))) {
						// showLongToast("服务器无推送数据");
						showShortToast("服务器无推送数据:\n" + (String) result.get(2));
						showAlterDialog("服务器无推送数据:\n" + (String) result.get(2));
					} else
						showNotification(AppApplication.getInstance(), target, b, content,
								"江陵县云检系统", content);
					break;
				case Constants.MQTT_UPDATE_FAIL:
					break;

				}

			}
		};
		MQTT.handler = handler;
		// 注册Handler
		registHandler();
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

		// if (!checkInternet()) {
		// dismissProgressDialog();
		// return;
		// }

		if (!checkSDCard()) {
			dismissProgressDialog();
			return;
		}

		inputUserName = etUserName.getText().toString().trim();
		Log.v("Login", "UserName: " + inputUserName + ":" + inputUserName.hashCode());
		inputPassword = etPassword.getText().toString().trim();
		Log.v("Login", "UserPsd: " + inputPassword + ":" + inputPassword.hashCode());

		if (inputUserName == null || inputUserName.equals("") || inputPassword == null
				|| inputPassword.equals("")) {
			dismissProgressDialog();
			showAlterDialog("登录错误", "帐号或者密码不能为空，\n请输入后再登录！", R.drawable.login_error_icon, "确定",
					null);
			return;
		}

		// 无网络情况
		if (!Utils.isNetworkAvailable(Login.this)) {
			if (getUserIc() != null && getUserId() != null) {
				if (getUserName().toLowerCase().equals(inputUserName.toLowerCase())) {
					if (!isMiWen)
						inputPassword = Generate_md5.generate_md5(inputPassword);
					if (getUserIc().equals(inputPassword))
						webRequestManager.loginUpdate(Login.this);
					else {
						dismissProgressDialog();
						showAlterDialog("登录错误", "帐号或者密码不能为空，\n请输入后再登录！",
								R.drawable.login_error_icon, "确定", null);
					}
				} else {
					dismissProgressDialog();
					showAlterDialog("登录错误", "请检查网络连接状态", R.drawable.login_error_icon, "确定", null);
				}
			} else {
				dismissProgressDialog();
				showAlterDialog("登录错误", "请检查网络连接状态", R.drawable.login_error_icon, "确定", null);
			}

		}
		// 有网络情况
		else {
			// // 服务器ping不通
			// if (!Utils.serverPing(Contants.SERVER)) {
			// showShortToast("服务器错误");
			// } else {
			if (!isMiWen)
				inputPassword = Generate_md5.generate_md5(inputPassword);
			webRequestManager.login(inputUserName.toLowerCase(), inputPassword);
			// }
		}

		// //如果是第一次登录
		// if (getUserIc() == null || getUserId() == null) {
		// webRequestManager.login(inputUserName.toLowerCase(),
		// Generate_md5.generate_md5(inputPassword));
		// } else {
		// if (!getUserName().equals(inputUserName.toLowerCase())) {
		// if (Utils.isNetworkAvailable(Login.this)) {
		// webRequestManager.login(inputUserName.toLowerCase(),
		// Generate_md5.generate_md5(inputPassword));
		// } else {
		// dismissProgressDialog();
		// showAlterDialog("登录错误", "请检查网络连接状态", R.drawable.login_error_icon,
		// "确定", null);
		// }
		// } else {
		// if (getUserName().toLowerCase().equals(inputUserName.toLowerCase())
		// && getUserIc().equals(Generate_md5.generate_md5(inputPassword))) {
		// webRequestManager.loginUpdate(Login.this);
		// } else {
		// dismissProgressDialog();
		// showAlterDialog("登录错误", "密码错误", R.drawable.login_error_icon, "确定",
		// null);
		// }
		// }
		// }

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
	public void onBackPressed() {
		finish();
		System.exit(0);
		super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// 点击返回键
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (MySharedPreference.get(Login.this, MySharedPreference.USER_NAME, "").isEmpty()) {

			} else {
				// 如果正在登录，取消登录
				dismissProgressDialog();
				AppApplication.getInstance().myQueue.cancelAll(this);
				Utils.showShortToast(this, "取消登录");
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.v("Login", "OnPause");
	}

	@Override
	protected void onDestroy() {
		Log.v("Login", "onDestroy,注册Handler");
		super.onDestroy();
		// MessageHandlerManager.getInstance().unregister(Constants.LOGIN_SUCCESS,
		// LoginResponse.class.getName());
		// MessageHandlerManager.getInstance().unregister(Constants.LOGIN_FAIL,
		// LoginResponse.class.getName());
		//
		// MessageHandlerManager.getInstance().unregister(Constants.LOGIN_UPDATE_SUCCESS,
		// UpdateResponse.class.getName());
		// MessageHandlerManager.getInstance().unregister(Constants.LOGIN_UPDATE_SAVE_FAIL,
		// UpdateResponse.class.getName());
		// MessageHandlerManager.getInstance().unregister(Constants.LOGIN_UPDATE_FAIL,
		// UpdateResponse.class.getName());

	}

	// 注册Handler
	private void registHandler() {

		// MessageHandlerManager.getInstance().register(handler,
		// Constants.MQTT_UPDATE_SUCCESS,
		// UpdateResponse.class.getName());
		// MessageHandlerManager.getInstance().register(handler,
		// Constants.MQTT_UPDATE_FAIL,
		// UpdateResponse.class.getName());

		MessageHandlerManager.getInstance().register(handler, Constants.LOGIN_SUCCESS,
				LoginResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.LOGIN_FAIL,
				LoginResponse.class.getName());

		MessageHandlerManager.getInstance().register(handler, Constants.LOGIN_UPDATE_SUCCESS,
				UpdateResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.LOGIN_UPDATE_SAVE_FAIL,
				UpdateResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.LOGIN_UPDATE_FAIL,
				UpdateResponse.class.getName());
	}

	private void showNotification(Context c, Class<?> target, Bundle b, String trick, String title,
			String content) {
		NotificationManager nm = (NotificationManager) c
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification n = new Notification(R.drawable.ic_launcher, trick,
				System.currentTimeMillis());
		n.flags = Notification.FLAG_AUTO_CANCEL;
		n.defaults = Notification.DEFAULT_ALL;
		long[] vibrate = { 0, 100, 200, 300 };
		n.vibrate = vibrate;
		Intent i = new Intent(c, target);
		if (b != null)
			i.putExtras(b);
		i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		// PendingIntent
		PendingIntent contentIntent = PendingIntent.getActivity(c, R.string.app_name, i,
				PendingIntent.FLAG_UPDATE_CURRENT);

		n.setLatestEventInfo(c, title, content, contentIntent);
		nm.notify(R.string.app_name, n);
	}
}
