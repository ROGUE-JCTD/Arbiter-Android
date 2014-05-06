package com.lmn.Arbiter_Android.Dialog.Dialogs;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.MapActivity;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.Dialog.ProgressDialog.SyncProgressDialog;
import com.lmn.Arbiter_Android.Map.Map;

public class SyncTimeOutDialog extends ArbiterDialogFragment{
	private View view;
	private CallbackContext callback;
	
	public static SyncTimeOutDialog newInstance(String title, String ok, 
			String cancel, int layout, CallbackContext callback){
		SyncTimeOutDialog frag = new SyncTimeOutDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		frag.callback = callback;
		
		return frag;
	}
	
	@Override
	public void onPositiveClick() {
		//Keep waiting for sync to finish
		this.callback.success();
	}

	@Override
	public void onNegativeClick() {
		//cancel the sync
		this.callback.error(0);
		
		final Activity activity = getActivity();
		String title = activity.getResources().getString(R.string.sync_in_progress);
		String message = activity.getResources().getString(R.string.sync_cancelling);
		
		SyncProgressDialog.setTitleAndMessage(activity, title, message);
	}

	@Override
	public void beforeCreateDialog(View view) {
		this.view = view;
	}
}
