package nercms.schedule.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.wxapp.service.elec.dao.TaskInsDao;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;
import nercms.schedule.R.menu;
import nercms.schedule.adapter.ZhilingAdapter;
import nercms.schedule.view.NoScrollViewPager;

public class ZhilingActivity extends BaseActivity implements OnClickListener {

	ListView listview;
	ZhilingAdapter adapter;
	TaskInsDao dao;
	List<tb_task_instructions> data;

	List<View> list = new ArrayList<View>();
	private View tab01;
	private View tab02;
	private TextView mUnRead;
	private TextView mRead;
	private NoScrollViewPager viewPager;
	private PagerAdapter adapter2;
	private ListView mUnReadListView;
	private ListView mReadListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_zhiling);

		iniActionBar(true, null, "¡Ÿ ±÷∏¡Ó");

		initViews();
		
		viewPager = (NoScrollViewPager) findViewById(R.id.lv_viewpager);
		
		adapter2 = new PagerAdapter() {
			
			@Override
			public boolean isViewFromObject(View arg0, Object arg1) {
				return arg0 == arg1;
			}
			
			@Override
			public int getCount() {
				return list.size();
			}
			
			@Override
			public void destroyItem(ViewGroup container, int position,
					Object object) {//…æ≥˝“≥ø®
				container.removeView(list.get(position));
			}

			@Override
			public Object instantiateItem(ViewGroup container, int position) {
				View view = list.get(position);//ÃÌº”“≥ø®
				container.addView(view);

				return view;
			}
		};
		
		viewPager.setAdapter(adapter2);

		// dao = new TaskInsDao(this);
		// data = dao.getAllIns();
		//
		// listview = (ListView) findViewById(R.id.listview);
		// adapter = new ZhilingAdapter(this, data);
		// listview.setAdapter(adapter);
		//
		// listview.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view, int
		// position, long id) {
		// Intent intent = new Intent(ZhilingActivity.this, NewTask.class);
		// intent.putExtra("taskInsId", data.get(position).getId());
		// startActivity(intent);
		// }
		// });
	}

	private void initViews() {
		LayoutInflater inflater = LayoutInflater.from(this);

		tab01 = inflater.inflate(R.layout.activity_zhiling_tab01, null);
		tab02 = inflater.inflate(R.layout.activity_zhiling_tab02, null);
		list.add(tab01);
		list.add(tab02);

		mUnRead = (TextView) findViewById(R.id.tv_unread);
		mRead = (TextView) findViewById(R.id.tv_read);

		mUnRead.setOnClickListener(this);
		mRead.setOnClickListener(this);
		
		mUnReadListView = (ListView) tab01.findViewById(R.id.lv_unread);
		mReadListView = (ListView) tab01.findViewById(R.id.lv_read);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_unread:
			viewPager.setCurrentItem(0);
			mUnRead.setTextColor(getResources().getColor(R.color.specialgreen));
			mRead.setTextColor(getResources().getColor(R.color.specialgray));
			break;

		case R.id.tv_read:
			viewPager.setCurrentItem(1);
			mUnRead.setTextColor(getResources().getColor(R.color.specialgray));
			mRead.setTextColor(getResources().getColor(R.color.specialgreen));
			break;
		default:
			break;
		}
	}

	// @Override
	// public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem
	// item) {
	// switch (item.getItemId()) {
	// case android.R.id.home:
	// finish();
	// break;
	// }
	// return super.onOptionsItemSelected(item);
	// }
}
