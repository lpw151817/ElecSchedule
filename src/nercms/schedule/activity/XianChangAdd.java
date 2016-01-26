package nercms.schedule.activity;

import java.io.File;
import java.io.Serializable;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.adapter.XianChangAddAdapter;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.MyGPS;
import nercms.schedule.utils.MyLocationListener.ReceiveGPS;
import nercms.schedule.utils.Utils;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
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
	Button bt_jieshurenwu, bt_shangchuanfujian;
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

	public static boolean hasContent = false;// 用来标志条目里面是否有内容来决定XianChangUpload中loadAttachment方法是否执行

	private XianChangAddAdapter xianChangAddAdapter;

	private Handler handler;
	private int fileCount;

	WebRequestManager requestManager;
	boolean isContinueTask;
	PlanTaskDao planTaskDao;
	// TODO 附件列表，用于显示
	List<tb_task_attachment> atts;
	/*
	 * 每一个条目都有一个upload界面，每个upload界面用mediaIndex来标记附件在附件集中的位置，
	 * 在点击delete按钮的时候，就删除对应的附件，总共有6个upload界面，你从upload界面退出的时候
	 * mediaindex会被置为0，那么下次进来的时候，附件的下标从0开始，但是这样就和附件的路径不对应了
	 */
	private int[] myMediaIndexs;

	private String DownloadfileFolder = Environment.getExternalStorageDirectory().getPath()
			+ "/nercms-Schedule/DownloadAttachments/";

	private int[] counts;// 为从网络上下载的附件设置下标index
	boolean isClickShangchuanfujian = false;
	boolean hasUpload = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_add);

		requestManager = new WebRequestManager(AppApplication.getInstance(), this);

		enterType = getIntent().getIntExtra("enterType", -1);
		tid = getIntent().getStringExtra("tid");
		isContinueTask = getIntent().getBooleanExtra("isContinueTask", false);
		switch (enterType) {
		case 1:
			iniActionBar(true, null, "作业现场");
			break;
		case 2:
			iniActionBar(true, null, "操作现场");
			break;
		case 3:
			iniActionBar(true, null, "故障紧急抢修现场");
			break;
		}

		bt_jieshurenwu = (Button) findViewById(R.id.jieshurenwu);
		bt_jieshurenwu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!hasUpload) {
					fileCount = getFileCount();// 获取文件的个数，上传完后finish当前页免
					// Log.e("TAG", "xianChangAdd fileCount : "+ fileCount);
					isClickShangchuanfujian = false;
					attachmentUploadRequest();// 上传附件
				} else {
					requestManager.endTask(XianChangAdd.this, tid, System.currentTimeMillis() + "");
				}
			}

		});

		bt_shangchuanfujian = (Button) findViewById(R.id.shangchuanfujian);
		bt_shangchuanfujian.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (hasUpload) {
					showLongToast("附件已上传");
				} else {
					fileCount = getFileCount();// 获取文件的个数，上传完后finish当前页免
					// Log.e("TAG", "xianChangAdd fileCount : "+ fileCount);
					isClickShangchuanfujian = true;
					attachmentUploadRequest();// 上传附件
				}
			}
		});

		tv_time = (TextView) findViewById(R.id.time);

		mListView = (ListView) findViewById(R.id.listview);
		xianChangAddAdapter = new XianChangAddAdapter(this, enterType);
		mListView.setAdapter(xianChangAddAdapter);

		// myMediaIndexs = new int[6];
		myMediaIndexs = new int[xianChangAddAdapter.getCount()];
		// 初始化
		for (int i = 0; i < xianChangAddAdapter.getCount(); i++) {
			List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
			mList.add(mItem);
		}

		// mPath.add((String) getIntent().getCharSequenceExtra("path"));
		if (!isContinueTask) {// 不是从继续任务plantask这个页面跳过了
			flag = getIntent().getIntExtra("position", -1);
			mList.get(flag).add((Map<String, Object>) getIntent().getSerializableExtra("path"));

			System.out.println("传递过来的位置信息flag :" + flag);
		}

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent intent = new Intent(XianChangAdd.this, XianChangUpload.class);
				if (from.equals("XianChangUpload")) {
					intent.putExtra("mediaIndex", mediaIndex);
					System.out.println("传回mediaIndex的值来记录上次附件的下标");
				}

				boolean enterFlag = false;
				List<String> mFileName = new ArrayList<String>();
				if (isContinueTask) {// 是从继续任务plantask这个页面跳过来的
					for (int i = 0; i < xianChangAddAdapter.getCount(); i++) {
						myMediaIndexs[i] = counts[i];
					}
					intent.putExtra("myMediaIndex", myMediaIndexs[position]);

					isContinueTask = false;
				} else {
					if (flag == position) {
						myMediaIndexs[flag] = 1;
						intent.putExtra("myMediaIndex", myMediaIndexs[position]);
					} else {
						intent.putExtra("myMediaIndex", myMediaIndexs[position]);
					}
				}

				for (List<Map<String, Object>> ma : mList) {
					for (Map<String, Object> mm : ma) {
						String attachmentFilePath = (String) mm.get("path");
						mFileName.add(attachmentFilePath);
					}
				}

				if (mFileName.size() != 0) {
					File attachmentFile = new File(mFileName.get(mFileName.size() - 1));

					if (!attachmentFile.exists()) {
						Toast.makeText(XianChangAdd.this, "附件正在下载", Toast.LENGTH_SHORT).show();
						// enterFlag = true;
						return;
					}
				}

				// 将所有的文件名都存入到List中，当最后一个文件存在的时候才允许点击
				// if (enterFlag == true){
				// return;//目的是不让iscontinueTask置为false
				// }

				intent.putExtra("url", (Serializable) mList.get(position));
				intent.putExtra("position", position);// 把被点击的条目的位置传递进去，这样在接收的时候就知道该该改变哪个list的内容
				XianChangAdd.this.startActivityForResult(intent, 100);
			}
		});

		// 如果是从继续任务界面过来则初始化附件列表并显示出来
		if (isContinueTask) {
			planTaskDao = new PlanTaskDao(this);

			tb_task_info data = planTaskDao.getPlanTask(tid);
			if (!TextUtils.isEmpty(data.getEnd_time())) {
				v_bottom = (View) findViewById(R.id.bottom);
				v_bottom.setVisibility(View.GONE);
			} else {

				atts = planTaskDao.getPlanTaskAtt(tid);

				// atts.get(0).getStandard();
				// atts每个任务原来的已经上传的附件列表，它的getStandard是返回的是附件所属的条目，

				counts = new int[xianChangAddAdapter.getCount()];

				// TODO 还要将附件下载下来
				for (tb_task_attachment attachment : atts) {
					String standard = attachment.getStandard();

					GpsDao gpsDao = new GpsDao(this);
					GPS gps = gpsDao.getHistory(attachment.getHistorygps());// 从数据库中获取gps信息
					MyGPS mGPS = new MyGPS(gps.getOllectionTime(),
							Double.valueOf(gps.getLongitude()), Double.valueOf(gps.getLatitude()),
							Float.valueOf(gps.getAccuracy()), Double.valueOf(gps.getHeight()),
							Float.valueOf(gps.getSpeed()), gps.getCoordinate());

					String mediaName = attachment.getUrl();
					String filePath = DownloadfileFolder + File.separator + mediaName;
					String downUrl = android.wxapp.service.elec.request.Contants.HFS_URL
							+ File.separator + mediaName;

					File file = new File(filePath);
					if (!file.exists()) {// 文件有缓存，就不需要下载了
						new HttpDownloadTask(XianChangAdd.this).execute(downUrl,
								"/nercms-Schedule/DownloadAttachments/", mediaName);// 将附件下载下来
					}

					// 附件信息
					Map<String, Object> mMap1 = new HashMap<String, Object>();
					mMap1.put("gps", mGPS);
					mMap1.put("path", filePath);
					mMap1.put("time", gps.getOllectionTime());// 传递附件的时间

					if (standard.equals("standard01")) {
						mMap1.put("index", counts[0] + "");
						counts[0]++;
						mList.get(0).add(mMap1);
					}
					if (standard.equals("standard02")) {
						mMap1.put("index", counts[1] + "");
						counts[1]++;
						mList.get(1).add(mMap1);
					}
					if (standard.equals("standard03")) {
						mMap1.put("index", counts[2] + "");
						counts[2]++;
						mList.get(2).add(mMap1);
					}
					if (standard.equals("standard04")) {
						mMap1.put("index", counts[3] + "");
						counts[3]++;
						mList.get(3).add(mMap1);
					}
					if (standard.equals("standard05")) {
						mMap1.put("index", counts[4] + "");
						counts[4]++;
						mList.get(4).add(mMap1);
					}
					if (standard.equals("standard06")) {
						mMap1.put("index", counts[5] + "");
						counts[5]++;
						mList.get(5).add(mMap1);
					}

					// 操作现场
					if (standard.equals("standard07")) {
						mMap1.put("index", counts[0] + "");
						counts[0]++;
						mList.get(0).add(mMap1);
					}

					if (standard.equals("standard08")) {
						mMap1.put("index", counts[1] + "");
						counts[1]++;
						mList.get(1).add(mMap1);
					}

					if (standard.equals("standard09")) {
						mMap1.put("index", counts[2] + "");
						counts[2]++;
						mList.get(2).add(mMap1);
					}

					if (standard.equals("standard10")) {
						mMap1.put("index", counts[3] + "");
						counts[3]++;
						mList.get(3).add(mMap1);
					}

					if (standard.equals("standard11")) {
						mMap1.put("index", counts[4] + "");
						counts[4]++;
						mList.get(4).add(mMap1);
					}
				}
				System.out.println("XianChangAdd atts : " + atts.toString());
			}
		}

		initHandler();

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

	@Override
	protected void onResume() {
		super.onResume();

		changeTextColor();// 重新出现的时候字体更改 因为oncreate只执行一次
	}

	String from = "";
	int mediaIndex;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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

	private void attachmentUploadRequest() {
		if (!Utils.isNetworkAvailable(XianChangAdd.this)) {
			Toast.makeText(XianChangAdd.this, "网络不可用", Toast.LENGTH_SHORT).show();
			return;
		}
		String uploadUrl = android.wxapp.service.elec.request.Contants.HFS_URL;
		for (List<Map<String, Object>> mLi : mList) {

			for (Map<String, Object> map : mLi) {
				if (map != null) {
					if (map.get("path") != null) {
						Log.e("TAG", "上传的路径 : " + map.get("path"));
						new HttpUploadTask(new TextView(this), this)
								.execute((String) map.get("path"), uploadUrl);
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

						Toast.makeText(XianChangAdd.this, "上传成功", Toast.LENGTH_SHORT).show();
						// 请求http接口
						List<TaskAttachment> attachment = new ArrayList<TaskAttachment>();
						for (int i = 0; i < mList.size(); i++) {
							StringBuilder standard = new StringBuilder("standard");
							// 作业现场
							if (enterType == 1) {
								switch (i) {
								// 工作票
								case 0:
									standard.append("01");
									break;
								case 1:
									standard.append("02");
									break;
								case 2:
									standard.append("03");
									break;
								case 3:
									standard.append("04");
									break;
								case 4:
									standard.append("05");
									break;
								case 5:
									standard.append("06");
									break;
								}
							}
							// 操作现场
							else if (enterType == 2) {
								switch (i) {
								case 0:
									standard.append("07");
									break;
								case 1:
									standard.append("08");
									break;
								case 2:
									standard.append("09");
									break;
								case 3:
									standard.append("10");
									break;
								case 4:
									standard.append("11");
									break;
								}
							}
							// 故障抢修
							else if (enterType == 3) {
								switch (i) {
								case 0:
									standard.append("01");
									break;
								case 1:
									standard.append("02");
									break;
								case 2:
									standard.append("03");
									break;
								case 3:
									standard.append("04");
									break;
								case 4:
									standard.append("05");
									break;
								case 5:
									standard.append("06");
									break;
								}
							}

							List<Attachments> sublist = new ArrayList<Attachments>();
							String server = android.wxapp.service.elec.request.Contants.HFS_URL;
							for (int j = 0; j < mList.get(i).size(); j++) {
								Map<String, Object> attItem = mList.get(i).get(j);

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
											myGPS.getLongitude() + "", myGPS.getLatitude() + "", "",
											myGPS.getRadius() + "", myGPS.getAltitude() + "",
											myGPS.getSpeed() + "",
											Utils.formatDateMs(System.currentTimeMillis()),
											myGPS.getCoorType(), "");

									// String md5 = DigestUtils
									// .md5Hex(new FileInputStream(new
									// File(filePath)));
									String md5 = Utils.getFileMD5(new File(filePath));
									Attachments att = new Attachments(type,
											server + File.separator + path2FileName(filePath),
											(String) attItem.get("time"), gps, md5);
									sublist.add(att);

								}
							}
							TaskAttachment item = new TaskAttachment(standard.toString(), sublist);
							attachment.add(item);
						}

						requestManager.uploadTaskAttachment(XianChangAdd.this, tid, enterType + "",
								attachment);

					}

					break;
				case Constant.FILE_UPLOAD_FAIL:
					Toast.makeText(XianChangAdd.this, "上传失败", Toast.LENGTH_SHORT).show();
					break;

				case Constants.UPLOAD_TASK_ATT_SUCCESS:
					hasUpload = true;
					if (!isClickShangchuanfujian)
						requestManager.endTask(XianChangAdd.this, tid,
								System.currentTimeMillis() + "");
					break;
				case Constants.END_TASK_SUCCESS:
					showLongToast("任务已结束");
					findViewById(R.id.bottom).setVisibility(View.GONE);
					break;

				case Constants.UPLOAD_TASK_ATT_SAVE_FAIL:
				case Constants.UPLOAD_TASK_ATT_FAIL:
				case Constants.END_TASK_FAIL:
				case Constants.END_TASK_SAVE_FAIL:
					if (msg.obj != null) {
						showAlterDialog("上传失败", ((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "确定", null);
					} else {
						showAlterDialog("上传失败", "请检查是否与服务器连接正常", R.drawable.login_error_icon, "确定",
								null);
					}
					break;
				default:
					break;
				}
			}

		};

		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_FAIL,
				"XianChangAdd");
		MessageHandlerManager.getInstance().register(handler, Constant.FILE_UPLOAD_SUCCESS,
				"XianChangAdd");

		MessageHandlerManager.getInstance().register(handler, Constants.UPLOAD_TASK_ATT_SUCCESS,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.UPLOAD_TASK_ATT_SAVE_FAIL,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.UPLOAD_TASK_ATT_FAIL,
				UploadTaskAttachmentResponse.class.getName());

		MessageHandlerManager.getInstance().register(handler, Constants.END_TASK_SUCCESS,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.END_TASK_FAIL,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().register(handler, Constants.END_TASK_SAVE_FAIL,
				StartTaskResponse.class.getName());
	}

	@Override
	public void onReceiveGPS(MyGPS gps) {

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_FAIL, "XianChangAdd");
		MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_SUCCESS,
				"XianChangAdd");

		MessageHandlerManager.getInstance().unregister(Constants.UPLOAD_TASK_ATT_SUCCESS,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.UPLOAD_TASK_ATT_SAVE_FAIL,
				UploadTaskAttachmentResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.UPLOAD_TASK_ATT_FAIL,
				UploadTaskAttachmentResponse.class.getName());

		MessageHandlerManager.getInstance().unregister(Constants.END_TASK_SUCCESS,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.END_TASK_FAIL,
				StartTaskResponse.class.getName());
		MessageHandlerManager.getInstance().unregister(Constants.END_TASK_SAVE_FAIL,
				StartTaskResponse.class.getName());
	}

	private String path2FileName(String path) {
		return path.substring(path.lastIndexOf(File.separator) + 1);
	}
}
