package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.app.AlertDialog;
import android.view.View;

import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;

public class LayerInfoDialog extends ArbiterDialogFragment{
	public static LayerInfoDialog newInstance(String title, String ok, 
			String cancel, int layout){
		LayerInfoDialog frag = new LayerInfoDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		return frag;
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
		// TODO Auto-generated method stub
		
	}
}
