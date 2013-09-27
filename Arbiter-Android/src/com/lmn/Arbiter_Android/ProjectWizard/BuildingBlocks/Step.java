package com.lmn.Arbiter_Android.ProjectWizard.BuildingBlocks;

/**
 * Building block of the project wizard
 * @author Zusy
 *
 */
public class Step{
	private String title;
	private String message;
	private String ok;
	private String cancel;
	private DialogCallbacks callbacks;
	
	public Step(String title, String message, String ok,
			String cancel, DialogCallbacks callbacks){
		this.title = title;
		this.message = message;
		this.ok = ok;
		this.cancel = cancel;
		this.callbacks = callbacks;
	}
	
	public String getTitle(){
		return this.title;
	}
	
	public String getMessage(){
		return this.message;
	}
	
	public String getOk(){
		return this.ok;
	}
	
	public String getCancel(){
		return this.cancel;
	}
	
	public DialogCallbacks getCallbacks(){
		return this.callbacks;
	}
}
