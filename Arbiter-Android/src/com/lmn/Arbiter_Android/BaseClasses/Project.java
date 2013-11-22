package com.lmn.Arbiter_Android.BaseClasses;

import java.util.ArrayList;

public class Project {
	private String projectName;
	private ArrayList<Layer> layers;
	private String aoi;
	private String includeDefaultLayer;
	private String defaultLayerVisibility;
	
	public Project(String projectName, String aoi, 
			String includeDefaultLayer, String defaultLayerVisibility){
		this.projectName = projectName;
		this.layers = new ArrayList<Layer>();
		this.aoi = aoi;
		this.includeDefaultLayer = includeDefaultLayer;
		this.defaultLayerVisibility = defaultLayerVisibility;
	}
	
	public Project(Project project){
		this.projectName = project.getProjectName();
		this.aoi = project.getAOI();
		this.includeDefaultLayer = project.includeDefaultLayer();
		this.defaultLayerVisibility = project.getDefaultLayerVisibility();
		
		this.layers = new ArrayList<Layer>();
		ArrayList<Layer> pLayers = project.getLayers();
		for(int i = 0; i < pLayers.size(); i++){
			this.layers.add(new Layer(pLayers.get(i)));
		}
	}
	
	public void setDefaultLayerVisibility(String visibility){
		this.defaultLayerVisibility = visibility;
	}
	
	public String getDefaultLayerVisibility(){
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
	
	public String includeDefaultLayer(){
		return includeDefaultLayer;
	}
	
	public void includeDefaultLayer(String includeDefaultLayer){
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
