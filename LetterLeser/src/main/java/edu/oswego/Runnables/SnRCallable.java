package edu.oswego.Runnables;

import edu.oswego.model.Email;
import java.util.List;
import java.util.concurrent.Callable;

public class SnRCallable implements Callable {
	private List<Email> emails;
	private String user;

	private static final double oneDay = 86400000;

	/**
	 * @author Phoenix Boisnier
	 */
	public SnRCallable(List<Email> emails, String userEmail) {
		this.emails = emails;
		this.user = userEmail;
	}

	/**
	 * This method assumes that no emails repeat in the emails array list.
	 * 		indexes [0][x] represent sent emails where x is [0,6]
	 * 		indexes [1][x] represent received emails where x is [0,6]
	 * 		x represents the day of the week, 0 for Sunday, 1 for Monday, ... , 6 for Saturday.
	 */
	@Override
	public Object call() throws Exception {
		int[][] stats = new int[2][7];

		for(Email e : emails){
			if(e.getFrom().get(0).getEmailAddress().equalsIgnoreCase(user)) stats[0][findDay(e)]++;
			else stats[1][findDay(e)]++;
		}

		return stats;
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
		return (((int) dayLong) - 3) % 7;
	}
}
