package com.lmn.Arbiter_Android.Activities;

import com.lmn.Arbiter_Android.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class DialogActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		this.requestWindowFeature(Window.FEATURE_ACTION_BAR);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND,
				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		
		WindowManager.LayoutParams params = this.getWindow().getAttributes();
		params.height = WindowManager.LayoutParams.MATCH_PARENT;
		params.width = WindowManager.LayoutParams.MATCH_PARENT;
		params.gravity = Gravity.CENTER;
		params.horizontalMargin = 10;
		params.verticalMargin = 10;
		setContentView(R.layout.add_layers_dialog);
	}
}
