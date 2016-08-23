package nercms.schedule.activity;

import nercms.schedule.R;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnInfoListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
  
public class Video extends Activity implements OnClickListener {  
    private SurfaceView mCameraPreview;  
    private SurfaceHolder mSurfaceHolder;  
    private ImageButton mShutter;  
    private TextView mMinutePrefix;  
    private TextView mMinuteText;  
    private TextView mSecondPrefix;  
    private TextView mSecondText;  
  
    private Camera mCamera;  
    private MediaRecorder mRecorder;  
  
    private final static int CAMERA_ID = 0;  
  
    private boolean mIsRecording = false;  
    private boolean mIsSufaceCreated = false;  
  
    private static final String TAG = "Video";  
  
    private Handler mHandler = new Handler();
    
    private int maxTime = 10;//���¼��ʱ������λ�룬Ĭ��10��
  
    @Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.video);  
        
        videoPath = getIntent().getStringExtra("videoPath");
  
        mCameraPreview = (SurfaceView) findViewById(R.id.camera_preview);  
        mMinutePrefix = (TextView) findViewById(R.id.timestamp_minute_prefix);  
        mMinuteText = (TextView) findViewById(R.id.timestamp_minute_text);  
        mSecondPrefix = (TextView) findViewById(R.id.timestamp_second_prefix);  
        mSecondText = (TextView) findViewById(R.id.timestamp_second_text);  
  
        mSurfaceHolder = mCameraPreview.getHolder();  
        mSurfaceHolder.addCallback(mSurfaceCallback);  
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
  
        mShutter = (ImageButton) findViewById(R.id.record_shutter);  
        mShutter.setOnClickListener(this);
        
        if (getIntent() != null)
        {
        	maxTime = getIntent().getIntExtra("maxTime", 10);//���¼��ʱ��
        }
    }  
  
    @Override  
    protected void onPause() {  
        super.onPause();  
        if (mIsRecording) {  
            stopRecording();  
        }  
        stopPreview();  
    }  
  
    private  SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {  
  
        @Override  
        public void surfaceDestroyed(SurfaceHolder holder) {  
            mIsSufaceCreated = false;  
        }  
  
        @Override  
        public void surfaceCreated(SurfaceHolder holder) {  
            mIsSufaceCreated = true;  
        }  
  
        @Override  
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {  
            startPreview();  
        }  
    };  
  
    //����Ԥ��  
    private void startPreview() {  
        //��ֻ֤��һ��Camera����  
        if (mCamera != null || !mIsSufaceCreated) {  
            Log.d(TAG, "startPreview will return");  
            return;  
        }  
  
        mCamera = Camera.open(CAMERA_ID);  
  
        Parameters parameters = mCamera.getParameters();  
//        Size size = getBestPreviewSize(CameraUtils.PREVIEW_WIDTH, CameraUtils.PREVIEW_HEIGHT, parameters);
        WindowManager windowManager = this.getWindowManager();
        int width  = windowManager.getDefaultDisplay().getWidth();
        int height = windowManager.getDefaultDisplay().getHeight();
        size1 = getBestPreviewSize(height, width, parameters);  

        if (size1 != null) {  
            parameters.setPreviewSize(size1.width, size1.height);  
        }  
  
        parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);  
        parameters.setPreviewFrameRate(20);  
  
        //�������Ԥ������  
        mCamera.setDisplayOrientation(90);  
  
        mCamera.setParameters(parameters);  
  
        try {  
            mCamera.setPreviewDisplay(mSurfaceHolder);  
//          mCamera.setPreviewCallback(mPreviewCallback);  
        } catch (Exception e) {  
            Log.d(TAG, e.getMessage());  
        }  
  
        mCamera.startPreview();  
    }  
  
    private void stopPreview() {  
        //�ͷ�Camera����  
        if (mCamera != null) {  
            try {  
                mCamera.setPreviewDisplay(null);  
            } catch (Exception e) {  
                Log.e(TAG, e.getMessage());  
            }  
  
            mCamera.stopPreview();  
            mCamera.release();  
            mCamera = null;  
        }  
    }  
  
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {  
        Camera.Size result = null;  
        Log.d("TAG", "width1" + width + " height1"+height);
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {  
            if (size.width <= width && size.height <= height) {  
            	Log.d("TAG", "width" + size.width + " height"+size.height);
                if (result == null) {  
                    result = size;  
                } else {  
                    int resultArea = result.width * result.height;  
                    int newArea = size.width * size.height;  
  
                    if (newArea > resultArea) {  
                        result = size;  
                    }  
                }  
            }  
        }  
        Log.d("TAG", "result_width1" + width + " result_height1"+height);
        return result;  
    }  
  
    @Override  
    public void onClick(View v) {  
        if (mIsRecording) {  
            stopRecording();  
        } else {  
            initMediaRecorder();  
            startRecording();  
  
            //��ʼ¼���ÿ��1sȥ����¼���ʱ���  
            mHandler.postDelayed(mTimestampRunnable, 1000);  
        }  
    }  
  
    private void initMediaRecorder() {  
    	
        mRecorder = new MediaRecorder();//ʵ����  
        mCamera.unlock();  
        //��Recorder����Camera���󣬱�֤¼���Ԥ���ķ��򱣳�һ��  
        mRecorder.setCamera(mCamera);  
        mRecorder.setOrientationHint(90);  //�ı䱣������Ƶ�ļ�����ʱ�Ƿ����(������䣬��Ƶ�ļ����ŵ�ʱ��Ƕ��Ƿ���)  
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // ���ô���˷�ɼ�����  
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // ���ô�����ͷ�ɼ�ͼ��  
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);  // ������Ƶ�������ʽ ΪMP4
        mRecorder.setMaxDuration(15000); //�������¼��ʱ��Ϊ15s  
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC); // ������Ƶ�ı����ʽ  
        mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); // ������Ƶ�ı����ʽ  
