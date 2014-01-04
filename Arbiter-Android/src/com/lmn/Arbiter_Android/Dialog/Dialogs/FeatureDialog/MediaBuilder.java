package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.LayoutInflater;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.Dialogs.MediaDialog;
import com.lmn.Arbiter_Android.Map.Map;
import com.lmn.Arbiter_Android.Media.MediaHelper;

public class MediaBuilder {
	// The width and height of the media image view in dp
	public static final int IMAGE_VIEW_WIDTH = 180;
	public static final int IMAGE_VIEW_HEIGHT = 180;
		
	private Activity activity;
	private FragmentActivity fragActivity;
	private CordovaWebView webview;
	
	private LinearLayout outerLayout;
	private LayoutInflater inflater;
	private ImageButton takePictureBtn;
	
	public MediaBuilder(Activity activity, LinearLayout outerLayout,
			LayoutInflater inflater){
		
		this.activity = activity;
		this.outerLayout = outerLayout;
		this.inflater = inflater;
		
		try{
			fragActivity = (FragmentActivity) activity;
			
			webview = ((Map.CordovaMap) activity).getWebView();
		}catch(ClassCastException e){
			e.printStackTrace();
		}
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
	
	private void takePicture(String key, String media) {
	    webview.loadUrl("javascript:Arbiter.MediaHelper.takePicture('"
	    		+ key + "', " + media + ");");
	}
	
	private void onMediaClick(String imageUri){
		MediaDialog.newInstance(imageUri).show(fragActivity
				.getSupportFragmentManager(), "MediaDialog");;
	}
	
	private boolean cameraExists(){
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
	        return true;
	    }
	    
	    return false;
	}
	
	public void appendMedia(final String key, final String media) throws JSONException{
		// Append scrollview
		RelativeLayout mediaLayout = (RelativeLayout) inflater
				.inflate(R.layout.feature_media, null);
		
		LinearLayout mediaView = (LinearLayout) mediaLayout
				.findViewById(R.id.mediaList);
		
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
					takePicture(key, media);	
				}
			});
			
			// Default start not in edit mode
			takePictureBtn.setEnabled(false);
		}
		
		if(media == null || media.equals("null")){
			
			outerLayout.addView(mediaLayout);
			
			return;
		}
		
		JSONArray mediaArray = new JSONArray(media);
		int count = mediaArray.length();
		MediaHelper mediaHelper = new MediaHelper(activity);
		
		if(count <= 0){
			outerLayout.addView(mediaLayout);
			
			return;
		}
		
		if(!mediaHelper.isExternalStorageReadable()){
			Log.w("FeatureDialogBuilder", "FeatureDialogBuilder.appendMedia"
					+ " - External storage is not available");
			
			outerLayout.addView(mediaLayout);
			
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
		
		outerLayout.addView(mediaLayout);
	}
	
	public void toggleEditMode(){
		if(takePictureBtn != null){
			takePictureBtn.setEnabled(!takePictureBtn.isEnabled());
		}
	}
}
