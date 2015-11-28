package nercms.schedule.activity;

import java.io.IOException;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.EditText;

public class Setting extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	private boolean isServerChanged = false;

	SharedPreferences sharedPref;

	private EditTextPreference appServerPref;
	private EditTextPreference fileServerPref;
	private EditTextPreference mqttServerPref;

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
		// String syncConnPref = sharedPref.getString(KEY_PREF_SYNC_CONN, "");
		sharedPref.registerOnSharedPreferenceChangeListener(this);

		// 绑定Perference
		appServerPref = (EditTextPreference) findPreference("pref_key_app_server");
		fileServerPref = (EditTextPreference) findPreference("pref_key_file_server");
		mqttServerPref = (EditTextPreference) findPreference("pref_key_mqtt_server");
		// 设置preference变动监听，改变显示值
		appServerPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference arg0,
							Object arg1) {
						Log.v("Setting", arg0.getKey());
						appServerPref.setSummary(String.valueOf(arg1));
						return false;
					}
				});
		fileServerPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference arg0,
							Object arg1) {
						Log.v("Setting", arg0.getKey());
						fileServerPref.setSummary(String.valueOf(arg1));
						return false;
					}
				});
		mqttServerPref
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference arg0,
							Object arg1) {
						Log.v("Setting", arg0.getKey());
						mqttServerPref.setSummary(String.valueOf(arg1));
						return false;
					}
				});

		// SD卡容量显示
		Preference sdcardVolumePref = findPreference("pref_sdcard_volume");
		sdcardVolumePref.setTitle("当前SD卡可用容量:"
				+ String.valueOf(Utils.getSDFreeSize()) + "MB");
		sdcardVolumePref.setSummary("SD卡总容量:"
				+ String.valueOf(Utils.getSDAllSize()) + "MB");

		// 附件目录
		final String attachPath = Environment.getExternalStorageDirectory()
				.getPath() + "/nercms-Schedule/Attachments/";
		// 缩略图目录
		final String thumbnailPath = Environment.getExternalStorageDirectory()
				.getPath() + "/nercms-Schedule/Thumbnail/";
		// 图片缓存目录
		final String cachePath = Environment.getExternalStorageDirectory()
				.getPath() + "/nercms-Schedule/cache/";
		// 操作日志目录
		final String logPath = Environment.getExternalStorageDirectory()
				.getPath() + "/nercms-Schedule/Log/";

		Preference deleteAttachPref = findPreference("pref_key_delete_attach");
		deleteAttachPref.setSummary("附件(含缩略图)占用空间:"
				+ String.valueOf(Utils.getFileOrFilesSize(attachPath, 3)
						+ Utils.getFileOrFilesSize(thumbnailPath, 3)) + "MB");
		deleteAttachPref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						new AlertDialog.Builder(Setting.this)
								.setTitle("清理附件存储")
								.setMessage(
										"清除操作后，在附件需要显示的地方需要通过网络重新下载\n确定清除吗？")
								.setPositiveButton("确定", new OnClickListener() {

									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {
										try {
											Utils.deleteFolderFile(attachPath,
													false);
											Utils.deleteFolderFile(
													thumbnailPath, false);
										} catch (IOException e) {
											e.printStackTrace();
										}
									}
								}).setNegativeButton("取消", null).create()
								.show();
						return false;
					}
				});
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// Log.v("Setting", key);
		if (key.equals("pref_key_app_server")) {
			// Log.v("Setting", "pref_key_app_server");
		}
		if (key.equals("pref_key_file_server")) {

		}
		if (key.equals("pref_key_mqtt_server")) {

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isServerChanged) {
				new AlertDialog.Builder(Setting.this).setTitle("重新登录确认")
						.setMessage("服务器地址已变更，程序需要重新登录")
						.setView(new EditText(this))
						.setPositiveButton("确定", new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								Intent intent = new Intent(Setting.this,
										Login.class);
								intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
										| Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivity(intent);
							}
						}).setNegativeButton("取消", new OnClickListener() {

							@Override
							public void onClick(DialogInterface arg0, int arg1) {

							}
						}).create().show();
			}
		}
		return super.onKeyDown(keyCode, event);
	}

}
