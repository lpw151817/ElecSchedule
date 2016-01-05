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
	


	/**
	 * 需要修改是否有内容的标志位
	 * 
	 * @param position
	 *            修改第几项，从0开始
	 * @param hasContent
	 *            1表示有内容，0表示无内容
	 */
	public void setContent(int position, int hasContent) {
		if (position > -1 && position < this.hasContent.length) {
			this.hasContent[position] = hasContent;
			this.notifyDataSetChanged();
		}
	}

	/**
	 * 
	 * @param c
	 * @param type
	 *            1：作业现场，2：操作现场，3：故障紧急
	 * @param hasContent
	 *            标记每一项是否有内容，1：有，0：没有
	 */
	public XianChangAddAdapter(Context c, int type) {
		this.mContext = c;
		if (type == 1) {
			ss = c.getResources().getStringArray(R.array.zuoyexianchang_si_data);
		} else if (type == 2) {
			ss = c.getResources().getStringArray(R.array.caozuoxianchang_si_data);
		} else if (type == 3) {
			ss = c.getResources().getStringArray(R.array.guzhangjinji_si_data);
		}
		this.hasContent = new int[ss.length];
		// 初始化
		for (int i = 0; i < hasContent.length; i++)
			this.hasContent[i] = 0;
	}
	
	public XianChangAddAdapter(){
		
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
		if (hasContent[position] == 1){
			holder.textView.setTextColor(Color.RED);
		} else {
			holder.textView.setTextColor(Color.WHITE);
		}
		return convertView;
	}

	class Holder {
		TextView textView;
		ImageButton video, radio, photo;
	}
	


}
