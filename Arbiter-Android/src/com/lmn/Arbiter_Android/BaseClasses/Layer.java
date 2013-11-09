package com.lmn.Arbiter_Android.BaseClasses;

public class Layer {
	public static final int DEFAULT_FLAG = -1;
	public static final String DEFAULT_LAYER_NAME = "OpenStreetMap";
	
	/**
	 * Create a key for the methods requiring a key (removeDuplicates, setCheckedLayers..)
	 * @param layer Layer to create the key with
	 * @return
	 */
	public static String buildLayerKey(Layer layer){
		return Integer.valueOf(layer.getServerId()).toString() + ":" +
				layer.getFeatureType();
	}
	
	private int layerId;
	private String featureType;
	
	// Only for the adapter, for displaying
    private String serverName;
    private String serverUrl;
	
	private String title;
	private String srs;
	private String boundingBox;
	private int serverId;
	
	// Recycled for whether the layer is checked in the AddLayers List
	// and for the layers visibility
	private boolean checked;
	
	private boolean isDefaultLayer;
	
	public Layer(int layerId, String featureType, int serverId, String serverName, String serverUrl,
			String title, String srs, String boundingBox, boolean checked){
		this.layerId = layerId;
		this.featureType = featureType;
		this.serverName = serverName;
		this.title = title;
		this.srs = srs;
		this.boundingBox = boundingBox;
		this.serverId = serverId;
		this.serverUrl = serverUrl;
		this.isDefaultLayer = false;
		
		setChecked(checked);
	}
	
	// For cloning
	public Layer(Layer item){
		this.layerId = item.getLayerId();
		this.featureType = item.getFeatureType();
		this.serverName = item.getServerName();
		this.title = item.getLayerTitle();
		this.srs = item.getLayerSRS();
		this.boundingBox = item.getLayerBBOX();
		this.checked = item.isChecked();
		this.serverId = item.getServerId();
		this.serverUrl = item.getServerUrl();
		this.isDefaultLayer = item.isDefaultLayer();
	}
	
	public boolean isDefaultLayer(){
		return this.isDefaultLayer;
	}
	
	public void setIsDefaultLayer(boolean isDefaultLayer){
		this.isDefaultLayer = isDefaultLayer;
	}
	
	public int getLayerId(){
		return layerId;
	}
	
	public String getFeatureType(){
		return featureType;
	}
	
	public String getServerName(){
        return serverName;
	}
	
	public void setServerName(String name){
		this.serverName = name;
	}
	
	public String getLayerTitle(){
		return title;
	}
	
	public String getLayerSRS(){
		return srs;
	}

	public String getLayerBBOX(){
		return boundingBox;
	}
	
	public boolean isChecked(){
		return this.checked;
	}
	
	public int getServerId(){
		return this.serverId;
	}
	
	public String getServerUrl(){
        return this.serverUrl;
	}
	
	public void setServerUrl(String url){
		this.serverUrl = url;
	}
	
	public void setChecked(boolean check){
		this.checked = check;
	}
	
	@Override
	public String toString(){
		return  "\t layerId: " + layerId +
				"\n\t featureType: " + featureType + 
				"\n\t serverName: " + serverName +
				"\n\t serverId: " + serverId +
				"\n\t title: " + title + 
				"\n\t srs: " + srs +
				"\n\t boundingBox: " + boundingBox;
	}
	
}
