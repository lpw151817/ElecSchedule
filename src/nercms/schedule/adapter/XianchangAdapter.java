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
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.model.bean.table.tb_task_info;
import android.wxapp.service.jerry.model.affair.CreateTaskRequestIds;
import android.wxapp.service.jerry.model.affair.QueryAffairListResponseAffairs;
import android.wxapp.service.jerry.model.person.GetPersonInfoResponse;
import nercms.schedule.R;
import nercms.schedule.activity.TaskDetail;
import nercms.schedule.utils.Utils;

public class XianchangAdapter extends BaseAdapter {

	List<tb_task_info> data;
	Context mContext;
	int renwuleibie;
	OrgDao dao;

	public void updateData(List<tb_task_info> data) {
		this.data.clear();
		this.data.addAll(data);
		notifyDataSetChanged();
	}

	public XianchangAdapter(Context c, int renwuleibie, List<tb_task_info> data) {
		this.mContext = c;
		this.renwuleibie = renwuleibie;
		this.data = data;
		this.dao = new OrgDao(c);
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
			holder.xcfzr = (TextView) convertView.findViewById(R.id.xcfzr);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.taskName.setText(data.get(position).getName());
//		holder.time.setText(Utils.formatDateMs(data.get(position).getCreator_time()));
		holder.time.setText(Utils.formatDateMs(data.get(position).getPlan_start_time()));
		try {
			holder.xcfzr
					.setText(dao.getPerson(data.get(position).getResponsibility_user()).getName());
		} catch (Exception e) {
			holder.xcfzr.setText("Пе");
		}

		return convertView;
	}

	class Holder {
		TextView taskName, time, xcfzr;
	}

}
