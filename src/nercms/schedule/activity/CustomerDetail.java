package nercms.schedule.activity;

import java.util.ArrayList;

import nercms.schedule.R;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.model.CustomerContactModel;
import android.wxapp.service.model.CustomerModel;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;

//0527
//�����������
//�ͻ������   ����ϵ���������������  �ֶβ�ͬ  ��ť��ͬ ���޸�ɾ�� 
public class CustomerDetail extends BaseActivity {
	private static final String TAG = "CustomerDetail";
	// �����������
	private WebRequestManager webRequestManager;
	// ���������������
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao = daoFactory.getPersonDao(CustomerDetail.this);

	private String customerID;
	private String userID;
	private CustomerModel thisCustomer;
	private ArrayList<CustomerContactModel> customerContactList;
	private TextView customerName;// �ͻ�����
	private TextView customerUnit;// ���ڵ�λ
	private TextView customerDesc;// ����

	private TextView customerMobile;// �ֻ�
	private TextView customerEmail;// ����
	private TextView customerAddress;// ��ַ

	private Button customer_btn_chat;// ����Ϣ
	private Button customer_btn_phone;// ��绰

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.customer_detail);

		customerID = getIntent().getExtras().getString("CUSTOMER_ID");
		userID = MySharedPreference.get(CustomerDetail.this,
				MySharedPreference.USER_ID, null);

		webRequestManager = new WebRequestManager(AppApplication.getInstance(),
				this);

		customerName = (TextView) findViewById(R.id.customer_name);
		customerUnit = (TextView) findViewById(R.id.customer_unit);
		customerDesc = (TextView) findViewById(R.id.customer_description);
		customerMobile = (TextView) findViewById(R.id.customer_phone);
		customerEmail = (TextView) findViewById(R.id.customer_mail);
		customerAddress = (TextView) findViewById(R.id.customer_address);
		customer_btn_chat = (Button) findViewById(R.id.customer_btn_chat);
		customer_btn_phone = (Button) findViewById(R.id.customer_btn_phone);

		customer_btn_chat.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 2014-6-11 ϵͳ������
				if (!customerMobile.getText().toString().isEmpty()) {
					Uri uri = Uri.parse("smsto:"
							+ customerMobile.getText().toString());
					Intent it = new Intent(Intent.ACTION_SENDTO, uri);
					startActivity(it);
				}

			}
		});
		customer_btn_phone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 2014-6-11 ϵͳ��绰
				if (!customerMobile.getText().toString().isEmpty()) {
					Uri uri = Uri.parse("tel:"
							+ customerMobile.getText().toString());
					Intent it = new Intent(Intent.ACTION_DIAL, uri);
					startActivity(it);
				}
			}
		});

		// ��ʼ��ActionBar
		initActionBar();

		initData();

	}

	private void initData() {
		// ������
		thisCustomer = personDao.getCustomerByID(customerID);
		String _name = thisCustomer.getName();
		customerName.setText(_name);
		String _unit = thisCustomer.getUnit();
		customerUnit.setText(_unit);
		String _desc = thisCustomer.getDescription();
		customerDesc.setText(_desc);
		// �ͻ���ϵ��ʽ
		customerContactList = personDao.getCustomerContact(customerID);
		if (customerContactList != null) {
			CustomerContactModel ccm;
			for (int i = 0; i < customerContactList.size(); i++) {
				ccm = customerContactList.get(i);
				switch (ccm.getType()) {
				case 2:
					String _mobile = ccm.getContent();
					customerMobile.setText(_mobile);
					break;
				case 5:
					String _email = ccm.getContent();
					customerEmail.setText(_email);
					break;
				case 6:
					String _address = ccm.getContent();
					customerAddress.setText(_address);
					break;
				default:
					break;
				}
				ccm.getType();
			}
				}
	}

	// ����actionbar����
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("�ͻ�����");
	}

	// �Ҳౣ�水ť��� ����������˵�
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu subMenu = menu.addSubMenu("add item");

		subMenu.add(0, 1, 0, "�޸Ŀͻ�");
		subMenu.add(0, 2, 0, "ɾ���ͻ�");
		MenuItem addItem = subMenu.getItem();
		addItem.setIcon(R.drawable.mm_title_btn_menu);
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	// ���水ť���ʱ �ж�����+���ݱ��浽����
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// ���������ҳ
			finish();
			break;
		case 1:// �޸Ŀͻ���Ϣ
				// **************************�½��༭ҳ��*****
			Intent intent = new Intent(this, ContactAdd.class);
			intent.putExtra("entrance_type", 2);
			intent.putExtra("CUSTOMER_ID", customerID);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			Log.v(TAG, "��ת���ͻ��޸�ҳ��");
			break;
		case 2: // ɾ����ϵ��
			new AlertDialog.Builder(CustomerDetail.this)
					.setTitle("ɾ���ͻ�")
					.setMessage("ȷ��ɾ���ÿͻ�?")
					.setPositiveButton("ȷ��",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									webRequestManager
											.deleteCustomer(customerID);
									CustomerDetail.this.finish();
								}
							}).setNegativeButton("ȡ��", null).create().show();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	protected void onResume() {
		// ��;���ʲô��ҳʲô��ť��ʱ�˳�����ʱ�Ļָ�
		super.onResume();
		initData();
	}
}
