package com.lmn.Arbiter_Android.About;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.lmn.Arbiter_Android.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class About {
	private Activity activity;
	public static final Locale DEFAULT_LOCALE = Locale.ENGLISH;
	
	public About(Activity activity){
		this.activity = activity;
	}
	
	public void displayAboutDialog(){
		
		activity.runOnUiThread(new Runnable(){
    		@Override
    		public void run(){
    			
    			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    			
    			builder.setIcon(R.drawable.icon);
    			builder.setTitle(R.string.about);
    			
    			LayoutInflater inflater = activity.getLayoutInflater();
    			
    			View view = inflater.inflate(R.layout.about, null);
    			
    			setVersionInfo(view);
    			
    			builder.setView(view);
    			
    			builder.setPositiveButton(android.R.string.ok, null);
    			
    			builder.create().show();
    		}
    	});
	}
	
	private void setVersionInfo(View view){
		
		TextView buildDate = (TextView) view.findViewById(R.id.build_date);
		TextView version = (TextView) view.findViewById(R.id.version);
		TextView commitDate = (TextView) view.findViewById(R.id.commit_date);
		
		buildDate.setText(getBuildDate());
		
		String appVersion = getAppVersion();
		
		int appVersionLength = appVersion.length();
		
		String unknown = activity.getResources().getString(R.string.unknown);
		
		if(appVersionLength < 7){
			
			version.setText(unknown);
			commitDate.setText(unknown);
			
			return;
		}
		
		version.setText(appVersion.substring(0, 7));
		
		if(appVersionLength < 35){
			
			commitDate.setText(unknown);
			
			return;
		}
		
		try {
			commitDate.setText(formatDate(appVersion.substring(10)));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String getAppVersion(){
		
		String appVersion = null;
		
		try {
			PackageInfo pInfo = activity.getPackageManager().getPackageInfo(
					activity.getPackageName(), PackageManager.GET_META_DATA);
			
			appVersion = pInfo.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return appVersion;
	}
	
	private String formatDate(String dateStr) throws ParseException{
		String formattedDate = null;
		
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		Date date = formatter.parse(dateStr);
		
		formattedDate = SimpleDateFormat.getInstance().format(date);
		
		return formattedDate;
	}
	
	private String getBuildDate(){
		String formattedDate = null;
		
		try{
		     ApplicationInfo ai = activity.getPackageManager().getApplicationInfo(
		    		 activity.getPackageName(), PackageManager.GET_META_DATA);
		     
		     ZipFile zf = new ZipFile(ai.sourceDir);
		     ZipEntry ze = zf.getEntry("classes.dex");
		     long time = ze.getTime();
		     
		     Date date = new java.util.Date(time);
		     
		     formattedDate = SimpleDateFormat.getInstance().format(date);
		     
		     zf.close();
		     
		  }catch(Exception e){
			  e.printStackTrace();
		  }
		
		return formattedDate;
	}
}
