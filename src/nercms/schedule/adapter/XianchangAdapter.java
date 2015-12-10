package nercms.schedule.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.wxapp.service.jerry.model.affair.CreateTaskRequestIds;
import android.wxapp.service.jerry.model.affair.QueryAffairListResponseAffairs;
import android.wxapp.service.jerry.model.person.GetPersonInfoResponse;
import nercms.schedule.R;
import nercms.schedule.activity.TaskDetail;
import nercms.schedule.adapter.TaskListViewAdapter.Holder;
import nercms.schedule.utils.Utils;

public class XianchangAdapter extends BaseAdapter {

	List<Bean> data;
	Context mContext;

	public XianchangAdapter(Context c, int type) {
		this.mContext = c;
		this.data = new ArrayList<XianchangAdapter.Bean>();
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
			convertView = inflater.inflate(R.layout.xianchang_adapter, null);
			holder = new Holder();
			holder.taskName = ((TextView) convertView.findViewById(R.id.task_name));
			holder.time = ((TextView) convertView.findViewById(R.id.task_time));
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.taskName.setText(data.get(position).getTn());
		holder.time.setText(data.get(position).getT());

		return convertView;
	}

	class Holder {
		TextView taskName, time;
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
