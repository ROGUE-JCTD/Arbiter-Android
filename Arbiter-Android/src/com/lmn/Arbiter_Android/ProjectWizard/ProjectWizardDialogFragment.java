package com.lmn.Arbiter_Android.ProjectWizard;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ProjectWizard.BuildingBlocks.Step;
import com.lmn.Arbiter_Android.R.drawable;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * This class creates a dialog for the project wizard
 */
public class ProjectWizardDialogFragment extends DialogFragment {
	private Step step;
	
	/**
	 * Create a new instance of ProjectWizardDialogFragment, 
	 * providing "message" as an argument.
	 */
	static ProjectWizardDialogFragment newInstance(Step step){
		ProjectWizardDialogFragment frag = new ProjectWizardDialogFragment();
		
		frag.step = step;
		
		return frag;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		return new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.icon)
			.setTitle(step.getTitle())
			.setMessage(step.getMessage())
			.setPositiveButton(step.getOk(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            step.getCallbacks().positiveClick();
                        }
                    }
                )
                .setNegativeButton(step.getCancel(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            step.getCallbacks().negativeClick();
                        }
                    }
                )
                .create();
	}
}
