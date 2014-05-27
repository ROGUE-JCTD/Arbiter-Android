package com.lmn.Arbiter_Android.Media;

import java.io.File;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.lmn.Arbiter_Android.ArbiterProject;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;

public class MediaHelper {
	public static final String MEDIA = "photos";
	public static final String FOTOS = "fotos";
	
	private Activity activity;
	
	public MediaHelper(Activity activity){
		this.activity = activity;
	}
	
	public String getMediaUri(String fileName){
		
		String uri = ProjectStructure.getMediaPath(ArbiterProject.getArbiterProject()
				.getOpenProject(activity)) + File.separator
				+ fileName;
		
		return uri;
	}
	
	public String getMediaFileFromUri(String fullUri){
		
		return fullUri.substring(fullUri.lastIndexOf(File.separator) + 1);
	}
	
	public boolean isExternalStorageReadable(){
		String state = Environment.getExternalStorageState();
	    
		if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    
	    return false;
	}
	
	private Bitmap scaleBitmap(Bitmap bitmap, Integer scaleWidth, Integer scaleHeight){
		return Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);
	}
	
	private Bitmap scaleBitmap(String imageUri, Integer scaleWidth, Integer scaleHeight){
		Bitmap scaledBitmap = null;
		BitmapFactory.Options bmOptions = null;
		
		if(imageUri != null){
			
			if(scaleWidth != null && scaleHeight != null){
				// Get the dimensions of the bitmap
				bmOptions = new BitmapFactory.Options();
				bmOptions.inJustDecodeBounds = true;
				
				BitmapFactory.decodeFile(imageUri, bmOptions);
				
				int photoWidth = bmOptions.outWidth;
				int photoHeight = bmOptions.outHeight;
				
				// Determine how much to scale down the image
				int scaleFactor = Math.min((photoWidth + scaleWidth - 1)/scaleWidth, (photoHeight + scaleHeight - 1)/scaleHeight);
				
				// Decode the image file scaled to the width and height
				bmOptions.inJustDecodeBounds = false;
				bmOptions.inSampleSize = scaleFactor;
				bmOptions.inPurgeable = true;
				
				Log.w("MediaHelper", "MediaHelper.scaleBitmap photoWidth = " 
						+ photoWidth + ", photoHeight = " + photoHeight);
			}
			
			scaledBitmap = BitmapFactory.decodeFile(imageUri, bmOptions);
		}
		
		return scaledBitmap;
	}
	
	/**
	 * 
	 * @param imageUri File uri of the image.
	 * @param scaleWidth Width to scale to.
	 * @param scaleHeight Height to scale to.
	 * @return
	 */
	public Bitmap getImageBitmap(String imageUri, Integer scaleWidth, Integer scaleHeight){
		Log.w("MediaHelper", "MediaHelper.getImageBitmap imageUri = "
				+ imageUri + ", width = " + scaleWidth 
				+ ", height = " + scaleHeight);
		
		File file = new File(imageUri);
		Bitmap bitmap = null;
		
		if(file.exists()){
			
			Log.w("MediaHelper", "MediaHelper.getImageBitmap file exists");
			bitmap = scaleBitmap(file.getAbsolutePath(),
					scaleWidth, scaleHeight);
		}
		
		if(bitmap == null){
			Log.w("MediaHelper", "MediaHelper.getImageBitmap bitmap is null");
		}
		
		return bitmap;
	}
	
	/**
	 * 
	 * @param imageUri File uri of the image.
	 * @param scaleWidth Width to scale to.
	 * @param scaleHeight Height to scale to.
	 * @return
	 */
	public Bitmap getImageBitmap(Bitmap bitmap, Integer scaleWidth, Integer scaleHeight){

		Bitmap scaledBitmap = scaleBitmap(bitmap,
					scaleWidth, scaleHeight);
		
		if(scaledBitmap == null){
			Log.w("MediaHelper", "MediaHelper.getImageBitmap scaledBitmap is null");
		}
		
		return scaledBitmap;
	}
}
