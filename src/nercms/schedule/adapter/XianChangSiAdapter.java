package nercms.schedule.adapter;

import java.io.File;

import nercms.schedule.R;
import nercms.schedule.activity.NewTask;
import nercms.schedule.activity.RecordActivity;
import nercms.schedule.activity.XianChangSi;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class XianChangSiAdapter extends BaseAdapter {
	Context mContext;
	String[] ss;
	int[] isRadio;
	int[] isPhoto;
	int[] isVideo;
	int type;
	ListPosition lp;

	// 拍照时图片的路径
	protected String mImagePath;
	public static String imagePath;
	public static String videopath;

	public XianChangSiAdapter(Context c, int type) {
		this.mContext = c;
		this.type = type;
		if (type == 1) {
			ss = c.getResources()
					.getStringArray(R.array.zuoyexianchang_si_data);
			isRadio = c.getResources().getIntArray(
					R.array.zuoyexianchang_si_radio);
			isPhoto = c.getResources().getIntArray(
					R.array.zuoyexianchang_si_photo);
			isVideo = c.getResources().getIntArray(
					R.array.zuoyexianchang_si_video);
		} else if (type == 2) {
			ss = c.getResources().getStringArray(
					R.array.caozuoxianchang_si_data);
			isRadio = c.getResources().getIntArray(
					R.array.caozuoxianchang_si_radio);
			isPhoto = c.getResources().getIntArray(
					R.array.caozuoxianchang_si_photo);
			isVideo = c.getResources().getIntArray(
					R.array.caozuoxianchang_si_video);
		} else if (type == 3) {
			ss = c.getResources().getStringArray(R.array.guzhangjinji_si_data);
			isRadio = c.getResources().getIntArray(
					R.array.guzhangjinji_si_radio);
			isPhoto = c.getResources().getIntArray(
					R.array.guzhangjinji_si_photo);
			isVideo = c.getResources().getIntArray(
					R.array.guzhangjinji_si_video);
		}
	}

	@Override
	public int getCount() {
		return ss.length;
	}

	@Override
	public Object getItem(int position) {
		return ss[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.xianchang_si_adapter, null);
			holder = new Holder();
			holder.textView = ((TextView) convertView
					.findViewById(R.id.textview));
			holder.video = ((ImageButton) convertView.findViewById(R.id.video));
			holder.photo = (ImageButton) convertView.findViewById(R.id.photo);
			holder.radio = (ImageButton) convertView.findViewById(R.id.radio);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		holder.textView.setText(ss[position]);
		if (isPhoto[position] == 1)
			holder.photo.setVisibility(View.VISIBLE);
		else
			holder.photo.setVisibility(View.GONE);
		if (isRadio[position] == 1)
			holder.radio.setVisibility(View.VISIBLE);
		else
			holder.radio.setVisibility(View.GONE);
		if (isVideo[position] == 1)
			holder.video.setVisibility(View.VISIBLE);
		else
			holder.video.setVisibility(View.GONE);

		holder.photo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 拍照
				Intent cameraintent = new Intent();
				// 指定开启系统相机的Action
				cameraintent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
				cameraintent.addCategory(Intent.CATEGORY_DEFAULT);

				mImagePath = NewTask.fileFolder + File.separator
						+ Utils.getFileDate() + ".jpg";
				imagePath = mImagePath;
				// 根据文件地址创建文件
				File imagefile = new File(mImagePath);
				if (imagefile.exists()) {
					imagefile.delete();
				}
				// 把文件地址转换成Uri格式
				Uri imageUri = Uri.fromFile(imagefile);
				// 设置系统相机拍摄照片完成后图片文件的存放地址
				cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				((Activity) mContext).startActivityForResult(cameraintent,
						LocalConstant.CAPTURE_IMAGE_REQUEST_CODE);

				lp.getPositon(position);
			}
		});
		holder.radio.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent audiointent = new Intent(((XianChangSi) mContext),
						RecordActivity.class);
				((Activity) mContext).startActivityForResult(audiointent,
						LocalConstant.CAPTURE_AUDIO_REQUEST_CODE);

				lp.getPositon(position);
			}
		});

		holder.video.setOnClickListener(new OnClickListener() {

			

			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction("android.media.action.VIDEO_CAPTURE");
				intent.addCategory("android.intent.category.DEFAULT");

				String fileName = Utils.getFileDate();
				videopath = NewTask.fileFolder + "/" + fileName + ".mp4";
				File file = new File(videopath);
				if (file.exists()) {
					file.delete();
				}
				Uri uri = Uri.fromFile(file);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
				((Activity) mContext).startActivityForResult(intent,
						LocalConstant.CAPTURE_VIDEO_REQUEST_CODE);
				
				lp.getPositon(position);
			}
		});

		return convertView;
	}

	class Holder {
		TextView textView;
		ImageButton video, radio, photo;
	}

	public void setListPostion(ListPosition lp) {
		System.out.println("setListPostion " + lp);
		this.lp = lp;
	}

	public interface ListPosition {
		public int getPositon(int position);
	}

}
