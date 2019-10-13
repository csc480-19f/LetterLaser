package edu.oswego.props;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Time {
	
	private static SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static Date getDate(String date) {
		try {
			return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String parseDateTime(Date date) {
		return sdf.format(date);
	}

}
