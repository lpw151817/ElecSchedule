package nercms.schedule.adapter;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.model.DeleteTaskResponse;
import android.wxapp.service.elec.model.bean.table.tb_task_info;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import nercms.schedule.R;
import nercms.schedule.utils.Utils;

public class MeiRiJiHuaAdapter extends BaseAdapter {
	Context mContext;
	List<tb_task_info> data;
	PlanTaskDao dao;
	OnItemDeleteButtonClick listener;

	public List<tb_task_info> getData() {
		return data;
	}

	public MeiRiJiHuaAdapter(Context c, List<tb_task_info> data, PlanTaskDao dao,
			OnItemDeleteButtonClick listener) {
		this.mContext = c;
		this.data = data;
		this.dao = dao;
		this.listener = listener;
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.meirijihua_adapter, null);
			holder = new Holder();
			holder.name = ((TextView) convertView.findViewById(R.id.textview));
			holder.time = ((TextView) convertView.findViewById(R.id.time));
			holder.delete = (ImageView) convertView.findViewById(R.id.delete);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.name.setText(data.get(position).getName());
		holder.time.setText(Utils.formatDateMs(data.get(position).getPlan_start_time()));
		holder.delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showAlterDialog("提示", "确认删除吗？", R.drawable.login_error_icon, "确认",
						new android.content.DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						listener.onClick(position);
					}
				}, "取消", null);
			}
		});

		return convertView;
	}

	class Holder {
		TextView name, time;
		ImageView delete;
	}

	protected void showAlterDialog(String title, String content, Integer icon, String pB,
			android.content.DialogInterface.OnClickListener pbListener, String nB,
			android.content.DialogInterface.OnClickListener nbListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

		builder.setTitle(title);
		if (content != null)
			builder.setMessage(content);
		if (icon != null)
			builder.setIcon(icon);
		if (pB != null)
			builder.setPositiveButton(pB, pbListener);
		if (nB != null)
			builder.setNegativeButton(nB, nbListener);
		builder.create().show();
	}

	public interface OnItemDeleteButtonClick {
		public void onClick(int position);
	}
}
