package com.lmn.Arbiter_Android.ProjectWizard;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.ProjectWizard.Steps.Step;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

/**
 * This class creates a dialog for the project wizard
 */
public class ProjectWizardDialogFragment extends DialogFragment {
	private Step step;
	private AlertDialog dialog;
	
	/**
	 * Create a new instance of ProjectWizardDialogFragment
	 * @param step The first step of the wizard
	 * @return
	 */
	public static ProjectWizardDialogFragment newInstance(Step step){
		ProjectWizardDialogFragment frag = new ProjectWizardDialogFragment();
		
		frag.setStep(step);
		
		return frag;
	}
	
	public void setStep(Step step){
		this.step = step;
	}
	
	public Step getStep(){
		return this.step;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		LayoutInflater inflater = getActivity().getLayoutInflater();
		
		dialog = new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.icon)
			.setTitle(step.getTitle())
			.setView(inflater.inflate(step.getLayout(), null))
			.setPositiveButton(step.getOk(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            step.positiveClick();
                        }
                    }
                )
                .setNegativeButton(step.getCancel(),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	step.negativeClick();
                        }
                    }
                )
                .create();
		
		dialog.getWindow().getAttributes().windowAnimations = R.style.project_wizard_animation;
		return dialog;
	}
	
	/**
	 * Save the step for recreation
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		//saveCurrentStep(this.step, savedInstanceState);
	}
}
