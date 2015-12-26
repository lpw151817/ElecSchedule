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
 * @description �����û������� ��¼ϵͳ
 *
 * @version V1.1
 * @author WEIHAO
 * @date 2014-6-25
 * @new �û���¼һϵ�е��߼������� ���ؽ�������ʾ����ϵ�����ر�����ɺ���ת
 * @version V1.3
 * @new ��¼�ɹ���ע��MQTT
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

		// // Ĭ����ʾ�ϴε�¼���û�ID
		// etUserName.setText(MySharedPreference.get(Login.this,
		// MySharedPreference.USER_NAME, ""));
		//
		// // ����û���Ϊ�գ���۽��û���������۽�����
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
		// MyLog.i(TAG, "��¼��ť���");
		// login_mainschedule();
		// }
		// });
		//
		// // ע���������ע��Handler
		// initHandler();
		// Log.v("Login", "OnResume,ע���������ע��Handler");
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
				case Constant.LOGIN_REQUEST_SUCCESS:

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

					// дһ��Timer��ʱ����������Ƿ��и�������
					getOrgInfoUpdate();
					getAffairUpdate();
					getMessageUpdate();
					// conference�ĸ��²���
					getConferenceUpdate();
					// group�ĸ���
					getGroupUpdate();
					// // TODO gps update
					// getGpsUpdate();

					// ��ȡ������Ϣ����
					GetPersonInfoResponse mPersonInfo = DAOFactory.getInstance()
							.getPersonDao(Login.this).getCustomer();
					// ���û�����ݣ����Ƚ�����������
					if (mPersonInfo == null) {
						webRequestManager.GetPersonInfo(getUserId());
					}

					// д��־
					MyLog.i(TAG, "�û���" + userID + " ��½�ɹ�");
					break;
				// ��¼ ʧ��
				case Constant.LOGIN_REQUEST_FAIL:
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
				// ����orgcodeʧ��
				case Constant.SAVE_ORG_CODE_FAIL:
					MyLog.e(TAG, "����orgcodeʧ��");
					break;
				// ����orgcode�ɹ�
				case Constant.SAVE_ORG_CODE_SUCCESS:
					MyLog.e(TAG, "����orgcode�ɹ�");
					isGetOrgCode = true;
					startInMain();
					break;
				// ��ȡ��֯���ʧ��
				case Constant.QUERY_ORG_NODE_REQUEST_FAIL:
					MyLog.i(TAG, "��ȡ���ʧ��");
					dismissProgressDialog();
					showAlterDialog("��ȡ���ʧ��",
							Utils.getErrorMsg(((NormalServerResponse) msg.obj).getEc()),
							R.drawable.login_error_icon);
					break;
				// �洢orgperson�ɹ�
				case Constant.SAVE_ORG_PERSON_SUCCESS:
					MyLog.e(TAG, "����orgperson�ɹ�");
					isGetOrgPerson = true;
					startInMain();
					break;
				// �洢orgpersonʧ��
				case Constant.SAVE_ORG_PERSON_FAIL:
					MyLog.e(TAG, "����orgpersonʧ��");
					break;
				// ��ȡorgpersonʧ��
				case Constant.QUERY_ORG_PERSON_REQUEST_FAIL:
					showAlterDialog("��ȡorgpersonʧ��",
							Utils.getErrorMsg(((NormalServerResponse) msg.obj).getEc()),
							R.drawable.login_error_icon);
					break;
				// ������ϵ�����
				case Constant.SAVE_ALL_PERSON_SUCCESS:
					// 5.��ת��������
					startInMain();
					break;
				// ��������ɹ�
				case Constant.SAVE_TASK_SUCCESS:
					isGetAffair = true;
					startInMain();
					break;
				// ������Ϣ�ɹ�
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
					Log.e("LoginActivity", msg.what + "<<<<δ����");
					break;
				}

			}
		};
		// ע��Handler
		registHandler();
	}

	// TODO ��֤��Ҫ����
	private void startInMain() {
		if (isGetAffair && isGetMessage && isGetOrgCode && isGetOrgPerson && isGetConference
				&& isGetGroup && isGetGps) {
			dismissProgressDialog();
			startActivity(new Intent(Login.this, Main.class));
			// �򿪶�ʱ���µ�service
			// // TODO ���Ƿ����������ݣ������Ǹ���ʱ��������
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

	// ��ȡ��֯�����Ϣ���������ݿ�
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
			// ��ȡȫ������
			webRequestManager.getAffairUpdate("1");
		} else {
			isGetAffair = true;
			startInMain();
		}
	}

	private void getMessageUpdate() {
		if (MySharedPreference.get(this, MySharedPreference.LAST_UPDATE_MESSAGE_TIMESTAMP,
				null) == null)
			// ��ȡȫ������
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
		String imsi = Utils.getIMSI(Login.this);

		if (inputUserName == null || inputUserName.equals("") || inputPassword == null
				|| inputPassword.equals("")) {
			dismissProgressDialog();
			new AlertDialog.Builder(Login.this)
					.setIcon(getResources().getDrawable(R.drawable.login_error_icon))
					.setTitle("��¼����").setMessage("�ʺŻ������벻��Ϊ�գ�\n��������ٵ�¼��").create().show();
			return;
		}
		// ������������е�¼��֤
		webRequestManager.loginVarification(inputUserName, inputPassword, imsi);
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
		// ע��Handler
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
		Log.v("Login", "onDestroy,ע��Handler");
		super.onDestroy();
	}

	// ע��Handler
	private void registHandler() {
		// ��½ʧ��
		MessageHandlerManager.getInstance().register(handler, Constant.LOGIN_REQUEST_FAIL,
				Contants.METHOD_PERSON_LOGIN);
		// ��½�ɹ�
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
