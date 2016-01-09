package nercms.schedule.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.adapter.XianChangSiAdapter;
import nercms.schedule.adapter.XianChangSiAdapter.ListPosition;
import nercms.schedule.utils.LocalConstant;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.actionbarsherlock.view.MenuItem;

public class XianChangSi extends BaseActivity {

	ListView mListView;
	// 1:作业现场，2：操作现场，3：故障现场
	int type;
	String tid;

	private XianChangSiAdapter xianChangSiAdapter;
	private MyListPosition myList;
	private Map<Integer, Map<String, String>> mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_si);

		type = getIntent().getIntExtra("enterType", -1);
		tid = getIntent().getStringExtra("tid");

		switch (type) {
		case 1:
			iniActionBar(true, null, "作业现场");
			break;
		case 2:
			iniActionBar(true, null, "操作现场");
			break;
		case 3:
			iniActionBar(true, null, "故障紧急抢修现场");
		}

		mListView = (ListView) findViewById(R.id.listView1);
		mContent = new HashMap<Integer, Map<String, String>>();

		xianChangSiAdapter = new XianChangSiAdapter(this, type);
		mListView.setAdapter(xianChangSiAdapter);
		myList = new MyListPosition();
		xianChangSiAdapter.setListPostion(myList);

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Intent intent = new Intent(XianChangSi.this, ShowXianChangAttachment.class);
			intent.putExtra("enterType", type);
			intent.putExtra("tid", tid);

			Map<String, String> mMap = new HashMap<String, String>();

			switch (requestCode) {
			case LocalConstant.CAPTURE_IMAGE_REQUEST_CODE:

				// Bundle bundle = new Bundle();
				// bundle.putCharSequence("type", "image");
				// bundle.putCharSequence("image",
				// XianChangSiAdapter.imagePath);
				// bundle.putInt("position",
				// myList.position);//被选中的listview的位置
				// intent.putExtra("address", bundle);

				mMap.put("type", "image");
				mMap.put("image", XianChangSiAdapter.imagePath);
				mMap.put("position", myList.position + "");
				mMap.put("from", "XianChangSi");

				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable) mContent);

				System.out.println("outer : " + myList.position);

				break;

			case LocalConstant.CAPTURE_AUDIO_REQUEST_CODE:

				// Bundle bundle = new Bundle();
				// bundle.putCharSequence("type", "audio");
				// bundle.putCharSequence("audiopath",
				// data.getStringExtra("path"));
				// bundle.putInt("position", myList.position);//被选中的listview的位置
				// System.out.println("position123: "+ myList.position);
				// intent.putExtra("address", bundle);

				mMap.put("type", "audio");
				mMap.put("audiopath", data.getStringExtra("path"));
				mMap.put("position", myList.position + "");
				mMap.put("from", "XianChangSi");

				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable) mContent);
				// System.out.println("XianchangsiActivity: " +
				// data.getStringExtra("path"));
				break;

			default:
				break;
			}
			startActivity(intent);
		}

	}

	class MyListPosition implements ListPosition {
		int position;

		@Override
		public int getPositon(int position) {
			System.out.println("position: " + position);
			this.position = position;
			return 0;
		}

	}
}
