package nercms.schedule.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import nercms.schedule.R;

public class MeiRiJiHuaAdapter extends BaseAdapter {
	List<Bean> data;
	Context mContext;

	public MeiRiJiHuaAdapter(Context c) {
		this.mContext = c;
		this.data = new ArrayList<Bean>();
		for (int i = 0; i < 5; i++) {
			data.add(new Bean(i + "", i + ""));
		}

	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return this.data.get(position);
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
			convertView = inflater.inflate(R.layout.meirijihua_adapter, null);
			holder = new Holder();
			holder.name = ((TextView) convertView.findViewById(R.id.textview));
			holder.time = ((TextView) convertView.findViewById(R.id.time));
			holder.delete = (ImageButton) convertView.findViewById(R.id.delete);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.name.setText(data.get(position).getTn());
		holder.time.setText(data.get(position).getT());

		return convertView;
	}

	class Holder {
		TextView name, time;
		ImageButton delete;
	}

	class Bean {
		String tn, t;

		public String getTn() {
			return tn;
		}

		public void setTn(String tn) {
			this.tn = tn;
		}

		public String getT() {
			return t;
		}

		public void setT(String t) {
			this.t = t;
		}

		public Bean(String tn, String t) {
			super();
			this.tn = tn;
			this.t = t;
		}

	}
}
