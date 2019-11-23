package edu.oswego.runnables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.oswego.model.Email;
import edu.oswego.model.EmailAddress;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class DomainCallable implements Callable {
	private List<Email> emails;

	public DomainCallable(List<Email> emails) {
		this.emails = emails;
	}

	@Override
	public Object call() {
		HashMap<String, Integer> domains = new HashMap<>();

		for (Email e : emails) {
			List<EmailAddress> senders = e.getFrom();
			for (EmailAddress ea : senders) {
				String domain = ea.getEmailAddress().split("@")[1];
				String parent = domain.split("\\.")[0];
				if (domains.containsKey(domain)) {
					int d = domains.get(domain);
					int p = domains.get(parent);
					domains.put(domain, ++d);
					domains.put(parent, ++p);
				} else if(domains.containsKey(parent)){
					int p = domains.get(parent);
					domains.put(parent, ++p);
					domains.put(domain, 1);
				}else{
					domains.put(domain, 1);
					domains.put(parent, 1);
				}
			}
		}

		JsonObject emailsByDomain = new JsonObject();
		JsonArray domainObjs = new JsonArray();
		for (String domain : domains.keySet()) {
			JsonObject domainObj = new JsonObject();
			JsonObject innerData = new JsonObject();

			String[] domainMeta = domain.split("\\.");
			String domainName = (domainMeta.length == 2)? domainMeta[1] : domainMeta[0];
			String domainParent = (domainMeta.length == 2)? domainMeta[0] : "0";

			innerData.addProperty("domainname", domainName);
			innerData.addProperty("domainparent", domainParent);
			innerData.addProperty("contribution", domains.get(domain));
			domainObj.add("domainobj", innerData);
			domainObjs.add(domainObj);
		}
		emailsByDomain.add("emailbydomain", domainObjs);
		return domainObjs;
	}
}
