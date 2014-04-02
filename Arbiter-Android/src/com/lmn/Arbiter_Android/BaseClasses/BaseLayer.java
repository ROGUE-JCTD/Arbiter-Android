package com.lmn.Arbiter_Android.BaseClasses;

import org.json.JSONException;
import org.json.JSONObject;

public class BaseLayer {
	
	public static final String NAME = "name";
	public static final String URL = "url";
	public static final String SERVER_NAME = "serverName";
	public static final String SERVER_ID = "serverId";
	public static final String FEATURE_TYPE = "featureType";
	
	private String name;
	private String url;
	private String serverName;
	private String serverId;
	private String featureType;
	
	public static BaseLayer createOSMBaseLayer(){
		String osm = "OpenStreetMap";
		
		return new BaseLayer(osm, null, osm, osm, "");
	}
	
	public BaseLayer(String name, String url, String serverName, String serverId, String featureType){
		this.name = name;
		this.url = url;
		this.serverName = serverName;
		this.serverId = serverId;
		this.featureType = featureType;
	}
	
	public BaseLayer(JSONObject obj){
		
		try{
			this.name = obj.getString(NAME);
			this.url = obj.getString(URL);
			this.serverId = obj.getString(SERVER_ID);
			this.serverName = obj.getString(SERVER_NAME);
			this.featureType = obj.getString(FEATURE_TYPE);
		}catch(JSONException e){
			e.printStackTrace();
		}
	}
	
	public String getName(){
		return name;
	}
	
	public String getUrl(){
		return url;
	}
	
	public String getServerName(){
		return serverName;
	}
	
	public String getServerId(){
		return serverId;
	}
	
	public String getFeatureType(){
		return featureType;
	}
	
	public JSONObject getJSON() throws JSONException{
		String json = "{'" + NAME + "': '" + name + "'," 
			+ "'" + URL + "': '" + url + "',"
			+ "'" + SERVER_ID + "': '" + serverId + "',"
			+ "'" + SERVER_NAME + "': '" + serverName + "',"
			+ "'" + FEATURE_TYPE + "': '" + featureType + "'}";
		
		return new JSONObject(json);
	}
}