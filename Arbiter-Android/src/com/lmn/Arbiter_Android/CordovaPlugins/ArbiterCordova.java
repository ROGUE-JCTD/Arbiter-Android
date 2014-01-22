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
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.OOMWorkaround;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.Activities.TileConfirmation;
import com.lmn.Arbiter_Android.CordovaPlugins.Helpers.FeatureHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ControlPanelHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.FeatureDialog;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.MediaSyncHelper;
import com.lmn.Arbiter_Android.Map.Map;

public class ArbiterCordova extends CordovaPlugin{
	private static final String TAG = "ArbiterCordova";
	private final ArbiterProject arbiterProject;
	public static final String mainUrl = "file:///android_asset/www/main.html";
	public static final String aoiUrl = "file:///android_asset/www/aoi.html";
	
	private ProgressDialog mediaUploadProgressDialog;
	private ProgressDialog mediaDownloadProgressDialog;
	
	private ProgressDialog vectorUploadProgressDialog;
	private ProgressDialog vectorDownloadProgressDialog;
	
	private ProgressDialog downloadingSchemasProgressDialog;
	
	private ProgressDialog tileCachingProgressDialog;
	
	public ArbiterCordova(){
		super();
		this.arbiterProject = ArbiterProject.getArbiterProject();
		this.mediaUploadProgressDialog = null;
		this.mediaDownloadProgressDialog = null;
		
		this.vectorUploadProgressDialog = null;
		this.vectorDownloadProgressDialog = null;
		
		this.downloadingSchemasProgressDialog = null;
		
		this.tileCachingProgressDialog = null;
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
			String tileCount = args.getString(1);
			
			setProjectsAOI(aoi, tileCount);
			
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
		}else if("syncFailed".equals(action)){
			syncFailed(args.getString(0), callbackContext);
			
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
		}else if("showMediaUploadingStatus".equals(action)){
			String layer = args.getString(0);
			String total = args.getString(1);
			
			showMediaUploadingStatus(layer, total);
			
			return true;
		}else if("updateMediaUploadingStatus".equals(action)){
			String layer = args.getString(0);
			String finished = args.getString(1);
			String total = args.getString(2);
			
			updateMediaUploadingStatus(layer, finished, total);
			
			return true;
		}else if("showMediaDownloadingStatus".equals(action)){
			
			String layer = args.getString(0);
			String total = args.getString(1);
			
			showMediaDownloadingStatus(layer, total);
			
			return true;
		}else if("updateMediaDownloadingStatus".equals(action)){
			String layer = args.getString(0);
			String finished = args.getString(1);
			String total = args.getString(2);
			
			updateMediaDownloadingStatus(layer, finished, total);
			
			return true;
		}else if("finishMediaUploading".equals(action)){
			
			finishMediaUploading();
			
			return true;
		}else if("finishMediaDownloading".equals(action)){
			finishMediaDownloading();
			
			return true;
		}else if("showUploadingVectorDataProgress".equals(action)){
			
			String count = args.getString(0);
			
			showUploadingVectorDataProgress(count);
			
			return true;
		}else if("updateUploadingVectorDataProgress".equals(action)){
			
			String finished = args.getString(0);
			String total = args.getString(1);
			
			updateUploadingVectorDataProgress(finished, total);
			
			return true;
		}else if("dismissUploadingVectorDataProgress".equals(action)){
			
			dismissUploadingVectorDataProgress();
			
			return true;
		}else if("showDownloadingVectorDataProgress".equals(action)){
			
			String count = args.getString(0);
			
			showDownloadingVectorDataProgress(count);
			
			return true;
		}else if("updateDownloadingVectorDataProgress".equals(action)){
			
			String finished = args.getString(0);
			String total = args.getString(1);
			
			updateDownloadingVectorDataProgress(finished, total);
			
			return true;
		}else if("dismissDownloadingVectorDataProgress".equals(action)){
			
			dismissDownloadingVectorDataProgress();
			
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
			
			dismissDownloadingSchemasProgress();
			
			return true;
		}else if("showErrorsSyncing".equals(action)){
			JSONArray failedVectorUploads = args.getJSONArray(0);
			JSONArray failedVectorDownloads = args.getJSONArray(1);
			JSONObject failedMediaUploads = args.getJSONObject(2);
			JSONObject failedMediaDownloads = args.getJSONObject(3);
			
		//	showErrorsSyncing(failedVectorUploads);
			
			return true;
		}
		
