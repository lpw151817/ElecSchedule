package nercms.schedule.fragment;

/*
 * *
 * �и���ʱ���ֶ�Ӧ�仯
 * ��������״̬�ı�ʱ��ͳ�����ݸı�
 * 
 */

import java.util.ArrayList;

import nercms.schedule.R;
import nercms.schedule.activity.TaskList;
import nercms.schedule.utils.LocalConstant;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.AffairDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.model.AffairModel;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;

import com.actionbarsherlock.app.SherlockFragment;

public class Task extends SherlockFragment implements OnClickListener {

	private static final String TAG = "TaskFragment";

	// TaskFragmentҳ���пؼ�
	private TextView start_delayed, start_doing, start_finished, rcv_delayed, rcv_doing, rcv_finished;
	private ImageView stt_delayed_tips, stt_doing_tips, stt_finished_tips, rcv_delayed_tips,
			rcv_doing_tips, rcv_finished_tips;

	// ���ݻ�ȡ
	private static DAOFactory daoFactory = DAOFactory.getInstance();
	private AffairDao affairDao;
	public ArrayList<AffairModel> affairList;

	private Intent intent;

	private String userID;
	private int stDoingNum, stDelayNum, stFinishNum;
	private int rvDoingNum, rvDelayNum, rvFinishNum;
	private boolean stDoingNew, stDelayNew, stFinishNew;
	private boolean rvDoingNew, rvDelayNew, rvFinishNew;

	private static Task taskFragment = null;

	private Handler handler;

	private WebRequestManager webRequestManager;

	public static Task newInstance() {
		Task taskFragment = new Task();
		return taskFragment;
	}

	// ����
	// public static Task getInstance(){
	// if (taskFragment == null) {
	// synchronized (Task.class) {
	// if (taskFragment == null)
	// taskFragment = new Task();
	// }
	// }
	// return taskFragment;
	// }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.task_fragment, null);

		webRequestManager = new WebRequestManager(AppApplication.getInstance(), getActivity());

		initData();

		// 2014-04-28 ��6��ͳ���������ڿؼ� ���������
		// 2014-5-23 ���ָ�ֵ
		// 2014-7-7 ��6��δ����ʾ����־
		start_delayed = (TextView) v.findViewById(R.id.task_start_delayed_number);
		start_delayed.setOnClickListener(this);// �������ת����Ӧ�б�
		stt_delayed_tips = (ImageView) v.findViewById(R.id.task_start_delayed_tips);
		stt_delayed_tips.setVisibility(View.GONE);

		start_doing = (TextView) v.findViewById(R.id.task_start_doing_number);
		start_doing.setOnClickListener(this);
		stt_doing_tips = (ImageView) v.findViewById(R.id.task_start_doing_tips);
		stt_doing_tips.setVisibility(View.GONE);

		start_finished = (TextView) v.findViewById(R.id.task_start_finished_number);
		start_finished.setOnClickListener(this);
		stt_finished_tips = (ImageView) v.findViewById(R.id.task_start_finished_tips);
		stt_finished_tips.setVisibility(View.GONE);

		rcv_delayed = (TextView) v.findViewById(R.id.task_rcv_delayed_number);
		rcv_delayed.setOnClickListener(this);
		rcv_delayed_tips = (ImageView) v.findViewById(R.id.task_rcv_delayed_tips);
		rcv_delayed_tips.setVisibility(View.GONE);

		rcv_doing = (TextView) v.findViewById(R.id.task_rcv_doing_number);
		rcv_doing.setOnClickListener(this);
		rcv_doing_tips = (ImageView) v.findViewById(R.id.task_rcv_doing_tips);
		rcv_doing_tips.setVisibility(View.GONE);

		rcv_finished = (TextView) v.findViewById(R.id.task_rcv_finished_number);
		rcv_finished.setOnClickListener(this);
		rcv_finished_tips = (ImageView) v.findViewById(R.id.task_rcv_finished_tips);
		rcv_finished_tips.setVisibility(View.GONE);

		resetNumber();

		// ����Ϣ���ѿؼ�viewbadger��library�� ������

		// application = (AppApplication) getActivity().getApplication();
		// webRequestManager = new WebRequestManager(application,
		// getActivity());///////////////////
		//
		// Log.d(TAG, "����ͳ��");
		// webRequestManager.getAffairUpdate();
		// Log.d(TAG, "����ͳ�����");

		initHandler();

