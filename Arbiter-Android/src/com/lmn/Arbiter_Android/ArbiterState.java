package com.lmn.Arbiter_Android;

import com.lmn.Arbiter_Android.BaseClasses.Feature;

public class ArbiterState {
	private ArbiterProject arbiterProject;
	private String newAOI;
	private Feature feature;
	private String layerId;
	
	private ArbiterState(){
		this.arbiterProject = ArbiterProject.getArbiterProject();
		this.newAOI = null;
	}
	
	private static ArbiterState arbiterState = null;
	
	public static ArbiterState getArbiterState(){
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
	
	public void editingFeature(Feature feature, String layerId){
		this.feature = feature;
		this.layerId = layerId;
	}
	
	public void doneEditingFeature(){
		this.feature = null;
	}
	
	public Feature isEditingFeature(){
		return this.feature;
	}
	
	public String getLayerBeingEdited(){
		return this.layerId;
	}
}
