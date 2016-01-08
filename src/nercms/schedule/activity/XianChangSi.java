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
	int type = 1;

	private XianChangSiAdapter xianChangSiAdapter;
	private MyListPosition myList;
	private Map<Integer, Map<String, String>> mContent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_si);

		type = getIntent().getIntExtra("enterType", 1);
		initActionBar();

		mListView = (ListView) findViewById(R.id.listView1);
		mContent = new HashMap<Integer, Map<String,String>>();
		
		xianChangSiAdapter = new XianChangSiAdapter(this, type);
		mListView.setAdapter(xianChangSiAdapter);
		myList = new MyListPosition();
		xianChangSiAdapter.setListPostion( myList);
		
	}

	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		switch (type) {
		case 1:
			getSupportActionBar().setTitle("作业现场");
			break;
		case 2:
			getSupportActionBar().setTitle("操作现场");
			break;
		case 3:
			getSupportActionBar().setTitle("故障紧急抢修现场");
		}
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

		switch (requestCode) {
		case LocalConstant.CAPTURE_IMAGE_REQUEST_CODE:
			if (resultCode == RESULT_OK){
				Intent intent = new Intent(XianChangSi.this, ShowXianChangAttachment.class);
//				Bundle bundle = new Bundle();
//				bundle.putCharSequence("type", "image");
//				bundle.putCharSequence("image", XianChangSiAdapter.imagePath);
//				bundle.putInt("position", myList.position);//被选中的listview的位置
//				intent.putExtra("address", bundle);
				
				Map<String, String> mMap = new HashMap<String, String>();
				mMap.put("type", "image");
				mMap.put("image", XianChangSiAdapter.imagePath);
				mMap.put("position", myList.position+"");
				mMap.put("from", "XianChangSi");
				
				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable)mContent);
				startActivity(intent);
				
				System.out.println("outer : " + myList.position);
			}

			break;
			
		case LocalConstant.CAPTURE_AUDIO_REQUEST_CODE:
			if (resultCode == RESULT_OK){
				Intent intent = new Intent(XianChangSi.this, ShowXianChangAttachment.class);
//				Bundle bundle = new Bundle();
//				bundle.putCharSequence("type", "audio");
//				bundle.putCharSequence("audiopath", data.getStringExtra("path"));
//				bundle.putInt("position", myList.position);//被选中的listview的位置
//				System.out.println("position123: "+ myList.position);
//				intent.putExtra("address", bundle);
				
				
				Map<String, String> mMap = new HashMap<String, String>();
				mMap.put("type", "audio");
				mMap.put("audiopath", data.getStringExtra("path"));
				mMap.put("position", myList.position+"");
				mMap.put("from", "XianChangSi");
				
				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable)mContent);
				startActivity(intent);
//				System.out.println("XianchangsiActivity: " +  data.getStringExtra("path"));
			}
			break;
			

		default:
			break;
		}
	}
	
	class MyListPosition implements ListPosition{
		int position;
		@Override
		public int getPositon(int position) {
			System.out.println("position: "+ position);
			this.position = position;
			return 0;
		}
		
	}
}
