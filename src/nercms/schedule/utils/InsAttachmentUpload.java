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
import com.imooc.treeview.utils.Node;

import android.R.integer;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;
import android.wxapp.service.elec.dao.DatabaseHelper;
import android.wxapp.service.elec.dao.GpsDao;
import android.wxapp.service.elec.dao.PlanTaskDao;
import android.wxapp.service.elec.dao.TaskInsDao;
import android.wxapp.service.elec.model.CreateInsRequest;
import android.wxapp.service.elec.model.UploadTaskAttachmentRequest;
import android.wxapp.service.elec.model.UploadTaskAttachmentResponse;
import android.wxapp.service.elec.model.bean.Attachments;
import android.wxapp.service.elec.model.bean.TaskAttachment;
import android.wxapp.service.elec.model.bean.Uid;
import android.wxapp.service.elec.model.bean.table.tb_task_attachment;
import android.wxapp.service.elec.model.bean.table.tb_task_info;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions_attachment;
import android.wxapp.service.elec.request.Contants;
import android.wxapp.service.util.CustomMultipartEntity;
import android.wxapp.service.util.MySharedPreference;
import android.wxapp.service.util.CustomMultipartEntity.ProgressListener;

public class InsAttachmentUpload {
	public static Object _exit_flag = new Object();

	private static ScheduledExecutorService _network_status_service = null;

	private static Context _ctx = null;

	public static boolean _running = false;

	// private static Handler _handler = null;

	// 单键处理
	private volatile static InsAttachmentUpload _unique_instance = null;

	private volatile static Thread attachment_upload_thread = null;

	// 绝对不可采用同步方法的方式，同步方法仅对类的一个实例起作用
	public static InsAttachmentUpload instance() {

		// 检查实例,如是不存在就进入同步代码区
		if (null == _unique_instance) {
			// 对其进行锁,防止两个线程同时进入同步代码区
			synchronized (InsAttachmentUpload.class) {
				// 必须双重检查
				if (null == _unique_instance) {
					_unique_instance = new InsAttachmentUpload();

					attachment_upload_thread = new Thread() {
						@Override
						public void run() {
							try {
								while (true) {
									upload_attachement();
									Thread.sleep(1000);
								}

							} catch (Exception e) {
								Log.v("Baidu", "upload attachment error: " + e.toString());
							}
						}
					};
					_network_status_service = Executors.newScheduledThreadPool(1);
					_network_status_service.schedule(attachment_upload_thread, 100,
							TimeUnit.MILLISECONDS);
				}
			}
		}

		return _unique_instance;
	}

	public synchronized void start(Context context) {
		_ctx = context;

	}

	public void stop(Context context) {
		_network_status_service.shutdownNow();
	}

	private static HashMap<String, String> _query_result = null;

