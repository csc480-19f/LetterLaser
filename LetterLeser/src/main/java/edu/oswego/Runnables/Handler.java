package edu.oswego.Runnables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.oswego.database.Database;
import edu.oswego.model.Email;
import edu.oswego.model.UserFavourites;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.json.Json;
import javax.websocket.Session;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class Handler implements Runnable {
	private Database database;
	private JsonObject jsonObject;
	private UserFavourites userFavourites;
	private String email;
	private Session session;

	public Handler(Session session, Database database, String email, JsonObject jsonObject) {
		this.session = session;
		this.database = database;
		this.jsonObject = jsonObject;
		this.email = email;
	}

	public Handler(Session session, Database database, String email, UserFavourites userFavourites) {
		this.session = session;
		this.database = database;
		this.userFavourites = userFavourites;
		this.email = email;
	}

	@Override
	public void run() {

		if (jsonObject != null) {
			try {
				String folderName = jsonObject.get("foldername").getAsString();
				String sd = jsonObject.get("date").getAsString();
				String interval = jsonObject.get("interval").getAsString();
				boolean attachment = jsonObject.get("attachment").getAsBoolean();
				boolean seen = jsonObject.get("seen").getAsBoolean();
				DateTime startDate = new DateTime(DateTimeFormat.forPattern("MM/dd/yyyy HH:mm").parseMillis(sd));
				DateTime endDate = getEndDate(startDate, interval);
				List<Email> emails = database.getEmailByFilter(attachment, startDate.toDate().toString(),
						endDate.toDate().toString(), seen, folderName);
				performCalculations(emails);
			}catch(IllegalArgumentException iae){
				JsonObject js = new JsonObject();
				js.addProperty("messagetype","statusupdate");
				js.addProperty("message","IllegalArgument for DATETIME");
				try {
					session.getBasicRemote().sendText(js.toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else if (userFavourites != null) {
			List<Email> emails = database.getEmailByFilter(userFavourites.isHasAttachment(),userFavourites.getStartDate().toString(),userFavourites.getEndDate().toString(),userFavourites.isSeen(),userFavourites.getFolder().getFolder().getFullName());
			performCalculations(emails);
		} else {
			System.out.println("no userfav or json so no calc can be preformed");
		}
	}

	private void performCalculations(List<Email> emailList) {
		if(emailList.isEmpty()){
			JsonObject js = new JsonObject();
			js.addProperty("messagetype", "statusupdate");

			js.addProperty("message", "no emails obtained with current filter");

			try {
				session.getBasicRemote().sendText(js.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}

		SentimentScoreCallable sentimentScoreCallable = new SentimentScoreCallable(emailList);
		DomainCallable domainCallable = new DomainCallable(emailList);
		FolderCallable folderCallable = new FolderCallable(emailList);
		NumOfEmailsCallable numOfEmailsCallable = new NumOfEmailsCallable(emailList);
		SnRCallable snRCallable = new SnRCallable(emailList, email);
		TimeBetweenRepliesCallable timeBetweenRepliesCallable = new TimeBetweenRepliesCallable(emailList, email);

		FutureTask ssc = new FutureTask(sentimentScoreCallable);
		FutureTask dc = new FutureTask(domainCallable);
		FutureTask fc = new FutureTask(folderCallable);
		FutureTask noec = new FutureTask(numOfEmailsCallable);
		FutureTask snrc = new FutureTask(snRCallable);
		FutureTask tbrc = new FutureTask(timeBetweenRepliesCallable);

		Thread t1 = new Thread(ssc);
		t1.start();
		Thread t2 = new Thread(dc);
		t2.start();
		Thread t3 = new Thread(fc);
		t3.start();
		Thread t4 = new Thread(noec);
		t4.start();
		Thread t5 = new Thread(snrc);
		t5.start();
		Thread t6 = new Thread(tbrc);
		t6.start();
		try {
			int sentiment = (int) ssc.get();
			JsonArray domain = (JsonArray) dc.get();
			JsonArray folder = (JsonArray) fc.get();
			JsonArray numOfMail = (JsonArray) noec.get();
			JsonObject sendNRec = (JsonObject) snrc.get();
			JsonObject timeBetween = (JsonObject) tbrc.get();

			JsonObject js = new JsonObject();
			js.addProperty("messagetype", "graphs");
			JsonObject graph = new JsonObject();
			graph.addProperty("sentimentscore", sentiment);
			graph.add("emailbydomain", domain);
			graph.add("emailbyfolder", folder);
			graph.add("emailssentandrecieved", sendNRec);
			graph.add("numberofemails", numOfMail);
			graph.add("timebetweenreplies", timeBetween);
			js.add("graphs", graph);

			session.getBasicRemote().sendText(js.toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private DateTime getEndDate(DateTime startDate, String interval) {
		if (interval.equals("year")) {
			return startDate.plusYears(1);
		} else if (interval.equals("month")) {
			return startDate.plusMonths(1);
		} else {// week
			return startDate.plusWeeks(1);
		}
	}

}
