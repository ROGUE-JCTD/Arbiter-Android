package com.lmn.Arbiter_Android.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.PreferencesHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class IncompleteProjectHelper {
	private MenuItem insertButton;
	private ImageButton syncButton;
	
	private Context context;
	private Activity activity;
	private HasThreadPool threadPoolSupplier;
	
	public IncompleteProjectHelper(Activity activity){
		this.context = activity.getApplicationContext();
		this.activity = activity;
		
		try{
			this.threadPoolSupplier = (HasThreadPool) this.activity;
		}catch(ClassCastException e){
			e.printStackTrace();
			
			throw new ClassCastException(activity.toString() 
					+ " must implement HasThreadPool");
		}
	}
	
	public void toggleComplete(boolean complete){    	
    	insertButton.setEnabled(complete);
    		
    	syncButton.setEnabled(complete);
	}
	
	private String getProjectPath(){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(activity);
		
		return ProjectStructure.getProjectPath(projectName);
	}
	
	private SQLiteDatabase getProjectDatabase(){
		return ProjectDatabaseHelper.getHelper(context,
				getProjectPath(), false).getWritableDatabase();
	}
	
	private String getAOI(){
		return PreferencesHelper.getHelper().get(getProjectDatabase(),
				context, ArbiterProject.AOI);
	}
	
	private ProgressDialog showProgressDialog(){
		String title = context.getResources().getString(R.string.initializing);
		String message = context.getResources().getString(R.string.please_wait);
		
		return ProgressDialog.show(activity, title,
				message, true);
	}
	
	public void checkForAOI(){
		if(insertButton == null || syncButton == null){
			
			return;
		}
		
		final ProgressDialog checkAOIProgress = showProgressDialog();
		
		threadPoolSupplier.getThreadPool().execute(new Runnable(){
			@Override
			public void run(){
				
				final String aoi = getAOI();
				
				Log.w("IncompleteProjectHelper", "IncompleteProjectHelper aoi = " + aoi);
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						if(aoi == null || aoi.equals("")){
							Log.w("IncompleteProjectHelper", "IncompleteProjectHelper aoi isn't set!");
							toggleComplete(false);
						}else{
							toggleComplete(true);
						}
						
						checkAOIProgress.dismiss();
					}
				});
			}
		});
	}
	
	public void setInsertButton(Menu menu){
		this.insertButton = menu.getItem(0);
		
		checkForAOI();
	}
	
	public void setSyncButton(ImageButton syncButton){
		this.syncButton = syncButton;
		
		checkForAOI();
	}
}
