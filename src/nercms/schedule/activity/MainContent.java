package nercms.schedule.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.nercms.schedule.ui.MediaInstance;

import nercms.schedule.R;
import nercms.schedule.fragment.FirstFragment;
import nercms.schedule.fragment.FourthFragment;
import nercms.schedule.fragment.SecondFragment;
import nercms.schedule.fragment.ThirdFragment;
import nercms.schedule.utils.AttachmentUpload;
import nercms.schedule.utils.HeartBeat;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.NoScrollViewPager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.util.MySharedPreference;

public class MainContent extends FragmentActivity implements OnClickListener {

	protected static final int UPDATE_CONTENT = 564;
	int count;
	int secondCount;
	private NoScrollViewPager contentPager;
	private List<Fragment> mLi = new ArrayList<Fragment>();
	Map<String, String> mPageReferenceMap = new HashMap<String, String>();
	private FirstFragment mFirstFrag;
	private SecondFragment mSecondFrag;
	private ThirdFragment mThirdFrag;
	private FourthFragment mFourthFrag;
	public int zuoyecount;
	public int caozuocount;
	public int qiangxiucount;

	private LinearLayout zuoYeLayout;
	private LinearLayout caoZuoLayout;
	private LinearLayout qiangXiuLayout;
	private LinearLayout leaderLayout;
	private TextView zuoye;
	private TextView caozuo;
	private TextView qiangxiu;
	private TextView leader;
	private PlanTaskDao dao;
	private com.jauker.widget.BadgeView badgeView1;
	private com.jauker.widget.BadgeView badgeView2;
	private com.jauker.widget.BadgeView badgeView3;
	private ScheduledExecutorService service;
	int delayedTime = 1000;// 延时1s
	private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.e("Demo", "MainContent:Oncreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_content);

		HeartBeat.start(getApplicationContext());
		AttachmentUpload.instance().start(getApplicationContext());

		dao = new PlanTaskDao(this);
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

		if (isAdmin()) {
			
			leaderLayout = (LinearLayout) findViewById(R.id.leaderLayout);
			leaderLayout.setVisibility(View.VISIBLE);
			leader = (TextView) findViewById(R.id.leader);
			leader.setVisibility(View.VISIBLE);
			mFourthFrag = new FourthFragment(MainContent.this);
//			mLi.add(mFourthFrag);
			leaderLayout.setOnClickListener(this);
		}

		zuoye = (TextView) findViewById(R.id.zuoye);
		caozuo = (TextView) findViewById(R.id.caozuo);
		qiangxiu = (TextView) findViewById(R.id.qiangxiu);
		

		mFirstFrag = new FirstFragment(MainContent.this, zuoyecount);
		mSecondFrag = new SecondFragment(MainContent.this, caozuocount);
		mThirdFrag = new ThirdFragment(MainContent.this, qiangxiucount);

		badgeView1 = new com.jauker.widget.BadgeView(this);
		zuoYeLayout.addView(badgeView1);

		badgeView2 = new com.jauker.widget.BadgeView(this);
		caoZuoLayout.addView(badgeView2);

		badgeView3 = new com.jauker.widget.BadgeView(this);
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
		if(isAdmin()){
			mLi.add(mFourthFrag);
		}

		contentPager.setAdapter(madapter);
		contentPager.setOffscreenPageLimit(4);

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				if (msg.what == UPDATE_CONTENT) {
					zuoyecount = ((Integer) msg.obj).intValue();
					caozuocount = msg.arg1;
					qiangxiucount = msg.arg2;
					badgeView1.setText("" + zuoyecount);
					badgeView2.setText("" + caozuocount);
					badgeView3.setText("" + qiangxiucount);

					System.out.println("zuoyecount : " + zuoyecount
							+ " caozuocount : " + caozuocount);
					mFirstFrag.getBadgeView1().setText("" + zuoyecount);
					mSecondFrag.getBadgeView1().setText("" + caozuocount);
					mThirdFrag.getBadgeView1().setText("" + qiangxiucount);
				}

			}
		};
	}

	@Override
	protected void onResume() {
		Log.e("Demo", "MainContent:OnResume");
		super.onResume();

		Runnable thread = new Runnable() {

			@Override
			public void run() {

				if (dao == null) {
					dao = new PlanTaskDao(MainContent.this);
				}

				if (isAdmin()) {
					zuoyecount = dao.getPlanTasks(1, 3, null, "0").size();
					caozuocount = dao.getPlanTasks(2, 3, null, "0").size();
					qiangxiucount = dao.getPlanTasks(3, 3, null, "0").size();
				} else {
					zuoyecount = dao.getPlanTasks(1, 3, getUserId(), "0")
							.size();
					caozuocount = dao.getPlanTasks(2, 3, getUserId(), "0")
							.size();
					qiangxiucount = dao.getPlanTasks(3, 3, getUserId(), "0")
							.size();
				}

				Message msg = new Message();
				msg.what = UPDATE_CONTENT;
				msg.obj = new Integer(zuoyecount);
				msg.arg1 = caozuocount;
				msg.arg2 = qiangxiucount;
				handler.sendMessage(msg);
			}
		};

		service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(thread, 100, delayedTime,
				TimeUnit.MILLISECONDS);

		// PlanTaskDao mDao;
		// mDao = new PlanTaskDao(this);
		// /*
		// * userid,如果是管理员就传入null,如果不是就getUserId在BaseActivity中，
		// */
		// // dao.getPlanTasks(1, 3, "", 0).size();
		// if (isAdmin()) {
		// zuoyecount = mDao.getPlanTasks(1, 3, null, "0").size();
		// caozuocount = mDao.getPlanTasks(2, 3, null, "0").size();
		// qiangxiucount = mDao.getPlanTasks(3, 3, null, "0").size();
		// } else {
		// zuoyecount = mDao.getPlanTasks(1, 3, getUserId(), "0").size();
		// caozuocount = mDao.getPlanTasks(2, 3, getUserId(), "0").size();
		// qiangxiucount = mDao.getPlanTasks(3, 3, getUserId(), "0").size();
		// }
		//
		// badgeView1.setText("" + zuoyecount);
		// badgeView2.setText("" + caozuocount);
		// badgeView3.setText("" + qiangxiucount);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

	}

	@Override
	protected void onPause() {
		super.onPause();
		service.shutdownNow();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.zuoyeLayout:
			// Utils.showToast(MainContent.this, "tab1");
			if (isAdmin()) {
				contentPager.setCurrentItem(0);
				
				if (leader != null)
				leader.setTextColor(getResources().getColor(R.color.deepgray));
			} else {
				contentPager.setCurrentItem(0);
			}
			zuoye.setTextColor(getResources().getColor(R.color.orange));
			caozuo.setTextColor(getResources().getColor(R.color.deepgray));
			qiangxiu.setTextColor(getResources().getColor(R.color.deepgray));
			
			break;

		case R.id.caozuoLayout:
			// Utils.showToast(MainContent.this, "tab2");

			if (isAdmin()) {
				contentPager.setCurrentItem(1);
				if (leader != null)
				leader.setTextColor(getResources().getColor(R.color.deepgray));
			} else {
				contentPager.setCurrentItem(1);
			}
			caozuo.setTextColor(getResources().getColor(R.color.orange));
			zuoye.setTextColor(getResources().getColor(R.color.deepgray));
			qiangxiu.setTextColor(getResources().getColor(R.color.deepgray));
			
			break;

		case R.id.qiangxiuLayout:
			// Utils.showToast(MainContent.this, "tab2");
			if (isAdmin()) {
				contentPager.setCurrentItem(2);
				if (leader != null)
				leader.setTextColor(getResources().getColor(R.color.deepgray));
			} else {
				contentPager.setCurrentItem(2);
			}
			qiangxiu.setTextColor(getResources().getColor(R.color.orange));
			zuoye.setTextColor(getResources().getColor(R.color.deepgray));
			caozuo.setTextColor(getResources().getColor(R.color.deepgray));
			
			break;

		case R.id.leaderLayout:
			if (isAdmin())
				contentPager.setCurrentItem(3);
			qiangxiu.setTextColor(getResources().getColor(R.color.deepgray));
			zuoye.setTextColor(getResources().getColor(R.color.deepgray));
			caozuo.setTextColor(getResources().getColor(R.color.deepgray));
			leader.setTextColor(getResources().getColor(R.color.orange));

		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (isAdmin()) {
			MenuItem item2 = menu.add(0, 3, 0, "日计划录入");
			item2.setIcon(R.drawable.icon_shulu);
			item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);// 总是作为Action项显示
		}
		// 子菜单 ：发起任务，消息，会议，视频直播，智能调度，添加客户
		SubMenu addMenu = menu.addSubMenu("add item");
		addMenu.add(0, 1, 1, "视频调度").setIcon(R.drawable.iconfont_video);
		// addMenu.add(0, 2, 1, "修改密码").setIcon(R.drawable.ofm_meeting_icon);
		// addMenu.add(0, -1, 1, "通知").setIcon(R.drawable.iconfont_tongzhi);
		addMenu.add(0, 4, 1, "退出").setIcon(R.drawable.iconfont_tongzhi);

		MenuItem addItem = addMenu.getItem();
		addItem.setIcon(R.drawable.ic_action_overflow);
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);// 总是作为Action项显示

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			startActivity(Login.class);
			MainContent.this.finish();
			break;
		case -1:
			startActivity(ZhilingActivity.class);
			break;
		case 2:
			// TODO 修改密码
			break;
		case 1:
			// startActivity(ScheduleActivity.class);
			// 将页面调至前台
			// Intent intent2 = new Intent(getApplicationContext(),
			// ScheduleActivity.class);
			// intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
			// Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// getApplicationContext().startActivity(intent2);
			ScheduleActivity.wakeUp(MainContent.this, null);
			break;
		case 3:
			// 添加日计划
			Intent intent = new Intent(MainContent.this, PlanAdd.class);
			intent.putExtra("enterType", 1);
			MainContent.this.startActivity(intent);
			break;
		case 4:
			// 退出
			Log.e("Demo", "api shutdown");
			HeartBeat.stop(getApplicationContext());
			AttachmentUpload.instance().stop(getApplicationContext());
			MediaInstance.instance().api_shutdown();
			finish();
			System.exit(0);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected boolean isAdmin() {
		OrgDao dao = new OrgDao(this);
		try {
			if (dao.getPerson(getUserId()).getType() != null) {
				return dao.getPerson(getUserId()).getType().equals("1");
			} else {
				return dao.getPerson(getUserId()).getName().contains("管理员")
						|| dao.getPerson(getUserId()).getName().contains("领导");
			}
		} catch (Exception e) {
			return false;
		}

	}

	protected String getUserId() {
		return MySharedPreference.get(this, MySharedPreference.USER_ID, null);
	}

	protected void startActivity(Bundle bundle, Class<?> targetActivity) {
		Intent intent = new Intent();
		intent.setClass(this, targetActivity);
		if (bundle != null)
			intent.putExtras(bundle);
		startActivity(intent);
	}

	protected void startActivity(Class<?> targetActivity) {
		startActivity(null, targetActivity);
	}

	@Override
	public void onBackPressed() {
		Log.e("Demo", "MainContent:onBackPressed");
		moveTaskToBack(true);
	}

	@Override
	protected void onRestart() {
		Log.e("Demo", "MainContent:OnRestart");
		super.onRestart();
	}

	@Override
	protected void onStart() {
		Log.e("Demo", "MainContent:OnStart");
		super.onStart();
	}

}
