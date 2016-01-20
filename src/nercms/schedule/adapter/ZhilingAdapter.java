package nercms.schedule.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions;
import nercms.schedule.R;
import nercms.schedule.adapter.XianchangAdapter.Holder;
import nercms.schedule.utils.Utils;

public class ZhilingAdapter extends BaseAdapter {
	List<tb_task_instructions> data;
	Context c;

	OrgDao orgDao;

	public ZhilingAdapter(Context c, List<tb_task_instructions> data) {
		this.data = data;
		this.c = c;
		orgDao = new OrgDao(c);
	}

	@Override
	public int getCount() {
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
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
			convertView = inflater.inflate(R.layout.zhiling_item, null);
			holder = new Holder();
			holder.isRead = (CheckBox) convertView.findViewById(R.id.isRead);
			holder.name = ((TextView) convertView.findViewById(R.id.name));
			holder.time = ((TextView) convertView.findViewById(R.id.time));
			holder.content = (TextView) convertView.findViewById(R.id.content);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.name.setText(orgDao.getPerson(data.get(position).getSend_id()).getName());
		holder.time.setText(Utils.formatDateMs(data.get(position).getSend_time()));
		holder.content.setText(data.get(position).getContent());
		return convertView;
	}

	class Holder {
		CheckBox isRead;
		TextView name, time, content;
	}
}
