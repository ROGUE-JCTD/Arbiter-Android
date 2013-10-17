package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.DatabaseHelpers.DbHelpers;
import com.lmn.Arbiter_Android.DatabaseHelpers.GlobalDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandList;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;

public class AddServerDialog extends ArbiterDialogFragment{
	private CommandList commandList;
	private View view;
	
	public static AddServerDialog newInstance(String title, String ok, 
			String cancel, int layout){
		AddServerDialog frag = new AddServerDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		frag.commandList = CommandList.getCommandList();
		return frag;
	}
	
	@Override
	public void onPositiveClick() {
		final Context context = this.getActivity();
		
		// Queue the command to insert the project
		commandList.queueCommand(new Runnable(){
			@Override
			public void run() {
				Log.w("SERVERS DIALOG", "INSERT SERVER");
				GlobalDatabaseHelper helper = DbHelpers.getDbHelpers(context).getGlobalDbHelper();
				EditText nameField = (EditText) view.findViewById(R.id.server_name);
				EditText urlField = (EditText) view.findViewById(R.id.server_url);
				EditText usernameField = (EditText) view.findViewById(R.id.server_username);
				EditText passwordField = (EditText) view.findViewById(R.id.server_password);
				
				helper.createServer(context, new ServerListItem(
												nameField.getText().toString(),
												urlField.getText().toString(),
												usernameField.getText().toString(),
												passwordField.getText().toString()));
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
