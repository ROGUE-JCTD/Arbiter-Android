package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.R;

public class DownloadingTilesetsDialog extends ArbiterDialogFragment{

	private String tilesetName = " ";

	public static DownloadingTilesetsDialog newInstance(String title, String cancel, int layout, String _tilesetName){

		DownloadingTilesetsDialog frag = new DownloadingTilesetsDialog();

		frag.setTitle(title);
		frag.setCancel(cancel);
		frag.setLayout(layout);

		frag.tilesetName = _tilesetName;
		
		return frag;
	}

	@Override
	public void onPositiveClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		// TODO Auto-generated method stub
		TextView name = (TextView)view.findViewById(R.id.progress_tileset_name);
		name.setText("Tileset: " + this.tilesetName);

		TextView progress = (TextView)view.findViewById(R.id.progress_tileset_text);
		progress.setText("Preparing..");
	}
}
