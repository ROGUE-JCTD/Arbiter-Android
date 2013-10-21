package com.lmn.Arbiter_Android.Loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.lmn.Arbiter_Android.BroadcastReceivers.AddLayersBroadcastReceiver;
import com.lmn.Arbiter_Android.Comparators.CompareAddLayersListItems;
import com.lmn.Arbiter_Android.Dialog.Dialogs.AddLayersDialog;
import com.lmn.Arbiter_Android.ListItems.AddLayersListItem;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;
import com.lmn.Arbiter_Android.R;

public class AddLayersListLoader extends AsyncTaskLoader<ArrayList<AddLayersListItem>> {
	public static final String ADD_LAYERS_LIST_UPDATED = "ADD_LAYERS_LIST_UPDATED";
	private static final String TEST_CREDENTIALS = "a3p1c3k6enVzeTE="; 
	
	private static final String LAYER_TAG = "Layer";
	private static final String LAYER_NAME = "Name";
	private static final String LAYER_TITLE = "Title";
	private static final String LAYER_BOUNDING_BOX = "BoundingBox";
	private static final String LAYER_SRS = "SRS";
	
	private AddLayersBroadcastReceiver loaderBroadcastReceiver = null;
	private AddLayersDialog dialog = null;
	
	private ArrayList<AddLayersListItem> layers;
	
	public AddLayersListLoader(AddLayersDialog dialog) {
		super(dialog.getActivity().getApplicationContext());
		this.dialog = dialog;
	}

	@Override
	public ArrayList<AddLayersListItem> loadInBackground() {
		ArrayList<AddLayersListItem> _layers = null;
		
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
	
	public ServerListItem getSelectedServer(){
		int selectedIndex = dialog.getSpinner().getSelectedItemPosition();
		return dialog.getAdapter().getItem(selectedIndex);
	}
	
	public ArrayList<AddLayersListItem> getLayers(ServerListItem server) throws Exception {
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
			
			return parseGetCapabilities(reader);
		}
		
		return null;
	}
	
	private ArrayList<AddLayersListItem> parseGetCapabilities(BufferedReader reader) throws XmlPullParserException, IOException{
		XmlPullParserFactory factory;
		factory = XmlPullParserFactory.newInstance();
		factory.setNamespaceAware(false);
		
		XmlPullParser pullParser = factory.newPullParser();
		pullParser.setInput(reader);
		
		String name = null;
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
		
		List<AddLayersListItem> layers = new ArrayList<AddLayersListItem>();
		
		int eventType = pullParser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT){
			eventName = pullParser.getName();
			
			if(inLayerTag){
				if(eventType == XmlPullParser.START_TAG){
					
					if(eventName.equalsIgnoreCase(AddLayersListLoader.LAYER_NAME)){
						if(pullParser.next() == XmlPullParser.TEXT){
							name = pullParser.getText();
						}
					} else if(eventName.equalsIgnoreCase(AddLayersListLoader.LAYER_TITLE)){
						if(pullParser.next() == XmlPullParser.TEXT){
							title = pullParser.getText();
						}
					} else if(eventName.equalsIgnoreCase(AddLayersListLoader.LAYER_SRS)){
						if(pullParser.next() == XmlPullParser.TEXT){
							srs = pullParser.getText();
						}
					} else if(eventName.equalsIgnoreCase(AddLayersListLoader.LAYER_BOUNDING_BOX)){
						minx = pullParser.getAttributeValue(null, "minx");
						miny = pullParser.getAttributeValue(null, "miny");
						maxx = pullParser.getAttributeValue(null, "maxx");
						maxy = pullParser.getAttributeValue(null, "maxy");
						
						boundingBox = minx + ", " + miny + ", " + maxx + ", " + maxy;
					}
				}else if(eventType == XmlPullParser.END_TAG){
					if(eventName.equalsIgnoreCase(AddLayersListLoader.LAYER_TAG)){
						// Create the new layer object, and since this was the end of 
						// the layer element, specify that we're out.
						layers.add(new AddLayersListItem(name, "someServer", title,
																srs, boundingBox));
						inLayerTag = false;
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
		
		Collections.sort(layers, new CompareAddLayersListItems());
		
		return (ArrayList<AddLayersListItem>) layers;
	}
	
	/**
     * Called when there is new data to deliver to the client.  The
     * super class will take care of delivering it; the implementation
     * here just adds a little more logic.
     */
    @Override public void deliverResult(ArrayList<AddLayersListItem> _layers) {
        if (isReset()) {
            // An async query came in while the loader is stopped.  We
            // don't need the result.
            if (layers != null) {
          //      onReleaseResources(cursor);
            }
        }
        
        ArrayList<AddLayersListItem> oldLayers = _layers;
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
    @Override public void onCanceled(ArrayList<AddLayersListItem> _layers) {
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
    protected void onReleaseResources(ArrayList<AddLayersListItem> _layers) {
        // For a simple List<> there is nothing to do.  For something
        // like a Cursor, we would close it here.
    	
    }
}
