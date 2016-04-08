package nercms.schedule.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

public class Utils {
	
	public static Toast mToast;
	public static void showToast(Context mContext, String msg){
		if (mToast == null){
			mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
		}
		mToast.setText(msg);
		mToast.show();
	}
	

	public static void setEditTextUnEditable(EditText et) {
		et.setFocusable(false);
		et.setFocusableInTouchMode(false);
	}

	public static final int EC_USER_NOT_EXIST = 1;
	public static final int EC_PWD_ERROR = 2;
	public static final int EC_USERNAME_ERROR = 3;

	public static String getFileMD5(File file) {
		if (!file.isFile()) {
			return null;
		}
		MessageDigest digest = null;
		FileInputStream in = null;
		byte buffer[] = new byte[1024];
		int len;
		try {
			digest = MessageDigest.getInstance("MD5");
			in = new FileInputStream(file);
			while ((len = in.read(buffer, 0, 1024)) != -1) {
				digest.update(buffer, 0, len);
			}
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		BigInteger bigInt = new BigInteger(1, digest.digest());
		return bigInt.toString(16);
	}

	public static String judgeFileLeixin(String s) {
		String kuozhanming = s.substring(s.lastIndexOf(".") + 1).toLowerCase();
		if (kuozhanming.equals("mp4"))
			return "attachmentType03";
		else if (kuozhanming.equals("amr") || kuozhanming.equals("mp3"))
			return "attachmentType02";
		else if (kuozhanming.equals("jpg") || kuozhanming.equals("png")
				|| kuozhanming.equals("gif"))
			return "attachmentType01";
		return null;

	}

	public static String formatDateMs(String ms) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(new Date(Long.parseLong(ms)));
	}

