import Runnables.ValidationRunnable;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import edu.oswego.database.Database;
import edu.oswego.model.Email;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@ServerEndpoint("/engine")
public class Websocket {
    //this is to manage all current/last active threads for each unique sessions
    //the format of the object array is as such [Thread, Handler, int, string]
    ConcurrentHashMap<String,Object[]> sessionThreadMapper = new ConcurrentHashMap<>();
    //TODO add a CHM ^^^ to account for handling validation threads

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Session "+session.getId()+" been established");
        Object[] threadNrunnable = {null,null,-1,null, null};
        sessionThreadMapper.put(session.getId(),threadNrunnable);
    }

    //This method allows you to message a specific user.
        /*
        session.getBasicRemote().sendText(message);
        */
    @OnMessage //method that communicates with clients
    public void onMessage(String message, Session session) {
        Object[] array = sessionThreadMapper.get(session.getId());
        array[2] =(int) array[2]+1;
        //The first message that must be sent to the websocket is a valid googlejsonobject which will construct everything
        if((int) array[2]==0){

            JsonObject oAuth2 = makeJsonObject(message);
            if(oAuth2 == null){
                try {
                    session.getBasicRemote().sendText("m:invalid_jsonObject: you've been disconnected");
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            Database db = new Database();
            AtomicBoolean emailsExist = new AtomicBoolean(false);
            AtomicReference<Database> database = new AtomicReference<>(db);
            AtomicReference<JsonObject> googleOauth2 = new AtomicReference<>(oAuth2);

            AtomicReference<Session> atomicSession = new AtomicReference<>(session);

            message = "folders";
            AtomicReference<String> atomicMessage = new AtomicReference<>(message);

            Handler handler = new Handler(atomicSession,atomicMessage,googleOauth2,database,emailsExist);
            ValidationRunnable validationRunnable = new ValidationRunnable(atomicSession,googleOauth2,database,emailsExist);

            Thread handlerThread = new Thread(handler);
            Thread validationThread = new Thread(validationRunnable);

            validationThread.start();
            handlerThread.start();


            array[0] = handlerThread;
            array[1] = handler;
            array[3] = validationThread;
            array[4] = validationRunnable;


        }else if(message.equals("refresh")){
            databaseValidation(session);
        }else{
            newRequest(session,message,array);
        }

    }

    @OnClose //method to disconnect from a session : this also interrupts everything and stops everything
    public void onClose(Session session) {
        System.out.println("onClose");
        ((Thread) sessionThreadMapper.get(session.getId())[0]).interrupt();
        sessionThreadMapper.remove(session.getId());
    }

    @OnError
    public void onError(Throwable t, Session session) {
        System.out.println("onError::");
    }

    /*
    all private functional methods are below
     */

    private void databaseValidation(Session session){
        Object[] array = sessionThreadMapper.get(session.getId());
        Thread dv = (Thread) array[3];

        if(dv.getState().equals(Thread.State.TERMINATED)){//any state is possible except start
            ValidationRunnable oldValidationRunnable = (ValidationRunnable) array[4];
            ValidationRunnable validationRunnable = new ValidationRunnable(oldValidationRunnable);
            Thread validationThread = new Thread(validationRunnable);
            validationThread.start();

            array[3]=validationThread;
            array[4]=validationRunnable;
        }
        try {
            session.getBasicRemote().sendText("m:incomplete request");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void newRequest(Session session, String message, Object[] array){
        Thread oldHandlerThread = (Thread) array[0];

        if(!oldHandlerThread.getState().equals(Thread.State.TERMINATED)){
            oldHandlerThread.interrupt();
        }

        Handler oldHandler = (Handler) array[1];
        Handler newHandler = new Handler(oldHandler,message,oldHandlerThread);
        Thread newHandlerThread = new Thread(newHandler);

        array[0]=newHandlerThread;
        array[1]=newHandler;

        sessionThreadMapper.put(session.getId(),array);
    }

    //new JsonParser().parse(message).getAsJsonObject();
    private JsonObject makeJsonObject(String message){
        try {
            return new JsonParser().parse(message).getAsJsonObject();
        }catch(Exception e){

        }
        return null;
    }
}
