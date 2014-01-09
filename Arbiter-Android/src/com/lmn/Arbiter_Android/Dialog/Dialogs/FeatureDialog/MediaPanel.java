package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.ArrayList;

import org.apache.cordova.CordovaWebView;
import org.json.JSONException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.Map.Map;

public class MediaPanel {
		
	private Activity activity;
	private CordovaWebView webview;
	
	private LinearLayout outerLayout;
	private LayoutInflater inflater;
	private ImageButton takePictureBtn;
	private Feature feature;
	private MediaLoader mediaLoader;
	
	private ArrayList<String> mediaToSend;
	
	public MediaPanel(Activity activity, Feature feature, 
			LinearLayout outerLayout, LayoutInflater inflater){
		
		this.activity = activity;
		this.outerLayout = outerLayout;
		this.inflater = inflater;
		this.feature = feature;
		this.mediaToSend = new ArrayList<String>();
		
		try{
			
			webview = ((Map.CordovaMap) activity).getWebView();
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	private void takePicture(String key) {
		String media = getMedia(key);
		
	    webview.loadUrl("javascript:Arbiter.MediaHelper.takePicture('"
	    		+ key + "', " + media + ");");
	}
	
	private boolean cameraExists(){
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
	        return true;
	    }
	    
	    return false;
	}
	
	private String getMedia(String key){
		ContentValues attributes = feature.getAttributes();
		
		String media = attributes.getAsString(key);
		
		if(media == null || media.equals("null") ||
				media.equals("undefined") || media.equals("")){
			
			media = "[]";
		}
		
		return media;
	}
	
	private RelativeLayout initMediaPanel(final String key, boolean startInEditMode){
				
		// Append scrollview
		RelativeLayout mediaLayout = (RelativeLayout) inflater
				.inflate(R.layout.feature_media, null);
		
		TextView mediaLabel = (TextView) mediaLayout
				.findViewById(R.id.attributeLabel);
		
		mediaLabel.setText(key);
	    
		takePictureBtn = (ImageButton) mediaLayout.findViewById(R.id.takePicture);
		
		if(!cameraExists()){
			// If the camera doesn't exist, then make the takePictureBtn hidden
			takePictureBtn.setVisibility(View.GONE);
		}else{
			takePictureBtn.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					takePicture(key);	
				}
			});
			
			// Default start not in edit mode
			takePictureBtn.setEnabled(startInEditMode);
		}
		
		// Append the mediaLayout to the dialog
		outerLayout.addView(mediaLayout);
		
		return mediaLayout;
	}
	
	public void loadMedia() throws JSONException{
		
		if(mediaLoader != null){
			mediaLoader.loadMedia();
		}
	}
	
	public void appendMedia(String key, String media, boolean startInEditMode) throws JSONException{
		
		RelativeLayout mediaLayout = initMediaPanel(key, startInEditMode);
		
		LinearLayout mediaView = (LinearLayout) mediaLayout
						.findViewById(R.id.mediaList);
		
		this.mediaLoader = new MediaLoader(activity, key,
				feature, mediaView, inflater);
		
		loadMedia();
	}
	
	public void setEditMode(boolean editMode){
		if(takePictureBtn != null){
			//takePictureBtn.setEnabled(!takePictureBtn.isEnabled());
			takePictureBtn.setEnabled(editMode);
		}
	}
	
	public void addMediaToSend(String fileName){
		mediaToSend.add(fileName);
	}
	
	public void clearMediaToSend(){
		mediaToSend.clear();
	}
	
	public ArrayList<String> getMediaToSend(){
		return mediaToSend;
	}
}
