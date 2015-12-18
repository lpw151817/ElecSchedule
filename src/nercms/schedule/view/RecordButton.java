package nercms.schedule.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;
 
public class RecordButton extends Button {
 
    private static final int MIN_RECORD_TIME = 1; // 最短录音时间，单位秒
    private static final int RECORD_OFF = 0; // 不在录音
    private static final int RECORD_ON = 1; // 正在录音
 
    private RecordStrategy mAudioRecorder;
    private Thread mRecordThread;
    private RecordListener listener;
 
    private int recordState = 0; // 录音状态
    private float recodeTime = 0.0f; // 录音时长，如果录音时间太短则录音失败
    private boolean isCanceled = false; // 是否取消录音
//    private float downY;
 
    private Context mContext;
    
    public interface AudioFinishRecorderListener{
		void onFinished(String filePath);
	}
    
    public interface AudioStartRecorderListener{
    	void onStart();
    }
	
	private AudioFinishRecorderListener mListener;
	
	public void setAudioFinishRecorderListener(AudioFinishRecorderListener listener)
	{
		mListener=listener;
	}
	
	private AudioStartRecorderListener mStartListener;
	public void setAudioStartRecorderListener(AudioStartRecorderListener listener){
		mStartListener = listener;
	}
 
    public RecordButton(Context context) {
        super(context);
        init(context);
    }
 
    public RecordButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }
 
    public RecordButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
 
    private void init(Context context) {
        mContext = context;
        this.setText("按住 说话");
    }
 
    public void setAudioRecord(RecordStrategy record) {
        this.mAudioRecorder = record;
    }
 
    public void setRecordListener(RecordListener listener) {
        this.listener = listener;
    }
 
    // 录音时间太短时Toast显示
    private void showWarnToast(String toastText) {
    	
    	Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT).show();
    }
 
    // 开启录音计时线程
    private void callRecordTimeThread() {
        mRecordThread = new Thread(recordThread);
        mRecordThread.start();
    }
 
 
    // 录音线程
    private Runnable recordThread = new Runnable() {
 
        @Override
        public void run() {
            recodeTime = 0.0f;
            while (recordState == RECORD_ON) {
                {
                    try {
                        Thread.sleep(100);
                        recodeTime += 0.1;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    };
 
 
    @Override
    public boolean onTouchEvent(MotionEvent event) {
    	
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN: // 按下按钮
        	this.setText("松开手指 取消录音");
            if (recordState != RECORD_ON) {
                if (mAudioRecorder != null) {
                    mAudioRecorder.ready();
                    recordState = RECORD_ON;
                    mAudioRecorder.start();
                    callRecordTimeThread();
                    
                    if (mStartListener != null){
                    	mStartListener.onStart();
                    }
                }
                
            }
            break;
        case MotionEvent.ACTION_UP: // 松开手指
            if (recordState == RECORD_ON) {
                recordState = RECORD_OFF;
                mAudioRecorder.stop();
                mRecordThread.interrupt();
                if (isCanceled) {
                    mAudioRecorder.deleteOldFile();
                } else {
                    if (recodeTime < MIN_RECORD_TIME) {
                        showWarnToast("时间太短  录音失败");
                        mAudioRecorder.deleteOldFile();
                    } 
                    else {
                    	if (mListener!=null) {// 并且callbackActivity，保存录音
            				
        					mListener.onFinished(mAudioRecorder.getFilePath());
        				}
                    	
                    }
                }
                
                isCanceled = false;
                this.setText("按住 说话");
            }
            break;
//        	this.setText("按住 说话");
        }
        return true;
    }
 
    public interface RecordListener {
        public void recordEnd(String filePath);
    }
}
