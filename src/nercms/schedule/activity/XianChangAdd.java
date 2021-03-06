package nercms.schedule.activity;

import java.io.File;
import java.io.Serializable;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.actionbarsherlock.view.MenuItem;
import com.baidu.location.b.f;

import nercms.schedule.R;
import nercms.schedule.adapter.XianChangAddAdapter;
import nercms.schedule.utils.AttachmentDatabase;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.MyGPS;
import nercms.schedule.utils.MyLocationListener.ReceiveGPS;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.PlayVideo;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.dao.GpsDao;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.model.StartTaskResponse;
import android.wxapp.service.elec.model.UploadTaskAttachmentResponse;
import android.wxapp.service.elec.model.bean.Attachments;
import android.wxapp.service.elec.model.bean.GPS;
import android.wxapp.service.elec.model.bean.TaskAttachment;
import android.wxapp.service.elec.model.bean.table.tb_task_attachment;
import android.wxapp.service.elec.model.bean.table.tb_task_info;
import android.wxapp.service.elec.request.Constants;
import android.wxapp.service.elec.request.WebRequestManager;
import android.wxapp.service.handler.MessageHandlerManager;
import android.wxapp.service.jerry.model.normal.NormalServerResponse;
import android.wxapp.service.util.Constant;
import android.wxapp.service.util.DownloadMutex;
import android.wxapp.service.util.HttpDownloadTask;
import android.wxapp.service.util.HttpUploadTask;

/*
 * showXianchangAttachment和XianChangUpload可以跳转到该界面
 */
public class XianChangAdd extends BaseActivity implements ReceiveGPS {
	// 测试数据
	int enterType;
	// 上一个界面传过来的任务id
	String tid;

	ListView mListView;
	Button bt_jieshurenwu, bt_video;
	TextView tv_time;
	View v_bottom;

	int flag;// 用来标记哪个条目有内容

	// 每个条目的附件路径
	// 一个Map包含附件的路径和它的GPS信息和时间戳
	Map<String, Object> mAttachment = new HashMap<String, Object>();

	// 每个条目的附件集合
	List<Map<String, Object>> mContent = new ArrayList<Map<String, Object>>();

	// 整个List
	List<List<Map<String, Object>>> mList = new ArrayList<List<Map<String, Object>>>();

	// 存放每个条目中被上传了的附件
	List<List<Map<String, Object>>> mUploadList = new ArrayList<List<Map<String, Object>>>();

	// 存放对应条目中上传的附件
	List<Map<String, Object>> mUploadUrl = new ArrayList<Map<String, Object>>();

	List<Map<String, Object>> mVideoAttach = new ArrayList<Map<String, Object>>();

	// 存放每個條目裡面的附件的數目
	private int[] mContentCount;

	public static boolean hasContent = false;// 用来标志条目里面是否有内容来决定XianChangUpload中loadAttachment方法是否执行

	private XianChangAddAdapter xianChangAddAdapter;

	private Handler handler;
	private int fileCount;

	WebRequestManager requestManager;
	boolean isContinueTask;
	PlanTaskDao planTaskDao;
	tb_task_info data;
	// 附件列表，用于显示
	List<tb_task_attachment> atts;
	/*
	 * 每一个条目都有一个upload界面，每个upload界面用mediaIndex来标记附件在附件集中的位置，
	 * 在点击delete按钮的时候，就删除对应的附件，总共有6个upload界面，你从upload界面退出的时候
	 * mediaindex会被置为0，那么下次进来的时候，附件的下标从0开始，但是这样就和附件的路径不对应了
	 */
	private int[] myMediaIndexs;

	public static String DownloadfileFolder = Environment
			.getExternalStorageDirectory().getPath()
			+ "/nercms-Schedule/DownloadAttachments/";

	private int[] counts;// 为从网络上下载的附件设置下标index
	boolean isClickShangchuanfujian = false;
	boolean hasUpload = false;

	private String videopath;
	boolean isEnd = false;
	boolean isShowAlertDialog = true;// 防止重复显示AlertDialog