		// Returning false results in a "MethodNotFound" error.
		return false;
	}
	
	private void showDownloadingSchemasProgress(final String count){
		
		if(downloadingSchemasProgressDialog == null){
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					String title = activity.getResources().getString(R.string.downloading_schemas);
					String message = activity.getResources().getString(R.string.downloaded);
					
					message += "\t0\t/\t" + count;
					
					downloadingSchemasProgressDialog = ProgressDialog.show(cordova.getActivity(), title, message, true);
				}
			});
		}
	}
	
	private void updateDownloadingSchemasProgress(final String finished, final String total){
		
		if(downloadingSchemasProgressDialog != null){
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					String message = activity.getResources().getString(R.string.downloaded);
					
					message += "\t" + finished + "\t/\t" + total;
					
					downloadingSchemasProgressDialog.setMessage(message);
				}
			});
		}
	}

	private void dismissDownloadingSchemasProgress(){
		if(downloadingSchemasProgressDialog != null){
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					
					downloadingSchemasProgressDialog.dismiss();
					downloadingSchemasProgressDialog = null;
				}
			});
		}
	}
	
	private void showUploadingVectorDataProgress(final String count){
		
		if(vectorUploadProgressDialog == null){
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					String title = activity.getResources().getString(R.string.syncing_vector_data);
					String message = activity.getResources().getString(R.string.uploaded);
					
					message += "\t0\t/\t" + count;
					
					vectorUploadProgressDialog = ProgressDialog.show(cordova.getActivity(), title, message, true);
				}
			});
		}
	}
	
	private void updateUploadingVectorDataProgress(final String finished, final String total){
		
		if(vectorUploadProgressDialog != null){
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					String message = activity.getResources().getString(R.string.uploaded);
					
					message += "\t" + finished + "\t/\t" + total;
					
					vectorUploadProgressDialog.setMessage(message);
				}
			});
		}
	}

	private void dismissUploadingVectorDataProgress(){
		if(vectorUploadProgressDialog != null){
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					
					vectorUploadProgressDialog.dismiss();
					vectorUploadProgressDialog = null;
				}
			});
		}
	}

	private void showDownloadingVectorDataProgress(final String count){
		
		if(vectorDownloadProgressDialog == null){
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					String title = activity.getResources().getString(R.string.syncing_vector_data);
					String message = activity.getResources().getString(R.string.downloaded);
					
					message += "\t0\t/\t" + count;
					
					vectorDownloadProgressDialog = ProgressDialog.show(cordova.getActivity(), title, message, true);
				}
			});
		}
	}
	
	private void updateDownloadingVectorDataProgress(final String finished, final String total){
		
		if(vectorDownloadProgressDialog != null){
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					String message = activity.getResources().getString(R.string.downloaded);
					
					message += "\t" + finished + "\t/\t" + total;
					
					vectorDownloadProgressDialog.setMessage(message);
				}
			});
		}
	}

	private void dismissDownloadingVectorDataProgress(){
		if(vectorDownloadProgressDialog != null){
			final Activity activity = cordova.getActivity();
			
			activity.runOnUiThread(new Runnable(){
				@Override
				public void run(){
					
					vectorDownloadProgressDialog.dismiss();
					vectorDownloadProgressDialog = null;
				}
			});
		}
	}
	
	private void finishMediaUploading(){
		Log.w("ArbiterCordova", TAG + " finisheMediaUploading");
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(mediaUploadProgressDialog != null){
					Log.w("ArbiterCordova", TAG + " finisheMediaUploading dismissing");
					mediaUploadProgressDialog.dismiss();
					mediaUploadProgressDialog = null;
				}
			}
		});
	}
	
	private void finishMediaDownloading(){
		Log.w("ArbiterCordova", TAG + " finisheMediaDownloading");
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				if(mediaDownloadProgressDialog != null){
					Log.w("ArbiterCordova", TAG + " finisheMediaDownloading dismissing");
					mediaDownloadProgressDialog.dismiss();
					mediaDownloadProgressDialog = null;
				}
			}
		});
	}
	
	private void showMediaUploadingStatus(final String layer, final String total){
		
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				String title = activity.getResources().getString(R.string.syncing_media_title);
				String message = layer + "\n\n\t";
				
				message += activity.getResources().getString(R.string.uploaded);
				
				message += "\t0\t/\t" + total;
				
				if(mediaUploadProgressDialog == null){
					mediaUploadProgressDialog = ProgressDialog.show(activity, title, message, true);
				}
			}
		});
	}
	
	private void showMediaDownloadingStatus(final String layer, final String total){
		
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				String title = activity.getResources().getString(R.string.syncing_media_title);
				String message = layer + "\n\n\t";
				
				message += activity.getResources().getString(R.string.downloaded);
				
				message += "\t0\t/\t" + total;
				
				if(mediaDownloadProgressDialog == null){
					mediaDownloadProgressDialog = ProgressDialog.show(activity, title, message, true);
				}
			}
		});
	}
	
	private void updateMediaUploadingStatus(final String layer,
			final String finished, final String total){
		
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				String message = layer + "\n\n\t";
				
				message += activity.getResources().getString(R.string.uploaded);
				
				message += "\t" + finished + "\t/\t" + total;
				
				if(mediaUploadProgressDialog != null){
					mediaUploadProgressDialog.setMessage(message);
				}
			}
		});
	}
	
	private void updateMediaDownloadingStatus(final String layer,
			final String finished, final String total){
	
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				String message = layer + "\n\n\t";
				
				message += activity.getResources().getString(R.string.downloaded);
				
				message += "\t" + finished + "\t/\t" + total;
				
				if(mediaDownloadProgressDialog != null){
					mediaDownloadProgressDialog.setMessage(message);
				}
			}
		});
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
		
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				String message = activity.getResources()
						.getString(R.string.downloaded);
				
				message += "\t" + percentComplete + "\t/\t" + "100%";
				
				if(tileCachingProgressDialog != null){
					
					tileCachingProgressDialog.setMessage(message);
				}else{
					
					tileCachingProgressDialog = ProgressDialog.show(activity, title, message, true);
				}
			}
		});
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
	
	// Clear the mediaToSend property in the Preferences table,
	// which keeps track of files that need to be synced
	private void clearMediaToSend(){
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				MediaSyncHelper helper = new MediaSyncHelper(cordova.getActivity());
				helper.clearMediaToSend();
			}
		});
	}
	
	private void syncCompleted(final CallbackContext callbackContext){
		final Activity activity = cordova.getActivity();
		
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				// If a sync completed and new project wasn't null,
				// That means a project was just created.
				if(arbiterProject.getNewProject() != null){
					
					arbiterProject.doneCreatingProject(
							activity.getApplicationContext());
				}
				
				if(tileCachingProgressDialog != null){
					tileCachingProgressDialog.dismiss();
					tileCachingProgressDialog = null;
				}
				
				clearMediaToSend();
				
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
	
	private void syncFailed(final String error, final CallbackContext callback){
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				//arbiterProject.dismissSyncProgressDialog();
				
				Util.showDialog(cordova.getActivity(), R.string.error_syncing, 
						R.string.error_syncing_msg, error, null, null, null);
			}
		});
		
		callback.success();
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
	
	private void featureSelected(final String featureType, final String featureId,
			final String layerId, final String wktGeometry, final String mode){
		
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				FeatureHelper helper = new FeatureHelper(getFragmentActivity());
				helper.displayFeatureDialog(featureType, featureId,
						layerId, wktGeometry, mode);
				
				if(mode.equals(ControlPanelHelper.CONTROLS.INSERT)){
					try{
						
						((Map.MapChangeListener) cordova.getActivity())
						.getMapChangeHelper().doneInsertingFeature();
						
					}catch(ClassCastException e){
						e.printStackTrace();
					}
				}
				
				notifyDoneEditingFeature();
			}
		});
	}
	
	/**
	 * Notify the MapListener that that the feature is done,
	 * being edited.
	 */
	private void notifyDoneEditingFeature(){
		try{
			// Done editing so toggle the buttons
			((Map.MapChangeListener) cordova.getActivity())
				.getMapChangeHelper().toggleEditButtons(false);
		} catch(ClassCastException e){
			e.printStackTrace();
			throw new ClassCastException(cordova.getActivity().toString() 
					+ " must be an instance of Map.MapChangeListener");
		}
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
		
		callbackContext.success();
		
		cordova.getActivity().finish();
	}
	
	private void showAOIConfirmationDialog(final String aoi, final String count){
		final Activity activity = cordova.getActivity();
		
		String message = activity.getResources()
				.getString(R.string.update_aoi_alert_msg);
		
		message += "\n\n" + activity.getResources().getString(
				R.string.tile_cache_warning) + " " + count;
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setIcon(R.drawable.icon);
		builder.setTitle(R.string.update_aoi_alert);
		builder.setMessage(message);
		builder.setNegativeButton(android.R.string.cancel, null);
		
		builder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				ArbiterState.getArbiterState().setNewAOI(aoi);
				
				activity.finish();
			}
		});
		
		builder.create().show();
	}
	
	/**
	 * Set the ArbiterProject Singleton's newProject aoi, commit the project, and return to the map
	 */
	private void setProjectsAOI(final String aoi, final String count){
		cordova.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				showAOIConfirmationDialog(aoi, count);
			}
		});
	} 
	
	
	private void resetWebApp(final String currentExtent, final String zoomLevel,
			final CallbackContext callbackContext){
		
		final Activity activity = this.cordova.getActivity();
		final CordovaWebView webview = this.webView;
		final boolean isCreatingProject = ArbiterState
				.getArbiterState().isCreatingProject();
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				OOMWorkaround oom = new OOMWorkaround(activity);
				oom.setSavedBounds(currentExtent, zoomLevel, isCreatingProject);
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
		                
						webview.loadUrl("about:blank");
					}
				});
			}
		});
	}
}
