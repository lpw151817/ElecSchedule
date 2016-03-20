package nercms.schedule.activity;

import java.util.List;

import nercms.schedule.R;
import android.os.Bundle;
import android.widget.TextView;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions;

public class Notification extends BaseActivity {

	private TextView tv_title;
	private TextView tv_time;
	private TextView tv_content;

	String mTitle = "";
	String mTime = "";
	String mContent = "";
	
	List<tb_task_instructions> data;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_notification);
		
		iniActionBar(true, null, "֪ͨ");

		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_time = (TextView) findViewById(R.id.tv_time);
		tv_content = (TextView) findViewById(R.id.content);
		
		mTitle = getIntent().getStringExtra("name");
		mTime = getIntent().getStringExtra("time");
		mContent = getIntent().getStringExtra("content");
		

		tv_title.setText(mTitle);
		tv_time.setText(mTime);
		tv_content.setText("	"+ mContent);

	}
	
	@Override
	public boolean onOptionsItemSelected(
			com.actionbarsherlock.view.MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}
