package Websocket4131;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import org.junit.Test;

import edu.oswego.websocket.Websocket;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.joda.time.DateTime;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 *
 * @author Emma Brunell
 */
public class GetStartDateTest {

    Websocket websocket = new Websocket();

    @BeforeEach
    void setUp() throws Exception {
    }

    @AfterEach
    void tearDown() throws Exception {
    }
    
    @Test
    public void testGetStartDate() throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = Websocket.class.getDeclaredMethod("getStartDate", String.class);
        method.setAccessible(true);
        String startDate = "2019/11/12 00:00:00";
        DateTime result = (DateTime) method.invoke(websocket, startDate);
        DateTime expected = new DateTime("2019-11-12T00:00:00-05:00");
        assertEquals(expected, result);
    }
    
    @Test
    public void testNullStartDate() throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = Websocket.class.getDeclaredMethod("getStartDate", String.class);
        method.setAccessible(true);
        DateTime result = (DateTime) method.invoke(websocket, "");
        assertNull(result);
    }

}
