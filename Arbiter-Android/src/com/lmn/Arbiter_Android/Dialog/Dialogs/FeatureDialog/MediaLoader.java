package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.Dialog.Dialogs.MediaDialog;
import com.lmn.Arbiter_Android.Media.MediaHelper;

public class MediaLoader {
	// The width and height of the media image view in dp
	public static final int IMAGE_VIEW_WIDTH = 180;
	public static final int IMAGE_VIEW_HEIGHT = 180;
		
	private Activity activity;
	private String key;
	private Feature feature;
	private LinearLayout mediaView;
	private MediaHelper mediaHelper;
	private LayoutInflater inflater;
	private FragmentActivity fragActivity;
	
	public MediaLoader(Activity activity, String key, Feature feature,
			LinearLayout mediaView, LayoutInflater inflater){
		
		this.activity = activity;
		this.key = key;
		this.feature = feature;
		this.mediaView = mediaView;
		this.mediaHelper = new MediaHelper(activity);
		this.inflater = inflater;
		
		try{
			this.fragActivity = (FragmentActivity) activity;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	private void clearMedia(){
		mediaView.removeAllViews();
	}
	
	private String getMedia(){
		LinkedHashMap<String, String> attributes = feature.getAttributes();
		
		String media = attributes.get(key);
		
		if(media == null || media.equals("null") ||
				media.equals("undefined") || media.equals("")){
			
			media = "[]";
		}
		
		return media;
	}
	
	private int dpToPx(int dp){
		DisplayMetrics displayMetrics = activity.getApplicationContext()
				.getResources().getDisplayMetrics();
		
	    int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT)); 
	    
	    return px;
	}
	
	private int[] getImageDimensions(){
		int[] dimensions = new int[2];
		dimensions[0] = dpToPx(IMAGE_VIEW_WIDTH);
		dimensions[1] = dpToPx(IMAGE_VIEW_HEIGHT);
		
		return dimensions;
	}
	
	private Bitmap getWarningBitmap(MediaHelper helper, Integer width, Integer height){
		Bitmap scaledBitmap = null;
		
		Drawable drawable = activity.getApplicationContext()
				.getResources().getDrawable(R.drawable.warning);
		
		scaledBitmap = helper.getImageBitmap(
			((BitmapDrawable) drawable).getBitmap(),
			width,
			height
		);
		
		return scaledBitmap;
	}
	
	private void onMediaClick(String imageUri){
		MediaDialog.newInstance(imageUri).show(fragActivity
				.getSupportFragmentManager(), "MediaDialog");;
	}
	
	public void loadMedia() throws JSONException{
		clearMedia();
		
		String media = getMedia();
		
		if(media == null || media.equals("null") ||
				media.equals("") || media.equals("undefined")){
			
			return;
		}
		
		Log.w("MediaLoader", "MediaLoader.loadMedia() media = " + media);
		
		JSONArray mediaArray = new JSONArray(media);
		int count = mediaArray.length();
		
		if(count <= 0){
			
			return;
		}
		
		if(!mediaHelper.isExternalStorageReadable()){
			Log.w("FeatureDialogBuilder", "FeatureDialogBuilder.appendMedia"
					+ " - External storage is not available");
			
			return;
		}
		
		int[] imageDimensions = getImageDimensions();
		
		for(int i = 0; i < count; i++){
			final String uri = mediaHelper.getMediaUri(mediaArray.getString(i));
			
			ImageView imageView = (ImageView) inflater.inflate(R.layout.media_image_view, null);
			
			Bitmap image = mediaHelper.getImageBitmap(
				uri, 
				imageDimensions[0], 
				imageDimensions[1]
			);
			
			if(image == null){
				imageView.setImageBitmap(getWarningBitmap(mediaHelper,
						imageDimensions[0], imageDimensions[1]));
			}else{
				imageView.setImageBitmap(image);
			}
			
			imageView.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					
					onMediaClick(uri);
				}
			});
			
			mediaView.addView(imageView);
		}
	}
}
