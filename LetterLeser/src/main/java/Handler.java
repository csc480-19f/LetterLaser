import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.websocket.Session;
import java.util.concurrent.atomic.AtomicBoolean;

public class Handler implements Runnable {
    private static Session session;
    private static MySQLServer sqlServer;
    private static Calculations calculations;
    private static InboxHandler inboxHandler;
    private static JsonObject googleAccessToken;
    private static AtomicBoolean dataBaseHasData;
    private static String message;

    Handler(Session session){
        this.session=session;
        this.sqlServer = new MySQLServer();
        this.calculations = new Calculations();
        this.inboxHandler = new InboxHandler();
        googleAccessToken=null;
        message = null;
        dataBaseHasData = new AtomicBoolean(false);
    }
    Handler(Session session,String message){
        this.session=session;
        this.sqlServer = new MySQLServer();
        this.calculations = new Calculations();
        this.inboxHandler = new InboxHandler();
        googleAccessToken=null;
        this.message = message;
        dataBaseHasData = new AtomicBoolean(false);
    }


    @Override
    public void run() {

        if (googleAccessToken == null || Thread.interrupted()) {
            return;
        }else{
            handleMessage(message);
        }

    }

    public JsonObject getGoogleAccessToken(){
        return googleAccessToken;
    }

    public AtomicBoolean getAtomicBoolean(){
        return dataBaseHasData;
    }


    public boolean setGoogleAccessToken(String message){
        try {
            googleAccessToken = new JsonParser().parse(message).getAsJsonObject();
            return true;
        }catch(Exception e){
            return false;
        }
    }




    private void handleMessage(String message){

    }

}
