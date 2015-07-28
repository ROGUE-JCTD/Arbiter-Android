package com.lmn.Arbiter_Android.Map.Helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xmlpull.v1.XmlPullParserException;

import android.util.Base64;
import android.util.Log;
import android.app.Activity;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.Comparators.CompareAddLayersListItems;
import com.lmn.Arbiter_Android.Comparators.CompareAddTilesetsListItems;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;
import com.lmn.Arbiter_Android.Map.Helpers.Parsers.ParseGetCapabilities;

public class GetCapabilities {
	private ParseGetCapabilities parser;
	int timeout = 30000;
	int soTimeout = 40000;
	
	public GetCapabilities(){
		parser = ParseGetCapabilities.getParser();
	}
	
	/**
	 * Send the getCapabilities request and get the layers back
	 * @param server
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Layer> getLayers(Server server, ArrayList<Layer> layersInProject) throws IOException{
		if(server != null && server.getUrl() != null){
			String url = server.getUrl() + "?service=wms&version=1.1.1&request=GetCapabilities";
			
			HttpParams params = new BasicHttpParams();
			
			HttpConnectionParams.setConnectionTimeout(params, timeout);
			HttpConnectionParams.setSoTimeout(params, soTimeout);
			
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			
			if(!"".equals(server.getUsername()) && !"".equals(server.getPassword())){
				
				String credentials = server.getUsername() + ":" + server.getPassword();
				credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
				
				request.addHeader("Authorization", "Basic " + credentials);
			}
			
			Log.w("GetCapabilities", "GetCapabilities: " + url);
			HttpResponse response = null;
			
			try {
				response = client.execute(request);
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(response == null){
				return null;
			}
			
			Log.w("ADD_LAYERS_LIST_LOADER", "ADD_LAYERS_LIST_LOADER - Sending GET request to URL: " + url);
			Log.w("ADD_LAYERS_LIST_LOADER", "ADD_LAYERS_LIST_LAODER - Response Code : " + response.getStatusLine().getStatusCode());
			
			BufferedReader reader = null;
			
			try {
				reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				
			List<Layer> layers = null;
			
			try {
				layers = parser.parseGetCapabilities(server, reader);
			} catch (XmlPullParserException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(layers != null){
				removeDuplicates(layers, layersInProject);
				
				Collections.sort(layers, new CompareAddLayersListItems());
			}
			
			return (ArrayList<Layer>) layers;
		}
		
		return null;
	}

	public ArrayList<Tileset> getTilesets(Server server, final Activity activity) {
		if(server != null && server.getUrl() != null) {
			//String url = server.getUrl() + "?service=wms&version=1.1.1&request=GetCapabilities";
			String[] thisUrl = server.getUrl().split("/");
			String url = "http://" + thisUrl[2] + "/api/tileset/";

			HttpParams params = new BasicHttpParams();

			int connectionTimeout = 15000; //15s
			int socketTimeout = 25000; //25s
			HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
			HttpConnectionParams.setSoTimeout(params, socketTimeout);

			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);

			if (!"".equals(server.getUsername()) && !"".equals(server.getPassword())) {

				String credentials = server.getUsername() + ":" + server.getPassword();
				credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);

				request.addHeader("Authorization", "Basic " + credentials);
			}

			Log.w("GetCapabilities", "GetCapabilities: " + url);
			HttpResponse response = null;

			try {
				response = client.execute(request);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				final String serverName = server.getName();
				activity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						TilesetsHelper.getTilesetsHelper().serverResponseDialog(activity, serverName);
					}
				});
			}

			if(response == null){
				return null;
			}

			Log.w("ADD_TILESET_LIST_LOADER", "ADD_TILESETS_LIST_LOADER - Sending GET request to URL: " + url);
			Log.w("ADD_TILESET_LIST_LOADER", "ADD_TILESETS_LIST_LOADER - Response Code: " + response.getStatusLine().getStatusCode());

			BufferedReader reader = null;

			try {
				reader = new BufferedReader(
						new InputStreamReader(response.getEntity().getContent()));
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			List<Tileset> tilesets = null;

			try {
				tilesets = parser.parseGetCapabilitiesTileset(server, reader, activity);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(tilesets != null){
				//TODO: Fix this function
				//removeDuplicateTilesets(tilesets, tilesetsInProject);

				Collections.sort(tilesets, new CompareAddTilesetsListItems());

				if (tilesets.size() <= 0){
					// Nothing was returned
					final String serverName = server.getName();
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							TilesetsHelper.getTilesetsHelper().serverNoTilesetsResponseDialog(activity, serverName);
						}
					});
				}
			}

			return (ArrayList<Tileset>) tilesets;
		}

		return null;
	}

	// TODO: CAN AVOID ITERATING THOUGH THE LIST OF PULLED IN LAYERS AGAIN
	// BY CHECKING THE HASHMAP BEFORE THE LAYER EVEN GETS ADDED TO THE LIST
	// INSIDE OF THE PARSEGETCAPABILITIES METHOD
	/**
	 * Remove layers that are already in the project
	 * @param pulledLayers
	 * @param myLayers
	 */
	private void removeDuplicates(List<Layer> pulledLayers, ArrayList<Layer> projectLayers){
		if(projectLayers != null){
			// key: server_id:featuretype
			// value: Boolean
			HashMap<String, Boolean> layersInProject = new HashMap<String, Boolean>();
			String key = null;
			Layer currentLayer = null;
			int i;
			
			// Add all of the layers in the project already to the hashmap
			for(i = 0; i < projectLayers.size(); i++){
				currentLayer = projectLayers.get(i);
				
				key = Layer.buildLayerKey(currentLayer);
				
				if(!layersInProject.containsKey(key)){
					layersInProject.put(key, true);
				}
			}
			
			// If the layer is already in the project, remove it
			for(i = pulledLayers.size() - 1; i >= 0; i--){
				currentLayer = pulledLayers.get(i);
				
				key = Layer.buildLayerKey(currentLayer);
				
				if(layersInProject.containsKey(key)){
					pulledLayers.remove(i);
				}
			}
		}
	}

	private void removeDuplicateTilesets(List<Tileset> pulledTilesets, ArrayList<Tileset> projectTilesets){
		if(projectTilesets != null){
			// key: server_id:featuretype
			// value: Boolean
			HashMap<String, Boolean> tilesetsInProject = new HashMap<String, Boolean>();
			String key = null;
			Tileset currentTileset = null;
			int i;

			// Add all of the layers in the project already to the hashmap
			for(i = 0; i < projectTilesets.size(); i++){
				currentTileset = projectTilesets.get(i);

				key = Tileset.buildTilesetKey(currentTileset);

				if(!tilesetsInProject.containsKey(key)){
					tilesetsInProject.put(key, true);
				}
			}

			// If the layer is already in the project, remove it
			for(i = pulledTilesets.size() - 1; i >= 0; i--){
				currentTileset = pulledTilesets.get(i);

				key = Tileset.buildTilesetKey(currentTileset);

				if(tilesetsInProject.containsKey(key)){
					pulledTilesets.remove(i);
				}
			}
		}
	}
}
