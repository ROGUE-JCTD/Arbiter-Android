package com.lmn.Arbiter_Android.Settings;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.AOIActivity;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class Settings {
	private Activity activity;
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	
	private boolean disableWMSDBValue = false;
	private boolean downloadPhotosDBValue = false;
	private boolean noConnectionChecksDBValue = false;
	private boolean alwaysShowLocationDBValue = false;
	
	public Settings(Activity activity){
		this.activity = activity;
	}
	
	public void displaySettingsDialog(final boolean newProject){
		
		activity.runOnUiThread(new Runnable(){
    		@Override
    		public void run(){
    			
    			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    			
    			builder.setIcon(R.drawable.icon);
    			builder.setTitle(R.string.settings);
    			
    			LayoutInflater inflater = activity.getLayoutInflater();
    			
    			final View view = inflater.inflate(R.layout.settings, null);
    			
    			populateSettings(view, newProject);
    			
    			builder.setView(view);
    			
    			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveSettings(view, newProject);
						if (newProject) {
							Intent aoiIntent = new Intent(activity, AOIActivity.class);
				    		activity.startActivity(aoiIntent);
						}
					}
    				
    			});
    			builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (newProject) {
							ArbiterProject.getArbiterProject().doneCreatingProject(activity.getApplicationContext());
						}
					}
    				
    			});
    			
    			builder.create().show();
    		}
    	});
	}
	
	private void populateSettings(View view, boolean newProject){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		if (newProject) {
			
			downloadPhotosDBValue = false;
			
			disableWMSDBValue = false;
			
			noConnectionChecksDBValue = false;
			
			alwaysShowLocationDBValue = false;
		}else{
			
			String path = ProjectStructure.getProjectPath(projectName);
			
			SQLiteDatabase projectDb = ProjectDatabaseHelper.getHelper(activity.getApplicationContext(), path, false).getWritableDatabase();
			
			String result = PreferencesHelper.getHelper().get(projectDb, activity.getApplicationContext(), PreferencesHelper.DOWNLOAD_PHOTOS);
			if (result != null) {
				downloadPhotosDBValue = Boolean.parseBoolean(result);
			}
			
			result = PreferencesHelper.getHelper().get(projectDb, activity.getApplicationContext(), PreferencesHelper.DISABLE_WMS);
			if (result != null) {
				disableWMSDBValue = Boolean.parseBoolean(result);
			}
			
			result = PreferencesHelper.getHelper().get(projectDb, activity.getApplicationContext(), PreferencesHelper.NO_CON_CHECKS);
			if (result != null) {
				noConnectionChecksDBValue = Boolean.parseBoolean(result);
			}
			
			result = PreferencesHelper.getHelper().get(projectDb, activity.getApplicationContext(), PreferencesHelper.ALWAYS_SHOW_LOCATION);
			if(result != null){
				alwaysShowLocationDBValue = Boolean.parseBoolean(result);
			}
		}
		
		CheckBox downloadPhotos = (CheckBox) view.findViewById(R.id.download_photos);
		CheckBox disableWMS = (CheckBox) view.findViewById(R.id.disable_wms);
		CheckBox noConnectionChecks = (CheckBox) view.findViewById(R.id.no_con_checks);
		CheckBox alwaysShowLocation = (CheckBox) view.findViewById(R.id.always_show_location);
		
		downloadPhotos.setChecked(downloadPhotosDBValue);
		
		disableWMS.setChecked(disableWMSDBValue);
		
		noConnectionChecks.setChecked(noConnectionChecksDBValue);
		
		alwaysShowLocation.setChecked(alwaysShowLocationDBValue);
	}
	
	private void saveSettings(View view, boolean newProject) {
		
		CheckBox downloadPhotos = (CheckBox) view.findViewById(R.id.download_photos);
		CheckBox disableWMS = (CheckBox) view.findViewById(R.id.disable_wms);
		CheckBox noConnectionChecks = (CheckBox) view.findViewById(R.id.no_con_checks);
		CheckBox alwaysShowLocation = (CheckBox) view.findViewById(R.id.always_show_location);
		
		boolean downloadPhotosValue = downloadPhotos.isChecked();
		boolean disableWMSValue = disableWMS.isChecked();
		boolean noConnectionChecksValue = noConnectionChecks.isChecked();
		boolean alwaysShowLocationValue = alwaysShowLocation.isChecked();
		
		if (newProject) {
			Project project = ArbiterProject.getArbiterProject().getNewProject();
			
			project.setDownloadPhotos(Boolean.toString(downloadPhotosValue));
			project.setDisableWMS(Boolean.toString(disableWMSValue));
			project.setNoConnectionChecks(Boolean.toString(noConnectionChecksValue));
			project.setAlwaysShowLocation(Boolean.toString(alwaysShowLocationValue));
			
			// This will actually get set in the InserProjectHelper's insert method
		}else{
			
			String projectName = ArbiterProject.getArbiterProject()
					.getOpenProject(activity);
			
			String path = ProjectStructure.getProjectPath(projectName);
			
			SQLiteDatabase projectDb = ProjectDatabaseHelper.getHelper(activity.getApplicationContext(), path, false).getWritableDatabase();
			
			if (downloadPhotosValue != downloadPhotosDBValue) {
				PreferencesHelper.getHelper().put(projectDb, activity.getApplicationContext(), PreferencesHelper.DOWNLOAD_PHOTOS, Boolean.toString(downloadPhotosValue));
			}
			if (disableWMSValue != disableWMSDBValue) {
				PreferencesHelper.getHelper().put(projectDb, activity.getApplicationContext(), PreferencesHelper.DISABLE_WMS, Boolean.toString(disableWMSValue));
				
				Map.MapChangeListener mapListener = (Map.MapChangeListener) activity;
				mapListener.getMapChangeHelper().reloadMap();
			}
			if (noConnectionChecksValue != noConnectionChecksDBValue) {
				PreferencesHelper.getHelper().put(projectDb, activity.getApplicationContext(), PreferencesHelper.NO_CON_CHECKS, Boolean.toString(noConnectionChecksValue));
			}
			
			if(alwaysShowLocationValue != alwaysShowLocationDBValue){
				PreferencesHelper.getHelper().put(projectDb, activity.getApplicationContext(), PreferencesHelper.ALWAYS_SHOW_LOCATION, Boolean.toString(alwaysShowLocationValue));
			
				Map.MapChangeListener mapListener = (Map.MapChangeListener) activity;
				mapListener.getMapChangeHelper().reloadMap();
			}
		}
	}
}
