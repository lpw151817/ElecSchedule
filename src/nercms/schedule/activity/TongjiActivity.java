package nercms.schedule.activity;

import com.Generate_md5;
import com.google.gson.Gson;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.wxapp.service.elec.model.LoginRequest;
import android.wxapp.service.elec.request.Contants;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;
import nercms.schedule.R.menu;

public class TongjiActivity extends BaseActivity {

	WebView webView;
	String url = Contants.SERVER_URL + Contants.TONGJI_METHOD + Contants.TONGJI_PARAM;

	// ���ڿ���webView����ʾtoast
	boolean isShowToast = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tongji);

		LoginRequest param = new LoginRequest(getUserName(), getUserIc(), null);
		url += new Gson().toJson(param);

		showAlterDialog("", url);

		iniActionBar(true, null, "ͳ��");

		webView = (WebView) findViewById(R.id.webView1);

		// ����֧��javascript
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		// ����ʹ�û���
		webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

		// Ĭ�ϴ���ҳ
		webView.loadUrl(TextUtils.isEmpty(url) ? Contants.SERVER_URL + "supervise" : url);
		// ����WebViewĬ��ʹ�õ�������ϵͳĬ�����������ҳ����Ϊ��ʹ��ҳ��WebView��
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// ����ֵ��true��ʱ�����ȥWebView�򿪣�Ϊfalse����ϵͳ�����������������
				view.loadUrl(url);
				return true;
			}
		});

	}

	// ��д�������������ص��߼�
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView.canGoBack()) {
				webView.goBack();// ������һҳ��
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			TongjiActivity.this.finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
