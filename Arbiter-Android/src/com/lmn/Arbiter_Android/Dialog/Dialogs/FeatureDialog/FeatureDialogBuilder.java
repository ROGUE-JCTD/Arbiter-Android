package com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import android.app.Activity;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.BaseClasses.Feature;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.FeaturesHelper;
import com.lmn.Arbiter_Android.Dialog.Dialogs.MediaDialog;
import com.lmn.Arbiter_Android.Media.MediaHelper;

public class FeatureDialogBuilder {
	// The width and height of the media image view in dp
	public static final int IMAGE_VIEW_WIDTH = 180;
	public static final int IMAGE_VIEW_HEIGHT = 180;
	
	private Activity activity;
	private FragmentActivity fragActivity;
	
	private View view;
	private Feature feature;
	private LayoutInflater inflater;
	private ArrayList<EditText> editTexts;
	private LinearLayout outerLayout;
	
	public FeatureDialogBuilder(Activity activity, View view, Feature feature){
		this.activity = activity;
		this.view = view;
		this.feature = feature;
		this.editTexts = new ArrayList<EditText>();
		
		inflater = activity.getLayoutInflater();
		
		try{
			fragActivity = (FragmentActivity) activity;
		}catch(ClassCastException e){
			e.printStackTrace();
		}
	}
	
	public void build(){
		String geometryName = feature.getGeometryName();
		
		String value = null;
		
		ContentValues attributes = feature.getAttributes();
		
		for(String key : attributes.keySet()){
			value = attributes.getAsString(key);
			
			if(!key.equals(FeaturesHelper.SYNC_STATE) 
					&& !key.equals(FeaturesHelper.MODIFIED_STATE) 
					&& !key.equals(FeaturesHelper.FID)){
				
				if(key.equals(MediaHelper.MEDIA) || key.equals(MediaHelper.FOTOS)){
					try {
						Log.w("FeatureDialogBuilder", "FeatureDialogBuilder.appendMedia(" + value + ")");
						
						appendMedia(key, value);
					} catch (JSONException e) {
						Log.e("FeatureDialogBuilder", "FeatureDialogBuilder.build() could not parse media json");
						e.printStackTrace();
					}
				}else{
					appendAttribute(key, value);
				}
			}
		}
	}
	
	private LinearLayout getOuterLayout(){
		if(outerLayout == null){
			outerLayout = (LinearLayout) view.findViewById(R.id.outerLayout);
		}
		
		return outerLayout;
	}
	
	private void appendGeometry(String geometry){
		
		
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
		Log.w("FeatureDialogBuilder", "FeatureDialogBuilder.onMediaClick uri = " + imageUri);
		
		MediaDialog.newInstance(imageUri).show(fragActivity.getSupportFragmentManager(), "MediaDialog");;
	}
	
	private void appendMedia(String key, String media) throws JSONException{
		if(media == null || media.equals("null")){
			return;
		}
		
		JSONArray mediaArray = new JSONArray(media);
		int count = mediaArray.length();
		MediaHelper mediaHelper = new MediaHelper(activity);
		
		if(count <= 0){
			return;
		}
		
		if(!mediaHelper.isExternalStorageReadable()){
			Log.w("FeatureDialogBuilder", "FeatureDialogBuilder.appendMedia"
					+ " External storage is not available");
			
			return;
		}
		
		LinearLayout outer = getOuterLayout();
		
		// Append scrollview
		RelativeLayout mediaLayout = (RelativeLayout) inflater
				.inflate(R.layout.feature_media, null);
		
		LinearLayout mediaView = (LinearLayout) mediaLayout
				.findViewById(R.id.mediaList);
		
		TextView mediaLabel = (TextView) mediaLayout
				.findViewById(R.id.attributeLabel);
		
		mediaLabel.setText(key);
		
		int[] imageDimensions = getImageDimensions();
		
		for(int i = 0; i < count; i++){
			final String uri = mediaHelper.getMediaUri(mediaArray.getString(i));
			
			ImageView imageView = (ImageView) inflater.inflate(R.layout.media_image_view, null);
			Log.w("FeatureDialogBuilder", "FeatureDialogBuilder.appendMedia width = " 
					+ imageDimensions[0] + ", height = " + imageDimensions[1]);
			
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
		
		Log.w("FeatureDialogBuilder", "FeatureDialogBuilder.appendMedia adding view now");
		outer.addView(mediaLayout);
	}
	
	private void appendAttribute(String key, String value){
		LinearLayout outer = getOuterLayout();
		
		View attributeView = inflater.inflate(R.layout.feature_attribute, null);
		
		if(key != null){
			TextView attributeLabel = (TextView) attributeView.findViewById(R.id.attributeLabel);
			
			if(attributeLabel != null){
				attributeLabel.setText(key);
			}
		}
		
		EditText attributeValue = (EditText) attributeView.findViewById(R.id.attributeText);
		
		if(attributeValue != null){
			attributeValue.setText(value);
		}
		
		outer.addView(attributeView);
		
		editTexts.add(attributeValue);
	}
	
	public boolean toggleEditTexts(){
		EditText editText = null;
		boolean focusable = false;
		
		for(int i = 0, count = editTexts.size(); i < count; i++){
			editText = editTexts.get(i);
			focusable = editText.isFocusable();
			
			editText.setFocusable(!focusable);
			editText.setFocusableInTouchMode(!focusable);
		}
		
		return !focusable;
	}
	
	public void updateFeature(){
		ContentValues attributes = feature.getAttributes();
		int i = 0;
		
		for(String key : attributes.keySet()){
			if(!key.equals(FeaturesHelper.SYNC_STATE) 
					&& !key.equals(FeaturesHelper.MODIFIED_STATE)
					&& !key.equals(FeaturesHelper.FID)){
				
				attributes.put(key, editTexts.get(i++)
					.getText().toString());
			}
		}
	}
}
