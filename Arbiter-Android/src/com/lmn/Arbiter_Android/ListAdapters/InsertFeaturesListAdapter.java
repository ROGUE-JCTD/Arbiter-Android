package com.lmn.Arbiter_Android.ListAdapters;

import java.util.ArrayList;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.GeometryColumnsHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.ChooseGeometryTypeDialog;
import com.lmn.Arbiter_Android.Map.Map.MapChangeListener;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class InsertFeaturesListAdapter extends BaseAdapter implements ArbiterAdapter<ArrayList<Layer>>{

	private MapChangeListener mapChangeListener;
	
	private ArrayList<Layer> items;
	private final LayoutInflater inflater;
	private int itemLayout;
	private Context context;
	private DialogFragment dialog;
	private HasThreadPool hasThreadPool;
	
	public InsertFeaturesListAdapter(DialogFragment dialog, int itemLayout){
		
		this.context = dialog.getActivity().getApplicationContext();
		this.inflater = LayoutInflater.from(this.context);
		this.items = new ArrayList<Layer>();
		this.itemLayout = itemLayout;
		this.dialog = dialog;
		
		try {
			mapChangeListener = (MapChangeListener) dialog.getActivity();
		} catch (ClassCastException e){
			throw new ClassCastException(dialog.getActivity().toString() 
					+ " must implement MapChangeListener");
		}
		
		try {
			hasThreadPool = (HasThreadPool) dialog.getActivity();
		} catch (ClassCastException e){
			throw new ClassCastException(dialog.getActivity()
					.toString() + " must implement HasThreadPool");
		}
	}
	
	public void setData(ArrayList<Layer> data){
		items = data;
		
		notifyDataSetChanged();
	}
	
	private void startInsertMode(final String featureType, final long layerId, final String geometryType){
		
		dialog.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				
				mapChangeListener.getMapChangeHelper().startInsertMode(featureType, layerId, geometryType);
				
				dialog.dismiss();
			}
		});
	}
	
	private void showGeometryTypeChooser(final String featureType,
			final long layerId, final String geometryType){
		
		dialog.getActivity().runOnUiThread(new Runnable(){
			
			@Override
			public void run(){
				
				String title = context.getResources().getString(R.string.choose_geometry_type);
				String cancel = context.getResources().getString(android.R.string.cancel);
				
				ChooseGeometryTypeDialog chooseGeometryTypeDialog = 
						ChooseGeometryTypeDialog.newInstance(title, cancel,
								featureType, layerId, ChooseGeometryTypeAdapter.Mode.INSERT);
				
				chooseGeometryTypeDialog.show(dialog.getActivity()
						.getSupportFragmentManager(), ChooseGeometryTypeDialog.TAG);
				
				dialog.dismiss();
			}
		});
	}
	
	private void dismissProgressDialog(final ProgressDialog progressDialog){
		
		dialog.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				progressDialog.dismiss();
			}
		});
	}
	
	private SQLiteDatabase getFeatureDb(){
		String projectName = ArbiterProject.getArbiterProject()
				.getOpenProject(dialog.getActivity());
		
		return FeatureDatabaseHelper.getHelper(context,
				ProjectStructure.getProjectPath(projectName),
				false).getWritableDatabase();
	}
	
	private void chooseGeometryHandler(final String featureType, final long layerId){
		
		String title = context.getResources().getString(R.string.loading);
		String msg = context.getResources().getString(R.string.please_wait);
		
		final ProgressDialog progressDialog = ProgressDialog.show(dialog.getActivity(), title, msg, true);
		
		hasThreadPool.getThreadPool().execute(new Runnable(){
			@Override
			public void run(){
				
				String geometryType = GeometryColumnsHelper.getHelper()
						.getGeometryType(getFeatureDb(), featureType);
				
				Log.w("InsertFeaturesListAdapter", "InsertFeaturesListAdapter geometryType = " + geometryType);
				
				if(geometryType != null){
					
					if(geometryType.contains("MultiCurve")
							|| geometryType.contains("MultiGeometry") 
							|| geometryType.contains("Geometry")){
						showGeometryTypeChooser(featureType, layerId, geometryType);
					}else{
						startInsertMode(featureType, layerId, geometryType);
					}
				}
				
				dismissProgressDialog(progressDialog);
			}
		});
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent){
		View view = convertView;
		
		// Inflate the layout
		if(view == null){
			view = inflater.inflate(itemLayout, null);
		}
		
		final Layer layer = getItem(position);
		
		if(layer != null){
            TextView layerNameView = (TextView) view.findViewById(R.id.layerName);
            TextView serverNameView = (TextView) view.findViewById(R.id.serverName);
            
            if(serverNameView != null){
            	serverNameView.setText(layer.getServerName());
            }
            
            if(!layer.isReadOnly()){
            	
            	if(layerNameView != null){
            		
                	layerNameView.setText(layer.getLayerTitle());
                }
            	
            	view.setBackgroundColor(0x00000000);
            	
            	setBackground(view, dialog.getActivity().getResources().getDrawable(R.drawable.list_selector));
            	
            	view.setOnClickListener(new OnClickListener(){
        			@Override
        			public void onClick(View v){
        				
        				dialog.getActivity().runOnUiThread(new Runnable(){
        					@Override
        					public void run(){
        						chooseGeometryHandler(layer.getFeatureTypeNoPrefix(), layer.getLayerId());
        					}
        				});
        			}
        		});
            }else{
            	
            	if(layerNameView != null){
            		String readOnlyText = dialog.getActivity().getString(R.string.read_only);
                	layerNameView.setText(layer.getLayerTitle() + " (" + readOnlyText + ")");
                }
            	
            	setBackground(view, null);
            	
            	view.setBackgroundColor(dialog.getActivity().getResources().getColor(android.R.color.darker_gray));
            	
            	view.setOnClickListener(null);
            }
		}
		
		return view;
	}
	
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setBackground(View view, Drawable drawable){
		
		int sdk = android.os.Build.VERSION.SDK_INT;
    	if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
    	    view.setBackgroundDrawable(drawable);
    	} else {
    	    view.setBackground(drawable);
    	}
	}
	
	@Override
	public int getCount() {
		if(items == null){
			return 0;
		}
		
		return items.size();
	}

	@Override
	public Layer getItem(int position) {
		return items.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public ArrayList<Layer> getLayers(){
		return items;
	}
}
