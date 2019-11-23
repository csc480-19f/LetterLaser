package edu.oswego.runnables;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import com.google.gson.JsonArray;

import edu.oswego.model.Email;
import org.joda.time.DateTime;

public class NumOfEmailsCallable implements Callable {
	private static List<Email> emails;

	private static final double oneDay = 86400000;
	private static final double fourHours = 14400000;

	/**
	 * @author Phoenix Boisnier Things to keep in mind: Jan 1st, 1970 was a
	 *         Thursday. 86400000 milliseconds in a day. 14400000 milliseconds in
	 *         four hours.
	 * @param emails
	 *            The list of email objects.
	 */
	public NumOfEmailsCallable(List<Email> emails) {
		this.emails = emails;
	}

	/**
	 * This method calculates the heat map scores. It returns a 2-D Array of
	 * Integers where the first index corresponds to the day of the week and the
	 * second index corresponds to the hour interval the email belongs to.
	 */
	@Override
	public Object call() throws Exception {
		int[][] retVal = new int[7][6];

		for(Email e : emails){
			DateTime dateTime = new DateTime(e.getDateReceived());
			int dayIndex = dateTime.getDayOfWeek()-1;
			int hourIndex = this.findHour(dateTime);
			retVal[dayIndex][hourIndex]++;
		}

		// For each email1
//		for (Email e : emails) {
//			// Figure out which day it came in and hour interval
//			int dayIndex = this.findDay(e);
//			int hourIndex = this.findHour(e);
//			// Which get used as the index in the retVal 2-D array
//			retVal[dayIndex][hourIndex]++;
//			// And adds 1 to the value at that location.
//		}

		JsonArray complete = new JsonArray();
		for (int q = 0; q < 7; q++) {
			JsonArray row = new JsonArray();
			for (int w = 0; w < 6; w++) {
				row.add(retVal[q][w]);
			}
			complete.add(row);
		}
		return complete;
	}



	private int findHour(DateTime date){
		int hour = date.hourOfDay().get();
		if(hour>=0 && hour<4){
			return 0;
		}else if(hour>=4 && hour<8){
			return 1;
		}else if(hour>=8 && hour<12){
			return 2;
		}else if(hour>=12 && hour<16){
			return 3;
		}else if(hour>=16 && hour<20){
			return 4;
		}else if(hour>=20 && hour<24){
			return 5;
		}
		return 0;
	}

	/**
	 * This method takes an email object and determines which set of hours the email
	 * was received in.
	 *
	 * @param e
	 *            The email.
	 * @return 0 for hours 0-3:59, 1 for hours 4-7:59, ... , 5 for hours 20-23:59
	 */
//	private int findHour(Email e) {
//		Date d = e.getDateReceived();
//		int hoursLong = (int)(d.getTime() % oneDay);
//		if(daylightSavings()) hoursLong -= (fourHours / 4);
//		int temp = ((int) oneDay+hoursLong) / (int) fourHours;
//		return (temp+5) % 6;
//	}
//
//	/**
//	 * Checks for daylight savings time.
//	 * @return True or False depending on if it is, in fact, daylight savings time. Surprised, right?
//	 */
//	private boolean daylightSavings(){
//
//		long timeNow = System.currentTimeMillis();
//		Date today = new Date(timeNow);
//		int month = Integer.parseInt(today.toString().substring(5,7));
//		int day = Integer.parseInt(today.toString().substring(8));
//		if(month >= 11 && day >= 3) return true;
//		if(month <= 3 && day < 10) return true;
//		return false;
//	}
//
//	/**
//	 * This method takes an email object and determines which day the email was
//	 * receive on.
//	 *
//	 * @param e
//	 *            The email. This email has a date that is at least three days
//	 *            greater than 1-1-1970.
//	 * @return 0 for Sun, 1 for Mon, 2 for Tue, ... , 6 for Sat.
//	 */
//	private int findDay(Email e) {
//		double dayLong = e.getDateReceived().getTime() / oneDay;
//		// -3 shifts Sunday (3 % 7) to be (0 % 7) since (0 % 7) = Thursday normally.
//		return (((int) dayLong) - 3) % 7;
//	}
}