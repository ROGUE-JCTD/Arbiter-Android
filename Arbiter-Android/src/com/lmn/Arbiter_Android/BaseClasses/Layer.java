package com.lmn.Arbiter_Android.BaseClasses;

public class Layer {
	
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
	private String srs;
	private String workspace;
	
	// Only for the adapter, for displaying
    private String serverName;
    private String serverUrl;
	
	private String title;
	private String boundingBox;
	private String color = null;
	
	private int serverId;
	private int layerOrder;
	
	// Recycled for whether the layer is checked in the AddLayers List
	// and for the layers visibility
	private boolean checked;
	
	
	public Layer(int layerId, String featureType, String workspace, int serverId, String serverName, String serverUrl,
			String title, String boundingBox, String color, int layerOrder, boolean checked){
		this.layerId = layerId;
		this.featureType = featureType;
		this.serverName = serverName;
		this.title = title;
		this.boundingBox = boundingBox;
		this.color = color;
		this.srs = null;
		this.serverId = serverId;
		this.serverUrl = serverUrl;
		this.workspace = workspace;
		this.layerOrder = layerOrder;
		
		setChecked(checked);
	}
	
	public Layer(int layerId, String featureType, String workspace, int serverId, String serverName, String serverUrl,
			String title, String srs, String boundingBox, String color, int layerOrder, boolean checked){
		this(layerId, featureType, workspace, serverId, serverName, serverUrl,
				title, boundingBox, color, layerOrder, checked);
		
		this.srs = srs;
	}
	
	// For cloning
	public Layer(Layer item){
		this.layerId = item.getLayerId();
		this.featureType = item.getFeatureType();
		this.serverName = item.getServerName();
		this.title = item.getLayerTitle();
		this.boundingBox = item.getLayerBBOX();
		this.color = item.getColor();
		this.checked = item.isChecked();
		this.srs = item.getSRS();
		this.serverId = item.getServerId();
		this.serverUrl = item.getServerUrl();
		this.workspace = item.getWorkspace();
		this.layerOrder = item.getLayerOrder();
	}
	
	public int getLayerId(){
		return layerId;
	}
	
	public String getFeatureType(){
		return featureType;
	}
	
	public String getFeatureTypeNoPrefix(){
		if(featureType.contains(":")){
			return featureType.split(":")[1];
		}
		
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

	public String getSRS(){
		return srs;
	}
	
	public String getLayerBBOX(){
		return boundingBox;
	}
	
	public String getColor(){
		return color;
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
	
	public void setColor(String color){
		this.color = color;
	}
	
	public String getWorkspace(){
		return this.workspace;
	}
	
	public int getLayerOrder(){
		return this.layerOrder;
	}
	
	public void setLayerOrder(int layerOrder){
		this.layerOrder = layerOrder;
	}
}
