package com.lmn.Arbiter_Android.BaseClasses;

import java.util.ArrayList;

public class Project {
	private String projectName;
	private ArrayList<Layer> layers;
	private ArrayList<Tileset> tilesets;
	private String aoi;
	private BaseLayer baseLayer;
	private String downloadPhotos;
	private String disableWMS;
	private String noConnectionChecks;
	private String alwaysShowLocation;
	
	public Project(String projectName, String aoi){
		this.projectName = projectName;
		this.layers = new ArrayList<Layer>();
		this.tilesets = new ArrayList<Tileset>();
		this.aoi = aoi;
		this.baseLayer = null;
		this.downloadPhotos = null;
		this.disableWMS = null;
		this.noConnectionChecks = null;
		this.alwaysShowLocation = null;
	}
	
	public Project(Project project){
		this.projectName = project.getProjectName();
		this.aoi = project.getAOI();
		
		this.layers = new ArrayList<Layer>();
		ArrayList<Layer> pLayers = project.getLayers();
		for(int i = 0; i < pLayers.size(); i++){
			this.layers.add(new Layer(pLayers.get(i)));
		}

		this.tilesets = new ArrayList<Tileset>();
		ArrayList<Tileset> pTilesets = project.getTilesets();
		for(int i = 0; i < pTilesets.size(); i++){
			this.tilesets.add(new Tileset(pTilesets.get(i)));
		}
		
		this.baseLayer = project.getBaseLayer();
		
		this.downloadPhotos = project.shouldDownloadPhotos();
		this.disableWMS = project.shouldDisableWMS();
		this.noConnectionChecks = project.shouldCheckConnections();
		this.alwaysShowLocation = project.shouldAlwaysShowLocation();
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

	public void addTileset(Tileset tileset) { tilesets.add(tileset); }
	
	public void setBaseLayer(BaseLayer baseLayer){
		this.baseLayer = baseLayer;
	}
	
	public BaseLayer getBaseLayer(){
		return this.baseLayer;
	}
	
	public String shouldDownloadPhotos(){
		return this.downloadPhotos;
	}
	
	public String shouldDisableWMS(){
		return this.disableWMS;
	}
	
	public String shouldCheckConnections(){
		return this.noConnectionChecks;
	}
	
	public String shouldAlwaysShowLocation(){
		return this.alwaysShowLocation;
	}
	
	public void setDownloadPhotos(String downloadPhotos){
		this.downloadPhotos = downloadPhotos;
	}
	
	public void setDisableWMS(String disableWMS){
		this.disableWMS = disableWMS;
	}
	
	public void setNoConnectionChecks(String noConnectionChecks){
		this.noConnectionChecks = noConnectionChecks;
	}
	
	public void setAlwaysShowLocation(String alwaysShowLocation){
		this.alwaysShowLocation = alwaysShowLocation;
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

	public void addTilesets(ArrayList<Tileset> tilesets){
		for(int i = 0; i < tilesets.size(); i++){
			addTileset(tilesets.get(i));
		}
	}

	public ArrayList<Tileset> getTilesets(){
		return tilesets;
	}
}
