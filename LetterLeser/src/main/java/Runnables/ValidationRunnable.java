package Runnables;

import edu.oswego.database.Database;
import edu.oswego.mail.Mailer;

import com.google.gson.JsonObject;

import java.util.concurrent.atomic.AtomicBoolean;

public class ValidationRunnable implements Runnable{
    private static AtomicBoolean emailStored;
    private static JsonObject googleAccessToken;
    
    public ValidationRunnable(AtomicBoolean ab){
        emailStored=ab;
    }

    public void setGoogleAccessToken(JsonObject jo){
        googleAccessToken = jo;
    }
    @Override
    public void run() {
        Database db = new Database();
        Mailer m = new Mailer();
        /*
        get db emails waiting on method
        obj listOfEmails = db.method();
         */

        /*
        if(listOfEmails != null/0){
            emailStored = true;
            run a one to one comparison
        }else{
            just dump everything from Mailer into db
        }
         */
    }
}
