package Runnables;

import edu.oswego.database.Database;

import com.google.gson.JsonObject;

import javax.websocket.Session;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class ValidationRunnable implements Runnable{
    private volatile AtomicReference<Session> session;
    private volatile AtomicReference<JsonObject> googleAccessToken;
    private volatile AtomicReference<Database> atomicDatabase;
    public volatile AtomicBoolean emailStored;

    public ValidationRunnable(AtomicReference<Session> session,AtomicReference<JsonObject> googleAccessToken, AtomicReference<Database> atomicDatabase,AtomicBoolean emailStored){
        this.emailStored=emailStored;
        this.atomicDatabase = atomicDatabase;
        this.googleAccessToken = googleAccessToken;
        this.session = session;
    }

    public ValidationRunnable(ValidationRunnable validationRunnable){
        this.emailStored=validationRunnable.emailStored;
        this.atomicDatabase = validationRunnable.atomicDatabase;
        this.googleAccessToken = validationRunnable.googleAccessToken;
        this.session = validationRunnable.session;
    }

    @Override
    public void run() {
        //TODO check if that is how you get email from google json object
        String email = googleAccessToken.get().getAsJsonObject("profileObj").get("email").getAsString();
        Database db = atomicDatabase.get();
        if(db.hasEmails(email)){
            emailStored.compareAndSet(false,true);
            //TODO validate DB
        }else{
            db.populateDatabase(email);
        }
    }
}
