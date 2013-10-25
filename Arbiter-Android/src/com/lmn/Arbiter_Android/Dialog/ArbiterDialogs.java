package com.lmn.Arbiter_Android.Dialog;

import android.content.res.Resources;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.BaseClasses.Project;
import com.lmn.Arbiter_Android.Dialog.Dialogs.AddLayersDialog;
import com.lmn.Arbiter_Android.Dialog.Dialogs.AddServerDialog;
import com.lmn.Arbiter_Android.Dialog.Dialogs.ChooseAOIDialog;
import com.lmn.Arbiter_Android.Dialog.Dialogs.GoOfflineDialog;
import com.lmn.Arbiter_Android.Dialog.Dialogs.LayerInfoDialog;
import com.lmn.Arbiter_Android.Dialog.Dialogs.LayersDialog;
import com.lmn.Arbiter_Android.Dialog.Dialogs.ProjectNameDialog;
import com.lmn.Arbiter_Android.Dialog.Dialogs.ServersDialog;
import com.lmn.Arbiter_Android.Dialog.Dialogs.WelcomeDialog;

public class ArbiterDialogs {
	private Resources resources;
	private FragmentManager fragManager;
	
	public ArbiterDialogs(Resources resources, FragmentManager fragManager){
		this.setResources(resources);
		this.setFragManager(fragManager);
	}
	
	public void setFragManager(FragmentManager fragManager){
		this.fragManager = fragManager;
	}
	
	public void setResources(Resources resources){
		this.resources = resources;
	}
	
	public void showWelcomeDialog(){
		String title = resources.getString(R.string.welcome_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		int layout = R.layout.welcome_dialog;
		
		DialogFragment dialog = WelcomeDialog.newInstance(title, ok, cancel, layout);
		dialog.show(fragManager, "welcomeDialog");
	}

	public void showProjectNameDialog(){
		String title = resources.getString(R.string.project_name_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		int layout = R.layout.project_name_dialog;
		
		DialogFragment dialog = ProjectNameDialog.newInstance(title, ok, cancel, layout);
		dialog.show(fragManager, "projectNameDialog");
	}
	
	public void showAddServerDialog(){
		String title = resources.getString(R.string.add_server_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		int layout = R.layout.add_server_dialog;
		
		DialogFragment dialog = AddServerDialog.newInstance(title, ok, cancel, layout);
		dialog.show(fragManager, "addServerDialog");
	}
	
	public void showServersDialog(){
		String title = resources.getString(R.string.server_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		int layout = R.layout.servers_dialog;
		
		DialogFragment dialog = ServersDialog.newInstance(title, ok, cancel, layout);
		dialog.show(fragManager, "serversDialog");
	}
	
	public void showAddLayersDialog(Layer[] layersInProject){
		String title = resources.getString(R.string.add_layers_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		int layout = R.layout.add_layers_dialog;
		
		DialogFragment dialog;
		
		dialog = AddLayersDialog.
				newInstance(title, ok, cancel, layout, layersInProject);
		
		dialog.show(fragManager, "addLayersDialog");
	}
	
	public void showAddLayersDialog(boolean creatingProject){
		String title = resources.getString(R.string.add_layers_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		int layout = R.layout.add_layers_dialog;
		
		DialogFragment dialog;
		
		dialog = AddLayersDialog.
				newInstance(title, ok, cancel, layout, creatingProject);
		
		dialog.show(fragManager, "addLayersDialog");
	}
	
	public void showGoOfflineDialog(boolean creatingProject){
		String title = resources.getString(R.string.go_offline_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		int layout = R.layout.add_layers_dialog;
		
		DialogFragment dialog = GoOfflineDialog.newInstance(title, ok, cancel, layout, creatingProject);
		dialog.show(fragManager, "goOfflineDialog");
	}
	
	public void showChooseAOIDialog(){
		String title = resources.getString(R.string.choose_aoi_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		int layout = R.layout.choose_aoi_dialog;
		
		DialogFragment dialog = ChooseAOIDialog.newInstance(title, ok, cancel, layout);
		dialog.show(fragManager, "chooseAOIDialog");
	}
	
	public void showLayersDialog(){
		String title = resources.getString(R.string.layers_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		int layout = R.layout.layers_dialog;
		
		DialogFragment dialog = LayersDialog.newInstance(title, ok, cancel, layout);
		dialog.show(fragManager, "layersDialog");
	}
	
	public void showLayerInfoDialog(){
		String title = resources.getString(R.string.layer_info_dialog_title);
		String ok = resources.getString(android.R.string.ok);
		String cancel = resources.getString(android.R.string.cancel);
		int layout = R.layout.layer_info_dialog;
		
		DialogFragment dialog = LayerInfoDialog.newInstance(title, ok, cancel, layout);
		dialog.show(fragManager, "layerInfoDialog");
	}
}
