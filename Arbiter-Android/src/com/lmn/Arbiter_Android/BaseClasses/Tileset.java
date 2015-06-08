package com.lmn.Arbiter_Android.BaseClasses;

import java.util.ArrayList;

public class Tileset {
	public static final String DEFAULT_TILESET_NAME = "UndefinedTileset";

	public static String buildTilesetKey(Tileset tileset){
		return Integer.valueOf(tileset.getName() + tileset.getSourceId()).toString();
	}

	//private int fingerprint;
	private String tilesetName;
	private int created_at_time;
	private String created_by;
	private int filesize;
	private String source_id;

	// TODO: Add polygon data. ArrayList of ints?
	// That doesn't take care of MultiPolygon bounds. ^
	//private ArrayList<int> bounds;

	private boolean checked;

	// Server stuff
	private String serverName;
	private String serverUrl;
	private int serverId;

	public Tileset(){
		this.tilesetName = null;
		this.created_at_time = -1;
		this.created_by = null;
		this.filesize = -1;
		this.source_id = null;
		//this.bounds = null;

		this.checked = false;

		this.serverId = -1;
		this.serverName = null;
		this.serverUrl = null;
	}

	public Tileset(String name, int created_at, String created_by,
				   int filesize, String source_id /*,ArrayList<int> bounds*/){
		this.tilesetName = name;
		this.created_at_time = created_at;
		this.created_by = created_by;
		this.filesize = filesize;
		this.source_id = source_id;
		//this.bounds = bounds;

		// This will be setup later
		this.serverId = -1;
		this.serverName = null;
		this.serverUrl = null;
	}

	public Tileset(Tileset item)
	{
		this.tilesetName = item.getName();
		this.created_at_time = item.getCreatedTime();
		this.created_by = item.getCreatedBy();
		this.filesize = item.getFilesize();
		this.source_id = item.getSourceId();
		//this.bounds = item.getBounds();

		this.serverId = item.getServerId();
		this.serverName = item.getServerName();
		this.serverUrl = item.getServerUrl();
	}

	public boolean isChecked() { return checked; }
	public void setChecked(boolean check) { this.checked = check; }
	
	public String getName(){
		return tilesetName;
	}
	
	public void setName(String name){
		this.tilesetName = name;
	}

	public int getCreatedTime(){
		return created_at_time;
	}

	public void setCreatedTime(int time){
		this.created_at_time = time;
	}

	public String getCreatedBy(){
		return created_by;
	}
	
	public void setCreatedBy(String createdby){
		this.created_by = createdby;
	}

	
	public int getFilesize(){
		return filesize;
	}

	public void setFilesize(int size){
		this.filesize = size;
	}
	
	public String getSourceId(){
		return source_id;
	}
	
	public void setSourceId(String id){
		this.source_id = id;
	}

	/*public ArrayList<int> getBounds() {
		return bounds;
	}

	public void setBounds(ArrayList<int> bounds){
		this.bounds = bounds;
	}*/

	public String getServerName() { return serverName; }
	public void setServerName(String name) { this.serverName = name; }

	public String getServerUrl() { return serverUrl; }
	public void setServerUrl(String url) { this.serverUrl = url; }

	public int getServerId() { return serverId; }
	public void setServerId(int id) { this.serverId = id; }
	
	@Override
	public String toString(){
		return "{" +
				"\ttilesetName: " + tilesetName + "\n" +
				"\tcreated_at_time: " + created_at_time + "\n" +
				"\tcreated_by: " + created_by + "\n" +
				"\tfilesize: " + filesize + "\n" +
				"\tsource_id: " + source_id + "\n" +
				//"\bounds: " + bounds + "\n" +
				"}";
	}
}
