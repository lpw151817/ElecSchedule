package nercms.schedule.activity;

import nercms.schedule.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FourthFragment extends Fragment {
	Context context;

	public FourthFragment(Context _ctx){
		context = _ctx;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.tab04, container, false);
		
		return view;
	}
}
