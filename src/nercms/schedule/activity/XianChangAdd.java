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
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.MyGPS;
import nercms.schedule.utils.MyLocationListener.ReceiveGPS;
import nercms.schedule.utils.Utils;
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
import android.wxapp.service.util.HttpDownloadTask;
import android.wxapp.service.util.HttpUploadTask;

/*
 * showXianchangAttachment��XianChangUpload������ת���ý���
 */
public class XianChangAdd extends BaseActivity implements ReceiveGPS {
	// ��������
	int enterType;
	// ��һ�����洫����������id
	String tid;

	ListView mListView;
	Button bt_jieshurenwu, bt_video;
	TextView tv_time;
	View v_bottom;

	int flag;// ��������ĸ���Ŀ������

	// ÿ����Ŀ�ĸ���·��
	// һ��Map����������·��������GPS��Ϣ��ʱ���
	Map<String, Object> mAttachment = new HashMap<String, Object>();

	// ÿ����Ŀ�ĸ�������
	List<Map<String, Object>> mContent = new ArrayList<Map<String, Object>>();

	// ����List
	List<List<Map<String, Object>>> mList = new ArrayList<List<Map<String, Object>>>();

	// ���ÿ����Ŀ�б��ϴ��˵ĸ���
	List<List<Map<String, Object>>> mUploadList = new ArrayList<List<Map<String, Object>>>();

	// ��Ŷ�Ӧ��Ŀ���ϴ��ĸ���
	List<Map<String, Object>> mUploadUrl = new ArrayList<Map<String, Object>>();

	List<Map<String, Object>> mVideoAttach = new ArrayList<Map<String, Object>>();

	// ���ÿ���lĿ�e��ĸ����Ĕ�Ŀ
	private int[] mContentCount;

	public static boolean hasContent = false;// ������־��Ŀ�����Ƿ�������������XianChangUpload��loadAttachment�����Ƿ�ִ��

	private XianChangAddAdapter xianChangAddAdapter;

	private Handler handler;
	private int fileCount;

	WebRequestManager requestManager;
	boolean isContinueTask;
	PlanTaskDao planTaskDao;
	tb_task_info data;
	// �����б�������ʾ
	List<tb_task_attachment> atts;
	/*
	 * ÿһ����Ŀ����һ��upload���棬ÿ��upload������mediaIndex����Ǹ����ڸ������е�λ�ã�
	 * �ڵ��delete��ť��ʱ�򣬾�ɾ����Ӧ�ĸ������ܹ���6��upload���棬���upload�����˳���ʱ��
	 * mediaindex�ᱻ��Ϊ0����ô�´ν�����ʱ�򣬸������±��0��ʼ�����������ͺ͸�����·������Ӧ��
	 */
	private int[] myMediaIndexs;

	private String DownloadfileFolder = Environment.getExternalStorageDirectory().getPath()
			+ "/nercms-Schedule/DownloadAttachments/";

	private int[] counts;// Ϊ�����������صĸ��������±�index
	boolean isClickShangchuanfujian = false;
	boolean hasUpload = false;

