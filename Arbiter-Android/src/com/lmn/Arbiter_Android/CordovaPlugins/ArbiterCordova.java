package com.lmn.Arbiter_Android.CordovaPlugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.MapChangeHelper;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.CordovaPlugins.Helpers.FeatureHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ArbiterCordova extends CordovaPlugin{
	private static final String TAG = "ArbiterCordova";
	private final ArbiterProject arbiterProject;
	public static final String mainUrl = "file:///android_asset/www/main.html";
	public static final String aoiUrl = "file:///android_asset/www/aoi.html";
	//public static final String mainUrl = "content://jsHybugger.org/file:///android_asset/www/main.html";
	//public static final String aoiUrl = "content://jsHybugger.org/file:///android_asset/www/aoi.html";
	
	public ArbiterCordova(){
		super();
		this.arbiterProject = ArbiterProject.getArbiterProject();
	}
	
	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		
		if("setProjectsAOI".equals(action)){
			
			String aoi = args.getString(0);
			
			setProjectsAOI(aoi, callbackContext);
			
			return true;
		}else if("resetWebApp".equals(action)){
			String extent = args.getString(0);
            String zoomLevel = args.getString(1);
            
			resetWebApp(extent, zoomLevel, callbackContext);
			
			return true;
		}else if("setNewProjectsAOI".equals(action)){
			String aoi = args.getString(0);
			
			setNewProjectsAOI(aoi, callbackContext);
			
			return true;
		}else if("doneCreatingProject".equals(action)){
			
			doneCreatingProject();
			
			return true;
		}else if("errorCreatingProject".equals(action)){
			errorCreatingProject();
			
			return true;
		}else if("errorLoadingFeatures".equals(action)){
			errorLoadingFeatures();
			
			return true;
		}else if("doneAddingLayers".equals(action)){
			doneAddingLayers();
			
			return true;
		}else if("errorAddingLayers".equals(action)){
			String error = args.getString(0);
			
			errorAddingLayers(error);
			
			return true;
		}else if("featureSelected".equals(action)){
			String featureType = args.getString(0);
			String id = args.getString(1);
			
			featureSelected(featureType, id);
			
			return true;
		}else if("updatedGeometry".equals(action)){
			String updatedGeometry = args.getString(0);
			
			updateGeometry(updatedGeometry);
		}else if("doneInsertingFeature".equals(action)){
			String featureType = args.getString(0);
			String wktGeometry = args.getString(1);
			
			doneInsertingFeature(featureType, wktGeometry);
		}else if("syncCompleted".equals(action)){
			syncCompleted();
			
			return true;
		}else if("syncFailed".equals(action)){
			syncFailed((args.length() > 0) 
					? args.getString(0) : null);
			
			return true;
		}
		
		// Returning false results in a "MethodNotFound" error.
		return false;
	}
	
	private void syncCompleted(){
		arbiterProject.dismissSyncProgressDialog();
	}
	
	private void syncFailed(String error){
		arbiterProject.dismissSyncProgressDialog();
		
		showDialog(R.string.error_syncing, 
				R.string.error_syncing_msg, error);
	}
	
	private SQLiteDatabase getFeatureDatabase(){
		Context context = cordova.getActivity().getApplicationContext();
		
		String openProjectName = ArbiterProject.
				getArbiterProject().getOpenProject(cordova.getActivity());
		
		return FeatureDatabaseHelper.getHelper(context,
				ProjectStructure.getProjectPath(context, 
						openProjectName), false).getWritableDatabase();
	}
	
	private Feature getNewFeature(String featureType, String wktGeometry){
		return FeaturesHelper.getHelper().getNewFeature(
				getFeatureDatabase(), featureType, wktGeometry);
	}
	
	private void doneInsertingFeature(String featureType, String wktGeometry){
		Feature feature = getNewFeature(featureType, wktGeometry);
		
		ArbiterState.getArbiterState().editingFeature(feature);
		
		try{
			((Map.MapChangeListener) cordova.getActivity())
				.getMapChangeHelper().doneInsertingFeature();
		} catch(ClassCastException e){
			e.printStackTrace();
			throw new ClassCastException(cordova.getActivity().toString() 
					+ " must be an instance of Map.MapChangeListener");
		}
	}
	
	/**
	 * Cast the activity to a FragmentActivity
	 * @return
	 */
	private FragmentActivity getFragmentActivity(){
		FragmentActivity activity;
		
		try {
			activity = (FragmentActivity) cordova.getActivity();
		} catch (ClassCastException e){
			e.printStackTrace();
			throw new ClassCastException(cordova.getActivity().toString() 
					+ " must be an instance of FragmentActivity");
		}
		
		return activity;
	}
	
	private void featureSelected(String featureType, String id){
		FeatureHelper helper = new FeatureHelper(getFragmentActivity());
		helper.displayFeatureDialog(featureType, id);
	}
	
	/**
	 * Update the geometry of the feature being edited.
	 * @param updatedGeometry
	 */
	private void updateFeaturesGeometry(String updatedGeometry){
		Feature feature = ArbiterState
				.getArbiterState().isEditingFeature();
		
		ContentValues attributes = feature.getAttributes();
		
		attributes.put(feature.getGeometryName(), updatedGeometry);
	}
	
	/**
	 * Notify the MapListener that that the feature is done,
	 * being edited.
	 */
	private void notifyDoneEditingFeature(){
		try{
			((Map.MapChangeListener) cordova.getActivity())
				.getMapChangeHelper().doneEditingFeature();
		} catch(ClassCastException e){
			e.printStackTrace();
			throw new ClassCastException(cordova.getActivity().toString() 
					+ " must be an instance of Map.MapChangeListener");
		}
	}
	
	private void updateGeometry(String updatedGeometry){
		updateFeaturesGeometry(updatedGeometry);
		
		FeatureHelper helper = new FeatureHelper(getFragmentActivity());
		helper.displayUpdatedFeature();
		
		notifyDoneEditingFeature();
	}
	
	private void doneAddingLayers(){
		arbiterProject.doneAddingLayers(cordova.getActivity().getApplicationContext());
	}
	
	private void errorAddingLayers(String error){
		doneAddingLayers();
		
		showDialog(R.string.error_adding_layers, 
				R.string.error_adding_layers_msg, error);
	}
	
	private void errorLoadingFeatures(){
		showDialog(R.string.error_loading_features, 
				R.string.error_loading_features_msg, null);
	}
	
	private void showDialog(int title, int message, String optionalMessage){
		final Activity activity = cordova.getActivity();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		String msg = activity.getResources().getString(message);
		
		if(optionalMessage != null){
			msg += ": \n\t" + optionalMessage;
		}
		
		builder.setTitle(title);
		builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
		builder.setMessage(msg);
		
		builder.create().show();
	}
	
	private void errorCreatingProject(){
		Log.w("ArbiterCordova", "ArbiterCordova.errorCreatingProject");
		ArbiterProject.getArbiterProject().errorCreatingProject(
				cordova.getActivity());
	}
	
	private void doneCreatingProject(){
		ArbiterProject.getArbiterProject().doneCreatingProject(
				cordova.getActivity().getApplicationContext());
	}
	
	private void setNewProjectsAOI(final String aoi, final CallbackContext callbackContext){
		//ArbiterState.getState().setNewAOI(aoi);
		ArbiterProject.getArbiterProject().getNewProject().setAOI(aoi);
		
		callbackContext.success();
		
		cordova.getActivity().finish();
	}
	
	/**
	 * Set the ArbiterProject Singleton's newProject aoi, commit the project, and return to the map
	 */
	private void setProjectsAOI(final String aoi, final CallbackContext callbackContext){
		
		ArbiterState.getArbiterState().setNewAOI(aoi);
		
		callbackContext.success();
		
		cordova.getActivity().finish();
	} 
	
	private void resetWebApp(final String currentExtent, final String zoomLevel, final CallbackContext callbackContext){
		final Activity activity = this.cordova.getActivity();
		final CordovaWebView webview = this.webView;
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				arbiterProject.setSavedZoomLevel(zoomLevel);
                arbiterProject.setSavedBounds(currentExtent);
                
				webview.loadUrl("about:blank");
			}
		});
	}
}
