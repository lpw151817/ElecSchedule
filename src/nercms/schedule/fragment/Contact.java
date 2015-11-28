package nercms.schedule.fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.activity.ChatDetail;
import nercms.schedule.adapter.SimpleTreeListViewAdapter;
import nercms.schedule.adapter.SuperTreeViewAdapter;
import nercms.schedule.adapter.TreeViewAdapter;
import android.R.integer;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.dao.*;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.person.Org;
import android.wxapp.service.jerry.model.person.OrgInfo;
import android.wxapp.service.model.CustomerModel;
import android.wxapp.service.model.OrgNodeModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockFragment;
import com.imooc.treeview.utils.Node;
import com.imooc.treeview.utils.adapter.TreeListViewAdapter.OnTreeNodeClickListener;
import com.imooc.treeview.utils.adapter.TreeListViewAdapter.onTreeNodeLongClickListener;

/**
 * @author jiaocuina@gmail.com
 * @date 2014-04-14
 * @version V1.0
 * @description 04-16�ύ��κ��������
 */

@SuppressLint("HandlerLeak")
public class Contact extends SherlockFragment {

	private static final String TAG = "ContactFragment";

	// �ؼ����
	ListView listView;
	List<Org> data = new ArrayList<Org>();
	Node myself;

	private Button enterpriseBtn, personalBtn;

	// ��ȡ�������
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao dao;
	private GroupDao groupDao;
	// public ArrayList<OrgNodeModel> orgNodeSecondList;
	// public Map<String, Map<String, ArrayList<StructuredStaffModel>>>
	// bigTreeMap;
	private Handler handler;
	private String userID;

	public String[] groups = { "����ͨѶ¼" };

	public static Contact newInstance() {
		Contact contactsFragment = new Contact();
		return contactsFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		userID = MySharedPreference.get(getActivity(), MySharedPreference.USER_ID, null);
		View view = inflater.inflate(R.layout.contact_fragment, null);
		dao = new PersonDao(getActivity().getApplicationContext());
		groupDao = new GroupDao(getActivity().getApplicationContext());
		personalBtn = (Button) view.findViewById(R.id.btn_contacts_personal);
		enterpriseBtn = (Button) view.findViewById(R.id.btn_contacts_company);
		listView = (ListView) view.findViewById(R.id.id_tree);
		// expandableListView = (ExpandableListView)
		// view.findViewById(R.id.expandablelistview);
		personalBtn.setOnClickListener(listener);
		enterpriseBtn.setOnClickListener(listener);
		// // Ĭ��ѡ�� ��ҵ
		// enterpriseBtn.performClick();

		initHandler();

		return view;
	}

