package nercms.schedule.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import nercms.schedule.R;

public class XianChangAddAdapter extends BaseAdapter {
	Context mContext;
	String[] ss;
	int[] hasContent;

	public XianChangAddAdapter(Context c, int type, int[] hasContent) {
		this.mContext = c;
		this.hasContent = hasContent;
		if (type == 1) {
			ss = c.getResources().getStringArray(R.array.zuoyexianchang_si_data);
		} else if (type == 2) {
			ss = c.getResources().getStringArray(R.array.caozuoxianchang_si_data);
		} else if (type == 3) {
			ss = c.getResources().getStringArray(R.array.guzhangjinji_si_data);
		}
	}

	@Override
	public int getCount() {
		return ss.length;
	}

	@Override
	public Object getItem(int position) {
		return ss[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.xianchang_add_adapter, null);
			holder = new Holder();
			holder.textView = ((TextView) convertView.findViewById(R.id.textview));
			holder.video = ((ImageButton) convertView.findViewById(R.id.video));
			holder.photo = (ImageButton) convertView.findViewById(R.id.photo);
			holder.radio = (ImageButton) convertView.findViewById(R.id.radio);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.textView.setText(ss[position]);
		if (hasContent[position] == 1)
			holder.textView.setTextColor(Color.RED);
		return convertView;
	}

	class Holder {
		TextView textView;
		ImageButton video, radio, photo;
	}

}
