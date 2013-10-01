package com.lmn.Arbiter_Android.ProjectWizard.Steps;

import com.lmn.Arbiter_Android.ProjectWizard.ProjectWizard;
import com.lmn.Arbiter_Android.ProjectWizard.Transition;

/**
 * Building block of the project wizard
 * @author Zusy
 *
 */
public class Step{
	private String title;
	private int layout; // R.layout.*
	private String ok;
	private String cancel;
	private ProjectWizard projectWizard;
	
	public Step(String title, int layout,
			String ok, String cancel, ProjectWizard projectWizard){
		this.title = title;
		this.layout = layout;
		this.ok = ok;
		this.cancel = cancel;
		this.projectWizard = projectWizard;
	}
	
	public String getTitle(){
		return title;
	}
	
	public String getOk(){
		return ok;
	}
	
	public String getCancel(){
		return cancel;
	}
	
	public int getLayout(){
		return layout;
	}
	
	public ProjectWizard getProjectWizard(){
		return projectWizard;
	}
	
	public void positiveClick(){
		projectWizard.transition(Transition.OK);
	}
	
	public void negativeClick(){
		projectWizard.transition(Transition.SKIP);
	}
}
