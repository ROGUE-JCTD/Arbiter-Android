package com.lmn.Arbiter_Android.Dialog.Dialogs;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.text.TextWatcher;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.CookieManager.ArbiterCookieManager;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.CommandExecutor.CommandExecutor;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.ListAdapters.ServerTypesAdapter;
import com.lmn.Arbiter_Android.Map.Map;

public class AddServerDialog extends ArbiterDialogFragment {
	private Server server;
	private Spinner serverTypeSpinner;
	private EditText nameField;
	private EditText urlField;
	private EditText usernameField;
	private EditText passwordField;
	private CheckBox showPassword;
	private ServerTypesAdapter serverTypeAdapter;
	
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
	
	public void putServer(final ProgressDialog progressDialog){
		
		// Queue the command to insert the project
		CommandExecutor.runProcess(new Runnable(){
			@Override
			public void run() {
				Context context = getActivity().getApplicationContext();

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

				getActivity().runOnUiThread(new Runnable(){
					@Override
					public void run(){

						dismiss();

						if(progressDialog != null){
							progressDialog.dismiss();
						}
					}
				});
			}
		});
	}
	
	public void attemptAuthentication(final ProgressDialog progressDialog){
		
		try{		
			
			((HasThreadPool) this.getActivity()).getThreadPool().execute(new Runnable(){
				@Override
				public void run(){
					
					DefaultHttpClient client = new DefaultHttpClient();
					HttpParams params = client.getParams();
					params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
					
					// THIS URL IS ONLY BEING USED TEMPORARILY.  THE REST ENDPOINT IS LOCKED DOWN SO THAT '/rest' REQUIRES ADMIN PRIVILEGES,
					// BUT WE WANT ANY REGISTERED USER.  AT THE TIME I WROTE THIS (5/7/2014), THIS WAS ONE WAY TO CHECK.  COMPLETE HACK, BUT
					// IT GOT THE JOB DONE.  IN THE FUTURE, THIS MAY NOT BE A VIABLE ENDPOINT AND SHOULD BE CHANGED TO SOME ENDPOINT THAT ONLY
					// REGISTERED USERS CAN ACCESS.
					HttpGet request = new HttpGet(urlField.getText().toString().replace("/wms", "/rest/process/batchdownload/arbiterAuthenticatedUserLoginTest"));
					
					String credentials = usernameField.getText().toString() + ":" + passwordField.getText().toString();
					credentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
					
					request.addHeader("Authorization", "Basic " + credentials);
					
					try {
						HttpResponse response = client.execute(request);
						int code = response.getStatusLine().getStatusCode();
						switch (code) {
						case 200:
							
							new ArbiterCookieManager(getActivity().getApplicationContext()).getCookieForServer(
									urlField.getText().toString(), 
									usernameField.getText().toString(),
									passwordField.getText().toString());
							
							putServer(progressDialog);
							
							break;
						case 404:
							
							new ArbiterCookieManager(getActivity().getApplicationContext()).getCookieForServer(
									urlField.getText().toString(), 
									usernameField.getText().toString(),
									passwordField.getText().toString());
							
							putServer(progressDialog);
							
							break;
						case 401:
							
							displayAuthenticationError(R.string.authentication_failed, progressDialog);
							
							break;
						case 403:
							
							new ArbiterCookieManager(getActivity().getApplicationContext()).getCookieForServer(
									urlField.getText().toString(), 
									usernameField.getText().toString(),
									passwordField.getText().toString());
							
							putServer(progressDialog);
							
							break;
						default:	
							
							Log.w("AddServerDialog", "AddServerDialog default: statusCode = " + code);
							
							displayAuthenticationError(R.string.unable_to_connect, progressDialog);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();

                        String unableToConnectStr = getActivity().getApplicationContext().getResources().getString(R.string.unable_to_connect);
                        displayAuthenticationError(unableToConnectStr + ": " + e.getMessage(), progressDialog);
					}
				}
			});
		}catch(ClassCastException e){
			e.printStackTrace();
			
			progressDialog.dismiss();
		}
	}


    public void displayAuthenticationError(final int errorId, final ProgressDialog progressDialog){
        displayAuthenticationError(getActivity().getApplicationContext().getResources().getString(errorId), progressDialog);
    }

    public void displayAuthenticationError(final String msg, final ProgressDialog progressDialog){
        final Activity activity = getActivity();
        final Context context = activity.getApplicationContext();

        activity.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				AlertDialog.Builder builder = new AlertDialog.Builder(activity);

				builder.setTitle(context.getResources().getString(R.string.error));
				builder.setIcon(context.getResources().getDrawable(R.drawable.icon));
				builder.setMessage(msg);

				builder.create().show();

				if (progressDialog != null) {
					progressDialog.dismiss();
				}
			}
		});
    }

	@Override
	public void onPositiveClick() {
		Activity activity = getActivity();
		Context context = activity.getApplicationContext();
		
		String title = context.getResources().getString(R.string.validating_server);
		String message = context.getResources().getString(R.string.please_wait);
		
		final ProgressDialog progressDialog = ProgressDialog.show(activity, title, message, false);
		
		String type = (String) this.serverTypeSpinner.getSelectedItem();
		
		if(type.equals(ServerTypesAdapter.Types.WMS)){
			
			String url = urlField.getText().toString();
			int urlLength = url.length();
			
			if(url.substring(urlLength - 4, urlLength).equals("/wms")){
				
				if(!usernameField.getText().toString().equals("") && !passwordField.getText().toString().equals("")){
					attemptAuthentication(progressDialog);
				}else{
					putServer(progressDialog);
				}
			}else{
				displaySlashWMSError(progressDialog);
			}
		}else{
			
			String url = urlField.getText().toString();
			int urlLength = url.length();
			
			if(url.substring(urlLength - 11, urlLength).equals("/hot/1.0.0/")){
				
				putServer(progressDialog);
			}else{
				
				displaySlashTMSError(progressDialog);
			}
		}
	}
	
	private void displaySlashWMSError(ProgressDialog progressDialog){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle(getActivity().getResources().getString(R.string.invalid_url));
		
		builder.setMessage(getActivity().getResources().getString(R.string.invalid_url_slash_wms));
		
		builder.setPositiveButton(getActivity().getResources().getString(R.string.close), null);
		
		builder.create().show();
		
		progressDialog.dismiss();
	}
	
	private void displaySlashTMSError(ProgressDialog progressDialog){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		
		builder.setTitle(getActivity().getResources().getString(R.string.invalid_url));
		
		builder.setMessage(getActivity().getResources().getString(R.string.invalid_url_slash_tms));
		
		builder.setPositiveButton(getActivity().getResources().getString(R.string.close), null);
		
		builder.create().show();
		
		progressDialog.dismiss();
	}	
	private void setFields(Server server){
		serverTypeSpinner.setSelection(serverTypeAdapter.getPositionFromType(server.getType()));
		nameField.setText(server.getName());
		urlField.setText(server.getUrl());
		usernameField.setText(server.getUsername());
		passwordField.setText(server.getPassword());
	}
	
	private void setServer(Server server){
		server.setType((String) serverTypeSpinner.getSelectedItem());
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
		
		ServersHelper.getServersHelper().updateAlert(activity, new Runnable() {

			@Override
			public void run() {
				CommandExecutor.runProcess(new Runnable() {
					@Override
					public void run() {
						ServersHelper.getServersHelper().update(helper.getWritableDatabase(),
								context, server);

						activity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								try {
									((Map.MapChangeListener) activity).getMapChangeHelper().onServerUpdated();
								} catch (ClassCastException e) {
									e.printStackTrace();
								}
							}
						});
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
		
		this.serverTypeSpinner = (Spinner) view.findViewById(R.id.server_type);
		this.nameField = (EditText) view.findViewById(R.id.server_name);
		this.urlField = (EditText) view.findViewById(R.id.server_url);
		this.usernameField = (EditText) view.findViewById(R.id.server_username);
		this.passwordField = (EditText) view.findViewById(R.id.server_password);
		this.showPassword = (CheckBox) view.findViewById(R.id.server_show_password);

		this.serverTypeAdapter = new ServerTypesAdapter(getActivity());
		
		this.serverTypeSpinner.setAdapter(this.serverTypeAdapter);
		
		this.serverTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
									   int position, long id) {
				// TODO Auto-generated method stub

				String type = serverTypeAdapter.getItem(position);

				if (type.equals(ServerTypesAdapter.Types.WMS)) {

					urlField.setHint(ServerTypesAdapter.Hints.WMS);
				} else {
					urlField.setHint(ServerTypesAdapter.Hints.TMS);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub

			}
		});

		this.showPassword.setEnabled(false);
		this.showPassword.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (showPassword.isChecked()) {
					passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
				} else {
					passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				}
				int position = passwordField.length();
				Editable etext = passwordField.getText();
				Selection.setSelection(etext, position);
			}

		});

		this.passwordField.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
										  int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
									  int count) {
				if (passwordField.length() > 0)
					showPassword.setEnabled(true);
				else
					showPassword.setEnabled(false);
			}
		});
		
		if(server != null){
			setFields(server);
		}
	}


}
