package com.lmn.Arbiter_Android.Layers;

public class LayerListItem {
	private String layerName;
	private String serverName;
	
	public LayerListItem(String layerName, String serverName){
		this.layerName = layerName;
		this.serverName = serverName;
	}
	
	public String getLayerName(){
		return layerName;
	}
	
	public String getServerName(){
		return serverName;
	}
}
