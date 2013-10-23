package com.lmn.Arbiter_Android.ListItems;

public class Layer {
	private String featureType;
	
	// Only for the adapter, for displaying
	private String serverName;
	
	private String title;
	private String srs;
	private String boundingBox;
	private int serverId;
	private boolean checked;
	
	public Layer(String featureType, int serverId, String serverName, 
			String title, String srs, String boundingBox){
		this.featureType = featureType;
		this.serverName = serverName;
		this.title = title;
		this.srs = srs;
		this.boundingBox = boundingBox;
		this.serverId = serverId;
		setChecked(false);
	}
	
	// For cloning
	public Layer(Layer item){
		this.featureType = item.getFeatureType();
		this.serverName = item.getServerName();
		this.title = item.getLayerTitle();
		this.srs = item.getLayerSRS();
		this.boundingBox = item.getLayerBBOX();
		this.checked = item.isChecked();
		this.serverId = item.getServerId();
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
