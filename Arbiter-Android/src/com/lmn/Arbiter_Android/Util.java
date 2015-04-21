package com.lmn.Arbiter_Android;

import java.security.KeyStore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.FeatureDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.ProjectDatabaseHelper;
import com.lmn.Arbiter_Android.ProjectStructure.ProjectStructure;
import com.lmn.Arbiter_Android.SSL.AllowAllSSLSocketFactory;

public class Util {
	
	public Util(){}
	
	public boolean convertIntToBoolean(int number){
		return (number > 0) ? true : false;
	}
	
	public ContentValues contentValuesFromHashMap(HashMap<String, String> hashmap){
		ContentValues values = new ContentValues();
		
		for(String key : hashmap.keySet()){
			values.put(key, hashmap.get(key));
		}
		
		return values;
	}
	
	public static void showNoNetworkDialog(final Activity activity){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		builder.setTitle(R.string.no_network);
		
		builder.setMessage(R.string.check_network_connection);
		
		builder.setPositiveButton(R.string.close, null);
		
		builder.create().show();
	}
	
	public static void showDialog(final Activity activity, int title, int message, 
			String optionalMessage, Integer negativeBtnText,
			Integer positiveBtnText, final Runnable onPositive){
		
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		
		String msg = activity.getResources().getString(message);
		
		if(optionalMessage != null){
			msg += ": \n\t" + optionalMessage;
		}
		
		builder.setTitle(title);
		builder.setIcon(activity.getResources().getDrawable(R.drawable.icon));
		builder.setMessage(msg);
		
		if(negativeBtnText != null){
			builder.setNegativeButton(negativeBtnText, null);
		}
		
		if(positiveBtnText != null && onPositive != null){
			builder.setPositiveButton(positiveBtnText, new OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					onPositive.run();
				}
			});
		}
		
		builder.create().show();
	}
	
	public boolean isInteger(String str){
		try{
			Integer.parseInt(str);
			return true;
		}catch(NumberFormatException e){}
		
		return false;
	}
	
	public boolean isDouble(String str){
		try{
			Double.parseDouble(str);
			return true;
		}catch(NumberFormatException e){}
		
		return false;
	}
	
	public boolean isLong(String str){
		
		try{
			Long.parseLong(str);
			return true;
		}catch(NumberFormatException e){}
		
		return false;
	}
	
	public boolean isFloat(String str){
	
		try{
			Float.parseFloat(str);
			return true;
		}catch(NumberFormatException e){}
		
		return false;
	}
	
	public int getOffsetFromUTC(){
		TimeZone tz = TimeZone.getDefault();
		
		return tz.getOffset((new Date()).getTime());
	}
	
	public String getNow(String type, boolean inGMT) throws Exception{
		
		SimpleDateFormat formatter = getSimpleDateFormat(type);
		
		Date local = new Date();
		
		String now = null;
		
		if(inGMT){
			now = formatter.format(new Date(local.getTime() - getOffsetFromUTC()));
		}else{
			now = formatter.format(local);
		}
		
		Log.w("Util", "Util getNow = " + now);
		
		return now;
	}

	public SimpleDateFormat getSimpleDateFormat(String type) throws Exception{
		
		String format = null;
		
		if("xsd:dateTime".equals(type)){
			
			format = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
		}else if("xsd:date".equals(type)){
			
			format = "yyyy-MM-dd'Z'";
		}else if("xsd:time".equals(type)){
			
			format = "HH:mm:ss.SSS'Z'";
		}else{
			throw new Exception(type + " is not a valid type.");
		}
		
		TimeZone tz = TimeZone.getDefault();
		SimpleDateFormat df = new SimpleDateFormat(format);
		df.setTimeZone(tz);
		
		return df;
	}
	
	public String getHumanReadableDate(Calendar calendar, String type) throws Exception{
		
		DateFormat format = null;
		
		if("xsd:dateTime".equals(type)){
			format = DateFormat.getDateTimeInstance();
		}else if("xsd:date".equals(type)){
			format = DateFormat.getDateInstance();
		}else if("xsd:time".equals(type)){
			format = DateFormat.getTimeInstance();
		}else{
			throw new Exception(type + " is not a valid type");
		}
		
		format.setCalendar(calendar);
		format.setTimeZone(TimeZone.getDefault());
		
		return format.format(calendar.getTime());
	}
	
	public SQLiteDatabase getProjectDb(Activity activity, boolean reset){
		
		String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
		String projectPath = ProjectStructure.getProjectPath(projectName);
		return ProjectDatabaseHelper.getHelper(activity.getApplicationContext(), projectPath, reset).getWritableDatabase();
	}
	
	public SQLiteDatabase getApplicationDb(Context context){
		
		return ApplicationDatabaseHelper.getHelper(context).getWritableDatabase();
	}
	
	public SQLiteDatabase getFeatureDb(Activity activity, boolean reset){
		String projectName = ArbiterProject.getArbiterProject().getOpenProject(activity);
		String projectPath = ProjectStructure.getProjectPath(projectName);
		return FeatureDatabaseHelper.getHelper(activity.getApplicationContext(), projectPath, reset).getWritableDatabase();
	}


    // Note: when using SSL endpoints, if the certs are not properly configured and chained in the
    //       proper order, you can end up with 'No peer certificate' exception when attempting to
    //       talk to the server. The client here makes it such that so long as an SSL cert is present,
    //       the connection continues. Since arbiter is meant for collecting open data and reducing
    //       the technical ability of the organizations involved is beneficial, we are allowing
    //       arbiter to be more lenient. to change this behavior allowAnySSLCertificate can be
    //       changed to false
    static public DefaultHttpClient getHttpClientWithSSLConsiderations() {
        boolean allowAnySSLCertificate = true;

        if (allowAnySSLCertificate) {
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustStore.load(null, null);

                AllowAllSSLSocketFactory sf = new AllowAllSSLSocketFactory(trustStore);
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

                HttpParams params = new BasicHttpParams();
                HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);

                SchemeRegistry registry = new SchemeRegistry();
                registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(new Scheme("https", sf, 443));

                ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

                return new DefaultHttpClient(ccm, params);
            } catch (Exception e) {
                e.printStackTrace();
                return new DefaultHttpClient();
            }
        } else {
            return new DefaultHttpClient();
        }
    }
}
