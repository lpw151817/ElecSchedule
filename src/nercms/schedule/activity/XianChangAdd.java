package nercms.schedule.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.adapter.XianChangAddAdapter;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.LocationAttachment;
import nercms.schedule.utils.MyGPS;
import nercms.schedule.utils.MyLocationListener.ReceiveGPS;
import nercms.schedule.utils.Utils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.HttpUploadTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;

/*
 * showXianchangAttachment和XianChangUpload可以跳转到该界面
 */
public class XianChangAdd extends BaseActivity implements ReceiveGPS{
	// TODO 测试数据
	int enterType = 1;

	ListView mListView;
	Button bt_jieshurenwu;
	TextView tv_time;

	int flag;// 用来标记哪个条目有内容

	// 每个条目的附件路径
	// 一个Map包含附件的路径和它的GPS信息和时间戳
	Map<String, Object> mAttachment = new HashMap<String, Object>();

	// 每个条目的附件集合
	List<Map<String, Object>> mContent = new ArrayList<Map<String, Object>>();

	// 整个List
	List<List<Map<String, Object>>> mList = new ArrayList<List<Map<String, Object>>>();

	public static boolean hasContent = false;// 用来标志条目里面是否有内容来决定XianChangUpload中loadAttachment方法是否执行

	private XianChangAddAdapter xianChangAddAdapter;


	private Handler handler;
	private int fileCount;
	
	/*
	 * 每一个条目都有一个upload界面，每个upload界面用mediaIndex来标记附件在附件集中的位置，
	 * 在点击delete按钮的时候，就删除对应的附件，总共有6个upload界面，你从upload界面退出的时候
	 * mediaindex会被置为0，那么下次进来的时候，附件的下标从0开始，但是这样就和附件的路径不对应了
	 */
	private int[] myMediaIndexs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_add);


		switch (enterType) {
		case 1:
			getSupportActionBar().setTitle("作业现场列表");
			break;
		case 2:
			getSupportActionBar().setTitle("操作现场列表");
			break;
		case 3:
			getSupportActionBar().setTitle("故障紧急抢修现场列表");
			break;
		}

		bt_jieshurenwu = (Button) findViewById(R.id.jieshurenwu);
		bt_jieshurenwu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				fileCount = getFileCount();// 获取文件的个数，上传完后finish当前页免
				// Log.e("TAG", "xianChangAdd fileCount : "+ fileCount);
				attachmentUploadRequest();// 上传附件
			}

		});
		tv_time = (TextView) findViewById(R.id.time);

		mListView = (ListView) findViewById(R.id.listview);
		xianChangAddAdapter = new XianChangAddAdapter(this, enterType);
		mListView.setAdapter(xianChangAddAdapter);
		
		myMediaIndexs = new int[6];

		// 创建6个条目
		for (int i = 0; i < 6; i++) {
			List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
			mList.add(mItem);
		}

		flag = getIntent().getIntExtra("position", -1);
		System.out.println("传递过来的位置信息flag :" + flag);
		


		// mPath.add((String) getIntent().getCharSequenceExtra("path"));
		mList.get(flag).add((Map<String, Object>) getIntent().getSerializableExtra("path"));

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(XianChangAdd.this,XianChangUpload.class);
				if (from.equals("XianChangUpload")){
					intent.putExtra("mediaIndex", mediaIndex);
					System.out.println("传回mediaIndex的值来记录上次附件的下标");
				}
				
				if (flag == position){
					myMediaIndexs[flag] = 1;
					intent.putExtra("myMediaIndex", myMediaIndexs[position]);
				}else {
					intent.putExtra("myMediaIndex", myMediaIndexs[position]);
				}
				
				intent.putExtra("url", (Serializable) mList.get(position));
				intent.putExtra("position", position);// 把被点击的条目的位置传递进去，这样在接收的时候就知道该该改变哪个list的内容
				XianChangAdd.this.startActivityForResult(intent, 100);
			}
		});

		initHandler();

	}
	


	protected int getFileCount() {
		int content = 0;
		for (List<Map<String, Object>> mLi : mList) {


			for (Map<String, Object> map : mLi) {
				if (map != null){
					if (map.get("path") != null) {//调用map的get方法的前提是map != null
						content++;
					}
				}
			}
		}
		return content;
	}


	@Override
	protected void onResume() {
		super.onResume();
		
		changeTextColor();//重新出现的时候字体更改  因为oncreate只执行一次
	}

	String from = "";
	int mediaIndex;


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (resultCode == LocalConstant.SELECT_ATTACHMENT) {
			int index = data.getIntExtra("position", -1);
			from = data.getStringExtra("from");
			mediaIndex = data.getIntExtra("mediaIndex", 0);
			System.out.println("index : " + index);
			mContent = (List<Map<String, Object>>) data.getSerializableExtra("url");
			mList.set(index, mContent);// 改变在Upload里面对应被更改的内容
			
			myMediaIndexs[index] = mediaIndex;
		}

	}

	//改变list的颜色
	private void changeTextColor() {
		int position = 0;
		// 根据mList里面是否有内容来设置字体的颜色
		for (List<Map<String, Object>> mLi : mList) {
//			System.out.println("map : " + mLi.toString());
			int content = 0;

			for (Map<String, Object> map : mLi) {
				if (map != null){
					if (map.get("path") != null) {
						System.out.println("file path : " + map.get("path"));
						content++;
					}
				}
			}

			if (content != 0) {
				xianChangAddAdapter.setContent(position, 1);
			} else {
				// System.out.println("XianChangAdd 没有内容了");
				xianChangAddAdapter.setContent(position, 0);
			}
			position++;
		}
	}

	private void attachmentUploadRequest() {
		if (!Utils.isNetworkAvailable(XianChangAdd.this)) {
			Toast.makeText(XianChangAdd.this, "网络不可用", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		String uploadUrl = LocalConstant.FILE_SERVER_ATTACH_URL;
		for (List<Map<String, Object>> mLi : mList) {

			for (Map<String, Object> map : mLi) {
				if (map != null) {
					if (map.get("path") != null) {
						new HttpUploadTask(new TextView(this), this).execute(
								(String)map.get("path"), uploadUrl);
					}
				}
			}
		}

	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.FILE_UPLOAD_SUCCESS:// 当所有的附件都上传完了之后finish当前页面

					fileCount--;
					Log.i("TAG", "count : " + fileCount);
					if (fileCount == 0) {
						Toast.makeText(XianChangAdd.this, "上传成功",
								Toast.LENGTH_SHORT).show();
						finish();
					}

					break;
				case Constant.FILE_UPLOAD_FAIL:
					break;
				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler,
				Constant.FILE_UPLOAD_FAIL, "XianChangAdd");
		MessageHandlerManager.getInstance().register(handler,
				Constant.FILE_UPLOAD_SUCCESS, "XianChangAdd");
	}

	@Override
	public void onReceiveGPS(MyGPS gps) {
		// TODO Auto-generated method stub
		
	}

}
