package com.lmn.Arbiter_Android.Settings;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class Settings {
	private Activity activity;
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	
	private boolean disableWMSDBValue = false;
	private boolean downloadPhotosDBValue = false;
	
	public Settings(Activity activity){
		this.activity = activity;
	}
	
	public void displaySettingsDialog(){
		
		activity.runOnUiThread(new Runnable(){
    		@Override
    		public void run(){
    			
    			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    			
    			builder.setIcon(R.drawable.icon);
    			builder.setTitle(R.string.settings);
    			
    			LayoutInflater inflater = activity.getLayoutInflater();
    			
    			final View view = inflater.inflate(R.layout.settings, null);
    			
    			populateSettings(view);
    			
    			builder.setView(view);
    			
    			builder.setPositiveButton(android.R.string.ok, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						saveSettings(view);
					}
    				
    			});
    			builder.setNegativeButton(android.R.string.cancel, null);
    			
    			builder.create().show();
    		}
    	});
	}
	
	private void populateSettings(View view){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		String path = ProjectStructure.getProjectPath(projectName);
		
		SQLiteDatabase projectDb = ProjectDatabaseHelper.getHelper(activity.getApplicationContext(), path, false).getWritableDatabase();
		CheckBox downloadPhotos = (CheckBox) view.findViewById(R.id.download_photos);
		CheckBox disableWMS = (CheckBox) view.findViewById(R.id.disable_wms);
		
		String result = PreferencesHelper.getHelper().get(projectDb, activity.getApplicationContext(), PreferencesHelper.DOWNLOAD_PHOTOS);
		if (result != null) {
			downloadPhotosDBValue = Boolean.parseBoolean(result);
		}
		downloadPhotos.setChecked(downloadPhotosDBValue);
		
		result = PreferencesHelper.getHelper().get(projectDb, activity.getApplicationContext(), PreferencesHelper.DISABLE_WMS);
		if (result != null) {
			disableWMSDBValue = Boolean.parseBoolean(result);
		}
		disableWMS.setChecked(disableWMSDBValue);
		
	}
	
	private void saveSettings(View view) {
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		String path = ProjectStructure.getProjectPath(projectName);
		
		SQLiteDatabase projectDb = ProjectDatabaseHelper.getHelper(activity.getApplicationContext(), path, false).getWritableDatabase();
		CheckBox downloadPhotos = (CheckBox) view.findViewById(R.id.download_photos);
		CheckBox disableWMS = (CheckBox) view.findViewById(R.id.disable_wms);
		
		if (downloadPhotos.isChecked() != downloadPhotosDBValue) {
			PreferencesHelper.getHelper().put(projectDb, activity.getApplicationContext(), PreferencesHelper.DOWNLOAD_PHOTOS, Boolean.toString(downloadPhotos.isChecked()));
		}
		if (disableWMS.isChecked() != disableWMSDBValue) {
			PreferencesHelper.getHelper().put(projectDb, activity.getApplicationContext(), PreferencesHelper.DISABLE_WMS, Boolean.toString(disableWMS.isChecked()));
			Map.MapChangeListener mapListener = (Map.MapChangeListener) activity;
			mapListener.getMapChangeHelper().reloadMap();
		}
	}
}
