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
				if (domains.containsKey(domain)) {
					int x = domains.get(domain);
					domains.put(domain, ++x);
				} else {
					domains.put(domain, 1);
				}
			}
		}

		JsonArray domainObjs = new JsonArray();
		for (String domain : domains.keySet()) {
			JsonObject domainObj = new JsonObject();
			JsonObject innerData = new JsonObject();
			String[] domainMeta = domain.split("\\.");
			innerData.addProperty("domainname", domainMeta[1]);
			innerData.addProperty("domainparent", domainMeta[0]);
			innerData.addProperty("contribution", domains.get(domain));
			domainObj.add("domainobj", innerData);
			domainObjs.add(domainObj);
		}
		return domainObjs;
	}
}
