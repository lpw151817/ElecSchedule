package nercms.schedule.activity;

import java.util.ArrayList;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.jerry.model.person.Contacts;
import android.wxapp.service.jerry.model.person.GetPersonInfoResponse;
import android.wxapp.service.model.ContactModel;
import android.wxapp.service.model.CustomerContactModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class Profile extends BaseActivity {

	private WebRequestManager webRequestManager;
	private Handler handler;

	// private static DAOFactory daoFactory = DAOFactory.getInstance();
	// private PersonDao personDao = null;

	GetPersonInfoResponse mPersonInfo;

	private TextView name;
	private TextView id;
	private TextView orgDesc;// �����ڵ�����
	private TextView position;// ְ��
	private TextView rank;// ְ��

	private TextView mobile;// �ֻ�
	private TextView email;// ����
	private TextView address;// ��ַ

	private StructuredStaffModel mySSM;// ��½�û����˵�SSMģ��
	private ArrayList<ContactModel> myContactList;
	public static final String TAG = "Profile";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.profile);

		webRequestManager = new WebRequestManager(AppApplication.getInstance(), this);

		// ��ʼ���ؼ�
		initView();
		// ��ʼ��ActionBar
		initActionBar();
		// ��ʼ��handler
		initHandler();
		// ��ʼ����������
		initData();

	}

	private void initView() {
		name = (TextView) findViewById(R.id.my_name);
		id = (TextView) findViewById(R.id.my_id);
		orgDesc = (TextView) findViewById(R.id.my_org);
		position = (TextView) findViewById(R.id.my_position);
		rank = (TextView) findViewById(R.id.my_rank);
		mobile = (TextView) findViewById(R.id.my_phone);
		email = (TextView) findViewById(R.id.my_mail);
		address = (TextView) findViewById(R.id.my_address);
	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("�ҵ�����");
	}

	public boolean onCreateOptionsMenu(Menu menu) {
//		MenuItem modify = menu.add(0, 1, 0, "�޸�����");
//		modify.setIcon(R.drawable.ic_action_modify);
//		modify.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// ���������ҳ
			finish();
			break;
		case 1: // �޸�����
			// showModifyDialog();
			Log.e(getClass().getName(), "�޸�����Ľӿڵ��ã��ѷ���");
			showLongToast("�޸�����Ľӿڵ��ã��ѷ���");
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	// private void showModifyDialog() {
	// LayoutInflater layoutInflater = LayoutInflater.from(this);
	// View modifyView = layoutInflater.inflate(R.layout.modify_pwd, null);
	// final EditText old_pwd = (EditText)
	// modifyView.findViewById(R.id.edit_old_pwd);
	// final EditText new_pwd = (EditText)
	// modifyView.findViewById(R.id.edit_new_pwd);
	// new
	// AlertDialog.Builder(Profile.this).setTitle("�޸�����").setView(modifyView)
	// .setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int i) {
	// String _oldPwd = old_pwd.getText().toString();
	// String _newPwd = new_pwd.getText().toString();
	// Log.i("TAG", "������" + _oldPwd + "������" + _newPwd);
	// // webRequestManager.changePassword(userID, _oldPwd,
	// // _newPwd);
	// // jerry 15.5.22
	// webRequestManager.changePassword(_oldPwd, _newPwd);
	// }
	// })
	//
	// .setNegativeButton("ȡ��", null).create().show();
	// }

	private void initData() {
		// �ȴ����ݿ��ж�ȡ����
		mPersonInfo = DAOFactory.getInstance().getPersonDao(this).getPersonInfo(getUserId());
		// ���û�����ݣ����Ƚ�����������
		if (mPersonInfo == null) {
			showProgressDialog("loading...");
			webRequestManager.GetPersonInfo(getUserId());
		}
		// ����Ѿ��õ����ݣ��������ʾ
		else {
			id.setText(getUserId());
			name.setText(mPersonInfo.getN());
			orgDesc.setText(mPersonInfo.getD());
			position.setText(mPersonInfo.getR());
			// rank.setText(mySSM.getRank());

			List<Contacts> contants = mPersonInfo.getContacts();
			for (Contacts contact : contants) {
				try {
					if (contact.getT().equals(Contacts.CONTACT_ITEM.EMAIL)) {
						email.setText(contact.getC());
						continue;
					} else if (contact.getT().equals(Contacts.CONTACT_ITEM.MOBILE)) {
						mobile.setText(contact.getC());
						continue;
					}
				} catch (Exception e) {
					e.printStackTrace();
					Log.e("Profile", e.toString());
					continue;
				}
			}
		}
		// personDao = daoFactory.getPersonDao(Profile.this);
		// GetPersonInfoResponse r = personDao.getCustomer();
		// if (r == null) {
		// } else {
		//
		// mySSM = personDao.getSSMByID(userID);
		// id.setText(userID);
		// name.setText(mySSM.getName());
		// orgDesc.setText(mySSM.getOrgDescription());
		// position.setText(mySSM.getPosition());
		// rank.setText(mySSM.getRank());
		//
		// myContactList = personDao.getContactListByID(userID);
		// if (myContactList != null) {
		// for (int i = 0; i < myContactList.size(); i++) {
		// ContactModel cm = myContactList.get(i);
		// switch (cm.getType()) {
		// case 2:
		// String _mobile = cm.getContent();
		// mobile.setText(_mobile);
		// break;
		// case 5:
		// String _email = cm.getContent();
		// email.setText(_email);
		// break;
		// case 6:
		// String _address = cm.getContent();
		// address.setText(_address);
		// break;
		// default:
		// break;
		// }
		// cm.getType();
		// }
		// }
		// }
	}

	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				dismissProgressDialog();
				switch (msg.what) {

				case Constant.CHANGE_PASSWORD_REQUEST_SUCCESS:
					Utils.showShortToast(Profile.this, "�޸�����ɹ�");
					Profile.this.finish();
					break;
				case Constant.CHANGE_PASSWORD_REQUEST_FAIL:
					Utils.showShortToast(Profile.this, "�޸�����ʧ��");
					break;
				// ��ȡ��Ա��Ϣ�ɹ�
				case Constant.QUERY_PERSON_INFO_REQUEST_SUCCESS:
					mPersonInfo = (GetPersonInfoResponse) msg.obj;
					initData();
					break;
				// ��ȡ��Ա��Ϣʧ��
				case Constant.QUERY_PERSON_INFO_REQUEST_FAIL:
					NormalServerResponse r = (NormalServerResponse) msg.obj;
					showAlterDialog(Utils.getErrorMsg(r.getEc()));
					break;
				}
			}
		};
		MessageHandlerManager.getInstance().register(handler,
				Constant.QUERY_PERSON_INFO_REQUEST_SUCCESS, Contants.METHOD_PERSON_GET_PERSON_INFO);
		MessageHandlerManager.getInstance().register(handler,
				Constant.QUERY_PERSON_INFO_REQUEST_FAIL, Contants.METHOD_PERSON_GET_PERSON_INFO);
		// MessageHandlerManager.getInstance().register(handler,
		// Constant.CHANGE_PASSWORD_REQUEST_SUCCESS,
		// "Profile");
		// MessageHandlerManager.getInstance().register(handler,
		// Constant.CHANGE_PASSWORD_REQUEST_FAIL,
		// "Profile");

	}

	@Override
	protected void onDestroy() {
		MessageHandlerManager.getInstance().unregister(Constant.QUERY_PERSON_INFO_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_GET_PERSON_INFO);
		MessageHandlerManager.getInstance().unregister(Constant.QUERY_PERSON_INFO_REQUEST_FAIL,
				Contants.METHOD_PERSON_GET_PERSON_INFO);
		super.onDestroy();
	}
}
