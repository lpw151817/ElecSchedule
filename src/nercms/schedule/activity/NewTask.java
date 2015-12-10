package nercms.schedule.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import nercms.schedule.R;
import nercms.schedule.R.id;
import nercms.schedule.R.layout;
import nercms.schedule.R.menu;

public class NewTask extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_task);
	}
}
