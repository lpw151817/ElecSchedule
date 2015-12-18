package nercms.schedule.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.Toast;
 
public class RecordButton extends Button {
 
    private static final int MIN_RECORD_TIME = 1; // ���¼��ʱ�䣬��λ��
    private static final int RECORD_OFF = 0; // ����¼��
    private static final int RECORD_ON = 1; // ����¼��
 
    private RecordStrategy mAudioRecorder;
    private Thread mRecordThread;
    private RecordListener listener;
 
    private int recordState = 0; // ¼��״̬
    private float recodeTime = 0.0f; // ¼��ʱ�������¼��ʱ��̫����¼��ʧ��
    private boolean isCanceled = false; // �Ƿ�ȡ��¼��
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
        this.setText("��ס ˵��");
    }
 
    public void setAudioRecord(RecordStrategy record) {
        this.mAudioRecorder = record;
    }
 
    public void setRecordListener(RecordListener listener) {
        this.listener = listener;
    }
 
    // ¼��ʱ��̫��ʱToast��ʾ
    private void showWarnToast(String toastText) {
    	
    	Toast.makeText(mContext, toastText, Toast.LENGTH_SHORT).show();
    }
 
    // ����¼����ʱ�߳�
    private void callRecordTimeThread() {
        mRecordThread = new Thread(recordThread);
        mRecordThread.start();
    }
 
 
    // ¼���߳�
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
        case MotionEvent.ACTION_DOWN: // ���°�ť
        	this.setText("�ɿ���ָ ȡ��¼��");
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
        case MotionEvent.ACTION_UP: // �ɿ���ָ
            if (recordState == RECORD_ON) {
                recordState = RECORD_OFF;
                mAudioRecorder.stop();
                mRecordThread.interrupt();
                if (isCanceled) {
                    mAudioRecorder.deleteOldFile();
                } else {
                    if (recodeTime < MIN_RECORD_TIME) {
                        showWarnToast("ʱ��̫��  ¼��ʧ��");
                        mAudioRecorder.deleteOldFile();
                    } 
                    else {
                    	if (mListener!=null) {// ����callbackActivity������¼��
            				
        					mListener.onFinished(mAudioRecorder.getFilePath());
        				}
                    	
                    }
                }
                
                isCanceled = false;
                this.setText("��ס ˵��");
            }
            break;
//        	this.setText("��ס ˵��");
        }
        return true;
    }
 
    public interface RecordListener {
        public void recordEnd(String filePath);
    }
}
