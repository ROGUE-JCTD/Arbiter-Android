package com.lmn.Arbiter_Android.TimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class LocalTime {

	private boolean isDateTime;
	private String dateTimeStr;
	private SimpleDateFormat dateFormat;
	private Calendar localCalendar;
	
	public LocalTime(String dateTimeStr, boolean isDateTime){
		this.isDateTime = isDateTime;
		this.dateTimeStr = dateTimeStr;
		this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
	}
	
	public LocalTime(Calendar calendar){
		this.localCalendar = calendar;
	}
	
	public Calendar getLocalCalendar() throws ParseException{
		
		if(this.localCalendar != null){
			return this.localCalendar;
		}
		
		long offsetInMilliseconds = TimeZone.getDefault().getRawOffset();
		
		Date date = dateFormat.parse(dateTimeStr);
		
		long localTime = date.getTime() + offsetInMilliseconds;
		
		Date localDate = new Date(localTime);
		
		Calendar calendar = Calendar.getInstance();
		
		calendar.setTime(localDate);
		
		return calendar;
	}
	
	public Calendar getISOCalendar(Calendar calendar){
		
		long offsetInMilliseconds = TimeZone.getDefault().getRawOffset();
		
		long isoTime = calendar.getTime().getTime() - offsetInMilliseconds;
		
		Date isoDate = new Date(isoTime);
		
		calendar.setTime(isoDate);
		
		return calendar;
	}
}
