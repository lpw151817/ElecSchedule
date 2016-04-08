package nercms.schedule.utils;
import org.apache.http.Header;  
import org.apache.http.client.CookieStore;  
import org.apache.http.message.BasicHeader;  
  
import android.content.Context;  
import android.database.Cursor;  
import android.net.Uri;  
import android.net.wifi.WifiManager;  
  
/** 
 * http����Ļ����һЩ���õĲ��� 
 * @author helijun 
 * 
 */  
public class HttpCookies {  
    /** ÿҳ������ʾ����� */  
    private static int pageSize = 10;  
    /** ��ǰ�Ự���cokie��Ϣ */  
    private static CookieStore uCookie = null;  
    /** ���õ�HTTP��ʾͷ��Ϣ */  
    private static Header[] httpHeader;  
    /** HTTP���ӵ�����ڵ� */  
    private static String httpProxyStr;  
    /**http����Ĺ���url����**/  
    public static String baseurl = "http://192.168.50.56:5056/River";  
    /**�����Ķ���**/  
    Context context;  
      
    public HttpCookies(Context context){  
        this.context = context;  
        /** y��������ͷ **/  
        /** y��������ͷ **/  
        Header[] header = {  
                new BasicHeader("PagingRows", String.valueOf(pageSize)) };  
        httpHeader = header;  
    }  
      
    /** 
     * �����Զ�ѡ�����磬����Ӧcmwap��CMNET��wifi��3G 
     */  
    @SuppressWarnings("static-access")  
    public void initHTTPProxy() {  
        WifiManager wifiManager = (WifiManager) (context.getSystemService(context.WIFI_SERVICE));  
        if (!wifiManager.isWifiEnabled()) {  
            Uri uri = Uri.parse("content://telephony/carriers/preferapn"); // ��ȡ��ǰ����ʹ�õ�APN�����  
            Cursor mCursor =context. getContentResolver().query(uri, null, null, null,  
                    null);  
            if (mCursor != null) {  
                mCursor.moveToNext(); // �α�������һ����¼����ȻҲֻ��һ��  
                httpProxyStr = mCursor.getString(mCursor  
                        .getColumnIndex("proxy"));  
            }  
        } else {  
            httpProxyStr = null;  
        }  
    }  
      
    public int getPageSize() {  
        return pageSize;  
    }  
  
    public void setPageSize(int pageSize) {  
        this.pageSize = pageSize;  
    }  
  
    public CookieStore getuCookie() {  
        return uCookie;  
    }  
  
    public void setuCookie(CookieStore uCookie) {  
        this.uCookie = uCookie;  
    }  
  
  
    public Header[] getHttpHeader() {  
        return httpHeader;  
    }  
  
    public String getHttpProxyStr() {  
        return httpProxyStr;  
    }  
} 