package nercms.schedule.activity;

import java.util.ArrayList;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.model.CustomerContactModel;
import android.wxapp.service.model.CustomerModel;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author jiaocuina@gmail.com
 * @date 2014-03-07
 * @version V1.0
 * @description �ͻ����ҳ�� ��Ե�ǰ��¼�û��������ͻ�
 * 
 *              0527�޸Ľ��� ͷ����Ϊactionbar��ʽ
 */
public class ContactAdd extends BaseActivity {

	// �����������
	private WebRequestManager webRequestManager;
	// ���������������
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao = null;
	private Handler handler;

	// ��ǰ��¼�û�ID
	private String userID;// ����ӦCustomerModel�е�contactID

	// �ؼ�
	private EditText name;// �ͻ���
	private EditText mobile;// �ֻ�
	private EditText phone;// ����
	private EditText email;// ����
	private EditText address;// ��ַ
	private EditText unit;// ���ڲ���
	private EditText description; // ���������š�ְ�񡢼���ȣ�

	private CustomerModel customer;// �����ͻ�id ���� ���ţ�������������ϵ��id
	private ArrayList<CustomerContactModel> customerList;// �����ͻ�id ����Ӧ��ϵ��ʽ����
															// ��1-ID��2-�ֻ����룻3-�������룻5-���䣻6-ͨ�ŵ�ַ
	public static final String TAG = "ContactAdd";

	private String customerID;// �Զ����ɿͻ�id

	private int entranceType; // 1-�½��ͻ���2-�޸Ŀͻ���Ϣ

