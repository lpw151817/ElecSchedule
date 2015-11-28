package nercms.schedule.activity;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ImageView;
import android.widget.TextView;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.ConferenceDao;
import android.wxapp.service.dao.ConferencePersonDao;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.conference.ConferenceUpdateQueryResponseItem;
import android.wxapp.service.jerry.model.conference.ConferenceUpdateQueryResponseRids;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.model.ConferenceModel;
import android.wxapp.service.model.ConferencePersonModel;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class MeetingDetail extends BaseActivity {

	private ImageView iconIv;
	private TextView titleTv;// ��������
	private TextView sponsorTv;// ���鷢����
	private TextView typeTv;// ��������
	private TextView statusTv;
	private TextView speakerTv; // ������
	private TextView participatorTv;// ���������
	private TextView timeTv;// ����ʱ��

	private String conferenceID;
	private PersonDao personDao;
	private ConferenceDao conferenceDao;

	private WebRequestManager manager;
	private Handler handler;

	ConferenceUpdateQueryResponseItem data;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meeting_detail);

		personDao = new PersonDao(MeetingDetail.this);
		conferenceDao = new ConferenceDao(MeetingDetail.this);

		conferenceID = getIntent().getExtras().getString("conference_id");

		iniParams();

	}

	void iniParams() {
		initView();
		initData();
		initActionBar();
	}

	private void iniHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				switch (msg.what) {
				case Constant.CONFERENCE_QUERY_SECCUESS:
					dismissProgressDialog();
					MeetingDetail.this.data = (ConferenceUpdateQueryResponseItem) msg.obj;
					iniParams();
					break;

				case Constant.CONFERENCE_QUERY_FAIL:
					dismissProgressDialog();
					showLongToast("������룺" + ((NormalServerResponse) msg.obj).getEc());
					break;
				}
			}

		};
		MessageHandlerManager.getInstance().register(handler, Constant.CONFERENCE_QUERY_SECCUESS,
				Contants.METHOD_CONFERENCE_QUERY);
		MessageHandlerManager.getInstance().register(handler, Constant.CONFERENCE_QUERY_FAIL,
				Contants.METHOD_CONFERENCE_QUERY);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		MessageHandlerManager.getInstance().unregister(Constant.CONFERENCE_QUERY_SECCUESS,
				Contants.METHOD_CONFERENCE_QUERY);
		MessageHandlerManager.getInstance().unregister(Constant.CONFERENCE_QUERY_FAIL,
				Contants.METHOD_CONFERENCE_QUERY);
	}

	private void initView() {
		iconIv = (ImageView) findViewById(R.id.meeting_detail_icon_iv);
		titleTv = (TextView) findViewById(R.id.meeting_detail_title_tv);
		sponsorTv = (TextView) findViewById(R.id.meeting_detail_sponsor_tv);
		typeTv = (TextView) findViewById(R.id.meeting_detail_type_tv);
		statusTv = (TextView) findViewById(R.id.meeting_detail_status_tv);
		timeTv = (TextView) findViewById(R.id.meeting_detail_time_tv);
		participatorTv = (TextView) findViewById(R.id.meeting_detail_participator_tv);
		speakerTv = (TextView) findViewById(R.id.meeting_detail_speaker_tv);
	}

	private void initData() {

		data = conferenceDao.getConferenceByCid(conferenceID);

		// ����������
		String sponsorName = personDao.getPersonInfo(data.getSid()).getN();
		sponsorTv.setText(sponsorName);
		// ��������
		titleTv.setText(data.getN());

		// �����вλ���Ա�б��У���ȡ���������б�Ͳ����ߣ����ڣ��б�
		String tempSpeaker = "";
		String tempListener = "";
		List<ConferenceUpdateQueryResponseRids> rids = data.getRids();
		for (ConferenceUpdateQueryResponseRids item : rids) {
			if (item.getT().equals("1")) {// ������
				tempSpeaker += (personDao.getPersonInfo(item.getRid()).getN() + "/");
			} else if (item.getT().equals("2")) {// ������
				tempListener += (personDao.getPersonInfo(item.getRid()).getN() + "/");
			}
		}
		speakerTv.setText(tempSpeaker);
		participatorTv.setText(tempListener);
		typeTv.setText("ԤԼ����");
		if (Long.parseLong(data.getCt()) > System.currentTimeMillis()) {
			statusTv.setText("��ԤԼ���ȴ���ʼ");
			iconIv.setImageResource(R.drawable.meeting_clock);
		} else {
			statusTv.setText("�ѽ���");
			iconIv.setImageResource(R.drawable.meeting_over);
		}
		timeTv.setText(Utils.formatDateMs(data.getCt()));

	}

	// ����actionbar����
	private void initActionBar() {
		getSupportActionBar().setDisplayShowCustomEnabled(false);
		getSupportActionBar().setDisplayShowTitleEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle("��������");
	}

	// ���水ť���ʱ �ж�����+���ݱ��浽����
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			// ���������ҳ
			finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

}
