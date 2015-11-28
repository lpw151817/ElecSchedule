package nercms.schedule.adapter;

import java.util.ArrayList;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.activity.ContactDetail;
import nercms.schedule.activity.CustomerDetail;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.util.MySharedPreference;

public class TreeViewAdapter extends BaseExpandableListAdapter {

	public static final int ItemHeight = (int) (Utils.Constant.displayWidth * 0.15f);
	public static final int PaddingLeft = (int) (Utils.Constant.displayWidth * 0.04f);
	private int myPaddingLeft = 0;
	public static final int Text_Size = 16;

	// 全局变量，1为联系人页面|2为联系人选择页面
	private int contactAdapterType = -1;
	private String userID = null;

	static public class TreeNode {
		public Object parent;
		public ArrayList<StructuredStaffModel> childs = new ArrayList<StructuredStaffModel>();
	}

	List<TreeNode> treeNodes = new ArrayList<TreeNode>();
	Context parentContext;

	public TreeViewAdapter(Context context, int myPaddingLeft, int contactAdapterType) {
		parentContext = context;
		this.myPaddingLeft = myPaddingLeft;
		this.contactAdapterType = contactAdapterType;
		userID = MySharedPreference.get(context, MySharedPreference.USER_ID, "");
	}

	public List<TreeNode> getTreeNode() {
		return treeNodes;
	}

	public void updateTreeNode(List<TreeNode> nodes) {
		treeNodes = nodes;
	}

	public void removeAll() {
		treeNodes.clear();
	}

	@Override
	public StructuredStaffModel getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return treeNodes.get(groupPosition).childs.get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}

	static public TextView getTextView(Context context) {
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
				ItemHeight);
		TextView textView = new TextView(context);
		textView.setLayoutParams(lp);
		textView.setTextSize(Text_Size);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
		return textView;
	}

	@SuppressLint("ResourceAsColor")
	@SuppressWarnings("deprecation")
	@Override
	public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
			View convertView, ViewGroup parent) {
		// ***************0521***添加checkbox***********************************************
		if (contactAdapterType == 1) {// 进入联系人展示页面
			TextView textView2 = getTextView(this.parentContext);
			textView2.setText(getChild(groupPosition, childPosition).getName());
			textView2.setPadding(myPaddingLeft + PaddingLeft, 0, 0, 0);
			textView2.setBackgroundDrawable(parentContext.getResources().getDrawable(
					R.drawable.text_contact_list_item));
			textView2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					StructuredStaffModel ssm = getChild(groupPosition, childPosition);
					// **********************个人通讯录 调用系统显示页面*************
					if (ssm.getOrgCode().equals("")) {
						Toast.makeText(parentContext, "没有数据", Toast.LENGTH_SHORT).show();
					} else if (ssm.getOrgCode().equals("P")) {
						Intent it = new Intent(Intent.ACTION_VIEW, Uri
								.parse("content://contacts/people/" + ssm.getContactID().toString()));
						parentContext.startActivity(it);
						Log.d("TreeView", ssm.getContactID().toString());
						Log.d("TreeView", ssm.getName().toString());
					}
					// 客户判断
					else if (ssm.getOrgCode().equals("C")) {

						Intent intent = new Intent(parentContext, CustomerDetail.class);
						intent.putExtra("CUSTOMER_ID", ssm.getContactID());
						Log.i("TreeViewAdapter", ssm.getContactID());
						parentContext.startActivity(intent);
					} else { // orgCode！=null,该点击为企业联系人
						Intent intent = new Intent(parentContext, ContactDetail.class);
						if (ssm.getContactID().contains("Group")) {
							intent.putExtra("IS_GROUP", 1);
							intent.putExtra("CONTACT_ID", ssm.getOrgCode()); // 此处传机构节点代码
						} else {
							intent.putExtra("IS_GROUP", 0);
							intent.putExtra("CONTACT_ID", ssm.getContactID()); // 此处传联系人ID
						}
						parentContext.startActivity(intent);
					}
				}
			});
			return textView2;

		} else {// 联系人选择页面
			RelativeLayout layout = (RelativeLayout) LayoutInflater.from(parentContext).inflate(
					R.layout.contact_select_item, null);
			layout.setMinimumHeight(ItemHeight);
			TextView textView = (TextView) layout.findViewById(R.id.childtext);
			textView.setText(getChild(groupPosition, childPosition).getName());
			textView.setPadding(myPaddingLeft + PaddingLeft, 0, 0, 0);

			final CheckBox box = (CheckBox) layout.findViewById(R.id.childcheck);

			box.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
				String name, id;

				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if (isChecked) {

						name = getChild(groupPosition, childPosition).getName();
						id = getChild(groupPosition, childPosition).getContactID();
						// 0625 当前登陆用户不可勾选，toast语句提醒 并取消勾选
						if (userID.equalsIgnoreCase(id)) {
							box.setChecked(false);
							Utils.showShortToast(parentContext, "不可选中自己");
							return;
						}

						String[] temp = new String[] { id, name };
						MessageHandlerManager.getInstance().sendMessage(
								LocalConstant.SELECT_CONTACT_CHECKED, temp, "ContactSelect");
					} else {
						name = getChild(groupPosition, childPosition).getName();
						id = getChild(groupPosition, childPosition).getContactID();

						String[] temp = new String[] { id, name };

						MessageHandlerManager.getInstance().sendMessage(
								LocalConstant.SELECT_CONTACT_UNCHECKED, temp, "ContactSelect");

					}
				}

			});
			return layout;
		}
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return treeNodes.get(groupPosition).childs.size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return treeNodes.get(groupPosition).parent;
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return treeNodes.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		TextView textView = getTextView(this.parentContext);
		textView.setText(getGroup(groupPosition).toString());
		textView.setPadding(10 + myPaddingLeft + (PaddingLeft >> 1), 0, 0, 0);
		return textView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return true;
	}

}
