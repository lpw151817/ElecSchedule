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
 * showXianchangAttachment��XianChangUpload������ת���ý���
 */
public class XianChangAdd extends BaseActivity implements ReceiveGPS{
	// TODO ��������
	int enterType = 1;

	ListView mListView;
	Button bt_jieshurenwu;
	TextView tv_time;

	int flag;// ��������ĸ���Ŀ������

	// ÿ����Ŀ�ĸ���·��
	// һ��Map����������·��������GPS��Ϣ��ʱ���
	Map<String, Object> mAttachment = new HashMap<String, Object>();

	// ÿ����Ŀ�ĸ�������
	List<Map<String, Object>> mContent = new ArrayList<Map<String, Object>>();

	// ����List
	List<List<Map<String, Object>>> mList = new ArrayList<List<Map<String, Object>>>();

	public static boolean hasContent = false;// ������־��Ŀ�����Ƿ�������������XianChangUpload��loadAttachment�����Ƿ�ִ��

	private XianChangAddAdapter xianChangAddAdapter;


	private Handler handler;
	private int fileCount;
	
	/*
	 * ÿһ����Ŀ����һ��upload���棬ÿ��upload������mediaIndex����Ǹ����ڸ������е�λ�ã�
	 * �ڵ��delete��ť��ʱ�򣬾�ɾ����Ӧ�ĸ������ܹ���6��upload���棬���upload�����˳���ʱ��
	 * mediaindex�ᱻ��Ϊ0����ô�´ν�����ʱ�򣬸������±��0��ʼ�����������ͺ͸�����·������Ӧ��
	 */
	private int[] myMediaIndexs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_add);


		switch (enterType) {
		case 1:
			getSupportActionBar().setTitle("��ҵ�ֳ��б�");
			break;
		case 2:
			getSupportActionBar().setTitle("�����ֳ��б�");
			break;
		case 3:
			getSupportActionBar().setTitle("���Ͻ��������ֳ��б�");
			break;
		}

		bt_jieshurenwu = (Button) findViewById(R.id.jieshurenwu);
		bt_jieshurenwu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				fileCount = getFileCount();// ��ȡ�ļ��ĸ������ϴ����finish��ǰҳ��
				// Log.e("TAG", "xianChangAdd fileCount : "+ fileCount);
				attachmentUploadRequest();// �ϴ�����
			}

		});
		tv_time = (TextView) findViewById(R.id.time);

		mListView = (ListView) findViewById(R.id.listview);
		xianChangAddAdapter = new XianChangAddAdapter(this, enterType);
		mListView.setAdapter(xianChangAddAdapter);
		
		myMediaIndexs = new int[6];

		// ����6����Ŀ
		for (int i = 0; i < 6; i++) {
			List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
			mList.add(mItem);
		}

		flag = getIntent().getIntExtra("position", -1);
		System.out.println("���ݹ�����λ����Ϣflag :" + flag);
		


		// mPath.add((String) getIntent().getCharSequenceExtra("path"));
		mList.get(flag).add((Map<String, Object>) getIntent().getSerializableExtra("path"));

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(XianChangAdd.this,XianChangUpload.class);
				if (from.equals("XianChangUpload")){
					intent.putExtra("mediaIndex", mediaIndex);
					System.out.println("����mediaIndex��ֵ����¼�ϴθ������±�");
				}
				
				if (flag == position){
					myMediaIndexs[flag] = 1;
					intent.putExtra("myMediaIndex", myMediaIndexs[position]);
				}else {
					intent.putExtra("myMediaIndex", myMediaIndexs[position]);
				}
				
				intent.putExtra("url", (Serializable) mList.get(position));
				intent.putExtra("position", position);// �ѱ��������Ŀ��λ�ô��ݽ�ȥ�������ڽ��յ�ʱ���֪���øøı��ĸ�list������
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
					if (map.get("path") != null) {//����map��get������ǰ����map != null
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
		
		changeTextColor();//���³��ֵ�ʱ���������  ��Ϊoncreateִֻ��һ��
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
			mList.set(index, mContent);// �ı���Upload�����Ӧ�����ĵ�����
			
			myMediaIndexs[index] = mediaIndex;
		}

	}

	//�ı�list����ɫ
	private void changeTextColor() {
		int position = 0;
		// ����mList�����Ƿ��������������������ɫ
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
				// System.out.println("XianChangAdd û��������");
				xianChangAddAdapter.setContent(position, 0);
			}
			position++;
		}
	}

	private void attachmentUploadRequest() {
		if (!Utils.isNetworkAvailable(XianChangAdd.this)) {
			Toast.makeText(XianChangAdd.this, "���粻����", Toast.LENGTH_SHORT)
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
				case Constant.FILE_UPLOAD_SUCCESS:// �����еĸ������ϴ�����֮��finish��ǰҳ��

					fileCount--;
					Log.i("TAG", "count : " + fileCount);
					if (fileCount == 0) {
						Toast.makeText(XianChangAdd.this, "�ϴ��ɹ�",
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
