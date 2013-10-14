package com.lmn.Arbiter_Android;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;

public class AddLayersButton extends ImageButton {

	public AddLayersButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public AddLayersButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public AddLayersButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public void setOnClick(){
		this.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
			}
		});
	}
}
