package nercms.schedule.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.NoScrollViewPager;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.readystatesoftware.viewbadger.BadgeView;

public class MainContent extends FragmentActivity implements OnClickListener {

	private TextView tv;
	int count;
	int secondCount;
	private BadgeView badgeView;
	private TextView tv_second;
	private BadgeView secondBadgeView;
	private TextView tv_third;
	private NoScrollViewPager contentPager;
	private List<Fragment> mLi = new ArrayList<Fragment>();
	Map<String, String> mPageReferenceMap = new HashMap<String, String>();
	private FirstFragment mFirstFrag;
	private SecondFragment mSecondFrag;
	private ThirdFragment mThirdFrag;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_content);
		WindowManager wm = (WindowManager) (MainContent.this)
			     .getSystemService(Context.WINDOW_SERVICE);
			 
			     int width = wm.getDefaultDisplay().getWidth();
			     int height = wm.getDefaultDisplay().getHeight();

	    contentPager = (NoScrollViewPager) findViewById(R.id.lv_viewpager);
		tv = (TextView) findViewById(R.id.tv_first);
		tv_second = (TextView) findViewById(R.id.tv_second);
		tv_third = (TextView) findViewById(R.id.tv_third);
		count = 0;
		
		tv.setLayoutParams(new LayoutParams( width/3, LayoutParams.MATCH_PARENT));
		tv_second.setLayoutParams(new LayoutParams( width/3, LayoutParams.MATCH_PARENT));

		//°ó¶¨¼àÌý
		tv.setOnClickListener(this);
		tv_second.setOnClickListener(this);
		tv_third.setOnClickListener(this);

		badgeView = new BadgeView(MainContent.this, tv);
		secondBadgeView = new BadgeView(MainContent.this, tv_second);
		
		
		mFirstFrag = new FirstFragment(MainContent.this, tv);
		 
		mSecondFrag = new SecondFragment(MainContent.this, tv_second);
		 
		mThirdFrag = new ThirdFragment(MainContent.this);
		FragmentPagerAdapter madapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
			
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
		
//		show(count);
		
		
	}

//	private void show(int count) {
//		if (count == 0 || count < 0){
//			badgeView.setVisibility(View.GONE);
//			secondBadgeView.setVisibility(View.GONE);
//			return;
//		}
//		badgeView.setText(count+"");
//		badgeView.show();
//		
//		secondBadgeView.setText(count+"");
//		secondBadgeView.show();
//		
//		
//	}
	
//	private void showFragmentBadge(int count){
//		int index = contentPager.getCurrentItem();
//		switch (index) {
//		case 0:
//			mFirstFrag.show(count);
//			break;
//			
//		case 1:
//			mSecondFrag.show(count);
//			break;
//
//		default:
//			break;
//		}
//	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_first:
			Utils.showToast(MainContent.this, "tab1");
			contentPager.setCurrentItem(0);
			break;
			
		case R.id.tv_second:
			Utils.showToast(MainContent.this, "tab2");
			contentPager.setCurrentItem(1);
			break;
			
		case R.id.tv_third:
			Utils.showToast(MainContent.this, "tab2");
			contentPager.setCurrentItem(2);
			break;
		default:
			break;
		}
	}

}
