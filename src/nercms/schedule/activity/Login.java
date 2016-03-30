package nercms.schedule.activity;

import java.io.File;

import com.Generate_md5;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.dao.TaskInsDao;
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

	private EditText etUserName; // �û����༭��
	private EditText etPassword; // ����༭��
	// String un = "admin";
	// String pwd = "admin";

	private Button btnLogin;// ��¼��ť

	private String inputUserName = null;
	private String inputPassword = null;

	private Handler handler;

	private WebRequestManager webRequestManager;

	private String TAG = "Login";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		// // this is test
		// initActionBar();
		// // �û�����ʹ�ã�ֱ��������Activity
		// startActivity(MainContent.class);
		// return;

		Log.v("Login", "Login onCreate");
		webRequestManager = new WebRequestManager(AppApplication.getInstance(), Login.this);

		initActionBar();

		etUserName = (EditText) findViewById(R.id.login_user_edit);
		etPassword = (EditText) findViewById(R.id.login_passwd_edit);
		// etUserName.setText(un);
		// etPassword.setText(pwd);

		btnLogin = (Button) findViewById(R.id.login_login_btn);

		// Ĭ����ʾ�ϴε�¼���û�ID
		etUserName.setText(MySharedPreference.get(Login.this, MySharedPreference.USER_NAME, ""));
		// TODO �˴���������ʹ��
		etPassword.setText(MySharedPreference.get(Login.this, MySharedPreference.USER_NAME, ""));

		// ����û���Ϊ�գ���۽��û���������۽�����
		if (TextUtils.isEmpty(etUserName.getText().toString())) {
			etUserName.requestFocus();
		} else {
			etPassword.requestFocus();
		}

		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				MyLog.i(TAG, "��¼��ť���");
				login_mainschedule();
			}
		});

		// ע���������ע��Handler
		initHandler();
		Log.v("Login", "OnResume,ע���������ע��Handler");
	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setTitle("��¼");
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
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				// ��¼�ɹ�
				case Constants.LOGIN_SUCCESS:

					// �����û�ID
					String userID = ((LoginResponse) msg.obj).getUid();
					// �����û�id
					MySharedPreference.save(Login.this, MySharedPreference.USER_ID, userID);
					// �����û��ĵ�¼��
					MySharedPreference.save(Login.this, MySharedPreference.USER_NAME,
							inputUserName.toLowerCase());
					// �����û�����
					MySharedPreference.save(Login.this, MySharedPreference.USER_IC,
							Generate_md5.generate_md5(inputPassword));

					webRequestManager.loginUpdate(Login.this);

					// д��־
					MyLog.i(TAG, "�û���" + userID + " ��½�ɹ�");
					break;
				// ��¼ ʧ��
				case Constants.LOGIN_FAIL:

					MyLog.i(TAG, "��¼ʧ��");
					dismissProgressDialog();
					if (msg.obj != null) {
						// String errorCode = ((NormalServerResponse)
						// msg.obj).getEc();
						// showAlterDialog("��¼ʧ��", Utils.getErrorMsg(errorCode),
						// R.drawable.login_error_icon, "ȷ��", null);
						showAlterDialog("��¼ʧ��", ((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "ȷ��", null);
					} else {
						showAlterDialog("��¼ʧ��", "�����Ƿ����������������", R.drawable.login_error_icon, "ȷ��",
								null);
					}
					break;

				case Constants.LOGIN_UPDATE_FAIL:
					dismissProgressDialog();
					if (msg.obj != null) {
						// String errorCode = ((NormalServerResponse)
						// msg.obj).getEc();
						// showAlterDialog("��¼ʧ��", Utils.getErrorMsg(errorCode),
						// R.drawable.login_error_icon, "ȷ��", null);
						showAlterDialog("��¼ʧ��", ((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "ȷ��", null);
					} else {
						showLongToast("�޷��������ݣ������Ƿ����������������");
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
					showLog_v("������ɡ�����������������������������");

					MQTT.SERVER_URL = "tcp://" + Contants.MQTT_SERVER + ":" + Contants.MQTT_PORT;
					MQTT.CLIENT_ID = "m_" + getUserId();
					MQTT.get_instance().publish_message(
							MQTT.SUBSCRIBE_TOPIC_PREFIX + "m_" + getUserId(), "This is test", 1);

					// // MQTT
					// // �½��߳�ȥ����MQTT����
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
					showAlterDialog("��¼ʧ��", "�洢ʧ��", R.drawable.login_error_icon, "ȷ��", null);
					break;
				default:
					Log.e("LoginActivity", msg.what + "<<<<δ����");
					break;

				case Constants.MQTT_UPDATE_SUCCESS:
					MqttResponse response = (MqttResponse) msg.obj;
					Class target = null;
					Bundle b = null;
					b = new Bundle();
					String content = "";
					target = TaskSelectorActivity.class;
					if (response != null) {
						if (response.getType().equals("1")) {
							// ��ʼ�ͽ����������͸��쵼
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							content += "�����µ�����";
						} else if (response.getType().equals("2")) {
							// �½�ָ��ʱ�������͸���������
							b.putString("tid", new TaskInsDao(AppApplication.getInstance())
									.getTaskId(response.getId()));
							content += "�����µ���Ϣ";
						} else if (response.getType().equals("3")) {
							// �ϴ����������͸�Ӧ�ϸڵ�λ�쵼�����û�оͲ�����
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							content += "�����µ����񸽼�";
						} else if (response.getType().equals("4")) {
							// �ϴ����������͸�Ӧ�ϸڵ�λ�쵼�����û�оͲ�����
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							content += "�����ѿ�ʼ";
						} else if (response.getType().equals("5")) {
							// �ϴ����������͸�Ӧ�ϸڵ�λ�쵼�����û�оͲ�����
							b.putInt("enterType", 0);
							b.putString("tid", response.getId());
							content += "�����ѽ���";
						}
					}
					showNotification(AppApplication.getInstance(), target, b, content, "�������Ƽ�ϵͳ",
							content);
					break;
				case Constants.MQTT_UPDATE_FAIL:
					break;

				}

			}
		};
		MQTT.handler = handler;
		// ע��Handler
		registHandler();
	}

	// �ж��û������������Ҫ�����¼�ɹ�
	public void login_mainschedule() {

		// 2014-6-24 WeiHao
		// 1.��ʾ������
		mProgressDialog.setCancelable(true);
		mProgressDialog.setCanceledOnTouchOutside(false);
		showProgressDialog("���ڵ�¼", "Ŭ������������...���Ժ�~");
		// ���������
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
		inputPassword = etPassword.getText().toString().trim();

		if (inputUserName == null || inputUserName.equals("") || inputPassword == null
				|| inputPassword.equals("")) {
			dismissProgressDialog();
			showAlterDialog("��¼����", "�ʺŻ������벻��Ϊ�գ�\n��������ٵ�¼��", R.drawable.login_error_icon, "ȷ��",
					null);
			return;
		}
		// ���������
		if (Utils.isNetworkAvailable(Login.this)) {
			webRequestManager.login(inputUserName.toLowerCase(),
					Generate_md5.generate_md5(inputPassword));
		}
		// ���������
		else {
			if (getUserIc() != null && getUserId() != null) {
				if (getUserName().toLowerCase().equals(inputUserName.toLowerCase())
						&& getUserIc().equals(Generate_md5.generate_md5(inputPassword))) {
					webRequestManager.loginUpdate(Login.this);
				} else {
					dismissProgressDialog();
					showAlterDialog("��¼����", "������������״̬", R.drawable.login_error_icon, "ȷ��", null);
				}
			} else {
				dismissProgressDialog();
				showAlterDialog("��¼����", "������������״̬", R.drawable.login_error_icon, "ȷ��", null);
			}
		}

		// //����ǵ�һ�ε�¼
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
		// showAlterDialog("��¼����", "������������״̬", R.drawable.login_error_icon,
		// "ȷ��", null);
		// }
		// } else {
		// if (getUserName().toLowerCase().equals(inputUserName.toLowerCase())
		// && getUserIc().equals(Generate_md5.generate_md5(inputPassword))) {
		// webRequestManager.loginUpdate(Login.this);
		// } else {
		// dismissProgressDialog();
		// showAlterDialog("��¼����", "�������", R.drawable.login_error_icon, "ȷ��",
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
		// SD���жϲ���ʾ
		if (Utils.isExistSDCard()) {
			if (Utils.getSDFreeSize() < 200) { // �洢�ռ�С��200MB
				Toast.makeText(Login.this, "����SD���洢�ռ䲻�㣬�뼰ʱ��������������ޣ�", Toast.LENGTH_SHORT).show();
				return false;
			} else { // SD�������ҿռ��㹻������Ŀ¼׼��
				// ����һ���ļ��ж��󣬸�ֵΪ�ⲿ�洢����Ŀ¼
				File sdcardDir = Environment.getExternalStorageDirectory();
				// �õ�һ��·����������sdcard�ĸ���·��
				String path1 = sdcardDir.getPath() + "/nercms-Schedule/Attachments";
				File filePath1 = new File(path1);
				// �õ�һ��·����������sdcard�ĸ�������ͼ·��
				String path2 = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail";
				File filePath2 = new File(path2);

				if (!filePath1.exists()) {
					// �������ڣ�����Ŀ¼
					filePath1.mkdirs();
				}

				if (!filePath2.exists()) {
					filePath2.mkdirs();
				}
				return true;
			}
		} else {
			Toast.makeText(Login.this, "δ��⵽SD�������������ޣ�", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private boolean checkInternet() {
		ConnectivityManager connectivityManager;
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info != null && info.isAvailable()) {
			String name = info.getTypeName();
			Log.d(TAG, "��ǰ�������ƣ�" + name);
			// pingӦ�÷�����
			// if (Utils.serverPing()) {
			// return true;
			// } else {
			// Toast.makeText(Login.this, "�������Ӳ�����", Toast.LENGTH_SHORT)
			// .show();
			// return false;
			// }
			return true;
		} else {
			Toast.makeText(Login.this, "���������", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// ������ؼ�
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// ������ڵ�¼��ȡ����¼
			dismissProgressDialog();
			AppApplication.getInstance().myQueue.cancelAll(this);
			Utils.showShortToast(this, "ȡ����¼");
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		Log.v("Login", "onDestroy,ע��Handler");
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

	// ע��Handler
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
