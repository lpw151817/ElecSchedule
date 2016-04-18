package nercms.schedule.fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nercms.schedule.R;
import nercms.schedule.activity.TaskList;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

	private ScheduledExecutorService service;
	int delayedTime = 1000;// 延时1s
	protected static final int UPDATE_CONTENT = 789;

	PlanTaskDao dao;

	public FourthFragment() {
	}

	public FourthFragment(Context _ctx) {
		context = _ctx;
		if (null == dao)
			dao = new PlanTaskDao(_ctx);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		view = inflater.inflate(R.layout.activity_sum, container, false);
		return view;
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == UPDATE_CONTENT) {

				MyData mdata = (MyData) msg.obj;

				if (mWeiZhiXing != null && mZhenZaiZhiXing != null
						&& mZhenChang != null && mYanWu != null
						&& mQuXiao != null && mWeiZhiXingYesterday != null
						&& mZhenZaiZhiXingYesterday != null
						&& mZhenChangYesterday != null
						&& mYanWuYesterday != null && mQuXiaoYesterday != null && mdata != null) {
					mWeiZhiXing.setText(mdata.get_weizhixing()+"");
					mZhenZaiZhiXing.setText(mdata.get_zhengzai()+"");
					mZhenChang.setText(mdata.get_zhengchang()+"");
					mYanWu.setText(mdata.get_yanwu()+"");
					mQuXiao.setText(mdata.get_quxiao()+"");

					mWeiZhiXingYesterday.setText(mdata
							.get_weizhixingyesterday()+"");
					mZhenZaiZhiXingYesterday.setText(mdata
							.get_zhengzaiyesterday()+"");
					mZhenChangYesterday
							.setText(mdata.get_zhengchangyesterday()+"");
					mYanWuYesterday.setText(mdata.get_yanwuyesterday()+"");
					mQuXiaoYesterday.setText(mdata.get_quxiaoyesterday()+"");
				}
			}
		};
	};

	private MyData data = new MyData();

	@Override
	public void onResume() {
		initView(view);

		Runnable thread = new Runnable() {

			@Override
			public void run() {
				if (null == dao)
					dao = new PlanTaskDao(context);

				SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日");

				Date tmp = new Date(System.currentTimeMillis());
				Date today = new Date(tmp.getYear(), tmp.getMonth(),
						tmp.getDate() + 1);
				Date yesterday = new Date(tmp.getYear(), tmp.getMonth(),
						tmp.getDate());
				Date theDayBeforeYesterday = new Date(tmp.getYear(),
						tmp.getMonth(), tmp.getDate() - 1);

				data.set_weizhixing(dao.getPlanTasks(4, 3, null, "0",
						yesterday.getTime() + "", today.getTime() + "").size());
				data.set_quxiao(dao.getPlanTasks(4, 3, null, "4",
						yesterday.getTime() + "", today.getTime() + "").size());
				data.set_yanwu(dao.getPlanTasks(4, 3, null, "2",
						yesterday.getTime() + "", today.getTime() + "").size());
				data.set_zhengzai(dao.getPlanTasks(4, 3, null, "1",
						yesterday.getTime() + "", today.getTime() + "").size());
				data.set_zhengchang(dao.getPlanTasks(4, 3, null, "3",
						yesterday.getTime() + "", today.getTime() + "").size());

				data.set_weizhixingyesterday(dao.getPlanTasks(4, 3, null, "0",
						theDayBeforeYesterday.getTime() + "",
						yesterday.getTime() + "").size());
				data.set_quxiaoyesterday(dao.getPlanTasks(4, 3, null, "4",
						theDayBeforeYesterday.getTime() + "",
						yesterday.getTime() + "").size());
				data.set_yanwuyesterday(dao.getPlanTasks(4, 3, null, "2",
						theDayBeforeYesterday.getTime() + "",
						yesterday.getTime() + "").size());
				data.set_zhengzaiyesterday(dao.getPlanTasks(4, 3, null, "1",
						theDayBeforeYesterday.getTime() + "",
						yesterday.getTime() + "").size());
				data.set_zhengchangyesterday(dao.getPlanTasks(4, 3, null, "3",
						theDayBeforeYesterday.getTime() + "",
						yesterday.getTime() + "").size());

				Message msg = new Message();
				msg.obj = data;
				msg.what = UPDATE_CONTENT;
				handler.sendMessage(msg);
			}
		};

		service = Executors.newScheduledThreadPool(1);
		service.scheduleAtFixedRate(thread, 100, delayedTime,
				TimeUnit.MILLISECONDS);
		super.onResume();
	}

	private void initView(View view) {
		if (null == dao)
			dao = new PlanTaskDao(context);

		SimpleDateFormat dateFormat = new SimpleDateFormat("MM月dd日");

		Date tmp = new Date(System.currentTimeMillis());
		Date today = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate() + 1);
		Date yesterday = new Date(tmp.getYear(), tmp.getMonth(), tmp.getDate());
		Date theDayBeforeYesterday = new Date(tmp.getYear(), tmp.getMonth(),
				tmp.getDate() - 1);

		mTitle = (TextView) view.findViewById(R.id.title_today);
		mTitle.setText("今天（" + dateFormat.format(yesterday) + "）的任务");

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

		// mTiQian = (TextView) view.findViewById(R.id.tiqian);

		mYanWu = (TextView) view.findViewById(R.id.yanwu);
		mYanWu.setText(dao.getPlanTasks(4, 3, null, "2",
				yesterday.getTime() + "", today.getTime() + "").size()
				+ "");
		mQuXiao = (TextView) view.findViewById(R.id.quxiao);
		mQuXiao.setText(dao.getPlanTasks(4, 3, null, "4",
				yesterday.getTime() + "", today.getTime() + "").size()
				+ "");

		mTitleYesterday = (TextView) view.findViewById(R.id.title_yesterday);
		mTitleYesterday.setText("昨天（"
				+ dateFormat.format(theDayBeforeYesterday) + "）的任务");
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
		// mTiQianYesterday = (TextView)
		// view.findViewById(R.id.tiqian_yesterday);
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
			// 4 表示今天 5表示昨天
			intent.putExtra("enterType", 5);
			// status 0,新的任务；1,执行中；2,延误；3,完成；4,取消任务。 null则不查询此字段
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
			// 4 表示今天 5表示昨天
			intent.putExtra("enterType", 4);
			// status 0,新的任务；1,执行中；2,延误；3,完成；4,取消任务。 null则不查询此字段
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

	class MyData {
		int _weizhixing;
		int _zhengzai;
		int _zhengchang;
		int _yanwu;
		int _quxiao;

		int _weizhixingyesterday;
		int _zhengzaiyesterday;
		int _zhengchangyesterday;
		int _yanwuyesterday;
		int _quxiaoyesterday;

		public int get_weizhixing() {
			return _weizhixing;
		}

		public void set_weizhixing(int _weizhixing) {
			this._weizhixing = _weizhixing;
		}

		public int get_zhengzai() {
			return _zhengzai;
		}

		public void set_zhengzai(int _zhengzai) {
			this._zhengzai = _zhengzai;
		}

		public int get_zhengchang() {
			return _zhengchang;
		}

		public void set_zhengchang(int _zhengchang) {
			this._zhengchang = _zhengchang;
		}

		public int get_yanwu() {
			return _yanwu;
		}

		public void set_yanwu(int _yanwu) {
			this._yanwu = _yanwu;
		}

		public int get_quxiao() {
			return _quxiao;
		}

		public void set_quxiao(int _quxiao) {
			this._quxiao = _quxiao;
		}

		public int get_weizhixingyesterday() {
			return _weizhixingyesterday;
		}

		public void set_weizhixingyesterday(int _weizhixingyesterday) {
			this._weizhixingyesterday = _weizhixingyesterday;
		}

		public int get_zhengzaiyesterday() {
			return _zhengzaiyesterday;
		}

		public void set_zhengzaiyesterday(int _zhengzaiyesterday) {
			this._zhengzaiyesterday = _zhengzaiyesterday;
		}

		public int get_zhengchangyesterday() {
			return _zhengchangyesterday;
		}

		public void set_zhengchangyesterday(int _zhengchangyesterday) {
			this._zhengchangyesterday = _zhengchangyesterday;
		}

		public int get_yanwuyesterday() {
			return _yanwuyesterday;
		}

		public void set_yanwuyesterday(int _yanwuyesterday) {
			this._yanwuyesterday = _yanwuyesterday;
		}

		public int get_quxiaoyesterday() {
			return _quxiaoyesterday;
		}

		public void set_quxiaoyesterday(int _quxiaoyesterday) {
			this._quxiaoyesterday = _quxiaoyesterday;
		}

	}
}
