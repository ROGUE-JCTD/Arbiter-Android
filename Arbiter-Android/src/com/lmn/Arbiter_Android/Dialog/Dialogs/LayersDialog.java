package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;
import android.widget.ImageButton;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;

public class LayersDialog extends ArbiterDialogFragment{
	
	public static LayersDialog newInstance(String title, String ok, 
			String cancel, int layout){
		LayersDialog frag = new LayersDialog();
		
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
	
	public void toggleLayerVisibility(View view){
		// Is the toggle on?
		boolean on = ((ToggleButton) view).isChecked();
		
		if (on) {
			
		} else {
			
		}
	}

	@Override
	public void beforeCreateDialog(View view) {
		if(view != null){
			ImageButton button = (ImageButton) view.findViewById(R.id.add_layers_button);
			if(button != null){
				button.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						// Open the add layers dialog
						(new ArbiterDialogs(getActivity().getResources(), 
								getActivity().getSupportFragmentManager())).showAddLayersDialog();
					}
				});
			}
		}
	}
}
