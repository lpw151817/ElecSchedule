package nercms.schedule.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface.OnClickListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.util.MySharedPreference;
import nercms.schedule.R;

import java.util.StringTokenizer;

import com.Generate_md5;
import com.actionbarsherlock.app.SherlockActivity;

public class BaseActivity extends SherlockActivity {
	protected ProgressDialog mProgressDialog;

	protected boolean isAdmin() {
		return new OrgDao(this).getPerson(getUserId()).getName().contains("π‹¿Ì‘±");
	}

	protected void iniActionBar(Boolean isUp, Integer iconId, String title) {
		if (isUp != null) {
			if (isUp) {
				getSupportActionBar().setDisplayShowCustomEnabled(false);
				getSupportActionBar().setDisplayShowTitleEnabled(true);
				if (iconId != null) {
					getSupportActionBar().setDisplayShowHomeEnabled(true);
					getSupportActionBar().setIcon(iconId);
				} else {
					getSupportActionBar().setDisplayShowHomeEnabled(false);
				}
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			}
		}
		if (!TextUtils.isEmpty(title)) {
			getSupportActionBar().setTitle(title);
		}
	}

	protected String getUserId() {
		return MySharedPreference.get(this, MySharedPreference.USER_ID, null);
	}

	protected String getUserIc() {
		return MySharedPreference.get(this, MySharedPreference.USER_IC, null);
	}

	protected void saveUserIc(String ic) {
		MySharedPreference.save(this, MySharedPreference.USER_IC, Generate_md5.generate_md5(ic));

	}

	protected String getUserName() {
		return MySharedPreference.get(this, MySharedPreference.USER_NAME, null);
	}

	protected void startActivity(Bundle bundle, Class<?> targetActivity) {
		Intent intent = new Intent();
		intent.setClass(this, targetActivity);
		if (bundle != null)
			intent.putExtras(bundle);
		startActivity(intent);
	}

	protected void startActivity(Class<?> targetActivity) {
		startActivity(null, targetActivity);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mProgressDialog = new ProgressDialog(this);
		Log.v(getClass().getName(), "On Create");
	}

	protected void showProgressDialog(String title, String message) {
		mProgressDialog.setTitle(title);
		if (message != null)
			mProgressDialog.setMessage(message);
		if (!mProgressDialog.isShowing())
			mProgressDialog.show();
	}

	protected void showProgressDialog(String title) {
		showProgressDialog(title, null);
	}

	protected void dismissProgressDialog() {
		if (mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

	protected void showAlterDialog(String title, String content, Integer icon, String pB,
			OnClickListener pbListener, String nB, OnClickListener nbListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(title);
		if (content != null)
			builder.setMessage(content);
		if (icon != null)
			builder.setIcon(icon);
		if (pB != null)
			builder.setPositiveButton(pB, pbListener);
		if (nB != null)
			builder.setNegativeButton(nB, nbListener);
		builder.create().show();
	}

	protected void showAlterDialog(String title, String content, int icon, String pB,
			OnClickListener pbListener) {
		showAlterDialog(title, content, icon, pB, pbListener, null, null);
	}

	protected void showAlterDialog(String title, String content, int icon) {
		showAlterDialog(title, content, icon, null, null, null, null);
	}

	protected void showAlterDialog(String title, String content) {
		showAlterDialog(title, content, null, null, null, null, null);
	}

	protected void showAlterDialog(String title) {
		showAlterDialog(title, null, null, null, null, null, null);
	}

	protected void showShortToast(String content) {
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
	}

	protected void showLongToast(String content) {
		Toast.makeText(this, content, Toast.LENGTH_LONG).show();
	}

	protected void showLog_e(String t) {
		Log.e(this.getClass().getName(), t);
	}

	protected void showLog_v(String t) {
		Log.v(this.getClass().getName(), t);
	}
}
