package edu.oswego.Runnables;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import edu.oswego.database.Database;
import edu.oswego.model.Email;
import edu.oswego.model.UserFavourites;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import javax.websocket.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
		this.userFavourites=null;
	}

	public Handler(Session session, Database database, String email, UserFavourites userFavourites) {
		this.session = session;
		this.database = database;
		this.userFavourites = userFavourites;
		this.email = email;
		this.jsonObject=null;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("hanlder");

		if (jsonObject != null) {
			try {
				String folderName = jsonObject.get("foldername").getAsString();
				String sd = jsonObject.get("date").getAsString();
				String interval = jsonObject.get("interval").getAsString();
				boolean attachment = jsonObject.get("attachment").getAsBoolean();
				boolean seen = jsonObject.get("seen").getAsBoolean();
				DateTime startDate = new DateTime(DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").parseMillis(sd));
				DateTime endDate = getEndDate(startDate, interval);
				List<Email> emails = database.getEmailByFilter(attachment, startDate.toDate().toString(),
						endDate.toDate().toString(), seen, folderName);
				performCalculations(emails);
			}catch(IllegalArgumentException | SQLException | ClassNotFoundException e){
				sendErrorMessage(session,"error:\n" + e.getMessage());
			}
		} else if (userFavourites != null) {
			List<Email> emails = null;
			try {
				emails = database.getEmailByFilter(userFavourites.isHasAttachment(),userFavourites.getStartDate().toString(),userFavourites.getEndDate().toString(),userFavourites.isSeen(),userFavourites.getFolder().getFolder().getFullName());
			} catch (SQLException | ClassNotFoundException e) {
				e.printStackTrace();
				sendErrorMessage(session,"error:\n" + e.getMessage());
			}
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
		JsonObject js = new JsonObject();
		try {
			int sentiment = (int) ssc.get(20, TimeUnit.SECONDS);
			JsonArray domain = (JsonArray) dc.get(20, TimeUnit.SECONDS);
			JsonArray folder = (JsonArray) fc.get(20, TimeUnit.SECONDS);
			JsonArray numOfMail = (JsonArray) noec.get(20, TimeUnit.SECONDS);
			JsonObject sendNRec = (JsonObject) snrc.get(20, TimeUnit.SECONDS);
			JsonObject timeBetween = (JsonObject) tbrc.get(20, TimeUnit.SECONDS);

			js = new JsonObject();
			js.addProperty("messagetype", "graphs");
			JsonObject graph = new JsonObject();
			graph.addProperty("sentimentscore", sentiment);
			graph.add("emailbydomain", domain);
			graph.add("emailbyfolder", folder);
			graph.add("emailssentandrecieved", sendNRec);
			graph.add("numberofemails", numOfMail);
			graph.add("timebetweenreplies", timeBetween);
			js.add("graphs", graph);


		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			js = new JsonObject();
			js.addProperty("messagetype","statusupdate");
			js.addProperty("message","request ended\n" +
					"TimeoutException Occurred\n" +
					"StackTrace:\n" +
					sw.toString());
		}catch(Exception e){
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			e.printStackTrace(pw);
			js = new JsonObject();
			js.addProperty("messagetype","statusupdate");
			js.addProperty("message","request ended\n" +
					"unknown exception Occurred\n" +
					"StackTrace:\n" +
					sw.toString());
		}

		try {
			session.getBasicRemote().sendText(js.toString());
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

	private void sendUpdateStatusMessage(Session session,String message){
		JsonObject js = new JsonObject();
		js.addProperty("messagetype","statusupdate");
		js.addProperty("message",message);
		sendMessageToClient(session,js);
	}

	private void sendErrorMessage(Session session,String errorMessage){
		JsonObject js = new JsonObject();
		js.addProperty("messagetype","error");
		js.addProperty("message",errorMessage);
		sendMessageToClient(session,js);
	}

	private void sendMessageToClient(Session session, JsonObject returnMessage) {
		try {
			session.getBasicRemote().sendText(returnMessage.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
