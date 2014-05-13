package com.lmn.Arbiter_Android.ReturnQueues;


public class OnReturnToProjects extends ArbiterQueue{
	
	private OnReturnToProjects(){
		
		super();
	}
	
	private static OnReturnToProjects onReturnToProjects;
	
	public static OnReturnToProjects getInstance(){
		
		if(onReturnToProjects == null){
			onReturnToProjects = new OnReturnToProjects();
		}
		
		return onReturnToProjects;
	}
}
