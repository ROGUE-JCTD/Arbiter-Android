package com.lmn.Arbiter_Android.Loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.BroadcastReceivers.AddLayersBroadcastReceiver;
import com.lmn.Arbiter_Android.Comparators.CompareAddLayersListItems;
import com.lmn.Arbiter_Android.Dialog.Dialogs.AddLayersDialog;

public class AddLayersListLoader extends AsyncTaskLoader<ArrayList<Layer>> {
	public static final String ADD_LAYERS_LIST_UPDATED = "ADD_LAYERS_LIST_UPDATED";
	
	private static final String LAYER_TAG = "Layer";
	private static final String FEATURE_TYPE = "Name";
	private static final String LAYER_TITLE = "Title";
	private static final String LAYER_BOUNDING_BOX = "BoundingBox";
	private static final String LAYER_SRS = "SRS";
	
	private AddLayersBroadcastReceiver loaderBroadcastReceiver = null;
	private AddLayersDialog dialog = null;
	
	private ArrayList<Layer> layers;
	
	public AddLayersListLoader(AddLayersDialog dialog) {
		super(dialog.getActivity().getApplicationContext());
		this.dialog = dialog;
	}

	@Override
	public ArrayList<Layer> loadInBackground() {
		ArrayList<Layer> _layers = null;
		
		try {
			_layers = getLayers(getSelectedServer());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (XmlPullParserException e){
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
		return _layers;
	}
	
	// TODO: CAN AVOID ITERATING THOUGH THE LIST OF PULLED IN LAYERS AGAIN
	// BY CHECKING THE HASHMAP BEFORE THE LAYER EVEN GETS ADDED TO THE LIST
	// INSIDE OF THE PARSEGETCAPABILITIES METHOD
	/**
	 * Remove layers that are already in the project
	 * @param pulledLayers
	 * @param myLayers
	 */
	public void removeDuplicates(List<Layer> pulledLayers, Layer[] projectLayers){
		if(projectLayers != null){
			// key: server_id:featuretype
			// value: Boolean
			HashMap<String, Boolean> layersInProject = new HashMap<String, Boolean>();
			String key = null;
			Layer currentLayer = null;
			int i;
			
			// Add all of the layers in the project already to the hashmap
			for(i = 0; i < projectLayers.length; i++){
				currentLayer = projectLayers[i];
				
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
	
	/**
	 * Get the selected server from the dropdown
	 * @return The selected server
	 */
	public Server getSelectedServer(){
		int selectedIndex = dialog.getSpinner().getSelectedItemPosition();
		
		if(selectedIndex > -1)
			return dialog.getAdapter().getItem(selectedIndex);
		else
			return null;
	}
	
	/**
	 * Send the getCapabilities request and get the layers back
	 * @param server
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Layer> getLayers(Server server) throws Exception {
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
				
			return parseGetCapabilities(server, reader);
		}
		
		return null;
	}
	
	/**
	 * Parse the getCapabilities request
	 * @param server Object with the current servers info
	 * @param reader The reader for reading in the request response
	 * @return An ArrayList of parsed Layer objects containing the layers info
	 * @throws XmlPullParserException
	 * @throws IOException
	 */
	private ArrayList<Layer> parseGetCapabilities(Server server, BufferedReader reader) throws XmlPullParserException, IOException{
		XmlPullParserFactory factory;
		factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(false);
		
		XmlPullParser pullParser = factory.newPullParser();
		pullParser.setInput(reader);
		
		String featureType = null;
		String title = null;
		String srs = null;
		
		String boundingBox = null;
		String minx = null; 
		String miny = null;
		String maxx = null;
		String maxy = null;
		
		String eventName;
		
		// For checking to see if the parser is in a layer element because
		// if not, we don't care about any of the data.
		boolean inLayerTag = false;
		
		List<Layer> layers = new ArrayList<Layer>();
		// TODO There was a bug here where the style 
		int eventType = pullParser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT){
			eventName = pullParser.getName();
			
			if(inLayerTag){
				if(eventType == XmlPullParser.START_TAG){
					
					if(eventName.equalsIgnoreCase(AddLayersListLoader.FEATURE_TYPE)){
						if(pullParser.next() == XmlPullParser.TEXT){
							if(featureType == null){
								featureType = pullParser.getText();
							}
						}
					} else if(eventName.equalsIgnoreCase(AddLayersListLoader.LAYER_TITLE)){
						if(pullParser.next() == XmlPullParser.TEXT){
							if(title == null){
								title = pullParser.getText();
							}
						}
					} else if(eventName.equalsIgnoreCase(AddLayersListLoader.LAYER_SRS)){
						if(pullParser.next() == XmlPullParser.TEXT){
							if(srs == null){
								srs = pullParser.getText();
							}
						}
					} else if(eventName.equalsIgnoreCase(AddLayersListLoader.LAYER_BOUNDING_BOX)){
						minx = pullParser.getAttributeValue(null, "minx");
						miny = pullParser.getAttributeValue(null, "miny");
						maxx = pullParser.getAttributeValue(null, "maxx");
						maxy = pullParser.getAttributeValue(null, "maxy");
						
						if(boundingBox == null){
							boundingBox = minx + ", " + miny + ", " + maxx + ", " + maxy;
						}
					}
				}else if(eventType == XmlPullParser.END_TAG){
					if(eventName.equalsIgnoreCase(AddLayersListLoader.LAYER_TAG)){
						// Create the new layer object, and since this was the end of 
						// the layer element, specify that we're out.
						layers.add(new Layer(-1, featureType, server.getId(), server.getServerName(), 
								server.getUrl(), title, srs, boundingBox));
						inLayerTag = false;
						
						// Reset the fields to prepare for the next layer
						// Needed for the null checks
						featureType = null;
						title = null;
						srs = null;
						boundingBox = null;
					}
				}
			}else{
				if(eventType == XmlPullParser.START_TAG){
					eventName = pullParser.getName();
					
					if(eventName.equalsIgnoreCase(AddLayersListLoader.LAYER_TAG)){
						inLayerTag = true;
					}
				}
			}
			
			eventType = pullParser.next();
		}
		
		removeDuplicates(layers, dialog.getLayersInProject());
		
		Collections.sort(layers, new CompareAddLayersListItems());
		
		return (ArrayList<Layer>) layers;
	}
	
	/**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(ArrayList<Layer> _layers) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (layers != null) {
          //      onReleaseResources(cursor);
            }
        }
        
        ArrayList<Layer> oldLayers = _layers;
        layers = _layers;

        if (isStarted()) {
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(layers);
        }

        // At this point we can release the resources associated with
        // 'oldApps' if needed; now that the new result is delivered we
        // know that it is no longer in use.
        if (oldLayers != null) {
            onReleaseResources(oldLayers);
        }
    }
    
    /**
     * Handles a request to start the Loader.
     */
    @Override protected void onStartLoading() {
        if (layers != null) {
            // If we currently have a result available, deliver it
            // immediately.
            deliverResult(layers);
        }

     // Start watching for changes in the app data.
        if (loaderBroadcastReceiver == null) {
        	loaderBroadcastReceiver = new AddLayersBroadcastReceiver(this);
        	LocalBroadcastManager.getInstance(getContext()).
        		registerReceiver(loaderBroadcastReceiver, new IntentFilter(AddLayersListLoader.ADD_LAYERS_LIST_UPDATED));
        }
        
        if (takeContentChanged() || layers == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    /**
     * Handles a request to stop the Loader.
     */
    @Override protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to cancel a load.
     */
    @Override public void onCanceled(ArrayList<Layer> _layers) {
        super.onCanceled(_layers);

        // At this point we can release the resources associated with 'apps'
        // if needed.
        onReleaseResources(_layers);
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

        // At this point we can release the resources associated with 'apps'
        // if needed.
        if (layers != null) {
            onReleaseResources(layers);
            layers = null;
        }
        
        // Stop monitoring for changes.
        if (loaderBroadcastReceiver != null) {
        	LocalBroadcastManager.getInstance(getContext()).
        		unregisterReceiver(loaderBroadcastReceiver);
            loaderBroadcastReceiver = null;
        }
    }

    /**
     * Helper function to take care of releasing resources associated
     * with an actively loaded data set.
     */
    protected void onReleaseResources(ArrayList<Layer> _layers) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    	
    }
}
