package com.lmn.Arbiter_Android.ProjectWizard.Steps;

import com.lmn.Arbiter_Android.ProjectWizard.ProjectWizard;

public class CreateProject extends Step{
	
	public CreateProject(String title, int layout,
			String ok, String cancel, ProjectWizard projectWizard){
		super(title, layout,
				ok, cancel, projectWizard);
	}
	
	@Override
	public void positiveClick() {
		super.positiveClick();
		
	}

	@Override
	public void negativeClick() {
		super.negativeClick();
		
	}
}
