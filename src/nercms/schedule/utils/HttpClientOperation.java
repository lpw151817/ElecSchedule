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
	/** 执行downfile后，得到下载文件的大小 */
	private long contentLength;
	/** 返回连接失败信息 **/
	private String strResult = "服务器无法连接，请检查网络";
	/** 得到上下文 **/
	private Context context;

	public HttpClient getHttpClient() {
		// 创建 HttpParams 以用来设置 HTTP 参数（这一部分不是必需的）
		this.httpParams = new BasicHttpParams();
		// 设置连接超时和 Socket 超时，以及 Socket 缓存大小
		HttpConnectionParams.setConnectionTimeout(httpParams, 20 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 20 * 1000);
		HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
		// 设置重定向，缺省为 true
		HttpClientParams.setRedirecting(httpParams, true);
		// 设置 user agent
		String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
		HttpProtocolParams.setUserAgent(httpParams, userAgent);
		// 创建一个 HttpClient 实例
		// 注意 HttpClient httpClient = new HttpClient(); 是Commons HttpClient
		// 中的用法，在 Android 1.5 中我们需要使用 Apache 的缺省实现 DefaultHttpClient

		/**
		 * 创建一个 HttpClient 实例 //增加自动选择网络，自适应cmwap、CMNET、wifi或3G
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
		/* 建立HTTPGet对象 */
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
			/* 发送请求并等待响应 */
			HttpResponse httpResponse = httpClient.execute(httpRequest);
			/* 若状态码为200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* 读返回数据 */
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
		/* 建立HTTPPost对象 */
		HttpPost httpRequest = new HttpPost(url);

		// String strResult = "doPostError";

		try {
			/* 添加请求参数到请求对象 */
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			// Log.v("Baidu", "url: " +
			// httpRequest.toString());//System.out.println(httpRequest);

			/* 发送请求并等待响应 */
			HttpResponse httpResponse = httpClient.execute(httpRequest);

			Log.v("http",
					"Duration " + url + " " + Long.toString(System.currentTimeMillis() - start));

			/* 若状态码为200 ok */
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				/* 读返回数据 */
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
	 * 异常自动恢复处理, 使用HttpRequestRetryHandler接口实现请求的异常恢复
	 */
	private static HttpRequestRetryHandler requestRetryHandler = new HttpRequestRetryHandler() {
		// 自定义的恢复策略
		public boolean retryRequest(IOException exception, int executionCount,
				HttpContext context) {
			// 设置恢复策略，在发生异常时候将自动重试N次
			if (executionCount >= 3) {
				// 如果超过最大重试次数，那么就不要继续了
				return false;
			}
			if (exception instanceof NoHttpResponseException) {
				// 如果服务器丢掉了连接，那么就重试
				return true;
			}
			if (exception instanceof SSLHandshakeException) {
				// 不要重试SSL握手异常
				return false;
			}
			HttpRequest request = (HttpRequest) context.getAttribute(ExecutionContext.HTTP_REQUEST);
			boolean idempotent = (request instanceof HttpEntityEnclosingRequest);
			if (!idempotent) {
				// 如果请求被认为是幂等的，那么就重试
				return true;
			}
			return false;
		}
	};

	/** 得到设备信息、系统版本、驱动类型 **/
	private Header[] getHeader() {
		/** 请求头信息 end **/
		HttpCookies li = new HttpCookies(context);
		return li.getHttpHeader();
	}

	public long getContentLength() {
		return contentLength;
	}

	/**
	 * 假如obj对象 是null返回""
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
