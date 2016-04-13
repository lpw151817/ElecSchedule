package nercms.schedule.activity;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.view.MenuItem;
import com.imooc.treeview.utils.Node;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.wxapp.service.elec.dao.Org;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.model.bean.table.TB_SYS_Person;
import android.wxapp.service.elec.model.bean.table.tb_task_info;

public class DailyTaskDetail extends BaseActivity {

	int enterType;
	String status;

	Node orgs;

	String tid;
	tb_task_info info;

	private TextView mName;
	private TextView mStopRange;
	private TextView mStopPlace;
	private TextView mMainContent;
	private TextView mWorkManager;
	private TextView mStartTime;
	private TextView mStopTime;
	private TextView mType;
	private TextView mReport;
	private TextView mSpecialNeed;
	private TextView mLeader;
	private TextView mThree;
	private TextView mBelong;
	private TextView mStop;
	private TextView mStopType;
	private TextView mCompany;
	private TextView mPeople;
	private TextView mMemo;
	private tb_task_info planTask2;

	OrgDao orgDao;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_daily_task);

		tid = getIntent().getStringExtra("tid");
		PlanTaskDao planTask = new PlanTaskDao(this);
		planTask2 = planTask.getPlanTask(tid);
		orgDao = new OrgDao(this);

		iniActionBar(true, null, "任务详情");
		initView();
	}

	private void initView() {
		mName = (TextView) findViewById(R.id.tv_project_name);
		mStopRange = (TextView) findViewById(R.id.tv_stop_range);
		mStopPlace = (TextView) findViewById(R.id.tv_stop_place);
		mMainContent = (TextView) findViewById(R.id.tv_main_content);
		mWorkManager = (TextView) findViewById(R.id.tv_work_manager);
		mStartTime = (TextView) findViewById(R.id.tv_start_time);
		mStopTime = (TextView) findViewById(R.id.tv_stop_time);
		mType = (TextView) findViewById(R.id.tv_type);
		mReport = (TextView) findViewById(R.id.tv_report);
		mSpecialNeed = (TextView) findViewById(R.id.tv_special_need);
		mLeader = (TextView) findViewById(R.id.tv_leader_manager);
		mThree = (TextView) findViewById(R.id.tv_three);
		mBelong = (TextView) findViewById(R.id.tv_belong);
		mStop = (TextView) findViewById(R.id.tv_stop);
		mStopType = (TextView) findViewById(R.id.tv_stop_type);

		mCompany = (TextView) findViewById(R.id.tv_company);
		mPeople = (TextView) findViewById(R.id.tv_people);
		mMemo = (TextView) findViewById(R.id.tv_memo);

		if (null == planTask2)
			return;

		mName.setText(TextUtils.isEmpty(planTask2.getName()) ? "空" : planTask2.getName());

		if (!TextUtils.isEmpty(planTask2.getPower_cut_range()))
			mStopRange.setText(planTask2.getPower_cut_range());
		else
			mStopRange.setHint("");

		if (!TextUtils.isEmpty(planTask2.getEffect_eara()))
			mStopPlace.setText(planTask2.getEffect_eara());
		else
			mStopPlace.setHint("");

		if (!TextUtils.isEmpty(planTask2.getContent()))
			mMainContent.setText(planTask2.getContent());
		else
			mMainContent.setHint("");

		if (!TextUtils.isEmpty(planTask2.getResponsibility_user()))
			mWorkManager.setText(orgDao.getPerson(planTask2.getResponsibility_user()).getName());
		else
			mWorkManager.setHint("");

		if (!TextUtils.isEmpty(planTask2.getPlan_start_time()))
			mStartTime.setText(Utils.formatDateMs(planTask2.getPlan_start_time()));

		if (!TextUtils.isEmpty(planTask2.getPlan_end_time()))
			mStopTime.setText(Utils.formatDateMs(planTask2.getPlan_end_time()));

		String cut_type = planTask2.getCategory();
		if (!TextUtils.isEmpty(cut_type)) {
			cut_type = cut_type.substring(planTask2.getCategory().length() - 2);
			if (cut_type.equals("02")) {
				mType.setText("计划停电");
			} else if (cut_type.equals("01")) {
				mType.setText("临时停电");
			} else if (cut_type.equals("03")) {
				mType.setText("其它");
			}
		}

		if (!TextUtils.isEmpty(planTask2.getIs_publish())) {
			if (planTask2.getIs_publish().equals("0")) {
				mReport.setText("否");
			} else if (planTask2.getIs_power_cut().equals("1")) {
				mReport.setText("是");
			}
		} else
			mReport.setText("");

		String special = planTask2.getSpecial();
		if (!TextUtils.isEmpty(special)) {
			if (special.equals("0")) {
				mSpecialNeed.setText("T");
				;
			} else if (special.equals("1")) {
				mSpecialNeed.setText("否");
			}
		}

		if (!TextUtils.isEmpty(planTask2.getLeader())) {
			TB_SYS_Person person = orgDao.getPerson(planTask2.getLeader());
			if (person != null)
				mLeader.setText(person.getName());
		} else
			mLeader.setText("");

		if (!TextUtils.isEmpty(planTask2.getMeasures()))
			mThree.setText(planTask2.getMeasures());
		else
			mThree.setHint("");

		String domain = planTask2.getDomain();
		if (!TextUtils.isEmpty(domain)) {
			domain = domain.substring(planTask2.getDomain().length() - 2);
			if (domain.equals("02")) {
				mBelong.setText("地");
			} else if (domain.equals("03")) {
				mBelong.setText("配");
			} else if (domain.equals("05")) {
				mBelong.setText("其它");
			} else if (domain.equals("01")) {
				mBelong.setText("省");
			} else if (domain.equals("04")) {
				mBelong.setText("县");
			}
		}

		if (!TextUtils.isEmpty(planTask2.getIs_power_cut())) {
			if (planTask2.getIs_power_cut().equals("0")) {
				mStop.setText("否");
			} else if (planTask2.getIs_power_cut().equals("1")) {
				mStop.setText("是");
			}
		} else
			mStop.setText("");

		String category = planTask2.getCut_type();
		if (!TextUtils.isEmpty(category)) {
			category = category.substring(planTask2.getCut_type().length() - 2);
			if (category.equals("01")) {
				mStopType.setText("临时停电");
			} else if (category.equals("02")) {
				mStopType.setText("计划停电");
			} else if (category.equals("03")) {
				mStopType.setText("其他");
			}
		}

		if (!TextUtils.isEmpty(planTask2.getImplement_org())) {
			Org org = orgDao.getOrg(planTask2.getImplement_org());
			if (org != null)
				mCompany.setText("江陵供电公司" + org.getTitle());

		} else
			mCompany.setHint("");

		if (!TextUtils.isEmpty(planTask2.getNumber()))
			mPeople.setText(planTask2.getNumber());

		if (!TextUtils.isEmpty(planTask2.getRemark()))
			mMemo.setText(planTask2.getRemark());
		else
			mMemo.setHint("");

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
