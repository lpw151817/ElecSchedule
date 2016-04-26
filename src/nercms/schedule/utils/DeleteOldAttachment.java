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

	// 单键处理
	private volatile static DeleteOldAttachment _unique_instance = null;

	private volatile static Thread attachment_delete_thread = null;

	// 绝对不可采用同步方法的方式，同步方法仅对类的一个实例起作用
	public static DeleteOldAttachment instance() {

		// 检查实例,如是不存在就进入同步代码区
		if (null == _unique_instance) {
			// 对其进行锁,防止两个线程同时进入同步代码区
			synchronized (DeleteOldAttachment.class) {
				// 必须双重检查
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

		// 查询数据库
		// 查最早一个未上传的附件
		_query_result = AttachmentDatabase.instance(_ctx)
				.query("select * from tb_task_attachment;");

		if (null == _query_result)
			return;

		int num = Integer.parseInt(_query_result.get("records_num"));

		for (int i = 0; i < num; ++i) {

			// 表名：任务现场附件表（tb_task_attachment）
			// 字段名称 类型 说明
			// id int 记录ID（自增量）
			// task_id int 任务id
			// historygps int 历史位置id
			// standard text 评价标准(数据字典)
			// type text 附件类型(数据字典)：图片；音频；视频
			// url text 附件链接
			// upload_time text 上传时间
			// md5 text MD5值
			// status text '0'-未上传，'1'-已上传HFS，'2'-已调用http接口通知服务器上传成功，

			String id = _query_result.get("id_" + String.valueOf(i));
			String task_id = _query_result.get("task_id_" + String.valueOf(i));
			String url = _query_result.get("url_" + String.valueOf(i));
			String upload_time = _query_result.get("upload_time_" + String.valueOf(i));
			String status = _query_result.get("status_" + String.valueOf(i));

			// 时戳过长（如15天以上）则删除文件，并删除记录
			if (15 * 24 * 3600 * 1000 <= System.currentTimeMillis() - Long.parseLong(upload_time)) {
				// 删除DB记录
				AttachmentDatabase.instance(_ctx)
						.execute("delete from tb_task_attachment where id = " + id + ";");
				// 删除文件
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
	 * 删除单个文件
	 * 
	 * @param filePath
	 *            被删除文件的文件名
	 * @return 文件删除成功返回true，否则返回false
	 */
	public boolean deleteFile(String filePath) {
		File file = new File(filePath);
		if (file.isFile() && file.exists()) {
			return file.delete();
		}
		return false;
	}

	/**
	 * 删除文件夹以及目录下的文件
	 * 
	 * @param filePath
	 *            被删除目录的文件路径
	 * @return 目录删除成功返回true，否则返回false
	 */
	public boolean deleteDirectory(String filePath) {
		boolean flag = false;
		// 如果filePath不以文件分隔符结尾，自动添加文件分隔符
		if (!filePath.endsWith(File.separator)) {
			filePath = filePath + File.separator;
		}
		File dirFile = new File(filePath);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		File[] files = dirFile.listFiles();
		// 遍历删除文件夹下的所有文件(包括子目录)
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				// 删除子文件
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} else {
				// 删除子目录
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前空目录
		return dirFile.delete();
	}

	/**
	 * 根据路径删除指定的目录或文件，无论存在与否
	 * 
	 * @param filePath
	 *            要删除的目录或文件
	 * @return 删除成功返回 true，否则返回 false。
	 */
	public boolean DeleteFolder(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return false;
		} else {
			if (file.isFile()) {
				// 为文件时调用删除文件方法
				return deleteFile(filePath);
			} else {
				// 为目录时调用删除目录方法
				return deleteDirectory(filePath);
			}
		}
	}
}