	private static void upload_attachement() {
		if (false == _running)
			return;

		// 查询数据库
		// 查最早一个未上传的附件
		_query_result = AttachmentDatabase.instance(_ctx).query(
				"select * from tb_task_instructions_attachment where status = '0' or status = '1' order by id asc limit 1;");

		if (null == _query_result)
			return;

		Log.d("JAMES", _query_result.toString());

		// "id";
		// "instructions_id";
		// "type";
		// "url";
		// "update_time";
		// "md5";
		// "status";

		String id = _query_result.get("id_0");
		String instructions_id = _query_result.get("instructions_id_0");
		String type = _query_result.get("type_0");
		String url = _query_result.get("url_0");
		String update_time = _query_result.get("update_time_0");
		String md5 = _query_result.get("md5_0");
		String status = _query_result.get("status_0");

		Log.v("before", "url:" + url);

		// 时戳过长（如7天以上）则不必处理
		if (7 * 24 * 3600 * 1000 <= System.currentTimeMillis() - Long.parseLong(update_time))
			return;

		if (true == status.equalsIgnoreCase("0")) {
			// HFS上传
			boolean ret = false;// 上传成功与否
			// ...
			tb_task_instructions_attachment uploadAtt = new tb_task_instructions_attachment(id,
					instructions_id, type, url, update_time, md5, status);
			String prefix = NewTask.fileFolder;
			// 前缀处理
			String nameWithStart = uploadAtt.getUrl();
			// String fileName = nameWithStart
			// .substring(nameWithStart.lastIndexOf(File.separator) + 1);
			// String uploadUrl =
			// android.wxapp.service.elec.request.Contants.HFS_URL +
			// File.separator
			// + nameWithStart.substring(0,
			// nameWithStart.lastIndexOf(File.separator));

			TaskInsDao insDao = new TaskInsDao(_ctx);
			tb_task_instructions instruction = insDao.getTaskIns(instructions_id, "1");

			String uploadUrl = android.wxapp.service.elec.request.Contants.HFS_URL + File.separator
					+ instruction.getTask_id() + File.separator + "C" + File.separator;

			// new HttpUploadTask(null, _ctx, uploadAtt).execute(
			// prefix + fileName, uploadUrl);

			try {
				String result = post(prefix + uploadAtt.getUrl(), uploadUrl);
				if (result.equals("success")) {
					ret = true;
				} else {
					ret = false;
				}

			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (true == ret) {
				AttachmentDatabase.instance(_ctx).execute(
						"update tb_task_instructions_attachment set status='1' where id = '" + id
								+ "';");
				// AttachmentDatabase.instance(_ctx).complete_transaction();
				status = "1";
				Map<String, String> _query_result = AttachmentDatabase.instance(_ctx).query(
						"select * from tb_task_instructions_attachment where id=" + id + ";");
				Log.v("login", _query_result.get("id_0") + ":" + _query_result.get("url_0") + ":"
						+ _query_result.get("status_0"));
			}
		}

		if (_running == false)
			return;

		if (true == status.equalsIgnoreCase("1")) {
			// http通知服务器

			// http通知服务器
			String json_request;
			// 构建json请求字符串
			// ...
			List<Attachments> sublist = new ArrayList<Attachments>();
			Attachments att = new Attachments(type,
					android.wxapp.service.elec.request.Contants.HFS_URL + File.separator + url,
					update_time, null, md5);
			sublist.add(att);

			TaskInsDao insDao = new TaskInsDao(_ctx);
			tb_task_instructions instruction = insDao.getTaskIns(instructions_id, "1");

			List<Uid> uidsList = new ArrayList<Uid>();
			for (Node uid : insDao.getMsgReceivers(instruction.getTask_id())) {
				// 过滤掉自己
				if (type.equals("0")) {
					if (!uid.getId().substring(1).equals(getUserId(_ctx)))
						uidsList.add(new Uid(uid.getId().substring(1)));
				} else {
					if (!uid.getId().equals(getUserId(_ctx)))
						uidsList.add(new Uid(uid.getId()));
				}
			}

			CreateInsRequest request = new CreateInsRequest(getUserId(_ctx), getUserIc(_ctx),
					uidsList, instruction.getTask_id(), instruction.getContent(), sublist, "1");

			Log.d("JAMES", request.toString());

			json_request = parase2Json(request);
			Object[] result = upload_attachment_http_request(json_request);// 通知成功与否;
			boolean ret = (Boolean) result[0];

			if (true == ret) {

				AttachmentDatabase.instance(_ctx).execute(
						"update tb_task_instructions_attachment set status='2' where id = '" + id
								+ "';");

				// AttachmentDatabase.instance(_ctx).complete_transaction();

				status = "2";

				// _handler.post(new Runnable() {
				//
				// @Override
				// public void run() {
				//
				// Toast.makeText(_ctx, "上传成功", Toast.LENGTH_SHORT).show();
				// }
				// });
			}
		}
	}

	private static Object[] upload_attachment_http_request(String json_request) {
		HttpClientOperation hco = new HttpClientOperation();

		// List<NameValuePair> params = new ArrayList<NameValuePair>();
		//
		// // 添加参数
		// params.add(new
		// BasicNameValuePair(Contants.UPLOAD_TASK_ATTACHMENT_PARAM,
		// json_request));

		Map params = new HashMap<String, String>();
		params.put(Contants.CREATETASKINS_PARAM.substring(0,
				Contants.CREATETASKINS_PARAM.length() - 1), json_request);

		// 设置http连接的参数
		hco.getHttpClient();
		Log.d("Baidu", params.toString());

		// 向http服务器发送json字符串，返回从http服务器端获取的字符串
		// String result = hco.doPost(Contants.SERVER_URL + Contants.MODEL_NAME
		// + Contants.UPLOAD_TASK_ATTACHMENT_METHOD, params);
		String result = hco
				.doGet(Contants.SERVER_URL + Contants.MODEL_NAME + Contants.CREATETASKINS_METHOD
						.substring(0, Contants.CREATETASKINS_METHOD.length() - 1), params);

		Object[] rr = new Object[2];

		if (null == result || 0 == result.length() || result.equalsIgnoreCase("{}")) {
			rr[0] = false;
			return rr;
		}
		if (false == result.contains("\"s\":\"0\"")) {
			rr[0] = false;
			return rr;
		}
		rr[0] = true;
		rr[1] = new Gson().fromJson(result, UploadTaskAttachmentResponse.class);
		return rr;
	}

	private static String post(String pathToOurFile, String urlServer)
			throws ClientProtocolException, IOException, JSONException {
		String result = null;
		if (!URLUtil.isNetworkUrl(urlServer)) {
			result = "服务器地址无效";
			return result;
		}

		HttpClient httpclient = new DefaultHttpClient();
		// 设置通信协议版本
		httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION,
				HttpVersion.HTTP_1_1);

		HttpPost httppost = new HttpPost(urlServer);
		File file = new File(pathToOurFile);

		FileBody cbFile = new FileBody(file);

		CustomMultipartEntity multipartContent = new CustomMultipartEntity(new ProgressListener() {
			@Override
			public void transferred(long num) {
				// TODO Auto-generated method stub
				// publishProgress((int) ((num / (float) totalSize) *
				// 100));
			}

		});

		multipartContent.addPart("data", cbFile);
		// totalSize = multipartContent.getContentLength();
		// mpEntity.addPart("userfile", cbFile); // <input type="file"
		// name="userfile" /> 对应的

		httppost.setEntity(multipartContent);
		System.out.println("executing request " + httppost.getRequestLine());

		HttpParams params = httpclient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 3000);
		HttpResponse response = httpclient.execute(httppost);

		System.out.println(response.getStatusLine());// 通信Ok

		if (response.getStatusLine().getStatusCode() == 200)
			result = "success";
		else
			result = "failed";

		httpclient.getConnectionManager().shutdown();

		return result;

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

}
