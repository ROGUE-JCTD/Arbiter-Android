package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.Dialog.ProgressDialog.PictureProgressDialog;
import com.lmn.Arbiter_Android.Map.Map;

public class MediaPanel {
		
	private String key;
	private Activity activity;
	private CordovaWebView webview;
	
	private LinearLayout outerLayout;
	private LayoutInflater inflater;
	private ImageButton takePictureBtn;
	
	// Specifically for the error capabilities
	private EditText errorEditText;
	
	private Feature feature;
	private MediaLoader mediaLoader;
	private boolean isNillable;
	
	private ArrayList<String> mediaToSend;
	
	public MediaPanel(String key, Activity activity, Feature feature, boolean isNillable,
			LinearLayout outerLayout, LayoutInflater inflater){
		
		this.key = key;
		this.activity = activity;
		this.outerLayout = outerLayout;
		this.inflater = inflater;
		this.feature = feature;
		this.mediaToSend = new ArrayList<String>();
		this.isNillable = isNillable;
		
		try{
			
			webview = ((Map.CordovaMap) activity).getWebView();
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	private void takePicture() {
		String media = getMedia();
		
		String js = "javascript:Arbiter.MediaHelper.takePicture('"
		+ key + "'";
		
		Log.w("MediaPanel", "MediaPanel takePicture media = '" + media + "'");
		
		if(media != null && !"".equals(media)){
			Log.w("MediaPanel", "MediaPanel not one of those things");
			
			js += ", " + media;
		}
		
		js += ");";
		
		Log.w("MediaPanel", "MediaPanel takePicture js = " + js);
		
	    webview.loadUrl(js);
	}
	
	private void selectPicture() {
		String media = getMedia();
		
		String js = "javascript:Arbiter.MediaHelper.selectPicture('"
		+ key + "'";
		
		Log.w("MediaPanel", "MediaPanel selectPicture media = '" + media + "'");
		
		if(media != null && !"".equals(media)){
			Log.w("MediaPanel", "MediaPanel not one of those things");
			
			js += ", " + media;
		}
		
		js += ");";
		
		Log.w("MediaPanel", "MediaPanel selectPicture js = " + js);
		
	    webview.loadUrl(js);
	}
	
	private boolean cameraExists(){
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
	        return true;
	    }
	    
	    return false;
	}
	
	public boolean checkValidity(){
		
		String media = getMedia();
		
		if(media == null || "null".equals(media) || "undefined".equals(media) 
				|| "".equals(media) || "[]".equals(media)){
			
			if(!isNillable){
				
				if(errorEditText != null){
					errorEditText.setError(activity.getResources().getString(R.string.required_field));
				}
				
				return false;
			}
		}
		
		if(errorEditText != null){
			
			errorEditText.setError(null);
		}
		
		return true;
	}
	
	private String getMedia(){
		LinkedHashMap<String, String> attributes = feature.getAttributes();
		
		String media = attributes.get(key);
		
		/*if(media == null || media.equals("null") ||
				media.equals("undefined") || media.equals("")){
			
			media = "[]";
		}*/
		
		return media;
	}
	
	private RelativeLayout initMediaPanel(boolean startInEditMode){
				
		// Append scrollview
		RelativeLayout mediaLayout = (RelativeLayout) inflater
				.inflate(R.layout.feature_media, null);
		
		TextView mediaLabel = (TextView) mediaLayout
				.findViewById(R.id.attributeLabel);
		
		String label = key;
		
		if(!isNillable){
			label += "*";
		}
		
		mediaLabel.setText(label);
	    
		takePictureBtn = (ImageButton) mediaLayout.findViewById(R.id.takePicture);
		
		errorEditText = (EditText) mediaLayout.findViewById(R.id.errorEditText);
		

		takePictureBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if (cameraExists()) {
					activity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							Resources resources = activity.getResources();
							
							AlertDialog.Builder builder = new AlertDialog.Builder(activity);
							
							String title = null;
							String msg = null;
	
							title = resources.getString(R.string.picture_source);
							msg = resources.getString(R.string.select_picture_source);
							
							builder.setTitle(title);
							builder.setIcon(resources.getDrawable(R.drawable.icon));
							builder.setMessage(msg);
							builder.setNegativeButton(R.string.from_camera, new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									takePicture();
								}
							});
							builder.setPositiveButton(R.string.from_library, new DialogInterface.OnClickListener(){
								@Override
								public void onClick(DialogInterface dialog, int which) {
									selectPicture();
								}
							});
							
							builder.create().show();
						}
					});
				} else {
					selectPicture();
				}
			}
		});
		
		// Default start not in edit mode
		takePictureBtn.setVisibility(startInEditMode ? View.VISIBLE : View.GONE);
		
		// Append the mediaLayout to the dialog
		outerLayout.addView(mediaLayout);
		
		return mediaLayout;
	}
	
	public void loadMedia() throws JSONException{
		
		if(mediaLoader != null){
			mediaLoader.loadMedia();
		}
	}
	
	public void appendMedia(String media, boolean startInEditMode) throws JSONException{
		
		RelativeLayout mediaLayout = initMediaPanel(startInEditMode);
		
		LinearLayout mediaView = (LinearLayout) mediaLayout
						.findViewById(R.id.mediaList);
		
		this.mediaLoader = new MediaLoader(activity, key,
				feature, mediaView, inflater, new Runnable(){

					@Override
					public void run() {
						
						checkValidity();
					}
		});
		
		if (startInEditMode) {
			mediaLoader.setEditMode(true);
		}
		
		loadMedia();
	}
	
	public void setEditMode(boolean editMode){
		if(takePictureBtn != null && cameraExists()){
			//takePictureBtn.setEnabled(!takePictureBtn.isEnabled());
			takePictureBtn.setVisibility(editMode ? View.VISIBLE : View.GONE);
		}
		mediaLoader.setEditMode(editMode);
	}
	
	public void addMediaToSend(String fileName){
		mediaToSend.add(fileName);
		
		PictureProgressDialog.dismiss(activity);
	}
	
	public void clearMediaToSend(){
		mediaToSend.clear();
	}
	
	public ArrayList<String> getMediaToSend(){
		return mediaToSend;
	}
	
	public ArrayList<String> getMediaToDelete() {
		return mediaLoader.getMediaToDelete();
	}
}
