package nercms.schedule.activity;

import java.util.ArrayList;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.adapter.SuperTreeViewAdapter;
import nercms.schedule.adapter.TreeViewAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.GroupDao;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.jerry.model.group.GroupUpdateQueryRequestGroups;
import android.wxapp.service.jerry.model.group.GroupUpdateQueryRequestIds;
import android.wxapp.service.jerry.model.person.Contacts;
import android.wxapp.service.jerry.model.person.GetPersonInfoResponse;
import android.wxapp.service.jerry.model.person.OrgPersonInfo;
import android.wxapp.service.jerry.model.person.Contacts.CONTACT_ITEM;
import android.wxapp.service.model.ContactModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * @author jiaocuina@gmail.com
 * @date 2014-03-06
 * @version V1.0
 * @description 跳转到联系人详细信息页面 不可以对联系人进行编辑及修改 可以发起消息|电话|会议 0527修改为actionbar样式
 *              数据主要参考StructuredStaffModel
 */
public class ContactDetail extends BaseActivity {

	// 本地数据请求入口
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao = null;
	private GroupDao groupDao;
	// 控件
	private TextView name;
	private TextView orgDesc;// 机构节点名称
	private TextView position;// 职务
	private TextView rank;// 职衔

	private TextView mobile;// 手机
	private TextView email;// 邮箱
	private TextView address;// 地址

	private Button btn_chat;
	private Button btn_phone;
	// private Button btn_meeting;

	private TextView groupOrgTv;
	private TextView groupPersonsTv;
	private LinearLayout contactLayout;
	private LinearLayout groupLayout;

	private String userID;// 本人ID
	private String contactID = null; // 联系人ID
	private String contactName = null;
	public static final String TAG = "ContactDetail";
	private SuperTreeViewAdapter superTree;
	public TreeViewAdapter treeViewAdapter;
	// private ArrayList<StructuredStaffModel> memberSSMList;

	private String IMSI = "";

