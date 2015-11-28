package nercms.schedule.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.activity.TaskDetail;
import nercms.schedule.utils.Utils;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.wxapp.service.dao.AffairDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.dao.PersonOnDutyDao;
import android.wxapp.service.jerry.model.affair.CreateTaskRequestIds;
import android.wxapp.service.jerry.model.affair.QueryAffairListResponseAffairs;
import android.wxapp.service.jerry.model.person.GetPersonInfoResponse;
import android.wxapp.service.model.AffairModel;
import android.wxapp.service.model.PersonOnDutyModel;
import android.wxapp.service.model.StructuredStaffModel;

/**
 * 6.10 DONE
 * 
 * @author JerryLiu
 *
 */
public class TaskListViewAdapter extends BaseAdapter {

	private DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao;
	private PersonOnDutyDao podDao;
	private AffairDao affairDao;

	private Intent intent;

	private int entranceType;
	private int entraceStatus;

	private List<Map<String, Object>> mList;// 任务列表
	// private List<QueryAffairListResponseAffairs> mList; // 任务列表
	private Context mContext;

	public TaskListViewAdapter(List<Map<String, Object>> list, Context context, int entranceType,
			int entranceStatus) {
		this.mList = list;
		this.mContext = context;
		podDao = daoFactory.getPersonOnDutyDao(mContext);
		personDao = daoFactory.getPersonDao(mContext);
		affairDao = daoFactory.getAffairDao(mContext);
		this.entranceType = entranceType;
		this.entraceStatus = entranceStatus;
	}

	public void refresh(List<Map<String, Object>> list) {
		mList = list;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return mList.get(arg0);
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
			convertView = inflater.inflate(R.layout.task_list_item, null);
			holder = new Holder();
			holder.mTitleText = ((TextView) convertView.findViewById(R.id.itemlist_title));
			holder.mContentText = ((TextView) convertView.findViewById(R.id.itemlist_content));
			holder.mParticipatorText = ((TextView) convertView.findViewById(R.id.itemlist_participator));
			holder.mDeadlineText = ((TextView) convertView.findViewById(R.id.itemlist_deadline));
			// holder.mReplyText = ((TextView) convertView
			// .findViewById(R.id.itemlist_reply));
			holder.mNewTips = (ImageView) convertView.findViewById(R.id.itemlist_new_tips);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		final QueryAffairListResponseAffairs tempData = (QueryAffairListResponseAffairs) mList.get(
				position).get("data");
		holder.mTitleText.setText(tempData.getTopic());
		holder.mContentText.setText(tempData.getD());
		// 获取负责人的名字
		String podsString = "";
		for (CreateTaskRequestIds item : tempData.getPod()) {
			GetPersonInfoResponse temp = personDao.getPersonInfo(item.getRid());
			if (temp != null)
				podsString += (temp.getN() + "/");
			else
				continue;
		}
		holder.mParticipatorText.setText(podsString);
		holder.mDeadlineText.setText(Utils.formatDateMs(tempData.getEt()));
		// holder.mReplyText.setText(Integer.toString(1));// 回复条数数据如何取

		if (!affairDao.getAffairIsReadByID(tempData.getAid())) {
			holder.mNewTips.setVisibility(View.VISIBLE);
		} else {
			holder.mNewTips.setVisibility(View.GONE);
		}

		// 设置任务点击事件
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				// 标记未已读
				affairDao.updateAffairIsRead(tempData.getAid());

				intent = new Intent(mContext, TaskDetail.class);
				intent.putExtra("id", tempData.getAid());
				intent.putExtra("type", entranceType);
				intent.putExtra("status", entraceStatus);
				mContext.startActivity(intent);
			}
		});

		return convertView;
	}

	class Holder {
		TextView mTitleText, mContentText, mParticipatorText, mDeadlineText, mReplyText;
		ImageView mNewTips;
	}
}
