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

	// 用于控制webView中显示toast
	boolean isShowToast = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tongji);

		LoginRequest param = new LoginRequest(getUserName(), getUserIc(), null);
		url += new Gson().toJson(param);

		showAlterDialog("", url);

		iniActionBar(true, null, "统计");

		webView = (WebView) findViewById(R.id.webView1);

		// 启用支持javascript
		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		// 优先使用缓存
		webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);

		// 默认打开主页
		webView.loadUrl(TextUtils.isEmpty(url) ? Contants.SERVER_URL + "supervise" : url);
		// 覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				// 返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
				view.loadUrl(url);
				return true;
			}
		});

	}

	// 改写物理按键——返回的逻辑
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (webView.canGoBack()) {
				webView.goBack();// 返回上一页面
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
