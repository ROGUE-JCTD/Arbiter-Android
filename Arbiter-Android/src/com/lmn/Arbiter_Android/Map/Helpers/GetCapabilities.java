package com.lmn.Arbiter_Android.Map.Helpers;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.util.Base64;
import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.Comparators.CompareAddLayersListItems;
import com.lmn.Arbiter_Android.Map.Helpers.Parsers.ParseGetCapabilities;

public class GetCapabilities {
	private ParseGetCapabilities parser;
	
	public GetCapabilities(){
		parser = ParseGetCapabilities.getParser();
	}
	
	/**
	 * Send the getCapabilities request and get the layers back
	 * @param server
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Layer> getLayers(Server server, ArrayList<Layer> layersInProject) throws Exception {
		if(server != null){
			String url = server.getUrl() + "/wms?service=wms&version=1.1.1&request=getCapabilities";
			
			HttpClient client = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			
			String credentials = server.getUsername() + ":" + server.getPassword();
			credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
			
			request.addHeader("Authorization", "Basic " + credentials);
			
			HttpResponse response = client.execute(request);
			
			Log.w("ADD_LAYERS_LIST_LOADER", "ADD_LAYERS_LIST_LOADER - Sending GET request to URL: " + url);
			Log.w("ADD_LAYERS_LIST_LOADER", "ADD_LAYERS_LIST_LAODER - Response Code : " + response.getStatusLine().getStatusCode());
			
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent()));
			
			/*BufferedReader reader = new BufferedReader(
					new InputStreamReader(this.dialog.getActivity().
							getApplicationContext().getResources().getAssets().open("getcapabilities.xml")));*/
				
			List<Layer> layers = parser.parseGetCapabilities(server, reader);
			
			removeDuplicates(layers, layersInProject);
			
			Collections.sort(layers, new CompareAddLayersListItems());
			
			return (ArrayList<Layer>) layers;
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
	public void removeDuplicates(List<Layer> pulledLayers, ArrayList<Layer> projectLayers){
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
				
				key = buildLayerKey(currentLayer);
				
				if(!layersInProject.containsKey(key)){
					layersInProject.put(key, true);
				}
			}
			
			// If the layer is already in the project, remove it
			for(i = 0; i < pulledLayers.size(); i++){
				currentLayer = pulledLayers.get(i);
				
				key = buildLayerKey(currentLayer);
				
				if(layersInProject.containsKey(key)){
					pulledLayers.remove(i);
				}
			}
		}
	}
	
	/**
	 * Create a key for the removeDuplicates method
	 * @param layer Layer to create the key with
	 * @return
	 */
	private String buildLayerKey(Layer layer){
		return Integer.valueOf(layer.getServerId()).toString() + ":" +
				layer.getFeatureType();
	}
}
