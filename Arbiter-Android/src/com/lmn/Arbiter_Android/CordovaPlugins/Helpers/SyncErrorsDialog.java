package com.lmn.Arbiter_Android.CordovaPlugins.Helpers;

import com.lmn.Arbiter_Android.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

public class SyncErrorsDialog extends DialogFragment{

	/*private AlertDialog myDialog;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		myDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		
		String title;
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout., null);
		
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.icon)
			.setTitle(this.title)
			.setView(view).create();
		
		this.myDialog = dialog;
		
		return dialog;
	}*/
}
