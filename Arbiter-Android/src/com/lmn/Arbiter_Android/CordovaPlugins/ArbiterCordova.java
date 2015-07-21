package com.lmn.Arbiter_Android.CordovaPlugins;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.InsertProjectHelper;
import com.lmn.Arbiter_Android.OOMWorkaround;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.Activities.MapChangeHelper;
import com.lmn.Arbiter_Android.Activities.ProjectsActivity;
import com.lmn.Arbiter_Android.Activities.TileConfirmation;
import com.lmn.Arbiter_Android.AppFinishedLoading.AppFinishedLoading;
import com.lmn.Arbiter_Android.AppFinishedLoading.AppFinishedLoadingJob;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.ConnectivityListeners.ConnectivityListener;
import com.lmn.Arbiter_Android.ConnectivityListeners.HasConnectivityListener;
import com.lmn.Arbiter_Android.CookieManager.ArbiterCookieManager;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ControlPanelHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.GeometryColumnsHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FailedSyncHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.FeatureDialog;
import com.lmn.Arbiter_Android.Dialog.ProgressDialog.PictureProgressDialog;
import com.lmn.Arbiter_Android.Dialog.ProgressDialog.SyncProgressDialog;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.Loaders.LayersListLoader;
import com.lmn.Arbiter_Android.Loaders.NotificationsLoader;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.Media.HandleZeroByteFiles;
import com.lmn.Arbiter_Android.OnAddingGeometryPart.OnAddingGeometryPart;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;
import com.lmn.Arbiter_Android.ReturnQueues.OnReturnToMap;
import com.lmn.Arbiter_Android.ReturnQueues.OnReturnToProjects;
import com.lmn.Arbiter_Android.ReturnQueues.ReturnToActivityJob;

public class ArbiterCordova extends CordovaPlugin{
	private static final String TAG = "ArbiterCordova";
	private final ArbiterProject arbiterProject;
	public static final String mainUrl = "file:///android_asset/www/main.html";
	public static final String aoiUrl = "file:///android_asset/www/aoi.html";
	
	public ArbiterCordova(){
		super();
		this.arbiterProject = ArbiterProject.getArbiterProject();
	}
	
	public class MediaSyncingTypes {
		public static final int UPLOADING = 0;
		public static final int DOWNLOADING = 1;
	}
	