	private String lastResonseCustomerID = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_add);

		entranceType = getIntent().getExtras().getInt("entrance_type");

		webRequestManager = new WebRequestManager(AppApplication.getInstance(),
				this);
		personDao = daoFactory.getPersonDao(this);

		// ��ǰ�û���Ϣ
		userID = MySharedPreference.get(ContactAdd.this,
				MySharedPreference.USER_ID, "");

		// ��ʼ��ActionBar
		initActionBar();
		// ��ʼ��Handler
		initHandler();

		name = (EditText) findViewById(R.id.customer_name);
		mobile = (EditText) findViewById(R.id.phoneNumber_mobile);
		phone = (EditText) findViewById(R.id.phoneNumber);
		email = (EditText) findViewById(R.id.customer_email);
		address = (EditText) findViewById(R.id.customer_address);
		unit = (EditText) findViewById(R.id.customer_unit);
		description = (EditText) findViewById(R.id.customer_description);

		if (entranceType == 2) {
			initData();
		}

	}

	private void initData() {
		getSupportActionBar().setTitle("�༭�ͻ���Ϣ");

		customerID = getIntent().getStringExtra("CUSTOMER_ID");
		customer = personDao.getCustomerByID(customerID);
		customerList = personDao.getCustomerContact(customerID);

		name.setText(customer.getName());
		unit.setText(customer.getUnit());
		description.setText(customer.getDescription());

		if (customerList != null) {
			CustomerContactModel ccm;
			for (int i = 0; i < customerList.size(); i++) {
				ccm = customerList.get(i);
				switch (ccm.getType()) {
				case 2:
					String _mobile = ccm.getContent();
					mobile.setText(_mobile);
					break;
				case 3:
					phone.setText(ccm.getContent());
					break;
				case 5:
					String _email = ccm.getContent();
					email.setText(_email);
					break;
				case 6:
					String _address = ccm.getContent();
					address.setText(_address);
					break;
				default:
					break;
				}
			}
		}

	}

	// ����actionbar����
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("����¿ͻ�");
	}

	// �Ҳౣ�水ť���
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem save = menu.add(0, 1, 0, "����");
		save.setIcon(R.drawable.ic_action_save);
		save.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	// ���水ť���ʱ �ж�����+���ݱ��浽����
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// ���������ҳ
			finish();
			break;
		case 1: // �Ҽ�����ͻ�
			if (entranceType == 1) {
				createCustomer();
			} else {
				modifyCustomer();
			}
			// this.finish();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private void modifyCustomer() {

		// �ͻ����
		String _name = name.getText().toString().trim();
		String _unit = unit.getText().toString().trim();
		String _desc = description.getText().toString().trim();

		customer = new CustomerModel(customerID, _name, _unit, _desc, userID);
		// ���½��ͻ���Ϣ�ϴ�����������
		webRequestManager.modifyCustomer(customer, customerList);

		// �ͻ���ϵ��ʽ��� 1-ID��2-�ֻ����룻3-�������룻5-���䣻6-ͨ�ŵ�ַ
		// ������ݵĸ�ʽδ��֤
		String _mobile = mobile.getText().toString();
		customerList.add(new CustomerContactModel(customerID, 2, _mobile));

		if (_name == null || _mobile == null) {
			new AlertDialog.Builder(ContactAdd.this).setTitle("�޷��޸Ŀͻ�")
					.setMessage("�����¼����Ϣ�Ƿ���д����").setPositiveButton("ȷ��", null)
					.create().show();
			return;
		}

		String _phone = phone.getText().toString();
		customerList.add(new CustomerContactModel(customerID, 3, _phone));

		String _email = email.getText().toString();
		if (!"".equals(_email)) {
			if (!Utils.isEmail(_email)) {
				Toast.makeText(this, "Email��ʽ����", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		customerList.add(new CustomerContactModel(customerID, 5, _email));

		String _addr = address.getText().toString();
		customerList.add(new CustomerContactModel(customerID, 6, _addr));

	}

	private void createCustomer() {

		customerList = new ArrayList<CustomerContactModel>();

		// �ͻ����
		String _name = name.getText().toString().trim();
		String _unit = unit.getText().toString().trim();
		String _desc = description.getText().toString().trim();
		// �ͻ���ϵ��ʽ��� 1-ID��2-�ֻ����룻3-�������룻5-���䣻6-ͨ�ŵ�ַ
		// ������ݵĸ�ʽδ��֤
		String _mobile = mobile.getText().toString();
		String _phone = phone.getText().toString();
		String _email = email.getText().toString();
		String _addr = address.getText().toString();

		if (_name.isEmpty() || _mobile.isEmpty()) {
			new AlertDialog.Builder(ContactAdd.this).setTitle("�޷���ӿͻ�")
					.setMessage("�����¼����Ϣ�Ƿ���д����").setPositiveButton("ȷ��", null)
					.create().show();
			return;
		}
		customer = new CustomerModel("1", _name, _unit, _desc, userID);
		customerList.add(new CustomerContactModel(customerID, 2, _mobile));

		customerList.add(new CustomerContactModel(customerID, 3, _phone));

		if (!"".equals(_email)) {
			if (!Utils.isEmail(_email)) {
				Utils.showShortToast(this, "Email��ʽ����");
				return;
			}
		}
		customerList.add(new CustomerContactModel(customerID, 5, _email));

		customerList.add(new CustomerContactModel(customerID, 6, _addr));

		// ���½��ͻ���Ϣ�ϴ�����������
		webRequestManager.newCustomer(customer, customerList);
	}

	// ������Ϣ�����߳�
	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.CREATE_CUSTOMER_REQUEST_SUCCESS:

					String tempID = String.valueOf((Object) msg.obj);
					Log.v("ContactAdd", "receive msg , id: " + tempID);
					customer.setCustomerID(tempID);
//					customer.save(ContactAdd.this);
					CustomerContactModel ccm;
					for (int i = 0; i < customerList.size(); i++) {
						ccm = customerList.get(i);
						switch (ccm.getType()) {
						case 2:
							personDao
									.saveCustomerContact(new CustomerContactModel(
											tempID, 2, ccm.getContent()));
							break;
						case 3:
							personDao
									.saveCustomerContact(new CustomerContactModel(
											tempID, 3, ccm.getContent()));
							break;
						case 5:
							personDao
									.saveCustomerContact(new CustomerContactModel(
											tempID, 5, ccm.getContent()));
							break;
						case 6:
							personDao
									.saveCustomerContact(new CustomerContactModel(
											tempID, 6, ccm.getContent()));
							break;

						default:
							break;
						}
					}
					Toast.makeText(ContactAdd.this, "�½��ͻ��ɹ�", Toast.LENGTH_LONG)
							.show();
					ContactAdd.this.finish();
					break;
				case Constant.CREATE_CUSTOMER_REQUEST_FAIL:
					Toast.makeText(ContactAdd.this, "�½��ͻ�ʧ��", Toast.LENGTH_LONG)
							.show();
					break;
				case Constant.MODIFY_CUSTOMER_REQUEST_SUCCESS:
					personDao.modifyCostomer(customer);
					CustomerContactModel ccm2;
					for (int i = 0; i < customerList.size(); i++) {
						ccm2 = customerList.get(i);
						switch (ccm2.getType()) {
						case 2:
							personDao
									.saveCustomerContact(new CustomerContactModel(
											customerID, 2, ccm2.getContent()));
							break;
						case 3:
							personDao
									.saveCustomerContact(new CustomerContactModel(
											customerID, 3, ccm2.getContent()));
							break;
						case 5:
							personDao
									.saveCustomerContact(new CustomerContactModel(
											customerID, 5, ccm2.getContent()));
							break;
						case 6:
							personDao
									.saveCustomerContact(new CustomerContactModel(
											customerID, 6, ccm2.getContent()));
							break;

						default:
							break;
						}
					}
					Toast.makeText(ContactAdd.this, "�༭�ͻ��ɹ�", Toast.LENGTH_LONG)
							.show();
					ContactAdd.this.finish();
					break;
				case Constant.MODIFY_CUSTOMER_REQUEST_FAIL:
					Toast.makeText(ContactAdd.this, "�༭�ͻ�ʧ��", Toast.LENGTH_LONG)
							.show();
					break;
				}
			}
		};
		MessageHandlerManager.getInstance().register(handler,
				Constant.CREATE_CUSTOMER_REQUEST_SUCCESS, "ContactAdd");
		MessageHandlerManager.getInstance().register(handler,
				Constant.CREATE_CUSTOMER_REQUEST_FAIL, "ContactAdd");
		MessageHandlerManager.getInstance().register(handler,
				Constant.MODIFY_CUSTOMER_REQUEST_SUCCESS, "ContactAdd");
		MessageHandlerManager.getInstance().register(handler,
				Constant.MODIFY_CUSTOMER_REQUEST_FAIL, "ContactAdd");
	}

	@Override
	protected void onDestroy() {
		Log.v("ContactAdd", "onDestroy");
		// ע��Handler
		MessageHandlerManager.getInstance().unregister(
				Constant.CREATE_CUSTOMER_REQUEST_SUCCESS, "ContactAdd");
		MessageHandlerManager.getInstance().unregister(
				Constant.CREATE_CUSTOMER_REQUEST_FAIL, "ContactAdd");
		MessageHandlerManager.getInstance().unregister(
				Constant.MODIFY_CUSTOMER_REQUEST_SUCCESS, "ContactAdd");
		MessageHandlerManager.getInstance().unregister(
				Constant.MODIFY_CUSTOMER_REQUEST_FAIL, "ContactAdd");

		super.onDestroy();
	}

}
