package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

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

public class FeatureDialogHelper {
	private FragmentActivity activity;
	private Map.MapChangeListener mapListener;
	
	private Feature feature;
	private FeatureDialogBuilder builder;
	private boolean editing;
	
	public FeatureDialogHelper(FragmentActivity activity, View view, 
			Feature feature, boolean startInEditMode,
			Button editButton, Button editOnMapButton){
		
		this.activity = activity;
		this.feature = feature;
		this.editing = false;
		
		try{
			this.mapListener = (Map.MapChangeListener) activity;
		} catch(ClassCastException e){
			e.printStackTrace();
			throw new ClassCastException(activity.toString() 
					+ " must be an instance of Map.MapChangeListener");
		}
		
		this.builder = new FeatureDialogBuilder(activity, view, feature);
		builder.build();
		
		if(startInEditMode && editButton != null
				&& editOnMapButton != null){
			
			startEditMode(editButton, editOnMapButton);
		}
	}
	
	private void toggleEditOnMapButton(Button editButton){
		if(editing){
			editButton.setVisibility(View.VISIBLE);
		}else{
			editButton.setVisibility(View.GONE);
		}
	}
	
	private void toggleEditButtonText(Button editButton){
		String text;
		Resources resources = activity.getResources();
		
		if(editing){
			text = resources.getString(R.string.done_editing);
		}else{
			text = resources.getString(R.string.edit_attributes);
		}
		
		editButton.setText(text);
	}
	
	/**
	 * Toggle the view and return whether or not it's in edit mode.
	 * @param editButton
	 * @param editOnMapButton
	 * @return
	 */
	private boolean toggleEditMode(Button editButton, Button editOnMapButton){
		this.editing = builder.toggleEditTexts();
		
		toggleEditButtonText(editButton);
		
		toggleEditOnMapButton(editOnMapButton);
		
		return editing;
	}
	
	public void startEditMode(Button editButton, Button editOnMapButton){
		toggleEditMode(editButton, editOnMapButton);
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
		ArbiterState.getArbiterState().editingFeature(feature);
		
		mapListener.onEditFeature(feature);
		
		dismiss();
	}
	
	private SQLiteDatabase getDb(){
		Context context = activity.getApplicationContext();
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		return FeatureDatabaseHelper.getHelper(context, 
				ProjectStructure.getProjectPath(context, 
						projectName)).getWritableDatabase();
	}
	
	private void save(){
		SQLiteDatabase db = getDb();
		
		builder.updateFeature();
		
		FeaturesHelper.getHelper().update(db, 
				feature.getFeatureType(), 
				feature.getId(), feature);
	}
	
	private ProgressDialog startUpdateProgress(){
		Resources resources = activity.getResources();
		
		return ProgressDialog.show(activity, 
				resources.getString(R.string.updating), 
				resources.getString(R.string.updating_msg), true);
	}
	
	private void areYouSure(final Button editButton, final Button editOnMapButton){
		Resources resources = activity.getResources();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(resources.getString(R.string.update_feature_title));
		builder.setIcon(resources.getDrawable(R.drawable.icon));
		builder.setMessage(resources.getString(R.string.update_feature_msg));
		builder.setPositiveButton(android.R.string.yes, new OnClickListener(){
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				final ProgressDialog progressDialog = startUpdateProgress();
				
				CommandExecutor.runProcess(new Runnable(){
					@Override
					public void run(){
						try{
							save();
							
							activity.runOnUiThread(new Runnable(){
								@Override
								public void run(){
									toggleEditMode(editButton, editOnMapButton);
								}
							});
						} catch (Exception e){
							e.printStackTrace();
						} finally {
							ArbiterState.getArbiterState().doneEditingFeature();
							progressDialog.dismiss();
						}	
					}
				});
			}
		});
		
		builder.setNegativeButton(android.R.string.no, null);
		
		builder.create().show();
	}
	
	public boolean isEditing(){
		return this.editing;
	}
	
	/**
	 * Done in edit mode.
	 * @param save
	 */
	public void endEditMode(Button editButton, Button editOnMapButton){
		areYouSure(editButton, editOnMapButton);
	}
	
	public void unselect(){
		mapListener.unselect();
	}
	
	public void cancel(){
		mapListener.cancelEditing();
		
		dismiss();
	}
}
