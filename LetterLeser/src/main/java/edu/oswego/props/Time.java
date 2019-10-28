package edu.oswego.props;

import java.text.ParseException;
import java.util.Date;

/**
 * Converts a string date into a Date object in a valid SQL format.
 * 
 * @author Jimmy Nguyen
 * @since 10/28/2019
 *
 */

public class Time {

	private Time() {} // Cannot instantiate an object

	/**
	 * Converts a string date into a valid SQL format Date object
	 * 
	 * @param date
	 * @return Date object in SQL format
	 */
	public static Date getDate(String date) {
		try {
			return new java.text.SimpleDateFormat("yyyy-MM-dd").parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Converts a Date object into a string in SQL format
	 * 
	 * @param date
	 * @return String date in SQL format
	 */
	public static String parseDateTime(Date date) {
		return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
	}

}
