package com.lmn.Arbiter_Android.BaseClasses;

public class Tileset {
	public static final String DEFAULT_TILESET_NAME = "UndefinedTileset";

	public static String buildTilesetKey(Tileset tileset){
		return Integer.valueOf(tileset.getTilesetName() + tileset.getServerURL()).toString();
	}

	private String tilesetName;
	private String createdAtTime;
	private String createdBy;
	private double filesize;
	private String geom;
	private String layerName;
	private int layerZoomStart;
	private int layerZoomStop;
	private String resourceURI;
	private String serverServiceType;
	private String downloadURL;

	private int serverID;
	private String serverURL;
	private String serverUsername;

	private String fileLocation;

	// Private for downloading
	private boolean isDownloading;
	private int downloadProgress;

	public Tileset(){
		this.tilesetName = null;
		this.createdAtTime = null;
		this.geom = null;
		this.layerName = null;
		this.layerZoomStart = -1;
		this.layerZoomStop = -1;
		this.resourceURI = null;
		this.serverServiceType = null;
		this.downloadURL = null;

		this.createdBy = null;
		this.filesize = -1;

		this.serverID = -1;
		this.serverURL = null;
		this.serverUsername = null;

		this.fileLocation = null;

		this.isDownloading = false;
		this.downloadProgress = 0;
	}

	public Tileset(String tilesetName, String created_at, String created_by, double filesize, String geom,
				   String layerName, int layerZoomStart, int layerZoomStop, String resourceURI,
				   String serverServiceType, String downloadURL, int serverID, String serverURL, String serverUsername,
				   String fileLocation){
		this.tilesetName = tilesetName;
		this.createdAtTime = created_at;
		this.createdBy = created_by;
		this.filesize = filesize;
		this.geom = geom;
		this.layerName = layerName;
		this.layerZoomStart = layerZoomStart;
		this.layerZoomStop = layerZoomStop;
		this.resourceURI = resourceURI;
		this.serverServiceType = serverServiceType;
		this.downloadURL = downloadURL;

		this.serverID = serverID;
		this.serverUsername = serverUsername;
		this.serverURL = serverURL;

		this.fileLocation = fileLocation;

		// Will be defaulted to false, if it needs to be downloaded, it will be after creation.
		this.isDownloading = false;
	}

	public Tileset(Tileset item)
	{
		this.tilesetName = item.getTilesetName();
		this.createdAtTime = item.getCreatedTime();
		this.createdBy = item.getCreatedBy();
		this.filesize = item.getFilesize();
		this.geom = item.getGeom();
		this.layerName = item.getLayerName();
		this.layerZoomStart = item.getLayerZoomStart();
		this.layerZoomStop = item.getLayerZoomStop();
		this.resourceURI = item.getResourceURI();
		this.serverServiceType = item.getServerServiceType();
		this.downloadURL = item.getDownloadURL();

		this.serverID = item.getServerID();
		this.serverUsername = item.getServerUsername();
		this.serverURL = item.getServerURL();

		this.fileLocation = item.getFileLocation();

		this.isDownloading = item.getIsDownloading();
		this.downloadProgress = item.getDownloadProgress();
	}

	
	public String getTilesetName(){
		return tilesetName;
	}
	public void setTilesetName(String name){
		this.tilesetName = name;
	}

	public String getCreatedTime(){
		return createdAtTime;
	}
	public void setCreatedTime(String time){
		this.createdAtTime = time;
	}

