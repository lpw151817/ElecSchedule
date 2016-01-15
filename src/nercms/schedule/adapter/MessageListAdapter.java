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
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.wxapp.service.AppApplication;
import android.wxapp.service.dao.DAOFactory;
import android.wxapp.service.dao.PersonDao;
import android.wxapp.service.jerry.model.message.ReceiveMessageResponse;
import android.wxapp.service.model.MessageModel;
import android.wxapp.service.util.HttpDownloadTask;
import android.wxapp.service.util.MySharedPreference;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.ImageRequest;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class MessageListAdapter extends BaseAdapter {

	/* 定义不同的消息视图item */
	public static final int RIGHT_ITEM = 0;
	public static final int LEFT_ITEM = 1;

	private Context context;
	private List<ReceiveMessageResponse> msglist;
	private LayoutInflater mInflater;

	// 显示大图对话框
	Dialog imageDialog;

	DisplayImageOptions options;

	// 软引用
	private HashMap<String, SoftReference<Bitmap>> imageCache = new HashMap<String, SoftReference<Bitmap>>();
	// 附件显示图片容器的集合
	private ArrayList<ImageView> imageViewList = new ArrayList<ImageView>();

	private DAOFactory daoFactory;
	private PersonDao personDao;

	public MessageListAdapter(Context context, List<ReceiveMessageResponse> msglist) {
		this.context = context;
		this.msglist = msglist;
		this.mInflater = LayoutInflater.from(context);
		this.daoFactory = DAOFactory.getInstance();
		personDao = daoFactory.getPersonDao(context);

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
		return msglist.size();
	}

	@Override
	public Object getItem(int arg0) {
		return msglist.get(arg0);
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
		String senderID = String.valueOf(((ReceiveMessageResponse) msglist.get(position)).getSid());

		String userID = MySharedPreference.get(context, MySharedPreference.USER_ID, "");

		if (senderID.equals(userID))
			return RIGHT_ITEM;
		else
			return LEFT_ITEM;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;

		final ReceiveMessageResponse msg = (ReceiveMessageResponse) msglist.get(position);

		if (getItemViewType(position) == RIGHT_ITEM) { // 自己发出的消息
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(R.layout.chat_item_right_example, parent, false);
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

			// 2014-7-31
			holder.userName = (TextView) convertView.findViewById(R.id.tv_username);
			if (msg.getT().equals("1") || msg.getT().equals("2")) {// 判断为群消息，显示左边发送者名字
				holder.userName.setVisibility(View.VISIBLE);
				holder.userName.setText(personDao.getPersonInfo(String.valueOf(msg.getSid())).getUn());
			} else { // 个人消息，隐藏名字显示
				holder.userName.setVisibility(View.GONE);
			}
		}

		// 获取控件对象
		holder.time = (TextView) convertView.findViewById(R.id.tv_sendtime);
		holder.contentLayout = (RelativeLayout) convertView.findViewById(R.id.rl_contentLayout);
		holder.text = (TextView) convertView.findViewById(R.id.tv_chatcontent);
		holder.media = (ImageView) convertView.findViewById(R.id.iv_chat_media);
		convertView.setTag(holder);

		holder.time.setText(Utils.formatDateMs(msg.getSt()));

		if (msg.getAu() == null || msg.getAu().equalsIgnoreCase("")) { // 文本消息
			holder.text.setVisibility(View.VISIBLE);
			holder.text.setText(msg.getC());
			holder.media.setVisibility(View.GONE);
		} else { // 附件消息
			holder.text.setVisibility(View.GONE);
			holder.media.setVisibility(View.VISIBLE);
			int type;
			if (msg == null || msg.getAt().equals(""))
				type = 0;
			else
				type = Integer.parseInt(msg.getAt());

			File sdcardDir = Environment.getExternalStorageDirectory();
			String path = sdcardDir.getPath() + "/nercms-Schedule/Attachments/";
			String videoThumbnailDir = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail/";

			switch (type) {
			case LocalConstant.IAMGE_TYPE:

				String picName = msg.getAu();
				final String picPath = path.toString() + picName;
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
									}
								});
						// 加入请求队列
						AppApplication.getInstance().myQueue.add(imageRequest);

						// 2014-6-23 WeiHao 使用ImageLoader来实现
						// ImageLoader imageLoader = new ImageLoader(
						// AppApplication.getInstance().myQueue,
						// new BitmapCache());
						// ImageListener listener =
						// ImageLoader.getImageListener(
						// holder.media, R.drawable.no_picture,
						// R.drawable.no_picture);
						// imageLoader.get(downUrl, listener);

					} else {
						// addImage(holder.media, picPath);
						// 异步加载本地图片
						com.nostra13.universalimageloader.core.ImageLoader.getInstance().displayImage(
								"file://" + picPath, holder.media, options);
					}
				}
				break;

			case LocalConstant.VIDEO_TYPE:

				String videoName = msg.getAu();
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
				if (Integer.parseInt(msg.getAt()) == LocalConstant.IAMGE_TYPE) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(
							Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath()
									+ "/nercms-Schedule/Attachments/" + msg.getAu()), "image/*");
					context.startActivity(intent);
				} else if (Integer.parseInt(msg.getAt()) == LocalConstant.VIDEO_TYPE) {
					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.setDataAndType(
							Uri.parse("file://" + Environment.getExternalStorageDirectory().getPath()
									+ "/nercms-Schedule/Attachments/" + msg.getAu()), "video/*");
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

		// 用户名（只针对群消息）
		TextView userName;
	}

	// 2014-6-26 WeiHao
	public void updateVideoThumbnail() {

	}

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

	public class BitmapCache implements ImageCache {

		private LruCache<String, Bitmap> mCache;

		public BitmapCache() {
			int maxSize = 4 * 1024 * 1024;
			mCache = new LruCache<String, Bitmap>(maxSize) {
				@Override
				protected int sizeOf(String key, Bitmap bitmap) {
					return bitmap.getRowBytes() * bitmap.getHeight();
				}
			};
		}

		@Override
		public Bitmap getBitmap(String url) {
			return mCache.get(url);
		}

		@Override
		public void putBitmap(String url, Bitmap bitmap) {
			mCache.put(url, bitmap);
		}

	}

}
