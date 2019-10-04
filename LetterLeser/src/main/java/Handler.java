import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.websocket.Session;

public class Handler implements Runnable {
    private static Session session;
    private static MySQLServer sqlServer;
    private static Calculations calculations;
    private static InboxHandler inboxHandler;
    private static RunnableObjects runnableObjects;
    private static JsonObject googleAccessToken;

    Handler(Session session){
        this.session=session;
        this.sqlServer = new MySQLServer();
        this.calculations = new Calculations();
        this.inboxHandler = new InboxHandler();
        this.runnableObjects = new RunnableObjects();
        googleAccessToken=null;
    }


    @Override
    public void run() {

        if (googleAccessToken == null) {
            return;
        }else{
            handleMessage();
        }

    }

    public boolean setGoogleAccessToken(String message){
        try {
            googleAccessToken = new JsonParser().parse(message).getAsJsonObject();
            return true;
        }catch(Exception e){
            return false;
        }
    }




    private void handleMessage(){

    }

}
