package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;

public class AddServerDialog extends ArbiterDialogFragment{
	private View view;
	
	public static AddServerDialog newInstance(String title, String ok, 
			String cancel, int layout){
		AddServerDialog frag = new AddServerDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		return frag;
	}
	
	@Override
	public void onPositiveClick() {
		final Context context = this.getActivity();
		
		// Queue the command to insert the project
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run() {
				Log.w("SERVERS DIALOG", "INSERT SERVER");
				GlobalDatabaseHelper helper = GlobalDatabaseHelper.getGlobalHelper(context);
				EditText nameField = (EditText) view.findViewById(R.id.server_name);
				EditText urlField = (EditText) view.findViewById(R.id.server_url);
				EditText usernameField = (EditText) view.findViewById(R.id.server_username);
				EditText passwordField = (EditText) view.findViewById(R.id.server_password);
				
				Server[] list = new Server[1];
				list[0] = new Server(
						nameField.getText().toString(),
						urlField.getText().toString(),
						usernameField.getText().toString(),
						passwordField.getText().toString(),
						-1);
				
				ServersHelper.getServersHelper().insert(helper.getWritableDatabase(), context, list);
			}
		});
		
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		this.view = view;
		
	}
}
