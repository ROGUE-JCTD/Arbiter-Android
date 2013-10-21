package com.lmn.Arbiter_Android.ListItems;

public class AddLayersListItem {
	private String layerName;
	private String serverName;
	private String title;
	private String srs;
	private String boundingBox;
	
	private boolean checked;
	
	public AddLayersListItem(String layerName, String serverName, 
			String title, String srs, String boundingBox){
		this.layerName = layerName;
		this.serverName = serverName;
		this.title = title;
		this.srs = srs;
		this.boundingBox = boundingBox;
		
		setChecked(false);
	}
	
	public String getLayerName(){
		return layerName;
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
	
	public void setChecked(boolean check){
		this.checked = check;
	}
	
	@Override
	public String toString(){
		return "\t layerName: " + layerName + 
				"\n\t serverName: " + serverName +
				"\n\t title: " + title + 
				"\n\t srs: " + srs +
				"\n\t boundingBox: " + boundingBox;
	}
	
}
