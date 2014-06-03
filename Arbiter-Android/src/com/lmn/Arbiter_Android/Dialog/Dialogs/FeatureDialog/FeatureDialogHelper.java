package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ControlPanelHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.GeometryEditor.GeometryEditor;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class FeatureDialogHelper {
	private FragmentActivity activity;
	private Map.MapChangeListener mapListener;
	private HasThreadPool threadPoolSupplier;
	
	private Feature feature;
	private String layerId;
	
	private FeatureDialogBuilder builder;
	private boolean editing;
	
	// To prevent the user from doing something broken for now...
	// TODO: Fix the ControlPanel.js logic...
	private boolean keepEditOnMapDisabled;
		
	public FeatureDialogHelper(FragmentActivity activity, View view, 
			Feature feature, boolean startInEditMode,
			Button editButton, Button editOnMapButton,
			Button cancelButton, Button deleteButton, String layerId, Util util){
		
		this.activity = activity;
		this.feature = feature;
		this.editing = startInEditMode;
		this.layerId = layerId;
		this.keepEditOnMapDisabled = false;
		
		Log.w("FeatureDialogHelper", "FeatureDialogHelper layerID = " + layerId);
		
		try{
			this.mapListener = (Map.MapChangeListener) activity;
			this.threadPoolSupplier = (HasThreadPool) activity;
		} catch(ClassCastException e){
			e.printStackTrace();
			throw new ClassCastException(activity.toString() 
					+ " must be an instance of Map.MapChangeListener");
		}
		
		this.builder = new FeatureDialogBuilder(activity,
				view, feature, startInEditMode, util);
		
		builder.build(startInEditMode);
		
		if(startInEditMode && editButton != null
				&& editOnMapButton != null){
			
			startEditMode(editButton, editOnMapButton,
					cancelButton, deleteButton);
		}
	}
	
	private void toggleCancelButton(boolean editMode, Button cancelButton){
		String text;
		Resources resources = activity.getResources();
		
		if(editMode){
			text = resources.getString(R.string.cancel);
		}else{
			text = resources.getString(R.string.back);
		}
		
		cancelButton.setText(text);
	}
	
	private void toggleDeleteButton(boolean editMode, Button deleteButton){
		if(editMode){
			deleteButton.setVisibility(View.GONE);
		}else{
			deleteButton.setVisibility(View.VISIBLE);
		}
	}
	
	private void toggleEditOnMapButton(boolean editMode, Button editButton){
		if(editMode){
			editButton.setVisibility(View.VISIBLE);
			
			if(feature.isNew()){
				
				keepEditOnMapDisabled = true;
			}
			
			editButton.setEnabled(!keepEditOnMapDisabled);
		}else{
			editButton.setVisibility(View.GONE);
		}
	}
	
	private void toggleEditButtonText(boolean editMode, Button editButton){
		String text;
		Resources resources = activity.getResources();
		
		if(editMode){
			text = resources.getString(R.string.done_editing);
		}else{
			text = resources.getString(R.string.edit_attributes);
		}
		
		editButton.setText(text);
	}
	
	private void toggleButtons(boolean editMode, Button editButton,
			Button editOnMapButton, Button cancelButton, Button deleteButton){
		
		toggleEditButtonText(editMode, editButton);
		
		toggleEditOnMapButton(editMode, editOnMapButton);
		
		toggleCancelButton(editMode, cancelButton);
		
		toggleDeleteButton(editMode, deleteButton);
	}
	
	/**
	 * Toggle the view and return whether or not it's in edit mode.
	 * @param editButton
	 * @return
	 */
	private boolean toggleEditMode(boolean editMode, Button editButton,
			Button editOnMapButton, Button cancelButton, Button deleteButton){
		
		this.editing = builder.setEditMode(editMode);
		
		toggleButtons(editMode, editButton, editOnMapButton,
				cancelButton, deleteButton);
		
		return editing;
	}
	
	public void startEditMode(Button editButton, Button editOnMapButton,
			Button cancelButton, Button deleteButton){
		
		toggleEditMode(true, editButton, editOnMapButton,
				cancelButton, deleteButton);
	}
	
	private void dismiss(){
		DialogFragment frag = (DialogFragment) activity.
				getSupportFragmentManager().findFragmentByTag(FeatureDialog.TAG);
		
		if(frag != null){
			frag.dismiss();
		}
	}
	
	/**
	 * Set the state of the app to editing
	 * the feature on the map.
	 */
	public void editOnMap(){
		ArbiterState.getArbiterState().editingFeature(feature, layerId);
		
		//mapListener.getMapChangeHelper().onEditFeature(feature);
		
		mapListener.getMapChangeHelper().setEditMode(GeometryEditor.Mode.EDIT);
		
		dismiss();
	}
	
	private SQLiteDatabase getFeatureDb(){
		Context context = activity.getApplicationContext();
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		return FeatureDatabaseHelper.getHelper(context, 
				ProjectStructure.getProjectPath(projectName), false).getWritableDatabase();
	}
	
	private String getNewMediaKey(){
		return layerId;
	}
	
	private void updateNewMedia(){
		HashMap<String, MediaPanel> mediaPanels = builder.getMediaPanels();
		MediaPanel mediaPanel = null;
		
		MediaSyncHelper helper = new MediaSyncHelper(activity);
		
		JSONObject newMedia = null;
		JSONArray mediaLayer = null;
		
		String newMediaKey = getNewMediaKey();
		
		String existingNewMedia = helper.getMediaToSend();
		boolean insert = false;
		
		if(existingNewMedia == null){
			insert = true;
			existingNewMedia = "{}";
		}
		
		try {
			newMedia = new JSONObject(existingNewMedia);
			
			if(newMedia.has(newMediaKey)){
				mediaLayer = newMedia.getJSONArray(newMediaKey);
			}else{
				mediaLayer = new JSONArray();
				newMedia.put(newMediaKey, mediaLayer);
			}
			
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<String> mediaToDelete = new ArrayList<String>();
		for(String key : mediaPanels.keySet()){
			mediaPanel = mediaPanels.get(key);
			
			ArrayList<String> mediaToSend = mediaPanel.getMediaToSend();
			mediaToDelete.addAll(mediaPanel.getMediaToDelete());
			
			for(int i = 0, count = mediaToSend.size(); i < count; i++){
				mediaLayer.put(mediaToSend.get(i));
			}
			
			mediaPanel.clearMediaToSend();
		}
		
		String newMediaString = newMedia.toString();
		
		for(int i = 0, count = mediaToDelete.size(); i < count; i++){
			String mediaElement = '\"' + mediaToDelete.get(i) + '\"';
			
			int index = newMediaString.indexOf(mediaElement);
			if (index > -1) {
				int length = mediaElement.length();
				if (newMediaString.indexOf(mediaElement + ",") == index) {
					length++;
				} else if (newMediaString.indexOf("," + mediaElement) == index - 1) {
					index--;
					length++;
				}
				newMediaString = newMediaString.substring(0, index) + newMediaString.substring(index+length);
			}
		}
		
		helper.updateMediaToSend(newMediaString);
	}
	
	private boolean save() throws Exception{
		SQLiteDatabase db = getFeatureDb();
		
		boolean insertedNewFeature = false;
		
		// Update the feature from the EditText fields
		builder.updateFeature();
		
		String featureId = null;
		
		if(feature.isNew()){
			feature.setSyncState(FeaturesHelper.SYNC_STATES.NOT_SYNCED);
			feature.setModifiedState(FeaturesHelper.MODIFIED_STATES.INSERTED);
			
			featureId = FeaturesHelper.getHelper().insert(db,
					feature.getFeatureType(), feature);
			
			if(featureId.equals("-1")){
				throw new Exception("An error occurred"
						+ " while inserting the feature.");
			}
			
			feature.setId(featureId);
			
			insertedNewFeature = true;
		}else{
			if(feature.getSyncState().equals(FeaturesHelper.SYNC_STATES.SYNCED)){
				feature.setSyncState(FeaturesHelper.SYNC_STATES.NOT_SYNCED);
				feature.setModifiedState(FeaturesHelper.MODIFIED_STATES.MODIFIED);
			}
			
			FeaturesHelper.getHelper().update(db, 
					feature.getFeatureType(), 
					feature.getId(), feature);
			
			featureId = feature.getId();
		}
		
		ControlPanelHelper controlPanelHelper = new ControlPanelHelper(activity);
		controlPanelHelper.set(featureId, layerId,
				ControlPanelHelper.CONTROLS.SELECT,
				feature.getGeometry(), null, null);
		
		// Update the mediaToSend property so that
		// we know which files need to be synced.
		updateNewMedia();
		
		return insertedNewFeature;
	}
	
	private ProgressDialog startUpdateProgress(){
		Resources resources = activity.getResources();
		
		return ProgressDialog.show(activity, 
				resources.getString(R.string.updating), 
				resources.getString(R.string.updating_msg), true);
	}
	
	private ProgressDialog startDeleteProgress(){
		Resources resources = activity.getResources();
		
		return ProgressDialog.show(activity, 
				resources.getString(R.string.delete_feature_warning), 
				resources.getString(R.string.deleting_msg), true);
	}
	
	private void saveFeature(final Button editButton, final Button editOnMapButton,
			final Button cancelButton, final Button deleteButton){
		
		final ProgressDialog progressDialog = startUpdateProgress();
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				try{
					final boolean insertedNewFeature = save();
					
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Resources resources = activity.getResources();
							
							AlertDialog.Builder builder = new AlertDialog.Builder(activity);
							
							String title = null;
							String msg = null;

							title = resources.getString(R.string.feature_saved);
							msg = resources.getString(R.string.feature_saved_msg);
							
							builder.setTitle(title);
							builder.setIcon(resources.getDrawable(R.drawable.icon));
							builder.setMessage(msg);
							builder.setNegativeButton(R.string.return_to_map, new OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which) {
									ArbiterState.getArbiterState().doneEditingFeature();
									toggleEditMode(false, editButton, editOnMapButton,
											cancelButton, deleteButton);
									
									/*if(insertedNewFeature){
										mapListener.getMapChangeHelper()
											.endInsertMode();
									}else*/{
										mapListener.getMapChangeHelper().reloadMap();
									}
									dismiss();
								}
							});
							
							builder.setPositiveButton(R.string.review_feature, new OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which) {
									toggleEditMode(false, editButton, editOnMapButton,
											cancelButton, deleteButton);
									
									/*if(insertedNewFeature){
										mapListener.getMapChangeHelper()
											.endInsertMode();
									}else*/{
										mapListener.getMapChangeHelper().reloadMap();
									}
								}
							});
							
							builder.create().show();

							progressDialog.dismiss();
						}
					});
					
				} catch (Exception e){
					e.printStackTrace();
					progressDialog.dismiss();
				}	
			}
		});
	}
	
	public boolean isEditing(){
		return this.editing;
	}
	
	/**
	 * Done in edit mode.
	 * @param save
	 */
	public void endEditMode(Button editButton, Button editOnMapButton,
			Button cancelButton, Button deleteButton){
		
		if(builder.checkFormValidity()){
			saveFeature(editButton, editOnMapButton, cancelButton, deleteButton);
		}else{
			AlertDialog.Builder errorBuilder = new AlertDialog.Builder(activity);
			
			errorBuilder.setIcon(R.drawable.icon);
			errorBuilder.setTitle(R.string.check_errors);
			errorBuilder.setMessage(R.string.check_errors_msg);
			
			errorBuilder.create().show();
		}
	}
	
	public void unselect(){
		mapListener.getMapChangeHelper().unselect();
	}
	
	public void back(){
		dismiss();
	}
	
	public void removeFeature(){
		displayDeleteAlert();
	}
	
	public void cancel(){
		Log.w("FeatureDialogHelper", "FeatureDialogHelper cancel");
		
		threadPoolSupplier.getThreadPool().execute(new Runnable(){
			@Override
			public void run(){
				
				ControlPanelHelper cpHelper = new ControlPanelHelper(activity);
				cpHelper.clearControlPanel();
				
				ArbiterState.getArbiterState().doneEditingFeature();
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						Log.w("FeatureDialogHelper", "FeatureDialogHelper reloadMap");
						
						//if(feature.isNew()){
							mapListener.getMapChangeHelper().reloadMap();
							mapListener.getMapChangeHelper().setEditMode(GeometryEditor.Mode.OFF);
						//}
						
						dismiss();
					}
				});
			}
		});
	}
	
	private void deleteFeature(){
		SQLiteDatabase db = getFeatureDb();
		
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
		
		ControlPanelHelper cpHelper = new ControlPanelHelper(activity);
		cpHelper.clearControlPanel();
	}
	
	private void displayDeleteAlert(){
		Resources resources = activity.getResources();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		String title = resources.getString(R.string.delete_feature_warning);
		String msg = resources.getString(R.string.delete_feature_warning_msg);
		
		builder.setTitle(title);
		builder.setIcon(resources.getDrawable(R.drawable.icon));
		builder.setMessage(msg);
		builder.setPositiveButton(R.string.delete_feature, new OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				final ProgressDialog deleteProgress = startDeleteProgress();
				
				CommandExecutor.runProcess(new Runnable(){
					@Override
					public void run(){
						
						deleteFeature();
						
						activity.runOnUiThread(new Runnable(){
							@Override
							public void run(){
								
								mapListener.getMapChangeHelper().reloadMap();
								
								mapListener.getMapChangeHelper().setEditMode(GeometryEditor.Mode.OFF);
								
								dismiss();
								
								deleteProgress.dismiss();
							}
						});
					}
				});
			}
		});
		
		builder.setNegativeButton(android.R.string.no, null);
		
		builder.create().show();
	}
	
	public void updateFeaturesMedia(String key, String media, String newMedia){
		this.builder.updateFeaturesMedia(key, media, newMedia);
	}
}
