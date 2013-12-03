package com.lmn.Arbiter_Android.DatabaseHelpers.TableHelpers;

public class FeatureHelperException extends Exception {
	private FeaturesHelper.Errors errorCode;
	
	public FeatureHelperException(FeaturesHelper.Errors errorCode){
		super();
		
		this.errorCode = errorCode;
	}
	
	public FeaturesHelper.Errors getErrorCode(){
		return this.errorCode;
	}
}
