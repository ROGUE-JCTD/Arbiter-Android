package com.lmn.Arbiter_Android.ProjectWizard.Steps;

import com.lmn.Arbiter_Android.ProjectWizard.ProjectWizard;

import android.util.Log;

public class ChooseName extends Step{
	
	public ChooseName(String title, int layout,
			String ok, String cancel, ProjectWizard projectWizard){
		super(title, layout,
				ok, cancel, projectWizard);
	}
	
	@Override
	public void positiveClick() {
		super.positiveClick();
		Log.w("Arbiter_Android", "CreateProject - Positive click");
	}

	@Override
	public void negativeClick() {
		super.negativeClick();
		Log.w("Arbiter_Android", "CreateProject - Negative click");
	}
}
