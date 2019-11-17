package runnables;

import com.google.gson.JsonObject;

import edu.oswego.model.Email;
import edu.oswego.model.EmailAddress;
import edu.oswego.runnables.DomainCallable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DomainCallableTest {

    private List<Email> emails;

    @BeforeEach
    void setUp() {
        emails = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            List<EmailAddress> senders = new ArrayList<>();
            senders.add(new EmailAddress(0, "mdoran@oswego.edu"));
            Email e = new Email(0, null, "", 0, false, false, null, null, senders);
            emails.add(e);
        }
        for(int i = 0; i < 9; i++){
            List<EmailAddress> senders = new ArrayList<>();
            senders.add(new EmailAddress(0, "mdoran@clarkson.edu"));
            Email e = new Email(0, null, "", 0, false, false, null, null, senders);
            emails.add(e);
        }
        for(int i = 0; i < 8; i++){
            List<EmailAddress> senders = new ArrayList<>();
            senders.add(new EmailAddress(0, "mdoran@yahoo.com"));
            Email e = new Email(0, null, "", 0, false, false, null, null, senders);
            emails.add(e);
        }
        for(int i = 0; i < 7; i++){
            List<EmailAddress> senders = new ArrayList<>();
            senders.add(new EmailAddress(0, "mdoran@something.dum"));
            Email e = new Email(0, null, "", 0, false, false, null, null, senders);
            emails.add(e);
        }
    }

    @AfterEach
    void tearDown(){ }

    @Test
    void testOutput() throws Exception{
        DomainCallable dc = new DomainCallable(emails);
        JsonObject returned = (JsonObject) dc.call();

        String expected = "{\"emailbydomain\":[" +
                "{\"domainobj\":{\"domainname\":\"dum\",\"domainparent\":\"something\",\"contribution\":7}}," +
                "{\"domainobj\":{\"domainname\":\"com\",\"domainparent\":\"yahoo\",\"contribution\":8}}," +
                "{\"domainobj\":{\"domainname\":\"edu\",\"domainparent\":\"clarkson\",\"contribution\":9}}," +
                "{\"domainobj\":{\"domainname\":\"edu\",\"domainparent\":\"oswego\",\"contribution\":10}}" +
                "]}";
        assertEquals(returned.toString(),expected);
    }
}
