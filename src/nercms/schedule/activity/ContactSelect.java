package nercms.schedule.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.adapter.PersonSelectAdapter;
import nercms.schedule.adapter.PersonSelectAdapter.DataChanged;
import nercms.schedule.adapter.SuperTreeViewAdapter;
import nercms.schedule.adapter.TreeViewAdapter;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ListView;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.GroupDao;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.group.CreateGroupResponse;
import android.wxapp.service.jerry.model.group.GroupUpdateQueryRequestIds;
import android.wxapp.service.jerry.model.person.Org;
import android.wxapp.service.model.OrgNodeModel;
import android.wxapp.service.model.StructuredStaffModel;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.imooc.treeview.utils.Node;

public class ContactSelect extends BaseActivity implements DataChanged {

	private static final String TAG = "ContactSelect";

	// ExpandableListView expandableListView1;
	// TreeViewAdapter adapter;
	// SuperTreeViewAdapter superAdapter;
	// private int check_count = 0;

	ListView listView;
	PersonSelectAdapter<Org> adapter;

	List<Node> selectedPerson;
	private List<Node> lsSelectedPod = new ArrayList<Node>();
	private List<Node> lsSelectedReceiver = new ArrayList<Node>();

	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private PersonDao dao;
	public ArrayList<OrgNodeModel> orgNodeSecondList;
	public Map<String, Map<String, ArrayList<StructuredStaffModel>>> bigTreeMap;
	private Handler handler;
	private ArrayList<String[]> check_contact_id_list = new ArrayList<String[]>(0);
	private MenuItem select_ok;
	private String userID;// ����ID
	// ��ϵ��ѡ����ڣ�1-��������2-����Ự;3-������������ѡ��4-������鷢����ѡ��
	private int entranceFlag;
	// 1-���������е������� 2-���������еĳ�����
	private int type = -1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_select);
		Log.d(TAG, "����ѡ��ҳ��");

		listView = (ListView) findViewById(R.id.id_tree);

		userID = MySharedPreference.get(ContactSelect.this, MySharedPreference.USER_ID, null);

		entranceFlag = getIntent().getExtras().getInt("entrance_flag");
		type = getIntent().getExtras().getInt("type");

		lsSelectedPod = (List<Node>) getIntent().getExtras().getSerializable("pod");
		lsSelectedReceiver = (List<Node>) getIntent().getExtras().getSerializable("receiver");

		initActionBar();
		// ��֯����������׼��
		dao = daoFactory.getPersonDao(ContactSelect.this);

		try {
			List<Org> data = new ArrayList<Org>();
			data = dao.getOrg2();
			adapter = new PersonSelectAdapter<Org>(listView, this, data, 1, lsSelectedPod,
					lsSelectedReceiver, entranceFlag, type);
			listView.setAdapter(adapter);
			adapter.setDataChangedListener(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		initHandler();
	}

	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				// case LocalConstant.SELECT_CONTACT_CHECKED:
				// check_count++;
				// check_contact_id_list.add((String[]) msg.obj);
				// select_ok.setTitle("ȷ��(" + check_count + ")");
				// break;
				// case LocalConstant.SELECT_CONTACT_UNCHECKED:
				// check_count--;
				// check_contact_id_list.remove((String[]) msg.obj);
				// select_ok.setTitle("ȷ��(" + check_count + ")");
				// break;
				case Constant.GROUP_CREATE_SECCESS:
					if (selectedPerson != null) {
						CreateGroupResponse r = (CreateGroupResponse) msg.obj;
						Intent intent = new Intent(ContactSelect.this, ChatDetail.class);
						intent.putExtra("entrance_type", 1); // ��Ϣ����������
						intent.putExtra("selected_id", Integer.parseInt(r.getGid()));
						GroupDao dao = new GroupDao(ContactSelect.this);
						intent.putExtra("selected_name", dao.queryGroupById(r.getGid()).getN());
						intent.putExtra("isGroup", true);
						startActivity(intent);
						ContactSelect.this.finish();
					} else {
						showLongToast("�����Զ���Ⱥ��ʧ��");
					}
					break;
				case Constant.GROUP_CREATE_FAIL:

					break;
				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler, Constant.GROUP_CREATE_SECCESS,
				Contants.METHOD_GROUP_CREATE);
		MessageHandlerManager.getInstance().register(handler, Constant.GROUP_CREATE_FAIL,
				Contants.METHOD_GROUP_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MessageHandlerManager.getInstance().unregister(Constant.GROUP_CREATE_SECCESS,
				Contants.METHOD_GROUP_CREATE);
		MessageHandlerManager.getInstance().unregister(Constant.GROUP_CREATE_FAIL,
				Contants.METHOD_GROUP_CREATE);
	}

	// actionbar��ʼ��
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("ѡ����ϵ��");
	}

	@Override
	public void onChanged(int size) {
		select_ok.setTitle("ȷ��(" + size + ")");
	}

	int size = 0;

	// �Ҳఴť
	public boolean onCreateOptionsMenu(Menu menu) {

		// ȷ����ť
		select_ok = menu.add(0, 1, 0, "ȷ��");
		if (lsSelectedPod != null && (entranceFlag == 4 || (entranceFlag == 1 && type == 1)))
			size = lsSelectedPod.size();
		else if (lsSelectedReceiver != null
				&& (entranceFlag == 3 || (entranceFlag == 1 && type == 2))) {
			size = lsSelectedReceiver.size();
		}
		select_ok.setTitle("ȷ��(" + size + ")");
		select_ok.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	// ��actionbar�еĲ˵�ע��
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:// ȡ���������ϲ�
			setResult(RESULT_CANCELED);
			finish();
			break;
		case 1: // ȷ����ť
			selectedPerson = adapter.getSelectedDate();

			// 2014-7-31 WeiHao �����ݴ�
			// String name = "";
			// String id = "";
			
//			if (selectedPerson.size() == 0) {
//				Utils.showShortToast(ContactSelect.this, "δѡ���κ���");
//				// �����ϲ�
//				setResult(RESULT_CANCELED);
//				finish();
//				break;
//			} else {
				// ���ѡ������Ա�����ȷ��
				Intent intent = null;

				// Ƕ��switch��֧
				switch (entranceFlag) {

				case 1: // ��������������ѡ��
					intent = new Intent();
					intent.putExtra("data", (Serializable) selectedPerson);
					intent.putExtra("type", type);
					setResult(RESULT_OK, intent);
					this.finish();
					break;

				case 2: // ������Ϣ
					if (selectedPerson.size() > 1) {
						showAlterDialog("�����Զ���Ⱥ��", "�Ƿ񴴽��Զ���Ⱥ�飿", null, "ȷ��", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								List<GroupUpdateQueryRequestIds> rids = new ArrayList<GroupUpdateQueryRequestIds>();
								for (Node item : selectedPerson) {
									rids.add(new GroupUpdateQueryRequestIds(
											item.getId().substring(1)));
								}
								rids.add(new GroupUpdateQueryRequestIds(getUserId()));
								WebRequestManager manager = new WebRequestManager(
										AppApplication.getInstance(), ContactSelect.this);
								manager.createGroup("2", dao.getCustomer().getN() + "������Ⱥ��",
										System.currentTimeMillis() + "",
										System.currentTimeMillis() + "", rids);
							}
						}, "ȡ��", null);
					} else {
						intent = new Intent(ContactSelect.this, ChatDetail.class);
						intent.putExtra("entrance_type", 1); // ��Ϣ����������
						intent.putExtra("data", (Serializable) selectedPerson);
						startActivity(intent);
						this.finish();
					}
					// // 2014-7-15 WeiHao
					// if (id.contains("Group")) {
					// intent.putExtra("selected_id",
					// Integer.parseInt(id.substring(5)));
					// } else {
					// intent.putExtra("selected_id", Integer.parseInt(id));
					// }

					break;
				case 3: // ������� ������ѡ��
					intent = new Intent();
					intent.putExtra("data", (Serializable) selectedPerson);
					setResult(RESULT_OK, intent);
					this.finish();
					break;
				// 2014-8-6
				case 4: // ������� ������ѡ��
					intent = new Intent();
					intent.putExtra("data", (Serializable) selectedPerson);
					setResult(RESULT_OK, intent);
					this.finish();
					break;

				default:
					break;
				}
//			}

			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * ������ ���֮��ѡcheckbox �ٴε��ͬһ�ӽڵ㣬ȡ����ѡ ��ѡ֮��ȷ����ť��������Ӧ�ı�
	 */
	OnChildClickListener stvClickEvent = new OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
				int childPosition, long id) {

			Toast.makeText(ContactSelect.this, groupPosition + ";" + childPosition,
					Toast.LENGTH_SHORT).show();
			return false;
		}
	};

}
