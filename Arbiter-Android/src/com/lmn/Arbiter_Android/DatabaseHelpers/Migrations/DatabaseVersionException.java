package com.lmn.Arbiter_Android.DatabaseHelpers.Migrations;

public class DatabaseVersionException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public DatabaseVersionException(String message){
		super(message);
	}
}
