package edu.oswego.runnables;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.websocket.Session;

import org.joda.time.DateTime;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.oswego.model.Email;
import edu.oswego.model.UserFavourites;
import edu.oswego.websocket.Messenger;

public class Handler implements Runnable {
	private JsonObject jsonObject;
	private UserFavourites userFavourites;
	private String email;
	private Session session;
	private Messenger messenger = new Messenger();

	public Handler(Session session, String email, JsonObject jsonObject) {
		this.session = session;
		this.jsonObject = jsonObject;
		this.email = email;
		this.userFavourites=null;
	}

	public Handler(Session session, String email, UserFavourites userFavourites) {
		this.session = session;
		this.userFavourites = userFavourites;
		this.email = email;
		this.jsonObject=null;
	}

	@Override
	public void run() {
//		Thread.currentThread().setName("handler");
//
//		if (jsonObject != null) {
//			try {
//				String folderName = jsonObject.get("foldername").getAsString();
//				String sd = jsonObject.get("date").getAsString();
//				String interval = jsonObject.get("interval").getAsString();
//				boolean attachment = jsonObject.get("attachment").getAsBoolean();
//				boolean seen = jsonObject.get("seen").getAsBoolean();
//				DateTime startDate = new DateTime(DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss").parseMillis(sd));
//				DateTime endDate = getEndDate(startDate, interval);
//				List<Email> emails;
//
//				SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//				String stringStartDate = formater.format(startDate.toDate());
//				String stringEndDate = formater.format(endDate.toDate());
//
//				try {
//					emails = database.getEmailByFilter(attachment, stringStartDate,
//                            stringEndDate, seen, folderName);
//				}catch(Throwable t){
//					messenger.sendErrorMessage(session,"Error in DB: \n"+t.getMessage());
//					return;
//				}
//				performCalculations(emails);
//			}catch(IllegalArgumentException e){
//				messenger.sendErrorMessage(session,"Error:\n" + e.getMessage());
//			}
//		} else if (userFavourites != null) {
//			List<Email> emails = null;
//			try {
//				emails = database.getEmailByFilter(userFavourites.isHasAttachment(), userFavourites.getStartDate().toString(), userFavourites.getEndDate().toString(), userFavourites.isSeen(), userFavourites.getFolder().getFolder().getFullName());
//			}catch(Throwable t){
//				messenger.sendErrorMessage(session,"Error in DB: \n"+t.getMessage());
//				return;
//			}
//			performCalculations(emails);
//		} else {
//			System.out.println("no userfav or json so no calc can be preformed");
//		}

	}

	private void performCalculations(List<Email> emailList) {
		if(emailList.isEmpty()){
			messenger.sendUpdateStatusMessage(session,"No emails obtained with current filter.\nemailList.isEmpty is true.");
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
		JsonObject js;
		try {
			JsonObject sentiment = (JsonObject) ssc.get(2, TimeUnit.MINUTES);
			JsonArray domain = (JsonArray) dc.get(2, TimeUnit.MINUTES);
			JsonArray folder = (JsonArray) fc.get(2, TimeUnit.MINUTES);
			JsonArray numOfMail = (JsonArray) noec.get(2, TimeUnit.MINUTES);
			JsonObject sendNRec = (JsonObject) snrc.get(2, TimeUnit.MINUTES);
			JsonObject timeBetween = (JsonObject) tbrc.get(2, TimeUnit.MINUTES);

			js = new JsonObject();
			js.addProperty("messagetype", "graphs");
			JsonObject graph = new JsonObject();
			graph.add("sentimentscore", sentiment);
			graph.add("emailbydomain", domain);
			graph.add("emailbyfolder", folder);
			graph.add("emailssentandrecieved", sendNRec);
			graph.add("numberofemails", numOfMail);
			graph.add("timebetweenreplies", timeBetween);
			js.add("graphs", graph);


		} catch (InterruptedException e) {
			e.printStackTrace();
			messenger.sendErrorMessage(session,"Request ended Callable interrupted\n"+e.getMessage());
			return;
		} catch (ExecutionException e) {
			e.printStackTrace();
			messenger.sendErrorMessage(session,"Request ended executionException:\n"+e.getMessage());
			return;
		} catch (TimeoutException e) {
			messenger.sendErrorMessage(session,"Request ended\n" +
					"TimeoutException Occurred\n" +
					e.getMessage());
			messenger.sendUpdateStatusMessage(session,"Calculation took longer than 2 minutes, please.\n" +
					e.getMessage());
			return;
		}catch(Exception e){
			messenger.sendErrorMessage(session,"Request ended\n" +
					"unknown exception Occurred\n"  +
					e.getMessage());
			return;
		}
		messenger.sendMessageToClient(session,js);
		System.out.println("handler thread finished");

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
