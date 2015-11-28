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
 * @description 程序入口 控制actionBar和Fragment的调用
 */

public class Main extends SherlockFragmentActivity {
	private View mainActionBarView;// 头部action bar空间
	private FragmentPagerAdapter adapter;// fragment适配器

	private WebRequestManager webRequestManager;

	// 2014-6-17
	// 是否受限状态
	// 如果SD卡未挂载或被移除，程序进入受限状态
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

		initActionBar();// 调用依赖库 初始化action bar
		initView();
		initHandler();

		// SD卡判断并提示
		if (Utils.isExistSDCard()) {
			if (Utils.getSDFreeSize() < 200) { // 存储空间小于200MB
				Toast.makeText(Main.this, "您的SD卡存储空间不足，请及时清理，避免操作受限！", Toast.LENGTH_SHORT).show();
				isRestricted = false;
			} else { // SD卡挂载且空间足够，附件目录准备
				// 创建一个文件夹对象，赋值为外部存储器的目录
				File sdcardDir = Environment.getExternalStorageDirectory();
				// 得到一个路径，内容是sdcard的附件路径
				String path1 = sdcardDir.getPath() + "/nercms-Schedule/Attachments";
				File filePath1 = new File(path1);
				// 得到一个路径，内容是sdcard的附件缩略图路径
				String path2 = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail";
				File filePath2 = new File(path2);

				if (!filePath1.exists()) {
					// 若不存在，创建目录
					filePath1.mkdirs();
				}

				if (!filePath2.exists()) {
					filePath2.mkdirs();
				}
			}
		} else {
			Toast.makeText(Main.this, "未检测到SD卡，操作将受限！", Toast.LENGTH_SHORT).show();
			isRestricted = false;
		}

		// 启动SD卡监听服务
		startService(new Intent(Main.this, SDCardService.class));

