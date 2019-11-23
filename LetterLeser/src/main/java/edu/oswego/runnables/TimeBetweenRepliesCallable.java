package edu.oswego.runnables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.oswego.model.Email;
import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.format.DateTimeFormat;

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
     * 	    indexes [2][x] are total for sent
     * 	    indexes [3][x] are total for recieve
	 */
	@Override
	public Object call() throws Exception {
		//The returned value, a 2-d array that stores the average time between sent at [0][x], and received at [1][x].
		double[][] times = new double[4][7];
		//This is the total number of emails sent, corresponding to the times in the array above.
		//This bit will save us time later.
		ArrayList<String> keys = new ArrayList<>();

		/*
		This section converts the emails array list into a hash map.
		This hash map has an array list stored as its value at the subject key as emails in a conversation all have
		the same subject, at least for gmail.
		 */
		HashMap<String, ArrayList<Email>> subjects = new HashMap<>();
		for(Email e : emails){
			String subj = e.getSubject();
			//If the subject exists in subjects already
			if(subjects.containsKey(subj)){
				subjects.get(subj).add(e);
			}
			//Or if it exists as a reply
			else if(subj.length() >= 4 && subj.substring(0,4).equalsIgnoreCase("Re: ")){
				ArrayList<Email> list = subjects.get(subj.substring(4));
				if(list!=null){
					list.add(e);
				}else{
					subj = subj.substring(4);
					subjects.put(subj, new ArrayList<Email>());
					subjects.get(subj).add(e);
					keys.add(subj);
				}
			}
			//Or is it's new
			else{
				subjects.put(subj, new ArrayList<Email>());
				subjects.get(subj).add(e);
				keys.add(subj);
			}
		}


		for(String key : keys){
            ArrayList<Email> emailThread = subjects.get(key);
            for(int i=0;i<emailThread.size()-1;i++){
                if(emailThread==null){continue;}
                Email first = emailThread.get(i);
                Email next = emailThread.get(i+1);
                DateTime startDate = new DateTime(first.getDateReceived());
                DateTime endDate = new DateTime(next.getDateReceived());
                int hours;
                try {
					hours = Hours.hoursBetween(startDate, endDate).getHours();
				}catch(Exception e){
                	continue;
				}
                int dayOfWeek = getIndexOfDay(startDate.dayOfWeek().toString());
                int sentOrRecieved;
                //if sent else recieved
                if(first.getFrom().get(0).getEmailAddress().equalsIgnoreCase(user)){
                    sentOrRecieved = 0;
                 }else{
                    sentOrRecieved = 1;
                }

                times[sentOrRecieved][dayOfWeek] = times[sentOrRecieved][dayOfWeek] + hours;
                times[sentOrRecieved+2][dayOfWeek] = times[sentOrRecieved+2][dayOfWeek] + 1;

            }

        }
		//Here's where that bit from earlier is going to save us time.
		/*for(String key : keys){
			ArrayList<Email> conversation = subjects.remove(key);

			//We use this to check to see if the first email is from the user.
			boolean sent = false;
			boolean convNull = (conversation==null);
			if(!convNull) {
				if (conversation.get(0).getFrom().get(0).getEmailAddress().equalsIgnoreCase(user)) sent = true;
				//And here we figure out how long it took between emails and store it appropriately. It gets averaged later.
				if (sent) {
					//Since the user sent the first email, time between replies for sent starts at index 0,
					for (int q = 0; q < conversation.size(); q += 2) {
						int dayIndex = findDay(conversation.get(q));
						totals[0][dayIndex]++;

						//If there is an email that has been received as a result of sending the email at index q
						if (conversation.size() > q + 1) {
							//Then we calculate the time between replies.
							times[0][dayIndex] += findHour(conversation.get(q), conversation.get(q + 1));
						}
						//Otherwise,
						else {
							//We discount the email from the total.
							totals[0][dayIndex]--;
						}
					}
					//and time between replies for received starts at 1.
					for (int q = 1; q < conversation.size(); q += 2) {
						int dayIndex = findDay(conversation.get(q));
						totals[1][dayIndex]++;

						if (conversation.size() > q + 1) {
							times[1][dayIndex] += findHour(conversation.get(q), conversation.get(q + 1));
						} else {
							totals[1][dayIndex]--;
						}
					}
				}
				//Similarly, the same process as above is applied here, except that sent begins at 1 and received at 0.
				else {
					for (int q = 1; q < conversation.size(); q += 2) {
						int dayIndex = findDay(conversation.get(q));
						totals[0][dayIndex]++;

						if (conversation.size() > q + 1) {
							times[0][dayIndex] += findHour(conversation.get(q), conversation.get(q + 1));
						} else {
							totals[0][dayIndex]--;
						}
					}
					for (int q = 0; q < conversation.size(); q += 2) {
						int dayIndex = findDay(conversation.get(q));
						totals[1][dayIndex]++;

						if (conversation.size() > q + 1) {
							times[1][dayIndex] += findHour(conversation.get(q), conversation.get(q + 1));
						} else {
							totals[1][dayIndex]--;
						}
					}
				}
			}
		}*/

		//Here we just divide the total at each index and divide by its total count in totals to calculate the average.
		/*for(int q = 0; q < 7; q++){
                times[0][q] /= totals[0][q];
                times[1][q] /= totals[1][q];
		}*/
		for(int i=0;i<7;i++){
		    if(times[2][i]!=0){
                times[0][i] = times[0][i]/times[2][i];
            }else{
                times[0][i]=0;
            }
		    if(times[3][i]!=0){
                times[1][i] = times[1][i]/times[3][i];
            }else{
                times[1][i]=0;
            }
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

	private int getIndexOfDay(String day){
	    if(day.equalsIgnoreCase("sunday")){
	        return 0;
        }else if(day.equalsIgnoreCase("monday")){
            return 1;
        }else if(day.equalsIgnoreCase("tuesday")){
            return 2;
        }else if(day.equalsIgnoreCase("wednesday")){
            return 3;
        }else if(day.equalsIgnoreCase("thursday")){
            return 4;
        }else if(day.equalsIgnoreCase("friday")){
            return 5;
        }else{//saturday
            return 6;
        }

    }

	/*/**
	 * This method takes an email object and determines which day the email was
	 * receive on.
	 *
	 * @param e
	 *            The email. This email has a date that is at least three days
	 *            greater than 1-1-1970.
	 * @return 0 for Sun, 1 for Mon, 2 for Tue, ... , 6 for Sat.
	 */
	/*private int findDay(Email e) {
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
	/*private double findHour(Email start, Email end){
		long diff = end.getDateReceived().getTime()-start.getDateReceived().getTime();
		return diff / oneHour;
	}*/
}
