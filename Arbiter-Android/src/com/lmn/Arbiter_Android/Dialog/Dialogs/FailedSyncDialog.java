package com.lmn.Arbiter_Android.Dialog.Dialogs;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.lmn.Arbiter_Android.R;
import com.lmn.Arbiter_Android.Dialog.ProgressDialog.SyncProgressDialog;
import com.lmn.Arbiter_Android.Map.Map;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class FailedSyncDialog extends DialogFragment {
	public static final String TAG = "FailedSyncDialog";
	
	protected String title;
	protected String ok;
	protected String cancel;
	protected int layout;
	protected OnClickListener validatingClickListener = null;
	private AlertDialog myDialog;
	
	private String[] failedVectorUploads;
	private String[] failedVectorDownloads;
	private JSONObject failedMediaUploads;
	private String[] failedMediaDownloads;
	private Map.CordovaMap cordovaMap;
	
	public FailedSyncDialog(){}
	
	public static FailedSyncDialog newInstance(
			String[] failedVectorUploads, String[] failedVectorDownloads,
			JSONObject failedMediaUploads, String[] failedMediaDownloads){
		
		FailedSyncDialog dialog = new FailedSyncDialog();
		
		dialog.failedVectorUploads = failedVectorUploads;
		dialog.failedVectorDownloads = failedVectorDownloads;
		dialog.failedMediaUploads = failedMediaUploads;
		dialog.failedMediaDownloads = failedMediaDownloads;
		
		return dialog;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
	}
	
	@Override
	public void onStart(){
		super.onStart();
		myDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
		
		try{
			
			this.cordovaMap = (Map.CordovaMap) this.getActivity();
		}catch(ClassCastException e){
			e.printStackTrace();
		}

		if(validatingClickListener != null) {
			Button positiveButton = myDialog.getButton(DialogInterface.BUTTON_POSITIVE);
	        positiveButton.setOnClickListener(validatingClickListener);
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View view = inflater.inflate(R.layout.failed_sync, null);
		
		populateView(view);
		
		AlertDialog dialog = new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.icon)
			.setTitle(R.string.failed_to_sync)
			.setView(view)
			.setPositiveButton(R.string.retry,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        	onPositiveClick();
                        }
                    }
                )
                .setNegativeButton(android.R.string.no, null).create();
		
		this.myDialog = dialog;
		
		return dialog;
	}
	
	private void onPositiveClick(){
		
		this.getActivity().runOnUiThread(new Runnable(){
			@Override
			public void run(){
				SyncProgressDialog.show(getActivity());
				
				Map.getMap().sync(cordovaMap.getWebView());
			}
		});
	}
	
	private void populateView(View view){
		
		String failed = null;
		
		TextView errorsVectorUpload = (TextView) view.findViewById(R.id.vector_upload_errors);
		TextView errorsVectorUploadLabel = (TextView) view.findViewById(R.id.vector_upload_errors_label);
		
		TextView errorsVectorDownload = (TextView) view.findViewById(R.id.vector_download_errors);
		TextView errorsVectorDownloadLabel = (TextView) view.findViewById(R.id.vector_download_errors_label);
		
		TextView errorsMediaUpload = (TextView) view.findViewById(R.id.media_upload_errors);
		TextView errorsMediaUploadLabel = (TextView) view.findViewById(R.id.media_upload_errors_label);
		
		TextView errorsMediaDownload = (TextView) view.findViewById(R.id.media_download_errors);
		TextView errorsMediaDownloadLabel = (TextView) view.findViewById(R.id.media_download_errors_label);
	
		if(this.isFailed(this.failedVectorUploads)){
			
			failed = buildErrorString(failedVectorUploads);
			
			errorsVectorUpload.setText(failed);
		}else{
			errorsVectorUpload.setVisibility(View.GONE);
			errorsVectorUploadLabel.setVisibility(View.GONE);
		}
		
		if(this.isFailed(this.failedVectorDownloads)){
			failed = buildErrorString(failedVectorDownloads);
			
			errorsVectorDownload.setText(failed);
		}else{
			errorsVectorDownload.setVisibility(View.GONE);
			errorsVectorDownloadLabel.setVisibility(View.GONE);
		}
		
		if(this.isFailedMediaUploads(this.failedMediaUploads)){
			failed = buildErrorString(failedMediaUploads);
			
			errorsMediaUpload.setText(failed);
		}else{
			errorsMediaUpload.setVisibility(View.GONE);
			errorsMediaUploadLabel.setVisibility(View.GONE);
		}

		if(this.isFailed(this.failedMediaDownloads)){
			failed = buildErrorString(failedMediaDownloads);
			
			errorsMediaDownload.setText(failed);
		}else{
			errorsMediaDownload.setVisibility(View.GONE);
			errorsMediaDownloadLabel.setVisibility(View.GONE);
		}
	}
	
	private String buildErrorString(String[] failed){
		String results = "";
		
		if(failed == null){
			return results;
		}
		
		for(int i = 0; i < failed.length; i++){
			results += failed[i] + "\n";
		}
		
		return results;
	}
	
	private String buildErrorString(JSONObject failed){
		String results = "";
		String key = null;
		JSONArray mediaForLayer = null;
		
		if(failed == null){
			return null;
		}
		
		Iterator<?> iterator = failed.keys();
		
		try{
			while(iterator.hasNext()){
				key = (String) iterator.next();
				
				mediaForLayer = failed.getJSONArray(key);
				
				for(int i = 0, count = mediaForLayer.length(); i < count; i++){
					Log.w("FailedSyncDialog", "FailedSyncDialog mediaForLayer + " + mediaForLayer.getString(i));
					results += mediaForLayer.getString(i) + "\n";
				}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		return results;
	}
	
	public boolean isFailedMediaUploads(JSONObject failed){
		
		if(failed == null){
			return false;
		}
		
		Iterator<?> keys = failed.keys();
		
		JSONArray jsonArray = null;
		
		String key = null;
		
		try{
			
			while(keys.hasNext()){
				
				key = (String) keys.next();
				
				jsonArray = failed.getJSONArray(key);
				
				if(jsonArray.length() > 0){
					return true;
				}
			}
		}catch(JSONException e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	public boolean isFailed(String[] failed){
		if(failed != null && failed.length > 0){
			return true;
		}
		
		return false;
	}
}
