/*
 * Copyright (c) 2012-2013, Chris Brody
 * Copyright (c) 2005-2010, Nitobi Software Inc.
 * Copyright (c) 2010, IBM Corporation
 */

package org.pgsqlite;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.Number;
import java.util.HashMap;
import java.util.Collection;
import java.util.Iterator;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.MBTilesDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.*;
import android.util.Base64;
import android.util.Log;

public class SQLitePlugin extends CordovaPlugin
{
	
	///////////////////////////////////
	// kzusy changes
	//////////////////////////////////
	private static final String FEATURE_DATABASE_NAME = "featuredb";
	private static final String PROJECT_DATABASE_NAME = "projectdb";
	private static final String APPLICATION_DATABASE_NAME = "appdb";
	
	private String oldProjectName;

	/**
	 * Multiple database map (static).
	 */
	static HashMap<String, SQLiteDatabase> dbmap = new HashMap<String, SQLiteDatabase>();

	/**
	 * Get a SQLiteDatabase reference from the db map (public static accessor).
	 *
	 * @param dbname
	 *            The name of the database.
	 *
	 */
	public static SQLiteDatabase getSQLiteDatabase(String dbname)
	{
		return dbmap.get(dbname);
	}

	/**
	 * NOTE: Using default constructor, explicit constructor no longer required.
	 */

	public static void removeDBFromMap(String db) {
		Collection<SQLiteDatabase> DBValues = dbmap.values();
		Iterator<SQLiteDatabase> iter = DBValues.iterator();
		SQLiteDatabase Database = dbmap.get(db);

		while (iter.hasNext()){
			SQLiteDatabase iter_db = iter.next();
			if (iter_db.equals(Database)){
				iter.remove();
				break;
			}
		}
	}

	/**
	 * Executes the request and returns PluginResult.
	 *
	 * @param action
	 *            The action to execute.
	 *
	 * @param args
	 *            JSONArry of arguments for the plugin.
	 *
	 * @param cbc
	 *            Callback context from Cordova API
	 *
	 */
	@Override
	public boolean execute(String action, JSONArray args, CallbackContext cbc)
	{
		try {
			boolean status = true;

			if (action.equals("open")) {
				JSONObject o = args.getJSONObject(0);
				String dbname = o.getString("name");

				this.openDatabase(dbname, null);
			}
			else if (action.equals("close")) {
				JSONObject o = args.getJSONObject(0);
				String dbname = o.getString("path");

				this.closeDatabase(dbname);
			}
			else if (action.equals("delete")) {
				/* Stop & give up if API < 16: */
				if (android.os.Build.VERSION.SDK_INT < 16) return false;

				JSONObject o = args.getJSONObject(0);
				String dbname = o.getString("path");

				status = this.deleteDatabase(dbname);
			}
			else if (action.equals("executePragmaStatement"))
			{
				String dbName = args.getString(0);
				String query = args.getString(1);

				JSONArray jparams = (args.length() < 3) ? null : args.getJSONArray(2);

				String[] params = null;

				if (jparams != null) {
					params = new String[jparams.length()];

					for (int j = 0; j < jparams.length(); j++) {
						if (jparams.isNull(j))
							params[j] = "";
						else
							params[j] = jparams.getString(j);
					}
				}

				Cursor myCursor = this.getDatabase(dbName).rawQuery(query, params);

				String result = this.getRowsResultFromQuery(myCursor).getJSONArray("rows").toString();

				this.sendJavascriptCB("window.SQLitePluginCallback.p1('" + id + "', " + result + ");");
			}
			else if (action.equals("executeSqlBatch") || action.equals("executeBatchTransaction") || action.equals("backgroundExecuteSqlBatch"))
			{
				String[] 	queries 	= null;
				String[] 	queryIDs 	= null;

				JSONArray 	jsonArr 	= null;
				int 		paramLen	= 0;
				JSONArray[] 	jsonparams 	= null;

				JSONObject allargs = args.getJSONObject(0);
				JSONObject dbargs = allargs.getJSONObject("dbargs");
				String dbName = dbargs.getString("dbname");
				JSONArray txargs = allargs.getJSONArray("executes");

				if (txargs.isNull(0)) {
					queries = new String[0];
				} else {
					int len = txargs.length();
					queries = new String[len];
					queryIDs = new String[len];
					jsonparams = new JSONArray[len];

					for (int i = 0; i < len; i++)
					{
						JSONObject a	= txargs.getJSONObject(i);
						queries[i] 	= a.getString("sql");
						queryIDs[i] = a.getString("qid");
						jsonArr 	= a.getJSONArray("params");
						paramLen	= jsonArr.length();
						jsonparams[i] 	= jsonArr;
					}
				}

				boolean ex = action.equals("executeBatchTransaction");

				if (action.equals("backgroundExecuteSqlBatch"))
					this.executeSqlBatchInBackground(dbName, queries, jsonparams, queryIDs, cbc);
				else
					this.executeSqlBatch(dbName, queries, jsonparams, queryIDs, cbc);
			}

			return status;
		} catch (JSONException e) {
			// TODO: signal JSON problem to JS

			return false;
		}
	}

