package edu.oswego.Runnables;

import edu.oswego.model.Email;

import java.util.List;
import java.util.concurrent.Callable;

public class DomainCallable implements Callable {
	private List<Email> emails;

	public DomainCallable(List<Email> emails) {
		this.emails = emails;
	}

	@Override
	public Object call() throws Exception {
		return null;
	}
}
