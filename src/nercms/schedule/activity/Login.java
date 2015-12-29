package nercms.schedule.activity;

import java.io.File;

import nercms.schedule.R;
import nercms.schedule.utils.MyLog;
import nercms.schedule.utils.Utils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.elec.model.LoginResponse;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.jerry.model.person.GetPersonInfoResponse;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nercms.Push;

/**
 * 
 * @author J 2015-12-29
 */
public class Login extends BaseActivity {

	private EditText etUserName; // �û����༭��
	private EditText etPassword; // ����༭��
	// String un = "fm";
	// String pwd = "123456";

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

		// this is test
		// // �û�����ʹ�ã�ֱ��������Activity
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

		// Ĭ����ʾ�ϴε�¼���û�ID
		etUserName.setText(MySharedPreference.get(Login.this, MySharedPreference.USER_NAME, ""));

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

		// ���ð�ť
		MenuItem setting = menu.add(0, 1, 0, "����");
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
				// ��¼�ɹ�
				case Constants.LOGIN_SUCCESS:

					// �����û�ID
					String userID = ((LoginResponse) msg.obj).getUid();
					// �����û�id
					MySharedPreference.save(Login.this, MySharedPreference.USER_ID, userID);
					// �����û��ĵ�¼��
					MySharedPreference.save(Login.this, MySharedPreference.USER_NAME,
							inputUserName);
					// �����û�����
					MySharedPreference.save(Login.this, MySharedPreference.USER_IC, inputPassword);

					// // �½��߳�ȥ����MQTT����
					// new Thread(new Runnable() {
					// @Override
					// public void run() {
					// Looper.prepare();
					Push.PERSON_ID = getUserId();
					Push.get_instance(Login.this).ini();
					// }
					// }).start();

					webRequestManager.loginUpdate(Login.this);

					// д��־
					MyLog.i(TAG, "�û���" + userID + " ��½�ɹ�");
					break;
				// ��¼ ʧ��
				case Constants.LOGIN_FAIL:
				case Constants.LOGIN_UPDATE_FAIL:
					MyLog.i(TAG, "��¼ʧ��");
					dismissProgressDialog();
					if (msg.obj != null) {
						String errorCode = ((NormalServerResponse) msg.obj).getEc();
						showAlterDialog("��¼ʧ��", Utils.getErrorMsg(errorCode),
								R.drawable.login_error_icon, "ȷ��", null);
					} else {
						showAlterDialog("��¼ʧ��", "�����Ƿ����������������", R.drawable.login_error_icon, "ȷ��",
								null);
					}
					break;

				case Constants.LOGIN_UPDATE_SUCCESS:
					dismissProgressDialog();
					// TODO ҳ����ת
					break;

				case Constants.LOGIN_UPDATE_SAVE_FAIL:
					dismissProgressDialog();
					showAlterDialog("��¼ʧ��", "�洢ʧ��", R.drawable.login_error_icon, "ȷ��", null);
					break;
				default:
					Log.e("LoginActivity", msg.what + "<<<<δ����");
					break;
				}

			}
		};
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

		if (inputUserName == null || inputUserName.equals("") || inputPassword == null
				|| inputPassword.equals("")) {
			dismissProgressDialog();
			new AlertDialog.Builder(Login.this)
					.setIcon(getResources().getDrawable(R.drawable.login_error_icon))
					.setTitle("��¼����").setMessage("�ʺŻ������벻��Ϊ�գ�\n��������ٵ�¼��").create().show();
			return;
		}
		webRequestManager.login(inputUserName, inputPassword);
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
	protected void onDestroy() {
		Log.v("Login", "onDestroy,ע��Handler");
		super.onDestroy();
		MessageHandlerManager.getInstance().unregister(Constants.LOGIN_SUCCESS,
				LoginResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.LOGIN_FAIL,
				LoginResponse.class.getName());
	}

	// ע��Handler
	private void registHandler() {
		MessageHandlerManager.getInstance().register(handler, Constants.LOGIN_SUCCESS,
				LoginResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.LOGIN_FAIL,
				LoginResponse.class.getName());
	}
}
