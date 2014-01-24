package com.lmn.Arbiter_Android.Media;

import java.io.File;

public class HandleZeroByteFiles {

	private File mediaDir;
	
	public HandleZeroByteFiles(String path){
		
		this.mediaDir = new File(path);
	}
	
	public void deleteZeroByteFiles () {
	    
		deleteZeroByteFiles(mediaDir);
	} 
	
	private void deleteZeroByteFiles(File dir){
		if (dir.exists()) {
	        File[] files = dir.listFiles();
	        for (int i = 0; i < files.length; ++i) {
	            File file = files[i];
	            if (file.isDirectory()) {
	                deleteZeroByteFiles(file);
	            } else {
	                // do something here with the file
	            	
	            	if(file.length() == 0){
	            		file.delete();
	            	}
	            }
	        }
	    }
	}
}