	private boolean isGroup = false;
	private String orgCode = null;
	private String orgName = null;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_detail);
		// contactID初始化

		if (getIntent().getExtras().getInt("IS_GROUP") == 1) { // 是群组点击
			isGroup = true;
			orgCode = getIntent().getExtras().getString("CONTACT_ID");
			groupDao = new GroupDao(this);
		} else {
			isGroup = false;
			contactID = getIntent().getExtras().getString("CONTACT_ID");
		}
		userID = MySharedPreference.get(ContactDetail.this, MySharedPreference.USER_ID, null);

		// 初始化控件
		initView();
		// 初始化ActionBar
		initActionBar();
		// 初始化部分数据
		initData();
	}

	private void initView() {
		name = (TextView) findViewById(R.id.contact_name);
		orgDesc = (TextView) findViewById(R.id.contact_org);
		position = (TextView) findViewById(R.id.contact_position);
		rank = (TextView) findViewById(R.id.contact_rank);
		mobile = (TextView) findViewById(R.id.contact_phone);
		email = (TextView) findViewById(R.id.contact_mail);
		address = (TextView) findViewById(R.id.contact_address);

		groupOrgTv = (TextView) findViewById(R.id.contact_group_org);
		groupPersonsTv = (TextView) findViewById(R.id.contact_group_person_tv);
		contactLayout = (LinearLayout) findViewById(R.id.contact_detail_contact_ll);
		groupLayout = (LinearLayout) findViewById(R.id.contact_detail_group_ll);

		btn_chat = (Button) findViewById(R.id.btn_start_chat);
		btn_phone = (Button) findViewById(R.id.btn_start_phone);
		// btn_meeting = (Button) findViewById(R.id.btn_start_meeting);

		if (isGroup) {
			contactLayout.setVisibility(View.GONE);
			groupLayout.setVisibility(View.VISIBLE);
		} else {
			groupLayout.setVisibility(View.GONE);
			contactLayout.setVisibility(View.VISIBLE);
			// 0625 如果是本人则隐藏发消息，打电话，发起会议三个按钮
			if (contactID.equalsIgnoreCase(userID)) {
				btn_chat.setVisibility(View.INVISIBLE);
				btn_phone.setVisibility(View.INVISIBLE);
				// btn_meeting.setVisibility(View.INVISIBLE);

			}
		}

		btn_chat.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (isGroup) {
					// 与orgID对应的群组发起一个群聊
					Intent intent = new Intent(ContactDetail.this, ChatDetail.class);
					intent.putExtra("entrance_type", 1);
					intent.putExtra("isGroup", true);
					intent.putExtra("selected_id", Integer.parseInt(orgCode));
					intent.putExtra("selected_name", orgName);
					startActivity(intent);
				} else {
					// 与contactID对应的人发起一个聊天
					Intent intent = new Intent(ContactDetail.this, ChatDetail.class);
					intent.putExtra("entrance_type", 1);
					intent.putExtra("selected_id", Integer.parseInt(contactID));
					intent.putExtra("selected_name", contactName);
					startActivity(intent);
				}

			}
		});
		btn_phone.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!isGroup) {
					// 2014-6-11 测试电话
					// String phoneID = Utils.producePhoneID(userID);
					// String startTime = new SimpleDateFormat(
					// "yyyy-MM-dd HH:mm:ss").format(new Date(System
					// .currentTimeMillis()));
					// PhoneModel phone = new PhoneModel(phoneID, 1, Integer
					// .parseInt(userID), Integer.parseInt(contactID),
					// startTime, 0, "", "", Constant.READ);
					// phone.save(ContactDetail.this);
					// Toast.makeText(ContactDetail.this, "呼叫成功",
					// Toast.LENGTH_SHORT).show();

					// 2014-8-5
					Intent intent = new Intent(ContactDetail.this, VoiceCall.class);
					intent.putExtra("callee_id", contactID);
					intent.putExtra("callee_name", contactName);
					intent.putExtra("call_type", 2);
					startActivity(intent);

				}
			}
		});
		/*
		 * btn_meeting.setOnClickListener(new OnClickListener() { public void
		 * onClick(View v) { // 2014-7-18 WeiHao //
		 * 测试---------------------------------------------------------------
		 * boolean started_schedule_process = false; List<RunningAppProcessInfo>
		 * processes = ((ActivityManager) getSystemService(ACTIVITY_SERVICE))
		 * .getRunningAppProcesses();
		 * 
		 * for (RunningAppProcessInfo ra : processes) { if
		 * (ra.processName.equalsIgnoreCase("com.nercms.schedule")) {
		 * started_schedule_process = true; break; } }
		 * 
		 * String participants = "{\"t\":\"external_convene\",\"p\":[{\"id\":\""
		 * + IMSI + "\",\"n\":\"" + contactName + "\",\"r\":\"1\"}],\"v\":\"" +
		 * IMSI + "\"}"; Intent convene_schedule = new Intent();
		 * convene_schedule .setAction((true == started_schedule_process) ?
		 * "com.nercms.schedule.External_Convene_Schedule" :
		 * "com.nercms.schedule.External_Start_Schedule");
		 * convene_schedule.putExtra("participants", participants);
		 * 
		 * Log.v("ContactDetail", "发起会议 name:" + contactName + ",id:" +
		 * contactID + ",imsi:" + IMSI);
		 * 
		 * Utils.showShortToast(ContactDetail.this, IMSI);
		 * startActivity(convene_schedule); } });
		 */
	}

	private void initData() {

		personDao = daoFactory.getPersonDao(ContactDetail.this);

		if (isGroup) {
			String memberNameString = "";
			if (orgCode.startsWith("g")) {
				GroupUpdateQueryRequestGroups group = groupDao.queryGroupById(orgCode.substring(1));
				orgName = group.getN();
				name.setText(group.getN());
				for (GroupUpdateQueryRequestIds item : group.getRids()) {
					memberNameString += (personDao.getPersonInfo(item.getRid()).getN() + "/");
				}
			} else if (orgCode.startsWith("o")) {
				name.setText(personDao.getOrgDes(orgCode.substring(1)));
				for (OrgPersonInfo item : personDao.getOrgPersonInfos(orgCode.substring(1))) {
					memberNameString += item.getN() + "/";
				}
			}

			groupOrgTv.setText("???这里应该填啥???");
			groupPersonsTv.setText(memberNameString);

			// if (memberSSMList.size() > 0) {
			// String memberNameString = "";
			// StructuredStaffModel memberSSM;
			// for (int i = 0; i < memberSSMList.size(); i++) {
			// memberSSM = memberSSMList.get(i);
			// if (i != memberSSMList.size() - 1) { // 非最后一项
			// // if (i % 2 != 0) { // 偶数项时（i为奇）加换行，保证每行显示两项
			// // memberNameString += memberSSM.getName() + ";"
			// // + "\n";
			// // } else { // 奇数项时
			// memberNameString += memberSSM.getName() + " ; ";
			// // }
			// } else { // 最后一项，不显示分号
			// memberNameString += memberSSM.getName();
			// }
			// }
			// groupPersonsTv.setText(memberNameString);
			// }

		} else {
			GetPersonInfoResponse personInfo = personDao.getPersonInfo(contactID);
			String _name = personInfo.getN();
			contactName = _name;
			name.setText(_name);
			String _orgDesc = personDao.getOrgByPersonId(contactID);
			orgDesc.setText(_orgDesc);
			String _position = personInfo.getR();
			position.setText(_position);
			// String _rank = contactSSM.getRank();
			// rank.setText(_rank);
			List<Contacts> contacts = personInfo.getContacts();
			try {
				for (Contacts item : contacts) {
					// (1：手机号2：座机号3：SIM号4：手台号码5：邮箱)
					if (item.getT() == CONTACT_ITEM.MOBILE) {
						mobile.setText(item.getC());
					} else if (item.getT() == CONTACT_ITEM.PHONE) {

					} else if (item.getT() == CONTACT_ITEM.SIMI) {

					} else if (item.getT() == CONTACT_ITEM.HAND) {

					} else if (item.getT() == CONTACT_ITEM.EMAIL) {
						email.setText(item.getC());
					}
					// else if (item.getT().equals("6")) {
					// String _address = item.getC();
					// address.setText(_address);
					// }
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	// 顶部actionbar创建
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("联系人详情");
	}

	// 保存按钮点击时 判断输入+数据保存到本地
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// 左键返回主页
			finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
