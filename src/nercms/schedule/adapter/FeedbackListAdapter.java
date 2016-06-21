package nercms.schedule.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.conn.BasicEofSensorWatcher;

import nercms.schedule.R;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import net.tsz.afinal.FinalActivity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images.Thumbnails;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.AppApplication;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.dao.TaskInsDao;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions_attachment;
import android.wxapp.service.jerry.model.message.ReceiveMessageResponse;
import android.wxapp.service.model.FeedbackModel;
import android.wxapp.service.util.HttpDownloadTask;
import android.wxapp.service.util.MySharedPreference;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class FeedbackListAdapter extends BaseAdapter {

	/* 定义不同的消息视图item */
	public static final int RIGHT_ITEM = 0;
	public static final int LEFT_ITEM = 1;

	private Context context;
	private List<tb_task_instructions> fblist;
	List<tb_task_instructions_attachment> attachments;
	private LayoutInflater mInflater;

	// 显示大图对话框
	Dialog imageDialog;

	DisplayImageOptions options;

	OrgDao orgDao;
	TaskInsDao taskInsDao;

	MediaPlayer mp = new MediaPlayer();
	// 软引用
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
	// 附件显示图片容器的集合
	private ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();

	public void setFblist(List<tb_task_instructions> fblist) {
		this.fblist = fblist;
		notifyDataSetChanged();
	}

	public FeedbackListAdapter(Context context, List<tb_task_instructions> fblist) {
		this.context = context;
		this.fblist = fblist;
		this.mInflater = LayoutInflater.from(context);

		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.no_picture) // 设置图片在下载期间显示的图片
				.showImageForEmptyUri(R.drawable.no_picture)// 设置图片Uri为空或是错误的时候显示的图片
				.showImageOnFail(R.drawable.no_picture) // 设置图片加载/解码过程中错误时候显示的图片
				.cacheInMemory(true)// 设置下载的图片是否缓存在内存中
				.cacheOnDisc(true)// 设置下载的图片是否缓存在SD卡中
				.considerExifParams(true) // 是否考虑JPEG图像EXIF参数（旋转，翻转）
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
				.bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型//
				// .delayBeforeLoading(int delayInMillis)//int
				// delayInMillis为你设置的下载前的延迟时间
				// 设置图片加入缓存前，对bitmap进行设置
				// .preProcessor(BitmapProcessor preProcessor)
				.resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
				.displayer(new RoundedBitmapDisplayer(20))// 是否设置为圆角，弧度为多少
				.displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
				.build();// 构建完成
	}

	@Override
	public int getCount() {
		return fblist.size();
	}

	@Override
	public Object getItem(int arg0) {
		return fblist.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		// 根据发送人ID设置消息的位置在左端还是右端
		String senderID = fblist.get(position).getSend_id();
		String userID = MySharedPreference.get(context, MySharedPreference.USER_ID, "");
		if (senderID.equals(userID))
			return RIGHT_ITEM;
		else
			return LEFT_ITEM;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (getItemViewType(position) == RIGHT_ITEM) { // 自己发出的消息
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.chat_item_right_example, null);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
		} else {
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.chat_item_left_example, null);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
		}

		// 获取控件对象
		holder.time = (TextView) convertView.findViewById(R.id.tv_sendtime);
		holder.contentLayout = (RelativeLayout) convertView.findViewById(R.id.rl_contentLayout);
		holder.text = (TextView) convertView.findViewById(R.id.tv_chatcontent);
		holder.media = (ImageView) convertView.findViewById(R.id.iv_chat_media);
		holder.header = (TextView) convertView.findViewById(R.id.iv_userhead);
		convertView.setTag(holder);

		// 设置值
		final tb_task_instructions fb = fblist.get(position);
		holder.time.setText(Utils.formatDateMs(fb.getSend_time()));

		holder.text.setVisibility(View.VISIBLE);
		holder.media.setVisibility(View.GONE);

		if (TextUtils.isEmpty(fb.getContent())) {
			// content为空，则表示其为附件信息，则去附件列表中找附件
			holder.text.setVisibility(View.GONE);
			holder.media.setVisibility(View.VISIBLE);

			if (taskInsDao == null)
				taskInsDao = new TaskInsDao(context);

			attachments = taskInsDao.getTaskInsAtt(fb.getId());

			if (attachments != null && attachments.size() > 0) {
				// 进行附件的展示

				/*
				 * 
				 * attachments.get(0).getType()
				 * 
				 * attachmentType attachmentType01 图片 attachmentType
				 * attachmentType02 音频 attachmentType attachmentType03 视频
				 * 
				 * 
				 */

				tb_task_instructions_attachment attachment = attachments.get(0);
				if (attachment.getType().equals("attachmentType01")) {
					// TODO 图片

					/////////////////////////////////
					// attachments.get(0).get;
					//
					// holder.media.set;
					/////////////////////////////////

				} else if (attachment.getType().equals("attachmentType02")) {
					// 音频
					holder.media.setBackgroundResource(R.drawable.microphone_uncheck);
					holder.text.setText(attachments.get(0).getUrl());
					holder.media.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							if (taskInsDao == null)
								taskInsDao = new TaskInsDao(context);

							// 本地文件地址
							// String filePath =
							// Environment.getExternalStorageDirectory()
							// .getAbsolutePath() +
							// "/nercms-Schedule/DownloadAttachments/"
							// +
							// taskInsDao.getTaskInsAtt(fblist.get(position).getId()).get(0)
							// .getUrl();

							// 用在线语音地址
							String filePath = android.wxapp.service.elec.request.Contants.HFS_URL
									+ File.separator
									+ taskInsDao.getTaskInsAtt(fblist.get(position).getId()).get(0)
											.getUrl();

							// Toast.makeText(context, filePath,
							// Toast.LENGTH_SHORT).show();
							try {
								// 如果正在播放语音则停止播放
								if (mp.isPlaying()) {
									mp.stop();
								}

								mp.reset();
								mp.setDataSource(filePath);
								mp.prepare();
								mp.start();
							} catch (Exception e) {
								e.printStackTrace();
								if (e != null && e.getMessage() != null
										&& e.getMessage().equals("setDataSource failed."))
									Toast.makeText(context, "文件未找到", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} else if (attachment.getType().equals("attachmentType03")) {
					// TODO 视频

				}

			}

		} else
			holder.text.setText(fb.getContent());

		String headerText = null;
		if (getItemViewType(position) == RIGHT_ITEM) { // 自己发出的消息
			headerText = "我";
		} else {
			if (orgDao == null)
				orgDao = new OrgDao(context);
			headerText = orgDao.getPerson(fb.getSend_id()).getName();
		}
		holder.header.setText(headerText);

		return convertView;
	}

	private static class ViewHolder {
		// 时间
		TextView time;
		// 消息内容
		RelativeLayout contentLayout;
		// 消息文字
		TextView text;
		// 消息媒体文件
		ImageView media;
		// 时长（只针对视频和音频）
		TextView duration;
		TextView header;
	}

	// 2014-6-4 WeiHao

	/**
	 * 回收图片内存
	 */
	public void freeBitmap() {
		if (imageCache.isEmpty()) {
			return;
		}
		for (SoftReference<Bitmap> bitmap : imageCache.values()) {
			if (bitmap.get() != null && !bitmap.get().isRecycled()) {
				bitmap.get().recycle();
				bitmap = null;
			}
		}

		imageCache.clear();
		imageCache = null;
		System.gc();
	}

	@Override
	public boolean areAllItemsEnabled() {
		// return super.areAllItemsEnabled();
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		// return super.isEnabled(position);
		return false;
	}

}
