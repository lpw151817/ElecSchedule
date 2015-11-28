package nercms.schedule.activity;

import nercms.schedule.R;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.SearchView.OnCloseListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.LayoutParams;
import com.actionbarsherlock.view.MenuItem;

public class SearchActivity extends BaseActivity {

	private SearchView searchView;
	private Context context;

	private ListView listview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);

		listview = (ListView) findViewById(R.id.listview);

		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_SHOW_HOME
				| ActionBar.DISPLAY_SHOW_CUSTOM);

		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View customActionBarView = inflater.inflate(R.layout.actionbar_searchview, null);
		searchView = (SearchView) customActionBarView.findViewById(R.id.search_view);
		searchView.setIconified(false);
		TextView textView = (TextView) searchView.findViewById(searchView.getContext().getResources()
				.getIdentifier("android:id/search_src_text", null, null));
		textView.setTextColor(Color.WHITE);
		searchView.setOnCloseListener(new OnCloseListener() {

			@Override
			public boolean onClose() {
				finish();
				Log.e("<><<<<<", "OnCloseListener");
				return true;
			}
		});
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			public boolean onQueryTextSubmit(String query) {
				Log.e("<><<<<<", query);
				// TODO
				return true;
			}

			public boolean onQueryTextChange(String newText) {
				// TODO
				Log.e("<><<<<<", newText);
				if (newText != null && newText.length() > 0) {

				}
				return true;
			}
		});
		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		actionBar.setCustomView(customActionBarView, params);

		// show keyboard
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
						| WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			break;
		}
		return true;
	}

	@Override
	public void finish() {
		super.finish();
	}

}
