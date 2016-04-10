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

public class AttachmentUpload {
	public static Object _exit_flag = new Object();

	private static ScheduledExecutorService _network_status_service = null;

	private static Context _ctx = null;

	private static Handler _handler = null;

	public static void start(Context context, Handler handler) {
		// public static void start(Context context) {
		_ctx = context;
		_handler = handler;

		Thread attachment_upload_thread = new Thread() {
			@Override
			public void run() {
				try {
					while (true) {
						upload_attachement();
						Log.d("JAMES", "�߳���������");
						Thread.sleep(1000);
					}

				} catch (Exception e) {
					Log.v("Baidu", "upload attachment error: " + e.toString());
				}
			}
		};

		_network_status_service = Executors.newScheduledThreadPool(1);
		_network_status_service.scheduleAtFixedRate(attachment_upload_thread,
				100, 100, TimeUnit.MILLISECONDS);
	}

	public static void stop(Context context) {
		_network_status_service.shutdownNow();
	}

	private static HashMap<String, String> _query_result = null;

	private static void upload_attachement() {
		// ��ѯ���ݿ�
		// ������һ��δ�ϴ��ĸ���
		_query_result = AttachmentDatabase
				.instance(_ctx)
				.query("select * from tb_task_attachment where status = '0' or status = '1' order by id asc limit 1;");

		if (null == _query_result)
			return;

		Log.d("JAMES", _query_result.toString());
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

		String id = _query_result.get("id_0");
		String task_id = _query_result.get("task_id_0");
		String historygps = _query_result.get("historygps_0");
		String standard = _query_result.get("standard_0");
		String type = _query_result.get("type_0");
		String url = _query_result.get("url_0");
		String upload_time = _query_result.get("upload_time_0");
		String md5 = _query_result.get("md5_0");
		String status = _query_result.get("status_0");

		// ʱ����������7�����ϣ��򲻱ش���
		if (7 * 24 * 3600 * 1000 <= Long.parseLong(upload_time)
				- System.currentTimeMillis())
			return;

		if (true == status.equalsIgnoreCase("0")) {
			// HFS�ϴ�
			boolean ret = false;// �ϴ��ɹ����
			// ...

			tb_task_attachment uploadAtt = new tb_task_attachment(id, task_id,
					historygps, standard, type, url, upload_time, md5, status);
			String prefix = NewTask.fileFolder;
			String fileName = uploadAtt.getUrl();
			String uploadUrl = android.wxapp.service.elec.request.Contants.HFS_URL;

			// new HttpUploadTask(null, _ctx, uploadAtt).execute(
			// prefix + fileName, uploadUrl);

			try {
				String result = post(prefix + fileName, uploadUrl);
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
						"update tb_task_attachment set status='1' where id = '"
								+ id + "';");
				status = "1";
			}
		}

		if (true == status.equalsIgnoreCase("1")) {
			// http֪ͨ������

			// http֪ͨ������
			String json_request;
			// ����json�����ַ���
			// ...
			GpsDao gpsDao = new GpsDao(_ctx);

			List<Attachments> sublist = new ArrayList<Attachments>();
			List<TaskAttachment> attachment = new ArrayList<TaskAttachment>();
			Attachments att = new Attachments(type,
					android.wxapp.service.elec.request.Contants.HFS_URL
							+ File.separator + url, upload_time,
					gpsDao.getHistory(historygps), md5);
			sublist.add(att);
			TaskAttachment item = new TaskAttachment(standard, sublist);
			attachment.add(item);

			UploadTaskAttachmentRequest ctr = new UploadTaskAttachmentRequest(
					getUserId(_ctx), getUserIc(_ctx), task_id, type, attachment);

			Log.d("JAMES", ctr.toString());

			json_request = parase2Json(ctr);
			Object[] result = upload_attachment_http_request(json_request);// ֪ͨ�ɹ����;
			boolean ret = (Boolean) result[0];

			if (true == ret) {

				AttachmentDatabase.instance(_ctx).execute(
						"update tb_task_attachment set status='2' where id = '"
								+ id + "';");

				UploadTaskAttachmentResponse response = (UploadTaskAttachmentResponse) result[1];
				AttachmentDatabase.instance(_ctx).execute(
						"update tb_gps_history set id='"
								+ response.getGpss().get(0).getId()
								+ "' where id = '" + historygps + "';");
				AttachmentDatabase.instance(_ctx).execute(
						"update tb_task_attachment set id='"
								+ response.getAttachments().get(0).getId()
								+ "',historygps='"
								+ response.getGpss().get(0).getId()
								+ "' where id = '" + id + "';");

				status = "2";

				_handler.post(new Runnable() {

					@Override
					public void run() {

						Toast.makeText(_ctx, "�ϴ��ɹ�", Toast.LENGTH_SHORT).show();
					}
				});
			}
		}
	}

	private static Object[] upload_attachment_http_request(String json_request) {
		HttpClientOperation hco = new HttpClientOperation();

		// List<NameValuePair> params = new ArrayList<NameValuePair>();
		//
		// // ��Ӳ���
		// params.add(new
		// BasicNameValuePair(Contants.UPLOAD_TASK_ATTACHMENT_PARAM,
		// json_request));

		Map params = new HashMap<String, String>();
		params.put(Contants.UPLOAD_TASK_ATTACHMENT_PARAM.substring(0,
				Contants.UPLOAD_TASK_ATTACHMENT_PARAM.length() - 1),
				json_request);

		// ����http���ӵĲ���
		hco.getHttpClient();
		Log.d("Baidu", params.toString());

		// ��http����������json�ַ��������ش�http�������˻�ȡ���ַ���
		// String result = hco.doPost(Contants.SERVER_URL + Contants.MODEL_NAME
		// + Contants.UPLOAD_TASK_ATTACHMENT_METHOD, params);
		String result = hco.doGet(
				Contants.SERVER_URL
						+ Contants.MODEL_NAME
						+ Contants.UPLOAD_TASK_ATTACHMENT_METHOD
								.substring(0,
										Contants.UPLOAD_TASK_ATTACHMENT_METHOD
												.length() - 1), params);

		Object[] rr = new Object[2];

		if (null == result || 0 == result.length()
				|| result.equalsIgnoreCase("{}")) {
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
			result = "��������ַ��Ч";
			return result;
		}

		HttpClient httpclient = new DefaultHttpClient();
		// ����ͨ��Э��汾
		httpclient.getParams().setParameter(
				CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

		HttpPost httppost = new HttpPost(urlServer);
		File file = new File(pathToOurFile);

		FileBody cbFile = new FileBody(file);

		CustomMultipartEntity multipartContent = new CustomMultipartEntity(
				new ProgressListener() {
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
		// name="userfile" /> ��Ӧ��

		httppost.setEntity(multipartContent);
		System.out.println("executing request " + httppost.getRequestLine());

		HttpParams params = httpclient.getParams();
		HttpConnectionParams.setConnectionTimeout(params, 3000);
		HttpResponse response = httpclient.execute(httppost);

		System.out.println(response.getStatusLine());// ͨ��Ok

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
		return MySharedPreference
				.get(context, MySharedPreference.USER_IC, null);
	}

	protected static Gson gson = new GsonBuilder().setDateFormat(
			"yyyy-MM-dd HH:mm:ss").create();

	protected static String parase2Json(Object o) {
		String encode = URLEncoder.encode(gson.toJson(o));
		Log.e("BaseRequest", (URLDecoder.decode(encode).equals(gson.toJson(o)))
				+ "");
		return encode;
	}

}
