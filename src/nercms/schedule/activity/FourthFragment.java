package nercms.schedule.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import nercms.schedule.R;
import nercms.schedule.utils.AttachmentDatabase;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.wxapp.service.elec.dao.PlanTaskDao;

public class FourthFragment extends Fragment implements OnClickListener {
	Context context;
	private View view;

	TextView mTitle, mWeiZhiXing, mZhenZaiZhiXing, mZhenChang, mTiQian, mYanWu,
			mQuXiao;
	TextView mTitleYesterday, mWeiZhiXingYesterday, mZhenZaiZhiXingYesterday,
			mZhenChangYesterday, mTiQianYesterday, mYanWuYesterday,
			mQuXiaoYesterday;

	PlanTaskDao dao;

	public FourthFragment(Context _ctx) {
		context = _ctx;
		if (null == dao)
			dao = new PlanTaskDao(_ctx);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.activity_sum, container, false);
		initView(view);
		return view;
	}

	private void initView(View view) {
		if (null == dao)
			dao = new PlanTaskDao(context);

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM��dd��");

		Date tmp = new Date(System.currentTimeMillis());
		Date today = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate() + 1);
		Date yesterday = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate());
		Date theDayBeforeYesterday = new Date(tmp.getYear(), tmp.getMonth(),
				tmp.getDate() - 1);

		mTitle = (TextView) view.findViewById(R.id.title_today);
		mTitle.setText("���죨"+dateFormat.format(yesterday)+"��������");

		mWeiZhiXing = (TextView) view.findViewById(R.id.weizhixin);
		mWeiZhiXing.setText(dao.getPlanTasks(4, 3, null, "0",
				yesterday.getTime() + "", today.getTime() + "").size()
				+ "");

		mZhenZaiZhiXing = (TextView) view.findViewById(R.id.zhengzai);
		mZhenZaiZhiXing.setText(dao.getPlanTasks(4, 3, null, "1",
				yesterday.getTime() + "", today.getTime() + "").size()
				+ "");

		mZhenChang = (TextView) view.findViewById(R.id.zhengchang);
		mZhenChang.setText(dao.getPlanTasks(4, 3, null, "3",
				yesterday.getTime() + "", today.getTime() + "").size()
				+ "");

//		mTiQian = (TextView) view.findViewById(R.id.tiqian);

		mYanWu = (TextView) view.findViewById(R.id.yanwu);
		mYanWu.setText(dao.getPlanTasks(4, 3, null, "2",
				yesterday.getTime() + "", today.getTime() + "").size()
				+ "");
		mQuXiao = (TextView) view.findViewById(R.id.quxiao);
		mQuXiao.setText(dao.getPlanTasks(4, 3, null, "4",
				yesterday.getTime() + "", today.getTime() + "").size()
				+ "");

		mTitleYesterday = (TextView) view.findViewById(R.id.title_yesterday);
		mTitleYesterday.setText("���죨"+dateFormat.format(theDayBeforeYesterday)+"��������");
		mWeiZhiXingYesterday = (TextView) view
				.findViewById(R.id.weizhixin_yesterday);
		mWeiZhiXingYesterday.setText(dao.getPlanTasks(4, 3, null, "0",
				theDayBeforeYesterday.getTime() + "", yesterday.getTime() + "")
				.size()
				+ "");
		mZhenZaiZhiXingYesterday = (TextView) view
				.findViewById(R.id.zhengzai_yesterday);
		mZhenZaiZhiXingYesterday.setText(dao.getPlanTasks(4, 3, null, "1",
				theDayBeforeYesterday.getTime() + "", yesterday.getTime() + "")
				.size()
				+ "");
		mZhenChangYesterday = (TextView) view
				.findViewById(R.id.zhengchang_yesterday);
		mZhenChangYesterday.setText(dao.getPlanTasks(4, 3, null, "3",
				theDayBeforeYesterday.getTime() + "", yesterday.getTime() + "")
				.size()
				+ "");
//		mTiQianYesterday = (TextView) view.findViewById(R.id.tiqian_yesterday);
		mYanWuYesterday = (TextView) view.findViewById(R.id.yanwu_yesterday);
		mYanWuYesterday.setText(dao.getPlanTasks(4, 3, null, "2",
				theDayBeforeYesterday.getTime() + "", yesterday.getTime() + "")
				.size()
				+ "");
		mQuXiaoYesterday = (TextView) view.findViewById(R.id.quxiao_yesterday);
		mQuXiaoYesterday.setText(dao.getPlanTasks(4, 3, null, "4",
				theDayBeforeYesterday.getTime() + "", yesterday.getTime() + "")
				.size()
				+ "");

		mQuXiaoYesterday.setOnClickListener(this);
		mYanWuYesterday.setOnClickListener(this);
		mZhenChangYesterday.setOnClickListener(this);
		mZhenZaiZhiXingYesterday.setOnClickListener(this);
		mWeiZhiXingYesterday.setOnClickListener(this);
		mQuXiao.setOnClickListener(this);
		mYanWu.setOnClickListener(this);
		mZhenChang.setOnClickListener(this);
		mZhenZaiZhiXing.setOnClickListener(this);
		mWeiZhiXing.setOnClickListener(this);
		
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent(context, TaskList.class);
		switch (v.getId()) {
		case R.id.quxiao_yesterday:
			// 4 ��ʾ���� 5��ʾ����
			intent.putExtra("enterType", 5);
			// status 0,�µ�����1,ִ���У�2,����3,��ɣ�4,ȡ������ null�򲻲�ѯ���ֶ�
			intent.putExtra("statue", "4");
			break;
			
		case R.id.weizhixin_yesterday:
			intent.putExtra("enterType", 5);
			intent.putExtra("statue", "0");
			break;
			
		case R.id.zhengzai_yesterday:
			intent.putExtra("enterType", 5);
			intent.putExtra("statue", "1");
			break;
			
		case R.id.yanwu_yesterday:
			intent.putExtra("enterType", 5);
			intent.putExtra("statue", "2");
			break;
			
		case R.id.zhengchang_yesterday:
			intent.putExtra("enterType", 5);
			intent.putExtra("statue", "3");
			break;
			
		case R.id.quxiao:
			// 4 ��ʾ���� 5��ʾ����
			intent.putExtra("enterType", 4);
			// status 0,�µ�����1,ִ���У�2,����3,��ɣ�4,ȡ������ null�򲻲�ѯ���ֶ�
			intent.putExtra("statue", "4");
			break;
			
		case R.id.weizhixin:
			intent.putExtra("enterType", 4);
			intent.putExtra("statue", "0");
			break;
			
		case R.id.zhengzai:
			intent.putExtra("enterType", 4);
			intent.putExtra("statue", "1");
			break;
			
		case R.id.yanwu:
			intent.putExtra("enterType", 4);
			intent.putExtra("statue", "2");
			break;
			
		case R.id.zhengchang:
			intent.putExtra("enterType", 4);
			intent.putExtra("statue", "3");
			break;

		}
		context.startActivity(intent);
	}
}
