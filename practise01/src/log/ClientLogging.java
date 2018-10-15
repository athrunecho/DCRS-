package log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import log.LogFormat;

public class ClientLogging {
    
    public static void getLogMessage(String UserID, String loginfo){
        Logger logger = Logger.getLogger("Client");
 
         try {
             FileHandler fileHandler = new FileHandler("D:\\"+ UserID + ".txt");
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