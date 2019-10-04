package Runnables;

import Database.Database;
import JavaMail.Mailer;

import java.util.concurrent.atomic.AtomicBoolean;

public class ValidationRunnable implements Runnable{
    private static AtomicBoolean emailStored;
    public ValidationRunnable(AtomicBoolean ab){
        emailStored=ab;
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