	boolean isExistVideo = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.v("Http", "XianChangAdd onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_add);

		requestManager = new WebRequestManager(AppApplication.getInstance(), this);

		enterType = getIntent().getIntExtra("enterType", -1);
		tid = getIntent().getStringExtra("tid");
		isContinueTask = getIntent().getBooleanExtra("isContinueTask", false);

		if (planTaskDao == null)
			planTaskDao = new PlanTaskDao(this);
		if (data == null)
			data = planTaskDao.getPlanTask(tid);

		StringBuilder title = new StringBuilder();

		switch (enterType) {
		case 1:
			title.append("作业现场");
			break;
		case 2:
			title.append("操作现场");
			break;
		case 3:
			title.append("故障紧急抢修现场");
			break;
		}
		title.append(" - " + data.getName());
		iniActionBar(true, null, title.toString());

		// if (isAdmin() != PERSON_TYPE.XIANCHANG)
		{
			if (isAdmin() != PERSON_TYPE.XIANCHANG) {
				findViewById(R.id.bottom).setVisibility(View.GONE);
				findViewById(R.id.jieshurenwu).setVisibility(View.GONE);
			}
			PlanTaskDao pDao = new PlanTaskDao(XianChangAdd.this);
			List<tb_task_attachment> planTask = pDao.getPlanTaskAtts(tid);

			// boolean flag = false;
			for (final tb_task_attachment tb_task_attachment : planTask) {
				String standard = tb_task_attachment.getStandard();

				if (standard.equals("standard")) {

					// 有视频就显示播放视频的button,点击button播放视频
					Button playVideo = (Button) findViewById(R.id.playvideo);
					playVideo.setVisibility(View.VISIBLE);
					playVideo.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							// TODO 播放视频
							Intent videoIntent = new Intent(XianChangAdd.this,
									PlayVideo.class);

							//String path = NewTask.fileFolder + tb_task_attachment.getUrl();
							String path = NewTask.fileFolder + tb_task_attachment.getUrl().substring(tb_task_attachment.getUrl().lastIndexOf("/") + 1);

							File file = new File(path);
							if (!file.getParentFile().exists())
								file.getParentFile().mkdirs();

							// 如果文件未存在，或者文件已存在但无法执行或者读取，则重新下载
							if (!file.exists()
									|| (file.exists() && (/*
														 * !file.canExecute() ||
														 */!file.canRead() || !file
											.canWrite()))) {
								if (file.exists())
									file.delete();
								Toast.makeText(XianChangAdd.this,
										"视频正在下载中，请稍后", Toast.LENGTH_SHORT)
										.show();

							} else {

								videoIntent.putExtra("path", path);
								startActivity(videoIntent);
							}
							// flag = true;
						}
					});
				}
			}

			// if (!flag){
			// Utils.showShortToast(XianChangAdd.this, "当前没有录像");
			// }

		}

		bt_jieshurenwu = (Button) findViewById(R.id.jieshurenwu);
		bt_jieshurenwu.setOnClickListener(new OnClickListener() {

			// TODO 结束任务，要每个条目里面都有内容才能结束
			@Override
			public void onClick(View v) 
			{
				if (planTaskDao == null)
					planTaskDao = new PlanTaskDao(XianChangAdd.this);
				if (!TextUtils.isEmpty(planTaskDao.getPlanTask(tid)
						.getEnd_time())) {
					showShortToast("任务已结束");
					findViewById(R.id.bottom).setVisibility(View.GONE);
				} else {
					if (isfull()) {
						AlertDialog.Builder builder = new AlertDialog.Builder(
								XianChangAdd.this).setMessage("是否结束任务?");
						builder.setPositiveButton("是",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// 如果没有录像直接结束
										// if (mVideo.size() == 0) {
										Log.v("Demo", "XianChangAdd : "
												+ handler.toString());
										requestManager.endTask(
												XianChangAdd.this, tid,
												System.currentTimeMillis() + "");
										// }
										fileCount = getFileCount();// 获取文件的个数，上传完后finish当前页免
										// Log.e("TAG",
										// "xianChangAdd fileCount : "+
										// fileCount);
										isClickShangchuanfujian = false;
										// attachmentUploadRequest();// 上传附件

										isEnd = true;

									}
								});

						builder.setNegativeButton("否",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								});

						AlertDialog dialog = builder.create();
						dialog.show();

					} else {
						Utils.showToast(XianChangAdd.this, "必须每一项都有附件才能结束");
					}
				}
			}

		});

		// bt_video = (Button) findViewById(R.id.shangchuanfujian);
		bt_video = (Button) findViewById(R.id.video);
		// bt_video.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// if (getFileCount() < 1) {
		// showShortToast("请选择附件");
		// } else {
		// if (hasUpload) {
		// showLongToast("附件已上传");
		// } else {
		// fileCount = getFileCount();// 获取文件的个数，上传完后finish当前页免
		// // Log.e("TAG", "xianChangAdd fileCount : "+ fileCount);
		// isClickShangchuanfujian = true;
		// attachmentUploadRequest();// 上传附件
		// }
		// }
		// }
		// });

		// 上传视频附件
		bt_video.setOnClickListener(new OnClickListener() {

			// TODO
			@Override
			public void onClick(View v) {

				if (!isExistVideo) {// 不存在视频就录像

					// Intent intent = new Intent();
					// intent.setAction("android.media.action.VIDEO_CAPTURE");
					// intent.addCategory("android.intent.category.DEFAULT");

					Intent intent = new Intent(XianChangAdd.this, Video.class);

					String fileName = Utils.getFileDate();
					videopath = NewTask.fileFolder + "/" + fileName + ".mp4";
					File file = new File(videopath);
					if (file.exists()) {
						file.delete();
					}
					Uri uri = Uri.fromFile(file);
					intent.putExtra("videoPath", videopath);
					intent.putExtra("maxTime", 15);//任务录像最长15秒
					// intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
					// intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
					// intent.putExtra(MediaStore.EXTRA_SIZE_LIMIT, 20 * 1024 *
					// 1024);
					// intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 15);
					startActivityForResult(intent,
							LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);
				} else {// 存在视频就播放
					Intent videoIntent = new Intent(XianChangAdd.this,
							PlayVideo.class);

					if (mVideo == null)
						return;
					String path = (String) mVideo.get("path");

					videoIntent.putExtra("path", path);
					startActivity(videoIntent);
				}
			}
		});

		tv_time = (TextView) findViewById(R.id.time);

		tv_time.setText(Utils.formatDateMs(data.getCreator_time()));
		mContentCount = new int[getItemCount(XianChangAdd.this)];

		mListView = (ListView) findViewById(R.id.listview);
		xianChangAddAdapter = new XianChangAddAdapter(this, enterType,
				mContentCount, isAdmin());
		mListView.setAdapter(xianChangAddAdapter);

		// myMediaIndexs = new int[6];
		myMediaIndexs = new int[xianChangAddAdapter.getCount()];
		// 初始化
		for (int i = 0; i < xianChangAddAdapter.getCount(); i++) {
			List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
			mList.add(mItem);
		}
		for (int i = 0; i < xianChangAddAdapter.getCount(); i++) {
			List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
			mUploadList.add(mItem);
		}

		// mPath.add((String) getIntent().getCharSequenceExtra("path"));
		if (!isContinueTask) {// 不是从继续任务plantask这个页面跳过了
			flag = getIntent().getIntExtra("position", -1);
			mList.get(flag).add(
					(Map<String, Object>) getIntent().getSerializableExtra("path"));

			System.out.println("传递过来的位置信息flag :" + flag);
		}

		getDataFromDB();
		// }

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) 
			{
				if (isAdmin() != PERSON_TYPE.XIANCHANG)
				{
					if (mList.get(position).size() == 0)
					{
						showLongToast("没有附件信息查看");
						return;
					}
				}
				
				Intent intent = new Intent(XianChangAdd.this, XianChangUpload.class);
				
				String[] ss = getResources().getStringArray(R.array.zyxc_list);//fym2
				intent.putExtra("title", ss[position]);//fym2

				//fym2 if (((position == 3) && ((enterType == 1) || (enterType == 3))) || ((position == 2) && (enterType == 2)))
				if(enterType == 1 && (position == 1 || position == 2 || position == 3 || position == 7 || position == 8))
				{

					intent = new Intent(XianChangAdd.this, YanDianActivity.class);
					intent.putExtra("index", position);//fym2
				}
				else
				{
					if(from.equals("XianChangUpload")) 
					{
						intent.putExtra("mediaIndex", mediaIndex);
						System.out.println("传回mediaIndex的值来记录上次附件的下标");
					}

					List<String> mFileName = new ArrayList<String>();
					if (isContinueTask) {// 是从继续任务TaskSelector这个页面跳过来的
						for (int i = 0; i < xianChangAddAdapter.getCount(); i++) {
							myMediaIndexs[i] = counts[i];
						}
						intent.putExtra("myMediaIndex", myMediaIndexs[position]);

						isContinueTask = false;
					} else {// 从xianchangupload跳转的
						setMediaIndex();
						intent.putExtra("myMediaIndex", myMediaIndexs[position]);

					}

					for (List<Map<String, Object>> ma : mList) {
						for (Map<String, Object> mm : ma) {
							if (mm != null) {
								String attachmentFilePath = (String) mm
										.get("path");
								mFileName.add(attachmentFilePath);
							}

						}
					}

					if (mFileName.size() != 0) {
						File attachmentFile = new File(mFileName.get(mFileName
								.size() - 1));
						if (!attachmentFile.getParentFile().exists())
							attachmentFile.mkdirs();
						if (!attachmentFile.exists()) {
							Toast.makeText(XianChangAdd.this, "附件正在下载",
									Toast.LENGTH_SHORT).show();
							// enterFlag = true;
							return;
						}
					}

					intent.putExtra("mUploadUrl", (Serializable) mUploadList.get(position));
				}

				intent.putExtra("enterType", enterType);
				intent.putExtra("tid", tid);

				intent.putExtra("url", (Serializable) mList.get(position));
				intent.putExtra("position", position);// 把被点击的条目的位置传递进去，这样在接收的时候就知道该该改变哪个list的内容
				XianChangAdd.this.startActivityForResult(intent, LocalConstant.START_XIANCHANGUPLOAD);
			}
		});

		initHandler();

	}

	protected boolean isfull() {
		int flag = 0;
		// 判断每个条目是否都有内容
		for (int i = 0; i < mContentCount.length; i++) {
			if (mContentCount[i] == 0) {
				flag = 1;
				break;
			}
		}

		if (flag == 1) {
			return false;
		} else {
			return true;
		}

	}

	private void setMediaIndex() {
		for (int i = 0; i < mList.size(); i++) {
			int count = 0;
			for (int j = 0; j < mList.get(i).size(); j++) {
				if (mList.get(i).get(j) != null || mList.get(i).get(j) == null) {
					count++;
				}
			}
			myMediaIndexs[i] = count;
		}
	}

	private int getItemCount(Context c) {
		String[] ss = null;
		if (enterType == 1)
		{
			ss = c.getResources().getStringArray(R.array.zyxc_list);//fym2 ss = c.getResources().getStringArray(R.array.zuoyexianchang_si_data);
		}
		/*fym2 else if (enterType == 2)
		{
			ss = c.getResources().getStringArray(R.array.caozuoxianchang_si_data);
		}
		else if (enterType == 3)
		{
			ss = c.getResources().getStringArray(R.array.guzhangjinji_si_data);
		}*/

		return ss.length;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			break;

		default:
			break;
		}

		return super.onOptionsItemSelected(item);
	}

	protected int getFileCount() {
		int content = 0;
		for (List<Map<String, Object>> mLi : mList) {

			for (Map<String, Object> map : mLi) {
				if (map != null) {
					if (map.get("path") != null) {// 调用map的get方法的前提是map != null
						content++;
					}
				}
			}
		}
		return content;
	}

	String from = "";
	int mediaIndex;
	private Map<String, Object> mVideo = new HashMap<String, Object>();// 存放录制的视频信息

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case LocalConstant.START_XIANCHANGUPLOAD:
			if (resultCode == LocalConstant.SELECT_ATTACHMENT) {
				int index = data.getIntExtra("position", -1);
				from = data.getStringExtra("from");
				mediaIndex = data.getIntExtra("mediaIndex", 0);
				mContent = (List<Map<String, Object>>) data
						.getSerializableExtra("url");
				mList.set(index, mContent);// 改变在Upload里面对应被更改的内容

				mUploadUrl = (List<Map<String, Object>>) data
						.getSerializableExtra("mUploadUrl");
				mUploadList.set(index, mUploadUrl);
				myMediaIndexs[index] = mediaIndex;
			}
			break;

		// 录像
		case LocalConstant.CAPTURE_VIDEO_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				Log.i("TAG", "拍摄完成，resultCode=" + requestCode);

				File file = new File(videopath);

				Bitmap videoThumbnailBitmap = Utils.getVideoThumbnail(
						videopath, 400, 400,
						MediaStore.Images.Thumbnails.MINI_KIND);

				// uri4 = Uri.fromFile(file);
				// mediaID4 = mediaIndex++;

				String videoName = videopath.substring(videopath
						.lastIndexOf(File.separator) + 1);
				System.out.println("videoName: " + videoName);

				Intent intent = new Intent(XianChangAdd.this,
						ShowXianChangAttachment.class);
				Map<String, String> mMap = new HashMap<String, String>();
				mMap.put("type", "video");
				mMap.put("path", videopath);
				mMap.put("from", "XianChangUpload");
				// mMap.put("index", mediaID4 + "");// 传递附件的下标

				Map<Integer, Map<String, String>> mContent = new HashMap<Integer, Map<String, String>>();
				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable) mContent);
				startActivityForResult(intent,
						LocalConstant.SHOWXIANCHANG_ATTACHMENT);

			}
			break;

		case LocalConstant.SHOWXIANCHANG_ATTACHMENT:
			// TODO
			if (resultCode == RESULT_OK) {
				mVideo = (Map<String, Object>) data
						.getSerializableExtra("path");

				bt_video.setText("录像 (1)");
				// bt_video.setClickable(false);
				isExistVideo = true;
				writeToDatabase(mVideo);
			}

			break;

		default:
			break;
		}

	}

	private void writeToDatabase(Map<String, Object> map) {
		PlanTaskDao mDao = new PlanTaskDao(XianChangAdd.this);

		StringBuilder standard = new StringBuilder("standard");

		Map<String, Object> attItem = map;

		if (attItem == null) {
			return;
		}

		String filePath = (String) attItem.get("path");
		String type = Utils.judgeFileLeixin(filePath);
		if (type != null) {

			MyGPS myGPS = (MyGPS) attItem.get("gps");

			GpsDao mGpsDao = new GpsDao(XianChangAdd.this);
			long gpsId = mGpsDao.saveHistory(null, getUserId(),
					System.currentTimeMillis() + "", myGPS.getLongitude() + "",
					myGPS.getLatitude() + "", "", myGPS.getRadius() + "",
					myGPS.getAltitude() + "", myGPS.getSpeed() + "",
					System.currentTimeMillis() + "", myGPS.getCoorType(), "");

			String historygps = Long.valueOf(gpsId).toString();

			String md5 = Utils.getFileMD5(new File(filePath));
			String url = ((String) map.get("path"));
			String name = url.substring(url.lastIndexOf("/") + 1);
			name = tid + File.separator + "R" + File.separator + name;
			// String time = Utils.parseDateInFormat((String)
			// attItem.get("time"));
			String time = System.currentTimeMillis() + "";
			System.out.println("name: " + name + " time: " + time);

			// ///////////////////////////////////debug infos
			// HashMap<String, String> res =
			// AttachmentDatabase.instance(XianChangAdd.this)
			// // 数据库中的url为文件名
			// .query("select * from tb_task_attachment where url = '" + name +
			// "';");
			// if (null != res && 0 < Integer.parseInt(res.get("records_num")))
			// {
			// Log.v("login", "records_num " + res.get("records_num"));
			// return;
			// }
			// /////////////////////////////////////////

			Map<String, String> _query_result;

			// ////////////////////////////////// ///// Log所有附件
			// _query_result = AttachmentDatabase.instance(
			// XianChangAdd.this).query(
			// "select * from tb_task_attachment;");
			// for (int i = 0; i < Integer.parseInt(_query_result
			// .get("records_num")); i++) {
			// Log.v("Att",
			// "before:" + _query_result.get("id_" + i) + ":"
			// + _query_result.get("url_" + i) + ":"
			// + _query_result.get("status_" + i) + ":"
			// + _query_result.get("md5_" + i) + ":"
			// + _query_result.get("upload_time_" + i));
			// }
			// /////////////////////////////////////////////////////////////

			boolean id = mDao.savePlanTaskAtt(null, tid, historygps,
					standard.toString(), type, name, time, md5, "0");

			// ////////////////////////////////////////////////////////////
			// _query_result = AttachmentDatabase.instance(XianChangAdd.this)
			// .query("select * from tb_task_attachment;");
			// for (int i = 0; i < Integer.parseInt(_query_result
			// .get("records_num")); i++) {
			// Log.v("Att",
			// "after:" + _query_result.get("id_" + i) + ":"
			// + _query_result.get("url_" + i) + ":"
			// + _query_result.get("status_" + i) + ":"
			// + _query_result.get("md5_" + i) + ":"
			// + _query_result.get("upload_time_" + i));
			// }
			// ///////////////////////////////////////////////////////////

			if (id) {
				// if (!Utils.isNetworkAvailable(getApplicationContext())) {
				// Toast.makeText(getApplicationContext(), "无网络，附件将在网络恢复后上传",
				// Toast.LENGTH_SHORT)
				// .show();
				// } else
				Toast.makeText(XianChangAdd.this, "录像已存储，正在上传中",
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	// 改变list的颜色
	private void changeTextColor() {
		int position = 0;
		// 根据mList里面是否有内容来设置字体的颜色
		for (List<Map<String, Object>> mLi : mList) {
			// System.out.println("map : " + mLi.toString());
			int content = 0;

			for (Map<String, Object> map : mLi) {
				if (map != null) {
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

	// private void attachmentUploadRequest() {
	// if (!Utils.isNetworkAvailable(XianChangAdd.this)) {
	// Toast.makeText(XianChangAdd.this, "网络不可用", Toast.LENGTH_SHORT)
	// .show();
	// return;
	// }
	// String uploadUrl = android.wxapp.service.elec.request.Contants.HFS_URL;
	// if (getFileCount() < 1) {
	// showLongToast("请选择附件上传");
	// } else {
	//
	// if (mVideo.size() != 0) {
	// // 上传视频
	// new HttpUploadTask(new TextView(this), this, null).execute(
	// (String) mVideo.get("path"), uploadUrl);
	// }
	// }
	//
	// }

	@SuppressLint("HandlerLeak")
	private void initHandler() {
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				switch (msg.what) {
				case Constant.FILE_UPLOAD_SUCCESS:// 当所有的附件都上传完了之后finish当前页面

					Toast.makeText(XianChangAdd.this, "上传成功",
							Toast.LENGTH_SHORT).show();
					// 请求http接口
					List<TaskAttachment> attachment = new ArrayList<TaskAttachment>();
					StringBuilder standard = new StringBuilder("standard");

					List<Attachments> sublist = new ArrayList<Attachments>();
					String server = android.wxapp.service.elec.request.Contants.HFS_URL;
					Map<String, Object> attItem = mVideo;

					if (attItem == null) {
						return;
					}

					String filePath = (String) attItem.get("path");
					String type = Utils.judgeFileLeixin(filePath);
					if (type != null) {

						MyGPS myGPS = (MyGPS) attItem.get("gps");
						// 参数修改
						GPS gps = new GPS(getUserId(),
								Utils.formatDateMs(System.currentTimeMillis()),
								myGPS.getLongitude() + "", myGPS.getLatitude()
										+ "", "", myGPS.getRadius() + "",
								myGPS.getAltitude() + "",
								myGPS.getSpeed() + "",
								Utils.formatDateMs(System.currentTimeMillis()),
								myGPS.getCoorType(), "");

						String md5 = Utils.getFileMD5(new File(filePath));
						Attachments att = new Attachments(type, server
								+ File.separator + path2FileName(filePath),
								(String) attItem.get("time"), gps, md5);
						sublist.add(att);

					}
					TaskAttachment item = new TaskAttachment(
							standard.toString(), sublist);
					attachment.add(item);

					requestManager.uploadTaskAttachment(XianChangAdd.this, tid,
							enterType + "", attachment);

					break;
				case Constant.FILE_UPLOAD_FAIL:
					Toast.makeText(XianChangAdd.this, "上传失败",
							Toast.LENGTH_SHORT).show();
					break;

				case Constants.UPLOAD_TASK_ATT_SUCCESS:
					showLog_e("上传附件写入数据库成功");
					if (isEnd) {
						requestManager.endTask(XianChangAdd.this, tid,
								System.currentTimeMillis() + "");
					}
					// hasUpload = true;
					// if (!isClickShangchuanfujian) {
					// requestManager.endTask(XianChangAdd.this, tid,
					// System.currentTimeMillis() + "");
					// } else {
					// bt_video.setVisibility(View.GONE);
					// }
					break;
				case Constants.END_TASK_SUCCESS:
					showShortToast("任务已结束");
					findViewById(R.id.bottom).setVisibility(View.GONE);
					break;

				case Constants.UPLOAD_TASK_ATT_SAVE_FAIL:
				case Constants.UPLOAD_TASK_ATT_FAIL:
				case Constants.END_TASK_FAIL:
				case Constants.END_TASK_SAVE_FAIL:
					Log.e("Demo", "Xianchang Add handler fail");
					if (msg.obj != null) {
						showAlterDialog("上传失败",
								((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "确定", null);
					} else {
						if (isShowAlertDialog) {
							showAlterDialog("上传失败", "请检查是否与服务器连接正常",
									R.drawable.login_error_icon, "确定", null);
							isShowAlertDialog = false;
						}
					}
					break;
				default:
					break;
				}
			}

		};

	}

	@Override
	public void onReceiveGPS(MyGPS gps) {

	}

	@Override
	protected void onDestroy() {
		Log.v("Demo", getClass().getSimpleName() + "onDestroy");
		super.onDestroy();

	}

	private String path2FileName(String path) {
		return path.substring(path.lastIndexOf(File.separator) + 1);
	}

	@Override
	protected void onRestart() {
		Log.v("Demo", getClass().getSimpleName() + "onRestart");
		super.onRestart();
	}

	@Override
	protected void onStart() {
		Log.v("Demo", getClass().getSimpleName() + "onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.v("Demo", getClass().getSimpleName() + "onResume");
		super.onResume();

		// 避免重复加载
		
		for (int i = 0; i < mList.size(); i++) {
			mList.get(i).clear();
		}

		for (int i = 0; i < mUploadList.size(); i++) {
			mUploadList.get(i).clear();
		}

		getDataFromDB();

		// 遍历mList获取每个条目的数目
		for (int i = 0; i < mList.size(); i++) {
			int count = 0;
			for (int j = 0; j < mList.get(i).size(); j++) {
				if (mList.get(i).get(j) != null) {
					count++;
				}
			}
			mContentCount[i] = count;
		}
		xianChangAddAdapter.notifyDataSetChanged();
		changeTextColor();// 重新出现的时候字体更改 因为oncreate只执行一次

		MessageHandlerManager.getInstance().register(handler,
				Constant.FILE_UPLOAD_FAIL, "XianChangAdd");
		MessageHandlerManager.getInstance().register(handler,
				Constant.FILE_UPLOAD_SUCCESS, "XianChangAdd");

		MessageHandlerManager.getInstance().register(handler,
				Constants.UPLOAD_TASK_ATT_SUCCESS,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler,
				Constants.UPLOAD_TASK_ATT_SAVE_FAIL,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler,
				Constants.UPLOAD_TASK_ATT_FAIL,
				UploadTaskAttachmentResponse.class.getName());

		MessageHandlerManager.getInstance().register(handler,
				Constants.END_TASK_SUCCESS, StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler,
				Constants.END_TASK_FAIL, StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance()
				.register(handler, Constants.END_TASK_SAVE_FAIL,
						StartTaskResponse.class.getName());
	}

	// 从数据库里面查询数据显示
	private void getDataFromDB() {
		Log.i("Http", "getDataFromDB 1");

		// 如果是从继续任务界面过来则初始化附件列表并显示出来
		// if (isContinueTask) {
		if (planTaskDao == null)
			planTaskDao = new PlanTaskDao(this);
		if (data == null)
			data = planTaskDao.getPlanTask(tid);
		if (!TextUtils.isEmpty(data.getEnd_time())) {
			v_bottom = (View) findViewById(R.id.bottom);
			v_bottom.setVisibility(View.GONE);
		}

		atts = planTaskDao.getPlanTaskAtts(tid);

		// atts.get(0).getStandard();
		// atts每个任务原来的已经上传的附件列表，它的getStandard是返回的是附件所属的条目，

		counts = new int[xianChangAddAdapter.getCount()];

		Log.i("Http", "getDataFromDB 2");
		
		// TODO 还要将附件下载下来
		for (tb_task_attachment attachment : atts) {
			String standard = attachment.getStandard();

			GpsDao gpsDao = new GpsDao(this);
			GPS gps = gpsDao.getHistory(attachment.getHistorygps());// 从数据库中获取gps信息
			MyGPS mGPS = new MyGPS(gps.getOllectionTime(), Double.valueOf(gps
					.getLongitude()), Double.valueOf(gps.getLatitude()),
					Float.valueOf(gps.getAccuracy()), Double.valueOf(gps
							.getHeight()), Float.valueOf(gps.getSpeed()),
					gps.getCoordinate());

			String name = attachment.getUrl();
			String mediaName1 = name.replace("\\", File.separator);// 把所有的\替换成/
			String mediaName = mediaName1
					.substring(mediaName1.lastIndexOf("/") + 1);
			String filePath = DownloadfileFolder + mediaName;

			String downUrl = android.wxapp.service.elec.request.Contants.HFS_URL
					+ File.separator + mediaName1;

			File file = new File(filePath);
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			Log.i("Http", "getDataFromDB 3");
			//下载任务附件
			// 如果文件未存在，或者文件已存在但无法执行或者读取，则重新下载
			if (!file.exists() || (file.exists() && (/*!file.canExecute() ||*/!file.canRead() || !file.canWrite())))
			{
				if(false == DownloadMutex._download_task_tasks.containsKey(downUrl))
				{
					if (file.exists())
						file.delete();

					Log.i("Http", "downloadMutex._download_task_tasks.put");
					DownloadMutex._download_task_tasks.put(downUrl,
							(HttpDownloadTask)new HttpDownloadTask(XianChangAdd.this).execute(downUrl, "/nercms-Schedule/DownloadAttachments/", mediaName));
				}
			}

			// 附件信息
			Map<String, Object> mMap1 = new HashMap<String, Object>();
			mMap1.put("gps", mGPS);
			mMap1.put("path", filePath);// /storage/emulated/0/nercms-Schedule/DownloadAttachments/aq2016_06_03_143336.jpg
			mMap1.put("time", gps.getOllectionTime());// 传递附件的时间
			mMap1.put("pathContainsTid", DownloadfileFolder + mediaName1);// storage/emulated/0/nercms-Schedule/DownloadAttachments/356/4/aq2016_06_03_143336.jpg

			Log.v("Demo", "standard: " + standard);
			
			
//			修改  改变standard      chenqiang1,
			if (standard.contains("standard1-1")) {
				mMap1.put("index", counts[0] + "");
				counts[0]++;
				mList.get(0).add(mMap1);
				mUploadList.get(0).add(mMap1);
			}
			if (standard.contains("standard1-2")) {
				mMap1.put("index", counts[1] + "");
				counts[1]++;
				mList.get(1).add(mMap1);
				mUploadList.get(1).add(mMap1);
			}
			if (standard.contains("standard1-3")) {
				mMap1.put("index", counts[2] + "");
				counts[2]++;
				mList.get(2).add(mMap1);
				mUploadList.get(2).add(mMap1);
			}
			if (standard.contains("standard1-4")) {
				mMap1.put("index", counts[3] + "");
				counts[3]++;
				mList.get(3).add(mMap1);
				mUploadList.get(3).add(mMap1);
			}
			if (standard.contains("standard1-5")) {
				mMap1.put("index", counts[4] + "");
				counts[4]++;
				mList.get(4).add(mMap1);
				mUploadList.get(4).add(mMap1);
			}
			if (standard.contains("standard1-6")) {
				mMap1.put("index", counts[5] + "");
				counts[5]++;
				mList.get(5).add(mMap1);
				mUploadList.get(5).add(mMap1);
			}

			// 操作现场
			if (standard.contains("standard1-7")) {
				mMap1.put("index", counts[6] + "");
				counts[6]++;
				mList.get(6).add(mMap1);
				mUploadList.get(6).add(mMap1);
			}

			if (standard.contains("standard1-8")) {
				mMap1.put("index", counts[7] + "");
				counts[7]++;
				mList.get(7).add(mMap1);
				mUploadList.get(7).add(mMap1);
			}

			if (standard.contains("standard1-9")) {
				mMap1.put("index", counts[8] + "");
				counts[8]++;
				mList.get(8).add(mMap1);
				mUploadList.get(8).add(mMap1);
			}

//			if (standard.equals("standard10")) {
//				mMap1.put("index", counts[3] + "");
//				counts[3]++;
//				mList.get(3).add(mMap1);
//				mUploadList.get(3).add(mMap1);
//			}
//
//			if (standard.equals("standard11")) {
//				mMap1.put("index", counts[4] + "");
//				counts[4]++;
//				mList.get(4).add(mMap1);
//				mUploadList.get(4).add(mMap1);
//			}

			if (standard.equals("standard")) {
				mVideo = mMap1;
				bt_video.setText("录像（1）");
				// bt_video.setClickable(false);
				isExistVideo = true;
			}
		}
		System.out.println("XianChangAdd atts : " + atts.toString());
		// }
	}

	@Override
	protected void onPause() {
		Log.v("Demo", getClass().getSimpleName() + "onPause");
		super.onPause();

		MessageHandlerManager.getInstance().unregister(
				Constant.FILE_UPLOAD_FAIL, "XianChangAdd");
		MessageHandlerManager.getInstance().unregister(
				Constant.FILE_UPLOAD_SUCCESS, "XianChangAdd");

		MessageHandlerManager.getInstance().unregister(
				Constants.UPLOAD_TASK_ATT_SUCCESS,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(
				Constants.UPLOAD_TASK_ATT_SAVE_FAIL,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(
				Constants.UPLOAD_TASK_ATT_FAIL,
				UploadTaskAttachmentResponse.class.getName());

		MessageHandlerManager.getInstance().unregister(
				Constants.END_TASK_SUCCESS, StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.END_TASK_FAIL,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance()
				.unregister(Constants.END_TASK_SAVE_FAIL,
						StartTaskResponse.class.getName());
	}

	@Override
	protected void onStop() {
		Log.v("Demo", getClass().getSimpleName() + "onStop");
		super.onStop();
	}

}
