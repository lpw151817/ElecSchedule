package nercms.schedule.adapter;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.activity.ViewPagerActivity;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.PlayVideo;
import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import net.tsz.afinal.bitmap.display.Displayer;
import net.tsz.afinal.core.AsyncTask;
import android.app.ActionBar.LayoutParams;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.wxapp.service.elec.dao.OrgDao;
import android.wxapp.service.elec.dao.TaskInsDao;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions;
import android.wxapp.service.elec.model.bean.table.tb_task_instructions_attachment;
import android.wxapp.service.elec.request.Contants;
import android.wxapp.service.util.HttpDownloadTask;
import android.wxapp.service.util.MySharedPreference;
import android.wxapp.service.util.DownloadMutex;

import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class FeedbackListAdapter extends BaseAdapter {

	final String DOWNLOAD_ATTACHMENT_FOLDER = Environment
			.getExternalStorageDirectory().getAbsolutePath()
			+ "/nercms-Schedule/DownloadAttachments/";
	final String THUMBNAIL_FOLDER = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/nercms-Schedule/Thumbnail/";
	final String ATTACHMENT_FOLDER = Environment.getExternalStorageDirectory()
			.getAbsoluteFile() + "/nercms-Schedule/Attachments/";

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
	
	AnimationDrawable voiceAnimation = null;
	public static boolean isPlaying = false;

	public void setFblist(List<tb_task_instructions> fblist) {
		this.fblist = fblist;
		notifyDataSetChanged();
	}
	
	private void showAnimation(ImageView voiceIconView) {
		// play voice, and start animation
//		if (getItemViewType(position) == RIGHT_ITEM) {
//			voiceIconView.setImageResource(R.anim.voice_from_icon);
//		} else {
//			voiceIconView.setImageResource(R.anim.video);
//		}
		voiceIconView.setImageResource(R.anim.video_frame);
		voiceAnimation = (AnimationDrawable) voiceIconView.getDrawable();
		voiceAnimation.start();
	}
	public void stopPlayVoice()
	{
		voiceAnimation.stop();
//		if (message.direct == EMMessage.Direct.RECEIVE) {
//			voiceIconView.setImageResource(R.drawable.chatfrom_voice_playing);
//		} else {
//			voiceIconView.setImageResource(R.drawable.chatto_voice_playing);
//		}
		// stop play voice
		if (mp != null) {
			mp.stop();
			mp.release();
		}
		isPlaying = false;
	}
	public void playVoice(String filePath, final ImageView ivMedia) {
		try {
			// 如果正在播放语音则停止播放
//			if (mp.isPlaying()) {
//				mp.stop();
//				mp.release();
//
//			}
			mp = new MediaPlayer();
			
			mp.reset();
			mp.setDataSource(filePath);
			mp.prepare();
			mp.start();
			
			isPlaying = true;
			mp.setOnCompletionListener(new OnCompletionListener() {
				
				@Override
				public void onCompletion(MediaPlayer mp) {
					// TODO Auto-generated method stub
//					ivMedia.setImageResource(R.drawable.microphone_uncheck);
					ivMedia.setImageResource(R.drawable.voicesearch_feedback011);
					mp = null;
					stopPlayVoice();
				}
			});
			
			showAnimation(ivMedia);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("qq", e.toString());
			if (e != null
					&& e.getMessage() != null
					&& e.getMessage().equals(
							"setDataSource failed."))
				Toast.makeText(context, "文件未找到",
						Toast.LENGTH_SHORT).show();
		}
	}

	public FeedbackListAdapter(Context context, List<tb_task_instructions> fblist)
	{
		this.context = context;
		this.fblist = fblist;
		this.mInflater = LayoutInflater.from(context);

		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.no_picture) // 设置图片在下载期间显示的图片
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
	
	private void click_picture(final ViewHolder holder, final String file)
	{
		holder.media.setOnClickListener(new OnClickListener()
									{
										@Override
										public void onClick(View v)
										{
											if (new File(file).exists())
											{
												Log.v("Baidu", "click " + file);
												
												List<String> fileList = new ArrayList<String>();
												fileList.add(file);
					
												Intent showImageIntent = new Intent(context, ViewPagerActivity.class);
												showImageIntent.putExtra("images", (Serializable) fileList);
												showImageIntent.putExtra("currentItemIndex", 0);
												context.startActivity(showImageIntent);
											}
					
										}
									});
	}
	
	//public HashMap<String, HttpDownloadTask> _download_feedback_tasks = new HashMap<String, HttpDownloadTask>();
	private void show_picture(final int position, final ViewHolder holder, tb_task_instructions_attachment attachment)
	{
		holder.play.setVisibility(View.GONE);
		
		//修改， 控制图片和视频，录音显示图片大小
		matchSize(holder.media, 1);
		
		// 图片
		final String filepath = THUMBNAIL_FOLDER + taskInsDao.getTaskInsAtt(fblist.get(position).getId()).get(0).getUrl();

		if (getItemViewType(position) == RIGHT_ITEM)//发
		{
			Log.v("Temp", "local pic " + filepath);
			
			if (!new File(filepath).exists())//文件不存在
			{
				holder.media.setImageResource(R.drawable.download_fail);//fym holder.media.setBackgroundResource(R.drawable.download_fail);
				holder.media.setOnClickListener(null);//fym
			}
			else
			{
				//显示图片，优化滑动listview时卡顿现象
				Glide.with(context).load(filepath).override(200, 300).into(holder.media);
				// Bitmap bm = BitmapFactory.decodeFile(filepath);
				// holder.media.setImageBitmap(bm);// 不会变形

				//为图片添加点击事件，点击显示大图
				click_picture(holder, filepath);
			}
		}
		else//收
		{
			Log.v("Temp", "remote pic " + filepath);
			
			//作为接收方时，需要将图片下载下来显示
			String remote_url = Contants.HFS_URL + File.separator
					+ fblist.get(position).getTask_id()
					+ File.separator + "C" + File.separator
					+ attachment.getUrl();

			// 原图
			String local_path = DOWNLOAD_ATTACHMENT_FOLDER
					+ taskInsDao.getTaskInsAtt(fblist.get(position).getId()).get(0).getUrl();
			
			Log.v("Baidu", "feedback pic: " + remote_url + ", " + local_path);
			
			File file = new File(local_path);
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			// 如果文件未存在，或者文件已存在但无法执行或者读取，则重新下载
			if (!file.exists() || (file.exists() && (!file.canRead() || !file.canWrite())))
			{
				Log.v("Video", "---------- downdload 1");
				holder.media.setImageResource(R.drawable.downloading);
				holder.media.setOnClickListener(null);
				
				if(false == DownloadMutex._download_feedback_tasks.containsKey(remote_url))
				{
					Log.v("Video", "---------- downdload 2");
					
					if (file.exists())
						file.delete();
					
					HttpDownloadTask task = (HttpDownloadTask)new HttpDownloadTask(context).execute(remote_url,
							"/nercms-Schedule/DownloadAttachments/",
							attachment.getUrl());// 将附件下载下来
					
					DownloadMutex._download_feedback_tasks.put(remote_url, task);
				}
			}
			else
			{
				Log.v("Http", "====== file exist " + local_path);
				Log.v("Video", "---------- downdload 3");
				
				//fym
				HttpDownloadTask task = DownloadMutex._download_feedback_tasks.get(remote_url);
				if(null != task)//下载中
				{
					Log.v("Video", "---------- downdload 5");
					if(android.os.AsyncTask.Status.FINISHED == task.getStatus())//刚下载完毕
					{
						String path = DOWNLOAD_ATTACHMENT_FOLDER + attachment.getUrl();
						
						Log.v("Video", "---------- downdload 6 " + path);
						DownloadMutex._download_feedback_tasks.remove(remote_url);//fym
						
						Glide.with(context).load(path).override(200, 300).into(holder.media);
						click_picture(holder, local_path);
					}
					else//下载中
					{
						Log.v("Video", "---------- downdload 7");
						holder.media.setImageResource(R.drawable.downloading);
						holder.media.setOnClickListener(null);
					}
				}
				else//下载完毕
				{
					Log.v("Video", "---------- downdload 8");
					Glide.with(context).load(local_path).override(200, 300).into(holder.media);
					click_picture(holder, local_path);
				}
			}
		}
	}
	
	private void show_audio(final int position, final ViewHolder holder, tb_task_instructions_attachment attachment)
	{
		holder.play.setVisibility(View.GONE);
//		holder.media.setImageResource(R.drawable.microphone_uncheck);
		
		//修改， 控制图片和视频，录音显示图片大小
		matchSize(holder.media, 0);
		
		holder.media.setVisibility(View.GONE);
		holder.media.setVisibility(View.VISIBLE);
		holder.media.setImageResource(R.drawable.voicesearch_feedback011);
		
		//holder.media.setBackgroundResource(R.drawable.microphone_uncheck);
		holder.text.setText(attachments.get(0).getUrl());
		holder.media.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v) {

				if (taskInsDao == null)
					taskInsDao = new TaskInsDao(context);
				String filePath;
				if(getItemViewType(position) == RIGHT_ITEM)//发
				{
					// 本地文件地址
					filePath = DOWNLOAD_ATTACHMENT_FOLDER
							+ taskInsDao
									.getTaskInsAtt(
											fblist.get(position)
													.getId())
									.get(0).getUrl();
				}
				else//收
				{
					// 用在线语音地址
					filePath = android.wxapp.service.elec.request.Contants.HFS_URL
							+ File.separator
							+ fblist.get(position).getTask_id()
							+ File.separator
							+ "C"
							+ File.separator
							+ taskInsDao.getTaskInsAtt(fblist.get(position).getId()).get(0).getUrl();
				}

				Log.v("login", filePath);
				if (isPlaying)
				{
					stopPlayVoice();
				}
				else
				{
					playVoice(filePath, holder.media);
				}
				
			}
		});
	}
	
	private void show_video(final int position, final ViewHolder holder, tb_task_instructions_attachment attachment)
	{
		// TODO 视频
		holder.text.setText("");
		
		//修改， 控制图片和视频，录音显示图片大小
		holder.media.setScaleType(ScaleType.FIT_XY);
		//修改， 控制图片和视频，录音显示图片大小
		matchSize(holder.media, 1);
				
		String videoName = taskInsDao.getTaskInsAtt(fblist.get(position).getId()).get(0).getUrl();
		// 视频路径
		final String filepath = DOWNLOAD_ATTACHMENT_FOLDER + videoName;

		// 作为接收方，需要将视频下载下来
		if(getItemViewType(position) == LEFT_ITEM) {

			String remote_url = Contants.HFS_URL + File.separator
					+ fblist.get(position).getTask_id()
					+ File.separator + "C" + File.separator
					+ attachment.getUrl();

			File file = new File(filepath);
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			// 如果文件未存在，或者文件已存在但无法执行或者读取，则重新下载
			if (!file.exists() || (file.exists() && (/*!file.canExecute() || */!file.canRead() || !file.canWrite())))
			{
				holder.media.setImageResource(R.drawable.downloading);
				holder.media.setOnClickListener(null);
				
				if(false == DownloadMutex._download_feedback_tasks.containsKey(remote_url))
				{
					Log.v("Video", "---------- downdload 2");
					
					if (file.exists())
						file.delete();
					
					HttpDownloadTask task = (HttpDownloadTask)new HttpDownloadTask(context).execute(remote_url, "/nercms-Schedule/DownloadAttachments/", videoName);
					
					DownloadMutex._download_feedback_tasks.put(remote_url, task);
				}
			}
		}

		String thumbPath = THUMBNAIL_FOLDER + videoName;
		if (new File(filepath).exists())
		{
			//fym
			boolean done = true;
			if(getItemViewType(position) == LEFT_ITEM)
			{
				//fym
				String remote_url = Contants.HFS_URL + File.separator
						+ fblist.get(position).getTask_id()
						+ File.separator + "C" + File.separator
						+ attachment.getUrl();
				HttpDownloadTask task = DownloadMutex._download_feedback_tasks.get(remote_url);
				if(null != task)//下载中
				{
					Log.v("Video", "---------- downdload 5");
					if(android.os.AsyncTask.Status.FINISHED == task.getStatus())//刚下载完毕
					{
						String path = DOWNLOAD_ATTACHMENT_FOLDER + attachment.getUrl();
						
						Log.v("Video", "---------- downdload 6 " + path);
						DownloadMutex._download_feedback_tasks.remove(remote_url);//fym
						
						done = true;
					}
					else//下载中
					{
						Log.v("Video", "---------- downdload 7");
						holder.media.setImageResource(R.drawable.downloading);
						holder.media.setOnClickListener(null);
						done = false;
					}
				}
				else//下载完毕
				{
					Log.v("Video", "---------- downdload 8");
					done = true;
				}
			}
			
			if(true == done)
			{
				// 播放按钮可见
				holder.play.setVisibility(View.VISIBLE);

				// 缩略图不存在
				if (!new File(thumbPath).exists()) {
					// 生成并保存缩略图
					Bitmap bitmap = getVideoThumbnail(filepath, videoName, 400, 400,
							MediaStore.Images.Thumbnails.MINI_KIND);

				}
				/**
				 * @author 陈强
				 * 类型：修改
				 * 同一用Glide来显示图片，优化显示效果
				 */
				Glide.with(context).load(thumbPath).override(200, 300).into(holder.media);
				// showBitmap(holder.media, thumbPath);

				holder.media.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						playVideo(filepath);
					}
				});
				holder.play.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						playVideo(filepath);
					}
				});
			}
		}
		else
		{
			// 录像文件不存在
			if(getItemViewType(position) == LEFT_ITEM)
			{
				holder.media.setImageResource(R.drawable.downloading);
				holder.media.setOnClickListener(null);
			}
			else
			{
				holder.media.setImageResource(R.drawable.download_fail);//fym holder.media.setBackgroundResource(R.drawable.download_fail);
				holder.media.setOnClickListener(null);//fym
			}
			
			holder.media.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) {
					Toast.makeText(context, "录像文件不存在！", Toast.LENGTH_SHORT).show();
				}
			});
			holder.play.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) {
					Toast.makeText(context, "录像文件不存在！", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	//修改， 控制图片和视频，录音显示图片大小
	private void matchSize(final ImageView holder, int code)
	{
		switch (code) {
		case 0:
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			holder.setLayoutParams(params);

			break;

		case 1:
			RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(
					400, 500);
			holder.setLayoutParams(params1);

			break;

		default:
			break;
		}
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

	/**
	 * @author 陈强 
	 * 新增部分，根据位置填充视图
	 */
	public View createViewByType(int position) {
		if (getItemViewType(position) == RIGHT_ITEM) {
			return mInflater.inflate(R.layout.chat_item_right_example, null);

		} else {
			return mInflater.inflate(R.layout.chat_item_left_example, null);
		}
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (convertView == null) {
			holder = new ViewHolder();
			
			//避免图片重复显示
			convertView = createViewByType(position);

			// 获取控件对象
			holder.time = (TextView) convertView.findViewById(R.id.tv_sendtime);
			holder.contentLayout = (RelativeLayout) convertView.findViewById(R.id.rl_contentLayout);
			holder.text = (TextView) convertView.findViewById(R.id.tv_chatcontent);
			holder.media = (ImageView) convertView.findViewById(R.id.iv_chat_media);
			holder.play = (ImageView) convertView.findViewById(R.id.play);
			holder.header = (TextView) convertView.findViewById(R.id.iv_userhead);
			convertView.setTag(holder);

		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		// 设置值
		final tb_task_instructions fb = fblist.get(position);
		holder.time.setText(Utils.formatDateMs(fb.getSend_time()));

		holder.text.setVisibility(View.VISIBLE);
		holder.media.setVisibility(View.GONE);
		
		Log.v("Schedule", "feedback: " + fb.toString());
		if(false == TextUtils.isEmpty(fb.getContent()))
		{
			holder.text.setText(fb.getContent());
			holder.play.setVisibility(View.GONE);
		}
		else
		{
			if(false)
			{
				if (taskInsDao == null)
					taskInsDao = new TaskInsDao(context);
				
				//holder.text.setText("attachment");
				holder.text.setText(taskInsDao.getTaskInsAtt(fb.getId()).get(0).getUrl());
				holder.play.setVisibility(View.GONE);
			}
			// content为空，则表示其为附件信息，则去附件列表中找附件
			holder.text.setVisibility(View.GONE);
			holder.media.setVisibility(View.VISIBLE);

			if (taskInsDao == null)
				taskInsDao = new TaskInsDao(context);

			attachments = taskInsDao.getTaskInsAtt(fb.getId());

			if(attachments != null && attachments.size() > 0)
			{
				// 进行附件的展示
				tb_task_instructions_attachment attachment = attachments.get(0);
				
				if (attachment.getType().equals("attachmentType01"))
				{
					show_picture(position, holder, attachment);
				}
				else if (attachment.getType().equals("attachmentType02"))
				{
					show_audio(position, holder, attachment);
				}
				else if (attachment.getType().equals("attachmentType03"))
				{
					show_video(position, holder, attachment);
				}

			}

		}

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
		ImageView media, play;
		// 时长（只针对视频和音频）
		// TextView duration;
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

	/**
	 * 生成并保存缩略图
	 * 
	 * @param videoPath
	 *            video的绝对路径
	 * @param videoName
	 *            video的文件名
	 * @param width
	 * @param height
	 * @param kind
	 * @return
	 */
	private Bitmap getVideoThumbnail(String videoPath, String videoName,
			int width, int height, int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		// 保存视频缩略图
		Utils.saveBitmap(bitmap, THUMBNAIL_FOLDER + videoName);
		return bitmap;
	}

	private void playVideo(String videoPath) {
		Intent videoIntent = new Intent(context, PlayVideo.class);
		videoIntent.putExtra("path", videoPath);
		context.startActivity(videoIntent);
	}
}
