import Runnables.*;
import com.google.gson.JsonObject;
import edu.oswego.database.Database;
import edu.oswego.model.Email;
import edu.oswego.model.UserFolder;

import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Handler implements Runnable {
    private volatile AtomicReference<Session> session;
    private volatile AtomicReference<Database> database;
    private volatile AtomicReference<JsonObject> googleAccessToken;
    private volatile AtomicBoolean hasEmails;
    private volatile AtomicReference<String> message;
    private volatile AtomicReference<Thread> oldThread;

    Handler(AtomicReference<Session> session,AtomicReference<String> message, AtomicReference<JsonObject> googleAccessToken,AtomicReference<Database> database, AtomicBoolean hasEmails){
        this.session=session;
        this.message = message;
        this.googleAccessToken = googleAccessToken;
        this.database = database;
        this.hasEmails = hasEmails;
    }

    Handler(Handler handler, String message, Thread oldThread){
        this.session = handler.session;
        this.database=handler.database;
        this.googleAccessToken=handler.googleAccessToken;
        this.hasEmails=handler.hasEmails;
        this.message = new AtomicReference<>(message);
        this.oldThread = new AtomicReference<>(oldThread);
    }


    @Override
    public void run() {

        if (googleAccessToken == null || Thread.interrupted()) {
            return;
        }else if(message.equals("folders")){
            try {
                sendFolders();
            } catch (IOException e) {
                //cant really do anything
            }
        }else{
            try {
                handleMessage(message.get());
            } catch (InterruptedException e) {

            }
        }

    }

    private void sendFolders() throws IOException {
        List<UserFolder> folders =  database.get().getFolders(googleAccessToken.get().getAsJsonObject("").get("email").getAsString());
        session.get().getBasicRemote().sendText(folders.toString());
    }




    private void handleMessage(String message) throws InterruptedException {
        //waiting to know if db has data or not: look at ValidationRunnable
        while(!hasEmails.get()){
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new InterruptedException();
            }
        }
        //TODO make sure gui sends comma seperators
        /*
        filters include: folder,date,interval,seen_email,attatchment
        ex cs,2/12/2019,week,t,t
         */
        String[] filters = message.split(",");
        //Waiting for the oldThread to terminate
        while(oldThread.get().isAlive()){
            if(Thread.interrupted()){
                throw new InterruptedException();
            }
        }
        //parse the booleans
        boolean attachment;
        boolean seen;
        if(filters[3].contains("t")){
            seen = true;
        }else{
            seen = false;
        }
        if(filters[4].contains("t")){
            attachment = true;
        }else{
            attachment = false;
        }
        //get all the emails
        ArrayList<Email> emails = database.get().getMetaDataWithAppliedFilters(filters[0],filters[1],filters[2],attachment,seen);
        //this will store the final data
        if(Thread.interrupted()){
            throw new InterruptedException();
        }
        //Making all the callables and futures and executing them in threads
        DomainCallable dc = new DomainCallable(emails);
        SentimentScoreCallable ssc = new SentimentScoreCallable(emails);
        FolderCallable fc = new FolderCallable(emails);
        NumOfEmailsCallable noec = new NumOfEmailsCallable(emails);
        SnRCallable src = new SnRCallable(emails);
        TimeBetweenRepliesCallable tbrc = new TimeBetweenRepliesCallable(emails);

        FutureTask sscTask = new FutureTask<>(ssc);
        FutureTask dcTask = new FutureTask<>(dc);
        FutureTask fcTask = new FutureTask<>(fc);
        FutureTask noecTask = new FutureTask<>(noec);
        FutureTask srcTask = new FutureTask<>(src);
        FutureTask tbrcTask = new FutureTask<>(tbrc);


        if(Thread.interrupted()){
            throw new InterruptedException();
        }

        new Thread(sscTask).start();
        new Thread(dcTask).start();
        new Thread(fcTask).start();
        new Thread(noecTask).start();
        new Thread(srcTask).start();
        new Thread(tbrcTask).start();

        Object dcData;
        Object sscData;
        Object fcData;
        Object noecData;
        Object srcData;
        Object tbrcData;
        //todo add the calculations for each callable
        try {
            sscData = sscTask.get();
            dcData = dcTask.get();
            fcData = fcTask.get();
            noecData = noecTask.get();
            srcData = srcTask.get();
            tbrcData = tbrcTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        JsonObject dataSet = new JsonObject();
        //todo, combine all the data into final jsonobject and send it over to gui


    }



}
