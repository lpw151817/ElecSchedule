package nercms.schedule.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.adapter.OrgSelectAdapter;
import nercms.schedule.adapter.OrgSelectAdapter.DataChanged;
import nercms.schedule.adapter.PersonSelectAdapter;
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

public class OrgSelect extends BaseActivity implements DataChanged {

	private static final String TAG = "ContactSelect";

	ListView listView;
	OrgSelectAdapter<Org> adapter;

	List<Node> selectedPerson;
	private Node lsSelectedPod;

	private OrgDao dao;
	public ArrayList<OrgNodeModel> orgNodeSecondList;
	public Map<String, Map<String, ArrayList<StructuredStaffModel>>> bigTreeMap;
	private MenuItem select_ok;
	private String userID;// 本人ID

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contact_select);
		Log.d(TAG, "进入选择页面");

		iniActionBar(true, null, "选择实施单位");

		listView = (ListView) findViewById(R.id.id_tree);

		userID = MySharedPreference.get(OrgSelect.this, MySharedPreference.USER_ID, null);

		lsSelectedPod = (Node) getIntent().getExtras().getSerializable("pod");

		// 组织机构树数据准备
		dao = new OrgDao(this);

		try {
			List<Org> data = new ArrayList<Org>();
			data = dao.getAllOrgs();
			adapter = new OrgSelectAdapter<Org>(listView, this, data, 1, lsSelectedPod);
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
		select_ok.setTitle("确定(" + size + ")");
	}

	int size = 0;

	// 右侧按钮
	public boolean onCreateOptionsMenu(Menu menu) {

		// 确定按钮
		select_ok = menu.add(0, 1, 0, "确定");
		if (adapter.getSelectedDate() != null)
			size = adapter.getSelectedDate().size();
		select_ok.setTitle("确定(" + size + ")");
		select_ok.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		return super.onCreateOptionsMenu(menu);
	}

	// 将actionbar中的菜单注册
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:// 取消，返回上层
			setResult(RESULT_CANCELED);
			finish();
			break;
		case 1: // 确定按钮
			selectedPerson = adapter.getSelectedDate();

			// 2014-7-31 WeiHao 加入容错
			// String name = "";
			// String id = "";

			// if (selectedPerson.size() == 0) {
			// Utils.showShortToast(ContactSelect.this, "未选中任何人");
			// // 返回上层
			// setResult(RESULT_CANCELED);
			// finish();
			// break;
			// } else {
			// 如果选择了人员并点击确定
			Intent intent = null;

			intent = new Intent();
			intent.putExtra("data", (Serializable) adapter.getSelectedDate().get(0));
			setResult(RESULT_OK, intent);
			this.finish();
			break;

		// }

		}
		return super.onOptionsItemSelected(item);
	}

	/*
	 * 监听器 点击之后勾选checkbox 再次点击同一子节点，取消勾选 勾选之后，确定按钮中数字相应改变
	 */
	OnChildClickListener stvClickEvent = new OnChildClickListener() {
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
				int childPosition, long id) {

			Toast.makeText(OrgSelect.this, groupPosition + ";" + childPosition, Toast.LENGTH_SHORT)
					.show();
			return false;
		}
	};

}
