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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.imooc.treeview.utils.Node;
import com.imooc.treeview.utils.adapter.TreeListViewAdapter;

public class OrgSelectAdapter<T> extends TreeListViewAdapter<T> {

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

	private Node lsSelectedPod;
	Context c;

	public OrgSelectAdapter(ListView tree, Context context, List<T> datas, int defaultExpandLevel,
			Node lsPod) throws IllegalArgumentException, IllegalAccessException {
		super(tree, context, datas, defaultExpandLevel);
		this.lsSelectedPod = lsPod;
		this.c = context;
	}

	private boolean hasSelect = false;

	@Override
	public View getConvertView(final Node node, int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		convertView = mInflater.inflate(R.layout.list_item_select, parent, false);
		// convertView = mInflater.inflate(R.layout.list_item, parent, false);
		holder = new ViewHolder();
		holder.mIcon = (ImageView) convertView.findViewById(R.id.id_treenode_icon);
		holder.mText = (TextView) convertView.findViewById(R.id.id_treenode_label);
		holder.mPic = (ImageView) convertView.findViewById(R.id.id_treednode_pic);
		holder.mCb = (CheckBox) convertView.findViewById(R.id.isselect);
		holder.mPic.setImageResource(R.drawable.org);
		convertView.setTag(holder);

		holder.mCb.setFocusable(false);

		if (lsSelectedPod != null && lsSelectedPod.equals(node)) {
			holder.mCb.setChecked(true);
			if (!selected.contains(node))
				selected.add(node);
		}

		if (node.getIcon() == -1) {
			holder.mIcon.setVisibility(View.INVISIBLE);
		} else {
			holder.mIcon.setVisibility(View.VISIBLE);
			holder.mIcon.setImageResource(node.getIcon());
		}

		holder.mCb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (selected.size() == 1) {
					if (isChecked) {
						Toast.makeText(c, "只能选择一个", Toast.LENGTH_LONG).show();
						buttonView.setChecked(false);
					} else {
						selected.remove(node);
						if (changed != null)
							changed.onChanged(selected.size());
					}

				} else {
					if (isChecked)
						selected.add(node);
					else
						selected.remove(node);

					if (changed != null)
						changed.onChanged(selected.size());
				}
			}
		});

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
