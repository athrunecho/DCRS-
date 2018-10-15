package log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import log.LogFormat;

public class ServerLogging {
    
    public static void getLogMessage(String ServerName, String loginfo){
        Logger logger = Logger.getLogger("Server");
 
         try {
             FileHandler fileHandler = new FileHandler("D:\\"+ ServerName + ".txt");
             fileHandler.setFormatter(new LogFormat());
             logger.addHandler(fileHandler);
             logger.info(loginfo);
         } catch (SecurityException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
    }
}