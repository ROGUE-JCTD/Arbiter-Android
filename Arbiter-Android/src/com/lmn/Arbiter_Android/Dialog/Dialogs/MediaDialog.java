package com.lmn.Arbiter_Android.Dialog.Dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.Dialogs.FeatureDialog.MediaLoader;
import com.lmn.Arbiter_Android.Media.MediaHelper;

public class MediaDialog extends DialogFragment {
	private String uri;
	private int layout;
	private static MediaDialog dialog = null;
	private MediaLoader loader;
	
	public static MediaDialog newInstance(String uri, MediaLoader loader){
		if (dialog != null) {
			return null;
		}
		dialog = new MediaDialog();
		dialog.uri = uri;
		dialog.layout = R.layout.media_dialog;
		dialog.loader = loader;
		
		return dialog;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		dialog = null;
	}
	
	private Bitmap getWarningBitmap(){
		Bitmap bitmap = null;
		
		Drawable drawable = getActivity().getApplicationContext()
				.getResources().getDrawable(R.drawable.warning);
		
		bitmap = ((BitmapDrawable) drawable).getBitmap();
		
		return bitmap;
	}
	
	private Bitmap getImageBitmap(String uri){
		Bitmap bitmap = null;
		
		MediaHelper helper = new MediaHelper(getActivity());
		WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		DisplayMetrics displayMetrics = new DisplayMetrics();
		display.getMetrics(displayMetrics);
		
		bitmap = helper.getImageBitmap(uri, displayMetrics.widthPixels, displayMetrics.heightPixels);
		
		return bitmap;
	}
	
	private void setMediaResource(View view){
		ImageView imageView = (ImageView) view.findViewById(R.id.imageView);
		
		Bitmap bitmap = getImageBitmap(uri);
		
		if(bitmap == null){
			bitmap = getWarningBitmap();
		}
		
		imageView.setImageBitmap(bitmap);
		imageView.setScaleType(ScaleType.FIT_CENTER);
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(layout, null);
		
		setMediaResource(view);
		
		final AlertDialog mediaDialog = new AlertDialog.Builder(getActivity())
			.setView(view).create();
		
		mediaDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		Button deleteMedia = (Button) view.findViewById(R.id.deleteMedia);
		deleteMedia.setVisibility(loader.getEditMode() ? View.VISIBLE : View.GONE);
		deleteMedia.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {	
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			    			
			    			builder.setIcon(R.drawable.icon);
			    			builder.setTitle(R.string.warning);
			    			
			    			String message = getActivity().getResources()
			    					.getString(R.string.sure_delete_media);
			    			
			    			builder.setMessage(message);
			    			builder.setNegativeButton(android.R.string.cancel, null);
			    			builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener(){
		
								@Override
								public void onClick(DialogInterface dialog, int which) {
									loader.deleteMedia(uri);
									mediaDialog.dismiss();
								}
			    			});
			    			builder.create().show();
						}
					});
				}
			});
		
		return mediaDialog;
	}
}
