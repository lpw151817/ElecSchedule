package nercms.schedule.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.SubMenu;
import com.nercms.Push;
import com.viewpagerindicator.TabPageIndicator;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.request.Contants;
import android.wxapp.service.request.WebRequestManager;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.MySharedPreference;
import nercms.schedule.R;
import nercms.schedule.adapter.MainFragmentAdapter;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.SDCardService;
import nercms.schedule.utils.Utils;

/**
 * @author jiaocuina@gmail.com
 * @date 2014-03-02
 * @version V1.0
 * @description ������� ����actionBar��Fragment�ĵ���
 */

public class Main extends SherlockFragmentActivity {
	private View mainActionBarView;// ͷ��action bar�ռ�
	private FragmentPagerAdapter adapter;// fragment������

	private WebRequestManager webRequestManager;

	// 2014-6-17
	// �Ƿ�����״̬
	// ���SD��δ���ػ��Ƴ��������������״̬
	private boolean isRestricted = false;

	private Handler handler;

	private TextView netStatusTv;

	// // 2014-6-25 WeiHao
	// private DAOFactory daoFactory;
	// private PersonDao personDao;

	private String userID;

	// private QuerySuggestionsAdapter mSuggestionsAdapter;
	// private static final String[] COLUMNS = { BaseColumns._ID,
	// SearchManager.SUGGEST_COLUMN_TEXT_1, };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.i("Main", "On Create");
		userID = MySharedPreference.get(Main.this, MySharedPreference.USER_ID, null);

		webRequestManager = new WebRequestManager(AppApplication.getInstance(), Main.this);
		// daoFactory = DAOFactory.getInstance();
		// personDao = daoFactory.getPersonDao(Main.this);
		DisplayMetrics displayMetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		Utils.Constant.displayWidth = displayMetrics.widthPixels;
		Utils.Constant.displayHeight = displayMetrics.heightPixels;

		initActionBar();// ���������� ��ʼ��action bar
		initView();
		initHandler();

		// SD���жϲ���ʾ
		if (Utils.isExistSDCard()) {
			if (Utils.getSDFreeSize() < 200) { // �洢�ռ�С��200MB
				Toast.makeText(Main.this, "����SD���洢�ռ䲻�㣬�뼰ʱ��������������ޣ�", Toast.LENGTH_SHORT).show();
				isRestricted = false;
			} else { // SD�������ҿռ��㹻������Ŀ¼׼��
				// ����һ���ļ��ж��󣬸�ֵΪ�ⲿ�洢����Ŀ¼
				File sdcardDir = Environment.getExternalStorageDirectory();
				// �õ�һ��·����������sdcard�ĸ���·��
				String path1 = sdcardDir.getPath() + "/nercms-Schedule/Attachments";
				File filePath1 = new File(path1);
				// �õ�һ��·����������sdcard�ĸ�������ͼ·��
				String path2 = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail";
				File filePath2 = new File(path2);

				if (!filePath1.exists()) {
					// �������ڣ�����Ŀ¼
					filePath1.mkdirs();
				}

				if (!filePath2.exists()) {
					filePath2.mkdirs();
				}
			}
		} else {
			Toast.makeText(Main.this, "δ��⵽SD�������������ޣ�", Toast.LENGTH_SHORT).show();
			isRestricted = false;
		}

		// ����SD����������
		startService(new Intent(Main.this, SDCardService.class));

