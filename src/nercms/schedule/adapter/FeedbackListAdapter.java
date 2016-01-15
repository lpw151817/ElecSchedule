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

	/* 定义不同的消息视图item */
	public static final int RIGHT_ITEM = 0;
	public static final int LEFT_ITEM = 1;

	private Context context;
	private List<ReceiveMessageResponse> fblist;
	private LayoutInflater mInflater;

	// 显示大图对话框
	Dialog imageDialog;

	DisplayImageOptions options;

	// 软引用
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
	// 附件显示图片容器的集合
	private ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();

	public FeedbackListAdapter(Context context, List<ReceiveMessageResponse> fblist) {
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
		convertView.setTag(holder);

		// 设置值
		final ReceiveMessageResponse fb = fblist.get(position);
		holder.time.setText(Utils.formatDateMs(fb.getSt()));

		if (fb.getAt() == null || fb.getAt().equals("1")) { // 文本反馈
			holder.text.setVisibility(View.VISIBLE);
			holder.text.setText(fb.getC());
			holder.media.setVisibility(View.GONE);
		} else { // 附件反馈
			holder.media.setVisibility(View.VISIBLE);
			holder.text.setVisibility(View.GONE);
			int type = Integer.parseInt(fb.getAt());

			File sdcardDir = Environment.getExternalStorageDirectory();
			String path = sdcardDir.getPath() + "/nercms-Schedule/Attachments/";
			String videoThumbnailDir = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail/";

			switch (type) {
			case LocalConstant.IAMGE_TYPE:
				// 获取附件的url
				String picName = fb.getAu();
				// 获取文件名
				picName = picName.substring(picName.lastIndexOf("/" + 1));
				final String picPath = path.toString() + File.separator + picName;
				if (picPath != null && !picPath.equalsIgnoreCase("")) {
					holder.text.setVisibility(View.GONE);
					holder.media.setVisibility(View.VISIBLE);

					// 判断文件是否存在，不存在则连接文件服务器下载
					if (!new File(picPath).exists()) {
						String downUrl = android.wxapp.service.elec.request.Contants.HFS_URL + File.separator + picName;

						// new HttpDownloadTask(context).execute(downUrl,
						// "/nercms-Schedule/Attachments/", picName);

						// 请求网络图片
						ImageRequest imageRequest = new ImageRequest(downUrl,
								new Response.Listener<Bitmap>() {

									@Override
									public void onResponse(Bitmap response) {
										// 图片保存到本地
										Utils.saveBitmap(response, picPath);
										// 异步加载本地图片
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
						// 加入请求队列
						AppApplication.getInstance().myQueue.add(imageRequest);

					} else {
						// 异步加载本地图片
						com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(
								"file://" + picPath, holder.media, options);
					}
				}
				break;

			case LocalConstant.VIDEO_TYPE:
				// 获取附件的url
				String videoName = fb.getAu();
				// 获取文件名
				videoName = videoName.substring(videoName.lastIndexOf("/") + 1);
				String videoPath = path.toString() + videoName;
				if (videoPath != null && !videoPath.equalsIgnoreCase("")) {
					holder.text.setVisibility(View.GONE);
					holder.media.setVisibility(View.VISIBLE);
					// 判断文件是否存在，不存在则连接文件服务器下载
					if (!new File(videoPath).exists()) {
						String downUrl = LocalConstant.FILE_SERVER_ATTACH_URL + File.separator
								+ videoName;
						new HttpDownloadTask(context).execute(downUrl, "/nercms-Schedule/Attachments/",
								videoName);
					} else {
						// 判断视频缩略图是否存在，不存在则声称缩略图
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

}