//        mRecorder.setVideoSize(176, 144);  // ������Ƶ��С
        mRecorder.setVideoSize(size1.width, size1.height);  // ������Ƶ��С  
//        mRecorder.setVideoFrameRate(30); // ����֡��  
        mRecorder.setVideoEncodingBitRate(3 * 1024 * 1024);
        
//      //������ƵԴ  
//        mRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);  
//        //������ƵԴ  
//        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);  
//        
//        //�������������
//       CamcorderProfile cProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH); 
//       mRecorder.setProfile(cProfile);
//        //����¼�Ƶ���Ƶ֡��,ע���ĵ���˵��:  
//       mRecorder.setVideoFrameRate(30);
//        mRecorder.setMaxDuration(5000); //�������¼��ʱ��Ϊ10s  
        mRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());  
  
//        //������Ƶ�洢·��  
//        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES) + File.separator + "VideoRecorder");  
//        if (!file.exists()) {  
//            //�༶�ļ��еĴ���  
//            file.mkdirs();  
//        }  
//        mRecorder.setOutputFile(file.getPath() + File.separator + "VID_" + System.currentTimeMillis() + ".mp4");  
        mRecorder.setOutputFile(videoPath);
    }  
  
    private void startRecording() {  
        if (mRecorder != null) {  
            try {  
                mRecorder.prepare();  
                mRecorder.start();  
            } catch (Exception e) {  
                mIsRecording = false;  
                Log.e(TAG, e.getMessage());  
            }  
        }  
  
        mShutter.setImageDrawable(getResources().getDrawable(R.drawable.recording_shutter_hl));  
        mIsRecording = true;  
    }  
  
    private void stopRecording() {  
        if (mCamera != null) {  
            mCamera.lock();  
        }  
  
        if (mRecorder != null) { 
        	
            mRecorder.stop();  
            mRecorder.release();  
            mRecorder = null;  
        }  
  
        mShutter.setImageDrawable(getResources().getDrawable(R.drawable.recording_shutter));  
        mIsRecording = false;  
  
        mHandler.removeCallbacks(mTimestampRunnable);  
  
        //��¼��ʱ�仹ԭ  
        mMinutePrefix.setVisibility(View.VISIBLE);  
        mMinuteText.setText("0");  
        mSecondPrefix.setVisibility(View.VISIBLE);  
        mSecondText.setText("0");  
  
        setResult(RESULT_OK);
        finish();
        //����Ԥ��  
//        startPreview();  
    }  
  
    private Runnable mTimestampRunnable = new Runnable() {  
        @Override  
        public void run() {  
            updateTimestamp(); 
            
            //if(timeLimit > MAX_TIME)
            if(timeLimit > maxTime)
            {
            	stopRecording();
            }
            mHandler.postDelayed(this, 1000);  
        }  
    };
	private Size size1;
	private String videoPath;  
	private int timeLimit;
	//private int MAX_TIME = 15;//���¼��ʱ��10s
    private void updateTimestamp() {  
        int second = Integer.parseInt(mSecondText.getText().toString());  
        int minute = Integer.parseInt(mMinuteText.getText().toString());  
        second++;  
        //Log.d(TAG, "second: " + second);  
        timeLimit = second;
        if (second < 10) {  
            mSecondText.setText(String.valueOf(second));  
        } else if (second >= 10 && second < 60) {  
            mSecondPrefix.setVisibility(View.GONE);  
            mSecondText.setText(String.valueOf(second));  
        } else if (second >= 60) {  
            mSecondPrefix.setVisibility(View.VISIBLE);  
            mSecondText.setText("0");  
  
            minute++;  
            mMinuteText.setText(String.valueOf(minute));  
        } else if (minute >= 60) {  
            mMinutePrefix.setVisibility(View.GONE);  
        }  
    }  
}  
