package nercms.schedule.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.fragment.FirstFragment;
import nercms.schedule.fragment.SecondFragment;
import nercms.schedule.fragment.ThirdFragment;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.NoScrollViewPager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.util.MySharedPreference;

public class MainContent extends FragmentActivity implements OnClickListener {

	int count;
	int secondCount;
	private NoScrollViewPager contentPager;
	private List<Fragment> mLi = new ArrayList<Fragment>();
	Map<String, String> mPageReferenceMap = new HashMap<String, String>();
	private FirstFragment mFirstFrag;
	private SecondFragment mSecondFrag;
	private ThirdFragment mThirdFrag;
	public int zuoyecount;
	public int caozuocount;
	public int qiangxiucount;

	private LinearLayout zuoYeLayout;
	private LinearLayout caoZuoLayout;
	private LinearLayout qiangXiuLayout;
	private TextView zuoye;
	private TextView caozuo;
	private TextView qiangxiu;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_content);
		PlanTaskDao dao = new PlanTaskDao(this);
		/*
		 * userid,如果是管理员就传入null,如果不是就getUserId在BaseActivity中，
		 */
		// dao.getPlanTasks(1, 3, "", 0).size();
		if (isAdmin()) {
			zuoyecount = dao.getPlanTasks(1, 3, null, "0").size();
			caozuocount = dao.getPlanTasks(2, 3, null, "0").size();
			qiangxiucount = dao.getPlanTasks(3, 3, null, "0").size();
		} else {
			zuoyecount = dao.getPlanTasks(1, 3, getUserId(), "0").size();
			caozuocount = dao.getPlanTasks(2, 3, getUserId(), "0").size();
			qiangxiucount = dao.getPlanTasks(3, 3, getUserId(), "0").size();
		}

		contentPager = (NoScrollViewPager) findViewById(R.id.lv_viewpager);

		zuoYeLayout = (LinearLayout) findViewById(R.id.zuoyeLayout);
		caoZuoLayout = (LinearLayout) findViewById(R.id.caozuoLayout);
		qiangXiuLayout = (LinearLayout) findViewById(R.id.qiangxiuLayout);
		
		zuoye = (TextView) findViewById(R.id.zuoye);
		caozuo = (TextView) findViewById(R.id.caozuo);
		qiangxiu = (TextView) findViewById(R.id.qiangxiu);

		mFirstFrag = new FirstFragment(MainContent.this, zuoyecount);

		mSecondFrag = new SecondFragment(MainContent.this, caozuocount);

		mThirdFrag = new ThirdFragment(MainContent.this, qiangxiucount);

		com.jauker.widget.BadgeView badgeView1 = new com.jauker.widget.BadgeView(
				this);
		badgeView1.setText("" + zuoyecount);
		zuoYeLayout.addView(badgeView1);

		com.jauker.widget.BadgeView badgeView2 = new com.jauker.widget.BadgeView(
				this);
		badgeView2.setText("" + caozuocount);
		caoZuoLayout.addView(badgeView2);

		com.jauker.widget.BadgeView badgeView3 = new com.jauker.widget.BadgeView(
				this);
		badgeView3.setText("" + qiangxiucount);
		qiangXiuLayout.addView(badgeView3);

		zuoYeLayout.setOnClickListener(this);
		caoZuoLayout.setOnClickListener(this);
		qiangXiuLayout.setOnClickListener(this);
		

		FragmentPagerAdapter madapter = new FragmentPagerAdapter(
				getSupportFragmentManager()) {

			@Override
			public int getCount() {
				return mLi.size();
			}

			@Override
			public Fragment getItem(int arg0) {
				return mLi.get(arg0);
			}

		};

		mLi.add(mFirstFrag);
		mLi.add(mSecondFrag);
		mLi.add(mThirdFrag);
		contentPager.setAdapter(madapter);
		contentPager.setOffscreenPageLimit(3);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.zuoyeLayout:
//			Utils.showToast(MainContent.this, "tab1");
			contentPager.setCurrentItem(0);
			zuoye.setTextColor(getResources().getColor(R.color.blue));
			caozuo.setTextColor(getResources().getColor(R.color.black));
			qiangxiu.setTextColor(getResources().getColor(R.color.black));
			break;

		case R.id.caozuoLayout:
//			Utils.showToast(MainContent.this, "tab2");
			contentPager.setCurrentItem(1);
			caozuo.setTextColor(getResources().getColor(R.color.blue));
			zuoye.setTextColor(getResources().getColor(R.color.black));
			qiangxiu.setTextColor(getResources().getColor(R.color.black));
			break;

		case R.id.qiangxiuLayout:
//			Utils.showToast(MainContent.this, "tab2");
			contentPager.setCurrentItem(2);
			qiangxiu.setTextColor(getResources().getColor(R.color.blue));
			zuoye.setTextColor(getResources().getColor(R.color.black));
			caozuo.setTextColor(getResources().getColor(R.color.black));
			break;


		default:
			break;
		}
	}

	protected boolean isAdmin() {
		OrgDao dao = new OrgDao(this);
		if (dao.getPerson(getUserId()).getType() != null) {
			return dao.getPerson(getUserId()).getType().equals("0");
		} else {
			return dao.getPerson(getUserId()).getName().contains("管理员")
					|| dao.getPerson(getUserId()).getName().contains("领导");
		}

	}

	protected String getUserId() {
		return MySharedPreference.get(this, MySharedPreference.USER_ID, null);
	}

}
