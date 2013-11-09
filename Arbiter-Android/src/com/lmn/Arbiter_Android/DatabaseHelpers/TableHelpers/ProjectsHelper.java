package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.Loaders.ProjectsListLoader;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class ProjectsHelper implements BaseColumns{
	public static final String PROJECT_NAME = "name";
	public static final String PROJECTS_TABLE_NAME = "projects";
	public static final String PROJECT_AOI = "aoi";
	public static final String INCLUDE_DEFAULT_LAYER = "default_layer";
	public static final String DEFAULT_LAYER_VISIBILITY = "default_layer_visibility";
	
	private ProjectsHelper(){}
	
	private static ProjectsHelper helper = null;
	
	public static ProjectsHelper getProjectsHelper(){
		if(helper == null){
			helper = new ProjectsHelper();
		}
		
		return helper;
	}
	
	public void createTable(SQLiteDatabase db){
		String sql = "CREATE TABLE " + PROJECTS_TABLE_NAME + " (" +
					_ID +
					" INTEGER PRIMARY KEY AUTOINCREMENT, " + 
					PROJECT_NAME + " TEXT, " +
					PROJECT_AOI + " TEXT, " +
					DEFAULT_LAYER_VISIBILITY + " BOOLEAN, " +
					INCLUDE_DEFAULT_LAYER + " BOOLEAN);";
		
		Log.w("PROJECTSHELPER", "PROJECTSHELPER : " + sql);
		db.execSQL(sql);
	}
	
	public Project[] getAll(SQLiteDatabase db){
		// Projection - columns to get back
		String[] columns = {
				_ID, // 0
				PROJECT_NAME, // 1
				PROJECT_AOI, // 2
				INCLUDE_DEFAULT_LAYER, // 3
				DEFAULT_LAYER_VISIBILITY // 4
		};
		
		// How to sort the results
		String orderBy = ProjectsHelper.PROJECT_NAME + " COLLATE NOCASE";
		
		Cursor cursor =  db.query(PROJECTS_TABLE_NAME, columns, 
				null, null, null, null, orderBy);
		
		Project[] projects = new Project[cursor.getCount()];
		
		int i = 0;
		//Traverse the cursors to populate the projects array
		for(cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			projects[i] = new Project(cursor.getInt(0), 
					cursor.getString(1), cursor.getString(2), 
					cursor.getInt(3), cursor.getInt(4));
			i++;
		}
		
		cursor.close();
		
		return projects;
	}
	
	public long[] insert(SQLiteDatabase appDb, Context context, Project newProject){
		
		appDb.beginTransaction();
		long[] projectId = new long[1];
		
		try {
			ContentValues values = new ContentValues();
			values.put(PROJECT_NAME, newProject.getProjectName());
			values.put(PROJECT_AOI, newProject.getAOI());
			values.put(INCLUDE_DEFAULT_LAYER, newProject.includeDefaultLayer());
			values.put(DEFAULT_LAYER_VISIBILITY, newProject.getDefaultLayerVisibility());
			
			projectId[0] = appDb.insert(PROJECTS_TABLE_NAME, null, values);
			
			// If the project successfully inserted,
			// insert the layers with the projects id
			if(projectId[0] != -1){
				ProjectDatabaseHelper helper = ProjectDatabaseHelper.getHelper(context, ProjectStructure
						.getProjectPath(context, newProject.getProjectName()));
				
				LayersHelper.getLayersHelper().insert(helper.
						getWritableDatabase(), context, newProject.getLayers());
				
				appDb.setTransactionSuccessful();
				
				ArbiterProject.getArbiterProject().setOpenProject(context, 
						projectId[0], newProject.getProjectName(), newProject.includeDefaultLayer());
				
				LocalBroadcastManager.getInstance(context).
					sendBroadcast(new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			appDb.endTransaction();
		}
		
		return projectId;
	}

	public void delete(SQLiteDatabase db, Activity activity, Project project, Runnable callback) {
		db.beginTransaction();
		
		boolean successful = true;
		
		try {
			
			String whereClause = ProjectsHelper._ID + "=?";
			String[] whereArgs = {
					Long.toString(project.getId())	
			};
			
			db.delete(PROJECTS_TABLE_NAME, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(activity.getApplicationContext())
				.sendBroadcast(new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
		} catch (Exception e){
			successful = false;
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
		
		if(successful){
			if(callback != null){
				callback.run();
			}
		}
	}
	
	//TODO NEED TO FIX THE AOI THATS INSERTED INTO THE DEFAULT PROJECT
	public long ensureProjectExists(SQLiteDatabase appDb, Context context){
		appDb.beginTransaction();
		
		long[] projectId = {-1};
		
		try {
	    	projectId = insert(appDb, context, 
	    		new Project(-1, context.getResources().getString(
	    			R.string.default_project_name), "", true, true));
	    	
			appDb.setTransactionSuccessful();
			
			LocalBroadcastManager.getInstance(context)
				.sendBroadcast(new Intent(ProjectsListLoader.PROJECT_LIST_UPDATED));
		} catch (Exception e){
			
		} finally {
			appDb.endTransaction();
		}
    	
    	return projectId[0];
    }
	
	public String getProjectAOI(SQLiteDatabase db, Context context, long projectId){
		String[] columns = {
			PROJECT_AOI	// 0
		};
		
		String where = _ID + "=?";
		String[] whereArgs = {
			Long.toString(projectId)	
		};
		
		Cursor cursor = db.query(PROJECTS_TABLE_NAME, columns, where, whereArgs, null, null, null);
		boolean hasResult = cursor.moveToFirst();
		String aoi = "";
		
		if(hasResult){
			aoi = cursor.getString(0);
		}
		
		cursor.close();
		
		return aoi;
	}
	
	public boolean[] getIncludeDefaultLayer(SQLiteDatabase db, Context context, long projectId){
		String[] columns = {
			INCLUDE_DEFAULT_LAYER, // 0
			DEFAULT_LAYER_VISIBILITY // 1
		};
		
		String where = _ID + "=?";
		String[] whereArgs = {
			Long.toString(projectId)
		};
		
		Cursor cursor = db.query(PROJECTS_TABLE_NAME, columns, where, whereArgs, null, null, null);
		boolean hasResult = cursor.moveToFirst();
		boolean[] defaultLayerInfo = null;
		
		if(hasResult){
			defaultLayerInfo = new boolean[2];
			defaultLayerInfo[0] = Util.convertIntToBoolean(cursor.getInt(0));
			defaultLayerInfo[1] = Util.convertIntToBoolean(cursor.getInt(1));
		}
		
		cursor.close();
		
		return defaultLayerInfo;
	}
	
	public void setIncludeDefaultLayer(SQLiteDatabase db, Context context, 
			long projectId, boolean includeDefaultLayer, Runnable callback){
		
		db.beginTransaction();
		
		try {
			
			String whereClause = ProjectsHelper._ID + "=?";
			String[] whereArgs = {
					Long.toString(projectId)	
			};
			
			ContentValues values = new ContentValues();
			
			values.put(INCLUDE_DEFAULT_LAYER, (includeDefaultLayer) ? 1 : 0);
			
			db.update(PROJECTS_TABLE_NAME, values, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
			
			if(callback != null){
				callback.run();
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	
	public void setDefaultLayerVisibility(SQLiteDatabase db, Context context,
			long projectId, boolean defaultLayerVisibility, Runnable callback){
		
		db.beginTransaction();
		
		try {
			
			String whereClause = ProjectsHelper._ID + "=?";
			String[] whereArgs = {
					Long.toString(projectId)	
			};
			
			ContentValues values = new ContentValues();
			
			values.put(DEFAULT_LAYER_VISIBILITY, (defaultLayerVisibility) ? 1 : 0);
			
			db.update(PROJECTS_TABLE_NAME, values, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
			
			if(callback != null){
				callback.run();
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	
	public void updateProjectAttributes(SQLiteDatabase db, Context context,
			long projectId, ContentValues values, Runnable callback){
		db.beginTransaction();
		
		try {
			String whereClause = _ID + "=?";
			String[] whereArgs = {
				Long.toString(projectId)
			};
			
			db.update(PROJECTS_TABLE_NAME, values, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
			
			if(callback != null){
				callback.run();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
	
	public void setProjectsAOI(SQLiteDatabase db, Context context, 
			long projectId, String aoi, Runnable callback){
		
		db.beginTransaction();
		
		try {
			
			String whereClause = ProjectsHelper._ID + "=?";
			String[] whereArgs = {
					Long.toString(projectId)	
			};
			
			ContentValues values = new ContentValues();
			
			values.put(PROJECT_AOI, aoi);
			
			db.update(PROJECTS_TABLE_NAME, values, whereClause, whereArgs);
			
			db.setTransactionSuccessful();
			
			if(callback != null){
				callback.run();
			}
		} catch (Exception e){
			e.printStackTrace();
		} finally {
			db.endTransaction();
		}
	}
}
