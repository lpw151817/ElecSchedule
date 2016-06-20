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
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.wxapp.service.elec.dao.DatabaseHelper;
import android.wxapp.service.elec.dao.GpsDao;
import android.wxapp.service.elec.model.bean.GPS;

import com.actionbarsherlock.view.MenuItem;

public class YanDianActivity extends BaseActivity {

	protected static final int YANDIANREQUESTCODE = 654;
	ListView listview;
	private String tid;
	private int enterType;
	private int position;// �ɼ�����ʱ������
	List<Map<String, Object>> mUrl = new ArrayList<Map<String, Object>>();// ��ȷ�����������и���
	boolean isContinueTask;

	private final int itemCount = 4;
	private int[] counts = new int[itemCount];
	private int[] myMediaIndexs = new int[itemCount];

	// ��ȷ��������
	List<List<Map<String, Object>>> mList = new ArrayList<List<Map<String, Object>>>();
	List<List<Map<String, Object>>> mUploadList = new ArrayList<List<Map<String, Object>>>();
	private YanDianAdapter yanDianAdapter;
	private int[] mContentCount = new int[itemCount];
	private HashMap<String, String> data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_yandian);
		iniActionBar(true, null, "yandian");
		initData();

		listview = (ListView) findViewById(R.id.listview_yandian);
		yanDianAdapter = new YanDianAdapter(YanDianActivity.this, isAdmin(),
				mContentCount);
		listview.setAdapter(yanDianAdapter);
		listview.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Intent intent = new Intent(YanDianActivity.this,
						XianChangUpload.class);

				if (arg2 == 2) {
					intent = new Intent(YanDianActivity.this,
							DiXianActivity.class);
				}

				intent.putExtra("enterType", enterType);
				intent.putExtra("tid", tid);
				intent.putExtra("url", (Serializable) mList.get(arg2));
				intent.putExtra("position", position);
				intent.putExtra("secondIndex", arg2);
				intent.putExtra("mUploadUrl",
						(Serializable) mUploadList.get(arg2));
				intent.putExtra("yandian", "yandian");
				setIndex(intent, arg2);
				intent.putExtra("from", "YanDianActivity");
				startActivityForResult(intent, YANDIANREQUESTCODE);

			}
		});

	}

	private void setIndex(Intent intent, int position) {

		if (isContinueTask) {// �ǴӼ�������TaskSelector���ҳ����������
			for (int i = 0; i < itemCount; i++) {
				myMediaIndexs[i] = counts[i];
			}
			intent.putExtra("myMediaIndex", myMediaIndexs[position]);

			isContinueTask = false;
		} else {// ��xianchangupload��ת��
			setMediaIndex();
			intent.putExtra("myMediaIndex", myMediaIndexs[position]);

		}
	}

	@SuppressWarnings("unchecked")
	public void initData() {
		tid = getIntent().getStringExtra("tid");
		enterType = getIntent().getIntExtra("enterType", -1);
		position = getIntent().getIntExtra("position", -1);
		mUrl = (List<Map<String, Object>>) getIntent().getSerializableExtra(
				"url");

		for (int i = 0; i < itemCount; i++) {
			List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
			mList.add(mItem);
		}

		for (int i = 0; i < itemCount; i++) {
			List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
			mUploadList.add(mItem);
		}

		getDataFromDB();
	}

	private void getDataFromDB() {
		
		for (int i = 0; i < mList.size(); i++) {
			mList.get(i).clear();
		}

		for (int i = 0; i < mUploadList.size(); i++) {
			mUploadList.get(i).clear();
		}
		
		data = AttachmentDatabase.instance(this)
		// ���ݿ��е�urlΪ�ļ���
				.query("SELECT * from tb_task_attachment where task_id = "
						+ tid + " AND (url LIKE '" + tid + "/" + (position + 1)
						+ "%' or url LIKE '" + tid + "\\" + (position + 1)
						+ "%' or url LIKE '" + tid + "//" + (position + 1)
						+ "%' or url LIKE '" + tid + "\\\\" + (position + 1)
						+ "%')");

		if (data != null) {
			// String fileCount = data.get("records_num");

			for (int i = 0; i < Integer.parseInt(data.get("records_num")); i++) {
				Log.v("login",
						data.get("id_" + i) + ":" + data.get("url_" + i) + ":"
								+ data.get("status_" + i) + ":"

								+ data.get("task_id_" + i) + ":"
								+ data.get("dixian_" + i));

				String standard = data.get("standard_" + i);

				GpsDao gpsDao = new GpsDao(this);
				GPS gps = gpsDao.getHistory(data.get("historygps_" + i));// �����ݿ��л�ȡgps��Ϣ
				MyGPS mGPS = new MyGPS(gps.getOllectionTime(),
						Double.valueOf(gps.getLongitude()), Double.valueOf(gps
								.getLatitude()), Float.valueOf(gps
								.getAccuracy()),
						Double.valueOf(gps.getHeight()), Float.valueOf(gps
								.getSpeed()), gps.getCoordinate());

				String name = data.get("url_" + i);
				String mediaName1 = name.replace("\\", File.separator);// �����е�\�滻��/
				String mediaName = mediaName1.substring(mediaName1
						.lastIndexOf("/") + 1);
				String filePath = XianChangAdd.DownloadfileFolder + mediaName;

				// ������Ϣ
				Map<String, Object> mMap1 = new HashMap<String, Object>();
				mMap1.put("gps", mGPS);
				mMap1.put("path", filePath);
				mMap1.put("time", gps.getOllectionTime());// ���ݸ�����ʱ��
				mMap1.put("pathContainsTid", XianChangAdd.DownloadfileFolder
						+ mediaName1);

				String path1 = (String) mMap1.get("pathContainsTid");// storage/emulated/0/nercms-Schedule/DownloadAttachments/356/4/aq2016_06_03_143336.jpg
				String path = path1.replace("//", File.separator);
				int itemId = path.lastIndexOf("/");
				String name1 = path.substring(itemId + 1);
				String attId = path.substring(itemId - 1, itemId);

				if (!name1.contains("D")) {

					if (attId.equals("1")) {// ���ߵ�û�����
						mMap1.put("index", counts[0] + "");
						counts[0]++;
						mList.get(0).add(mMap1);
						mUploadList.get(0).add(mMap1);
					} else if (attId.equals("2")) {
						mMap1.put("index", counts[1] + "");
						counts[1]++;
						mList.get(1).add(mMap1);
						mUploadList.get(1).add(mMap1);
					} else if (attId.equals("4")) {
						mMap1.put("index", counts[3] + "");
						counts[3]++;
						mList.get(3).add(mMap1);
						mUploadList.get(3).add(mMap1);
					}
				} else {
					counts[2]++;
					mList.get(2).add(mMap1);
					mUploadList.get(2).add(mMap1);
				}

			}
		}
	}

	private void setMediaIndex() {// �ж�ÿ����Ŀ�����ж��ٸ�����
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
		if (requestCode == YANDIANREQUESTCODE) {
			if (resultCode == LocalConstant.SELECT_ATTACHMENT) {

				String dixian = data.getStringExtra("DiXianData");
				if (dixian != null) {
					@SuppressWarnings("unchecked")
					List<Map<String, Object>> mContent = (List<Map<String, Object>>) data
							.getSerializableExtra("url");
					mList.set(data.getIntExtra("secondIndex", -1), mContent);
				} else {
					int index = data.getIntExtra("secondIndex", -1);// ����ȷ�����޸ĵ����ĸ���Ŀ���������
					int mediaIndex = data.getIntExtra("mediaIndex", 0);

					@SuppressWarnings("unchecked")
					List<Map<String, Object>> mContent = (List<Map<String, Object>>) data
							.getSerializableExtra("url");
					mList.set(index, mContent);// �ı���Upload�����Ӧ�����ĵ�����

					@SuppressWarnings("unchecked")
					List<Map<String, Object>> mUploadUrl = (List<Map<String, Object>>) data
							.getSerializableExtra("mUploadUrl");
					mUploadList.set(index, mUploadUrl);
					myMediaIndexs[index] = mediaIndex;
				}

			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		getDataFromDB();
		changeTextColor();
	}

	// �ı�list����ɫ
	private void changeTextColor() {

		// ����mList��ȡÿ����Ŀ����Ŀ
		for (int i = 0; i < mList.size(); i++) {
			int count = 0;
			for (int j = 0; j < mList.get(i).size(); j++) {
				if (mList.get(i).get(j) != null) {
					count++;
				}
			}
			mContentCount[i] = count;
		}
		yanDianAdapter.notifyDataSetChanged();

		int position = 0;
		// ����mList�����Ƿ��������������������ɫ
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
				yanDianAdapter.setContent(position, 1);
			} else {
				// System.out.println("XianChangAdd û��������");
				yanDianAdapter.setContent(position, 0);
			}
			position++;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}

class YanDianAdapter extends BaseAdapter {

	String[] str = new String[] { "�ֳ����쵥���ֳ�����ͼ", "�����Ƭ", "������Ƭ", "������Ҫ��ȫ������ʩ��Ƭ" };
	Context context;

	int[] hasContent = new int[str.length];
	int[] contentCount;
	PERSON_TYPE isAdmin;

	/**
	 * ��Ҫ�޸��Ƿ������ݵı�־λ
	 * 
	 * @param position
	 *            �޸ĵڼ����0��ʼ
	 * @param hasContent
	 *            1��ʾ�����ݣ�0��ʾ������
	 */
	public void setContent(int position, int hasContent) {
		if (position > -1 && position < this.hasContent.length) {
			this.hasContent[position] = hasContent;
			this.notifyDataSetChanged();
		}
	}

	@Override
	public int getCount() {
		return str.length;
	}

	public YanDianAdapter(Context context, PERSON_TYPE isAdmin, int[] num) {
		this.context = context;
		this.isAdmin = isAdmin;
		this.contentCount = num;
	}

	@Override
	public Object getItem(int arg0) {
		return str[arg0];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;

		if (convertView == null) {

			convertView = LayoutInflater.from(context).inflate(
					R.layout.dixian_adapter, null);
			holder = new Holder();
			holder.textview = (TextView) convertView
					.findViewById(R.id.textview_dixian);
			holder.imgview = (ImageView) convertView
					.findViewById(R.id.imageView1_dixian);

			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.textview.setText(str[position]);

		if (contentCount != null) {
			if (contentCount[position] == 0) {
				holder.textview.setText(str[position]);
			} else {
				holder.textview.setText(str[position] + "    " + "( "
						+ contentCount[position] + " )");
			}

		} else {
			holder.textview.setText(str[position]);
		}

		if (hasContent[position] == 1) {
			holder.textview.setTextColor(Color.RED);
		} else {
			holder.textview.setTextColor(Color.BLACK);
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
