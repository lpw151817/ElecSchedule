package nercms.schedule.utils;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import nercms.schedule.activity.NewTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;
import android.wxapp.service.elec.dao.GpsDao;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.model.UploadTaskAttachmentRequest;
import android.wxapp.service.elec.model.UploadTaskAttachmentResponse;
import android.wxapp.service.elec.model.bean.Attachments;
import android.wxapp.service.elec.model.bean.TaskAttachment;
import android.wxapp.service.elec.model.bean.table.tb_task_attachment;
import android.wxapp.service.elec.model.bean.table.tb_task_info;
import android.wxapp.service.elec.request.Contants;
import android.wxapp.service.util.CustomMultipartEntity;
import android.wxapp.service.util.MySharedPreference;
import android.wxapp.service.util.CustomMultipartEntity.ProgressListener;

public class DeleteOldAttachment {
	public static Object _exit_flag = new Object();

	private static Context _ctx = null;

	// private static Handler _handler = null;

	// ��������
	private volatile static DeleteOldAttachment _unique_instance = null;

	private volatile static Thread attachment_delete_thread = null;

	// ���Բ��ɲ���ͬ�������ķ�ʽ��ͬ�������������һ��ʵ��������
	public static DeleteOldAttachment instance() {

		// ���ʵ��,���ǲ����ھͽ���ͬ��������
		if (null == _unique_instance) {
			// ���������,��ֹ�����߳�ͬʱ����ͬ��������
			synchronized (DeleteOldAttachment.class) {
				// ����˫�ؼ��
				if (null == _unique_instance) {
					_unique_instance = new DeleteOldAttachment();
					iniThread();
				}
			}
		}

		return _unique_instance;
	}

	private static void iniThread() {
		attachment_delete_thread = new Thread() {
			@Override
			public void run() {
				try {
					// _query_result =
					// AttachmentDatabase.instance(_ctx).query(
					// "select * from tb_task_attachment order
					// by id asc;");
					//
					// if (null == _query_result)
					// return;
					//
					// int num =
					// Integer.parseInt(_query_result.get("records_num"));
					//
					// for(int i = 0; i < num; ++i)
					// {
					// String id = _query_result.get("id_" +
					// String.valueOf(i));
					// String task_id =
					// _query_result.get("task_id_" +
					// String.valueOf(i));
					// String url = _query_result.get("url_" +
					// String.valueOf(i));
					// String upload_time =
					// _query_result.get("upload_time_"
					// + String.valueOf(i));
					// String status =
					// _query_result.get("status_" +
					// String.valueOf(i));
					//
					// Log.v("login", "atta: " + id + ", " + ",
					// " + task_id
					// + ", " + url + ", " + upload_time + ", "
					// + status);
					// }
					//
					// if(true) return;

					deleteAttachment();

				} catch (Exception e) {
					Log.v("Baidu", "upload attachment error: " + e.toString());
				} finally {
					attachment_delete_thread = null;
				}
			}
		};
	}

	public synchronized void start(Context context) {
		// Log.d("before", "time");
		// public static void start(Context context) {
		_ctx = context;
		// _handler = handler;

		if (null == attachment_delete_thread)
			iniThread();
		attachment_delete_thread.start();

	}

	private static HashMap<String, String> _query_result = null;

	public static String fileFolder = Environment.getExternalStorageDirectory().getPath()
			+ "/nercms-Schedule/DownloadAttachments/";

	private static void deleteAttachment() {
		Log.d("before", "deleteAttachment");

		// ��ѯ���ݿ�
		// ������һ��δ�ϴ��ĸ���
		_query_result = AttachmentDatabase.instance(_ctx)
				.query("select * from tb_task_attachment;");

		if (null == _query_result)
			return;

		int num = Integer.parseInt(_query_result.get("records_num"));

		for (int i = 0; i < num; ++i) {

			// �����������ֳ�������tb_task_attachment��
			// �ֶ����� ���� ˵��
			// id int ��¼ID����������
			// task_id int ����id
			// historygps int ��ʷλ��id
			// standard text ���۱�׼(�����ֵ�)
			// type text ��������(�����ֵ�)��ͼƬ����Ƶ����Ƶ
			// url text ��������
			// upload_time text �ϴ�ʱ��
			// md5 text MD5ֵ
			// status text '0'-δ�ϴ���'1'-���ϴ�HFS��'2'-�ѵ���http�ӿ�֪ͨ�������ϴ��ɹ���

			String id = _query_result.get("id_" + String.valueOf(i));
			String task_id = _query_result.get("task_id_" + String.valueOf(i));
			String url = _query_result.get("url_" + String.valueOf(i));
			String upload_time = _query_result.get("upload_time_" + String.valueOf(i));
			String status = _query_result.get("status_" + String.valueOf(i));

			// ʱ����������15�����ϣ���ɾ���ļ�����ɾ����¼
			if (15 * 24 * 3600 * 1000 <= System.currentTimeMillis() - Long.parseLong(upload_time)) {
				// ɾ��DB��¼
				AttachmentDatabase.instance(_ctx)
						.execute("delete from tb_task_attachment where id = " + id + ";");
				// ɾ���ļ�
				File file = new File(fileFolder + url);
				if (file.exists())
					file.delete();
				Log.v("before", "delete atta: " + id + ", " + "," + task_id + ", " + url + ", "
						+ upload_time + ", " + status);
			}

		}

	}

	protected static String getUserId(Context c) {
		return MySharedPreference.get(c, MySharedPreference.USER_ID, null);
	}

	protected static String getUserIc(Context context) {
		return MySharedPreference.get(context, MySharedPreference.USER_IC, null);
	}

	protected static Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

	protected static String parase2Json(Object o) {
		String encode = URLEncoder.encode(gson.toJson(o));
		Log.e("BaseRequest", (URLDecoder.decode(encode).equals(gson.toJson(o))) + "");
		return encode;
	}

	/**
	 * ɾ�������ļ�
	 * 
	 * @param filePath
	 *            ��ɾ���ļ����ļ���
	 * @return �ļ�ɾ���ɹ�����true�����򷵻�false
	 */
	public boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			return file.delete();
		}
		return false;
	}

	/**
	 * ɾ���ļ����Լ�Ŀ¼�µ��ļ�
	 * 
	 * @param filePath
	 *            ��ɾ��Ŀ¼���ļ�·��
	 * @return Ŀ¼ɾ���ɹ�����true�����򷵻�false
	 */
	public boolean deleteDirectory(String filePath) {
		boolean flag = false;
		// ���filePath�����ļ��ָ�����β���Զ�����ļ��ָ���
		if (!filePath.endsWith(File.separator)) {
			filePath = filePath + File.separator;
		}
		File dirFile = new File(filePath);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		File[] files = dirFile.listFiles();
		// ����ɾ���ļ����µ������ļ�(������Ŀ¼)
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				// ɾ�����ļ�
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} else {
				// ɾ����Ŀ¼
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// ɾ����ǰ��Ŀ¼
		return dirFile.delete();
	}

	/**
	 * ����·��ɾ��ָ����Ŀ¼���ļ������۴������
	 * 
	 * @param filePath
	 *            Ҫɾ����Ŀ¼���ļ�
	 * @return ɾ���ɹ����� true�����򷵻� false��
	 */
	public boolean DeleteFolder(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return false;
		} else {
			if (file.isFile()) {
				// Ϊ�ļ�ʱ����ɾ���ļ�����
				return deleteFile(filePath);
			} else {
				// ΪĿ¼ʱ����ɾ��Ŀ¼����
				return deleteDirectory(filePath);
			}
		}
	}
}
