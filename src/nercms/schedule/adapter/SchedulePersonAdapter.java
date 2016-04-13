package nercms.schedule.adapter;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.elec.dao.Org;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.jerry.model.person.OrgPersonInfo;
import android.wxapp.service.util.MySharedPreference;
import nercms.schedule.R;
import nercms.schedule.fragment.Task;

public class SchedulePersonAdapter extends BaseAdapter {
	Context c;
	List<Org> data;
	boolean isSelected = false;
	String selectedVideo;
	List<Org> selectedPeople = new ArrayList<Org>();
	OrgDao dao;
	private final int MAX_USER = 2;

	public List<Org> getSelectedPeople() {
		return selectedPeople;
	}

	public String getSelectedVideo() {
		return selectedVideo;
	}

	public SchedulePersonAdapter(Context c, List<Org> data) {
		this.c = c;
		this.data = data;
		this.dao = new OrgDao(c);
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
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(c);
			convertView = inflater.inflate(R.layout.schedule_person_item, null);
			holder = new Holder();
			holder.name = ((TextView) convertView.findViewById(R.id.name));
			holder.ischecked = (CheckBox) convertView.findViewById(R.id.ischecked);
			// holder.mark = ((TextView) convertView.findViewById(R.id.mark));
			// holder.audio = ((CheckBox) convertView.findViewById(R.id.audio));
			holder.video = ((CheckBox) convertView.findViewById(R.id.video));
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.video.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (selectedPeople.contains(new Org(data.get(position).getId(), "", ""))) {
					if (!isChecked) {
						selectedVideo = null;
						isSelected = false;
					} else if (isChecked && !isSelected) {
						selectedVideo = data.get(position).getId();
						isSelected = true;
					} else {
						Toast.makeText(c, "只能选一个视频源", Toast.LENGTH_SHORT).show();
						buttonView.setChecked(false);
					}
				} else {
					if (isChecked) {
						Toast.makeText(c, "请选择已勾选的人作为视频源", Toast.LENGTH_SHORT).show();
						buttonView.setChecked(false);
					} else {
						selectedVideo = null;
						isSelected = false;
					}
				}
			}
		});

		holder.ischecked.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (!selectedPeople.contains(data.get(position))) {
					if (selectedPeople.size() < MAX_USER)
						selectedPeople.add(data.get(position));
					else {
						Toast.makeText(c, "只能选择" + MAX_USER + "个人", Toast.LENGTH_SHORT).show();
						buttonView.setChecked(false);
					}
				} else {
					selectedPeople.remove(data.get(position));
				}

			}
		});

		// holder.audio.setOnCheckedChangeListener(new OnCheckedChangeListener()
		// {
		// @Override
		// public void onCheckedChanged(CompoundButton buttonView, boolean
		// isChecked) {
		// // TODO Auto-generated method stub
		//
		// }
		// });

		// 标记本人
		if (data.get(position).equals(new Org("p" + getUserId(), "", ""))) {
			holder.ischecked.setChecked(true);
			holder.ischecked.setEnabled(false);
			if (!selectedPeople.contains(data.get(position)))
				selectedPeople.add(data.get(position));
		} else {
			holder.ischecked.setChecked(false);
			holder.ischecked.setEnabled(true);
			if (selectedPeople.contains(data.get(position)))
				selectedPeople.remove(data.get(position));
		}

		// 如果自己是现场负责人，则屏蔽掉领导的视频源按钮
		if (!isAdmin(getUserId()) && isAdmin(data.get(position).getId().substring(1)))
			holder.video.setVisibility(View.GONE);
		else
			holder.video.setVisibility(View.VISIBLE);

		holder.name.setText(data.get(position).getTitle());

		return convertView;
	}

	class Holder {
		CheckBox ischecked;
		TextView name, mark;
		CheckBox video, audio;
	}

	protected String getUserId() {
		return MySharedPreference.get(c, MySharedPreference.USER_ID, null);
	}

	protected boolean isAdmin(String pid) {
		try {
			if (dao.getPerson(pid).getType() != null) {
				return dao.getPerson(pid).getType().equals("1");
			} else {
				return dao.getPerson(pid).getName().contains("管理员")
						|| dao.getPerson(pid).getName().contains("领导");
			}
		} catch (Exception e) {
			return false;
		}
	}
}