	@Override
	public boolean execute(String action, JSONArray args,
			final CallbackContext callbackContext) throws JSONException {
		
		if("setProjectsAOI".equals(action)){
			
			String aoi = args.getString(0);
			//String tileCount = args.getString(1);
			
			setProjectsAOI(aoi);
			
			return true;
		}else if("invalidGeometriesEntered".equals(action)){
			
			final Activity activity = cordova.getActivity();
			
			final String featureType = args.getString(0);
			final String featureId = args.getString(1);
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					
					builder.setTitle(R.string.warning);
					
					String validDescription = "\t" + activity.getResources().getString(R.string.valid_point_geometry_description);
					
					validDescription += "\n\n\t" + activity.getResources().getString(R.string.valid_line_geometry_description);
					
					validDescription += "\n\n\t" + activity.getResources().getString(R.string.valid_polygon_geometry_description);
					
					validDescription += "\n\n\t" + activity.getResources().getString(R.string.valid_multipoint_geometry_description);
					
					validDescription += "\n\n\t" + activity.getResources().getString(R.string.valid_multiline_geometry_description);
					
					validDescription += "\n\n\t" + activity.getResources().getString(R.string.valid_multipolygon_geometry_description);
					
					validDescription += "\n\n\t" + activity.getResources().getString(R.string.valid_geometry_collection_description);
					
					if(!"null".equals(featureId) && featureId != null){
						
						builder.setMessage(activity.getResources().getString(R.string.no_valid_geometries_delete_or_cancel)
								+ "\n\n" + validDescription);
						
						builder.setPositiveButton(R.string.delete_feature, new DialogInterface.OnClickListener() {
							
							@Override
							public void onClick(DialogInterface dialog, int which) {
										
								deleteFeature(featureType, featureId);
							}
						});
						
						builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog, int which) {
										
								String title = activity.getResources().getString(R.string.loading);
								String message = activity.getResources().getString(R.string.please_wait);
								
								final ProgressDialog progressDialog = ProgressDialog.show(
										activity, title, message, true);
								
								cordova.getThreadPool().execute(new Runnable(){
									@Override
									public void run(){
										
										ControlPanelHelper cpHelper = new ControlPanelHelper(activity);
										cpHelper.clearControlPanel();
										
										ArbiterState.getArbiterState().doneEditingFeature();
										
										activity.runOnUiThread(new Runnable(){
											@Override
											public void run(){
												Log.w("FeatureDialogHelper", "FeatureDialogHelper reloadMap");
												
												try{
													Map.MapChangeListener mapListener = (Map.MapChangeListener) activity;
													
													mapListener.getMapChangeHelper().reloadMap();
													mapListener.getMapChangeHelper().setEditMode(GeometryEditor.Mode.OFF);
												}catch(ClassCastException e){
													e.printStackTrace();
												}finally{
													progressDialog.dismiss();
												}
											}
										});
									}
								});
							}
						});
					}else{
						
						builder.setMessage(activity.getResources().getString(R.string.no_valid_geometries)
								+ "\n\n" + validDescription);
						
						builder.setPositiveButton(R.string.close, null);
					}
					
					builder.create().show();
				}
			});
			return true;
		}else if("isAddingGeometryPart".equals(action)){
			
			boolean isAddingPart = args.getBoolean(0);
			
			OnAddingGeometryPart.getInstance().checked(isAddingPart);
			
			return true;
		}else if("osmLinkClicked".equals(action)){
			
			Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.openstreetmap.org/copyright"));
			cordova.getActivity().startActivity(browserIntent);
			
			return true;
		}else if("gotPicture".equals(action)){
			
			PictureProgressDialog.show(cordova.getActivity());
			
			return true;
		}else if("featureNotInAOI".equals(action)){
			
			String featureId = args.getString(0);
			
			showFeatureNotInAOIWarning(featureId, callbackContext);
			
			return true;
		}else if("appFinishedLoading".equals(action)){
			
			cordova.getActivity().runOnUiThread(new Runnable(){
				@Override
				public void run(){
					AppFinishedLoading.getInstance().setFinishedLoading(true);
				}
			});
			
			return true;
		}else if("syncOperationTimedOut".equals(action)){
			
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					
					builder.setTitle(R.string.sync_timed_out);
					
					builder.setMessage(R.string.sync_timed_out_msg);
					
					builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							Log.w("ArbiterCordova", "ArbiterCordova syncOperationTimedOut cancel");
							callbackContext.error(0);
						}
					});
					
					builder.setPositiveButton(R.string.continue_sync, new DialogInterface.OnClickListener(){

						@Override
						public void onClick(DialogInterface dialog, int which) {
							
							Log.w("ArbiterCordova", "ArbiterCordova syncOperationTimedOut continue");
							callbackContext.success();
						}
					});
					
					builder.create().show();
				}
			});
			
			return true;
		}else if("layersAlreadyInProject".equals(action)){
			
			final JSONArray layersAlreadyInProject = args.getJSONArray(0);
			
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					
					if(layersAlreadyInProject != null && layersAlreadyInProject.length() > 0){
						
						LocalBroadcastManager.getInstance(activity.getApplicationContext())
							.sendBroadcast(new Intent(LayersListLoader.LAYERS_LIST_UPDATED));
						
						String message = activity.getResources().getString(R.string.layers_already_in_project);
						
						for(int i = 0, count = layersAlreadyInProject.length(); i < count; i++){
							
							if(i == 0){
								message += "\n";
							}
							
							try {
								message += "\n" + layersAlreadyInProject.getString(i);
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						AlertDialog.Builder builder = new AlertDialog.Builder(activity);
						
						builder.setTitle(activity.getResources().getString(R.string.warning));
						
						builder.setMessage(message);
						
						builder.setPositiveButton(R.string.close, null);
						
						builder.create().show();
					}
				}
			});
			
			return true;
		}else if("resetWebApp".equals(action)){
			String extent = args.getString(0);
            String zoomLevel = args.getString(1);
            
			resetWebApp(extent, zoomLevel, callbackContext);
			
			return true;
		}else if("confirmTileCount".equals(action)){
			String count = args.getString(0);
			
			confirmTileCount(count);
		}else if("setNewProjectsAOI".equals(action)){
			String aoi = args.getString(0);
			
			setNewProjectsAOI(aoi, callbackContext);
			
			return true;
		}else if("finishedGettingLocation".equals(action)){
			
			final Activity activity = this.cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					
					ImageButton locationButton = (ImageButton) activity.findViewById(R.id.locationButton);
					
					if(locationButton != null){
						locationButton.clearAnimation();
					}
				}
			});
			
			return true;
		}else if("goToProjects".equals(action)){
			final String extent = args.getString(0);
            final String zoomLevel = args.getString(1);
            
    		final Activity activity = this.cordova.getActivity();
    		
    		CommandExecutor.runProcess(new Runnable(){
    			@Override
    			public void run(){
    				OOMWorkaround oom = new OOMWorkaround(activity);
    				oom.resetSavedBounds(false);
    				oom.setSavedBounds(extent, zoomLevel, false);
    				
    				activity.runOnUiThread(new Runnable(){
    					@Override
    					public void run(){
    		        		Intent projectsIntent = new Intent(activity, ProjectsActivity.class);
    		        		activity.startActivity(projectsIntent);
    					}
    				});
    			}
    		});
		}else if("createNewProject".equals(action)){
			final String extent = args.getString(0);
            final String zoomLevel = args.getString(1);
            
    		final Activity activity = this.cordova.getActivity();
    		
    		CommandExecutor.runProcess(new Runnable(){
    			@Override
    			public void run(){
    				OOMWorkaround oom = new OOMWorkaround(activity);
    				oom.resetSavedBounds(false);
    				oom.setSavedBounds(extent, zoomLevel, false);
    				
    				activity.runOnUiThread(new Runnable(){
    					@Override
    					public void run(){
    						FragmentActivity activity = getFragmentActivity();
    						ArbiterDialogs dialogs = new ArbiterDialogs(activity.getApplicationContext(),
    								activity.getResources(),
    								activity.getSupportFragmentManager());
    						
    						try{
    							
    							dialogs.showProjectNameDialog(((HasConnectivityListener) activity).getListener());
    						}catch(ClassCastException e){
    							e.printStackTrace();
    						}
    					}
    				});
    			}
    		});
			
			return true;
		}else if("errorCreatingProject".equals(action)){
			errorCreatingProject(callbackContext);
			
			return true;
		}else if("errorLoadingFeatures".equals(action)){
			errorLoadingFeatures();
			
			return true;
		}else if("errorAddingLayers".equals(action)){
			String error = args.getString(0);
			
			errorAddingLayers(error);
			
			return true;
		}else if("featureSelected".equals(action)){
			String featureType = args.getString(0);
			String featureId = args.getString(1);
			String layerId = args.getString(2);
			String wktGeometry = args.getString(3);
			String mode = args.getString(4);
			
			featureSelected(featureType, featureId,
					layerId, wktGeometry, mode);
			
			return true;
		}else if("updateTileSyncingStatus".equals(action)){
			String percentComplete = args.getString(0);
			
			updateTileSyncingStatus(percentComplete);
			
			return true;
		}else if("syncCompleted".equals(action)){
			syncCompleted(callbackContext);
			
			return true;
		}else if("errorUpdatingAOI".equals(action)){
			errorUpdatingAOI(args.getString(0), callbackContext);
			
			return true;
		}else if("addMediaToFeature".equals(action)){
			String key = args.getString(0);
			String media = args.getString(1);
			String newMedia = args.getString(2);
			
			addMediaToFeature(key, media, newMedia);
			
			return true;
		}else if("updateMediaUploadingStatus".equals(action)){
			
			String featureType = args.getString(0);
			int finishedMediaCount = args.getInt(1);
			int totalMediaCount = args.getInt(2);
			int finishedLayersUploading = args.getInt(3);
			int totalLayers = args.getInt(4);
			
			updateMediaUploadingStatus(featureType, finishedMediaCount,
					totalMediaCount, finishedLayersUploading, totalLayers);
			
			return true;
		}else if("updateMediaDownloadingStatus".equals(action)){
			
			try{
				String featureType = args.getString(0);
				int finishedMediaCount = args.getInt(1);
				int totalMediaCount = args.getInt(2);
				int finishedLayersDownloading = args.getInt(3);
				int totalLayers = args.getInt(4);
				
				updateMediaDownloadingStatus(featureType, finishedMediaCount,
						totalMediaCount, finishedLayersDownloading, totalLayers);
			}catch(JSONException e){
				e.printStackTrace();
			}
			
			return true;
		}else if("updateUploadingVectorDataProgress".equals(action)){
			
			int finished = args.getInt(0);
			int total = args.getInt(1);
			
			updateUploadingVectorDataProgress(finished, total);
			
			return true;
		}else if("updateDownloadingVectorDataProgress".equals(action)){
			
			int finished = args.getInt(0);
			int total = args.getInt(1);
			
			updateDownloadingVectorDataProgress(finished, total);
			
			return true;
		}else if("showDownloadingSchemasProgress".equals(action)){
			
			String count = args.getString(0);
			
			showDownloadingSchemasProgress(count);
			
			return true;
		}else if("updateDownloadingSchemasProgress".equals(action)){
			
			String finished = args.getString(0);
			String total = args.getString(1);
			
			updateDownloadingSchemasProgress(finished, total);
			
			return true;
		}else if("dismissDownloadingSchemasProgress".equals(action)){
			
			return true;
		}else if("alertGeolocationError".equals(action)){
			
			alertGeolocationError();
			
			return true;
		}else if("featureUnselected".equals(action)){
			
			featureUnselected();
			
			return true;
		}else if("showUpdatedGeometry".equals(action)){
			
			String featureType = args.getString(0);
			String featureId = args.getString(1);
			String layerId = args.getString(2);
			String wktGeometry = args.getString(3);
			
			showUpdatedGeometry(featureType, featureId, layerId, wktGeometry);
			
			return true;
		}else if("setMultiPartBtnsEnabled".equals(action)){
			
			boolean enable = args.getBoolean(0);
			boolean enableCollection = args.getBoolean(1);
			
			setMultiPartBtnsEnabled(enable, enableCollection);
			
			return true;
		}else if("confirmPartRemoval".equals(action)){
			
			confirmPartRemoval(callbackContext);
			
			return true;
		}else if("confirmGeometryRemoval".equals(action)){
			
			confirmGeometryRemoval(callbackContext);
			
			return true;
		}else if("hidePartButtons".equals(action)){
			
			hidePartButtons(); 
			
			return true;
		}else if("reportLayersWithUnsupportedCRS".equals(action)){
			
			JSONArray layers = args.getJSONArray(0);
			
			reportLayersWithUnsupportedCRS(layers);
			
			return true;
		}else if("gotNotifications".equals(action)){
			
			gotNotifications();
			
			return true;
		}
		
		// Returning false results in a "MethodNotFound" error.
		return false;
	}
	
	private void deleteFeature(final String featureType, final String featureId){
		
		final Activity activity = cordova.getActivity();
		
		String title = activity.getResources().getString(R.string.loading);
		String message = activity.getResources().getString(R.string.please_wait);
		
		final ProgressDialog progressDialog = ProgressDialog.show(
				activity, title, message, true);
		
		cordova.getThreadPool().execute(new Runnable(){
			@Override
			public void run(){
				
				SQLiteDatabase db = (new Util()).getFeatureDb(activity, false);
				
				Feature feature = FeaturesHelper.getHelper().getFeature(db, featureId, featureType);
				
				if(feature.getSyncState().equals(FeaturesHelper.SYNC_STATES.SYNCED)){
					feature.setModifiedState(FeaturesHelper.MODIFIED_STATES.DELETED);
					feature.setSyncState(FeaturesHelper.SYNC_STATES.NOT_SYNCED);
					
					FeaturesHelper.getHelper().update(db, 
							feature.getFeatureType(), feature.getId(),
							feature);
				}else{ 
					
					feature.setModifiedState(FeaturesHelper.MODIFIED_STATES.DELETED);
					
					String fid = feature.getFID();
					
					if(fid != null && !fid.equals("")){
						FeaturesHelper.getHelper().update(db, 
								feature.getFeatureType(), feature.getId(),
								feature);
					}else{
						// If the feature isn't synced and it's not on the server, 
						// then we don't need to keep track of it anymore.
						FeaturesHelper.getHelper().delete(db,
								feature.getFeatureType(), feature.getId());
					}
				}
				
				ControlPanelHelper cpHelper = new ControlPanelHelper(cordova.getActivity());
				cpHelper.clearControlPanel();
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						
						try{
							
							((Map.MapChangeListener) activity).getMapChangeHelper().reloadMap();
							
							AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){

								@Override
								public void run() {
									
									progressDialog.dismiss();
								}
							});
						}catch(ClassCastException e){
							e.printStackTrace();
						}
					}
				});
			}
		});
	}
	
	private void showFeatureNotInAOIWarning(final String featureId, final CallbackContext callbackContext){
		
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				
				builder.setTitle(activity.getResources().getString(R.string.warning));
				
				builder.setMessage(activity.getResources().getString(R.string.feature_outside_aoi_warning));
				
				String positiveString = null;
				
				if(featureId != null && !"null".equals(featureId)){
					positiveString = activity.getResources().getString(R.string.edit_attributes);
				}else{
					positiveString = activity.getResources().getString(R.string.insert);
				}
				
				builder.setPositiveButton(positiveString, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						callbackContext.success();
					}
				});
				
				builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						try{
							((Map.MapChangeListener) activity).getMapChangeHelper().setEditMode(GeometryEditor.Mode.OFF);
							callbackContext.error(0);
						}catch(ClassCastException e){
							e.printStackTrace();
						}
					}
				});
				
				builder.setCancelable(false);
				
				builder.create().show();
			}
		});
	}
	
	private void reportLayersWithUnsupportedCRS(final JSONArray layers){
		
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				try{
					
					JSONObject layer = null;
					
					String list = cordova.getActivity().getResources().getString(R.string.loading_unsupported_crs) + "\n";
					
					for(int i = 0, count = layers.length(); i < count; i++){
						
						layer = layers.getJSONObject(i);
						
						list += "\n" + (i + 1) + ". " + layer.getString(LayersHelper.LAYER_TITLE) 
								+ ", " + layer.getString(LayersHelper.WORKSPACE) 
								+ ", " + layer.getString(GeometryColumnsHelper.FEATURE_GEOMETRY_SRID) + "\n";
					}
					
					AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					
					builder.setTitle(R.string.unsupported_crs);
					builder.setMessage(list);
					builder.setPositiveButton(android.R.string.ok, null);
					
					builder.create().show();
				}catch(JSONException e){
					e.printStackTrace();
				}
			}
		});
	}
	
	private void hidePartButtons(){
		
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				try{
					
					((Map.MapChangeListener) activity).getMapChangeHelper().hidePartButtons();
				}catch(ClassCastException e){
					e.printStackTrace();
				}
			}
		});
	}
	
	private void confirmPartRemoval(final CallbackContext callback){
		
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				
				builder.setIcon(R.drawable.icon);
				builder.setTitle(R.string.confirm_part_removal_title);
				builder.setMessage(R.string.confirm_part_removal_msg);
				
				builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						callback.success();
						
						switchToSelectMode();
					}
				});
				
				builder.setNegativeButton(android.R.string.cancel, null);
				
				builder.create().show();
			}
		});
	}
	
	private void confirmGeometryRemoval(final CallbackContext callback){
		
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				
				builder.setIcon(R.drawable.icon);
				builder.setTitle(R.string.confirm_geometry_removal_title);
				builder.setMessage(R.string.confirm_geometry_removal_msg);
				
				builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
						callback.success();
						
						switchToSelectMode();
					}
				});
				
				builder.setNegativeButton(android.R.string.cancel, null);
				
				builder.create().show();
			}
		});
	}
	
	private void switchToSelectMode(){
		try{
			((Map.MapChangeListener) cordova.getActivity())
				.getMapChangeHelper().setEditMode(GeometryEditor.Mode.SELECT);
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	private void setMultiPartBtnsEnabled(boolean enable, boolean enableCollection){
		
		try{
			
			((Map.MapChangeListener) cordova.getActivity())
				.getMapChangeHelper().enableMultiPartBtns(enable, enableCollection);
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	private void showUpdatedGeometry(String featureType,
			String featureId, String layerId, String wktGeometry){
		
		Log.w("ArbiterCordova", "ArbiterCordova featureType: " + featureType 
				+ ", featureId = " + featureId + ", wktGeometry = " + wktGeometry);
		
		try{
			MapChangeHelper helper = ((Map.MapChangeListener) cordova.getActivity()).getMapChangeHelper();
			helper.showUpdatedGeometry(
						featureType, featureId, layerId, wktGeometry);
			helper.setEditMode(GeometryEditor.Mode.OFF);
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	private void alertGeolocationError(){
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				
				builder.setIcon(R.drawable.icon);
				builder.setTitle(R.string.geolocation_error);
				builder.setMessage(R.string.geolocation_error_msg);
				builder.setPositiveButton(android.R.string.ok, null);
				
				builder.create().show();
			}
		});
	}
	
	private void showDownloadingSchemasProgress(final String count){
		final Activity activity = cordova.getActivity();
		String title = activity.getResources().getString(R.string.downloading_schemas);
		String message = activity.getResources().getString(R.string.downloaded);
		message += "\t0\t/\t" + count;
		
		SyncProgressDialog.setTitleAndMessage(activity, title, message);
	}
	
	private void updateDownloadingSchemasProgress(final String finished, final String total){
		final Activity activity = cordova.getActivity();
		String message = activity.getResources().getString(R.string.downloaded);	
		message += "\t" + finished + "\t/\t" + total;
		
		SyncProgressDialog.setMessage(activity, message);
	}
	
	private boolean dismissVectorProgress(int finishedCount, int totalCount){
		return finishedCount == totalCount;
	}
	
	private void updateUploadingVectorDataProgress(final int finished, final int total){
		final Activity activity = cordova.getActivity();
		
		final boolean shouldDismissProgress = dismissVectorProgress(finished, total);
				
		if(!shouldDismissProgress){
			
			String title = activity.getResources().
					getString(R.string.syncing_vector_data);
			
			String message = activity.getResources().getString(R.string.uploaded);
			
			message += "\t" + finished + "\t/\t" + total;
			
			SyncProgressDialog.setTitleAndMessage(activity, title, message);
		}
	}
	
	private void updateDownloadingVectorDataProgress(final int finished, final int total){
		final Activity activity = cordova.getActivity();
		
		final boolean shouldDismissProgress = dismissVectorProgress(finished, total);

		if(!shouldDismissProgress){
			
			String title = activity.getResources().
					getString(R.string.syncing_vector_data);
			
			String message = activity.getResources().getString(R.string.downloaded);
			
			message += "\t" + finished + "\t/\t" + total;
			
			SyncProgressDialog.setTitleAndMessage(activity, title, message);
		}
	}
	
	private boolean mediaSyncShouldBeDismissed(int finishedMediaCount,
			int totalMediaCount, int finishedLayersCount, int totalLayersCount){
		
		boolean shouldBeDismissed = false;
		
		if(finishedMediaCount == totalMediaCount 
				&& finishedLayersCount == totalLayersCount){
			shouldBeDismissed = true;
		}
		
		return shouldBeDismissed;
	}
	
	private void updateMediaUploadingStatus(final String featureType,
			final int finishedMediaCount, final int totalMediaCount,
			final int finishedLayersUploading, final int totalLayers){
		
		final Activity activity = cordova.getActivity();
		
		final boolean shouldBeDismissed = mediaSyncShouldBeDismissed(
				finishedMediaCount, totalMediaCount,
				finishedLayersUploading, totalLayers);
		
		if(!shouldBeDismissed){
			String title = activity.getResources().getString(R.string.syncing_media_title);
			
			String message = featureType + "\n\n\t";
			
			message += activity.getResources().getString(R.string.uploaded);
			
			message += "\t" + finishedMediaCount + "\t/\t" + totalMediaCount;
			
			SyncProgressDialog.setTitleAndMessage(activity, title, message);
		}
	}
	
	
	private void updateMediaDownloadingStatus(final String featureType,
			final int finishedMediaCount, final int totalMediaCount,
			final int finishedLayersUploading, final int totalLayers){
		
		final Activity activity = cordova.getActivity();
		
		final boolean shouldBeDismissed = mediaSyncShouldBeDismissed(
				finishedMediaCount, totalMediaCount,
				finishedLayersUploading, totalLayers);
		
		if(!shouldBeDismissed){
			String title = activity.getResources().getString(R.string.syncing_media_title);
			
			String message = featureType + "\n\n\t";
			
			message += activity.getResources().getString(R.string.downloaded);
			
			message += "\t" + finishedMediaCount + "\t/\t" + totalMediaCount;
			
			SyncProgressDialog.setTitleAndMessage(activity, title, message);
		}
	}
	
	private void addMediaToFeature(String key, String media, String newMedia){
		FeatureDialog dialog = (FeatureDialog) getFragmentActivity()
				.getSupportFragmentManager()
				.findFragmentByTag(FeatureDialog.TAG);
		
		dialog.updateFeaturesMedia(key, media, newMedia);
	}
	
	private void updateTileSyncingStatus(final String percentComplete){
		
		final Activity activity = cordova.getActivity();
		
		final String title = activity.getResources().getString(R.string.downloading_tiles);
				
		String message = activity.getResources()
				.getString(R.string.downloaded);
		
		message += "\t" + percentComplete + "\t/\t" + "100%";
		
		SyncProgressDialog.setTitleAndMessage(activity, title, message);
	}
	
	private void errorUpdatingAOI(final String error, final CallbackContext callback){
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				//arbiterProject.dismissSyncProgressDialog();
				
				Util.showDialog(cordova.getActivity(), R.string.error_updating_aoi, 
						R.string.error_updating_aoi_msg, error, null, null, null);
			}
		});
		
		callback.success();
	}
	
	private void gotNotifications(){
		
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(
						new Intent(NotificationsLoader.NOTIFICATIONS_UPDATED));
				
				SyncProgressDialog.dismiss(activity);
			}
		});
	}
	
	private void syncCompleted(final CallbackContext callbackContext){
		final Activity activity = cordova.getActivity();
		
		String projectName = arbiterProject.getOpenProject(activity);
		final String mediaPath = ProjectStructure.getMediaPath(projectName);
		String projectPath = ProjectStructure.getProjectPath(projectName);
		
		FragmentActivity fragActivity = null;
		ConnectivityListener connectivityListener = null;
		HasThreadPool hasThreadPool = null;
		
		try{
			fragActivity = (FragmentActivity) activity;
			connectivityListener = ((HasConnectivityListener) activity).getListener();
			hasThreadPool = (HasThreadPool) activity;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
		
		final SQLiteDatabase projectDb = ProjectDatabaseHelper.getHelper(fragActivity.getApplicationContext(),
				projectPath, false).getWritableDatabase();
		
		FailedSyncHelper failedSyncHelper = new FailedSyncHelper(fragActivity,
				projectDb, connectivityListener, hasThreadPool);
		
		failedSyncHelper.checkIncompleteSync();
		
		cordova.getThreadPool().execute(new Runnable(){
			@Override
			public void run(){
				HandleZeroByteFiles handler = new HandleZeroByteFiles(mediaPath);
				handler.deleteZeroByteFiles();
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						
						// If a sync completed and new project wasn't null,
						// That means a project was just created.
						if(arbiterProject.getNewProject() != null){
							
							arbiterProject.doneCreatingProject(
									activity.getApplicationContext());
						}
						
						LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(
								new Intent(NotificationsLoader.NOTIFICATIONS_UPDATED));
						
						LocalBroadcastManager.getInstance(activity.getApplicationContext()).sendBroadcast(
								new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
						
						SyncProgressDialog.dismiss(activity);
						
						try{
							((Map.MapChangeListener) activity)
								.getMapChangeHelper().onSyncCompleted();
							
						} catch(ClassCastException e){
							e.printStackTrace();
							throw new ClassCastException(activity.toString() 
									+ " must be an instance of Map.MapChangeListener");
						}
						
						callbackContext.success();
					}
				});
			}
		});
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
	
	private void featureUnselected(){
		
		try{
			((Map.MapChangeListener) cordova.getActivity())
				.getMapChangeHelper().onUnselectFeature();
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	private void featureSelected(final String featureType, final String featureId,
			final String layerId, final String wktGeometry, final String mode){
		
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				try{
					Log.w(TAG, TAG + ".featureSelected");
					((Map.MapChangeListener) cordova.getActivity())
						.getMapChangeHelper().onSelectFeature(featureType,
								featureId, layerId, wktGeometry, mode);
				}catch(ClassCastException e){
					e.printStackTrace();
				}
			}
		});
	}
	
	private void errorAddingLayers(final String error){
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				Util.showDialog(cordova.getActivity(), R.string.error_adding_layers, 
						R.string.error_adding_layers_msg, error, null, null, null);
			}
		});
	}
	
	private void errorLoadingFeatures(){
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				Util.showDialog(cordova.getActivity(), R.string.error_loading_features, 
						R.string.error_loading_features_msg, null, null, null, null);
			}
		});
	}
	
	private void errorCreatingProject(final CallbackContext callback){
		Log.w("ArbiterCordova", "ArbiterCordova.errorCreatingProject");
		
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				ArbiterProject.getArbiterProject().errorCreatingProject(
						cordova.getActivity());
				
				callback.success();
			}
		});
	}
	
	private void confirmTileCount(final String count){
		
		try{
			TileConfirmation tileConfirmation = (TileConfirmation) cordova.getActivity();
			tileConfirmation.confirmTileCount(count);
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	private void setNewProjectsAOI(final String aoi, final CallbackContext callbackContext){
		//ArbiterState.getState().setNewAOI(aoi);
		ArbiterProject.getArbiterProject().getNewProject().setAOI(aoi);
		
		OnReturnToMap.getInstance().push(new ReturnToActivityJob(){

			@Override
			public void run(final Activity activity) {
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						
						SyncProgressDialog.show(activity);
		    			
						cordova.getThreadPool().execute(new Runnable(){
							@Override
							public void run(){
								
								new ArbiterCookieManager(cordova.getActivity().getApplicationContext()).updateAllCookies();
								
								cordova.getActivity().runOnUiThread(new Runnable(){
									@Override
									public void run(){
										
										Project newProject = ArbiterProject.getArbiterProject().getNewProject();
						    			
						    			ArbiterProject.getArbiterProject().doneCreatingProject(activity.getApplicationContext());
						    			
						    			InsertProjectHelper insertHelper = new InsertProjectHelper(activity, newProject);
						    			insertHelper.insert();
									}
								});
							}
						});
					}
				});
			}
		});
		
		OnReturnToProjects.getInstance().push(new ReturnToActivityJob(){
			
			@Override
			public void run(Activity activity){
				
				// Finish the projects activity to jump back to the map.
				activity.finish();
			}
		});
		
		callbackContext.success();
		
		cordova.getActivity().finish();
	}
	
	private void showAOIConfirmationDialog(final String aoi){
		final Activity activity = cordova.getActivity();
		
		String message = activity.getResources()
				.getString(R.string.update_aoi_alert_msg);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setIcon(R.drawable.icon);
		builder.setTitle(R.string.update_aoi_alert);
		builder.setMessage(message);
		builder.setNegativeButton(android.R.string.cancel, null);
		
		builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				try{
				
					ConnectivityListener listener = ((HasConnectivityListener) activity).getListener();
					
					if(listener != null && listener.isConnected()){
						ArbiterState.getArbiterState().setNewAOI(aoi);
						
						activity.finish();
					}else{
						
						Util.showNoNetworkDialog(activity);
					}
				}catch(ClassCastException e){
					e.printStackTrace();
				}
			}
		});
		
		builder.create().show();
	}
	
	/**
	 * Set the ArbiterProject Singleton's newProject aoi, commit the project, and return to the map
	 */
	private void setProjectsAOI(final String aoi){
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){

				showAOIConfirmationDialog(aoi);
			}
		});
	} 
	
	
	private void resetWebApp(final String currentExtent, final String zoomLevel,
			final CallbackContext callbackContext){
		
		final Activity activity = this.cordova.getActivity();
		final CordovaWebView webview = this.webView;
		final View overlay = (View) activity.findViewById(R.id.mapOverlay);
		final boolean isCreatingProject = ArbiterState
				.getArbiterState().isCreatingProject();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				final ProgressDialog dialog = ProgressDialog.show(activity, activity.getResources().getString(
						R.string.loading), activity.getResources().getString(R.string.please_wait), true);
				
				CommandExecutor.runProcess(new Runnable(){
					@Override
					public void run(){
						OOMWorkaround oom = new OOMWorkaround(activity);
						oom.setSavedBounds(currentExtent, zoomLevel, isCreatingProject);
						
						activity.runOnUiThread(new Runnable(){
							@Override
							public void run(){
								AppFinishedLoading.getInstance().setFinishedLoading(false);
								if (overlay != null){
									overlay.setVisibility(View.VISIBLE);
								}
								webview.loadUrl("about:blank");
								
								AppFinishedLoading.getInstance().onAppFinishedLoading(new AppFinishedLoadingJob(){
									@Override
									public void run() {
										
										activity.runOnUiThread(new Runnable(){
											@Override
											public void run(){
												
												if(overlay != null){
													overlay.setVisibility(View.GONE);
												}
												
												dialog.dismiss();
											}
										});
									}
								});
							}
						});
					}
				});
			}
		});
	}
}
