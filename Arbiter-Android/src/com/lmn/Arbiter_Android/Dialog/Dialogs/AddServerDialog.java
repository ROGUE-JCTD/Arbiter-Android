package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;

public class AddServerDialog extends ArbiterDialogFragment{
	private Server server;
	private EditText nameField;
	private EditText urlField;
	private EditText usernameField;
	private EditText passwordField;
	private CheckBox showPassword;
	
	public static AddServerDialog newInstance(String title, String ok, 
			String cancel, int layout, Server server){
		AddServerDialog frag = new AddServerDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		frag.server = server;
		
		return frag;
	}
	
	@Override
	public void onPositiveClick() {
		final Context context = this.getActivity();
		
		// Queue the command to insert the project
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run() {
				ApplicationDatabaseHelper helper = 
						ApplicationDatabaseHelper.getHelper(context);
				
				boolean insert = false;
				
				if(server == null){
					insert = true;
					server = new Server();
				}
				
				setServer(server);
				
				if(!insert){
					updateServer(helper, context, server);
				}else{
					insertServer(helper, context, server);
				}
			}
		});
		
	}
	
	private void setFields(Server server){
		nameField.setText(server.getName());
		urlField.setText(server.getUrl());
		usernameField.setText(server.getUsername());
		passwordField.setText(server.getPassword());
	}
	
	private void setServer(Server server){
		server.setName(nameField.getText().toString());
		server.setUrl(urlField.getText().toString());
		server.setUsername(usernameField.getText().toString());
		server.setPassword(passwordField.getText().toString());
	}
	
	private void insertServer(ApplicationDatabaseHelper helper, Context context, Server server){	
		Server[] list = new Server[1];
		list[0] = server;
		
		ServersHelper.getServersHelper().insert(helper.getWritableDatabase(), context, list);
	}

	private void updateServer(final ApplicationDatabaseHelper helper, final Context context, final Server server){
		final FragmentActivity activity = this.getActivity();
		
		ServersHelper.getServersHelper().updateAlert(activity, new Runnable(){

			@Override
			public void run() {
				CommandExecutor.runProcess(new Runnable(){
					@Override
					public void run() {
						ServersHelper.getServersHelper().update(helper.getWritableDatabase(),
								context, server);
					}
					
				});
			}
		});
	}
	
	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		
		this.nameField = (EditText) view.findViewById(R.id.server_name);
		this.urlField = (EditText) view.findViewById(R.id.server_url);
		this.usernameField = (EditText) view.findViewById(R.id.server_username);
		this.passwordField = (EditText) view.findViewById(R.id.server_password);
		this.showPassword = (CheckBox) view.findViewById(R.id.server_show_password);
		
		this.showPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(showPassword.isChecked()) {
					passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				} else {
					passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}
				int position = passwordField.length();
				Editable etext = passwordField.getText();
				Selection.setSelection(etext, position);
			}
			
		});
		
		if(server != null){
			setFields(server);
		}
	}
}
