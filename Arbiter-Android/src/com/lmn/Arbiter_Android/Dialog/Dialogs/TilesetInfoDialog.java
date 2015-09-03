package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.BaseClasses.Tileset;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.TilesetsHelper;


public class TilesetInfoDialog extends ArbiterDialogFragment{

	Tileset thisTileset;
	Calendar calendar;
	Date date;

	public static TilesetInfoDialog newInstance(String title, String back, String stop,
			int layout, Tileset tileset){

		TilesetInfoDialog frag = new TilesetInfoDialog();
		
		frag.setTitle(title);
		frag.setCancel(back);
		frag.setLayout(layout);
		frag.thisTileset = tileset;

		if (tileset.getIsDownloading())
			frag.setOk(stop);

		frag.date = null;

		return frag;
	}
	
	@Override
	public void onPositiveClick() {
		// TODO Auto-generated method stub
		thisTileset.setIsDownloading(false);
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {

		Context context = this.getActivity().getApplicationContext();

		// TODO Auto-generated method stub
		String nameStr 			= context.getString(R.string.tileset_info_name);
		String timeCreatedStr 	= context.getString(R.string.tileset_info_time_created);
		String createdByStr 	= context.getString(R.string.tileset_info_created_by);
		String filesizeStr 		= context.getString(R.string.tileset_info_filesize);
		String layerNameStr 	= context.getString(R.string.tileset_info_layer_name);
		String statusStr 		= context.getString(R.string.tileset_info_status);
		//String resourceUriStr 	= context.getString(R.string.tileset_info_resource_uri);
		//String serviceTypeStr 	= context.getString(R.string.tileset_info_service_type);
		//String downloadUrlStr 	= context.getString(R.string.tileset_info_download_url);
		//String serverUrlStr 	= context.getString(R.string.tileset_info_server_url);
		//String serverUsernameStr = context.getString(R.string.tileset_info_server_username);
		//String serverIDStr 		= context.getString(R.string.tileset_info_server_id);

		TextView nameTV 			= (TextView)view.findViewById(R.id.tileset_info_name);
		TextView timeCreatedTV 		= (TextView)view.findViewById(R.id.tileset_info_time_created);
		TextView createdByTV 		= (TextView)view.findViewById(R.id.tileset_info_created_by);
		TextView filesizeTV 		= (TextView)view.findViewById(R.id.tileset_info_filesize);
		TextView layerNameTV 		= (TextView)view.findViewById(R.id.tileset_info_layer_name);
		TextView statusTV 			= (TextView)view.findViewById(R.id.tileset_info_status);
		//TextView resourceUriTV 		= (TextView)view.findViewById(R.id.tileset_info_resourceURI);
		//TextView serviceTypeTV 		= (TextView)view.findViewById(R.id.tileset_info_service_type);
		//TextView downloadUrlTV 		= (TextView)view.findViewById(R.id.tileset_info_download_url);
		//TextView serverUrlTV 		= (TextView)view.findViewById(R.id.tileset_info_server_url);
		//TextView serverUsernameTV 	= (TextView)view.findViewById(R.id.tileset_info_server_username);
		//TextView serverIDTV 		= (TextView)view.findViewById(R.id.tileset_info_server_id);

		// Set TextViews
		nameTV.setText			(nameStr + " " + thisTileset.getTilesetName());

		// Parse TimeCreated
		String[] timeCreated = thisTileset.getCreatedTime().split("T");
		String actualString = "";
		actualString += timeCreated[0] + " - ";
		actualString += timeCreated[1].substring(0, "00:00:00".length());
		timeCreatedTV.setText	(timeCreatedStr + " " + actualString);

		// Parse Created By
		JSONObject JSONObj;
		String createdBy = null;
		try {
			JSONObj = new JSONObject(thisTileset.getCreatedBy());
			if (JSONObj.has("username")){
				try {
					createdBy = JSONObj.getString("username");
				} catch (JSONException e){
					Log.w("JSON Exception", e.getMessage());
				}
			}
			else{
				createdBy = "null";
			}
		} catch (JSONException e){
			Log.w("JSON Exception", e.getMessage());
		}

		createdByTV.setText		(createdByStr + " " + createdBy);
		filesizeTV.setText		(filesizeStr + " " + thisTileset.getFilesizeAfterConversion());
		layerNameTV.setText		(layerNameStr + " " + thisTileset.getLayerName());
		//resourceUriTV.setText	(resourceUriStr + " " + thisTileset.getResourceURI());
		//serviceTypeTV.setText	(serviceTypeStr + " " + thisTileset.getServerServiceType());
		//downloadUrlTV.setText	(downloadUrlStr + " " + thisTileset.getDownloadURL());
		//serverUrlTV.setText		(serverUrlStr + " " + thisTileset.getServerURL());
		//serverUsernameTV.setText(serverUsernameStr + " " + thisTileset.getServerUsername());
		//serverIDTV.setText		(serverIDStr + " " + thisTileset.getServerID());

		// Status
		String tilesetStatus = "";
		ApplicationDatabaseHelper appHelper = ApplicationDatabaseHelper.getHelper(context);
		TilesetsHelper helper = TilesetsHelper.getTilesetsHelper();

		if (helper.checkInDatabase(appHelper.getReadableDatabase(), thisTileset)) {
			ArrayList<Tileset> allTilesets = helper.getTilesetsInProject();
			for (int i = 0; i < allTilesets.size(); i++){
				Tileset tileset = allTilesets.get(i);
				if (tileset.getTilesetName().equals(thisTileset.getTilesetName())) {
					if (tileset.getIsDownloading()) {
						tilesetStatus = context.getString(R.string.tileset_status_downloading);
						tilesetStatus += " " + tileset.getDownloadProgress() + "%";
					} else
						tilesetStatus = context.getString(R.string.tileset_status_in_database);
				}
			}
		} else
			tilesetStatus = context.getString(R.string.tileset_status_on_server);

		statusTV.setText(statusStr + " " + tilesetStatus);
	}
}
