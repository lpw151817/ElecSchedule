package nercms.schedule.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import nercms.schedule.R;

public class XianChangSiAdapter extends BaseAdapter {
	Context mContext;
	String[] ss;
	int[] isRadio;
	int[] isPhoto;
	int[] isVideo;
	int type;

	public XianChangSiAdapter(Context c, int type) {
		this.mContext = c;
		this.type = type;
		if (type == 1) {
			ss = c.getResources().getStringArray(R.array.zuoyexianchang_si_data);
			isRadio = c.getResources().getIntArray(R.array.zuoyexianchang_si_radio);
			isPhoto = c.getResources().getIntArray(R.array.zuoyexianchang_si_photo);
			isVideo = c.getResources().getIntArray(R.array.zuoyexianchang_si_video);
		} else if (type == 2) {
			ss = c.getResources().getStringArray(R.array.caozuoxianchang_si_data);
			isRadio = c.getResources().getIntArray(R.array.caozuoxianchang_si_radio);
			isPhoto = c.getResources().getIntArray(R.array.caozuoxianchang_si_photo);
			isVideo = c.getResources().getIntArray(R.array.caozuoxianchang_si_video);
		} else if (type == 3) {
			ss = c.getResources().getStringArray(R.array.guzhangjinji_si_data);
			isRadio = c.getResources().getIntArray(R.array.guzhangjinji_si_radio);
			isPhoto = c.getResources().getIntArray(R.array.guzhangjinji_si_photo);
			isVideo = c.getResources().getIntArray(R.array.guzhangjinji_si_video);
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
			convertView = inflater.inflate(R.layout.xianchang_si_adapter, null);
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
		if (isPhoto[position] == 1)
			holder.photo.setVisibility(View.VISIBLE);
		else
			holder.photo.setVisibility(View.GONE);
		if (isRadio[position] == 1)
			holder.radio.setVisibility(View.VISIBLE);
		else
			holder.radio.setVisibility(View.GONE);
		if (isVideo[position] == 1)
			holder.video.setVisibility(View.VISIBLE);
		else
			holder.video.setVisibility(View.GONE);

		holder.photo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});
		holder.radio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

			}
		});

		return convertView;
	}

	class Holder {
		TextView textView;
		ImageButton video, radio, photo;
	}

}
