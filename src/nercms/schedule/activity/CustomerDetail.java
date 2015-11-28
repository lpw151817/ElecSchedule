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
//数据填充待添加
//客户详情表   跟联系人详情表稍有区别  字段不同  按钮不同 可修改删除 
public class CustomerDetail extends BaseActivity {
	private static final String TAG = "CustomerDetail";
	// 网络请求入口
	private WebRequestManager webRequestManager;
	// 本地数据请求入口
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao = daoFactory.getPersonDao(CustomerDetail.this);

	private String customerID;
	private String userID;
	private CustomerModel thisCustomer;
	private ArrayList<CustomerContactModel> customerContactList;
	private TextView customerName;// 客户姓名
	private TextView customerUnit;// 所在单位
	private TextView customerDesc;// 描述

	private TextView customerMobile;// 手机
	private TextView customerEmail;// 邮箱
	private TextView customerAddress;// 地址

	private Button customer_btn_chat;// 发消息
	private Button customer_btn_phone;// 打电话

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
				// 2014-6-11 系统发短信
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
				// 2014-6-11 系统打电话
				if (!customerMobile.getText().toString().isEmpty()) {
					Uri uri = Uri.parse("tel:"
							+ customerMobile.getText().toString());
					Intent it = new Intent(Intent.ACTION_DIAL, uri);
					startActivity(it);
				}
			}
		});

		// 初始化ActionBar
		initActionBar();

		initData();

	}

	private void initData() {
		// 绑定数据
		thisCustomer = personDao.getCustomerByID(customerID);
		String _name = thisCustomer.getName();
		customerName.setText(_name);
		String _unit = thisCustomer.getUnit();
		customerUnit.setText(_unit);
		String _desc = thisCustomer.getDescription();
		customerDesc.setText(_desc);
		// 客户联系方式
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

	// 顶部actionbar创建
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("客户详情");
	}

	// 右侧保存按钮添加 点击后下拉菜单
	public boolean onCreateOptionsMenu(Menu menu) {
		SubMenu subMenu = menu.addSubMenu("add item");

		subMenu.add(0, 1, 0, "修改客户");
		subMenu.add(0, 2, 0, "删除客户");
		MenuItem addItem = subMenu.getItem();
		addItem.setIcon(R.drawable.mm_title_btn_menu);
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	// 保存按钮点击时 判断输入+数据保存到本地
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// 左键返回主页
			finish();
			break;
		case 1:// 修改客户信息
				// **************************新建编辑页面*****
			Intent intent = new Intent(this, ContactAdd.class);
			intent.putExtra("entrance_type", 2);
			intent.putExtra("CUSTOMER_ID", customerID);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			Log.v(TAG, "跳转到客户修改页面");
			break;
		case 2: // 删除联系人
			new AlertDialog.Builder(CustomerDetail.this)
					.setTitle("删除客户")
					.setMessage("确定删除该客户?")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									webRequestManager
											.deleteCustomer(customerID);
									CustomerDetail.this.finish();
								}
							}).setNegativeButton("取消", null).create().show();
			break;
		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	protected void onResume() {
		// 中途点击什么主页什么按钮暂时退出程序时的恢复
		super.onResume();
		initData();
	}
}
