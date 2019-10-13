package edu.oswego.Runnables;

import edu.oswego.model.Email;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class NumOfEmailsCallable implements Callable {
    private static ArrayList<Email> emails;

    private static final int oneDay = 86400000;
    private static final int fourHours = 14400000;

    /**
     * @author Phoenix Boisnier
     * Things to keep in mind:
     * Jan 1st, 1970 was a Thursday.
     * 86400000 milliseconds in a day.
     * 14400000 milliseconds in four hours.
     * @param emails The list of email objects.
     */
    public NumOfEmailsCallable(ArrayList<Email> emails){
        this.emails = emails;
    }
    @Override
    /**
     * This method calculates the heat map scores. It returns a 2-D Array of Integers where
     * the first index corresponds to the day of the week and the second index
     * corresponds to the hour interval the email belongs to.
     */
    public Object call() throws Exception {
        int[][] retVal = new int[7][6];

        //For each email
        for(Email e : emails){
            //Figure out which day it came in and hour interval
            int dayIndex = this.findDay(e);
            int hourIndex = this.findHour(e);
            //Which get used as the index in the retVal 2-D array
            retVal[dayIndex][hourIndex]++;
            //And adds 1 to the value at that location.
        }

        return retVal;
    }

    /**
     * This method takes an email object and determines which set of hours the email was received in.
     * @param e The email.
     * @return 0 for hours 0-3:59, 1 for hours 4-7:59, ... , 5 for hours 20-23:59
     */
    private int findHour(Email e){
        long hoursLong = e.getDateReceived().getTime() % oneDay;
        return ((int) hoursLong) / fourHours;
    }

    /**
     * This method takes an email object and determines which day the email was receive on.
     * @param e The email. This email has a date that is at least three days greater than 1-1-1970.
     * @return 0 for Sun, 1 for Mon, 2 for Tue, ... , 6 for Sat.
     */
    private int findDay(Email e){
        long dayLong = e.getDateReceived().getTime() / oneDay;
        //-3 shifts Sunday (3 % 7) to be (0 % 7) since (0 % 7) = Thursday normally.
        return (((int) dayLong) - 3) % 7;
    }
}