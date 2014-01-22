package com.lmn.Arbiter_Android.Dialog.Dialogs;

import org.apache.cordova.CordovaInterface;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.Base64;
import android.util.Log;
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
		final AddServerDialog frag = new AddServerDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		frag.setValidatingClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				frag.onPositiveClick();
			}
		});
		frag.server = server;
		
		return frag;
	}
	
	@Override
	public void onPositiveClick() {
		final Activity activity = this.getActivity();
		final Context context = activity.getApplicationContext();
		
		try{		
			String title = context.getResources().getString(R.string.validating_server);
			String message = context.getResources().getString(R.string.please_wait);
			
			final ProgressDialog progressDialog = ProgressDialog.show(activity, title, message, false);
			
			((CordovaInterface) this.getActivity()).getThreadPool().execute(new Runnable(){
				@Override
				public void run(){
					
					DefaultHttpClient client = new DefaultHttpClient();
					HttpParams params = client.getParams();
					params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
					HttpGet request = new HttpGet(urlField.getText().toString() + "/rest");
					
					String credentials = usernameField.getText().toString() + ":" + passwordField.getText().toString();
					credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
					
					request.addHeader("Authorization", "Basic " + credentials);
					
					final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
					try {
						HttpResponse response = client.execute(request);
						int code = response.getStatusLine().getStatusCode();
						switch (code) {
						case 200:
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
									dismiss();
								}
							});
							break;
						case 401:
							activity.runOnUiThread(new Runnable(){
								@Override
								public void run(){
									builder.setTitle(context.getResources().getString(R.string.error));
									builder.setIcon(context.getResources().getDrawable(R.drawable.warning));
									builder.setMessage(context.getResources().getString(R.string.authentication_failed));
									
									builder.create().show();
								}
							});
							break;
						default:	
							activity.runOnUiThread(new Runnable(){
								@Override
								public void run(){
									builder.setTitle(context.getResources().getString(R.string.error));
									builder.setIcon(context.getResources().getDrawable(R.drawable.warning));
									builder.setMessage(context.getResources().getString(R.string.unable_to_connect));
									
									builder.create().show();
								}
							});
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						activity.runOnUiThread(new Runnable(){
							@Override
							public void run(){
								builder.setTitle(context.getResources().getString(R.string.error));
								builder.setIcon(context.getResources().getDrawable(R.drawable.warning));
								builder.setMessage(context.getResources().getString(R.string.unable_to_connect));
								
								builder.create().show();
							}
						});
					}
					activity.runOnUiThread(new Runnable(){
						@Override
						public void run(){
							progressDialog.dismiss();
						}
					});					
				}
			});
		}catch(ClassCastException e){
			e.printStackTrace();
		}
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