	public String getCreatedBy(){
		return createdBy;
	}
	public void setCreatedBy(String createdby){
		this.createdBy = createdby;
	}

	
	public double getFilesize(){
		return filesize;
	}
	public String getFilesizeAfterConversion(){
		// Will convert from bytes to bytes, KB, MB, or GB
		String result = "";

		if (filesize > 0.0) {
			if (filesize > 1073741824.0) {
				String num = String.format("%.2f", (filesize / 1073741824.0));
				result += num + "GB";
			} else if (filesize > 1048576.0) {
				String num = String.format("%.2f", (filesize / 1048576.0));
				result += num + "MB";
			} else if (filesize > 1024.0) {
				String num = String.format("%.2f", (filesize / 1024.0));
				result += num + "KB";
			} else {
				result += filesize + " bytes";
			}
		} else {
			if (filesize < -1073741824.0) {
				String num = String.format("%.2f", (filesize / 1073741824.0));
				result += num + "GB";
			} else if (filesize < -1048576.0) {
				String num = String.format("%.2f", (filesize / 1048576.0));
				result += num + "MB";
			} else if (filesize < -1024.0) {
				String num = String.format("%.2f", (filesize / 1024.0));
				result += num + "KB";
			} else {
				result += filesize + " bytes";
			}
		}

		return result;
	}
	public void setFilesize(double size){ this.filesize = size; }

	public String getGeom() { return geom; }
	public void setGeom(String bounds){ this.geom = bounds; }

	public String getLayerName(){return layerName;}
	public void setLayerName(String name){this.layerName = name;}

	public int getLayerZoomStart() { return layerZoomStart; }
	public void setLayerZoomStart(int start) { this.layerZoomStart = start; }

	public int getLayerZoomStop() { return layerZoomStop; }
	public void setLayerZoomStop(int stop) { this.layerZoomStop = stop; }

	public String getResourceURI() { return resourceURI; }
	public void setResourceURI(String uri) { this.resourceURI = uri; }

	public String getServerServiceType() { return serverServiceType; }
	public void setServerServiceType(String sst) { this.serverServiceType = sst; }

	public String getDownloadURL() { return downloadURL; }
	public void setDownloadURL(String url) { this.downloadURL = url; }

	// Server stuff
	public int getServerID() { return serverID; }
	public void setServerID(int id) { this.serverID = id; }

	public String getServerUsername() { return serverUsername; }
	public void setServerUsername(String name) { this.serverUsername = name; }

	public String getServerURL() { return serverURL; }
	public void setServerURL(String url) { this.serverURL = url; }

	public String getFileLocation() { return fileLocation; }
	public void setFileLocation(String loc) { this.fileLocation = loc; }

	// Private stuff
	public boolean getIsDownloading() { return isDownloading; }
	public void setIsDownloading(boolean d) { this.isDownloading = d; }

	public int getDownloadProgress() { return downloadProgress; }
	public void setDownloadProgress(int p) { this.downloadProgress = p; }

	public BaseLayer toBaseLayer(){

		// Extra datamembers
		String name = this.getTilesetName();
		String url = this.getFileLocation(); // "file://TileSets/osm.mbtiles";
		String serverId = this.getLayerName();
		String serverName = "OpenStreetMap";
		String featuretype = "";

		BaseLayer layer = new BaseLayer(name, url, serverName, serverId, featuretype);

		return layer;
	}
	
	@Override
	public String toString(){
		return "{" +
				"\ttilesetName: " + tilesetName + "\n" +
				"\tcreatedAtTime: " + createdAtTime + "\n" +
				"\tcreatedBy: " + createdBy + "\n" +
				"\tfilesize: " + filesize + "\n" +
				"\tgeom: " + geom + "\n" +
				"\tlayerName: " + layerName + "\n" +
				"\tlayerZoomStart: " + layerZoomStart + "\n" +
				"\tlayerZoomStop: " + layerZoomStop + "\n" +
				"\tresourceURI: " + resourceURI + "\n" +
				"\tserverServiceType: " + serverServiceType + "\n" +
				"\tdownloadURL: " + downloadURL + "\n" +
				"\tserverID: " + serverID + "\n" +
				"\tserverURL: " + serverURL + "\n" +
				"\tserverUsername: " + serverUsername + "\n" +
				"\tisDownloading: " + isDownloading + "\n" +
				"\tdownloadProgress: " + downloadProgress + "\n" +
				"}";
	}
}