	/**
	 *
	 * Clean up and close all open databases.
	 *
	 */
	/*@Override
	public void onDestroy() {
		while (!dbmap.isEmpty()) {
			String dbname = dbmap.keySet().iterator().next();
			this.closeDatabase(dbname);
			dbmap.remove(dbname);
		}
	}*/

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------

	/**
	 * Open a database.
	 *
	 * @param dbname
	 *            The name of the database-NOT including its extension.
	 *
	 * @param password
	 *            The database password or null.
	 *
	 */
	private void openDatabase(String dbname, String password) {
		/*if (this.getDatabase(dbname) != null) this.closeDatabase(dbname);

		File dbfile = this.cordova.getActivity().getDatabasePath(dbname + ".db");

		if (!dbfile.exists()) {
		    dbfile.getParentFile().mkdirs();
		}

		Log.v("info", "Open sqlite db: " + dbfile.getAbsolutePath());

		SQLiteDatabase mydb = SQLiteDatabase.openOrCreateDatabase(dbfile, null);

		dbmap.put(dbname, mydb);*/

		// //////////////////////////////
		// kzusy modifications
		// //////////////////////////////

		// The database opening logic is handled with the DatabaseHelpers
		// in com.lmn.Arbiter_Android.DatabaseHelpers, so we don't need to do
		// any of the opening above.

		// Get the path to the databases from the current project
		Context context = cordova.getActivity().getApplicationContext();

		this.oldProjectName = ArbiterProject.getArbiterProject()
				.getOpenProject(cordova.getActivity());

		String path = ProjectStructure.getProjectPath(this.oldProjectName);

		// If the plugin doesn't know about the db,
		// get it for plugin use.
		if (FEATURE_DATABASE_NAME.equals(dbname)) {
			dbmap.put(dbname,
					FeatureDatabaseHelper.getHelper(context, path, false)
							.getWritableDatabase());
		} else if (PROJECT_DATABASE_NAME.equals(dbname)) {
			dbmap.put(dbname,
					ProjectDatabaseHelper.getHelper(context, path, false)
							.getWritableDatabase());
		} else if (APPLICATION_DATABASE_NAME.equals(dbname)) {
			dbmap.put(dbname, ApplicationDatabaseHelper.getHelper(context)
					.getWritableDatabase());
		} else if (dbname.endsWith(".mbtiles")) {
			// Reset Helper in case there is a new MBTiles coming in
			MBTilesDatabaseHelper DBHelper = MBTilesDatabaseHelper.getHelper(context, null);
			DBHelper.close();

			// Create a 'new' static helper
			dbmap.put(dbname, MBTilesDatabaseHelper.getHelper(context, dbname)
					.getWritableDatabase());
		} else {
			try {
				throw new Exception("SQLitePlugin: Database '" + dbname
						+ " doesn't exist!");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * Close a database.
	 *
	 * @param dbName
	 *            The name of the database-NOT including its extension.
	 *
	 */
	private void closeDatabase(String dbName)
	{
		/*SQLiteDatabase mydb = this.getDatabase(dbName);

		if (mydb != null)
		{
			mydb.close();
			dbmap.remove(dbName);
		}*/
	}

	/**
	 * Delete a database.
	 *
	 * @param dbname
	 *            The name of the database-NOT including its extension.
	 *
	 * @return true if successful or false if an exception was encountered
	 *
	 */
	@SuppressLint("NewApi")
	private boolean deleteDatabase(String dbname)
	{
		boolean status = false; // assume the worst case:

		if (this.getDatabase(dbname) != null) this.closeDatabase(dbname);

		File dbfile = this.cordova.getActivity().getDatabasePath(dbname + ".db");

		Log.v("info", "delete sqlite db: " + dbfile.getAbsolutePath());

		// Use try & catch just in case android.os.Build.VERSION.SDK_INT >= 16 was lying:
		try {
			status = SQLiteDatabase.deleteDatabase(dbfile);
		} catch (Exception ex) {
			// log & give up:
			Log.v("executeSqlBatch", "deleteDatabase(): Error=" +  ex.getMessage());
			ex.printStackTrace();
		}

		return status;
	}

	/**
	 * Get a database from the db map.
	 *
	 * @param dbname
	 *            The name of the database.
	 *
	 */
	private SQLiteDatabase getDatabase(String dbname)
	{
		return dbmap.get(dbname);
	}

	/**
	 * Executes a batch request IN BACKGROUND THREAD and sends the results via sendJavascriptCB().
	 *
	 * @param dbName
	 *            The name of the database.
	 *
	 * @param queryarr
	 *            Array of query strings
	 *
	 * @param jsonparams
	 *            Array of JSON query parameters
	 *
	 * @param queryIDs
	 *            Array of query ids
	 *
	 * @param cbc
	 *            Callback context from Cordova API
	 *
	 */
	private void executeSqlBatchInBackground(final String dbName,
		final String[] queryarr, final JSONArray[] jsonparams, final String[] queryIDs, final CallbackContext cbc)
	{
		final SQLitePlugin myself = this;

		this.cordova.getThreadPool().execute(new Runnable() {
			public void run() {
				synchronized(myself) {
					myself.executeSqlBatch(dbName, queryarr, jsonparams, queryIDs, cbc);
				}
			}
		});
	}

	/**
	 * Check to see if the the project got switched.
	 * If so, the databases must be updated.
	 */
	private void updateDatabases(){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(this.cordova.getActivity());

		if(!projectName.equals(oldProjectName)){
			for(String name : dbmap.keySet()){
				openDatabase(name, null);
			}
		}
	}
	
	/**
	 * Executes a batch request and sends the results via sendJavascriptCB().
	 *
	 * @param dbname
	 *            The name of the database.
	 *
	 * @param queryarr
	 *            Array of query strings
	 *
	 * @param jsonparams
	 *            Array of JSON query parameters
	 *
	 * @param queryIDs
	 *            Array of query ids
	 *
	 * @param cbc
	 *            Callback context from Cordova API
	 *
	 */
	private void executeSqlBatch(final String dbname, final String[] queryarr,
			final JSONArray[] jsonparams, final String[] queryIDs, final CallbackContext cbc)
	{
		
		final Activity activity = this.cordova.getActivity();
		final SQLitePlugin sqlitePlugin = this;
		
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run(){
				
				updateDatabases();
				
				SQLiteDatabase mydb = sqlitePlugin.getDatabase(dbname);

				if (mydb == null) return;

				String query = "";
				String query_id = "";
				int len = queryarr.length;

				JSONArray batchResults = new JSONArray();

				for (int i = 0; i < len; i++) {
					query_id = queryIDs[i];

					JSONObject queryResult = null;
					String errorMessage = "unknown";

					try {
						boolean needRawQuery = true;

						query = queryarr[i];

						// UPDATE or DELETE:
						// NOTE: this code should be safe to RUN with old Android SDK.
						// To BUILD with old Android SDK remove lines from HERE: {{
						if (android.os.Build.VERSION.SDK_INT >= 11 &&
						    (query.toLowerCase().startsWith("update") ||
						     query.toLowerCase().startsWith("delete")))
						{
							SQLiteStatement myStatement = mydb.compileStatement(query);

							if (jsonparams != null) {
								for (int j = 0; j < jsonparams[i].length(); j++) {
									if (jsonparams[i].get(j) instanceof Float || jsonparams[i].get(j) instanceof Double ) {
										myStatement.bindDouble(j + 1, jsonparams[i].getDouble(j));
									} else if (jsonparams[i].get(j) instanceof Number) {
										myStatement.bindLong(j + 1, jsonparams[i].getLong(j));
									} else if (jsonparams[i].isNull(j)) {
										myStatement.bindNull(j + 1);
									} else {
										myStatement.bindString(j + 1, jsonparams[i].getString(j));
									}
								}
							}

							int rowsAffected = -1; // (assuming invalid)

							// Use try & catch just in case android.os.Build.VERSION.SDK_INT >= 11 is lying:
							try {
								rowsAffected = myStatement.executeUpdateDelete();
								// Indicate valid results:
								needRawQuery = false;
							} catch (SQLiteException ex) {
								// Indicate problem & stop this query:
								ex.printStackTrace();
								errorMessage = ex.getMessage();
								Log.v("executeSqlBatch", "SQLiteStatement.executeUpdateDelete(): Error=" +  errorMessage);
								needRawQuery = false;
							} catch (Exception ex) {
								// Assuming SDK_INT was lying & method not found:
								// do nothing here & try again with raw query.
							}

							if (rowsAffected != -1) {
								queryResult = new JSONObject();
								queryResult.put("rowsAffected", rowsAffected);
							}
						} // to HERE. }}

						// INSERT:
						if (query.toLowerCase().startsWith("insert") && jsonparams != null) {
							needRawQuery = false;

							SQLiteStatement myStatement = mydb.compileStatement(query);

							for (int j = 0; j < jsonparams[i].length(); j++) {
								if (jsonparams[i].get(j) instanceof Float || jsonparams[i].get(j) instanceof Double ) {
									myStatement.bindDouble(j + 1, jsonparams[i].getDouble(j));
								} else if (jsonparams[i].get(j) instanceof Number) {
									myStatement.bindLong(j + 1, jsonparams[i].getLong(j));
								} else if (jsonparams[i].isNull(j)) {
									myStatement.bindNull(j + 1);
								} else {
									myStatement.bindString(j + 1, jsonparams[i].getString(j));
								}
							}

							long insertId = -1; // (invalid)

							try {
								insertId = myStatement.executeInsert();
							} catch (SQLiteException ex) {
								ex.printStackTrace();
								errorMessage = ex.getMessage();
								Log.v("executeSqlBatch", "SQLiteDatabase.executeInsert(): Error=" +  errorMessage);
							}

							if (insertId != -1) {
								queryResult = new JSONObject();
								queryResult.put("insertId", insertId);
								queryResult.put("rowsAffected", 1);
							}
						}

						if (query.toLowerCase().startsWith("begin")) {
							needRawQuery = false;
							try {
								mydb.beginTransaction();

								queryResult = new JSONObject();
								queryResult.put("rowsAffected", 0);
							} catch (SQLiteException ex) {
								ex.printStackTrace();
								errorMessage = ex.getMessage();
								Log.v("executeSqlBatch", "SQLiteDatabase.beginTransaction(): Error=" +  errorMessage);
							}
						}

						if (query.toLowerCase().startsWith("commit")) {
							needRawQuery = false;
							try {
								mydb.setTransactionSuccessful();
								mydb.endTransaction();

								queryResult = new JSONObject();
								queryResult.put("rowsAffected", 0);
							} catch (SQLiteException ex) {
								ex.printStackTrace();
								errorMessage = ex.getMessage();
								Log.v("executeSqlBatch", "SQLiteDatabase.setTransactionSuccessful/endTransaction(): Error=" +  errorMessage);
							}
						}

						if (query.toLowerCase().startsWith("rollback")) {
							needRawQuery = false;
							try {
								mydb.endTransaction();

								queryResult = new JSONObject();
								queryResult.put("rowsAffected", 0);
							} catch (SQLiteException ex) {
								ex.printStackTrace();
								errorMessage = ex.getMessage();
								Log.v("executeSqlBatch", "SQLiteDatabase.endTransaction(): Error=" +  errorMessage);
							}
						}

						// raw query for other statements:
						if (needRawQuery) {
							String[] params = null;

							if (jsonparams != null) {
								params = new String[jsonparams[i].length()];

								for (int j = 0; j < jsonparams[i].length(); j++) {
									if (jsonparams[i].isNull(j))
										params[j] = "";
									else
										params[j] = jsonparams[i].getString(j);
								}
							}

							Cursor myCursor = mydb.rawQuery(query, params);

							if (query_id.length() > 0) {
								queryResult = sqlitePlugin.getRowsResultFromQuery(myCursor);
							}

							myCursor.close();
						}
					} catch (Exception ex) {
						ex.printStackTrace();
						errorMessage = ex.getMessage();
						Log.v("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): Error=" +  errorMessage);
					}

					try {
						if (queryResult != null) {
							JSONObject r = new JSONObject();
							r.put("qid", query_id);

							r.put("type", "success");
							r.put("result", queryResult);

							batchResults.put(r);
						} else {
							JSONObject r = new JSONObject();
							r.put("qid", query_id);
							r.put("type", "error");

							JSONObject er = new JSONObject();
							er.put("message", errorMessage);
							r.put("result", er);

							batchResults.put(r);
						}
					} catch (JSONException ex) {
						ex.printStackTrace();
						Log.v("executeSqlBatch", "SQLitePlugin.executeSql[Batch](): Error=" +  ex.getMessage());
						// TODO what to do?
					}
				}

				final JSONArray _batchResults = batchResults;
				
				activity.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						cbc.success(_batchResults);
					}
				});
			}
		});
	}

	/**
	 * Get rows results from query cursor.
	 *
	 * @param cur
	 *            Cursor into query results
	 *
	 * @return results in string form
	 *
	 */
	private JSONObject getRowsResultFromQuery(Cursor cur)
	{
		JSONObject rowsResult = new JSONObject();

		// If query result has rows
		if (cur.moveToFirst()) {
			JSONArray rowsArrayResult = new JSONArray();
			String key = "";
			int colCount = cur.getColumnCount();

			// Build up JSON result object for each row
			do {
				JSONObject row = new JSONObject();
				try {
					for (int i = 0; i < colCount; ++i) {
						key = cur.getColumnName(i);

						// NOTE: this code should be safe to RUN with old Android SDK.
						// To BUILD with old Android SDK remove lines from HERE: {{
						if(android.os.Build.VERSION.SDK_INT >= 11)
						{
							int curType = 3; /* Cursor.FIELD_TYPE_STRING */

							// Use try & catch just in case android.os.Build.VERSION.SDK_INT >= 11 is lying:
							try {
								curType = cur.getType(i);

								switch(curType)
								{
								case Cursor.FIELD_TYPE_NULL:
									row.put(key, JSONObject.NULL);
									break;
								case Cursor.FIELD_TYPE_INTEGER:
									row.put(key, cur.getInt(i));
									break;
								case Cursor.FIELD_TYPE_FLOAT:
									row.put(key, cur.getFloat(i));
									break;
								case Cursor.FIELD_TYPE_BLOB:
									row.put(key, new String(Base64.encode(cur.getBlob(i), Base64.DEFAULT)));
									break;
								case Cursor.FIELD_TYPE_STRING:
								default: /* (not expected) */
									row.put(key, cur.getString(i));
									break;
								}

							} catch (Exception ex) {
								// simply treat like a string
								row.put(key, cur.getString(i));
							}
						}
						else // to HERE. }}
						{
							row.put(key, cur.getString(i));
						}
					}

					rowsArrayResult.put(row);

				} catch (JSONException e) {
					e.printStackTrace();
				}

			} while (cur.moveToNext());

			try {
				rowsResult.put("rows", rowsArrayResult);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return rowsResult;
	}

	/**
	 * Send Javascript callback.
	 *
	 * @param cb
	 *            Javascript callback command to send
	 *
	 */
	private void sendJavascriptCB(String cb)
	{
		this.webView.sendJavascript(cb);
	}

	/**
	 * Send Javascript callback on GUI thread.
	 *
	 * @param cb
	 *            Javascript callback command to send
	 *
	 */
	private void sendJavascriptToGuiThread(final String cb)
	{
		final SQLitePlugin myself = this;

		this.cordova.getActivity().runOnUiThread(new Runnable() {
			public void run() {
				myself.webView.sendJavascript(cb);
			}
		});
	}
}
