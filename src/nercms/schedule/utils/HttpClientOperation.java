package nercms.schedule.utils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.Header;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.util.Log;

public class HttpClientOperation {
	private HttpParams httpParams;
	private HttpClient httpClient = null;
	/** ִ��downfile�󣬵õ������ļ��Ĵ�С */
	private long contentLength;
	/** ��������ʧ����Ϣ **/
	private String strResult = "�������޷����ӣ���������";
	/** �õ������� **/
	private Context context;

	public HttpClient getHttpClient() {
		// ���� HttpParams ���������� HTTP ��������һ���ֲ��Ǳ���ģ�
		this.httpParams = new BasicHttpParams();
		// �������ӳ�ʱ�� Socket ��ʱ���Լ� Socket �����С
		HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
		HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
		// �����ض���ȱʡΪ true
		HttpClientParams.setRedirecting(httpParams, true);
		// ���� user agent
		String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
		HttpProtocolParams.setUserAgent(httpParams, userAgent);
		// ����һ�� HttpClient ʵ��
		// ע�� HttpClient httpClient = new HttpClient(); ��Commons HttpClient
		// �е��÷����� Android 1.5 ��������Ҫʹ�� Apache ��ȱʡʵ�� DefaultHttpClient

		/**
		 * ����һ�� HttpClient ʵ�� //�����Զ�ѡ�����磬����Ӧcmwap��CMNET��wifi��3G
		 */
		HttpCookies li = new HttpCookies(context);
		String proxyStr = li.getHttpProxyStr();
		if (proxyStr != null && proxyStr.trim().length() > 0) {
			HttpHost proxy = new HttpHost(proxyStr, 80);
			httpClient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
		}

		SchemeRegistry schReg = new SchemeRegistry();
		schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
		schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));

		ClientConnectionManager conMgr = new ThreadSafeClientConnManager(httpParams, schReg);
		httpClient = new DefaultHttpClient(conMgr, httpParams);
		return httpClient;
	}

	public void closeHttpClient() {
		if (httpClient != null) {
			if (httpClient != null && httpClient.getConnectionManager() != null) {
				httpClient.getConnectionManager().shutdown();
			}
		}
	}

	public String doGet(String url, Map params) {
		/* ����HTTPGet���� */
		String paramStr = "";
		Iterator iter = params.entrySet().iterator();
		while (iter.hasNext()) {
			Map.Entry entry = (Map.Entry) iter.next();
			Object key = entry.getKey();
			Object val = entry.getValue();
			paramStr += paramStr = "&" + key + "=" + val;
		}
		if (!paramStr.equals("")) {
			paramStr = paramStr.replaceFirst("&", "?");
			url += paramStr;
		}
		HttpGet httpRequest = new HttpGet(url);
		Log.v("URL", url);
		System.out.println(httpRequest);
		String strResult = "doGetError";
		try {
			/* �������󲢵ȴ���Ӧ */
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			/* ��״̬��Ϊ200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* ���������� */
				strResult = EntityUtils.toString(httpResponse.getEntity());
			} else {
				strResult = "Error Response: " + httpResponse.getStatusLine().toString();
			}
		} catch (ClientProtocolException e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
		} catch (IOException e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
		} catch (Exception e) {
			strResult = e.getMessage().toString();
			e.printStackTrace();
		} finally {
			// httpRequest.abort();
			// this.closeHttpClient();
		}
		Log.v("strResult", strResult);
		return strResult;
	}

	public String doPost(String url, List<NameValuePair> params) {
		long start = System.currentTimeMillis();

		// Log.v("http", "url: " + url);
		/* ����HTTPPost���� */
		HttpPost httpRequest = new HttpPost(url);

		// String strResult = "doPostError";

		try {
			/* ������������������� */
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			// Log.v("Baidu", "url: " +
			// httpRequest.toString());//System.out.println(httpRequest);

			/* �������󲢵ȴ���Ӧ */
			HttpResponse httpResponse = httpClient.execute(httpRequest);

			Log.v("http",
					"Duration " + url + " " + Long.toString(System.currentTimeMillis() - start));

			/* ��״̬��Ϊ200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* ���������� */
				return EntityUtils.toString(httpResponse.getEntity());
			} else {
				// NetworkStatus.force_restart_sip_media_service(1);

				// return httpResponse.getStatusLine().toString();

				return null;
			}
		} catch (ClientProtocolException e) {
			// NetworkStatus.force_restart_sip_media_service(2);
			Log.v("http", e.toString());
			// strResult = e.getMessage().toString();
			// e.printStackTrace();
			return null;
		} catch (IOException e) {
			// NetworkStatus.force_restart_sip_media_service(3);
			Log.v("http", e.toString());
			// strResult = e.getMessage().toString();
			// e.printStackTrace();
			return null;
		} catch (Exception e) {
			// NetworkStatus.force_restart_sip_media_service(4);
			Log.v("http", e.toString());
			// strResult = e.getMessage().toString();
			// e.printStackTrace();
			return null;
		}
	}

	/**
	 * �쳣�Զ��ָ�����, ʹ��HttpRequestRetryHandler�ӿ�ʵ��������쳣�ָ�
	 */
	private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
		// �Զ���Ļָ�����
		public boolean retryRequest(IOException exception, int executionCount,
				HttpContext context) {
			// ���ûָ����ԣ��ڷ����쳣ʱ���Զ�����N��
			if (executionCount >= 3) {
				// �������������Դ�������ô�Ͳ�Ҫ������
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				// ������������������ӣ���ô������
				return true;
			}
			if (exception instanceof SSLHandshakeException) {
				// ��Ҫ����SSL�����쳣
				return false;
			}
			HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
			if (!idempotent) {
				// ���������Ϊ���ݵȵģ���ô������
				return true;
			}
			return false;
		}
	};

	/** �õ��豸��Ϣ��ϵͳ�汾���������� **/
	private Header[] getHeader() {
		/** ����ͷ��Ϣ end **/
		HttpCookies li = new HttpCookies(context);
		return li.getHttpHeader();
	}

	public long getContentLength() {
		return contentLength;
	}

	/**
	 * ����obj���� ��null����""
	 * 
	 * @param obj
	 * @return
	 */
	public static String nullToString(Object obj) {
		if (obj == null) {
			return "";
		}
		return obj.toString();
	}

}
