package runnables;

import edu.oswego.model.Email;
import edu.oswego.runnables.NumOfEmailsCallable;
import edu.oswego.runnables.TimeBetweenRepliesCallable;
import junit.framework.AssertionFailedError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Date;

import static junit.framework.TestCase.assertEquals;

public class NumOfEmailsCallableTest {
    private NumOfEmailsCallable noec = new NumOfEmailsCallable(null);
    private Method findHour;
    private Method findDay;

    @BeforeEach
    void setUp() {
        try {
            findHour = NumOfEmailsCallable.class.getDeclaredMethod("findHour", Email.class);
            findHour.setAccessible(true);
            findDay = NumOfEmailsCallable.class.getDeclaredMethod("findDay", Email.class);
            findDay.setAccessible(true);
        }catch(NoSuchMethodException nsme){
            nsme.printStackTrace();
        }
    }

    @Test
    void testFindHourOneHour(){
        Date novTwelvth = new Date(119,10,12);
        //Note: one hour is 3.6e+6 milliseconds, so end date is set 2.5 hours later than start
        Date date = new Date(novTwelvth.getTime());
        Email email = new Email(0, date, null,0,false,null,false,null,null,null);
        try {
            int result = (Integer) findHour.invoke(noec, email);
            assertEquals(0,result);
        }catch(InvocationTargetException ite){
            ite.printStackTrace();
        }catch(IllegalAccessException iae){
            iae.printStackTrace();
        }
    }

    @Test
    void testFindHourFullDay(){
        Date novTwelvth = new Date(119,10,12);
        for(int i = 0; i < 24; i++){
            long hour = (long)3.6e+6;
            Date date = new Date(novTwelvth.getTime() + (hour*i));
            Email email = new Email(0, date, null,0,false,null,false,null,null,null);
            int expected = i/4;
            try {
                int result = (Integer) findHour.invoke(noec, email);
                assertEquals(expected,result);
            }catch(InvocationTargetException ite){
                ite.printStackTrace();
            }catch(IllegalAccessException iae){
                iae.printStackTrace();
            }catch (AssertionFailedError afe){
                afe.printStackTrace();
            }
        }


    }

    @Test
    void testFindDaySingleDay(){
        //Some notes about java.sql.Date :
        //year = [actual year] - 1900
        //month = [actual date] - 1
        //day = [actual day]
        //So, new Date(119,10,12) is actually November 12th, 2019
        //(Which is a Tuesday)
        Date date = new Date(119,10,12);
        Email email = new Email(0, date, null,0,false,null,false,null,null,null);
        try {
            int result = (Integer) findDay.invoke(noec, email);
            //0 is Sunday, 1 is Monday, 2 is Tuesday etc.
            assertEquals(2,result);
        }catch(InvocationTargetException ite){
            ite.printStackTrace();
        }catch(IllegalAccessException iae){
            iae.printStackTrace();
        }
    }

    @Test
    void testFindDayFullYear() {
        //Iterates through the entire year of 2019
        //Testing for consistency through variable length months.
        //0 is January, 1 is February, 2, is March, etc
        int month = 0;
        int day = 0;
        int year = 119;
        //Jan 1st 2019 was a Tuesday
        int expected = 2;
        for (int i = 0; i < 365; i++) {
            switch (month) {
                case 1:
                    //in 2019, february has 28 days
                    if (day == 28) {
                        month++;
                        day = 0;
                    }
                    break;
                case 3:
                    //April has 30
                    if (day == 30) {
                        month++;
                        day = 0;
                    }
                    break;
                case 5:
                    //June
                    if (day == 30) {
                        month++;
                        day = 0;
                    }
                    break;
                case 8:
                    //September
                    if (day == 30) {
                        month++;
                        day = 0;
                    }
                    break;
                case 10:
                    //November
                    if (day == 30) {
                        month++;
                        day = 0;
                    }
                    break;
                default:
                    //Every other month
                    if (day == 31) {
                        month++;
                        day = 0;
                    }
                    break;
            }
            day++;
            Date date = new Date(year, month, day);
            Email email = new Email(0, date, null, 0, false, null, false, null, null, null);
            try {
                int result = (Integer) findDay.invoke(noec, email);
                assertEquals(expected, result);
            } catch (InvocationTargetException ite) {
                ite.printStackTrace();
            } catch (IllegalAccessException iae) {
                iae.printStackTrace();
            }
            expected = (expected + 1) % 7;
        }
    }
}
