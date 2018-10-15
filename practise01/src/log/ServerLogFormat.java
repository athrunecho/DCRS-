package log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class ServerLogFormat extends Formatter{ 
	
		public String format(LogRecord record) {
            Date date = new Date();  
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            String d = sd.format(date);  
            return "[" + d + "]"  + "[" +record.getLevel() + "]" + record.getMessage()+"\r\n"; 
		}   
} 