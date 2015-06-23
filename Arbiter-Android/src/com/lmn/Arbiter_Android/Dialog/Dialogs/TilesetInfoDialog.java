package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;

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

		frag.date = new Date();
		frag.calendar = Calendar.getInstance();
		
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
		String nameStr = context.getString(R.string.tileset_info_name);
		String timeCreatedStr = context.getString(R.string.tileset_info_time_created);
		String createdByStr = context.getString(R.string.tileset_info_created_by);
		String filesizeStr = context.getString(R.string.tileset_info_filesize);
		String serverStr = context.getString(R.string.tileset_info_server);
		String statusStr = context.getString(R.string.tileset_info_status);

		TextView nameTV = (TextView)view.findViewById(R.id.tileset_info_name);
		TextView timeCreatedTV = (TextView)view.findViewById(R.id.tileset_info_time_created);
		TextView createdByTV = (TextView)view.findViewById(R.id.tileset_info_created_by);
		TextView filesizeTV = (TextView)view.findViewById(R.id.tileset_info_filesize);
		TextView serverTV = (TextView)view.findViewById(R.id.tileset_info_server);
		TextView statusTV = (TextView)view.findViewById(R.id.tileset_info_status);

		// Get Time
		date.setTime(thisTileset.getCreatedTime());
		calendar.setTime(date);
		nameTV.setText(nameStr + " " + thisTileset.getName());
		timeCreatedTV.setText(timeCreatedStr + " " + Integer.toString(calendar.get(Calendar.MONTH)) + "/"
													+ Integer.toString(calendar.get(Calendar.DAY_OF_MONTH)) + "/"
													+ Integer.toString(calendar.get(Calendar.YEAR)));
		createdByTV.setText(createdByStr + " " + thisTileset.getCreatedBy());
		filesizeTV.setText(filesizeStr + " " + thisTileset.getFilesizeAfterConversion());
		serverTV.setText(serverStr + " " + thisTileset.getSourceId());

		String tilesetStatus;
		ApplicationDatabaseHelper appHelper = ApplicationDatabaseHelper.getHelper(context);
		TilesetsHelper helper = TilesetsHelper.getTilesetsHelper();

		if (helper.checkInDatabase(appHelper.getReadableDatabase(), thisTileset)) {
			if (thisTileset.getIsDownloading())
				tilesetStatus = context.getString(R.string.tileset_status_downloading);
			else
				tilesetStatus = context.getString(R.string.tileset_status_in_database);
		} else
			tilesetStatus = context.getString(R.string.tileset_status_on_server);

		statusTV.setText(statusStr + " " + tilesetStatus);
	}
}
