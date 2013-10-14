package com.lmn.Arbiter_Android.ListItems;

public class AddLayersListItem {
	private String layerName;
	private String serverName;
	private boolean checked;
	
	public AddLayersListItem(String layerName, String serverName){
		setLayerName(layerName);
		setServerName(serverName);
		setChecked(false);
	}
	
	public String getLayerName(){
		return layerName;
	}
	
	public void setLayerName(String layerName){
		this.layerName = layerName;
	}
	
	public String getServerName(){
		return serverName;
	}
	
	public void setServerName(String serverName){
		this.serverName = serverName;
	}
	
	public boolean isChecked(){
		return this.checked;
	}
	
	public void setChecked(boolean check){
		this.checked = check;
	}
}