	/**
	 * @description �󶨼�����
	 */
	public OnClickListener listener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// ���������ϵ��
			if (v == personalBtn) {
				// ��ť����ɫ����
				personalBtn.setBackgroundResource(R.drawable.qb_group_header_righttab_pressed);
				enterpriseBtn.setBackgroundResource(R.drawable.qb_group_header_lefttab_normal);
				initPersonData();
			} else if (v == enterpriseBtn) { // �����ҵ��ϵ��
				personalBtn.setBackgroundResource(R.drawable.qb_group_header_righttab_normal);
				enterpriseBtn.setBackgroundResource(R.drawable.qb_group_header_lefttab_pressed);
				initEnterpriseData();
			}
		}

		private void initPersonData() {
			try {
				data.clear();
				int i = 1;
				for (String item : groups) {
					data.add(new Org(i + "", 0 + "", item));
					i++;
				}
				Cursor cursor = selectPersonalContact(getActivity(),
						android.provider.ContactsContract.Contacts.CONTENT_URI, null);
				Log.d(TAG, "���ر�������ͨѶ¼");
				if (cursor.getCount() == 0) {
					data.add(new Org(i + "", 1 + "", "������"));
					i++;
				} else {
					while (cursor.moveToNext()) {
						// ����ͨѶ¼ ��ϵ��ID
						String _id = cursor
								.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
						// ����ͨѶ¼ ��ϵ������
						String _name_ = cursor.getString(
								cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
						data.add(new Org("p" + _id, 1 + "", _name_));
					}
				}
				SimpleTreeListViewAdapter<Org> adapter = new SimpleTreeListViewAdapter<Org>(
						listView, getActivity().getApplicationContext(), data, 1);
				listView.setAdapter(adapter);
				adapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {

					@Override
					public void onClick(Node node, int position) {
						if (node.isLeaf()) {
							String tempId = node.getId();
							String id = tempId.substring(1, tempId.length());
							Intent intent = new Intent(Intent.ACTION_VIEW,
									Uri.parse("content://contacts/people/" + id));
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							getActivity().getApplicationContext().startActivity(intent);
						}
					}
				});
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}

		}
	};

	private void initEnterpriseData() {
		try {
			data.clear();

			data = dao.getOrg2();
			for (Org org : data) {
				if (org.getId().substring(1).equals(userID)) {
					// ��ȡ�Լ����
					myself = new Node(org.getId(), org.getPid(), org.getTitle());
				} else
					continue;
			}
			if (myself == null) {
				Log.e(this.getClass().getName(), "û���ҵ��Լ����");
				return;
			}

			data.addAll(groupDao.queryMyAllGroups2("", userID));

			SimpleTreeListViewAdapter<Org> adapter = new SimpleTreeListViewAdapter<Org>(listView,
					getActivity().getApplicationContext(), data, 1);
			listView.setAdapter(adapter);
			adapter.setOnTreeNodeClickListener(new OnTreeNodeClickListener() {

				@Override
				public void onClick(Node node, int position) {
					if (node.isLeaf()
							&& (node.getId().startsWith("p") || node.getId().startsWith("g"))) {
						if (node.getId().substring(1).equals(userID)) {
							Toast.makeText(getActivity(), "���ܸ��Լ����죡", Toast.LENGTH_LONG).show();
						} else {
							List<Node> temp = new ArrayList<Node>();
							temp.add(node);
							Intent intent = new Intent(Contact.this.getActivity(),
									ChatDetail.class);
							Bundle bundle = new Bundle();
							bundle.putInt("entrance_type", 1);
							bundle.putSerializable("data", (Serializable) temp);
							intent.putExtras(bundle);
							Contact.this.startActivity(intent);
						}
					}
				}
			});

			adapter.setOnTreeNodeLongClickListener(new onTreeNodeLongClickListener() {

				@Override
				public void onLongClick(final Node node, int position) {
					if (myself != null) {
						if (myself.isThisParent(node)) {
							new AlertDialog.Builder(getActivity()).setTitle("�Ƿ�������Ⱥ������?")
									.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog, int which) {
									// ��ת����
									Intent intent = new Intent(getActivity(), ChatDetail.class);
									Bundle bundle = new Bundle();
									// ��־Ϊ��Ϣ
									bundle.putInt("entrance_type", 1);
									List<Node> tempData = new ArrayList<Node>();
									tempData.add(node);
									bundle.putSerializable("data", (Serializable) tempData);
									intent.putExtras(bundle);
									getActivity().startActivity(intent);
								}
							}).setNegativeButton("ȡ��", null).create().show();

						}
					}
				}
			});

		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

	// ���ұ���ͨѶ¼
	public static Cursor selectPersonalContact(Context context, Uri uri, String where) {
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(uri, null, where, null, null);
		return cursor;
	}

	@Override
	public void onResume() {
		super.onResume();
		enterpriseBtn.performClick();
	}

	private void initHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case Constant.CREATE_CUSTOMER_REQUEST_SUCCESS:
					Log.i("Customer", "customer�����");
					initEnterpriseData();
					break;
				case Constant.MODIFY_CUSTOMER_REQUEST_SUCCESS:
					Log.i("Customer", "customer���޸�");
					initEnterpriseData();
					break;
				case Constant.DELETE_CUSTOMER_REQUEST_SUCCESS:
					Log.i("Customer", "customer��ɾ��");
					initEnterpriseData();
					break;
				default:
					break;
				}
			}
		};
		MessageHandlerManager.getInstance().register(handler,
				Constant.CREATE_CUSTOMER_REQUEST_SUCCESS, "Main");
		MessageHandlerManager.getInstance().register(handler,
				Constant.MODIFY_CUSTOMER_REQUEST_SUCCESS, "Main");
		MessageHandlerManager.getInstance().register(handler,
				Constant.DELETE_CUSTOMER_REQUEST_SUCCESS, "Main");
	}

	@Override
	public void onDestroyView() {
		// unregister handler

		super.onDestroyView();
	}

}
