package nercms.schedule.activity;


import nercms.schedule.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.readystatesoftware.viewbadger.BadgeView;

public class FirstFragment extends Fragment {

	private View view;
	private BadgeView badgeView;
	Context context;
	TextView tv;
	private BadgeView badgeView2;
	int mcount = 0;

	public FirstFragment(Context context, TextView tv) {
		this.context = context;
		this.tv = tv;
	}
	
@Override
public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
}


	@Override
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container,  Bundle savedInstanceState) {
			
		
		view = inflater.inflate(R.layout.tab01, container, false);
		
		Button mNewTask = (Button) view.findViewById(R.id.tab01_bt_newtask);
		
		if (badgeView == null || badgeView2 == null){
		badgeView = new BadgeView(context, mNewTask);
		badgeView2 = new BadgeView(context, tv);
		}
		Button add = (Button) view.findViewById(R.id.tab01_bt_add);
		Button decrease = (Button) view.findViewById(R.id.tab01_bt_decrease);
		
		add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mcount++;
				show(mcount);
				
			}
		});
		
		decrease.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mcount--;
				show(mcount);
			}
		});
		
		return view;
	}
	
	
	
	public void show(int count) {
		if (count == 0 || count < 0){
			badgeView.setVisibility(View.GONE);
			badgeView2.setVisibility(View.GONE);
			mcount = 0;
			return;
		}
		badgeView.setText(count+"");
		badgeView.show();
		
		badgeView2.setText(count+"");
		badgeView2.show();
		
	}
	
}