		return v;
	}

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.MQTT_NEW_TASK:
					Log.i("Task", "�յ�mqtt�������");

					// //////TODO ������Ҫ����
					webRequestManager.getAffairUpdate("");
					initData();
					resetNumber();
					break;
				case Constant.MQTT_NEW_FEEDBACK:
					Log.i("Task", "�յ�mqtt�·�������ʼ������ȡ");
					webRequestManager.getFeedbackUpdate();
					break;
				case Constant.SAVE_TASK_SUCCESS:
					Log.i("Task", "�����񱣴���ɣ�ˢ��ҳ����ʾ");
					initData();
					resetNumber();
					break;
				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler, Constant.MQTT_NEW_TASK, "Task");
		MessageHandlerManager.getInstance().register(handler, Constant.MQTT_NEW_FEEDBACK, "Task");
		MessageHandlerManager.getInstance().register(handler, Constant.SAVE_TASK_SUCCESS, "Task");
	}

	// 2014-5-23 WeiHao
	// ׼��������������
	public void initData() {
		userID = MySharedPreference.get(getActivity(), MySharedPreference.USER_ID, null);
		affairDao = daoFactory.getAffairDao(getActivity());

		stDoingNum = affairDao.getAffairCountByTypeAndStatus(LocalConstant.START_TASK_TYPE,
				LocalConstant.DOING_TASK_STATUS, userID);
		stFinishNum = affairDao.getAffairCountByTypeAndStatus(LocalConstant.START_TASK_TYPE,
				LocalConstant.FINISHED_TASK_STATUS, userID);
		stDelayNum = affairDao.getAffairCountByTypeAndStatus(LocalConstant.START_TASK_TYPE,
				LocalConstant.DELAYED_TASK_STATUS, userID);

		rvDoingNum = affairDao.getAffairCountByTypeAndStatus(LocalConstant.RECEIVE_TASK_TYPE,
				LocalConstant.DOING_TASK_STATUS, userID);
		rvFinishNum = affairDao.getAffairCountByTypeAndStatus(LocalConstant.RECEIVE_TASK_TYPE,
				LocalConstant.FINISHED_TASK_STATUS, userID);
		rvDelayNum = affairDao.getAffairCountByTypeAndStatus(LocalConstant.RECEIVE_TASK_TYPE,
				LocalConstant.DELAYED_TASK_STATUS, userID);
		
		if (affairDao.getUnreadNumByTypeAndStatus(LocalConstant.START_TASK_TYPE,
				LocalConstant.DOING_TASK_STATUS, userID) > 0) {
			stDoingNew = true;
		} else {
			stDoingNew = false;
		}
		if (affairDao.getUnreadNumByTypeAndStatus(LocalConstant.START_TASK_TYPE,
				LocalConstant.FINISHED_TASK_STATUS, userID) > 0) {
			stFinishNew = true;
		} else {
			stFinishNew = false;
		}
		if (affairDao.getUnreadNumByTypeAndStatus(LocalConstant.START_TASK_TYPE,
				LocalConstant.DELAYED_TASK_STATUS, userID) > 0) {
			stDelayNew = true;
		} else {
			stDelayNew = false;
		}
		if (affairDao.getUnreadNumByTypeAndStatus(LocalConstant.RECEIVE_TASK_TYPE,
				LocalConstant.DOING_TASK_STATUS, userID) > 0) {
			rvDoingNew = true;
		} else {
			rvDoingNew = false;
		}
		if (affairDao.getUnreadNumByTypeAndStatus(LocalConstant.RECEIVE_TASK_TYPE,
				LocalConstant.FINISHED_TASK_STATUS, userID) > 0) {
			rvFinishNew = true;
		} else {
			rvFinishNew = false;
		}
		if (affairDao.getUnreadNumByTypeAndStatus(LocalConstant.RECEIVE_TASK_TYPE,
				LocalConstant.DELAYED_TASK_STATUS, userID) > 0) {
			rvDelayNew = true;
		} else {
			rvDelayNew = false;
		}

	}

	// ������ʾ����
	public void resetNumber() {
		start_delayed.setText(String.valueOf(stDelayNum));
		start_doing.setText(String.valueOf(stDoingNum));
		start_finished.setText(String.valueOf(stFinishNum));
		rcv_delayed.setText(String.valueOf(rvDelayNum));
		rcv_doing.setText(String.valueOf(rvDoingNum));
		rcv_finished.setText(String.valueOf(rvFinishNum));

		if (stDelayNew) {
			stt_delayed_tips.setVisibility(View.VISIBLE);
		} else {
			stt_delayed_tips.setVisibility(View.GONE);
		}
		if (stDoingNew) {
			stt_doing_tips.setVisibility(View.VISIBLE);
		} else {
			stt_doing_tips.setVisibility(View.GONE);
		}
		if (stFinishNew) {
			stt_finished_tips.setVisibility(View.VISIBLE);
		} else {
			stt_finished_tips.setVisibility(View.GONE);
		}
		if (rvDelayNew) {
			rcv_delayed_tips.setVisibility(View.VISIBLE);
		} else {
			rcv_delayed_tips.setVisibility(View.GONE);
		}
		if (rvDoingNew) {
			rcv_doing_tips.setVisibility(View.VISIBLE);
		} else {
			rcv_doing_tips.setVisibility(View.GONE);
		}
		if (rvFinishNew) {
			rcv_finished_tips.setVisibility(View.VISIBLE);
		} else {
			rcv_finished_tips.setVisibility(View.GONE);
		}
	}

	// 2014-5-22 WeiHao
	// type: 1-1-��������2-��������
	// status: 1-�����У�δ��ɣ���2-����ɣ�3-���ӳ�
	@Override
	public void onClick(View v) {
		intent = new Intent(getActivity(), TaskList.class);
		switch (v.getId()) {
		case R.id.task_start_doing_number:
			intent.putExtra("type", 1);
			intent.putExtra("status", 1);
			startActivity(intent);
			break;
		case R.id.task_start_finished_number:
			intent.putExtra("type", 1);
			intent.putExtra("status", 2);
			startActivity(intent);
			break;
		case R.id.task_start_delayed_number:
			intent.putExtra("type", 1);
			intent.putExtra("status", 3);
			startActivity(intent);
			break;
		case R.id.task_rcv_doing_number:
			intent.putExtra("type", 2);
			intent.putExtra("status", 1);
			startActivity(intent);
			break;
		case R.id.task_rcv_finished_number:
			intent.putExtra("type", 2);
			intent.putExtra("status", 2);
			startActivity(intent);
			break;
		case R.id.task_rcv_delayed_number:
			intent.putExtra("type", 2);
			intent.putExtra("status", 3);
			startActivity(intent);
			break;

		default:
			break;
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		initData();
		resetNumber();
	}

}
