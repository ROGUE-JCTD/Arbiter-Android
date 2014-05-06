package com.lmn.Arbiter_Android.Dialog.Dialogs;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Media.MediaHelper;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class MediaDialog extends DialogFragment {
	private String uri;
	private int layout;
	private static MediaDialog dialog = null;
	
	public static MediaDialog newInstance(String uri){
		if (dialog != null) {
			return null;
		}
		dialog = new MediaDialog();
		dialog.uri = uri;
		dialog.layout = R.layout.media_dialog;
		
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
		
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setView(view).create();
		
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		return dialog;
	}
}
