package com.lmn.Arbiter_Android.GeometryEditor;

import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;

import org.apache.cordova.CordovaWebView;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ArbiterState;
import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Util;
import com.lmn.Arbiter_Android.Activities.HasThreadPool;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.BaseClasses.Layer;
import com.lmn.Arbiter_Android.CordovaPlugins.Helpers.FeatureHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.LayersHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.ChooseGeometryTypeDialog;
import com.lmn.Arbiter_Android.ListAdapters.ChooseGeometryTypeAdapter;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.OnAddingGeometryPart.OnAddingGeometryPart;
import com.lmn.Arbiter_Android.OnAddingGeometryPart.OnAddingGeometryPartJob;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

import android.app.Activity;
import android.app.ProgressDialog;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;

public class GeometryEditor {

	private WeakReference<Activity> weakActivity;
	private Button cancelBtn;
	private Button doneBtn;
	
	//private ImageButton inspectBtn;
	//private ImageButton editBtn;
	private ImageButton addPartBtn;
	private ImageButton removePartBtn;
	private ImageButton addGeometryBtn;
	private ImageButton removeGeometryBtn;
	
	private InsertHandler insertHandler;
	private EditHandler editHandler;
	
	private String featureType;
	private String featureId;
	private String layerId;
	private String wktGeometry;
	private Feature feature;
	
	public static class Mode {
		public static final int OFF = 0;
		public static final int INSERT = 1;
		public static final int EDIT = 2;
		public static final int SELECT = 3;
	}
	
	private int editMode;
	
	public GeometryEditor(Activity activity){
		weakActivity = new WeakReference<Activity>(activity);
		
		editMode = Mode.OFF;
		
		if(activity != null){
			cancelBtn = (Button) activity.findViewById(R.id.cancelButton1);
			doneBtn = (Button) activity.findViewById(R.id.doneButton1);
			
			//inspectBtn = (ImageButton) activity.findViewById(R.id.infoBtn);
			//editBtn = (ImageButton) activity.findViewById(R.id.editBtn);
			addPartBtn = (ImageButton) activity.findViewById(R.id.addPartBtn);
			removePartBtn = (ImageButton) activity.findViewById(R.id.removePartBtn);
			addGeometryBtn = (ImageButton) activity.findViewById(R.id.addToCollectionBtn);
			removeGeometryBtn = (ImageButton) activity.findViewById(R.id.removeFromCollectionBtn);
			
			editHandler = new EditHandler(activity);
			insertHandler = new InsertHandler(activity);
			
			try{
				registerButtons();
			}catch(ClassCastException e){
				e.printStackTrace();
			}
			
		}
	}
	
