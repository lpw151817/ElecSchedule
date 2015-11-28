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

	// 全局变量
	private WebRequestManager webRequestManager;
	private static final String TAG = "TaskListActivity";
	private static DAOFactory daoFactory = DAOFactory.getInstance();

	// 本人ID
	private String userID;

	// 入口类型：1-发起任务；2-接收任务
	private int entranceType = -1;

	// 入口状态： 1-进行中（未完成）；2-已完成；3-已延迟
	private int entranceStatus = -1;

	// 界面标题
	private String activityTitle;

	// 控件
	private TextView tvActivityTitle;
	private Button btnBack;
	private ListView mListView;
	private Handler mHandler;
	private TextView mName, mContent, mParticipator, mDeadline, mReply;

	// 数据
	private AffairDao affairdao;
	// private List<QueryAffairListResponseAffairs> mList;// 任务列表
	private List<Map<String, Object>> mList;// 任务列表

	// 控件
	ListView taskListView;
	TaskListViewAdapter taskListViewAdapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_list);

		// 初始化网络请求管理器
		webRequestManager = new WebRequestManager(AppApplication.getInstance(), TaskList.this);

		// 准备用户信息
		userID = MySharedPreference.get(TaskList.this, MySharedPreference.USER_ID, "");

		// 根据任务界面传来的任务类型和任务状态初始化入口变量
		entranceType = getIntent().getIntExtra("type", -1);
		entranceStatus = getIntent().getIntExtra("status", -1);

		initActionBar();

		// onResume中已经调用过
		// // 初始化控件和数据
		// initViewAndData();
		// // 初始化列表适配器
		// initAdapter();

	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("任务列表");
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

		// 数据准备
		if (entranceType == 1) { // 发起任务
			activityTitle = "发起任务 - ";
			if (entranceStatus == 1) { // 进行中（未完成）
				activityTitle += "进行中";
				// 获取数据
				mList = affairdao.getAffairByTypeAndStatus(1, 1, userID);
			} else if (entranceStatus == 2) { // 已完成
				activityTitle += "已完成";
				mList = affairdao.getAffairByTypeAndStatus(1, 2, userID);
			} else if (entranceStatus == 3) { // 延迟
				activityTitle += "已延迟";
				mList = affairdao.getAffairByTypeAndStatus(1, 3, userID);
			} else {
				mList = null;
			}
		} else if (entranceType == 2) { // 接收任务
			activityTitle = "接收任务 - ";
			if (entranceStatus == 1) { // 进行中（未完成）
				activityTitle += "进行中";
				mList = affairdao.getAffairByTypeAndStatus(2, 1, userID);
			} else if (entranceStatus == 2) { // 已完成
				activityTitle += "已完成";
				mList = affairdao.getAffairByTypeAndStatus(2, 2, userID);
			} else if (entranceStatus == 3) { // 延迟
				activityTitle += "已延迟";
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