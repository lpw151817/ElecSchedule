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

	/* ���岻ͬ����Ϣ��ͼitem */
	public static final int RIGHT_ITEM = 0;
	public static final int LEFT_ITEM = 1;

	private Context context;
	private List<tb_task_instructions> fblist;
	List<tb_task_instructions_attachment> attachments;
	private LayoutInflater mInflater;

	// ��ʾ��ͼ�Ի���
	Dialog imageDialog;

	DisplayImageOptions options;

	OrgDao orgDao;
	TaskInsDao taskInsDao;

	MediaPlayer mp = new MediaPlayer();
	// ������
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
	// ������ʾͼƬ�����ļ���
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
			// ������ڲ���������ֹͣ����
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
				Toast.makeText(context, "�ļ�δ�ҵ�",
						Toast.LENGTH_SHORT).show();
		}
	}

	public FeedbackListAdapter(Context context, List<tb_task_instructions> fblist)
	{
		this.context = context;
		this.fblist = fblist;
		this.mInflater = LayoutInflater.from(context);

		options = new DisplayImageOptions.Builder()
				.showImageOnLoading(R.drawable.no_picture) // ����ͼƬ�������ڼ���ʾ��ͼƬ
				.showImageForEmptyUri(R.drawable.no_picture)// ����ͼƬUriΪ�ջ��Ǵ����ʱ����ʾ��ͼƬ
				.showImageOnFail(R.drawable.no_picture) // ����ͼƬ����/��������д���ʱ����ʾ��ͼƬ
				.cacheInMemory(true)// �������ص�ͼƬ�Ƿ񻺴����ڴ���
				.cacheOnDisc(true)// �������ص�ͼƬ�Ƿ񻺴���SD����
				.considerExifParams(true) // �Ƿ���JPEGͼ��EXIF��������ת����ת��
				.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// ����ͼƬ����εı��뷽ʽ��ʾ
				.bitmapConfig(Bitmap.Config.RGB_565)// ����ͼƬ�Ľ�������//
				// .delayBeforeLoading(int delayInMillis)//int
				// delayInMillisΪ�����õ�����ǰ���ӳ�ʱ��
				// ����ͼƬ���뻺��ǰ����bitmap��������
				// .preProcessor(BitmapProcessor preProcessor)
				.resetViewBeforeLoading(true)// ����ͼƬ������ǰ�Ƿ����ã���λ
				.displayer(new RoundedBitmapDisplayer(20))// �Ƿ�����ΪԲ�ǣ�����Ϊ����
				.displayer(new FadeInBitmapDisplayer(100))// �Ƿ�ͼƬ���غú���Ķ���ʱ��
				.build();// �������
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
		
		//�޸ģ� ����ͼƬ����Ƶ��¼����ʾͼƬ��С
		matchSize(holder.media, 1);
		
		// ͼƬ
		final String filepath = THUMBNAIL_FOLDER + taskInsDao.getTaskInsAtt(fblist.get(position).getId()).get(0).getUrl();

		if (getItemViewType(position) == RIGHT_ITEM)//��
		{
			Log.v("Temp", "local pic " + filepath);
			
			if (!new File(filepath).exists())//�ļ�������
			{
				holder.media.setImageResource(R.drawable.download_fail);//fym holder.media.setBackgroundResource(R.drawable.download_fail);
				holder.media.setOnClickListener(null);//fym
			}
			else
			{
				//��ʾͼƬ���Ż�����listviewʱ��������
				Glide.with(context).load(filepath).override(200, 300).into(holder.media);
				// Bitmap bm = BitmapFactory.decodeFile(filepath);
				// holder.media.setImageBitmap(bm);// �������

				//ΪͼƬ��ӵ���¼��������ʾ��ͼ
				click_picture(holder, filepath);
			}
		}
		else//��
		{
			Log.v("Temp", "remote pic " + filepath);
			
			//��Ϊ���շ�ʱ����Ҫ��ͼƬ����������ʾ
			String remote_url = Contants.HFS_URL + File.separator
					+ fblist.get(position).getTask_id()
					+ File.separator + "C" + File.separator
					+ attachment.getUrl();

			// ԭͼ
			String local_path = DOWNLOAD_ATTACHMENT_FOLDER
					+ taskInsDao.getTaskInsAtt(fblist.get(position).getId()).get(0).getUrl();
			
			Log.v("Baidu", "feedback pic: " + remote_url + ", " + local_path);
			
			File file = new File(local_path);
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			// ����ļ�δ���ڣ������ļ��Ѵ��ڵ��޷�ִ�л��߶�ȡ������������
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
							attachment.getUrl());// ��������������
					
					DownloadMutex._download_feedback_tasks.put(remote_url, task);
				}
			}
			else
			{
				Log.v("Http", "====== file exist " + local_path);
				Log.v("Video", "---------- downdload 3");
				
				//fym
				HttpDownloadTask task = DownloadMutex._download_feedback_tasks.get(remote_url);
				if(null != task)//������
				{
					Log.v("Video", "---------- downdload 5");
					if(android.os.AsyncTask.Status.FINISHED == task.getStatus())//���������
					{
						String path = DOWNLOAD_ATTACHMENT_FOLDER + attachment.getUrl();
						
						Log.v("Video", "---------- downdload 6 " + path);
						DownloadMutex._download_feedback_tasks.remove(remote_url);//fym
						
						Glide.with(context).load(path).override(200, 300).into(holder.media);
						click_picture(holder, local_path);
					}
					else//������
					{
						Log.v("Video", "---------- downdload 7");
						holder.media.setImageResource(R.drawable.downloading);
						holder.media.setOnClickListener(null);
					}
				}
				else//�������
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
		
		//�޸ģ� ����ͼƬ����Ƶ��¼����ʾͼƬ��С
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
				if(getItemViewType(position) == RIGHT_ITEM)//��
				{
					// �����ļ���ַ
					filePath = DOWNLOAD_ATTACHMENT_FOLDER
							+ taskInsDao
									.getTaskInsAtt(
											fblist.get(position)
													.getId())
									.get(0).getUrl();
				}
				else//��
				{
					// ������������ַ
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
		// TODO ��Ƶ
		holder.text.setText("");
		
		//�޸ģ� ����ͼƬ����Ƶ��¼����ʾͼƬ��С
		holder.media.setScaleType(ScaleType.FIT_XY);
		//�޸ģ� ����ͼƬ����Ƶ��¼����ʾͼƬ��С
		matchSize(holder.media, 1);
				
		String videoName = taskInsDao.getTaskInsAtt(fblist.get(position).getId()).get(0).getUrl();
		// ��Ƶ·��
		final String filepath = DOWNLOAD_ATTACHMENT_FOLDER + videoName;

		// ��Ϊ���շ�����Ҫ����Ƶ��������
		if(getItemViewType(position) == LEFT_ITEM) {

			String remote_url = Contants.HFS_URL + File.separator
					+ fblist.get(position).getTask_id()
					+ File.separator + "C" + File.separator
					+ attachment.getUrl();

			File file = new File(filepath);
			if (!file.getParentFile().exists())
				file.getParentFile().mkdirs();

			// ����ļ�δ���ڣ������ļ��Ѵ��ڵ��޷�ִ�л��߶�ȡ������������
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
				if(null != task)//������
				{
					Log.v("Video", "---------- downdload 5");
					if(android.os.AsyncTask.Status.FINISHED == task.getStatus())//���������
					{
						String path = DOWNLOAD_ATTACHMENT_FOLDER + attachment.getUrl();
						
						Log.v("Video", "---------- downdload 6 " + path);
						DownloadMutex._download_feedback_tasks.remove(remote_url);//fym
						
						done = true;
					}
					else//������
					{
						Log.v("Video", "---------- downdload 7");
						holder.media.setImageResource(R.drawable.downloading);
						holder.media.setOnClickListener(null);
						done = false;
					}
				}
				else//�������
				{
					Log.v("Video", "---------- downdload 8");
					done = true;
				}
			}
			
			if(true == done)
			{
				// ���Ű�ť�ɼ�
				holder.play.setVisibility(View.VISIBLE);

				// ����ͼ������
				if (!new File(thumbPath).exists()) {
					// ���ɲ���������ͼ
					Bitmap bitmap = getVideoThumbnail(filepath, videoName, 400, 400,
							MediaStore.Images.Thumbnails.MINI_KIND);

				}
				/**
				 * @author ��ǿ
				 * ���ͣ��޸�
				 * ͬһ��Glide����ʾͼƬ���Ż���ʾЧ��
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
			// ¼���ļ�������
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
					Toast.makeText(context, "¼���ļ������ڣ�", Toast.LENGTH_SHORT).show();
				}
			});
			holder.play.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v) {
					Toast.makeText(context, "¼���ļ������ڣ�", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	//�޸ģ� ����ͼƬ����Ƶ��¼����ʾͼƬ��С
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
		// ���ݷ�����ID������Ϣ��λ������˻����Ҷ�
		String senderID = fblist.get(position).getSend_id();
		String userID = MySharedPreference.get(context, MySharedPreference.USER_ID, "");
		if (senderID.equals(userID))
			return RIGHT_ITEM;
		else
			return LEFT_ITEM;
	}

	/**
	 * @author ��ǿ 
	 * �������֣�����λ�������ͼ
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
			
			//����ͼƬ�ظ���ʾ
			convertView = createViewByType(position);

			// ��ȡ�ؼ�����
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

		// ����ֵ
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
			// contentΪ�գ����ʾ��Ϊ������Ϣ����ȥ�����б����Ҹ���
			holder.text.setVisibility(View.GONE);
			holder.media.setVisibility(View.VISIBLE);

			if (taskInsDao == null)
				taskInsDao = new TaskInsDao(context);

			attachments = taskInsDao.getTaskInsAtt(fb.getId());

			if(attachments != null && attachments.size() > 0)
			{
				// ���и�����չʾ
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
		if (getItemViewType(position) == RIGHT_ITEM) { // �Լ���������Ϣ
			headerText = "��";
		} else {
			if (orgDao == null)
				orgDao = new OrgDao(context);
			headerText = orgDao.getPerson(fb.getSend_id()).getName();
		}
		holder.header.setText(headerText);

		return convertView;
	}

	private static class ViewHolder {
		// ʱ��
		TextView time;
		// ��Ϣ����
		RelativeLayout contentLayout;
		// ��Ϣ����
		TextView text;
		// ��Ϣý���ļ�
		ImageView media, play;
		// ʱ����ֻ�����Ƶ����Ƶ��
		// TextView duration;
		TextView header;
	}

	// 2014-6-4 WeiHao

	/**
	 * ����ͼƬ�ڴ�
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
	 * ���ɲ���������ͼ
	 * 
	 * @param videoPath
	 *            video�ľ���·��
	 * @param videoName
	 *            video���ļ���
	 * @param width
	 * @param height
	 * @param kind
	 * @return
	 */
	private Bitmap getVideoThumbnail(String videoPath, String videoName,
			int width, int height, int kind) {
		Bitmap bitmap = null;
		// ��ȡ��Ƶ������ͼ
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		// ������Ƶ����ͼ
		Utils.saveBitmap(bitmap, THUMBNAIL_FOLDER + videoName);
		return bitmap;
	}

	private void playVideo(String videoPath) {
		Intent videoIntent = new Intent(context, PlayVideo.class);
		videoIntent.putExtra("path", videoPath);
		context.startActivity(videoIntent);
	}
}
