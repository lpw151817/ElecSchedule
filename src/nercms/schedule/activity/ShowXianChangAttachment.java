package nercms.schedule.activity;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nercms.schedule.R;
import nercms.schedule.utils.Utils;
import nercms.schedule.view.AudioRecorder;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class ShowXianChangAttachment extends Activity implements OnClickListener{
	
	
	private ImageView mImage;
	private TextView mTime;
	private Button mOk;
	private Button mCancel;
	private String imagePath;
	private int mPosition;
	private String type;
	private String audioPath;
	
	private Map<Integer, Map<String, String>> mContent;
	private Map<String, String> mMap;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
		setContentView(R.layout.showxianchangsiattachment);
		
		mImage = (ImageView) findViewById(R.id.iv_attach);
		mTime = (TextView) findViewById(R.id.tv_time);
		mOk = (Button) findViewById(R.id.bt_ok);
		mCancel = (Button) findViewById(R.id.bt_cancel);
				
		
//		Intent intent  = getIntent();
//		Bundle bundle = intent.getBundleExtra("address");
//		type = bundle.getString("type");
//		mPosition = bundle.getInt("position");
		
		mContent =  (Map<Integer, Map<String, String>>) getIntent().getSerializableExtra("address");
		mMap = mContent.get(0);
		type = mMap.get("type");
		mPosition = Integer.parseInt(mMap.get("position"));
		
		
		if (type.equals("image")){
//			imagePath = bundle.getString("image");
			imagePath = mMap.get("image");
			Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
			//旋转
			int degree = Utils.readPictureDegree(imagePath);  
			bitmap = Utils.rotateBitmap(bitmap,degree) ;
			mImage.setImageBitmap(bitmap);
		} else if ( type.equals("audio")){
//			audioPath = bundle.getString("audiopath");
			audioPath = mMap.get("audiopath");
			System.out.println("showXianChangActivity: " + audioPath);
			mImage.setImageResource(R.drawable.record);
			mImage.setOnClickListener(this);
		}
		
		mTime.setText(Utils.formatDateMs(System.currentTimeMillis()));
		
		
		mOk.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		
	}

	

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.bt_ok:
			Intent intent = new Intent(ShowXianChangAttachment.this, XianChangAdd.class);
			intent.putExtra("position", mPosition);
			
			if (type.equals("image")){//传递附件的路径
				intent.putExtra("path", imagePath);
			} else if ( type.equals("audio")){
				intent.putExtra("path", audioPath);
			}
			startActivity(intent);
			finish();
			break;
			
		case R.id.bt_cancel:
			if (type.equals("image")){//传递附件的路径
				Utils.deleteMedia(imagePath);
			} else if ( type.equals("audio")){
				Utils.deleteMedia(audioPath);
			}
			
			
			finish();
			break;
			
		case R.id.iv_attach:
			File file = new File(audioPath);
			Uri uri = Uri.fromFile(file);
			Intent intent1 = new Intent(Intent.ACTION_MAIN);
			intent1.setAction(Intent.ACTION_DEFAULT);
			intent1.setDataAndType(uri, "audio/*");
			startActivity(intent1);
			break;

		default:
			break;
		}
	}
}
