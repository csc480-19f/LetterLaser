package runnables;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.oswego.model.Email;
import edu.oswego.model.EmailAddress;
import edu.oswego.runnables.DomainCallable;

public class DomainCallableTest {

    private List<Email> emails;
    private List<DomainObject> domainObjects;

    @BeforeEach
    void setUp() {
        emails = new ArrayList<>();
        domainObjects = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            List<EmailAddress> senders = new ArrayList<>();
            senders.add(new EmailAddress(0, "mdoran@oswego.edu"));
            Email e = new Email(0, null, "", 0, false, false, null, null, senders);
            emails.add(e);
        }
        domainObjects.add(new DomainObject("edu","oswego", 10));
        for(int i = 0; i < 9; i++){
            List<EmailAddress> senders = new ArrayList<>();
            senders.add(new EmailAddress(0, "mdoran@oswego.com"));
            senders.add(new EmailAddress(0, "mdoran@clarkson.edu"));
            Email e = new Email(0, null, "", 0, false, false, null, null, senders);
            emails.add(e);
        }
        domainObjects.add(new DomainObject("com","oswego", 9));
        for(int i = 0; i < 8; i++){
            List<EmailAddress> senders = new ArrayList<>();
            senders.add(new EmailAddress(0, "mdoran@oswego.org"));
            senders.add(new EmailAddress(0, "mdoran@yahoo.com"));
            Email e = new Email(0, null, "", 0, false, false, null, null, senders);
            emails.add(e);
        }
        domainObjects.add(new DomainObject("org","oswego", 8));
        for(int i = 0; i < 7; i++){
            List<EmailAddress> senders = new ArrayList<>();
            senders.add(new EmailAddress(0, "mdoran@clarkson.edu"));
            senders.add(new EmailAddress(0, "mdoran@something.dum"));
            Email e = new Email(0, null, "", 0, false, false, null, null, senders);
            emails.add(e);
        }
        domainObjects.add(new DomainObject("edu","clarkson", 7));
        domainObjects.add(new DomainObject("oswego","0", 27));
        domainObjects.add(new DomainObject("clarkson","0", 7));
    }

    @Test
    void testOutput() throws Exception{
        DomainCallable dc = new DomainCallable(emails);
        JsonObject returned = (JsonObject) dc.call();

        JsonArray domains = returned.getAsJsonArray("emailbydomain");

        assertEquals(domainObjects.size(), domains.size());
        for(int i = 0; i < 6; i++){
            JsonObject domain = domains.get(i).getAsJsonObject().get("domainobj").getAsJsonObject();

            String domainName = domain.get("domainname").getAsString();
            String domainParent = domain.get("domainparent").getAsString();
            int contribution = domain.get("contribution").getAsInt();

            for(DomainObject x : domainObjects){
                if(x.name == domainName){
                    assertEquals(x.parent, domainParent);
                    assertEquals(x.cont, contribution);
                }
            }
        }
    }

    private class DomainObject {
        public String name;
        public String parent;
        public int cont;
        public DomainObject(String n, String p, int c){
            name = n;
            parent = p;
            cont = c;
        }
    }
}
