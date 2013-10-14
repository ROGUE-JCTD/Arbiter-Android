package com.lmn.Arbiter_Android.Dialog.Dialogs;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class AddLayersDialog extends ArbiterDialogFragment{
	
	public static AddLayersDialog newInstance(String title, String ok, 
			String cancel, int layout){
		AddLayersDialog frag = new AddLayersDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}

	@Override
	public void onPositiveClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerCustomListeners(View view) {
		if(view != null){
			ImageButton button = (ImageButton) view.findViewById(R.id.add_server_button);
			
			if(button != null){
				button.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						(new ArbiterDialogs(getActivity().getResources(), getActivity().getSupportFragmentManager())).showAddServerDialog();
					}
				});
			}
		}
	}
}
