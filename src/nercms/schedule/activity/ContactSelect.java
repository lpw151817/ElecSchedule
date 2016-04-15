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
import android.wxapp.service.elec.dao.Org;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.model.bean.table.TB_SYS_Person;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.group.CreateGroupResponse;
import android.wxapp.service.jerry.model.group.GroupUpdateQueryRequestIds;
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

	private OrgDao dao;
	public ArrayList<OrgNodeModel> orgNodeSecondList;
	public Map<String, Map<String, ArrayList<StructuredStaffModel>>> bigTreeMap;
	private MenuItem select_ok;
	private String userID;// ����ID
	// ��������룬1��ÿ�ռƻ�¼�룬2��ָ��
	private int entranceFlag;
	// 1-���������� 2-Ӧ�ϸڵ�λ�쵼
	private int type = -1;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_select);
		Log.d(TAG, "����ѡ��ҳ��");

		listView = (ListView) findViewById(R.id.id_tree);

		userID = MySharedPreference.get(ContactSelect.this, MySharedPreference.USER_ID, null);

		entranceFlag = getIntent().getExtras().getInt("entrance_flag");
		type = getIntent().getExtras().getInt("type", -1);

		lsSelectedPod = (List<Node>) getIntent().getExtras().getSerializable("pod");
		lsSelectedReceiver = (List<Node>) getIntent().getExtras().getSerializable("receiver");
		if (type == 1)
			iniActionBar(true, null, "ѡ����������");
		else if (type == 2)
			iniActionBar(true, null, "ѡ��Ӧ�ϸڵ�λ�쵼");
		else if (type == 3)
			iniActionBar(false, null, "ѡ�������Ա");
		// ��֯����������׼��
		dao = new OrgDao(this);

		try {
			List<Org> data = new ArrayList<Org>();

			// data = dao.getOrg2();

			data.addAll(dao.getAllOrgs());
			// ѡ����������
			if (type == 1) {
				data.addAll(dao.convert(dao.getPersons("0")));
			} else if (type == 2) {
				if (isAdmin() == PERSON_TYPE.GUANLI)
					data.addAll(dao.convert(dao.getPersons("1")));
				else if (isAdmin() == PERSON_TYPE.LINGDAO) {
					List<TB_SYS_Person> tmp = dao.getPersons("1");
					tmp.addAll(dao.getPersons("2"));
					data.addAll(dao.convert(tmp));
				}
			}

			adapter = new PersonSelectAdapter<Org>(listView, this, data, 1, lsSelectedPod,
					lsSelectedReceiver, entranceFlag, type);
			listView.setAdapter(adapter);
			adapter.setDataChangedListener(this);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

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

			// if (selectedPerson.size() == 0) {
			// Utils.showShortToast(ContactSelect.this, "δѡ���κ���");
			// // �����ϲ�
			// setResult(RESULT_CANCELED);
			// finish();
			// break;
			// } else {
			// ���ѡ������Ա�����ȷ��
			Intent intent = null;

			// Ƕ��switch��֧
			switch (entranceFlag) {

			case 1: // ÿ�ռƻ�¼��
				intent = new Intent();
				intent.putExtra("data", (Serializable) selectedPerson);
				intent.putExtra("type", type);
				setResult(RESULT_OK, intent);
				this.finish();
				break;

			case 2: // ����ָ��
				intent = new Intent();
				intent.putExtra("data", (Serializable) selectedPerson);
				setResult(RESULT_OK, intent);
				this.finish();
				break;
			}
			// }

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
