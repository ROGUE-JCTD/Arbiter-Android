package com.lmn.Arbiter_Android.BaseClasses;

public class Layer {
	private int layerId;
	private String featureType;
	
	// Only for the adapter, for displaying
	private String serverName;
	private String serverUrl;
	
	private String title;
	private String srs;
	private String boundingBox;
	private int serverId;
	private boolean checked;
	
	public Layer(int layerId, String featureType, int serverId, String serverName, String serverUrl,
			String title, String srs, String boundingBox){
		this.layerId = layerId;
		this.featureType = featureType;
		this.serverName = serverName;
		this.title = title;
		this.srs = srs;
		this.boundingBox = boundingBox;
		this.serverId = serverId;
		this.serverUrl = serverUrl;
		setChecked(false);
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
	
	public void setChecked(boolean check){
		this.checked = check;
	}
	
	@Override
	public String toString(){
		return "\t featureType: " + featureType + 
				"\n\t serverName: " + serverName +
				"\n\t serverId: " + serverId +
				"\n\t title: " + title + 
				"\n\t srs: " + srs +
				"\n\t boundingBox: " + boundingBox;
	}
	
}