	private String videopath;
	boolean isEnd = false;

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
			iniActionBar(true, null, "��ҵ�ֳ�");
			break;
		case 2:
			iniActionBar(true, null, "�����ֳ�");
			break;
		case 3:
			iniActionBar(true, null, "���Ͻ��������ֳ�");
			break;
		}

		if (isAdmin()) {
			findViewById(R.id.bottom).setVisibility(View.GONE);
			findViewById(R.id.jieshurenwu).setVisibility(View.GONE);
		}

		bt_jieshurenwu = (Button) findViewById(R.id.jieshurenwu);
		bt_jieshurenwu.setOnClickListener(new OnClickListener() {

			// TODO ��������Ҫÿ����Ŀ���涼�����ݲ��ܽ���
			@Override
			public void onClick(View v) {
				if (planTaskDao == null)
					planTaskDao = new PlanTaskDao(XianChangAdd.this);
				if (!TextUtils.isEmpty(planTaskDao.getPlanTask(tid).getEnd_time())) {
					showShortToast("�����ѽ���");
					findViewById(R.id.bottom).setVisibility(View.GONE);
				} else {
					if (isfull()) {
						AlertDialog.Builder builder = new AlertDialog.Builder(XianChangAdd.this)
								.setMessage("�Ƿ��������");
						builder.setPositiveButton("��", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {
								// ���û��¼��ֱ�ӽ���
								if (mVideo.size() == 0) {
									Log.v("Demo", "XianChangAdd : " + handler.toString());
									requestManager.endTask(XianChangAdd.this, tid,
											System.currentTimeMillis() + "");
								}
								fileCount = getFileCount();// ��ȡ�ļ��ĸ������ϴ����finish��ǰҳ��
								// Log.e("TAG", "xianChangAdd fileCount : "+
								// fileCount);
								isClickShangchuanfujian = false;
								attachmentUploadRequest();// �ϴ�����

								isEnd = true;

							}
						});

						builder.setNegativeButton("��", new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog, int which) {

							}
						});

						AlertDialog dialog = builder.create();
						dialog.show();

					} else {
						Utils.showToast(XianChangAdd.this, "����ÿһ��и������ܽ���");
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
		// showShortToast("��ѡ�񸽼�");
		// } else {
		// if (hasUpload) {
		// showLongToast("�������ϴ�");
		// } else {
		// fileCount = getFileCount();// ��ȡ�ļ��ĸ������ϴ����finish��ǰҳ��
		// // Log.e("TAG", "xianChangAdd fileCount : "+ fileCount);
		// isClickShangchuanfujian = true;
		// attachmentUploadRequest();// �ϴ�����
		// }
		// }
		// }
		// });

		// �ϴ���Ƶ����
		bt_video.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction("android.media.action.VIDEO_CAPTURE");
				intent.addCategory("android.intent.category.DEFAULT");

				String fileName = Utils.getFileDate();
				videopath = NewTask.fileFolder + "/" + fileName + ".mp4";
				File file = new File(videopath);
				if (file.exists()) {
					file.delete();
				}
				Uri uri = Uri.fromFile(file);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				startActivityForResult(intent, LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);
			}
		});

		tv_time = (TextView) findViewById(R.id.time);
		if (planTaskDao == null)
			planTaskDao = new PlanTaskDao(this);
		if (data == null)
			data = planTaskDao.getPlanTask(tid);
		tv_time.setText(Utils.formatDateMs(data.getCreator_time()));
		mContentCount = new int[getItemCount(XianChangAdd.this)];

		mListView = (ListView) findViewById(R.id.listview);
		xianChangAddAdapter = new XianChangAddAdapter(this, enterType, mContentCount, isAdmin());
		mListView.setAdapter(xianChangAddAdapter);

		// myMediaIndexs = new int[6];
		myMediaIndexs = new int[xianChangAddAdapter.getCount()];
		// ��ʼ��
		for (int i = 0; i < xianChangAddAdapter.getCount(); i++) {
			List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
			mList.add(mItem);
		}
		for (int i = 0; i < xianChangAddAdapter.getCount(); i++) {
			List<Map<String, Object>> mItem = new ArrayList<Map<String, Object>>();
			mUploadList.add(mItem);
		}

		// mPath.add((String) getIntent().getCharSequenceExtra("path"));
		if (!isContinueTask) {// ���ǴӼ�������plantask���ҳ��������
			flag = getIntent().getIntExtra("position", -1);
			mList.get(flag).add((Map<String, Object>) getIntent().getSerializableExtra("path"));

			System.out.println("���ݹ�����λ����Ϣflag :" + flag);
		}

		// ����ǴӼ����������������ʼ�������б���ʾ����
		if (isContinueTask) {
			if (planTaskDao == null)
				planTaskDao = new PlanTaskDao(this);
			if (data == null)
				data = planTaskDao.getPlanTask(tid);
			if (!TextUtils.isEmpty(data.getEnd_time())) {
				v_bottom = (View) findViewById(R.id.bottom);
				v_bottom.setVisibility(View.GONE);
			}

			atts = planTaskDao.getPlanTaskAtt(tid);

			// atts.get(0).getStandard();
			// attsÿ������ԭ�����Ѿ��ϴ��ĸ����б�����getStandard�Ƿ��ص��Ǹ�����������Ŀ��

			counts = new int[xianChangAddAdapter.getCount()];

			// TODO ��Ҫ��������������
			for (tb_task_attachment attachment : atts) {
				String standard = attachment.getStandard();

				GpsDao gpsDao = new GpsDao(this);
				GPS gps = gpsDao.getHistory(attachment.getHistorygps());// �����ݿ��л�ȡgps��Ϣ
				MyGPS mGPS = new MyGPS(gps.getOllectionTime(), Double.valueOf(gps.getLongitude()),
						Double.valueOf(gps.getLatitude()), Float.valueOf(gps.getAccuracy()),
						Double.valueOf(gps.getHeight()), Float.valueOf(gps.getSpeed()),
						gps.getCoordinate());

				String mediaName = attachment.getUrl();
				String filePath = DownloadfileFolder + File.separator + mediaName;
				String downUrl = android.wxapp.service.elec.request.Contants.HFS_URL
						+ File.separator + mediaName;

				File file = new File(filePath);

				if (!file.getParentFile().exists())
					file.mkdirs();

				// ����ļ�δ���ڣ������ļ��Ѵ��ڵ��޷�ִ�л��߶�ȡ������������
				if (!file.exists() || (file.exists()
						&& (!file.canExecute() || !file.canRead() || !file.canWrite()))) {
					new HttpDownloadTask(XianChangAdd.this).execute(downUrl,
							"/nercms-Schedule/DownloadAttachments/", mediaName);// ��������������
				}

				// ������Ϣ
				Map<String, Object> mMap1 = new HashMap<String, Object>();
				mMap1.put("gps", mGPS);
				mMap1.put("path", filePath);
				mMap1.put("time", gps.getOllectionTime());// ���ݸ�����ʱ��

				if (standard.equals("standard01")) {
					mMap1.put("index", counts[0] + "");
					counts[0]++;
					mList.get(0).add(mMap1);
					mUploadList.get(0).add(mMap1);
				}
				if (standard.equals("standard02")) {
					mMap1.put("index", counts[1] + "");
					counts[1]++;
					mList.get(1).add(mMap1);
					mUploadList.get(1).add(mMap1);
				}
				if (standard.equals("standard03")) {
					mMap1.put("index", counts[2] + "");
					counts[2]++;
					mList.get(2).add(mMap1);
					mUploadList.get(2).add(mMap1);
				}
				if (standard.equals("standard04")) {
					mMap1.put("index", counts[3] + "");
					counts[3]++;
					mList.get(3).add(mMap1);
					mUploadList.get(3).add(mMap1);
				}
				if (standard.equals("standard05")) {
					mMap1.put("index", counts[4] + "");
					counts[4]++;
					mList.get(4).add(mMap1);
					mUploadList.get(4).add(mMap1);
				}
				if (standard.equals("standard06")) {
					mMap1.put("index", counts[5] + "");
					counts[5]++;
					mList.get(5).add(mMap1);
					mUploadList.get(5).add(mMap1);
				}

				// �����ֳ�
				if (standard.equals("standard07")) {
					mMap1.put("index", counts[0] + "");
					counts[0]++;
					mList.get(0).add(mMap1);
					mUploadList.get(0).add(mMap1);
				}

				if (standard.equals("standard08")) {
					mMap1.put("index", counts[1] + "");
					counts[1]++;
					mList.get(1).add(mMap1);
					mUploadList.get(1).add(mMap1);
				}

				if (standard.equals("standard09")) {
					mMap1.put("index", counts[2] + "");
					counts[2]++;
					mList.get(2).add(mMap1);
					mUploadList.get(2).add(mMap1);
				}

				if (standard.equals("standard10")) {
					mMap1.put("index", counts[3] + "");
					counts[3]++;
					mList.get(3).add(mMap1);
					mUploadList.get(3).add(mMap1);
				}

				if (standard.equals("standard11")) {
					mMap1.put("index", counts[4] + "");
					counts[4]++;
					mList.get(4).add(mMap1);
					mUploadList.get(4).add(mMap1);
				}

				if (standard.equals("standard")) {
					mVideo = mMap1;
					bt_video.setText("¼��1��");
					bt_video.setClickable(false);
				}
			}
			System.out.println("XianChangAdd atts : " + atts.toString());
		}
		// }

		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (isAdmin()) {
					if (mList.get(position).size() == 0) {
						showLongToast("û�и�����Ϣ�鿴");
						return;
					}
				}
				Intent intent = new Intent(XianChangAdd.this, XianChangUpload.class);
				intent.putExtra("tid", tid);
				if (from.equals("XianChangUpload")) {
					intent.putExtra("mediaIndex", mediaIndex);
					System.out.println("����mediaIndex��ֵ����¼�ϴθ������±�");
				}

				boolean enterFlag = false;
				List<String> mFileName = new ArrayList<String>();
				if (isContinueTask) {// �ǴӼ�������plantask���ҳ����������
					for (int i = 0; i < xianChangAddAdapter.getCount(); i++) {
						myMediaIndexs[i] = counts[i];
					}
					intent.putExtra("myMediaIndex", myMediaIndexs[position]);

					isContinueTask = false;
				} else {
					setMediaIndex();
					intent.putExtra("myMediaIndex", myMediaIndexs[position]);

				}

				for (List<Map<String, Object>> ma : mList) {
					for (Map<String, Object> mm : ma) {
						if (mm != null) {
							String attachmentFilePath = (String) mm.get("path");
							mFileName.add(attachmentFilePath);
						}

					}
				}

				if (mFileName.size() != 0) {
					File attachmentFile = new File(mFileName.get(mFileName.size() - 1));
					if (!attachmentFile.getParentFile().exists())
						attachmentFile.mkdirs();
					if (!attachmentFile.exists()) {
						Toast.makeText(XianChangAdd.this, "������������", Toast.LENGTH_SHORT).show();
						// enterFlag = true;
						return;
					}
				}

				// �����е��ļ��������뵽List�У������һ���ļ����ڵ�ʱ���������
				// if (enterFlag == true){
				// return;//Ŀ���ǲ���iscontinueTask��Ϊfalse
				// }

				intent.putExtra("mUploadUrl", (Serializable) mUploadList.get(position));
				intent.putExtra("url", (Serializable) mList.get(position));
				intent.putExtra("position", position);// �ѱ��������Ŀ��λ�ô��ݽ�ȥ�������ڽ��յ�ʱ���֪���øøı��ĸ�list������
				intent.putExtra("enterType", enterType);
				XianChangAdd.this.startActivityForResult(intent,
						LocalConstant.START_XIANCHANGUPLOAD);
			}
		});

		initHandler();

	}

	protected boolean isfull() {
		int flag = 0;
		// �ж�ÿ����Ŀ�Ƿ�������
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
		if (enterType == 1) {
			ss = c.getResources().getStringArray(R.array.zuoyexianchang_si_data);
		} else if (enterType == 2) {
			ss = c.getResources().getStringArray(R.array.caozuoxianchang_si_data);
		} else if (enterType == 3) {
			ss = c.getResources().getStringArray(R.array.guzhangjinji_si_data);
		}

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
					if (map.get("path") != null) {// ����map��get������ǰ����map != null
						content++;
					}
				}
			}
		}
		return content;
	}

	String from = "";
	int mediaIndex;
	private Map<String, Object> mVideo = new HashMap<String, Object>();// ���¼�Ƶ���Ƶ��Ϣ

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case LocalConstant.START_XIANCHANGUPLOAD:
			if (resultCode == LocalConstant.SELECT_ATTACHMENT) {
				int index = data.getIntExtra("position", -1);
				from = data.getStringExtra("from");
				mediaIndex = data.getIntExtra("mediaIndex", 0);
				mContent = (List<Map<String, Object>>) data.getSerializableExtra("url");
				mList.set(index, mContent);// �ı���Upload�����Ӧ�����ĵ�����

				mUploadUrl = (List<Map<String, Object>>) data.getSerializableExtra("mUploadUrl");
				mUploadList.set(index, mUploadUrl);
				myMediaIndexs[index] = mediaIndex;
			}
			break;

		// ¼��
		case LocalConstant.CAPTURE_VIDEO_REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				Log.i("TAG", "������ɣ�resultCode=" + requestCode);

				File file = new File(videopath);

				Bitmap videoThumbnailBitmap = Utils.getVideoThumbnail(videopath, 400, 400,
						MediaStore.Images.Thumbnails.MINI_KIND);

				// uri4 = Uri.fromFile(file);
				// mediaID4 = mediaIndex++;

				String videoName = videopath.substring(videopath.lastIndexOf(File.separator) + 1);
				System.out.println("videoName: " + videoName);

				Intent intent = new Intent(XianChangAdd.this, ShowXianChangAttachment.class);
				Map<String, String> mMap = new HashMap<String, String>();
				mMap.put("type", "video");
				mMap.put("path", videopath);
				mMap.put("from", "XianChangUpload");
				// mMap.put("index", mediaID4 + "");// ���ݸ������±�

				Map<Integer, Map<String, String>> mContent = new HashMap<Integer, Map<String, String>>();
				mContent.put(0, mMap);
				intent.putExtra("address", (Serializable) mContent);
				startActivityForResult(intent, LocalConstant.SHOWXIANCHANG_ATTACHMENT);

			}
			break;

		case LocalConstant.SHOWXIANCHANG_ATTACHMENT:
			if (resultCode == RESULT_OK) {
				mVideo = (Map<String, Object>) data.getSerializableExtra("path");

				bt_video.setText("¼�� (1)");
				bt_video.setClickable(false);
			}

			break;

		default:
			break;
		}

	}

	// �ı�list����ɫ
	private void changeTextColor() {
		int position = 0;
		// ����mList�����Ƿ��������������������ɫ
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
				// System.out.println("XianChangAdd û��������");
				xianChangAddAdapter.setContent(position, 0);
			}
			position++;
		}
	}

	private void attachmentUploadRequest() {
		if (!Utils.isNetworkAvailable(XianChangAdd.this)) {
			Toast.makeText(XianChangAdd.this, "���粻����", Toast.LENGTH_SHORT).show();
			return;
		}
		String uploadUrl = android.wxapp.service.elec.request.Contants.HFS_URL;
		if (getFileCount() < 1) {
			showLongToast("��ѡ�񸽼��ϴ�");
		} else {

			if (mVideo.size() != 0) {
				// �ϴ���Ƶ
				new HttpUploadTask(new TextView(this), this).execute((String) mVideo.get("path"),
						uploadUrl);
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

					Toast.makeText(XianChangAdd.this, "�ϴ��ɹ�", Toast.LENGTH_SHORT).show();
					// ����http�ӿ�
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
						// �����޸�
						GPS gps = new GPS(getUserId(),
								Utils.formatDateMs(System.currentTimeMillis()),
								myGPS.getLongitude() + "", myGPS.getLatitude() + "", "",
								myGPS.getRadius() + "", myGPS.getAltitude() + "",
								myGPS.getSpeed() + "",
								Utils.formatDateMs(System.currentTimeMillis()), myGPS.getCoorType(),
								"");

						String md5 = Utils.getFileMD5(new File(filePath));
						Attachments att = new Attachments(type,
								server + File.separator + path2FileName(filePath),
								(String) attItem.get("time"), gps, md5);
						sublist.add(att);

					}
					TaskAttachment item = new TaskAttachment(standard.toString(), sublist);
					attachment.add(item);

					requestManager.uploadTaskAttachment(XianChangAdd.this, tid, enterType + "",
							attachment);

					// fileCount--;
					// Log.i("TAG", "count : " + fileCount);
					// if (fileCount == 0) {
					//
					// Toast.makeText(XianChangAdd.this, "�ϴ��ɹ�",
					// Toast.LENGTH_SHORT).show();
					// // ����http�ӿ�
					// List<TaskAttachment> attachment = new
					// ArrayList<TaskAttachment>();
					// for (int i = 0; i < mList.size(); i++) {
					// StringBuilder standard = new StringBuilder(
					// "standard");
					// // ��ҵ�ֳ�
					// if (enterType == 1) {
					// switch (i) {
					// // ����Ʊ
					// case 0:
					// standard.append("01");
					// break;
					// case 1:
					// standard.append("02");
					// break;
					// case 2:
					// standard.append("03");
					// break;
					// case 3:
					// standard.append("04");
					// break;
					// case 4:
					// standard.append("05");
					// break;
					// case 5:
					// standard.append("06");
					// break;
					// }
					// }
					// // �����ֳ�
					// else if (enterType == 2) {
					// switch (i) {
					// case 0:
					// standard.append("07");
					// break;
					// case 1:
					// standard.append("08");
					// break;
					// case 2:
					// standard.append("09");
					// break;
					// case 3:
					// standard.append("10");
					// break;
					// case 4:
					// standard.append("11");
					// break;
					// }
					// }
					// // ��������
					// else if (enterType == 3) {
					// switch (i) {
					// case 0:
					// standard.append("01");
					// break;
					// case 1:
					// standard.append("02");
					// break;
					// case 2:
					// standard.append("03");
					// break;
					// case 3:
					// standard.append("04");
					// break;
					// case 4:
					// standard.append("05");
					// break;
					// case 5:
					// standard.append("06");
					// break;
					// }
					// }
					//
					// List<Attachments> sublist = new ArrayList<Attachments>();
					// String server =
					// android.wxapp.service.elec.request.Contants.HFS_URL;
					// for (int j = 0; j < mList.get(i).size(); j++) {
					// Map<String, Object> attItem = mList.get(i).get(
					// j);
					//
					// if (attItem == null) {
					// return;
					// }
					//
					// String filePath = (String) attItem.get("path");
					// String type = Utils.judgeFileLeixin(filePath);
					// if (type != null) {
					//
					// MyGPS myGPS = (MyGPS) attItem.get("gps");
					// // �����޸�
					// GPS gps = new GPS(getUserId(),
					// Utils.formatDateMs(System
					// .currentTimeMillis()),
					// myGPS.getLongitude() + "",
					// myGPS.getLatitude() + "", "",
					// myGPS.getRadius() + "",
					// myGPS.getAltitude() + "",
					// myGPS.getSpeed() + "",
					// Utils.formatDateMs(System
					// .currentTimeMillis()),
					// myGPS.getCoorType(), "");
					//
					// String md5 = Utils.getFileMD5(new File(
					// filePath));
					// Attachments att = new Attachments(type,
					// server + File.separator
					// + path2FileName(filePath),
					// (String) attItem.get("time"), gps,
					// md5);
					// sublist.add(att);
					//
					// }
					// }
					// TaskAttachment item = new TaskAttachment(
					// standard.toString(), sublist);
					// attachment.add(item);
					// }
					//
					// requestManager.uploadTaskAttachment(XianChangAdd.this,
					// tid, enterType + "", attachment);
					//
					// }

					break;
				case Constant.FILE_UPLOAD_FAIL:
					Toast.makeText(XianChangAdd.this, "�ϴ�ʧ��", Toast.LENGTH_SHORT).show();
					break;

				case Constants.UPLOAD_TASK_ATT_SUCCESS:
					showLog_e("�ϴ�����д�����ݿ�ɹ�");
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
					showShortToast("�����ѽ���");
					findViewById(R.id.bottom).setVisibility(View.GONE);
					break;

				case Constants.UPLOAD_TASK_ATT_SAVE_FAIL:
				case Constants.UPLOAD_TASK_ATT_FAIL:
				case Constants.END_TASK_FAIL:
				case Constants.END_TASK_SAVE_FAIL:
					if (msg.obj != null) {
						showAlterDialog("�ϴ�ʧ��", ((NormalServerResponse) msg.obj).getEc(),
								R.drawable.login_error_icon, "ȷ��", null);
					} else {
						showAlterDialog("�ϴ�ʧ��", "�����Ƿ����������������", R.drawable.login_error_icon, "ȷ��",
								null);
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
		// ����mList��ȡÿ����Ŀ����Ŀ
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
		changeTextColor();// ���³��ֵ�ʱ��������� ��Ϊoncreateִֻ��һ��

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
	protected void onPause() {
		Log.v("Demo", getClass().getSimpleName() + "onPause");
		super.onPause();

		// MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_FAIL,
		// "XianChangAdd");
		// MessageHandlerManager.getInstance().unregister(Constant.FILE_UPLOAD_SUCCESS,
		// "XianChangAdd");
		//
		// MessageHandlerManager.getInstance().unregister(Constants.UPLOAD_TASK_ATT_SUCCESS,
		// UploadTaskAttachmentResponse.class.getName());
		// MessageHandlerManager.getInstance().unregister(Constants.UPLOAD_TASK_ATT_SAVE_FAIL,
		// UploadTaskAttachmentResponse.class.getName());
		// MessageHandlerManager.getInstance().unregister(Constants.UPLOAD_TASK_ATT_FAIL,
		// UploadTaskAttachmentResponse.class.getName());
		//
		// MessageHandlerManager.getInstance().unregister(Constants.END_TASK_SUCCESS,
		// StartTaskResponse.class.getName());
		// MessageHandlerManager.getInstance().unregister(Constants.END_TASK_FAIL,
		// StartTaskResponse.class.getName());
		// MessageHandlerManager.getInstance().unregister(Constants.END_TASK_SAVE_FAIL,
		// StartTaskResponse.class.getName());
	}

	@Override
	protected void onStop() {
		Log.v("Demo", getClass().getSimpleName() + "onStop");
		super.onStop();
	}

}
