package com.lmn.Arbiter_Android;

public class ArbiterState {
	private ArbiterProject arbiterProject;
	private String newAOI;
	
	private ArbiterState(){
		this.arbiterProject = ArbiterProject.getArbiterProject();
		this.newAOI = null;
	}
	
	private static ArbiterState arbiterState = null;
	
	public static ArbiterState getState(){
		if(arbiterState == null){
			arbiterState = new ArbiterState();
		}
		
		return arbiterState;
	}
	
	public boolean isCreatingProject(){
		return (arbiterProject.getNewProject() != null);
	}
	
	public void setNewAOI(String newAOI){
		this.newAOI = newAOI;
	}
	
	public String getNewAOI(){
		return newAOI;
	}
	
	public boolean isSettingAOI(){
		return newAOI != null;
	}
}
