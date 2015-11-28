package nercms.schedule.adapter;

import java.util.ArrayList;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.activity.MeetingDetail;
import nercms.schedule.utils.Utils;
import android.R.integer;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.wxapp.service.dao.ConferenceDao;
import android.wxapp.service.jerry.model.conference.ConferenceUpdateQueryResponseItem;

public class MeetingListViewAdapter extends BaseAdapter {

	private ConferenceDao conDao;

	private Intent intent;
	private Context context;
	private List<ConferenceUpdateQueryResponseItem> conList;

	public MeetingListViewAdapter(Context context, String uid) {
		this.context = context;
		this.conDao = new ConferenceDao(context);
		this.conList = conDao.getConferenceListByPid(uid);
	}

	@Override
	public int getCount() {
		return conList.size();
	}

	@Override
	public Object getItem(int arg0) {
		return conList.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(context);
			convertView = inflater.inflate(R.layout.metting_fragment_item, null);
			holder = new Holder();
			holder.iconIv = (ImageView) convertView.findViewById(R.id.meeting_item_head);
			holder.timeTv = (TextView) convertView.findViewById(R.id.meeting_item_time);
			holder.statusTv = (TextView) convertView.findViewById(R.id.meeting_item_status);
			holder.titleTv = (TextView) convertView.findViewById(R.id.meeting_item_title);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.timeTv.setText(Utils.formatDateMs(conList.get(position).getCt()));
		if (Long.parseLong(conList.get(position).getCt()) > System.currentTimeMillis()) { // ԤԼ��
			holder.iconIv.setImageResource(R.drawable.meeting_clock);
			holder.statusTv.setText("ԤԼ��");
		} else {// �ѽ���
			holder.iconIv.setImageResource(R.drawable.meeting_over);
			holder.statusTv.setText("�ѽ���");
		}

		holder.titleTv.setText(conList.get(position).getN());

		// �����ת�������������
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				intent = new Intent(context, MeetingDetail.class);
				intent.putExtra("conference_id", conList.get(position).getCid());
				context.startActivity(intent);
			}
		});

//		// ��������ȷ��ɾ��
//		convertView.setOnLongClickListener(new OnLongClickListener() {
//
//			@Override
//			public boolean onLongClick(View arg0) {
//				// ..
//				String messageString = "ȷ��ɾ����ԤԼ���飿";
//				if (Long.parseLong(conList.get(position).getCt()) > System.currentTimeMillis()) { // ԤԼ��
//					messageString += "\nɾ���󣬸�ԤԼҲ���ᱻȡ����";
//				}
//				// else if (con.getStatus() == 2) { // �������
//				// messageString = "ȷ��ɾ���˻����¼��";
//				// }
//				new AlertDialog.Builder(context).setTitle("ɾ��").setMessage(messageString)
//						.setPositiveButton("ɾ��", new DialogInterface.OnClickListener() {
//
//							@Override
//							public void onClick(DialogInterface arg0, int arg1) {
//								// ..
//								conDao.deleteConferenceByID(conList.get(position).getCid());
//								conList.remove(position);
//								MeetingListViewAdapter.this.notifyDataSetChanged();
//
//							}
//						}).setNegativeButton("ȡ��", null).create().show();
//				return false;
//			}
//		});

		return convertView;
	}

	class Holder {
		ImageView iconIv;
		TextView timeTv, statusTv, titleTv;
	}

}
