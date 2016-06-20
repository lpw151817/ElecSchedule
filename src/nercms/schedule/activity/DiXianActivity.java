package nercms.schedule.activity;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.activity.BaseActivity.PERSON_TYPE;
import nercms.schedule.utils.AttachmentDatabase;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.MyGPS;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.elec.dao.GpsDao;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.model.bean.GPS;

import com.actionbarsherlock.view.MenuItem;

public class DiXianActivity extends BaseActivity implements OnClickListener {

	protected static final int RequestCode = 666;
	private ListView mListView;
	private Button mNewItem;
	List<String> list = new ArrayList<String>();
	private MyAdapter adapter;
	int enterType;
	int preCount;// 数据库中存储的记录的数目，这是之前创建的条目数
	private String tid;
	// 存放每个条目中被上传了的附件
	List<List<Map<String, Object>>> mUploadList = new ArrayList<List<Map<String, Object>>>();
	// 整个List
	List<List<Map<String, Object>>> mList = new ArrayList<List<Map<String, Object>>>();
	private int position;
	private int[] myMediaIndexs;
	// private int[] counts;//初始counts
	private boolean isContinueTask = false;
	private int lastDixianCount;
	private List<Map<String, Object>> mUrl;

	private HashMap<String, String> data;
	private int[] counts;
	private PlanTaskDao pDao;
	private int[] mContentCount;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dixian);

		iniActionBar(true, null, "地线照片");

		mListView = (ListView) findViewById(R.id.listview);
		mNewItem = (Button) findViewById(R.id.newitem);
		if (isAdmin() != PERSON_TYPE.XIANCHANG) {
			mNewItem.setVisibility(View.GONE);
		}
		mNewItem.setOnClickListener(this);

		adapter = new MyAdapter(DiXianActivity.this, list, isAdmin(), mContentCount);
		mListView.setAdapter(adapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				Intent intent = new Intent(DiXianActivity.this, XianChangUpload.class);

				intent.putExtra("enterType", enterType);
				intent.putExtra("tid", tid);
				intent.putExtra("mUploadUrl", (Serializable) mUploadList.get(arg2));
				intent.putExtra("url", (Serializable) mList.get(arg2));
				intent.putExtra("position", position);
				intent.putExtra("thirdIndex", arg2);
				intent.putExtra("dixian", "dixian");
				setIndex(intent, arg2);
				startActivityForResult(intent, RequestCode);

			}
		});

		getData();
	}

	@SuppressWarnings("unchecked")
	private void getData() {
		enterType = getIntent().getIntExtra("enterType", -1);
		tid = getIntent().getStringExtra("tid");
		position = getIntent().getIntExtra("position", -1);

		getDataFromDB();
	}

	private void getDataFromDB() {

		pDao = new PlanTaskDao(DiXianActivity.this);
		if (pDao != null) {
			lastDixianCount = pDao.getLastDixian(tid);
			counts = new int[lastDixianCount];
			mContentCount = new int[lastDixianCount];

			list.clear();
			mList.clear();
			mUploadList.clear();
			for (int i = 0; i < lastDixianCount; i++) {// 判断每个条目里面是否有内容
				list.add("第" + (i + 1) + "条");
			}

			for (int i = 0; i < lastDixianCount; i++) {
				List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
				mList.add(mItem);
			}

			for (int i = 0; i < lastDixianCount; i++) {
				List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
				mUploadList.add(mItem);
			}

		}

		adapter.notifyDataSetChanged();

		data = AttachmentDatabase.instance(this)
				// 数据库中的url为文件名
				.query("SELECT * from tb_task_attachment where task_id = " + tid
						+ " AND (url LIKE '" + tid + "/" + (4) + "/" + 3 + "%' or url LIKE '" + tid
						+ "\\" + (4) + "/" + 3 + "%' or url LIKE '" + tid + "//" + (4) + "/" + 3
						+ "%' or url LIKE '" + tid + "\\\\" + (4) + "/" + 3 + "%')");

		if (data != null) {
			// String fileCount = data.get("records_num");

			for (int i = 0; i < Integer.parseInt(data.get("records_num")); i++) {
				Log.v("login", data.get("id_" + i) + ":" + data.get("url_" + i) + ":"
						+ data.get("status_" + i) + ":"

				+ data.get("task_id_" + i) + ":" + data.get("dixian_" + i));

				String standard = data.get("standard_" + i);

				GpsDao gpsDao = new GpsDao(this);
				GPS gps = gpsDao.getHistory(data.get("historygps_" + i));// 从数据库中获取gps信息
				MyGPS mGPS = new MyGPS(gps.getOllectionTime(), Double.valueOf(gps.getLongitude()),
						Double.valueOf(gps.getLatitude()), Float.valueOf(gps.getAccuracy()),
						Double.valueOf(gps.getHeight()), Float.valueOf(gps.getSpeed()),
						gps.getCoordinate());

				String name = data.get("url_" + i);
				String mediaName1 = name.replace("\\", File.separator);// 把所有的\替换成/
				String mediaName = mediaName1.substring(mediaName1.lastIndexOf("/") + 1);
				String filePath = XianChangAdd.DownloadfileFolder + mediaName;

				// 附件信息
				Map<String, Object> mMap1 = new HashMap<String, Object>();
				mMap1.put("gps", mGPS);
				mMap1.put("path", filePath);
				mMap1.put("time", gps.getOllectionTime());// 传递附件的时间
				mMap1.put("pathContainsTid", XianChangAdd.DownloadfileFolder + mediaName1);

				String path1 = (String) mMap1.get("pathContainsTid");// storage/emulated/0/nercms-Schedule/DownloadAttachments/356/4/aq2016_06_03_143336.jpg
				String path = path1.replace("//", File.separator);
				int itemId = path.lastIndexOf("/");
				String name1 = path.substring(itemId + 1);
				String tmp[] = name1.split("-");
				String attId = tmp[0].substring(1);
				int attId1 = Integer.parseInt(attId);

				mMap1.put("index", counts[attId1 - 1] + "");
				counts[attId1 - 1]++;
				mList.get(attId1 - 1).add(mMap1);
				mUploadList.get(attId1 - 1).add(mMap1);
			}
		}
	}

	private void setIndex(Intent intent, int position) {
		myMediaIndexs = new int[mList.size()];

		if (isContinueTask) {// 是从继续任务yandianActivity这个页面跳过来的
			for (int i = 0; i < mList.size(); i++) {
				myMediaIndexs[i] = mList.get(i).size();// 原来每个条目里面的内容数
			}
			intent.putExtra("myMediaIndex", myMediaIndexs[position]);

		} else {// 从xianchangupload跳转的
			setMediaIndex();
			intent.putExtra("myMediaIndex", myMediaIndexs[position]);

		}
	}

	private void setMediaIndex() {// 判断每个条目里面有多少个数据
		for (int i = 0; i < mList.size(); i++) {
			int count = 0;
			for (int j = 0; j < mList.get(i).size(); j++) {
				if (mList.get(i).get(j) != null || mList.get(i).get(j) == null) {
					count++;
				}
			}
			myMediaIndexs[i] = count;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RequestCode) {
			if (resultCode == LocalConstant.SELECT_ATTACHMENT) {
				int index = data.getIntExtra("thirdIndex", -1);// 用来确定你修改的是哪个条目里面的内容
				int mediaIndex = data.getIntExtra("mediaIndex", 0);

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> mContent = (List<Map<String, Object>>) data
						.getSerializableExtra("url");
				mList.set(index, mContent);// 改变在Upload里面对应被更改的内容

				@SuppressWarnings("unchecked")
				List<Map<String, Object>> mUploadUrl = (List<Map<String, Object>>) data
						.getSerializableExtra("mUploadUrl");
				mUploadList.set(index, mUploadUrl);
				myMediaIndexs[index] = mediaIndex;
			}
		}
	}

	@Override
	public void onClick(View v) {

		// 动态初始化List,因为无法再以开始确定item的个数
		List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
		List<Map<String, Object>> mItem2 = new ArrayList<Map<String, Object>>();
		mList.add(mItem);
		mUploadList.add(mItem2);

		int count = list.size();

		if (count == 0) {
			list.add("第" + (count + 1) + "条");
		} else {// count != 0;
			if ((mList.get(count - 1).size()) != 0) {// 只需要判断最后一个条目里面有没有内容即可
				list.add("第" + (count + 1) + "条");
			} else {
				Toast.makeText(DiXianActivity.this, "请先添加照片", Toast.LENGTH_SHORT).show();
			}
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			back();

			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		back();
	}

	private void back() {

		Intent intent = new Intent();
		List<Map<String, Object>> url = new ArrayList<Map<String, Object>>();
		for (List<Map<String, Object>> furl : mList) {
			for (Map<String, Object> map : furl) {
				url.add(map);
			}
		}
		intent.putExtra("url", (Serializable) url);
		intent.putExtra("DiXianData", "DiXianData");
		intent.putExtra("secondIndex", getIntent().getIntExtra("secondIndex", -1));

		setResult(LocalConstant.SELECT_ATTACHMENT, intent);

		this.finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
		getDataFromDB();
		changeTextColor();
	}

	// 改变list的颜色
	private void changeTextColor() {

		// 遍历mList获取每个条目的数目
		for (int i = 0; i < mList.size(); i++) {
			int count = 0;
			for (int j = 0; j < mList.get(i).size(); j++) {
				if (mList.get(i).get(j) != null) {
					count++;
				}
			}
			mContentCount[i] = count;
			adapter.setContentCount(mContentCount);
		}
		adapter.notifyDataSetChanged();

		int position = 0;
		// 根据mList里面是否有内容来设置字体的颜色
		for (List<Map<String, Object>> mLi : mList) {
			// System.out.println("map : " + mLi.toString());
			int content = 0;

			for (Map<String, Object> map : mLi) {
				if (map != null) {
					if (map.get("path") != null) {
						System.out.println("file path : " + map.get("path"));
						content++;
					}
				}
			}

			if (content != 0) {
				adapter.setContent(position, 1);
			} else {
				// System.out.println("XianChangAdd 没有内容了");
				adapter.setContent(position, 0);
			}
			position++;
		}
	}
}

