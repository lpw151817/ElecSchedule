package nercms.schedule.adapter;

import nercms.schedule.fragment.Chat;
import nercms.schedule.fragment.Contact;
import nercms.schedule.fragment.Meeting;
import nercms.schedule.fragment.Phone;
import nercms.schedule.fragment.Task;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class MainFragmentAdapter extends FragmentPagerAdapter{
	private String[] titleStr = {"任务","通讯录","消息","电话","会议"};

	
	public MainFragmentAdapter(FragmentManager fm) {
		super(fm);
	}
	
	public MainFragmentAdapter(FragmentManager fm, Context context) {
		super(fm);
	}

	@Override
	public Fragment getItem(int position) {
		switch (position) {
		case 0:
			return Task.newInstance();
		case 1:
			return Contact.newInstance();
		case 2:
			return Chat.newInstance();		
		case 3:
			return Phone.newInstance();
		case 4:
			return Meeting.newInstance();	
		}
		return null;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		// TODO Auto-generated method stub
		return titleStr[position];
	}

	@Override
	public int getItemPosition(Object object) {
		// TODO Auto-generated method stub
		return POSITION_NONE;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return titleStr.length;
	}

}
