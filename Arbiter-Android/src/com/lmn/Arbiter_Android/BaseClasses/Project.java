package com.lmn.Arbiter_Android.BaseClasses;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.Util;

public class Project {
	private long id;
	private String projectName;
	private ArrayList<Layer> layers;
	private String aoi;
	private boolean includeDefaultLayer;
	private boolean defaultLayerVisibility;
	private boolean inCreation;
	
	public Project(long projectId, String projectName, String aoi, 
			boolean includeDefaultLayer, boolean defaultLayerVisibility){
		this.projectName = projectName;
		this.layers = new ArrayList<Layer>();
		this.id = projectId;
		this.aoi = aoi;
		this.includeDefaultLayer = includeDefaultLayer;
		this.defaultLayerVisibility = defaultLayerVisibility;
		this.inCreation = false;
	}
	
	public Project(long projectId, String projectName, String aoi, 
			int includeDefaultLayer, int defaultLayerVisibility){
		this.projectName = projectName;
		this.layers = new ArrayList<Layer>();
		this.id = projectId;
		this.aoi = aoi;
		this.includeDefaultLayer = Util.convertIntToBoolean(includeDefaultLayer);
		this.defaultLayerVisibility = Util.convertIntToBoolean(defaultLayerVisibility);
		this.inCreation = false;
	}
	
	public Project(Project project){
		this.projectName = project.getProjectName();
		this.id = project.getId();
		this.aoi = project.getAOI();
		this.includeDefaultLayer = project.includeDefaultLayer();
		this.defaultLayerVisibility = project.getDefaultLayerVisibility();
		this.inCreation = project.isBeingCreated();
		
		this.layers = new ArrayList<Layer>();
		ArrayList<Layer> pLayers = project.getLayers();
		for(int i = 0; i < pLayers.size(); i++){
			this.layers.add(new Layer(pLayers.get(i)));
		}
	}
	
	public boolean isBeingCreated(){
		return this.inCreation;
	}
	
	public void isBeingCreated(boolean inCreation){
		this.inCreation = inCreation;
	}
	
	public void setDefaultLayerVisibility(boolean visibility){
		this.defaultLayerVisibility = visibility;
	}
	
	public boolean getDefaultLayerVisibility(){
		return this.defaultLayerVisibility;
	}
	
	public String getAOI(){
		return this.aoi;
	}
	
	public void setAOI(String aoi){
		this.aoi = aoi;
	}
	
	public String getProjectName(){
		return projectName;
	}
	
	public void addLayer(Layer layer){
		layers.add(layer);
	}
	
	public long getId(){
		return id;
	}
	
	public boolean includeDefaultLayer(){
		return includeDefaultLayer;
	}
	
	public void includeDefaultLayer(boolean includeDefaultLayer){
		this.includeDefaultLayer = includeDefaultLayer;
	}
	
	/**
	 * Convenience method to add more than 1 layer at a time
	 * @param layer
	 */
	public void addLayers(ArrayList<Layer> layers){
		for(int i = 0; i < layers.size(); i++){
			addLayer(layers.get(i));
		}
	}
	
	public ArrayList<Layer> getLayers(){
		return layers;
	}
}
