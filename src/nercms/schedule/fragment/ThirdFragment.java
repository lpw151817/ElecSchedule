package nercms.schedule.fragment;


import nercms.schedule.R;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class ThirdFragment extends Fragment {
	Context context;
	public ThirdFragment(Context context) {
		this.context = context;
	}
	public View onCreateView(LayoutInflater inflater,
			ViewGroup container,  Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tab03, container, false);
	}
	
	
}
