package nercms.schedule.adapter;

import java.util.ArrayList;
import java.util.List;

import nercms.schedule.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.dao.Org;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.imooc.treeview.utils.Node;
import com.imooc.treeview.utils.adapter.TreeListViewAdapter;

public class PersonSelectAdapter<T> extends TreeListViewAdapter<T> {
	final int MAX_SELECT = 1;
	List<Node> selected = new ArrayList<Node>();

	public interface DataChanged {
		public void onChanged(int size);
	}

	DataChanged changed;

	public void setDataChangedListener(DataChanged listener) {
		this.changed = listener;
	}

	public List<Node> getSelectedDate() {
		return this.selected;
	}

	// public PersonSelectAdapter(ListView tree, Context context, List<T> datas,
	// int defaultExpandLevel) throws IllegalArgumentException,
	// IllegalAccessException {
	// super(tree, context, datas, defaultExpandLevel);
	// }

	private List<Node> lsSelectedPod;
	private List<Node> lsSelectedReceiver;
	int entranceFlag, type;
	List<T> datas;

	public PersonSelectAdapter(ListView tree, Context context, List<T> datas,
			int defaultExpandLevel, List<Node> lsPod, List<Node> lsReceiver, int entranceFlag,
			int type) throws IllegalArgumentException, IllegalAccessException {
		super(tree, context, datas, defaultExpandLevel);
		this.lsSelectedPod = lsPod;
		this.lsSelectedReceiver = lsReceiver;
		this.entranceFlag = entranceFlag;
		this.type = type;
		this.datas = datas;
	}

	@Override
	public View getConvertView(final Node node, int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		// if (convertView == null) {
		if (node.getId().startsWith("p")) {
			convertView = mInflater.inflate(R.layout.list_item_select, parent, false);
			holder = new ViewHolder();
			holder.mIcon = (ImageView) convertView.findViewById(R.id.id_treenode_icon);
			holder.mPic = (ImageView) convertView.findViewById(R.id.id_treednode_pic);
			holder.mPic.setImageResource(R.drawable.orgperson);
			holder.mText = (TextView) convertView.findViewById(R.id.id_treenode_label);
			holder.mCb = (CheckBox) convertView.findViewById(R.id.isselect);
			if (selected.contains(node))
				holder.mCb.setChecked(true);
			else
				holder.mCb.setChecked(false);

			// 每日计划录入
			if (entranceFlag == 1) {
				// 工作负责人
				if (type == 1) {
					if (lsSelectedPod != null && lsSelectedPod.contains(node)) {
						holder.mCb.setChecked(true);
						if (!selected.contains(node))
							selected.add(node);
					}
					if (lsSelectedReceiver != null && lsSelectedReceiver.contains(node)) {
						holder.mCb.setEnabled(false);
					}
				}
				// 应到位领导
				else if (type == 2) {
					if (lsSelectedPod != null && lsSelectedPod.contains(node)) {
						holder.mCb.setEnabled(false);
					}
					if (lsSelectedReceiver != null && lsSelectedReceiver.contains(node)) {
						holder.mCb.setChecked(true);
						if (!selected.contains(node))
							selected.add(node);
					}
				}
			}
			// 指令
			else if (entranceFlag == 2) {
				if (lsSelectedPod != null && lsSelectedPod.contains(node)) {
					holder.mCb.setChecked(true);
					if (!selected.contains(node))
						selected.add(node);
				}
			}

			holder.mCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {
						if (selected.size() == MAX_SELECT) {
							Toast.makeText(AppApplication.getInstance().getApplicationContext(),
									"最多只能选择" + MAX_SELECT + "个", Toast.LENGTH_SHORT).show();
							buttonView.setChecked(false);
						} else
							selected.add(node);
					} else
						selected.remove(node);
					if (changed != null)
						changed.onChanged(selected.size());
				}
			});
		} else {

			convertView = mInflater.inflate(R.layout.list_item, parent, false);
			holder = new ViewHolder();
			holder.mIcon = (ImageView) convertView.findViewById(R.id.id_treenode_icon);
			holder.mText = (TextView) convertView.findViewById(R.id.id_treenode_label);
			holder.mPic = (ImageView) convertView.findViewById(R.id.id_treednode_pic);
			holder.mPic.setImageResource(R.drawable.org);

			// // 如果组织结点下没有人员，则隐藏
			// if (node.isLeaf()) {
			// datas.remove(new Org(node.getId(), "", ""));
			// notifyDataSetChanged();
			// }
		}
		convertView.setTag(holder);
		// } else {
		// holder = (ViewHolder) convertView.getTag();
		// }

		if (node.getIcon() == -1) {
			holder.mIcon.setVisibility(View.INVISIBLE);
		} else {
			holder.mIcon.setVisibility(View.VISIBLE);
			holder.mIcon.setImageResource(node.getIcon());
		}

		holder.mText.setText(node.getName());

		return convertView;
	}

	private class ViewHolder {
		ImageView mIcon;
		ImageView mPic;
		TextView mText;
		CheckBox mCb;
	}
}