		// // 2014-6-24 WeiHao 登录之后，获取新任务、反馈、消息等
		// // //////TODO 需要更改请求参数
		// webRequestManager.getAffairUpdate("", "");
		// // /////接口 未实现
		// webRequestManager.getFeedbackUpdate();
		// // //////TODO 需要更改请求参数
		// webRequestManager.getMessageUpdate("", "");
	}

	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case LocalConstant.SD_MOUNTED:
					Toast.makeText(Main.this, "SD卡已挂载，操作恢复！", Toast.LENGTH_SHORT).show();
					isRestricted = false;
					break;
				case LocalConstant.SD_UNMOUNTED:
					Toast.makeText(Main.this, "SD卡已移除，操作将受限！", Toast.LENGTH_SHORT).show();
					isRestricted = true;
					break;
				case LocalConstant.NET_AVAILABLE:
					Toast.makeText(Main.this, "当前网络：" + msg.obj.toString(), Toast.LENGTH_SHORT)
							.show();
					// ping应用服务器
					// if (Utils.serverPing()) {
					// netStatusTv.setText("");
					// } else {
					// netStatusTv.setText("(未连接)");
					// }

					break;
				case LocalConstant.NET_UNAVAILABLE:
					Toast.makeText(Main.this, "无可用网络", Toast.LENGTH_SHORT).show();
					netStatusTv.setText("(未连接)");
					break;

				case Constant.SHOW_MESSAGE_NOTIFICATION:
					// MessageModel message = (MessageModel) msg.obj;
					//
					// // 2014-7-30 WeiHao 逻辑修改
					// // 判断收到的是否是群消息
					// // 如果是群消息，Notification显示收到来自群名的新消息
					// // 如果是个人消息，notification显示收到来自发送人的新消息
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
					Log.i("Chat", "Chat收到新消息");
					// //////TODO 需要更改请求参数
					webRequestManager.getMessageUpdate("");
					break;
				case Constant.SHOW_TASK_NOTIFICATION:
					// AffairModel affair = (AffairModel) msg.obj;
					// String sponsorName =
					// personDao.getPersonNameByID(String.valueOf(affair
					// .getSponsorID()));
					// boolean isModify = false; // 是否为修改任务
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
					// 注销mqtt
					Push.get_instance(Main.this).PERSON_ID = MySharedPreference.get(Main.this,
							MySharedPreference.USER_ID, "");
					try {
						Push.get_instance(Main.this).release();
					} catch (Exception e) {
						e.printStackTrace();
						Toast.makeText(getApplicationContext(), "MQTT连接失败", Toast.LENGTH_LONG)
								.show();
					}
					// 停止服务
					stopService(new Intent(Main.this, SDCardService.class));
					// 跳转到登录界面
					Intent intent9 = new Intent(Main.this, Login.class);
					intent9.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent9);
					Main.this.finish();
					break;
				case Constant.LOGOUT_REQUEST_FAIL:
					Log.e("Main", "logout fail");
					Toast.makeText(Main.this, "注销失败", Toast.LENGTH_SHORT).show();
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
		// 注销所有handler
		MessageHandlerManager.getInstance().unregister(Constant.LOGOUT_REQUEST_SUCCESS,
				Contants.METHOD_PERSON_LOGOUT);
		MessageHandlerManager.getInstance().unregister(Constant.LOGOUT_REQUEST_FAIL,
				Contants.METHOD_PERSON_LOGOUT);
		super.onDestroy();
	}

	/*
	 * 初始化视图
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
		// 可以自定义actionbar
		getSupportActionBar().setDisplayShowCustomEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		// 不在actionbar显示logo
		getSupportActionBar().setDisplayShowHomeEnabled(false);
		mainActionBarView = LayoutInflater.from(this).inflate(R.layout.main_action_bar, null);
		getSupportActionBar().setCustomView(mainActionBarView);
	}

	/*
	 * 创建选项菜单
	 */
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {

		// 搜索按钮核心算法
		// createSearchItem(menu);

		// // 搜索按钮 隐藏
		// MenuItem search = menu.add(0, -1, 0, "search");
		// search.setIcon(R.drawable.abs__ic_search);
		// search.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		// 子菜单 ：发起任务，消息，会议，视频直播，智能调度，添加客户
		SubMenu addMenu = menu.addSubMenu("add item");

		addMenu.add(0, 1, 0, "发起任务").setIcon(R.drawable.ofm_task_icon);
		addMenu.add(0, 2, 0, "发起消息").setIcon(R.drawable.ofm_group_chat_icon);
		addMenu.add(0, 3, 0, "发起会议").setIcon(R.drawable.ofm_meeting_icon);
		// addMenu.add(0, 4, 0, "视频直播").setIcon(R.drawable.ofm_broadcast_icon);
		// addMenu.add(0, 5, 0, "智能调度").setIcon(R.drawable.ofm_schedule_icon);
		// addMenu.add(0, 6, 0, "添加客户").setIcon(R.drawable.ofm_add_icon);

		MenuItem addItem = addMenu.getItem();
		addItem.setIcon(R.drawable.ic_action_new);
		addItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);// 总是作为Action项显示

		// 子菜单 ：设置，注销，退出,个人页面
		SubMenu subMenu = menu.addSubMenu("action item");

		subMenu.add(0, 7, 0, "我的资料").setIcon(R.drawable.ofm_me_icon);
		subMenu.add(0, 8, 0, "设置").setIcon(R.drawable.ofm_setting_icon);
		subMenu.add(0, 9, 0, "注销").setIcon(R.drawable.ofm_log_off_icon);
		subMenu.add(0, 10, 0, "退出").setIcon(R.drawable.ofm_log_out_icon);
		// subMenu.add(0, 11, 1, "Call").setIcon(R.drawable.ofm_task_icon);

		MenuItem menuItem = subMenu.getItem();
		menuItem.setIcon(R.drawable.ic_action_overflow);
		menuItem.setShowAsAction(
				MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * 点击选项菜单 进行相应跳转
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		if (isRestricted) {
			Toast.makeText(Main.this, "SD卡未挂载，限制进行此操作", Toast.LENGTH_SHORT);
		} else {

			switch (item.getItemId()) {
			// 搜索
			case -1:
				Intent intent = new Intent(Main.this, SearchActivity.class);
				Main.this.startActivity(intent);
				break;
			case 1:
				// 发起任务
				Intent intent1 = new Intent(this, TaskAdd.class);
				intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);// 再次点击主菜单时将会清除该进程空间的所有子菜单Activity。
				startActivity(intent1);
				return true;
			case 2:
				// 发起消息
				Intent intent2 = new Intent(this, ContactSelect.class);
				intent2.putExtra("entrance_flag", 2);

				intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(intent2);
				return true;
			case 3:
				// 发起会议
				Intent intent3 = new Intent(this, MeetingAdd.class);

				intent3.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

				startActivity(intent3);

				return true;
			case 4:
				// 视频直播
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
				// 添加客户
				Intent intent6 = new Intent(this, ContactAdd.class);

				intent6.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				intent6.putExtra("entrance_type", 1);

				startActivity(intent6);

				return true;

			case 7:
				// 个人资料
				Intent intent8 = new Intent(Main.this, Profile.class);
				startActivity(intent8);
				break;

			case 8:
				// 设置页面
				startActivity(new Intent(Main.this, Setting.class));
				break;
			case 9:
				// 注销页面

				webRequestManager.logOut();
				return true;
			case 10:
				// 弹出退出窗口
				showExitSelection();
				return true;
			case 11:
				// 2014-8-5
				// 2014-8-7
				ArrayList<String> speakerIDList = new ArrayList<String>(
						Arrays.asList("100011", "100013", "100015", "100016"));
				ArrayList<String> speakerNameList = new ArrayList<String>(
						Arrays.asList("自动一", "自动三", "自动五", "自动六"));
				ArrayList<String> participatorIDList = new ArrayList<String>(
						Arrays.asList("100012", "100014"));
				ArrayList<String> participatorNameList = new ArrayList<String>(
						Arrays.asList("自动二", "自动四", "自动七", "自动八", "自动九", "自动十", "自动11", "自动12",
								"自动13", "自动13", "自动13", "自动13", "自动13", "自动13", "自动13", "自动13"));
				// ArrayList<String> participatorNameList = new
				// ArrayList<String>(
				// Arrays.asList("自动懂"));
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
		new AlertDialog.Builder(Main.this).setTitle("退出程序").setMessage("退出程序后，是否继续接收新消息提醒？")
				.setPositiveButton("接收", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// 模拟Home按键，程序后台挂起
						Intent intent = new Intent(Intent.ACTION_MAIN);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.addCategory(Intent.CATEGORY_HOME);
						Main.this.startActivity(intent);
					}
				}).setNegativeButton("不接收", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						// 注销mqtt
						Push.get_instance(Main.this).PERSON_ID = MySharedPreference.get(Main.this,
								MySharedPreference.USER_ID, "");
						try {
							Push.get_instance(Main.this).release();
						} catch (Exception e) {
							e.printStackTrace();
							Toast.makeText(getApplicationContext(), "MQTT连接失败", Toast.LENGTH_LONG)
									.show();
						}
						// 停止服务
						stopService(new Intent(Main.this, SDCardService.class));
						// 杀死进程，程序退出
						android.os.Process.killProcess(android.os.Process.myPid()); // 获取PID
						System.exit(0); // 常规java、c#的标准退出法，返回值为0代表正常退出
					}
				}).create().show();
	}

	// public void show_task_list(View v) { // 点击任务页面：显示任务列表
	// Intent intent = new Intent();
	// intent.setClass(Main.this, TaskList.class);
	// startActivity(intent);
	// }
	//
	// public void startchat(View v) { // 点击消息页面：显示小黑对话界面
	// Intent intent = new Intent(Main.this, ChatDetail.class);
	// startActivity(intent);
	// }
	//
	// public void show_phone_detail(View v) { // 点击电话页面：显示电话列表
	// Intent intent = new Intent();
	// intent.setClass(Main.this, PhoneDetail.class);
	// startActivity(intent);
	// }
	//
	// public void show_meeting_detail(View v) { // 点击会议页面：显示会议列表
	// Intent intent = new Intent();
	// intent.setClass(Main.this, MeetingDetail.class);
	// startActivity(intent);
	// }

	/*
	 * private void createSearchItem(com.actionbarsherlock.view.Menu menu) {
	 * 
	 * SearchView searchView = new
	 * SearchView(getSupportActionBar().getThemedContext());
	 * searchView.setQueryHint("Search for countries…");
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
