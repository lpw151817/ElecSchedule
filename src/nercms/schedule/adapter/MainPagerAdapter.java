package nercms.schedule.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import nercms.schedule.R;

public class MainPagerAdapter extends BaseAdapter {
	Context c;
	String[] data = { "作业现场", "操作现场", "故障紧急抢修现场" };

	public MainPagerAdapter(Context c) {
		this.c = c;
	}

	@Override
	public int getCount() {
		return data.length;
	}

	@Override
	public Object getItem(int position) {
		return data[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(c);
			convertView = inflater.inflate(R.layout.xianchang_add_adapter, null);
			holder = new Holder();
			holder.tv = ((TextView) convertView.findViewById(R.id.textview));
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.tv.setText(data[position]);
		return convertView;
	}

	class Holder {
		TextView tv;
	}
}
