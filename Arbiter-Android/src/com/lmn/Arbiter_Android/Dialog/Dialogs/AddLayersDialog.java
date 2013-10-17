package com.lmn.Arbiter_Android.Dialog.Dialogs;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogFragment;
import com.lmn.Arbiter_Android.Dialog.ArbiterDialogs;
import com.lmn.Arbiter_Android.ListAdapters.ServerSpinnerAdapter;
import com.lmn.Arbiter_Android.ListItems.ServerListItem;
import com.lmn.Arbiter_Android.Loaders.ServersListLoader;
import com.lmn.Arbiter_Android.Projects.ProjectComponents;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Spinner;

public class AddLayersDialog extends ArbiterDialogFragment implements LoaderManager.LoaderCallbacks<ServerListItem[]>{
	private ServerSpinnerAdapter serverAdapter;
	private Spinner spinner;
	
	public static AddLayersDialog newInstance(String title, String ok, 
			String cancel, int layout){
		AddLayersDialog frag = new AddLayersDialog();
		
		frag.setTitle(title);
		frag.setOk(ok);
		frag.setCancel(cancel);
		frag.setLayout(layout);
		
		return frag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}

	@Override
	public void onPositiveClick() {
		//ProjectComponents project = getProject();
		
	//	if(project != null){
			// commit the layers that have been added, to the database
	//	}
	}

	@Override
	public void onNegativeClick() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeCreateDialog(View view) {
		if(view != null){
			this.serverAdapter = new ServerSpinnerAdapter(this.getActivity().getApplicationContext());
			
			this.spinner = (Spinner) view.findViewById(R.id.serversSpinner);
			
			this.spinner.setAdapter(this.serverAdapter);
			
			ImageButton button = (ImageButton) view.findViewById(R.id.add_server_button);
			
			if(button != null){
				button.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View v) {
						(new ArbiterDialogs(getActivity().getResources(), 
								getActivity().getSupportFragmentManager())).showAddServerDialog();
					}
				});
			}
		}
		
		// Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        this.getActivity().getSupportLoaderManager().initLoader(R.id.loader_servers, null, this);
	}
	
	@Override
	public Loader<ServerListItem[]> onCreateLoader(int id, Bundle bundle) {
		// This is called when a new Loader needs to be created.  This
        // sample only has one Loader with no arguments, so it is simple.
        return new ServersListLoader(this.getActivity().getApplicationContext());
	}

	@Override
	public void onLoadFinished(Loader<ServerListItem[]> loader, ServerListItem[] data) {
		serverAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<ServerListItem[]> loader) {
		serverAdapter.setData(null);
	}
}
