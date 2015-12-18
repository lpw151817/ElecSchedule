package nercms.schedule.view;



import nercms.schedule.R;
import android.app.Activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.view.Window;
import android.widget.MediaController;
import android.widget.RelativeLayout;

public class PlayVideo extends Activity {
	private MyVideoView video1;
	private MediaController media;
	private String path;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		setContentView(R.layout.activity_video);
		
		video1 =  (MyVideoView) findViewById(R.id.video1);
		media = new MediaController(this);
		
		Intent intent = getIntent();
		path = intent.getStringExtra("path");
		
		video1.setVideoPath(path);
		video1.setMediaController(media);
		media.setMediaPlayer(video1);
		
		
		   RelativeLayout.LayoutParams layoutParams=  
		              new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT, RelativeLayout.LayoutParams.FILL_PARENT);  
		           layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);  
		           layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);  
		           layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);  
		           layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);  
		           video1.setLayoutParams(layoutParams);
		
		video1.start();
		video1.requestFocus();
		
		video1.setOnCompletionListener(new OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer arg0) {
				finish();
			}
		});
		
		
		
	}
	
	
	
	
}
