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
 * @description 客户添加页面 针对当前登录用户，添加其客户
 * 
 *              0527修改界面 头部改为actionbar样式
 */
public class ContactAdd extends BaseActivity {

	// 网络请求入口
	private WebRequestManager webRequestManager;
	// 本地数据请求入口
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao = null;
	private Handler handler;

	// 当前登录用户ID
	private String userID;// 即对应CustomerModel中的contactID

	// 控件
	private EditText name;// 客户名
	private EditText mobile;// 手机
	private EditText phone;// 座机
	private EditText email;// 邮箱
	private EditText address;// 地址
	private EditText unit;// 所在部门
	private EditText description; // 描述（部门、职务、级别等）

	private CustomerModel customer;// 包含客户id 姓名 部门，描述，所述联系人id
	private ArrayList<CustomerContactModel> customerList;// 包含客户id 及对应联系方式类型
															// ：1-ID；2-手机号码；3-座机号码；5-邮箱；6-通信地址
	public static final String TAG = "ContactAdd";

	private String customerID;// 自动生成客户id

	private int entranceType; // 1-新建客户；2-修改客户信息

	private String lastResonseCustomerID = "";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_add);

		entranceType = getIntent().getExtras().getInt("entrance_type");

		webRequestManager = new WebRequestManager(AppApplication.getInstance(),
				this);
		personDao = daoFactory.getPersonDao(this);

		// 当前用户信息
		userID = MySharedPreference.get(ContactAdd.this,
				MySharedPreference.USER_ID, "");

		// 初始化ActionBar
		initActionBar();
		// 初始化Handler
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
		getSupportActionBar().setTitle("编辑客户信息");

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

	// 顶部actionbar创建
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("添加新客户");
	}

	// 右侧保存按钮添加
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem save = menu.add(0, 1, 0, "保存");
		save.setIcon(R.drawable.ic_action_save);
		save.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		return super.onCreateOptionsMenu(menu);
	}

	// 保存按钮点击时 判断输入+数据保存到本地
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// 左键返回主页
			finish();
			break;
		case 1: // 右键保存客户
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

		// 客户相关
		String _name = name.getText().toString().trim();
		String _unit = unit.getText().toString().trim();
		String _desc = description.getText().toString().trim();

		customer = new CustomerModel(customerID, _name, _unit, _desc, userID);
		// 将新建客户信息上传到服务器中
		webRequestManager.modifyCustomer(customer, customerList);

		// 客户联系方式相关 1-ID；2-手机号码；3-座机号码；5-邮箱；6-通信地址
		// 相关数据的格式未验证
		String _mobile = mobile.getText().toString();
		customerList.add(new CustomerContactModel(customerID, 2, _mobile));

		if (_name == null || _mobile == null) {
			new AlertDialog.Builder(ContactAdd.this).setTitle("无法修改客户")
					.setMessage("请重新检查信息是否填写完整").setPositiveButton("确定", null)
					.create().show();
			return;
		}

		String _phone = phone.getText().toString();
		customerList.add(new CustomerContactModel(customerID, 3, _phone));

		String _email = email.getText().toString();
		if (!"".equals(_email)) {
			if (!Utils.isEmail(_email)) {
				Toast.makeText(this, "Email格式有误", Toast.LENGTH_SHORT).show();
				return;
			}
		}
		customerList.add(new CustomerContactModel(customerID, 5, _email));

		String _addr = address.getText().toString();
		customerList.add(new CustomerContactModel(customerID, 6, _addr));

	}

	private void createCustomer() {

		customerList = new ArrayList<CustomerContactModel>();

		// 客户相关
		String _name = name.getText().toString().trim();
		String _unit = unit.getText().toString().trim();
		String _desc = description.getText().toString().trim();
		// 客户联系方式相关 1-ID；2-手机号码；3-座机号码；5-邮箱；6-通信地址
		// 相关数据的格式未验证
		String _mobile = mobile.getText().toString();
		String _phone = phone.getText().toString();
		String _email = email.getText().toString();
		String _addr = address.getText().toString();

		if (_name.isEmpty() || _mobile.isEmpty()) {
			new AlertDialog.Builder(ContactAdd.this).setTitle("无法添加客户")
					.setMessage("请重新检查信息是否填写完整").setPositiveButton("确定", null)
					.create().show();
			return;
		}
		customer = new CustomerModel("1", _name, _unit, _desc, userID);
		customerList.add(new CustomerContactModel(customerID, 2, _mobile));

		customerList.add(new CustomerContactModel(customerID, 3, _phone));

		if (!"".equals(_email)) {
			if (!Utils.isEmail(_email)) {
				Utils.showShortToast(this, "Email格式有误");
				return;
			}
		}
		customerList.add(new CustomerContactModel(customerID, 5, _email));

		customerList.add(new CustomerContactModel(customerID, 6, _addr));

		// 将新建客户信息上传到服务器中
		webRequestManager.newCustomer(customer, customerList);
	}

	// 发送消息到主线程
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
					Toast.makeText(ContactAdd.this, "新建客户成功", Toast.LENGTH_LONG)
							.show();
					ContactAdd.this.finish();
					break;
				case Constant.CREATE_CUSTOMER_REQUEST_FAIL:
					Toast.makeText(ContactAdd.this, "新建客户失败", Toast.LENGTH_LONG)
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
					Toast.makeText(ContactAdd.this, "编辑客户成功", Toast.LENGTH_LONG)
							.show();
					ContactAdd.this.finish();
					break;
				case Constant.MODIFY_CUSTOMER_REQUEST_FAIL:
					Toast.makeText(ContactAdd.this, "编辑客户失败", Toast.LENGTH_LONG)
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
		// 注销Handler
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
