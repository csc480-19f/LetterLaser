package edu.oswego.debug;

import java.io.IOException;
import java.util.logging.*;
/*
This class is only for debug use. Will be removed at final deployment thus no tests required for this class
 */
public class DebugLogger {
//    private static Logger logger;
//    private static FileHandler fh;
//    private static String piFilePath = "/home/csc480f19/LetterLeserLogFolder/LogFile.log";//use this only if you want to test on pi
//    private static String localFilePath = "D:\\logstuff\\logFile.log";//fill this for your local path if you want to debug on pc and replace piFilePath in filehandler constructor

    public static void logEvent(String name, Level level,String event){
//        logger = Logger.getLogger(name);
//        logger.setUseParentHandlers(false);
//        try {
//            //first var is the path to log file and second is to append or not (true is to append, replaces old file);
//            fh = new FileHandler(localFilePath,true);
//            logger.addHandler(fh);
//            fh.setFormatter(new SimpleFormatter());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        logger.log(level,event);
//        fh.close();
    }

}
