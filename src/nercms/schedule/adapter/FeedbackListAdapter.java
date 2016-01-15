package nercms.schedule.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import nercms.schedule.R;
import nercms.schedule.utils.LocalConstant;
import nercms.schedule.utils.Utils;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.Images.Thumbnails;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.wxapp.service.AppApplication;
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

	/* ���岻ͬ����Ϣ��ͼitem */
	public static final int RIGHT_ITEM = 0;
	public static final int LEFT_ITEM = 1;

	private Context context;
	private List<ReceiveMessageResponse> fblist;
	private LayoutInflater mInflater;

	// ��ʾ��ͼ�Ի���
	Dialog imageDialog;

	DisplayImageOptions options;

	// ������
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
	// ������ʾͼƬ�����ļ���
	private ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();

	public FeedbackListAdapter(Context context, List<ReceiveMessageResponse> fblist) {
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
		String senderID = fblist.get(position).getSid();
		String userID = MySharedPreference.get(context, MySharedPreference.USER_ID, "");
		if (senderID.equals(userID))
			return RIGHT_ITEM;
		else
			return LEFT_ITEM;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
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
		convertView.setTag(holder);

		// ����ֵ
		final ReceiveMessageResponse fb = fblist.get(position);
		holder.time.setText(Utils.formatDateMs(fb.getSt()));

		if (fb.getAt() == null || fb.getAt().equals("1")) { // �ı�����
			holder.text.setVisibility(View.VISIBLE);
			holder.text.setText(fb.getC());
			holder.media.setVisibility(View.GONE);
		} else { // ��������
			holder.media.setVisibility(View.VISIBLE);
			holder.text.setVisibility(View.GONE);
			int type = Integer.parseInt(fb.getAt());

			File sdcardDir = Environment.getExternalStorageDirectory();
			String path = sdcardDir.getPath() + "/nercms-Schedule/Attachments/";
			String videoThumbnailDir = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail/";

			switch (type) {
			case LocalConstant.IAMGE_TYPE:
				// ��ȡ������url
				String picName = fb.getAu();
				// ��ȡ�ļ���
				picName = picName.substring(picName.lastIndexOf("/" + 1));
				final String picPath = path.toString() + File.separator + picName;
				if (picPath != null && !picPath.equalsIgnoreCase("")) {
					holder.text.setVisibility(View.GONE);
					holder.media.setVisibility(View.VISIBLE);

					// �ж��ļ��Ƿ���ڣ��������������ļ�����������
					if (!new File(picPath).exists()) {
						String downUrl = android.wxapp.service.elec.request.Contants.HFS_URL + File.separator + picName;

						// new HttpDownloadTask(context).execute(downUrl,
						// "/nercms-Schedule/Attachments/", picName);

						// ��������ͼƬ
						ImageRequest imageRequest = new ImageRequest(downUrl,
								new Response.Listener<Bitmap>() {

									@Override
									public void onResponse(Bitmap response) {
										// ͼƬ���浽����
										Utils.saveBitmap(response, picPath);
										// �첽���ر���ͼƬ
										com.nostra13.universalimageloader.core.ImageLoader
												.getInstance()
												.displayImage("file://" + picPath, holder.media, options);
									}
								}, 0, 0, Config.RGB_565, new Response.ErrorListener() {

									@Override
									public void onErrorResponse(VolleyError error) {
										// TODO Auto-generated method stub

									}
								});
						// �����������
						AppApplication.getInstance().myQueue.add(imageRequest);

					} else {
						// �첽���ر���ͼƬ
						com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(
								"file://" + picPath, holder.media, options);
					}
				}
				break;

			case LocalConstant.VIDEO_TYPE:
				// ��ȡ������url
				String videoName = fb.getAu();
				// ��ȡ�ļ���
				videoName = videoName.substring(videoName.lastIndexOf("/") + 1);
				String videoPath = path.toString() + videoName;
				if (videoPath != null && !videoPath.equalsIgnoreCase("")) {
					holder.text.setVisibility(View.GONE);
					holder.media.setVisibility(View.VISIBLE);
					// �ж��ļ��Ƿ���ڣ��������������ļ�����������
					if (!new File(videoPath).exists()) {
						String downUrl = LocalConstant.FILE_SERVER_ATTACH_URL + File.separator
								+ videoName;
						new HttpDownloadTask(context).execute(downUrl, "/nercms-Schedule/Attachments/",
								videoName);
					} else {
						// �ж���Ƶ����ͼ�Ƿ���ڣ�����������������ͼ
						String thumbnailPath = videoThumbnailDir
								+ videoName.substring(0, videoName.indexOf(".")) + ".jpg";
						if (!new File(thumbnailPath).exists()) {
							Utils.saveBitmap(
									ThumbnailUtils.createVideoThumbnail(videoPath, Thumbnails.MINI_KIND),
									thumbnailPath);
						}

						com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(
								"file://" + thumbnailPath, holder.media, options);
					}
				}

				break;

			default:
				break;
			}

		}

		holder.media.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (fb.getAt().equals(LocalConstant.IAMGE_TYPE + "")) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(
							Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath()
									+ "/nercms-Schedule/Attachments/" + fb.getAu()), "image/*");
					context.startActivity(intent);
				} else if (fb.getAt().equals(LocalConstant.VIDEO_TYPE + "")) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(
							Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath()
									+ "/nercms-Schedule/Attachments/" + fb.getAu()), "video/*");
					context.startActivity(intent);
				}
			}
		});

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
		ImageView media;
		// ʱ����ֻ�����Ƶ����Ƶ��
		TextView duration;
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

}