		// // 2014-6-24 WeiHao ��¼֮�󣬻�ȡ�����񡢷�������Ϣ��
		// // //////TODO ��Ҫ�����������
		// webRequestManager.getAffairUpdate("", "");
		// // /////�ӿ� δʵ��
		// webRequestManager.getFeedbackUpdate();
		// // //////TODO ��Ҫ�����������
		// webRequestManager.getMessageUpdate("", "");
	}

	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case LocalConstant.SD_MOUNTED:
					Toast.makeText(Main.this, "SD���ѹ��أ������ָ���", Toast.LENGTH_SHORT).show();
					isRestricted = false;
					break;
				case LocalConstant.SD_UNMOUNTED:
					Toast.makeText(Main.this, "SD�����Ƴ������������ޣ�", Toast.LENGTH_SHORT).show();
					isRestricted = true;
					break;
				case LocalConstant.NET_AVAILABLE:
					Toast.makeText(Main.this, "��ǰ���磺" + msg.obj.toString(), Toast.LENGTH_SHORT)
							.show();
					// pingӦ�÷�����
					// if (Utils.serverPing()) {
					// netStatusTv.setText("");
					// } else {
					// netStatusTv.setText("(δ����)");
					// }

					break;
				case LocalConstant.NET_UNAVAILABLE:
					Toast.makeText(Main.this, "�޿�������", Toast.LENGTH_SHORT).show();
					netStatusTv.setText("(δ����)");
					break;

				case Constant.SHOW_MESSAGE_NOTIFICATION:
					// MessageModel message = (MessageModel) msg.obj;
					//
					// // 2014-7-30 WeiHao �߼��޸�
					// // �ж��յ����Ƿ���Ⱥ��Ϣ
					// // �����Ⱥ��Ϣ��Notification��ʾ�յ�����Ⱥ��������Ϣ
					// // ����Ǹ�����Ϣ��notification��ʾ�յ����Է����˵�����Ϣ
					// String showName = "";
					// String objectID = "";
					// if (String.valueOf(message.getReceiverID()).length() !=
					// 6) {
					// showName =
					// personDao.getOrgNodeByOrgID(String.valueOf(message.getReceiverID()))
					// .getDescription();
					// objectID = String.valueOf(message.getReceiverID());
					// } else {
					// showName =
					// personDao.getPersonNameByID(String.valueOf(message.getSenderID()));
					// objectID = String.valueOf(message.getSenderID());
					// }
					//
					// MyNotification.showMessageNotification(showName,
					// objectID, Main.this, new Intent(
					// Main.this, ChatDetail.class));
					break;
				case Constant.MQTT_NEW_MESSAGE:
					Log.i("Chat", "Chat�յ�����Ϣ");
					// //////TODO ��Ҫ�����������
					webRequestManager.getMessageUpdate("");
					break;
				case Constant.SHOW_TASK_NOTIFICATION:
					// AffairModel affair = (AffairModel) msg.obj;
					// String sponsorName =
					// personDao.getPersonNameByID(String.valueOf(affair
					// .getSponsorID()));
					// boolean isModify = false; // �Ƿ�Ϊ�޸�����
					// if (affair.getLastOperateType() == 4) {
					// isModify = true;
					// }
					// MyNotification.showAffairNotification(2,
					// affair.getStatus(), isModify, affair
					// .getAffairID(), sponsorName, affair.getTitle(),
					// Main.this, new Intent(
					// Main.this, TaskDetail.class));
					break;
				case Constant.SHOW_FEEDBACK_NOTIFICATION:
					// FeedbackModel fb = (FeedbackModel) msg.obj;
					// String senderName =
					// personDao.getPersonNameByID(String.valueOf(fb.getPersonID()));
					// MyNotification.showFeedbackNotification(senderName,
					// fb.getAffairID(), Main.this,
					// new Intent(Main.this, ChatDetail.class));
					break;

				case Constant.LOGOUT_REQUEST_SUCCESS:
					// ע��mqtt
					Push.get_instance(Main.this).PERSON_ID = MySharedPreference.get(Main.this,
							MySharedPreference.USER_ID, "");
					try {
						Push.get_instance(Main.this).release();
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(getApplicationContext(), "MQTT����ʧ��", Toast.LENGTH_LONG)
								.show();
					}
					// ֹͣ����
					stopService(new Intent(Main.this, SDCardService.class));
					// ��ת����¼����
					Intent intent9 = new Intent(Main.this, Login.class);
					intent9.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent9);
					Main.this.finish();
					break;
				case Constant.LOGOUT_REQUEST_FAIL:
					Log.e("Main", "logout fail");
					Toast.makeText(Main.this, "ע��ʧ��", Toast.LENGTH_SHORT).show();
					break;

				default:
					break;
				}
			}

		};

		// MessageHandlerManager.getInstance().register(handler,
		// LocalConstant.SD_MOUNTED, "Main");
		// MessageHandlerManager.getInstance().register(handler,
		// LocalConstant.SD_UNMOUNTED, "Main");
		// MessageHandlerManager.getInstance().register(handler,
		// LocalConstant.NET_AVAILABLE, "Main");
		// MessageHandlerManager.getInstance().register(handler,
		// LocalConstant.NET_UNAVAILABLE, "Main");
		//
		// MessageHandlerManager.getInstance().register(handler,
		// Constant.SHOW_TASK_NOTIFICATION, "Main");
		// MessageHandlerManager.getInstance()
		// .register(handler, Constant.SHOW_MESSAGE_NOTIFICATION, "Main");
		// MessageHandlerManager.getInstance().register(handler,
		// Constant.SHOW_FEEDBACK_NOTIFICATION,
		// "Main");
		// MessageHandlerManager.getInstance().register(handler,
		// Constant.MQTT_NEW_MESSAGE, "Main");

		MessageHandlerManager.getInstance().register(handler, Constant.LOGOUT_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_LOGOUT);
		MessageHandlerManager.getInstance().register(handler, Constant.LOGOUT_REQUEST_FAIL,
				Contants.METHOD_PERSON_LOGOUT);
	}

	@Override
	protected void onDestroy() {
		// ע������handler
		MessageHandlerManager.getInstance().unregister(Constant.LOGOUT_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_LOGOUT);
		MessageHandlerManager.getInstance().unregister(Constant.LOGOUT_REQUEST_FAIL,
				Contants.METHOD_PERSON_LOGOUT);
		super.onDestroy();
	}

	/*
	 * ��ʼ����ͼ
	 */
	private void initView() {
		netStatusTv = (TextView) findViewById(R.id.actionBarStatusText);
		adapter = new MainFragmentAdapter(getSupportFragmentManager(), this);
		ViewPager pager = (ViewPager) findViewById(R.id.pager);
		pager.setAdapter(adapter);
		TabPageIndicator indicator = (TabPageIndicator) findViewById(R.id.indicator);
		indicator.setViewPager(pager);

	}

	private void initActionBar() {
		// �����Զ���actionbar
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		// ����actionbar��ʾlogo
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		mainActionBarView = LayoutInflater.from(this).inflate(R.layout.main_action_bar, null);
		getSupportActionBar().setCustomView(mainActionBarView);
	}

	/*
	 * ����ѡ��˵�
	 */
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

		// ������ť�����㷨
		// createSearchItem(menu);

		// // ������ť ����
		// MenuItem search = menu.add(0, -1, 0, "search");
		// search.setIcon(R.drawable.abs__ic_search);
		// search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		// �Ӳ˵� ������������Ϣ�����飬��Ƶֱ�������ܵ��ȣ���ӿͻ�
		SubMenu addMenu = menu.addSubMenu("add item");

		addMenu.add(0, 1, 0, "��������").setIcon(R.drawable.ofm_task_icon);
		addMenu.add(0, 2, 0, "������Ϣ").setIcon(R.drawable.ofm_group_chat_icon);
		addMenu.add(0, 3, 0, "�������").setIcon(R.drawable.ofm_meeting_icon);
		// addMenu.add(0, 4, 0, "��Ƶֱ��").setIcon(R.drawable.ofm_broadcast_icon);
		// addMenu.add(0, 5, 0, "���ܵ���").setIcon(R.drawable.ofm_schedule_icon);
		// addMenu.add(0, 6, 0, "��ӿͻ�").setIcon(R.drawable.ofm_add_icon);

		MenuItem addItem = addMenu.getItem();
		addItem.setIcon(R.drawable.ic_action_new);
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);// ������ΪAction����ʾ

		// �Ӳ˵� �����ã�ע�����˳�,����ҳ��
		SubMenu subMenu = menu.addSubMenu("action item");

		subMenu.add(0, 7, 0, "�ҵ�����").setIcon(R.drawable.ofm_me_icon);
		subMenu.add(0, 8, 0, "����").setIcon(R.drawable.ofm_setting_icon);
		subMenu.add(0, 9, 0, "ע��").setIcon(R.drawable.ofm_log_off_icon);
		subMenu.add(0, 10, 0, "�˳�").setIcon(R.drawable.ofm_log_out_icon);
		// subMenu.add(0, 11, 1, "Call").setIcon(R.drawable.ofm_task_icon);

		MenuItem menuItem = subMenu.getItem();
		menuItem.setIcon(R.drawable.ic_action_overflow);
		menuItem.setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * ���ѡ��˵� ������Ӧ��ת
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		if (isRestricted) {
			Toast.makeText(Main.this, "SD��δ���أ����ƽ��д˲���", Toast.LENGTH_SHORT);
		} else {

			switch (item.getItemId()) {
			// ����
			case -1:
				Intent intent = new Intent(Main.this, SearchActivity.class);
				Main.this.startActivity(intent);
				break;
			case 1:
				// ��������
				Intent intent1 = new Intent(this, TaskAdd.class);
				intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// �ٴε�����˵�ʱ��������ý��̿ռ�������Ӳ˵�Activity��
				startActivity(intent1);
				return true;
			case 2:
				// ������Ϣ
				Intent intent2 = new Intent(this, ContactSelect.class);
				intent2.putExtra("entrance_flag", 2);

				intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(intent2);
				return true;
			case 3:
				// �������
				Intent intent3 = new Intent(this, MeetingAdd.class);

				intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(intent3);

				return true;
			case 4:
				// ��Ƶֱ��
				break;
			case 5:
				// ComponentName comp = new
				// ComponentName("com.nercms.schedule.External_Convene_Schedule","com.nercms.schedule.External_Start_Schedule");
				// Intent police_schedule = new Intent();
				// police_schedule.setComponent(comp);
				// police_schedule.setAction("android.intent.action.MAIN");
				// startActivity(police_schedule);
				// return true;
				break;
			case 6:
				// ��ӿͻ�
				Intent intent6 = new Intent(this, ContactAdd.class);

				intent6.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent6.putExtra("entrance_type", 1);

				startActivity(intent6);

				return true;

			case 7:
				// ��������
				Intent intent8 = new Intent(Main.this, Profile.class);
				startActivity(intent8);
				break;

			case 8:
				// ����ҳ��
				startActivity(new Intent(Main.this, Setting.class));
				break;
			case 9:
				// ע��ҳ��

				webRequestManager.logOut();
				return true;
			case 10:
				// �����˳�����
				showExitSelection();
				return true;
			case 11:
				// 2014-8-5
				// 2014-8-7
				ArrayList<String> speakerIDList = new ArrayList<String>(
						Arrays.asList("100011", "100013", "100015", "100016"));
				ArrayList<String> speakerNameList = new ArrayList<String>(
						Arrays.asList("�Զ�һ", "�Զ���", "�Զ���", "�Զ���"));
				ArrayList<String> participatorIDList = new ArrayList<String>(
						Arrays.asList("100012", "100014"));
				ArrayList<String> participatorNameList = new ArrayList<String>(
						Arrays.asList("�Զ���", "�Զ���", "�Զ���", "�Զ���", "�Զ���", "�Զ�ʮ", "�Զ�11", "�Զ�12",
								"�Զ�13", "�Զ�13", "�Զ�13", "�Զ�13", "�Զ�13", "�Զ�13", "�Զ�13", "�Զ�13"));
				// ArrayList<String> participatorNameList = new
				// ArrayList<String>(
				// Arrays.asList("�Զ���"));
				Intent intent11 = new Intent(Main.this, VoiceMeeting.class);
				intent11.putExtra("call_type", 1);
				intent11.putExtra("speaker_id_list", speakerIDList);
				intent11.putExtra("speaker_name_list", speakerNameList);
				intent11.putExtra("participator_id_list", participatorIDList);
				intent11.putExtra("participator_name_list", participatorNameList);
				startActivity(intent11);
				break;

			default:
				break;
			// return super.onOptionsItemSelected(item);
			}
		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			showExitSelection();
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showExitSelection() {
		new AlertDialog.Builder(Main.this).setTitle("�˳�����").setMessage("�˳�������Ƿ������������Ϣ���ѣ�")
				.setPositiveButton("����", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// ģ��Home�����������̨����
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.addCategory(Intent.CATEGORY_HOME);
						Main.this.startActivity(intent);
					}
				}).setNegativeButton("������", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// ע��mqtt
						Push.get_instance(Main.this).PERSON_ID = MySharedPreference.get(Main.this,
								MySharedPreference.USER_ID, "");
						try {
							Push.get_instance(Main.this).release();
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(getApplicationContext(), "MQTT����ʧ��", Toast.LENGTH_LONG)
									.show();
						}
						// ֹͣ����
						stopService(new Intent(Main.this, SDCardService.class));
						// ɱ�����̣������˳�
						android.os.Process.killProcess(android.os.Process.myPid()); // ��ȡPID
						System.exit(0); // ����java��c#�ı�׼�˳���������ֵΪ0���������˳�
					}
				}).create().show();
	}

	// public void show_task_list(View v) { // �������ҳ�棺��ʾ�����б�
	// Intent intent = new Intent();
	// intent.setClass(Main.this, TaskList.class);
	// startActivity(intent);
	// }
	//
	// public void startchat(View v) { // �����Ϣҳ�棺��ʾС�ڶԻ�����
	// Intent intent = new Intent(Main.this, ChatDetail.class);
	// startActivity(intent);
	// }
	//
	// public void show_phone_detail(View v) { // ����绰ҳ�棺��ʾ�绰�б�
	// Intent intent = new Intent();
	// intent.setClass(Main.this, PhoneDetail.class);
	// startActivity(intent);
	// }
	//
	// public void show_meeting_detail(View v) { // �������ҳ�棺��ʾ�����б�
	// Intent intent = new Intent();
	// intent.setClass(Main.this, MeetingDetail.class);
	// startActivity(intent);
	// }

	/*
	 * private void createSearchItem(com.actionbarsherlock.view.Menu menu) {
	 * 
	 * SearchView searchView = new
	 * SearchView(getSupportActionBar().getThemedContext());
	 * searchView.setQueryHint("Search for countries��");
	 * searchView.setOnQueryTextListener(this);
	 * searchView.setOnSuggestionListener(this);
	 * 
	 * if (mSuggestionsAdapter == null) { MatrixCursor cursor = new
	 * MatrixCursor(COLUMNS); cursor.addRow(new String[] { "1", "'Murica" });
	 * cursor.addRow(new String[] { "2", "Canada" }); cursor.addRow(new String[]
	 * { "3", "Denmark" }); mSuggestionsAdapter = new
	 * QuerySuggestionsAdapter(getSupportActionBar().getThemedContext(),
	 * cursor); }
	 * 
	 * searchView.setSuggestionsAdapter(mSuggestionsAdapter);
	 * 
	 * MenuItem searchItem = menu.add(0, 0, 0, "search");
	 * searchItem.setIcon(R.drawable.abs__ic_search);
	 * searchItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM |
	 * MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
	 * searchItem.setActionView(searchView); }
	 *//***
		 * implements SearchView.OnQueryTextListener
		 */
	/*
	 * public boolean onQueryTextSubmit(String query) {
	 * ToastUtil.showShortToast(this, "You searched for: " + query); return
	 * true; }
	 *//***
		 * implements SearchView.OnQueryTextListener
		 */
	/*
	 * public boolean onQueryTextChange(String newText) { return false; }
	 *//**
		 * implements SearchView.OnSuggestionListener
		 */
	/*
	 * public boolean onSuggestionSelect(int position) { return false; }
	 *//**
		 * implements SearchView.OnSuggestionListener
		 */
	/*
	 * public boolean onSuggestionClick(int position) { Cursor c = (Cursor)
	 * mSuggestionsAdapter.getItem(position); String query =
	 * c.getString(c.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));
	 * ToastUtil.showShortToast(this, "Suggestion clicked: " + query); return
	 * true; }
	 */

}
