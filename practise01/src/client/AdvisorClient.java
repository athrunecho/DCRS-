package client;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dcrsInterface.RemoteInterface;
import log.*;

public class AdvisorClient {
	private static int userstatus = 0;
	private static String userid;
	
	private static int Port(String ID) throws RemoteException{
		 
		 int portNbr = 0;
		 final Matcher m1;
		 final Matcher m2;
		 final Matcher m3;
		 final Pattern p1;
		 final Pattern p2;
		 final Pattern p3;
		 
		 p1 = Pattern.compile("COMP");
		 p2 = Pattern.compile("SOEN");
		 p3 = Pattern.compile("INSE");	
		 m1 = p1.matcher(ID);
		 if(m1.lookingAt() == true) {
		 portNbr = 1234;
		 return portNbr;
		 }
		 m2 = p2.matcher(ID);
		 if(m2.lookingAt() == true) {
		 portNbr = 1235;
		 return portNbr;
		 }
		 m3 = p3.matcher(ID);
		 if(m3.lookingAt() == true) {
		 portNbr = 1236;
		 return portNbr;
		 }
		 System.out.println("Not right ID");
		return portNbr;
	}
	
	//User check for advisor method.
	private static int Identity(String ID) throws RemoteException{
	    userid = ID;  
		if("".equals(ID) || ID == null ){
	    	  	return userstatus;
	        }
	        if(ID.length()!=9){
	        	return userstatus;
	        }
	        if("A".equals(ID.substring(4,5))){
	            userstatus = 2;
	            return userstatus;
	        }
	        return userstatus;
	    }
	
	 public static void main(String args[]) throws Exception {
		 boolean check = true;
		 System.out.println("Please input your ID:");
		
		 BufferedReader sin=new BufferedReader(new InputStreamReader(System.in));
		 String readline;
		 readline=sin.readLine();
		 //User check
		 Identity(readline);
		 System.out.println("Welcome,"+ readline);
		 Registry registry = LocateRegistry.getRegistry(Port(readline));
		 RemoteInterface obj = (RemoteInterface) registry.lookup(readline.substring(0,4) + "Server");
		 obj.login(readline);
		 ClientLogging.getLogMessage(userid, userid + " has login.");
		 while(check){
			 System.out.println("Please choose you action:\n"+"1.Add Course\n"+"2.Remove Course\n"+
		 "3.List Course Available\n"+"4.Enrol Course\n"+"5.Drop Course\n"+"6.Get Class Schedule\n"+"7.Exit");
			 readline = sin.readLine();
			 int n = Integer.parseInt(readline);
			 switch(n) {
			     case 1:
			    	 //add user check
			    	 if(userstatus == 2) {
			    	 System.out.println("Please input the course ID");
			    	 String courseID = sin.readLine();			    	 
			    	 System.out.println("Please input the semester");
			    	 String semester = sin.readLine();
			    	 System.out.println("Please input the capacity");
			    	 String cap = sin.readLine();
			    	 int capacity = Integer.parseInt(cap);
			    	 
			    	 System.out.println(obj.addCourse(courseID, semester,capacity));
					 ClientLogging.getLogMessage(userid, userid + " add " + courseID + " in " + semester);
			    	 check = true;
			    	 break;
			    	 }
			    	 System.out.println("You are not a advisor");
			    	 check = true;
			    	 break;
			     
			     case 2:
			    	 if(userstatus == 2) {
			    	 //add user check
			    	 System.out.println("Please input the semester");
			    	 String semester = sin.readLine();
			    	 System.out.println("Please input the course ID");
			    	 String courseID = sin.readLine();
			    	 System.out.println(obj.removeCourse(courseID, semester));
			    	 ClientLogging.getLogMessage(userid, userid + " remove " + courseID + " in " + semester);
			    	 check = true;
			    	 break;
			    	 }
			    	 System.out.println("You are not a advisor");
			    	 check = true;
			    	 break;

			     case 3:
			    	 if(userstatus == 2) {
			    		 //add user check
			    		 System.out.println("Please input semester");
			    		 String semester = sin.readLine();
			    		 System.out.println(obj.listCourseAvl(semester));
			    		 ClientLogging.getLogMessage(userid, userid + " list course available in " + semester);
			    		 check = true;
			    		 break;
	    	         	}
	    	         	System.out.println("You are not a advisor");
	    	         	check = true;
	    	         	break;
			     
			     case 4:
			    	 if(userstatus > 0) {
			    		System.out.println("Please input student ID"); 
			    		String studentID = sin.readLine();
						//Student can only enrol course from their department server.
						if(userid.substring(0, 4).equals(studentID.substring(0, 4)) == false){
							System.out.println("You can not enrol course for student from other department.");
							check = true;
							break;
						}
			    		System.out.println("Please input course ID");
			    		String courseID = sin.readLine();
			    		System.out.println("Please input semester");
			    		String semester = sin.readLine();
			    		System.out.println(obj.enrolCourse(studentID, courseID, semester));
			    		ClientLogging.getLogMessage(userid, userid + " enrolled " + courseID + " in " + semester + " for " + studentID);
			    		check = true;
			    		break;
			    	 }
	    	         System.out.println("You have not login");
	    	         check = true;
	    	         break;
			     
			     case 5:
			    	 if(userstatus > 0) {
			    		 System.out.println("Please input student ID"); 
			    		 String studentID = sin.readLine();
						 if(userid.substring(0, 4).equals(studentID.substring(0, 4)) == false){
							 System.out.println("You can not drop course for student from other department.");
							 check = true;
							 break;
						 }
			    		 System.out.println("Please input course ID");
			    		 String courseID = sin.readLine();
			    		 System.out.println(obj.dropCourse(studentID, courseID));
			    		 ClientLogging.getLogMessage(userid, userid + " dropped " + courseID + " for " + studentID);
			    	 check = true;
			    	 break;
			    	 }
	    	         System.out.println("You have not login");
	    	         check = true;
	    	         break;
			     
			     case 6:
			    	 if(userstatus > 0) {
			    		 System.out.println("Please input student ID"); 
			    		 String studentID = sin.readLine();
			    		 System.out.println(obj.getClassSche(studentID));
			    		 ClientLogging.getLogMessage(userid, userid + " get class Schedule of " + studentID);
			    	 check = true;
			    	 break;
			    	 }
			    	 System.out.println("You have not login");
			    	 check = true;
			    	 break;
			     
			     case 7:
			         check = false;
			         break;
			     
			     default:
			    	 readline = sin.readLine();
			    	 System.out.println("No such function");
			    	 check = true;
			    	 break;
			 	}
			 }
		 System.out.println("Client shutdown");
		 System.exit(0);
	}
}
