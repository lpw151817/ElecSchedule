package nercms.schedule.activity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.adapter.XianChangAddAdapter;
import nercms.schedule.utils.LocalConstant;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class XianChangAdd extends BaseActivity {
	// TODO ��������
	int enterType = 1;

	ListView mListView;
	Button bt_jieshurenwu;
	TextView tv_time;

	int flag;// ��������ĸ���Ŀ������

	// ÿ����Ŀ�ĸ���·��
	List<String> mPath = new ArrayList<String>();

	// ����List
	List<List<String>> mList = new ArrayList<List<String>>();

	public static boolean hasContent = false;// ������־��Ŀ�����Ƿ�������������XianChangUpload��loadAttachment�����Ƿ�ִ��

	private XianChangAddAdapter xianChangAddAdapter;

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
				// TODO Auto-generated method stub

			}
		});
		tv_time = (TextView) findViewById(R.id.time);

		mListView = (ListView) findViewById(R.id.listview);
		xianChangAddAdapter = new XianChangAddAdapter(this,
				enterType);
		mListView.setAdapter(xianChangAddAdapter);

		// ����6����Ŀ
		for (int i = 0; i < 6; i++) {
			List<String> mItem = new ArrayList<String>();
			mList.add(mItem);
		}
		System.out.println("mList: " + mList.toString());

		flag = getIntent().getIntExtra("position", -1);

		// mPath.add((String) getIntent().getCharSequenceExtra("path"));
		mList.get(flag).add((String) getIntent().getCharSequenceExtra("path"));

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(XianChangAdd.this,
						XianChangUpload.class);
				// if (position == flag){//ѡ�е���Ŀ�ʹ��ݹ�������Ŀʱͬһ����Ŀ���ͽ�����·�����ݹ�ȥ
				// intent.putExtra("url", (Serializable)mList.get(flag));
				//
				// hasContent = true;
				// } else {
				// hasContent = false;
				// }
				intent.putExtra("url", (Serializable) mList.get(position));
				intent.putExtra("position", position);// �ѱ��������Ŀ��λ�ô��ݽ�ȥ�������ڽ��յ�ʱ���֪���øøı��ĸ�list������
				XianChangAdd.this.startActivityForResult(intent, 100);
			}
		});

		changeTextColor();

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		changeTextColor();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub

		if (resultCode == LocalConstant.SELECT_ATTACHMENT) {
			int index = data.getIntExtra("position", -1);
			System.out.println("index : " + index);
			mPath = (List<String>) data.getSerializableExtra("url");
			mList.set(index, mPath);// �ı���Upload�����Ӧ�����ĵ�����
		}

	}
	
	private void changeTextColor(){
		int position = 0;
		// ����mList�����Ƿ��������������������ɫ
		for (List<String> mLi : mList) {
			
			int content = 0;
			for (String url : mLi) {
				if (! TextUtils.isEmpty(url)) {
					System.out.println("url :"+ url);
					content++;
					System.out.println("content : "+ content);
				}
			}
			if (content != 0) {
				xianChangAddAdapter.setContent(position, 1);
			} else {
//				System.out.println("XianChangAdd û��������");
				xianChangAddAdapter.setContent(position, 0);
			}
			position++;
		}
	}

}
