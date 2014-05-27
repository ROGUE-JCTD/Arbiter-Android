package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class ControlPanelHelper {
	public static final String ACTIVE_CONTROL = "cp_active_control";
	
	// The layer id of the feature selected or being modified
	public static final String LAYER_ID = "cp_layer_id";
	
	// The arbiter_id of the feature selected or being modified.
	public static final String FEATURE_ID = "cp_feature_id";
	
	public static final String GEOMETRY = "cp_geometry";
	
	public static final String GEOMETRY_TYPE = "cp_geometry_type";
	
	public static final String INDEX_CHAIN = "cp_index_chain";
	
	public static class CONTROLS {
		public static final String NONE = "0";
		public static final String SELECT = "1";
		public static final String MODIFY = "2";
		public static final String INSERT = "3";
	}

	private Activity activity;
	private Context context;
	
	public ControlPanelHelper(Activity activity){
		this.activity = activity;
		this.context = activity.getApplicationContext();
	}
	
	private SQLiteDatabase getProjectDb(){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		return ProjectDatabaseHelper.getHelper(context,
				ProjectStructure.getProjectPath(projectName), false).getWritableDatabase();
	}
	
	public void clearControlPanel(){
		SQLiteDatabase projectDb = getProjectDb();
		
		PreferencesHelper helper = PreferencesHelper.getHelper();
		
		helper.put(projectDb, context, ACTIVE_CONTROL,
				ControlPanelHelper.CONTROLS.NONE);
		helper.put(projectDb, context, LAYER_ID, "0");
		helper.put(projectDb, context, FEATURE_ID, "0");
		helper.put(projectDb, context, GEOMETRY, "0");
		helper.put(projectDb, context, GEOMETRY_TYPE, null);
		helper.put(projectDb, context, INDEX_CHAIN, null);
	}
	
	public void setFeatureId(String featureId){
		
		SQLiteDatabase projectDb = getProjectDb();
		
		PreferencesHelper.getHelper().put(projectDb, context, FEATURE_ID, featureId);
	}
	
	public void set(String featureId, String layerId, String control, 
			String geometry, String geometryType, String indexChain){
		
		SQLiteDatabase projectDb = getProjectDb();
		
		PreferencesHelper.getHelper().put(projectDb, context, ACTIVE_CONTROL, control);
		
		PreferencesHelper.getHelper().put(projectDb, context, LAYER_ID, layerId);
		
		PreferencesHelper.getHelper().put(projectDb, context, FEATURE_ID, featureId);
		
		PreferencesHelper.getHelper().put(projectDb, context, GEOMETRY, geometry);
		
		PreferencesHelper.getHelper().put(projectDb, context, GEOMETRY_TYPE, geometryType);
		
		PreferencesHelper.getHelper().put(projectDb, context, INDEX_CHAIN, indexChain);
	}
}
