package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class FeatureDialog extends ArbiterDialogFragment{
	private Feature feature;
	private String layerId;
	private FeatureDialogHelper helper;
	private boolean startInEditMode;
	private Button editOnMapButton;
	private Button editButton;
	private Button deleteButton;
	private Button cancelButton;
	
	public static String TAG = "FeatureDialog";
	
	public static FeatureDialog newInstance(String title, int layout, 
			Feature feature, String layerId, boolean startInEditMode){
		
		FeatureDialog frag = new FeatureDialog();
		
		frag.setTitle(title);
		frag.setLayout(layout);
		frag.startInEditMode = startInEditMode;
		frag.feature = feature;
		frag.layerId = layerId;
		
		return frag;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
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
			helper.cancel();
		}else{
			helper.unselect();
		}
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
				cancelButton, deleteButton, layerId);
	}
	
	private void registerListeners(View view){
		cancelButton = (Button) view.findViewById(R.id.cancelButton);
		
		if(cancelButton != null){
			cancelButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					onNegativeClick();
					
					if(feature.isNew()){
						
					}
					
					helper.back();
				}
			});
		}
		
		editOnMapButton = (Button) view.findViewById(R.id.editFeature);
		
		if(editOnMapButton != null){
			editOnMapButton.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					helper.editOnMap();
				}
			});
		}
		
		editButton = (Button) view.findViewById(R.id.editButton);
		
		if(editButton != null){
			editButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					boolean editing = helper.isEditing();
					if(!editing){
						helper.startEditMode(editButton, 
								editOnMapButton, cancelButton,
								deleteButton);
					}else{
						helper.endEditMode(editButton,
								editOnMapButton, cancelButton,
								deleteButton);
					}
				}
			});
		}
		
		deleteButton = (Button) view.findViewById(R.id.deleteButton);
		
		if(deleteButton != null){
			deleteButton.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					helper.removeFeature();
				}
			});
		}
	}
	
	public void updateFeaturesMedia(String key, String media, String newMedia){
		helper.updateFeaturesMedia(key, media, newMedia);
	}
}
