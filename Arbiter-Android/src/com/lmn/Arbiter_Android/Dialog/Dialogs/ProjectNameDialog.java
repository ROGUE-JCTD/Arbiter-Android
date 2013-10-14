package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.view.View;

import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;

public class ProjectNameDialog extends ArbiterDialogFragment{
	public static ProjectNameDialog newInstance(String title, String ok, 
			String cancel, int layout){
		ProjectNameDialog frag = new ProjectNameDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		return frag;
	}

	@Override
	public void onPositiveClick() {
		(new ArbiterDialogs(getActivity().getResources(), getActivity().getSupportFragmentManager())).showAddLayersDialog();
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
