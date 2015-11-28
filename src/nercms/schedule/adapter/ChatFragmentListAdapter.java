package nercms.schedule.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.activity.ChatDetail;
import nercms.schedule.utils.Utils;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.MessageDao;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.jerry.model.message.ReceiveMessageResponse;
import android.wxapp.service.model.MessageModel;
import android.wxapp.service.util.MySharedPreference;

public class ChatFragmentListAdapter extends BaseAdapter {

	private DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao personDao;
	private MessageDao msgDao;

	private List<String> rids;
	private List<String> userNames;
	private List<Map<String, String>> lastRecodes;
	private Intent intent;

	private Context context;

	private String userID = null;

	public ChatFragmentListAdapter(Context context) {
		this.context = context;
		// 设置用户ID
		this.userID = MySharedPreference.get(context, MySharedPreference.USER_ID, "");
		this.personDao = daoFactory.getPersonDao(context);
		this.msgDao = daoFactory.getMessageDao(context);
		this.lastRecodes = this.msgDao.getLastMessageRecodes(userID, "0");
		this.rids = new ArrayList<String>();
		this.userNames = new ArrayList<String>();

		for (Map<String, String> item : lastRecodes) {
			if (item.get("uid").equals(userID)) {
				this.rids.add(item.get("rid"));
				this.userNames.add(this.personDao.getPersonInfo(item.get("rid")).getN());
			} else {
				this.rids.add(item.get("uid"));
				this.userNames.add(this.personDao.getPersonInfo(item.get("uid")).getN());
			}
		}
	}

	@Override
	public int getCount() {
		return this.lastRecodes.size();
	}

	@Override
	public Object getItem(int arg0) {
		return this.lastRecodes.get(arg0);
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
			convertView = inflater.inflate(R.layout.chat_fragment_item, null);
			holder = new Holder();
			holder.nameText = (TextView) convertView.findViewById(R.id.message_list_name_tv);
			holder.contentText = (TextView) convertView.findViewById(R.id.message_list_content_tv);
			holder.timeText = (TextView) convertView.findViewById(R.id.message_list_time_tv);
			holder.unreadText = (TextView) convertView.findViewById(R.id.message_list_tips_num_tv);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		// // 2014-7-30 WeiHao
		// // 对象名称
		// String name = "";
		// if (msg.getIsGroup() == 1) { // 判断为群消息
		// objectID = Integer.toString(msg.getReceiverID());
		// name = personDao.getOrgNodeByOrgID(objectID).getDescription(); // 群组名
		// } else { // 判断为个人消息
		//
		// if (userID.equals(String.valueOf(msg.getSenderID()))) { // 本人为最新消息发送者
		// objectID = Integer.toString(msg.getReceiverID());
		// } else { // 本人为最新消息接收者
		// objectID = Integer.toString(msg.getSenderID());
		// }
		// name = personDao.getSSMByID(objectID).getName();
		// }
		//
		// final String tempName = name;

		holder.nameText.setText(this.userNames.get(position));
		// 时间
		holder.timeText.setText(Utils.formatDateMs(lastRecodes.get(position).get("time")));
		// 最新一条消息
		holder.contentText.setText(lastRecodes.get(position).get("recode"));

		// 未读消息数
		int unreadNum = msgDao.getUnreadNumByIDs(userID, rids.get(position), "0");
		if (unreadNum != 0) {
			holder.unreadText.setVisibility(View.VISIBLE);
			holder.unreadText.setText(String.valueOf(unreadNum));
		} else {
			holder.unreadText.setVisibility(View.GONE);
		}

		// 设置最近消息点击事件，跳转到消息对话界面
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Map<String, String> tempData = lastRecodes.get(position);
				Intent intent = new Intent(context.getApplicationContext(), ChatDetail.class);
				Bundle bundle = new Bundle();
				bundle.putInt("entrance_type", 1);
				if (tempData.get("uid").equals(userID)) {
					bundle.putInt("selected_id", Integer.parseInt(tempData.get("rid")));
					bundle.putString("selected_name", personDao.getPersonInfo(tempData.get("rid"))
							.getN());
				} else if (tempData.get("rid").equals(userID)) {
					bundle.putInt("selected_id", Integer.parseInt(tempData.get("uid")));
					bundle.putString("selected_name", personDao.getPersonInfo(tempData.get("uid"))
							.getN());
				}
				intent.putExtras(bundle);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.getApplicationContext().startActivity(intent);
			}
		});

		// 设置长按事件，弹出删除对话框
		convertView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View arg0) {

				// new AlertDialog.Builder(context).setTitle("删除")
				// .setMessage("确定删除与 " + tempName + " 的消息记录？")
				// .setPositiveButton("删除", new
				// DialogInterface.OnClickListener() {
				//
				// @Override
				// public void onClick(DialogInterface arg0, int arg1) {
				// // 删除与此人的消息记录，刷新列表
				// msgDao.deleteMessages(userID, objectID);
				// msgList.remove(position);
				// ChatFragmentListAdapter.this.notifyDataSetChanged();
				// }
				// }).setNegativeButton("取消", null).create().show();

				return true;
			}
		});

		return convertView;
	}

	class Holder {
		TextView nameText, contentText, timeText, unreadText;
	}
}
