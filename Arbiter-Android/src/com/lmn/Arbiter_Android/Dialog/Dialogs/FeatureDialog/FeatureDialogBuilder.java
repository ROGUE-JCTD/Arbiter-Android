package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.GeometryColumnsHelper;
import com.lmn.Arbiter_Android.Media.MediaHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class FeatureDialogBuilder {
	private Activity activity;
	private FragmentActivity fragActivity;
	private Context context;
	private HasThreadPool threadPoolSupplier;
	private AttributeHelper attributeHelper;
	private Feature feature;
	private LayoutInflater inflater;
	private LinearLayout outerLayout;
	private HashMap<String, MediaPanel> mediaPanels;
	private JSONObject enumeration;
	private NillableHelper nillableHelper;
	
	public FeatureDialogBuilder(Activity activity, View view,
			Feature feature, boolean startInEditMode, Util util){
		
		this.activity = activity;
		this.context = activity.getApplicationContext();
		
		try{
			this.threadPoolSupplier = (HasThreadPool) activity;
			this.fragActivity = (FragmentActivity) activity;
		} catch(ClassCastException e){
			e.printStackTrace();
		}
		
		this.feature = feature;
		
		this.attributeHelper = new AttributeHelper(feature, util);
		
		this.inflater = activity.getLayoutInflater();
		this.outerLayout = (LinearLayout) view.findViewById(R.id.outerLayout);
		
		this.mediaPanels = new HashMap<String, MediaPanel>();
		
		this.nillableHelper = null;
	}
	
	private SQLiteDatabase getDb(){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		String path = ProjectStructure.getProjectPath(projectName);
		
		return FeatureDatabaseHelper.getHelper(context,
				path, false).getWritableDatabase();
	}
	
	public void build(final boolean startInEditMode){
		String title = activity.getResources().getString(R.string.loading);
		String message = activity.getResources().getString(R.string.loading_feature_info);
		
		final ProgressDialog getEnumerationProgress = 
				ProgressDialog.show(activity, title, message, true);
		
		threadPoolSupplier.getThreadPool().execute(new Runnable(){
			@Override
			public void run(){
				
				SQLiteDatabase db = getDb();
				
				String _enumeration = GeometryColumnsHelper.getHelper()
						.getEnumeration(db, feature.getFeatureType());
				
				nillableHelper = GeometryColumnsHelper.getHelper().checkIfNillable(db, feature.getFeatureType());
				
				try {
					
					enumeration = new JSONObject(_enumeration);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						appendAttributes(startInEditMode);
						
						getEnumerationProgress.dismiss();
					}
				});
			}
		});
	}
	
	private void appendAttributes(boolean startInEditMode){
		String geometryName = feature.getGeometryName();
		
		String key = null;
		String value = null;
		
		LinkedHashMap<String, String> attributes = feature.getAttributes();
		
		for(Map.Entry<String, String> entry: attributes.entrySet()){
			
			key = entry.getKey();
			value = entry.getValue();
			
			if(!key.equals(FeaturesHelper.SYNC_STATE) 
					&& !key.equals(FeaturesHelper.MODIFIED_STATE) 
					&& !key.equals(FeaturesHelper.FID)){
				
				if(key.equals(MediaHelper.MEDIA) || key.equals(MediaHelper.FOTOS)){
					try {
						
						MediaPanel panel = new MediaPanel(key, activity, feature, nillableHelper.isNillable(key),
								this.outerLayout, this.inflater);
						
						panel.appendMedia(value, startInEditMode);
						
						mediaPanels.put(key, panel);
					} catch (JSONException e) {
						Log.e("FeatureDialogBuilder", "FeatureDialogBuilder.build() could not parse media json");
						e.printStackTrace();
					}
				}else if(key.equals(geometryName)){
					appendGeometry(key, value);
				}else{
					appendAttribute(key, value, startInEditMode);
				}
			}
		}
	}
	
	private void appendAttribute(String key, String value, boolean startInEditMode){
		JSONObject enumeration = null;
		
		try{
			enumeration = getEnumeration(key);
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		EnumerationHelper enumHelper = new EnumerationHelper(activity, enumeration);
		
		if(enumHelper.hasEnumeration()){
			try {
				ArrayAdapter<String> adapter = enumHelper.getSpinnerAdapter();
				
				appendDropDown(key, adapter, value,
						enumHelper, startInEditMode);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			appendEditText(key, value, startInEditMode, enumHelper);
		}
	}
	
	private void appendGeometry(String key, String value){
		View attributeView = inflater.inflate(R.layout.feature_attribute, null);
		boolean isNillable = true;
		
		if(key != null){
			
			isNillable = nillableHelper.isNillable(key);
			
			TextView attributeLabel = (TextView) attributeView.findViewById(R.id.attributeLabel);
			
			if(attributeLabel != null){
				
				String label = key;
				
				if(!isNillable){
					label += "*";
				}
				
				attributeLabel.setText(label);
			}
		}
		
		EditText attributeValue = (EditText) attributeView.findViewById(R.id.attributeText);
		
		if(attributeValue != null){
			
			attributeValue.setText(value);
			attributeHelper.add(fragActivity, key, attributeValue,
					null, isNillable, false, value);
		}
		
		outerLayout.addView(attributeView);
	}
	
	private Spinner appendDropDown(String key, ArrayAdapter<String> adapter, String value,
			EnumerationHelper enumHelper, boolean startInEditMode) throws JSONException{
		
		LinearLayout layout = (LinearLayout) inflater
				.inflate(R.layout.feature_dropdown, null);
		
		Spinner dropdown = (Spinner) layout.findViewById(R.id.spinner);
		
		dropdown.setAdapter(adapter);
	
		int position = adapter.getPosition(value);
		
		dropdown.setSelection(position);
		
		EditText errorEditText = (EditText) layout.findViewById(R.id.errorEditText);
		
		boolean isNillable = nillableHelper.isNillable(key);
		
		setLabel(layout, key, isNillable);
		
		outerLayout.addView(layout);
		
		attributeHelper.add(fragActivity, key, dropdown, errorEditText,
				enumHelper, isNillable, startInEditMode);
		
		return dropdown;
	}
	
	private void appendEditText(String key, String value, boolean startInEditMode, EnumerationHelper enumHelper){
		View attributeView = inflater.inflate(R.layout.feature_attribute, null);
		
		boolean isNillable = nillableHelper.isNillable(key);
		
		setLabel(attributeView, key, isNillable);
		
		final EditText attributeValue = (EditText) attributeView.findViewById(R.id.attributeText);
		
		if(attributeValue != null){
			
			attributeHelper.add(fragActivity, key, attributeValue,
					enumHelper, isNillable, startInEditMode, value);
		}
		
		outerLayout.addView(attributeView);
	}
	
	private JSONObject getEnumeration(String key) throws JSONException{
		JSONObject _enumeration = null;
		
		if(enumeration != null && enumeration.has(key)){
			_enumeration = enumeration.getJSONObject(key);
		}
		
		return _enumeration;
	}
	
	private void setLabel(View layout, String key, boolean isNillable){
		if(key != null){
			TextView attributeLabel = (TextView) layout.findViewById(R.id.attributeLabel);
			
			if(attributeLabel != null){
				
				String label = key;
				
				if(!isNillable){
					label += "*";
				}
				
				attributeLabel.setText(label);
			}
		}
	}
	
	private void toggleMediaPanels(boolean editMode){
		MediaPanel panel = null;
		
		for(String key : mediaPanels.keySet()){
			panel = mediaPanels.get(key);
			
			//panel.toggleEditMode();
			panel.setEditMode(editMode);
		}
	}
	
	public boolean setEditMode(boolean editMode){
		boolean _editMode = attributeHelper.setEditMode(editMode);
		
		Log.w("FeatureDialogBuilder", "FeatureDialogBuilder editMode = " + editMode);
		toggleMediaPanels(editMode);
		
		return _editMode;
	}
	
	public boolean checkFormValidity(){
		boolean formValidity = attributeHelper.checkFormValidity();
		
		MediaPanel panel = null;
		boolean panelValidity = true;
		
		for(String key : mediaPanels.keySet()){
			
			panel = mediaPanels.get(key);
			
			panelValidity = panel.checkValidity();
			
			if(!panelValidity){
				formValidity = false;
			}
		}
		
		return formValidity;
	}
	
	public void updateFeature(){
		attributeHelper.updateFeature();
	}
	
	public void updateFeaturesMedia(final String key, final String media, final String newMedia){
		activity.runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				LinkedHashMap<String, String> attributes = feature.getAttributes();
				
				attributes.put(key, media);
				
				MediaPanel panel = mediaPanels.get(key);
				
				try {
					
					// Add new media to the arrayList of
					// media to be synced.  This won't
					// get saved until the feature is saved.
					panel.addMediaToSend(newMedia);
					
					panel.loadMedia();
					
					panel.checkValidity();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}
	
	public HashMap<String, MediaPanel> getMediaPanels(){
		return mediaPanels;
	}
}
