package runnables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import edu.oswego.model.Email;
import edu.oswego.runnables.NumOfEmailsCallable;
import org.junit.Test;

import java.sql.Date;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumOfEmailsTest {

    private static final double oneDay = 86400000;
    private static final double fourHours = 14400000;

    @Test
    public void TestHeatMapHours(){
        ArrayList<Email> emails;
        NumOfEmailsCallable heatMap;

        //Checks for each slot
        emails = populateEmails0();
        heatMap = new NumOfEmailsCallable(emails);
        try {
            JsonArray heat = (JsonArray) heatMap.call();
            int[][] map = new int[7][6];
            for(int q = 0; q < 7; q++){
                JsonArray row = (JsonArray) heat.get(q);
                for(int w = 0; w < 6; w++){
                    JsonElement num = row.get(w);
                    int n = num.getAsInt();
                    map[q][w] = n;
                }
            }
            for(int[] row : map){
                for(Integer i : row){
                    assertEquals(2, row[i]);
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestHeatMapDays(){
        //Checks for each day
        ArrayList<Email> emails = populateEmails1();
        NumOfEmailsCallable heatMap = new NumOfEmailsCallable(emails);
        try {
            JsonArray heat = (JsonArray) heatMap.call();
            int[][] map = new int[7][6];
            for(int q = 0; q < 7; q++){
                JsonArray row = (JsonArray) heat.get(q);
                for(int w = 0; w < 6; w++){
                    JsonElement num = row.get(w);
                    int n = num.getAsInt();
                    map[q][w] = n;
                }
            }

            for(int[] row : map){
                for(Integer i : row){
                    Date d = new Date(System.currentTimeMillis());
                    Email e = new Email(0, d, null, 0, false, null, false,
                            null, null);
                    if(i == findHour(e)) {
                        assertEquals(2, row[i]);
                    }
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void TestHeatMapNow(){
        //Checks for this time
        Date d = new Date(System.currentTimeMillis());
        Email e = new Email(0, d, null, 0, false, null, false,
                null, null);
        int hour = findHour(e);
        int day = findDay(e);
        ArrayList<Email> emails = new ArrayList<>();
        emails.add(e);
        NumOfEmailsCallable heatMap = new NumOfEmailsCallable(emails);
        try {
            JsonArray heat = (JsonArray) heatMap.call();
            int[][] map = new int[7][6];
            for(int q = 0; q < 7; q++){
                JsonArray row = (JsonArray) heat.get(q);
                for(int w = 0; w < 6; w++){
                    JsonElement num = row.get(w);
                    int n = num.getAsInt();
                    map[q][w] = n;
                }
            }
            assertEquals(1, map[day][hour]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * This provides an even distribution of emails. 2 in each slot.
     */
    private static ArrayList<Email> populateEmails0(){
        ArrayList<Email> emails = new ArrayList<>();
        for(int q = 0; q < 84; q++){
            long time = System.currentTimeMillis();
            time -= fourHours*q;
            Date d = new java.sql.Date(time);
            Email e = new Email(q, d, null, 0, false, null, false, null, null);
            emails.add(e);
        }
        return emails;
    }

    /**
     * This provides an even distribution of emails across the first time slot for each day. 15 in each slot.
     */
    private static ArrayList<Email> populateEmails1(){
        ArrayList<Email> emails = new ArrayList<>();
        for(int q = 0; q < 14; q++){
            long time = System.currentTimeMillis();
            time -= oneDay*q;
            Date d = new java.sql.Date(time);
            Email e = new Email(q, d, null, 0, false, null, false, null, null);
            emails.add(e);
        }
        return emails;
    }

    /**
     * This method takes an email object and determines which set of hours the email
     * was received in.
     *
     * @param e
     *            The email.
     * @return 0 for hours 0-3:59, 1 for hours 4-7:59, ... , 5 for hours 20-23:59
     */
    private int findHour(Email e) {
        double hoursLong = e.getDateReceived().getTime() % oneDay;
        int temp = ((int) hoursLong) / (int) fourHours;
        return (temp+5) % 6;
    }

    /**
     * This method takes an email object and determines which day the email was
     * receive on.
     *
     * @param e
     *            The email. This email has a date that is at least three days
     *            greater than 1-1-1970.
     * @return 0 for Sun, 1 for Mon, 2 for Tue, ... , 6 for Sat.
     */
    private int findDay(Email e) {
        double dayLong = e.getDateReceived().getTime() / oneDay;
        // -3 shifts Sunday (3 % 7) to be (0 % 7) since (0 % 7) = Thursday normally.
        return (((int) dayLong) - 4) % 7;
    }
}
