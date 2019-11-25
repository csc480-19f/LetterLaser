package edu.oswego.runnables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.oswego.model.Email;
import edu.oswego.model.EmailAddress;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

public class DomainCallable implements Callable {
	private List<Email> emails;

	public DomainCallable(List<Email> emails) {
		this.emails = emails;
	}

	@Override
	public Object call() {

	    DomainMapper dm = new DomainMapper(emails);


		JsonObject emailsByDomain = new JsonObject();
		JsonArray domainObjs = new JsonArray();
		for (String domain : dm.getDomains()) {
			JsonObject domainObj = new JsonObject();
			JsonObject innerData = new JsonObject();

			String domainParent = (dm.hasParent(domain))? dm.getParent(domain) : "0";

			innerData.addProperty("domainname", domain);
			innerData.addProperty("domainparent", domainParent);
			innerData.addProperty("contribution", dm.getContribution(domain));
			domainObj.add("domainobj", innerData);
			domainObjs.add(domainObj);
		}
		emailsByDomain.add("emailbydomain", domainObjs);
		return domainObjs;
	}

	private class DomainMapper{

	    private HashMap<String, Integer> contribution = new HashMap<String, Integer>();
	    private HashMap<String, String> parents = new HashMap<>();
	    public DomainMapper(List<Email> emails){
	        for(Email e : emails){
	           for(EmailAddress ea : e.getFrom()){
	               String [] domainParts = ea.getEmailAddress().split("@")[1].split("\\.");
                   for (int i = 0; i < domainParts.length; i++) {
                       parents.put(domainParts[i],((i+1) == domainParts.length)? "0" : domainParts[i+1]);
                       incrementKey(contribution, domainParts[i]);
                   }
               }
            }
        }

        public Set<String> getDomains(){ return contribution.keySet(); }

        public boolean hasParent(String k){ return parents.containsKey(k); }

        public String getParent(String k){ return parents.get(k); }

        public int getContribution(String k ){ return contribution.get(k); }

        private HashMap<String, Integer> incrementKey(HashMap<String,Integer> map, String key) {
            if(map.containsKey(key)){
                int x = map.get(key);
                map.put(key, ++x);
            }else{
                map.put(key, 1);
            }
            return map;
        }
    }
}
