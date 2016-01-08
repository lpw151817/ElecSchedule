package nercms.schedule.utils;

import android.R.integer;
import android.wxapp.service.request.Contants;

public class LocalConstant {

	// ����
	public static final int START_TASK_TYPE = 1;
	public static final int RECEIVE_TASK_TYPE = 2;
	public static final int DOING_TASK_STATUS = 1;
	public static final int FINISHED_TASK_STATUS = 2;
	public static final int DELAYED_TASK_STATUS = 3;

	// ��������
	public static final int TEXT_TYPE = 1;
	public static final int IAMGE_TYPE = 2;
	public static final int VIDEO_TYPE = 3;
	public static final int SOUND_TYPE = 4;
	public static final int GPS_TYPE = 5;

	// ������
	public static final int CAPTURE_IMAGE_REQUEST_CODE = 101;
	public static final int CAPTURE_VIDEO_REQUEST_CODE = 102;
	public static final int SELECT_IMAGE_REQUEST_CODE = 103;
	public static final int CAPTURE_AUDIO_REQUEST_CODE = 104;
	public static final int MEETING_PARTICIPATOR_SELECT_REQUEST_CODE = 122;
	public static final int MEETING_SPEAKER_SELECT_REQUEST_CODE = 123;
	public static final int TASK_POD_SELECT_REQUEST_CODE = 124;
	public static final int SELECT_ATTACHMENT = 423;
	public static final int SHOWXIANCHANG_ATTACHMENT = 424;

	// ��ϵ���Ƿ�ѡ��
	public static final int SELECT_CONTACT_CHECKED = 120;
	public static final int SELECT_CONTACT_UNCHECKED = 121;

	// SD��״̬
	public static final int SD_MOUNTED = 130;
	public static final int SD_UNMOUNTED = 131;

	// ����״̬
	public static final int NET_AVAILABLE = 140;
	public static final int NET_UNAVAILABLE = 141;

	// Ӧ�÷�������ַ
	// public static final String APP_SERVER_IP = "202.114.117.234";
	// public static final String APP_SERVER_IP = "192.168.1.100";
	public static final String APP_SERVER_IP = "http://" + Contants.SERVER + ":8080";

	// �����ķ�������ַ
	// public static final String FILE_SERVER_ATTACH_URL =
	// "http://192.168.1.100:15005/HFSFileServer";
	public static final String FILE_SERVER_ATTACH_URL = "http://" + Contants.SERVER
			+ ":3000/ScheduleFileServer";

}
