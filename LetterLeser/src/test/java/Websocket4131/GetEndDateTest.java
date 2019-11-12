package Websocket4131;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.Test;

import edu.oswego.websocket.Websocket;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.joda.time.DateTime;
import static org.junit.Assert.assertEquals;

/**
 * @author Emma Brunell
 */
public class GetEndDateTest {
    
        Websocket websocket = new Websocket();


	@BeforeEach
	void setUp() throws Exception {
	}

	@AfterEach
	void tearDown() throws Exception {
	}
        
        @Test
        public void testGetEndDateYear() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Method method = Websocket.class.getDeclaredMethod("getEndDate", DateTime.class, String.class);
            method.setAccessible(true);
            DateTime startDate = new DateTime("2019-11-12T15:48:50+00:00");
            String interval = "year";
            DateTime result = (DateTime) method.invoke(websocket, startDate, interval);
            assertEquals(result, new DateTime("2020-11-12T15:48:50+00:00"));
        }
        
        @Test
        public void testGetEndDateMonth() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Method method = Websocket.class.getDeclaredMethod("getEndDate", DateTime.class, String.class);
            method.setAccessible(true);
            DateTime startDate = new DateTime("2019-11-12T15:48:50+00:00");
            String interval = "month";
            DateTime result = (DateTime) method.invoke(websocket, startDate, interval);
            assertEquals(result, new DateTime("2019-12-12T15:48:50+00:00"));
        }
        
        @Test
        public void testGetEndDateWeek() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Method method = Websocket.class.getDeclaredMethod("getEndDate", DateTime.class, String.class);
            method.setAccessible(true);
            DateTime startDate = new DateTime("2019-11-12T15:48:50+00:00");
            String interval = "week";
            DateTime result = (DateTime) method.invoke(websocket, startDate, interval);
            assertEquals(result, new DateTime("2019-11-19T15:48:50+00:00"));
        }

}

