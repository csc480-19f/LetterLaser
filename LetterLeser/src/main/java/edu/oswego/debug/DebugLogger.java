package edu.oswego.debug;

import java.io.IOException;
import java.util.logging.*;
/*
This class is only for debug use. Will be removed at final deployment thus no tests required for this class
 */
public class DebugLogger {
    private static Logger logger = Logger.getLogger(DebugLogger.class.getName());
    private static FileHandler fh;

    private DebugLogger(){
        logger.setUseParentHandlers(false);
        try {
            //first var is the path to log file and second is to append or not
            fh = new FileHandler("",true);
            logger.addHandler(fh);
            fh.setFormatter(new SimpleFormatter());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logEvent(Level level,String event){
        logger.log(level,event);
    }

}
