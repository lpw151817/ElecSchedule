package nercms.schedule.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.conn.BasicEofSensorWatcher;

import nercms.schedule.R;
import nercms.schedule.activity.XianChangAdd;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.PlayVideo;
import net.tsz.afinal.FinalActivity;
import net.tsz.afinal.FinalBitmap;
import net.tsz.afinal.bitmap.core.BitmapDisplayConfig;
import net.tsz.afinal.bitmap.display.Displayer;
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
import android.provider.MediaStore;
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
import android.wxapp.service.elec.request.Contants;
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

	final String DOWNLOAD_ATTACHMENT_FOLDER = Environment.getExternalStorageDirectory()
			.getAbsolutePath() + "/nercms-Schedule/DownloadAttachments/";
	final String THUMBNAIL_FOLDER = Environment.getExternalStorageDirectory().getAbsolutePath()
			+ "/nercms-Schedule/Thumbnail/";
	final String ATTACHMENT_FOLDER = Environment.getExternalStorageDirectory().getAbsoluteFile()
			+ "/nercms-Schedule/Attachments/";

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

	public void setFblist(List<tb_task_instructions> fblist) {
		this.fblist = fblist;
		notifyDataSetChanged();
	}

	public FeedbackListAdapter(Context context, List<tb_task_instructions> fblist) {
		this.context = context;
		this.fblist = fblist;
		this.mInflater = LayoutInflater.from(context);

		options = new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.no_picture) // ����ͼƬ�������ڼ���ʾ��ͼƬ
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

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		if (getItemViewType(position) == RIGHT_ITEM) { // �Լ���������Ϣ
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

		// ��ȡ�ؼ�����
		holder.time = (TextView) convertView.findViewById(R.id.tv_sendtime);
		holder.contentLayout = (RelativeLayout) convertView.findViewById(R.id.rl_contentLayout);
		holder.text = (TextView) convertView.findViewById(R.id.tv_chatcontent);
		holder.media = (ImageView) convertView.findViewById(R.id.iv_chat_media);
		holder.play = (ImageView) convertView.findViewById(R.id.play);
		holder.header = (TextView) convertView.findViewById(R.id.iv_userhead);
		convertView.setTag(holder);

		// ����ֵ
		final tb_task_instructions fb = fblist.get(position);
		holder.time.setText(Utils.formatDateMs(fb.getSend_time()));

		holder.text.setVisibility(View.VISIBLE);
		holder.media.setVisibility(View.GONE);

		if (TextUtils.isEmpty(fb.getContent())) {
			// contentΪ�գ����ʾ��Ϊ������Ϣ����ȥ�����б����Ҹ���
			holder.text.setVisibility(View.GONE);
			holder.media.setVisibility(View.VISIBLE);

			if (taskInsDao == null)
				taskInsDao = new TaskInsDao(context);

			attachments = taskInsDao.getTaskInsAtt(fb.getId());

			if (attachments != null && attachments.size() > 0) {
				// ���и�����չʾ

				/*
				 * 
				 * attachments.get(0).getType()
				 * 
				 * attachmentType attachmentType01 ͼƬ attachmentType
				 * attachmentType02 ��Ƶ attachmentType attachmentType03 ��Ƶ
				 * 
				 * 
				 */

				tb_task_instructions_attachment attachment = attachments.get(0);
				if (attachment.getType().equals("attachmentType01")) {
					// ͼƬ
					String filepath;
					if (getItemViewType(position) == RIGHT_ITEM) { // �Լ���������Ϣ
						// �����ļ���ַ
						filepath = DOWNLOAD_ATTACHMENT_FOLDER + taskInsDao
								.getTaskInsAtt(fblist.get(position).getId()).get(0).getUrl();
					} else {
						filepath = Contants.HFS_URL + File.separator
								+ fblist.get(position).getTask_id() + File.separator + "C"
								+ File.separator + attachment.getUrl();
					}
					showBitmap(holder.media, filepath);

				} else if (attachment.getType().equals("attachmentType02")) {
					// ��Ƶ
					holder.media.setBackgroundResource(R.drawable.microphone_uncheck);
					holder.text.setText(attachments.get(0).getUrl());
					holder.media.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {

							if (taskInsDao == null)
								taskInsDao = new TaskInsDao(context);
							String filePath;
							if (getItemViewType(position) == RIGHT_ITEM) { // �Լ���������Ϣ
								// �����ļ���ַ
								filePath = DOWNLOAD_ATTACHMENT_FOLDER
										+ taskInsDao.getTaskInsAtt(fblist.get(position).getId())
												.get(0).getUrl();
							} else {
								// ������������ַ
								filePath = android.wxapp.service.elec.request.Contants.HFS_URL
										+ File.separator + fblist.get(position).getTask_id()
										+ File.separator + "C" + File.separator
										+ taskInsDao.getTaskInsAtt(fblist.get(position).getId())
												.get(0).getUrl();
							}

							Log.v("login", filePath);

							// Toast.makeText(context, filePath,
							// Toast.LENGTH_SHORT).show();
							try {
								// ������ڲ���������ֹͣ����
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
									Toast.makeText(context, "�ļ�δ�ҵ�", Toast.LENGTH_SHORT).show();
							}
						}
					});
				} else if (attachment.getType().equals("attachmentType03")) {
					// TODO ��Ƶ
					String videoName = taskInsDao.getTaskInsAtt(fblist.get(position).getId()).get(0)
							.getUrl();
					// ��Ƶ·��
					final String filepath = DOWNLOAD_ATTACHMENT_FOLDER + videoName;
					String thumbPath = THUMBNAIL_FOLDER + videoName;
					// �����Ƶ����
					if (new File(filepath).exists()) {
						// ���Ű�ť�ɼ�
						holder.play.setVisibility(View.VISIBLE);

						// ����ͼ������
						if (!new File(thumbPath).exists()) {
							// ���ɲ���������ͼ
							getVideoThumbnail(filepath, videoName, 400, 400,
									MediaStore.Images.Thumbnails.MINI_KIND);
						}

						showBitmap(holder.media, thumbPath);

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

					} else {
						// ¼���ļ�������
						holder.media.setBackgroundResource(R.drawable.download_fail);
						holder.media.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								Toast.makeText(context, "¼���ļ������ڣ�", Toast.LENGTH_SHORT).show();
							}
						});
						holder.play.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								Toast.makeText(context, "¼���ļ������ڣ�", Toast.LENGTH_SHORT).show();
							}
						});
					}

				}

			}

		} else
			holder.text.setText(fb.getContent());

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
		TextView duration;
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

	// ��ʾ����ͼƬ
	private void showBitmap(ImageView im, String filename) {
		FinalBitmap finalBitmap = FinalBitmap.create(context);
		finalBitmap.configLoadingImage(R.drawable.loading);// ���ü���ͼƬ
		// ͼƬ��С
		finalBitmap.configBitmapMaxHeight(800);
		finalBitmap.configBitmapMaxWidth(480);
		// ���̻���·��
		finalBitmap.configDiskCachePath(ATTACHMENT_FOLDER);
		finalBitmap.configDiskCacheSize(10 * 1024);
		// ��һ������Ϊiamgeview������ڶ���Ϊ���ص�url��ַ
		finalBitmap.display(im, filename);
		// ������ʾ
		finalBitmap.configDisplayer(new Displayer() {

			@Override
			public void loadFailDisplay(View imageView, Bitmap bitmap) {
				// ����ʧ�� ��ʾͼƬ 404 �ȵ�
				imageView.setBackgroundResource(R.drawable.download_fail);
			}

			@Override
			public void loadCompletedisplay(View imageView, Bitmap bitmap,
					BitmapDisplayConfig config) {
				// ���سɹ� ���������ȵ�imageView.startAnimation();
			}
		});
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
	private Bitmap getVideoThumbnail(String videoPath, String videoName, int width, int height,
			int kind) {
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
