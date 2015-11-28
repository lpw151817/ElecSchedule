package nercms.schedule.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.adapter.TaskListViewAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.AffairDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.jerry.model.affair.QueryAffairListResponseAffairs;
import android.wxapp.service.model.AffairModel;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

/**
 * 6.10 FINAL
 * 
 * @author JerryLiu
 *
 */
public class TaskList extends BaseActivity {

	// 2014-5-23 WeiHao

	// ȫ�ֱ���
	private WebRequestManager webRequestManager;
	private static final String TAG = "TaskListActivity";
	private static DAOFactory daoFactory = DAOFactory.getInstance();

	// ����ID
	private String userID;

	// ������ͣ�1-��������2-��������
	private int entranceType = -1;

	// ���״̬�� 1-�����У�δ��ɣ���2-����ɣ�3-���ӳ�
	private int entranceStatus = -1;

	// �������
	private String activityTitle;

	// �ؼ�
	private TextView tvActivityTitle;
	private Button btnBack;
	private ListView mListView;
	private Handler mHandler;
	private TextView mName, mContent, mParticipator, mDeadline, mReply;

	// ����
	private AffairDao affairdao;
	// private List<QueryAffairListResponseAffairs> mList;// �����б�
	private List<Map<String, Object>> mList;// �����б�

	// �ؼ�
	ListView taskListView;
	TaskListViewAdapter taskListViewAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_list);

		// ��ʼ���������������
		webRequestManager = new WebRequestManager(AppApplication.getInstance(), TaskList.this);

		// ׼���û���Ϣ
		userID = MySharedPreference.get(TaskList.this, MySharedPreference.USER_ID, "");

		// ����������洫�����������ͺ�����״̬��ʼ����ڱ���
		entranceType = getIntent().getIntExtra("type", -1);
		entranceStatus = getIntent().getIntExtra("status", -1);

		initActionBar();

		// onResume���Ѿ����ù�
		// // ��ʼ���ؼ�������
		// initViewAndData();
		// // ��ʼ���б�������
		// initAdapter();

	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("�����б�");
	}

	private void initViewAndData() {
		// tvActivityTitle = (TextView) findViewById(R.id.tv_activity_title);

		// btnBack = (Button) findViewById(R.id.btn_back);
		// btnBack.setOnClickListener(new OnClickListener() {
		// @Override
		// public void onClick(View arg0) {
		// TaskList.this.finish();
		// }
		// });

		mListView = (ListView) findViewById(R.id.task_list);
		affairdao = daoFactory.getAffairDao(TaskList.this);

		activityTitle = "";

		// ����׼��
		if (entranceType == 1) { // ��������
			activityTitle = "�������� - ";
			if (entranceStatus == 1) { // �����У�δ��ɣ�
				activityTitle += "������";
				// ��ȡ����
				mList = affairdao.getAffairByTypeAndStatus(1, 1, userID);
			} else if (entranceStatus == 2) { // �����
				activityTitle += "�����";
				mList = affairdao.getAffairByTypeAndStatus(1, 2, userID);
			} else if (entranceStatus == 3) { // �ӳ�
				activityTitle += "���ӳ�";
				mList = affairdao.getAffairByTypeAndStatus(1, 3, userID);
			} else {
				mList = null;
			}
		} else if (entranceType == 2) { // ��������
			activityTitle = "�������� - ";
			if (entranceStatus == 1) { // �����У�δ��ɣ�
				activityTitle += "������";
				mList = affairdao.getAffairByTypeAndStatus(2, 1, userID);
			} else if (entranceStatus == 2) { // �����
				activityTitle += "�����";
				mList = affairdao.getAffairByTypeAndStatus(2, 2, userID);
			} else if (entranceStatus == 3) { // �ӳ�
				activityTitle += "���ӳ�";
				mList = affairdao.getAffairByTypeAndStatus(2, 3, userID);
			} else {
				mList = null;
			}
		}
		getSupportActionBar().setTitle(activityTitle);
	}

	private void initAdapter() {
		taskListViewAdapter = new TaskListViewAdapter(mList, TaskList.this, entranceType, entranceStatus);
		mListView.setAdapter(taskListViewAdapter);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onResume() {
		initViewAndData();
		initAdapter();
		super.onResume();
	}
}