package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.content.Context;
import android.view.View;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;

public class GoOfflineDialog extends ArbiterDialogFragment{
	private boolean creatingProject;
	
	public static GoOfflineDialog newInstance(String title, String ok, 
			String cancel, int layout, boolean creatingProject){
		GoOfflineDialog frag = new GoOfflineDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		frag.creatingProject = creatingProject;
		
		return frag;
	}

	@Override
	public void onPositiveClick() {
		// Write the project and layers to the database
		if(creatingProject){
			final Context context = this.getActivity().getApplicationContext();
			//ArbiterProject.getArbiterProject().commitProject(context);
		}
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		// TODO Auto-generated method stub
		
	}
}