	public static String formatDateMs(long ms) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return format.format(ms);
	}

	public static String parseDateInFormat(String fotmatTime) {
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return format.parse(fotmatTime).getTime() + "";
		} catch (ParseException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static String getErrorMsg(String ec) {

		try {
			switch (Integer.parseInt(ec)) {
			case EC_USER_NOT_EXIST:
				return "�û�������";
			case EC_PWD_ERROR:
				return "�������";
			case EC_USERNAME_ERROR:
				return "�û�����ͻ";
			default:
				return "";
			}
		} catch (Exception e) {
			Log.e("Utils", e.toString());
			return "ת������";

		}

	}

	public static class Constant {
		public static int displayWidth; // ��Ļ���
		public static int displayHeight; // ��Ļ�߶�
	}

	// 2014-7-18 WeiHao
	public static String getIMSI(Context context) {
		String sim_imsi = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
				.getSubscriberId();

		if (null == sim_imsi || 8 > sim_imsi.length())
			return "1";// ����SIM��Ҳ��ʹ��

		String sim_mnc_hex = Long.toHexString(Long.parseLong(sim_imsi.substring(3, 5)));
		String sim_msin_hex = Long.toHexString(Long.parseLong(sim_imsi.substring(7)));

		for (int i = sim_msin_hex.length(); i < 7 /* MSIN�Ϊ7 */; i++) {
			sim_msin_hex = "0" + sim_msin_hex;
		}

		String _local_imsi = Long.toString(Long.parseLong(sim_mnc_hex.concat(sim_msin_hex), 16),
				10);
		return _local_imsi;

	}

	// 2014-5-16 WeiHao ���� ---------------------------------------------------

	public static final int MEDIA_TYPE_IMAGE = 2;
	public static final int MEDIA_TYPE_VIDEO = 3;
	public static final int MEDIA_TYPE_AUDIO = 4;

	// ��������ַ�
	public static String getRandomString(int length) { // length��ʾ�����ַ����ĳ���
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	// ��������ͼ
	public static String produceThunmnailID() {
		StringBuilder thumbnailID = new StringBuilder();
		String nowTime = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date(System.currentTimeMillis()));
		thumbnailID.append(nowTime).append(getRandomString(12));
		return thumbnailID.toString();
	}

	// ����taskID
	public static String produceTaskID(String userID) {
		StringBuilder taskID = new StringBuilder();
		String nowTime = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date(System.currentTimeMillis()));
		taskID.append("T").append(userID).append(nowTime).append(getRandomString(5));

		return taskID.toString();
	}

	/**
	 * @author jiaocuina
	 * @description �Զ����ɿͻ�id
	 */
	public static String produceContactID(String userID) {
		StringBuilder contactID = new StringBuilder();
		String nowTime = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date(System.currentTimeMillis()));
		contactID.append("C").append(userID).append(nowTime).append(getRandomString(5));

		return contactID.toString();
	}

	// ����feedbackID
	public static String produceFeedbackID(String userID) {
		StringBuilder fbID = new StringBuilder();
		String nowTime = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date(System.currentTimeMillis()));
		fbID.append("F").append(userID).append(nowTime).append(getRandomString(5));

		return fbID.toString();
	}

	// ������ϢID
	public static String produceMessageID(String userID) {
		StringBuilder fbID = new StringBuilder();
		String nowTime = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date(System.currentTimeMillis()));
		fbID.append("M").append(userID).append(nowTime).append(getRandomString(5));

		return fbID.toString();
	}

	// ����PhoneID
	public static String producePhoneID(String userID) {
		StringBuilder fbID = new StringBuilder();
		String nowTime = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date(System.currentTimeMillis()));
		fbID.append("P").append(userID).append(nowTime).append(getRandomString(5));

		return fbID.toString();
	}

	/**
	 * @author jiaocuina
	 * @description �Զ����ɻ���id
	 */
	public static String produceConferenceID(String userID) {
		StringBuilder conferenceID = new StringBuilder();
		String nowTime = new SimpleDateFormat("yyyyMMddHHmmss")
				.format(new Date(System.currentTimeMillis()));
		conferenceID.append("C").append(userID).append(nowTime).append(getRandomString(5));

		return conferenceID.toString();
	}

	/**
	 * ���ɸ����洢·�������ļ�����
	 * 
	 * @param type
	 *            ��������
	 * @param ID
	 *            ����/��Ϣ/������ID����ID���ɷ���������
	 * @param context
	 *            ���ø÷�����������
	 * @return
	 */
	public static String produceAttachDir(int type, String ID, Context context) {

		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			// ����һ���ļ��ж��󣬸�ֵΪ�ⲿ�洢����Ŀ¼
			File sdcardDir = Environment.getExternalStorageDirectory();
			// �õ�һ��·����������sdcard�ĸ���·��
			String path = sdcardDir.getPath() + "/nercms-Schedule/Attachments";
			File filePath = new File(path);

			if (!filePath.exists()) {
				// �������ڣ�����Ŀ¼
				filePath.mkdirs();
			}

			if (type == MEDIA_TYPE_IMAGE) {
				return filePath.toString() + File.separator + ID + "_" + getRandomString(3)
						+ ".jpg";
			} else if (type == MEDIA_TYPE_VIDEO) {
				return filePath.toString() + File.separator + ID + "_" + getRandomString(3)
						+ ".mp4";
			} else if (type == MEDIA_TYPE_AUDIO) {
				return filePath.toString() + File.separator + ID + "_" + getRandomString(3)
						+ ".mp3";
			} else {
				return "";
			}

		} else { // sd��δ����
			showShortToast(context, "sd��δ����");
			return "";
		}
	}

	public static String getAttachThumbnailDir() {
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			// �õ�һ��·����������sdcard�ĸ�������ͼ·��
			String path = sdcardDir.getPath() + "/nercms-Schedule/Thumbnail";
			File filePath = new File(path);

			if (!filePath.exists()) {
				// �������ڣ�����Ŀ¼
				filePath.mkdirs();
			}

			return filePath.toString() + File.separator + "THUMB" + produceThunmnailID() + ".jpg";
		} else {
			return "";
		}
	}

	public static void getThumbnail(String originalUri, String thumbnailUri) {
		if (originalUri == null || originalUri.equals("")) {
			return;
		}
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			// ������ʵ�ʵ�bitmap����������ڴ�ռ��ֻ����һЩ����߽���Ϣ��ͼƬ��С��Ϣ
			options.inJustDecodeBounds = true;
			// ��ȡ���ͼƬ�Ŀ�͸ߣ�ע�⣬��ʱ����bitmapΪ��
			Bitmap bitmap = BitmapFactory.decodeFile(originalUri, options);
			// ��������Ϊfalse����һ�η���ʵ�ʵ�bitmap
			options.inJustDecodeBounds = false;

			// �������ű�
			int widthRatio = (int) Math.ceil(options.outWidth / (float) 1200);
			int heightRatio = (int) Math.ceil(options.outHeight / (float) 1600);
			if (heightRatio > 1 || widthRatio > 1) {
				if (heightRatio > widthRatio) {
					options.inSampleSize = heightRatio;
				} else {
					options.inSampleSize = widthRatio;
				}
			} else {
				options.inSampleSize = 1;
			}
			// ���¶���ͼƬ
			bitmap = BitmapFactory.decodeFile(originalUri, options);

			// ��ת
			int degree = readPictureDegree(originalUri);
			bitmap = rotateBitmap(bitmap, degree);
			// ��������ͼ
			saveBitmap(bitmap, thumbnailUri);

		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param path
	 * @author chenqiang
	 */
	public static int readPictureDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	/**
	 * @param bitmap
	 * @param rotate
	 * @author chenqiang
	 */
	public static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
		if (bitmap == null)
			return null;

		int w = bitmap.getWidth();
		int h = bitmap.getHeight();

		// Setting post rotate to 90
		Matrix mtx = new Matrix();
		mtx.postRotate(rotate);
		return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
	}

	/**
	 * @author chenqiang
	 */
	public static String getThumbnailDir() {
		// �õ�һ��·����������sdcard�ĸ�������ͼ·��
		String path = Environment.getExternalStorageDirectory().getPath() + "/TestRecord/Thumbnail";
		File filePath = new File(path);

		if (!filePath.exists()) {
			// �������ڣ�����Ŀ¼
			filePath.mkdirs();
		}

		return path + File.separator + getFileDate() + ".jpg";

	}

	/**
	 * @author chenqiang
	 */
	public static String getFileDate() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
		Date date = new Date(System.currentTimeMillis());
		String file = format.format(date);
		return file;
	}

	/**
	 * @author chenqiang
	 */
	// ɾ���ļ���
	// param folderPath �ļ�����������·��
	public static void delFolder(String folderPath) {
		try {
			delAllFile(folderPath); // ɾ����������������
			String filePath = folderPath;
			filePath = filePath.toString();
			java.io.File myFilePath = new java.io.File(filePath);
			myFilePath.delete(); // ɾ�����ļ���
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @author chenqiang
	 */
	// ɾ��ָ���ļ����������ļ�
	// param path �ļ�����������·��
	public static boolean delAllFile(String path) {
		boolean flag = false;
		File file = new File(path);
		if (!file.exists()) {
			return flag;
		}
		if (!file.isDirectory()) {
			return flag;
		}
		String[] tempList = file.list();
		File temp = null;
		for (int i = 0; i < tempList.length; i++) {
			if (path.endsWith(File.separator)) {
				temp = new File(path + tempList[i]);
			} else {
				temp = new File(path + File.separator + tempList[i]);
			}
			if (temp.isFile()) {
				temp.delete();
			}
			if (temp.isDirectory()) {
				delAllFile(path + "/" + tempList[i]);// ��ɾ���ļ���������ļ�
				delFolder(path + "/" + tempList[i]);// ��ɾ�����ļ���
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * ����bitmap
	 * 
	 * @param bitmap
	 * @param uri
	 */
	public static void saveBitmap(Bitmap bitmap, String uri) {
		File outFile = new File(uri);
		try {
			outFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		FileOutputStream mFileOutputStream = null;
		try {
			mFileOutputStream = new FileOutputStream(outFile);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		bitmap.compress(Bitmap.CompressFormat.JPEG, 50, mFileOutputStream);
		try {
			mFileOutputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			mFileOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * ɾ��ý���ļ�
	 * 
	 * @param path
	 * @return
	 */
	public static boolean deleteMedia(String path) {
		// ��ȡ�ļ����жϴ������
		File file = new File(path);
		if (!file.exists()) {
			return true;
		}
		// ɾ���ļ�
		if (file.delete()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @author WEIHAO
	 * @param fromFile
	 *            �����Ƶ��ļ�
	 * @param toFile
	 *            ���Ƶ�Ŀ¼�ļ�
	 * @param rewrite
	 *            �Ƿ����´����ļ�
	 * 
	 *            <p>
	 *            �ļ��ĸ��Ʋ�������
	 */
	public static void copyFile(File fromFile, File toFile, boolean rewrite) {

		if (!fromFile.exists()) {
			return;
		}

		if (!fromFile.isFile()) {
			return;
		}
		if (!fromFile.canRead()) {
			return;
		}
		if (!toFile.getParentFile().exists()) {
			toFile.getParentFile().mkdirs();
		}
		if (toFile.exists() && rewrite) {
			toFile.delete();
		}

		try {
			FileInputStream fosfrom = new FileInputStream(fromFile);
			FileOutputStream fosto = new FileOutputStream(toFile);

			byte[] bt = new byte[1024];
			int c;
			while ((c = fosfrom.read(bt)) > 0) {
				fosto.write(bt, 0, c);
			}
			// �ر����롢�����
			fosfrom.close();
			fosto.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// -------------------------------------------------------------------------
	// -------------------------------------------------------------------------

	public static void CopyStream(InputStream is, OutputStream os) {
		final int buffer_size = 1024;
		try {
			byte[] bytes = new byte[buffer_size];
			for (;;) {
				int count = is.read(bytes, 0, buffer_size);
				if (count == -1)
					break;
				os.write(bytes, 0, count);
			}
		} catch (Exception ex) {
		}
	}

	/**
	 * @author jiaocuina
	 * @description ��ֹƵ����� �����Ϊ500ms
	 */
	private static long lastClickTime;

	public static boolean isFastDoubleClick() {
		long time = System.currentTimeMillis();
		long timeD = time - lastClickTime;
		if (0 < timeD && timeD < 500) {
			return true;
		}
		lastClickTime = time;
		return false;
	}

	/**
	 * @author jiaocuina
	 * @description toast��д��
	 */
	public static void showShortToast(Context context, String message) {
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
		toast.show();
	}

	// ---------------------------------------------------------------------
	// -------------------2014-6-17---WeiHao------ServerPing----------------

	public static boolean serverPing() {
		try {
			Process p;
			try {
				p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + LocalConstant.APP_SERVER_IP);
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			} // ping3��

			int status = p.waitFor();

			if (status == 0) {
				return true;
			} else {
				return false;
			}
		} catch (InterruptedException e) {
			return false;
		}
	}

	// ---------------------------------------------------------------------
	// -------------------2014-6-17---WeiHao--FileUtils--�ļ�����-------------

	public static final int SIZETYPE_B = 1;// ��ȡ�ļ���С��λΪB��doubleֵ
	public static final int SIZETYPE_KB = 2;// ��ȡ�ļ���С��λΪKB��doubleֵ
	public static final int SIZETYPE_MB = 3;// ��ȡ�ļ���С��λΪMB��doubleֵ
	public static final int SIZETYPE_GB = 4;// ��ȡ�ļ���С��λΪGB��doubleֵ

	/**
	 * �ж�SD���Ƿ����
	 * 
	 * @return
	 */
	public static boolean isExistSDCard() {
		if (android.os.Environment.getExternalStorageState()
				.equals(android.os.Environment.MEDIA_MOUNTED)) {
			return true;
		} else
			return false;
	}

	/**
	 * ��ȡSD��ʣ������
	 * 
	 * @return
	 */
	public static long getSDFreeSize() {
		// ȡ��SD���ļ�·��
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		// ��ȡ�������ݿ�Ĵ�С(Byte)
		long blockSize = sf.getBlockSize();
		// ���е����ݿ������
		long freeBlocks = sf.getAvailableBlocks();
		// ����SD�����д�С
		// return freeBlocks * blockSize; //��λByte
		// return (freeBlocks * blockSize)/1024; //��λKB
		return (freeBlocks * blockSize) / 1024 / 1024; // ��λMB
	}

	/**
	 * ��ȡSD��������
	 * 
	 * @return
	 */
	public static long getSDAllSize() {
		// ȡ��SD���ļ�·��
		File path = Environment.getExternalStorageDirectory();
		StatFs sf = new StatFs(path.getPath());
		// ��ȡ�������ݿ�Ĵ�С(Byte)
		long blockSize = sf.getBlockSize();
		// ��ȡ�������ݿ���
		long allBlocks = sf.getBlockCount();
		// ����SD����С
		// return allBlocks * blockSize; //��λByte
		// return (allBlocks * blockSize)/1024; //��λKB
		return (allBlocks * blockSize) / 1024 / 1024; // ��λMB
	}

	// 2014-6-25 WeiHao
	/**
	 * ��ȡ�ļ�ָ���ļ���ָ����λ�Ĵ�С
	 * 
	 * @param filePath
	 *            �ļ�·��
	 * @param sizeType
	 *            ��ȡ��С������1ΪB��2ΪKB��3ΪMB��4ΪGB
	 * @return doubleֵ�Ĵ�С
	 */
	public static double getFileOrFilesSize(String filePath, int sizeType) {
		File file = new File(filePath);
		long blockSize = 0;
		try {
			if (file.isDirectory()) {
				blockSize = getFileSizes(file);
			} else {
				blockSize = getFileSize(file);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("��ȡ�ļ���С", "��ȡʧ��!");
		}
		return FormetFileSize(blockSize, sizeType);
	}

	/**
	 * ��ȡָ���ļ���С
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	private static long getFileSize(File file) throws Exception {
		long size = 0;
		if (file.exists()) {
			FileInputStream fis = null;
			fis = new FileInputStream(file);
			size = fis.available();
		} else {
			file.createNewFile();
			Log.e("��ȡ�ļ���С", "�ļ�������!");
		}
		return size;
	}

	/**
	 * ��ȡָ���ļ���
	 * 
	 * @param f
	 * @return
	 * @throws Exception
	 */
	private static long getFileSizes(File f) throws Exception {
		long size = 0;
		File flist[] = f.listFiles();
		for (int i = 0; i < flist.length; i++) {
			if (flist[i].isDirectory()) {
				size = size + getFileSizes(flist[i]);
			} else {
				size = size + getFileSize(flist[i]);
			}
		}
		return size;
	}

	/**
	 * ת���ļ���С,ָ��ת��������
	 * 
	 * @param fileS
	 * @param sizeType
	 * @return
	 */
	private static double FormetFileSize(long fileS, int sizeType) {
		DecimalFormat df = new DecimalFormat("#.00");
		double fileSizeLong = 0;
		switch (sizeType) {
		case SIZETYPE_B:
			fileSizeLong = Double.valueOf(df.format((double) fileS));
			break;
		case SIZETYPE_KB:
			fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
			break;
		case SIZETYPE_MB:
			fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
			break;
		case SIZETYPE_GB:
			fileSizeLong = Double.valueOf(df.format((double) fileS / 1073741824));
			break;
		default:
			break;
		}
		return fileSizeLong;
	}

	/**
	 * ɾ��ָ��Ŀ¼���ļ���Ŀ¼
	 * 
	 * @param deleteThisPath
	 * @param filepath
	 * @return
	 */
	public static void deleteFolderFile(String filePath, boolean deleteThisPath)
			throws IOException {
		if (!TextUtils.isEmpty(filePath)) {
			File file = new File(filePath);

			if (file.isDirectory()) {// ����Ŀ¼
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					deleteFolderFile(files[i].getAbsolutePath(), true);
				}
			}
			if (deleteThisPath) {
				if (!file.isDirectory()) {// ������ļ���ɾ��
					file.delete();
				} else {// Ŀ¼
					if (file.listFiles().length == 0) {// Ŀ¼��û���ļ�����Ŀ¼��ɾ��
						file.delete();
					}
				}
			}
		}
	}

	// ---------------------------------------------------------------------
	// -------------------2014-6-26---WeiHao--BitmapUtils--λͼ����------------

	/**
	 * ��ȡָ����ַͼƬ����ת����ָ����С A safer decodeStream method rather than the one of
	 * {@link BitmapFactory} which will be easy to get OutOfMemory Exception
	 * while loading a big image file.
	 * 
	 * @param uri
	 * @param width
	 * @param height
	 * @return
	 * @throws FileNotFoundException
	 */
	public static Bitmap retriveBitmapByUrl(String fileurl, int width, int height) {
		try {
			int scale = 1;
			BitmapFactory.Options options = new BitmapFactory.Options();

			if (width > 0 || height > 0) {
				// Decode image size without loading all data into memory
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeFile(fileurl, options);

				int w = options.outWidth;
				int h = options.outHeight;
				// LogUtil.produceDefaultLog(Thread.currentThread()
				// .getStackTrace()[2], "ѹ��ǰimgwidth:" + String.valueOf(w));
				// LogUtil.produceDefaultLog(Thread.currentThread()
				// .getStackTrace()[2],
				// "ѹ��ǰimgheight:" + String.valueOf(h));
				while (true) {
					if ((width > 0 && w < width) || (height > 0 && h < height)) {
						break;
					}
					w >>= 1;
					h >>= 1;
					scale <<= 1;
				}
			}

			// Decode with inSampleSize option
			options.inJustDecodeBounds = false;
			options.inSampleSize = scale;
			// LogUtil.produceDefaultLog(
			// Thread.currentThread().getStackTrace()[2],
			// "ѹ������SampleSize:" + String.valueOf(scale));
			return BitmapFactory.decodeFile(fileurl, options);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * ���ڽ�Bitmap�ȱ�����С��(w,h),�������µ�bitmap
	 * 
	 * @param bmp
	 * @param w
	 * @param h
	 * @return
	 */
	public Bitmap zoomBitmap(Bitmap bmp, int w) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Matrix matrix = new Matrix(); // ��������ͼƬ�õ�Matrix����
		float scaleWidth = ((float) w / width); // �������ű���
		matrix.postScale(scaleWidth, scaleWidth); // �������ű���
		// �����µ�bitmap���������Ƕ�ԭbitmap�����ź��ͼ
		return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true); // ���µ�bitmap������
	}

	/**
	 * ���ڽ�Bitmap�ȱ�����С��(w,h),�������µ�bitmap
	 * 
	 * @param bmp
	 * @param w
	 * @param h
	 * @return
	 */
	public Bitmap zoomBitmap(Bitmap bmp, int w, int h) {
		int width = bmp.getWidth();
		int height = bmp.getHeight();
		Matrix matrix = new Matrix(); // ��������ͼƬ�õ�Matrix����
		float scaleWidth = ((float) w / width); // �������ű���
		float scaleHeight = ((float) h / height);
		float scale = scaleWidth - scaleHeight > 0.00002 ? scaleHeight : scaleWidth;
		matrix.postScale(scale, scale); // �������ű���
		// �����µ�bitmap���������Ƕ�ԭbitmap�����ź��ͼ
		return Bitmap.createBitmap(bmp, 0, 0, width, height, matrix, true); // ���µ�bitmap������
	}

	/**
	 * ��drawable�е�ͼƬ��Ϊˮӡ����ӵ�ͼƬsrc��
	 * 
	 * @param src
	 * @param watermark
	 *            ˮӡͼƬID
	 * @return
	 */
	public Bitmap drawWatermark(Bitmap src, int drawableId, Context context)

	{
		// ��ȡrs��ˮӡͼƬ����Bitmap drawWatermark(Bitmap src, Bitmap watermark)�����ͷ��ڴ�
		Resources r = context.getResources();
		InputStream is = r.openRawResource(drawableId);
		BitmapDrawable bmpDraw = new BitmapDrawable(is);
		Bitmap watermark = zoomBitmap(bmpDraw.getBitmap(), 40, 40);

		return drawWatermark(src, watermark);
	}

	/**
	 * ��ͼƬ���ˮӡ
	 * 
	 * @param src
	 * @param watermark
	 * @return
	 */
	public Bitmap drawWatermark(Bitmap src, Bitmap watermark) {
		// ���ⴴ��һ��ͼƬ
		Bitmap newb = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Config.ARGB_8888);// ����һ���µĺ�SRC���ȿ��һ����λͼ
		Canvas canvas = new Canvas(newb);
		canvas.drawBitmap(src, 0, 0, null);// �� 0��0���꿪ʼ����ԭͼƬsrc
		canvas.drawBitmap(watermark, (src.getWidth() - watermark.getWidth()) / 2,
				(src.getHeight() - watermark.getHeight()) / 2, null); // ͿѻͼƬ����ԭͼƬ�м�λ��
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();

		watermark.recycle();
		watermark = null;

		return newb;
	}

	// ��ʽ�ж�
	public static Boolean isEmail(String str) {
		String regex = "[a-zA-Z_0-9.]{1,}[0-9]{0,}@(([a-zA-Z0-9]-*){1,}\\.){1,3}[a-zA-Z\\-]{1,}";
		return match(regex, str);
	}

	public static Boolean match(String regex, String str) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(str);
		return matcher.matches();
	}

	public static boolean isNetworkAvailable(Context context) {
		ConnectivityManager connectivity = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public static Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
		Bitmap bitmap = null;
		// ��ȡ��Ƶ������ͼ
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);

		if (bitmap == null) {
			return null;
		}

		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

}