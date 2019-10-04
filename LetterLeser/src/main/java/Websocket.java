import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashMap;

@ServerEndpoint("/engine")
public class Websocket {
    //this is to manage all current/last active threads for each unique sessions
    //the format of the object array is as such [Thread, Handler, int, string]
    HashMap<String,Object[]> sessionThreadMapper = new HashMap<>();


    @OnOpen
    public void onOpen(Session session) {


        System.out.println("Session "+session.getId()+" been established");
        Handler handler = new Handler(session);
        Thread sessionThread = new Thread(handler);//this is technically not needed but prevents null checks later in the code
        Thread dbValidThread = new Thread();
        /*
        validThread needs a runnable to handle database validation thread.
        once a correct runnable has been added, delete this comment
         */
        Object[] threadNrunnable = {sessionThread,handler,-1,dbValidThread};
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
        //The first message that must be sent to the websocket is a valid googlejsonobject
        if((int) array[2]==0){
            if(((Handler) array[1]).setGoogleAccessToken(message)){
                databaseValidation();
            }else {
                try {
                    session.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else if(message.equals("refresh")){
            databaseValidation();
        }else{
            newRequest(session,array);
        }

    }

    @OnClose //method to disconnect from a session
    public void onClose(Session session) {
        System.out.println("onClose");
        sessionThreadMapper.remove(session.getId());
    }

    @OnError
    public void onError(Throwable t, Session session) {
        ((Thread) sessionThreadMapper.get(session.getId())[0]).interrupt();
        System.out.println("onError::");
    }

    /*
    all private functional methods are below
     */

    private void databaseValidation(){

    }

    private void newRequest(Session session, Object[] array){
        Thread thread = (Thread) array[0];
        Handler handler = (Handler) array[1];
        Handler newHandler = new Handler(session);
        Thread newThread = new Thread(newHandler);
        if(!thread.getState().equals(Thread.State.TERMINATED)|| !thread.getState().equals(Thread.State.NEW)){
            thread.interrupt();
        }
        newThread.start();
        array[0]=newThread;
        array[1]=newHandler;
        sessionThreadMapper.put(session.getId(),array);
    }


}
