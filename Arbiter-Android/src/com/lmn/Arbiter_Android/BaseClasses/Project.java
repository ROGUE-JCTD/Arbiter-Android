package com.lmn.Arbiter_Android.BaseClasses;

import java.util.ArrayList;

public class Project {
	private int id;
	private String projectName;
	private ArrayList<Layer> layers;
	
	public Project(int projectId, String projectName){
		this.projectName = projectName;
		this.layers = new ArrayList<Layer>();
		this.id = projectId;
	}
	
	public Project(Project project){
		this.projectName = project.getProjectName();
		this.id = project.getId();
		
		this.layers = new ArrayList<Layer>();
		ArrayList<Layer> pLayers = project.getLayers();
		for(int i = 0; i < pLayers.size(); i++){
			this.layers.add(new Layer(pLayers.get(i)));
		}
	}
	
	public String getProjectName(){
		return projectName;
	}
	
	public void addLayer(Layer layer){
		layers.add(layer);
	}
	
	public int getId(){
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