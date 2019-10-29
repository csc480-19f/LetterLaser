package edu.oswego.Runnables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.oswego.model.Email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

public class TimeBetweenRepliesCallable implements Callable {
	private List<Email> emails;
	private String user;

	private static final double oneDay = 86400000;
	private static final double oneHour = 3600000;

	/**
	 * @author Phoenix Boisnier
	 */
	public TimeBetweenRepliesCallable(List<Email> emails, String username) {
		this.emails = emails;
		this.user = username;
	}

	/**
	 * This method assumes emails do not repeat in the emails array list.
	 * This method returns a 2-d array where:
	 * 		indexes [0][x] are times between sent.
	 * 		indexes [1][x] are times between received.
	 * 		indexes [x][0] are for Sunday.
	 * 		indexes [x][1] are for Monday.
	 * 		...
	 * 		indexes [x][6] are for Saturday.
	 */
	@Override
	public Object call() throws Exception {
		//The returned value, a 2-d array that stores the average time between sent at [0][x], and received at [1][x].
		double[][] times = new double[2][7];
		//This is the total number of emails sent, corresponding to the times in the array above.
		double[][] totals = new double[2][7];
		//This bit will save us time later.
		ArrayList<String> keys = new ArrayList<>();

		/*
		This section converts the emails array list into a hash map.
		This hash map has an array list stored as its value at the subject key as emails in a conversation all have
		the same subject, at least for gmail.
		 */
		HashMap<String, ArrayList<Email>> subjects = new HashMap<>();
		HashMap<String, Integer> addresses = new HashMap<>();
		for(Email e : emails){
			//The OR bit here takes any email with subject "Re: " and treats is as its subject w/o "Re: "
			if(subjects.containsKey(e.getSubject())||subjects.containsKey(e.getSubject().substring(5))){
				subjects.get(e.getSubject()).add(e);
			}
			else{
				subjects.put(e.getSubject(), new ArrayList<Email>());
				subjects.get(e.getSubject()).add(e);
				keys.add(e.getSubject());
			}

		}

		//Here's where that bit from earlier is going to save us time.
		for(String key : keys){
			ArrayList<Email> conversation = subjects.remove(key);

			//We use this to check to see if the first email is from the user.
			boolean sent = false;
			if(conversation.get(0).getFrom().get(0).getEmailAddress().equalsIgnoreCase(user)) sent = true;

			//And here we figure out how long it took between emails and store it appropriately. It gets averaged later.
			if(sent){
				//Since the user sent the first email, time between replies for sent starts at index 0, GOTO 111
				for(int q = 0; q < conversation.size(); q+=2){
					int dayIndex = findDay(conversation.get(q));
					totals[0][dayIndex]++;

					//If there is an email that has been received as a result of sending the email at index q
					if(conversation.size() > q+1){
						//Then we calculate the time between replies.
						times[0][dayIndex] += findHour(conversation.get(q), conversation.get(q+1));
					}
					//Otherwise,
					else{
						//We discount the email from the total.
						totals[0][dayIndex]--;
					}
				}
				//and time between replies for received starts at 1.
				for(int q = 1; q < conversation.size(); q+=2){
					int dayIndex = findDay(conversation.get(q));
					totals[1][dayIndex]++;

					if(conversation.size() > q+1){
						times[1][dayIndex] += findHour(conversation.get(q), conversation.get(q+1));
					}
					else{
						totals[1][dayIndex]--;
					}
				}
			}
			//Similarly, the same process as above is applied here, except that sent begins at 1 and received at 0.
			else{
				for(int q = 1; q < conversation.size(); q+=2){
					int dayIndex = findDay(conversation.get(q));
					totals[0][dayIndex]++;

					if(conversation.size() > q+1){
						times[0][dayIndex] += findHour(conversation.get(q), conversation.get(q+1));
					}
					else{
						totals[0][dayIndex]--;
					}
				}
				for(int q = 0; q < conversation.size(); q+=2){
					int dayIndex = findDay(conversation.get(q));
					totals[1][dayIndex]++;

					if(conversation.size() > q+1){
						times[1][dayIndex] += findHour(conversation.get(q), conversation.get(q+1));
					}
					else{
						totals[1][dayIndex]--;
					}
				}
			}
		}

		//Here we just divide the total at each index and divide by its total count in totals to calculate the average.
		for(int q = 0; q < 7; q++){
			times[0][q] /= totals[0][q];
			times[1][q] /= totals[1][q];
		}

		JsonObject combined = new JsonObject();
		JsonArray sent = new JsonArray();
		JsonArray rece = new JsonArray();
		for(int q = 0; q < 7; q++){
			sent.add(times[0][q]);
			rece.add(times[1][q]);
		}
		combined.add("SentEmails", sent);
		combined.add("ReceivedEmails", rece);

		return combined;
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

	/**
	 * This method finds the hour an email is received. Minutes are represented as decimal values after the whole.
	 * @param start The email prior to the one received / The email sent.
	 * @param end The email received / The email after the one sent.
	 * @return The hour received as a number of hours, whose decimal is equivalent to the minutes.
	 */
	private double findHour(Email start, Email end){
		long diff = end.getDateReceived().getTime()-start.getDateReceived().getTime();
		return diff / oneHour;
	}
}
