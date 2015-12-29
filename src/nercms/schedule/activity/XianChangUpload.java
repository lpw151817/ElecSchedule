package nercms.schedule.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;

public class XianChangUpload extends BaseActivity implements OnClickListener {

	Button bt_select, bt_upload;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_xian_chang_upload);

		iniActionBar(true, null);

		bt_select = (Button) findViewById(R.id.select);
		bt_upload = (Button) findViewById(R.id.upload);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.select:
			// TODO 添加附件
			break;
		case R.id.upload:
			// TODO 上传附件
			break;
		}

	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);

	}
}