package com.lmn.Arbiter_Android.Projects;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.ListItems.AddLayersListItem;

public class ProjectComponents{
	private String name;
	private ArrayList<AddLayersListItem> layers;
	
	private ProjectComponents(){
		this.name = null;
		this.layers = new ArrayList<AddLayersListItem>();
	}
	
	private static ProjectComponents projectComponents = null;
	
	public static ProjectComponents getProjectComponents(){
		if(projectComponents == null){
			projectComponents = new ProjectComponents();
		}
		
		return projectComponents;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public String getName(){
		return this.name;
	}
	
	public void addLayer(AddLayersListItem layer){
		layers.add(layer);
	}
	
	public void removeLayer(AddLayersListItem layer){
		layers.remove(layer);
	}
	
	public ArrayList<AddLayersListItem> getLayers(){
		return this.layers;
	}
}
