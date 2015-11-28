package nercms.schedule.activity;

import nercms.schedule.R;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.dao.PhoneDao;
import android.wxapp.service.model.PhoneModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class PhoneDetail extends BaseActivity {

	private String userID;
	private String phoneID;
	private String objectID;

	private StructuredStaffModel objectSSM;
	private PhoneModel phone;

	private DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao;
	private PhoneDao phoneDao;

	// �ؼ�
	private TextView nameTv, startTimeTv, typeTv, statusTv, departmentTv,
			positionTv, rankTv;
	private Button chatBtn, phoneBtn, videoChatBtn;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.phone_detail);
		// �����û�ID
		userID = MySharedPreference.get(PhoneDetail.this,
				MySharedPreference.USER_ID, "");
		phoneID = getIntent().getStringExtra("phone_id");
		objectID = getIntent().getStringExtra("object_id");

		initView();
		// ��ʼ��ActionBar
		initActionBar();
		initData();
	}

	// ����actionbar����
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("�绰����");
	}


	private void initData() {
		personDao = daoFactory.getPersonDao(PhoneDetail.this);
		phoneDao = daoFactory.getPhoneDao(PhoneDetail.this);

		objectSSM = personDao.getSSMByID(objectID);
		phone = phoneDao.getPhoneByID(phoneID);

		if (phone != null) {

			nameTv.setText(objectSSM.getName());
			startTimeTv.setText(phone.getStartTime());
			if (userID.equals(Integer.toString(phone.getCallerID()))) { // ����Ϊ������
				typeTv.setText("����");
			} else {
				typeTv.setText("����");
				if (phone.getIsAnswered() == 0) { // ���룬δ�ӵ绰
					startTimeTv.setTextColor(getResources().getColor(
							R.color.red));
					typeTv.setTextColor(getResources().getColor(R.color.red));
					statusTv.setTextColor(getResources().getColor(R.color.red));
				}
			}
			if (phone.getIsAnswered() == 1) { // �ѽ�ͨ
				statusTv.setText(phone.getDuration() + "��");
			} else {
				statusTv.setText("δ��ͨ");
			}

			departmentTv.setText(objectSSM.getOrgDescription());
			positionTv.setText(objectSSM.getPosition());
			rankTv.setText(objectSSM.getRank());
		}

		}


	private void initView(){
		nameTv = (TextView) findViewById(R.id.phone_detail_name_tv);
		startTimeTv = (TextView) findViewById(R.id.phone_detail_starttime_tv);
		typeTv = (TextView) findViewById(R.id.phone_detail_type_tv);
		statusTv = (TextView) findViewById(R.id.phone_detail_status_tv);
		departmentTv = (TextView) findViewById(R.id.phone_detail_department_tv);
		positionTv = (TextView) findViewById(R.id.phone_detail_position_tv);
		rankTv = (TextView) findViewById(R.id.phone_detail_rank_tv);
		chatBtn = (Button) findViewById(R.id.phone_detail_chat_btn);
		phoneBtn = (Button) findViewById(R.id.phone_detail_phone_btn);
//		videoChatBtn = (Button) findViewById(R.id.phone_detail_videochat_btn);

		chatBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// ��ȥ��Ϣ�Ի�����
				Intent intent = new Intent(PhoneDetail.this, ChatDetail.class);
				intent.putExtra("entrance_type", 1);
				intent.putExtra("selected_id", Integer.parseInt(objectID));
				intent.putExtra("selected_name", objectSSM.getName());
				startActivity(intent);
				PhoneDetail.this.finish();
			}
		});
		phoneBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub

			}
		});
//		videoChatBtn.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View arg0) {
//				// TODO Auto-generated method stub
//
//			}
//		});
	}

	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// ���������ҳ
			finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