class MyAdapter extends BaseAdapter {

	List<String> list;
	Context context;

	int[] hasContent = new int[30];
	int[] contentCount;
	PERSON_TYPE isAdmin;

	/**
	 * 需要修改是否有内容的标志位
	 * 
	 * @param position
	 *            修改第几项，从0开始
	 * @param hasContent
	 *            1表示有内容，0表示无内容
	 */
	public void setContent(int position, int hasContent) {
		if (position > -1 && position < this.hasContent.length) {
			this.hasContent[position] = hasContent;
			this.notifyDataSetChanged();
		}
	}

	public void setContentCount(int[] count) {
		contentCount = count;
	}

	public MyAdapter(Context context, List<String> list, PERSON_TYPE isAdmin, int[] num) {
		this.list = list;
		this.context = context;

		this.isAdmin = isAdmin;
		this.contentCount = num;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;

		if (convertView == null) {

			convertView = LayoutInflater.from(context).inflate(R.layout.dixian_adapter, null);
			holder = new Holder();
			holder.textview = (TextView) convertView.findViewById(R.id.textview_dixian);
			holder.imgview = (ImageView) convertView.findViewById(R.id.imageView1_dixian);

			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.textview.setText(list.get(position));

		if (contentCount != null && contentCount.length > position) {
			if (contentCount[position] == 0) {
				holder.textview.setText(list.get(position));
			} else {
				holder.textview.setText(
						list.get(position) + "    " + "( " + contentCount[position] + " )");
			}

		} else {
			holder.textview.setText(list.get(position));
		}

		if (hasContent != null && hasContent.length != 0) {

			if (hasContent[position] == 1) {
				holder.textview.setTextColor(Color.RED);
			} else {
				holder.textview.setTextColor(Color.BLACK);
			}
		}

		if (isAdmin != PERSON_TYPE.XIANCHANG) {
			if (contentCount[position] == 0) {
				holder.textview.setTextColor(Color.GRAY);
				holder.imgview.setVisibility(View.GONE);
			}
		}

		return convertView;
	}

	class Holder {
		TextView textview;
		ImageView imgview;
	}

}
