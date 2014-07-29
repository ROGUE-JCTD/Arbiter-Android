package com.lmn.Arbiter_Android.CookieManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import com.lmn.Arbiter_Android.BaseClasses.Server;
import com.lmn.Arbiter_Android.DatabaseHelpers.ApplicationDatabaseHelper;
import com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers.ServersHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.SparseArray;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class ArbiterCookieManager {

	private Context context;
	
	public ArbiterCookieManager(Context context){
		this.context = context;
	}
	
	public void getCookieForServer(String wmsUrl, String username, String password) throws ClientProtocolException, IOException{
		
		final DefaultHttpClient postClient = new DefaultHttpClient();
		HttpParams params = postClient.getParams();
		params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
		HttpPost postRequest = new HttpPost(wmsUrl.replace("/wms", "/j_spring_security_check"));
		List <NameValuePair> postParams = new ArrayList<NameValuePair>();
		postParams.add(new BasicNameValuePair("username", username));
		postParams.add(new BasicNameValuePair("password", password));

		postRequest.setEntity(new UrlEncodedFormEntity(postParams, HTTP.UTF_8));
		
		postClient.execute(postRequest);
		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		List<Cookie> cookies = postClient.getCookieStore().getCookies();
		for (int i = 0; i < cookies.size(); i++) {
			Cookie sessionCookie = cookies.get(i);
			if (sessionCookie != null) {
			    String cookieString = sessionCookie.getName() + "=" + sessionCookie.getValue() + "; domain=" + sessionCookie.getDomain();
			    cookieManager.setCookie(wmsUrl.replace("/geoserver/wms", ""), cookieString);
			}   
		}
	    CookieSyncManager.getInstance().sync();
	}
	
	private SQLiteDatabase getAppDb(){
		
		return ApplicationDatabaseHelper.getHelper(context).getWritableDatabase();
	}
	
	public SparseArray<Server> updateAllCookies(){
		
		SparseArray<Server> servers = ServersHelper.getServersHelper().getAll(getAppDb());
		
		Server server = null;
		
		for(int i = servers.size() - 1; i >= 0; i--){
			
			server = servers.valueAt(i);
			
			if(!"".equals(server.getUsername()) && !"".equals(server.getPassword())){
				
				try {
					getCookieForServer(server.getUrl(),
							server.getUsername(), server.getPassword());
				} catch (ClientProtocolException e) {
					
					e.printStackTrace();
					
					servers.removeAt(i);
				} catch (IOException e) {
					
					e.printStackTrace();
					servers.removeAt(i);
				}
			}
		}
		
		return servers;
	}
}
