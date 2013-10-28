package com.lmn.Arbiter_Android.BaseClasses;

import java.util.ArrayList;

public class Project {
	private long id;
	private String projectName;
	private ArrayList<Layer> layers;
	private String aoi;
	
	public Project(long projectId, String projectName, String aoi){
		this.projectName = projectName;
		this.layers = new ArrayList<Layer>();
		this.id = projectId;
		this.aoi = aoi;
	}
	
	public Project(Project project){
		this.projectName = project.getProjectName();
		this.id = project.getId();
		this.aoi = project.getAOI();
		
		this.layers = new ArrayList<Layer>();
		ArrayList<Layer> pLayers = project.getLayers();
		for(int i = 0; i < pLayers.size(); i++){
			this.layers.add(new Layer(pLayers.get(i)));
		}
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
