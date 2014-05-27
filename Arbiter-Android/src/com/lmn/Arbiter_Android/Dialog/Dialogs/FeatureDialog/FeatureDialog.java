package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FeatureDialog extends ArbiterDialogFragment{
	private Feature feature;
	private String layerId;
	private FeatureDialogHelper helper;
	private boolean startInEditMode;
	private Button editButton;
	private Button editOnMapButton;
	private Button deleteButton;
	private Button cancelButton;
	private boolean isReadOnly;
	
	private static FeatureDialog dialog = null;
	
	public static String TAG = "FeatureDialog";
	
	public static FeatureDialog newInstance(String title, int layout, 
			Feature feature, String layerId, 
			boolean startInEditMode, boolean isReadOnly){
		
		if (dialog != null) {
			return null;
		}
		
		dialog = new FeatureDialog();
		
		Log.w(TAG, TAG + " layerId = " + layerId);
		
		dialog.setTitle(title);
		dialog.setLayout(layout);
		dialog.startInEditMode = startInEditMode;
		dialog.feature = feature;
		dialog.layerId = layerId;
		dialog.isReadOnly = isReadOnly;
		
		return dialog;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		dialog = null;
	}
	
	@Override
	public void onCancel(DialogInterface dialog){
		onNegativeClick();
	}
	
	@Override
	public void onPositiveClick() {
		
	}
	
	@Override
	public void onNegativeClick() {
		
		if(helper.isEditing()){
			Log.w(TAG, TAG + ".onNegativeClick isEditing cancel");
			helper.cancel();
		}/*else{
			
			Log.w(TAG, TAG + ".onNegativeClick not editing unselect");
			helper.unselect();
		}*/
	}

	@Override
	public void beforeCreateDialog(View view) {
		if(view != null){
			registerListeners(view);
			populateView(view);
		}
	}
	
	private void populateView(View view){
		this.helper = new FeatureDialogHelper(getActivity(), 
				view, feature, startInEditMode,
				editButton, editOnMapButton,
				cancelButton, deleteButton, layerId, new Util());
	}
	
	private void registerListeners(View view){
		cancelButton = (Button) view.findViewById(R.id.cancelButton);
		
		if(cancelButton != null){
			cancelButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					onNegativeClick();
					
					if(feature.isNew()){
						
						ArbiterState.getArbiterState().doneEditingFeature();
					}
					
					helper.back();
				}
			});
		}
		
		editOnMapButton = (Button) view.findViewById(R.id.editFeature);
		
		Log.w("FeatureDialog", "FeatureDialog isReadOnly = " + isReadOnly);
		
		if(editOnMapButton != null){
			editOnMapButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					
					if(!isReadOnly){
						helper.editOnMap();
					}else{
						displayIsReadOnly();
					}
				}
			});
		}
		
		editButton = (Button) view.findViewById(R.id.editButton);
		
		if(editButton != null){
			editButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					if(!isReadOnly){
						
						boolean editing = helper.isEditing();
						if(!editing){
							helper.startEditMode(editButton, editOnMapButton,
									cancelButton, deleteButton);
						}else{
							helper.endEditMode(editButton, editOnMapButton,
									cancelButton, deleteButton);
						}
					}else{
						displayIsReadOnly();
					}
				}
			});
		}
		
		deleteButton = (Button) view.findViewById(R.id.deleteButton);
		
		if(deleteButton != null){
			deleteButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					if(!isReadOnly){
						helper.removeFeature();
					}else{
						displayIsReadOnly();
					}
				}
			});
		}
	}
	
	private void displayIsReadOnly(){
		
		Activity activity = getActivity();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.read_only);
		
		builder.setMessage(R.string.read_only_msg);
		
		builder.setPositiveButton(R.string.close, null);
		
		builder.create().show();
	}
	
	public void updateFeaturesMedia(String key, String media, String newMedia){
		helper.updateFeaturesMedia(key, media, newMedia);
	}
}