	private void registerButtons() throws ClassCastException{
		
		final Activity activity = weakActivity.get();
		
		if(activity != null){
			
			final CordovaWebView webview = ((Map.CordovaMap) activity).getWebView();
			
			this.cancelBtn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					if(editMode == Mode.EDIT){
						
						editHandler.cancel();
						
						setEditMode(Mode.SELECT);
						
						ArbiterState.getArbiterState().doneEditingFeature();
					}else if(editMode == Mode.INSERT){
						insertHandler.cancel(new Runnable(){
							@Override
							public void run(){
								setEditMode(Mode.OFF);
								
								ArbiterState.getArbiterState().doneEditingFeature();
							}
						});
					}
				}
			});
			
			this.doneBtn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					if(editMode == Mode.EDIT){
						Log.w("GeometryEditor", "GeometryEditor doneButton editMode = " + Mode.EDIT);
						editHandler.done();
					}else if(editMode == Mode.INSERT){
						Log.w("GeometryEditor", "GeometryEditor doneButton editMode = " + Mode.INSERT);
						insertHandler.done();
					}
					
					ArbiterState.getArbiterState().doneEditingFeature();
					
					setEditMode(Mode.OFF);
				}
			});
			
			this.addPartBtn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					Map.getMap().addPart(webview);
				}
			});
			
			this.removePartBtn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					Map.getMap().removePart(webview);
				}
			});
			
			this.addGeometryBtn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					OnAddingGeometryPart.getInstance().add(new OnAddingGeometryPartJob(){

						@Override
						public void run(boolean isAddingPartAlready) {
							
							if(!isAddingPartAlready){
								showAddGeometryChooser();
							}
						}
					});
					
					Map.getMap().checkIsAlreadyAddingGeometryPart(webview);
				}
			});
			
			this.removeGeometryBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v){
					Map.getMap().removeGeometry(webview);
				}
			});
		}
	}
	
	private void showAddGeometryChooser(){
		
		FragmentActivity activity = null;
		
		try{
			activity = (FragmentActivity) weakActivity.get();
		}catch(ClassCastException e){
			e.printStackTrace();
		}
		
		if(activity != null){
			String title = activity.getResources().getString(R.string.choose_geometry_type);
			String cancel = activity.getResources().getString(android.R.string.cancel);
			
			ChooseGeometryTypeDialog chooseGeometryTypeDialog = 
					ChooseGeometryTypeDialog.newInstance(title, cancel,
							featureType, Long.parseLong(layerId),
							ChooseGeometryTypeAdapter.Mode.ADD_PART);
			
			chooseGeometryTypeDialog.show(activity.getSupportFragmentManager(), ChooseGeometryTypeDialog.TAG);
		}		
	}
	
	private void startEditMode(){
		
		Activity activity = weakActivity.get();
		
		if(activity != null){
			
			// Activate modify mode on the js side
			try{
				final CordovaWebView webview = ((Map.CordovaMap) activity).getWebView();
				
				Map.getMap().enterModifyMode(webview);
			}catch(ClassCastException e){
				e.printStackTrace();
			}
			
			ArbiterState.getArbiterState().editingFeature(feature, layerId);
			
			toggleConfirmBtns(true);
		}
	}
	
	private void toggleMultiPartBtns(boolean visibility){
		
		int visible;
		
		Log.w("GeometryEditor", "GeometryEditor.toggleMultiPartBtns(" + visibility + ")");
		
		if(visibility){
			visible = View.VISIBLE;
		}else{
			visible = View.GONE;
		}
		
		this.addPartBtn.setVisibility(visible);
		this.removePartBtn.setVisibility(visible);
		this.addGeometryBtn.setVisibility(visible);
		this.removeGeometryBtn.setVisibility(visible);
	}
	
	private void toggleConfirmBtns(boolean visibility){
		
		int visible;
		
		if(visibility){
			visible = View.VISIBLE;
		}else{
			visible = View.GONE;
		}
		
		this.cancelBtn.setVisibility(visible);
		
		this.doneBtn.setVisibility(visible);
	}
	
	public int getEditMode(){
		return this.editMode;
	}
	
	public void setEditMode(int editMode){
		
		this.editMode = editMode;
		
		switch(editMode){
			case Mode.INSERT:
			
				Log.w("GeometyEditor", "GeometryEditor.setEditMode INSERT");
				toggleMultiPartBtns(false);
				toggleConfirmBtns(true);
				
				break;
			
			case Mode.EDIT:
				
				Log.w("GeometyEditor", "GeometryEditor.setEditMode EDIT");
				toggleMultiPartBtns(false);
				toggleConfirmBtns(true);
				
				if(this.wktGeometry.contains("GEOMETRYCOLLECTION")){
					this.addGeometryBtn.setVisibility(View.VISIBLE);
				}
				
				startEditMode();
				
				break;
				
			case Mode.SELECT:
				
				Log.w("GeometyEditor", "GeometryEditor.setEditMode SELECT");
				toggleMultiPartBtns(false);
				toggleConfirmBtns(false);
				
				try{
					
					final Activity activity = weakActivity.get();
					
					if(activity != null && layerId != null){
						
						ExecutorService threadPool = ((HasThreadPool) activity).getThreadPool();
						String title = activity.getResources().getString(R.string.loading);
						String message = activity.getResources().getString(R.string.please_wait);
						
						final ProgressDialog dialog = ProgressDialog.show(activity, title, message);
						
						threadPool.execute(new Runnable(){
							@Override
							public void run(){
								
								final Layer layer = LayersHelper.getLayersHelper().get((new Util().getProjectDb(
										activity, false)), Integer.parseInt(layerId));
								
								activity.runOnUiThread(new Runnable(){
									@Override
									public void run(){
										
										displayInfoDialog(false, layer.isReadOnly());
										
										dialog.dismiss();
									}
								});
							}
						});
					}
				}catch(ClassCastException e){
					e.printStackTrace();
				}
				
				break;
				
			case Mode.OFF:
				Log.w("GeometyEditor", "GeometryEditor.setEditMode OFF");
				toggleMultiPartBtns(false);
				toggleConfirmBtns(false);
				
				break;
			
			default:
				
				try {
					throw new Exception("Invalid Mode: " + editMode);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public void enableMultiPartBtns(boolean enable, boolean enableCollection){
		
		if(enable){
			this.addPartBtn.setVisibility(View.VISIBLE);
			this.removePartBtn.setVisibility(View.VISIBLE);
		}else{
			this.addPartBtn.setVisibility(View.GONE);
			this.removePartBtn.setVisibility(View.GONE);
		}
		
		if(enableCollection){
			this.addGeometryBtn.setVisibility(View.VISIBLE);
			this.removeGeometryBtn.setVisibility(View.VISIBLE);
		}else{
			this.addGeometryBtn.setVisibility(View.GONE);
			this.removeGeometryBtn.setVisibility(View.GONE);
		}
	}
	
	public void setFeatureInfo(String featureType, String featureId,
			String layerId, String wktGeometry, Runnable onSetFeature){
		
		this.featureType = featureType;
		this.featureId = featureId;
		this.layerId = layerId;
		this.wktGeometry = wktGeometry;
		
		setFeature(featureType, featureId, onSetFeature);
	}
	
	public void hidePartButtons(){
		this.addPartBtn.setVisibility(View.GONE);
		this.removePartBtn.setVisibility(View.GONE);
		this.removeGeometryBtn.setVisibility(View.GONE);
	}
	
	private void setFeature(final String featureType, final String featureId, final Runnable onSetFeature){
		final Activity activity = weakActivity.get();
		
		if(activity != null){
			
			try{
				ExecutorService threadPool = ((HasThreadPool) activity).getThreadPool();
				String title = activity.getResources().getString(R.string.loading);
				String message = activity.getResources().getString(R.string.please_wait);
				
				final ProgressDialog dialog = ProgressDialog.show(activity, title, message);
				
				Log.w("GeometryEditor", "GeometryEditor: featureId = '" + featureId + "'");
				
				threadPool.execute(new Runnable(){
					@Override
					public void run(){
						
						String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
						
						SQLiteDatabase db = FeatureDatabaseHelper.getHelper(activity.getApplicationContext(),
								ProjectStructure.getProjectPath(projectName), false).getWritableDatabase();
						
						if(featureId != null && featureId != "null"){
							
							final Feature _feature = FeaturesHelper.getHelper().getFeature(db, featureId, featureType);
							
							activity.runOnUiThread(new Runnable(){
								@Override
								public void run(){
									
									feature = _feature;
									
									onSetFeature.run();
									
									dialog.dismiss();
								}
							});
						}else{
							
							final Feature _feature = FeaturesHelper.getHelper().getNewFeature(db, featureType, wktGeometry);
							
							activity.runOnUiThread(new Runnable(){
								@Override
								public void run(){
									
									feature = _feature;
									
									onSetFeature.run();
									
									dialog.dismiss();
								}
							});
						}
					}
				});
			}catch(ClassCastException e){
				e.printStackTrace();
			}
		}
	}
	
	public void showUpdatedGeometry(final String featureType,
			final String featureId, final String layerId, final String wktGeometry){
		
		this.featureType = featureType;
		this.featureId = featureId;
		this.layerId = layerId;
		this.wktGeometry = wktGeometry;
		
		final Activity activity = weakActivity.get();
		
		Log.w("GeometryEditor", "GeometryEditor featureId: " + featureId);
		if(activity != null){
			
			try{
				ExecutorService threadPool = ((HasThreadPool) activity).getThreadPool();
				String title = activity.getResources().getString(R.string.loading);
				String message = activity.getResources().getString(R.string.please_wait);
				
				final ProgressDialog dialog = ProgressDialog.show(activity, title, message);
				
				threadPool.execute(new Runnable(){
					@Override
					public void run(){
						
						String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
						
						SQLiteDatabase db = FeatureDatabaseHelper.getHelper(activity.getApplicationContext(),
								ProjectStructure.getProjectPath(projectName), false).getWritableDatabase();
						
						if(featureId == null || featureId.equals("null")){
							feature = FeaturesHelper.getHelper().getNewFeature(db, featureType, wktGeometry);
						}else{
							feature = FeaturesHelper.getHelper().getFeature(db, featureId, featureType);
						}
						
						feature.updateAttribute(feature.getGeometryName(), wktGeometry);
						//feature.backupGeometry();
						
						final Layer layer = LayersHelper.getLayersHelper().get(
								(new Util().getProjectDb(activity, false)), Integer.parseInt(layerId));
						
						activity.runOnUiThread(new Runnable(){
							@Override
							public void run(){
								
								displayInfoDialog(true, layer.isReadOnly());
								
								dialog.dismiss();
							}
						});
					}
				});
			}catch(ClassCastException e){
				e.printStackTrace();
			}
		}
	}
	
	private void displayInfoDialog(boolean geomEdited, boolean isReadOnly){
		Activity activity = weakActivity.get();
		
		if(activity != null){
			try{
				Log.w("GeometryEditor", "GeometryEditor displayInfoDialog featureId = " + featureId + ", wktGeometry = " + wktGeometry);
				FeatureHelper helper = new FeatureHelper((FragmentActivity) activity);
				helper.displayFeatureDialog(feature, layerId, geomEdited, isReadOnly);
			}catch(ClassCastException e){
				e.printStackTrace();
			}
		}
	}
}
