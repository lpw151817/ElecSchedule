package nercms.schedule.activity;

import java.io.File;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.view.HackyViewPager;
import uk.co.senab.photoview.PhotoView;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

public class ViewPagerActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_viewpager);

//		String direc = Environment.getExternalStorageDirectory().getPath()
//				+ "/nercms-Schedule/Thumbnail/";
//		String[] str = { direc + "2016_05_13_093811.jpg",
//				direc + "2016_05_13_110734.jpg",
//				direc + "2016_05_13_110851.jpg",  direc + "2016_05_13_110734.jpg",direc + "2016_05_13_110734.jpg",direc + "2016_05_13_110734.jpg",direc + "2016_05_13_110734.jpg",direc + "2016_05_13_110734.jpg",direc + "2016_05_13_110734.jpg",};
		
		@SuppressWarnings("unchecked")
		List<String> list = (List<String>) getIntent().getSerializableExtra("images");
		int currentItemIndex = getIntent().getIntExtra("currentItemIndex", 0);
		String[] str = new String[list.size()];
		
		
		list.toArray(str);
		Uri[] uri = new Uri[str.length];

		for (int i = 0; i < str.length; i++) {
			uri[i] = Uri.fromFile(new File(str[i]));
		}

		HackyViewPager viewpager = (HackyViewPager) findViewById(R.id.view_pager);
		viewpager.setAdapter(new MyPagerAdapter(uri));
		viewpager.setCurrentItem(currentItemIndex);
	}
}

class MyPagerAdapter extends PagerAdapter {

	private Uri[] images;

	public MyPagerAdapter(Uri[] images) {
		this.images = images;
	}

	@Override
	public int getCount() {
		return images.length;
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0 == arg1;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		PhotoView photoView = new PhotoView(container.getContext());
		photoView.setImageURI(images[position]);
		// Now just add PhotoView to ViewPager and return it
		container.addView(photoView, LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);

		return photoView;
	}

}