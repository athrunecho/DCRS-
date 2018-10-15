package dcrsImpl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import dcrsInterface.RemoteInterface;
import log.ServerLogging;


public class SOENRemoteImpl extends UnicastRemoteObject implements RemoteInterface{
	private static final String localHostName = "SOEN";
	
	//This HashMap contain the course name and capacity in different semester. 
	private static HashMap<String, HashMap<String, Integer>> courseRecords = new HashMap<String, HashMap<String, Integer>>();
	
	//This HashMap contain the students' IDs in specific course.
	private static HashMap<String, List<String>> enrollist = new HashMap<String, List<String>>();	
	
	//This HashMap contain the courses by the specific student ID.
	private static HashMap<String, HashMap<String, String>> students = new HashMap<String, HashMap<String, String>>();
	
	//This HashMap count the course number for each semester which the student has enrolled.
	private static HashMap<String, HashMap<String, Integer>> courseLimit = new HashMap<String, HashMap<String, Integer>>();
	
	//This HashMap count the number of courses which the student enrolled from other departments.
	private static HashMap<String,Integer> deptlimit = new HashMap<>();
	
	//semname is a String Array to compare the semester from user input.
	private final static String[] semname = {"Fall", "Winter", "Summer"};
	
	//UserID records the current User's ID.
	private static String UserID;
	
	//Method of inheriting the parent class.
	public SOENRemoteImpl() throws RemoteException {
		super();
		
	}
	
	//login method records the User ID for print log file.
	public void login(String userID) throws RemoteException{
		UserID = userID;
		ServerLogging.getLogMessage(localHostName, UserID + " login success");
	}
	
	//addCourse receive the HashMap of Courses and return the result of add course¡£
	public synchronized String addCourse(String courseID, String semester,int capacity) throws RemoteException{

		HashMap<String, Integer> sem = new HashMap<String, Integer>();
		//SOEN advisor only can add SOEN courses.
		if(courseID.contains(localHostName)) {
			for(int i = 0; i < 3; i++) {
				if(semester.equals(semname[i])) {
					//Avoid null pointer errors.
					if(courseRecords.get(semester) == null) {
						ServerLogging.getLogMessage(localHostName, UserID + " Add "+ courseID + " in " + semester);
						sem.put(courseID, capacity);
						//Add new course to HashMap courses
						courseRecords.put(semester, sem);
						//Update enrollist to response the action of add course.
						enrollist.put(courseID+semester, null);
						ServerLogging.getLogMessage(localHostName, UserID + " Add "+ courseID +" success");
						return "Add "+ courseID +" success";
					}
				sem = courseRecords.get(semester);
				sem.put(courseID, capacity);
				courseRecords.put(semester, sem);
				enrollist.put(courseID+semester, null);
				ServerLogging.getLogMessage(localHostName, UserID + " Add "+ courseID +" success");
				return "Add "+ courseID +" success";
				}				
			}
		} 
		ServerLogging.getLogMessage(localHostName, UserID + "Add "+ courseID +" failed");
		return "Add "+ courseID +" failed";
	}

	//removeCourse receive the HashMap of Courses and return the result of remove course.
	public synchronized String removeCourse(String courseID, String semester) throws RemoteException{
		String reply = "";
		HashMap<String, Integer> sem = new HashMap<String, Integer>();
		
		for(int i = 0; i < 3; i++) {
			//Find the right semester.
			if(semester.equals(semname[i])) {
				if(courseRecords.get(semester) == null) {
					ServerLogging.getLogMessage(localHostName, UserID + " " + courseID +" not exist in " + semester);
					return courseID +" not exist in " + semester;
				}
				//Drop course for students who has already enrolled.
				List<String> namelist = enrollist.get(courseID+semester);
				if(enrollist.get(courseID+semester) != null) {
					Iterator<String> name = namelist.iterator();
				
					while (name.hasNext()) {
						String n = name.next();
						//local modify
						if(n.contains(localHostName)) {
							//Remove course from students.
							HashMap<String, String> enrolled = students.get(n);
							enrolled.remove(courseID, semester);
							students.put(n, enrolled);
						
							//modify the course limit.
							HashMap<String, Integer> limit = courseLimit.get(n);
							int m = limit.get(semester);
							m = m - 1;
							limit.put(semester, m);
							courseLimit.put(n, limit);
						}
					
						//Remote modify
						if(n.contains("COMP")) {
							reply = UDPrequest(5555, "removeCourse." + n + "." + courseID + "." + semester);
							//Check the reply status.
						if(reply.contains("success") == false) {
							ServerLogging.getLogMessage(localHostName, UserID + " Remove course to SOEN students failed");		
							return reply;
						}
						}
					
						if(n.contains("INSE")) {
							reply = UDPrequest(6667, "removeCourse." + n + "." + courseID + "." + semester);
							//Check the reply status.
							if(reply.contains("success") == false) {
								ServerLogging.getLogMessage(localHostName, UserID + " Remove course to INSE students failed");		
								return reply;
							}
						}
					}
				//Remove courseID from enrollist
				enrollist.remove(courseID+semester, namelist);
				}
				//Remove course record from courseRecords.
				sem = courseRecords.get(semester);
				int n = sem.get(courseID);
				sem.remove(courseID, n);
				ServerLogging.getLogMessage(localHostName, UserID + " Remove " + courseID + " success");
				return "Remove " + courseID + " success";
			}
		}
		ServerLogging.getLogMessage(localHostName, UserID + " Remove course failed");
		return "Remove course failed";
	}
	
	
	
	//listCourseAvl shows every courses in all the departments by semester.
	public synchronized String listCourseAvl(String semester) throws RemoteException{
		String reply1 = "";
		String reply2 = "";
		ServerLogging.getLogMessage(localHostName, UserID + " List course Available in " + semester);
		for(int i = 0; i < 3; i++) {
			//Find the right semester.
			if(semester.equals(semname[i])) {
				//UDP request for courses to COMP server.
				reply1 = UDPrequest(5555, "listCourseAvl."+semname[i]);
				ServerLogging.getLogMessage(localHostName, "SOENServer reply: " + reply1);
				//UDP request for courses to INSE server.
				reply2 = UDPrequest(6667, "listCourseAvl."+semname[i]);
				ServerLogging.getLogMessage(localHostName, "INSEServer reply: " + reply2);
				HashMap<String, Integer> sem = courseRecords.get(semname[i]);
				if(sem == null) {
					ServerLogging.getLogMessage(localHostName, "SOEN have no course"+"\n"+reply1+"\n"+reply2);
					return "SOEN have no course"+"\n"+reply1+"\n"+reply2;
				}
				ServerLogging.getLogMessage(localHostName, sem.toString()+"\n"+reply1+"\n"+reply2);
				return sem.toString()+"\n"+reply1+"\n"+reply2;
			}
		}
		ServerLogging.getLogMessage(localHostName, "List course available failed");
		return "List course available failed";
	}

	
	//enrolCourse can enrol course from all the department by studentID, courseID and semester
	public synchronized String enrolCourse(String studentID, String courseID, String semester) throws RemoteException {
		String reply = "";
		HashMap<String, String> enrolled = new HashMap<String, String>();
		List<String> namelist = new LinkedList<String>();
		
		//Determine which server the course is.
		if(courseID.contains(localHostName)) {

			for(int i = 0; i < 3; i++) {
				if(semester.equals(semname[i])) {
					//check if the studentID exist in the courseLimit.
					if (courseLimit.containsKey(studentID)) {
						HashMap<String, Integer> limit = courseLimit.get(studentID);
						//check if the course number has reached the upper limit.
						if(limit.get(semester) != null) {
							if(limit.get(semester) >= 3) {
							ServerLogging.getLogMessage(localHostName, "List course available failed");
							return "course number in "+ semester +" has reached the upper limit";
							}
						}
					}
					//Check if there is course in this semester.
					if(courseRecords.get(semester) == null) {
						ServerLogging.getLogMessage(localHostName, "No " + courseID + " in " + semester);
						return "No " + courseID + " in " + semester;
					}
					HashMap<String, Integer> sem = courseRecords.get(semester);
					Set<String> courseSet = sem.keySet();
					//Using loop to find the right course. 
					for(String courseKey : courseSet){
						if(courseKey.equals(courseID)) {
							int cap = sem.get(courseID);
							//Check if there is any seat in this course.
							if(cap > 0){
								//Modify the capacity.
								sem.put(courseID, cap-1);
								courseRecords.put(semester, sem);
								
								//Modify the HashMap of enroll list.
								for(i=0; i < 3; i++){										
									if(enrollist.get(courseID+semname[i]) != null) {
										namelist = enrollist.get(courseID+semname[i]);
										for(String data : namelist) {   
											if(data.equals(studentID)) {
												ServerLogging.getLogMessage(localHostName, studentID + " have already enrolled " + courseID + " in " + semname[i]);
												return studentID + " have already enrolled " + courseID + " in " + semname[i];
											}   
										}
									}
								}
								//Add student name to enroll list.
								if(enrollist.get(courseID+semester) == null) {
									namelist.add(studentID);
									enrollist.put(courseID+semester, namelist);
								} else {
									namelist = enrollist.get(courseID+semester);
									namelist.add(studentID);
									enrollist.put(courseID+semester, namelist);									
								}
								
								if(studentID.contains(localHostName)) {
									//Modify the course list of student.
									if(students.get(studentID) == null) {
										enrolled.put(courseID, semester);
										students.put(studentID, enrolled);
									} else {
										enrolled = students.get(studentID);
										enrolled.put(courseID, semester);
										students.put(studentID, enrolled);
									}
										
									//Modify the course limit in that semester of student.
									if(courseLimit.get(studentID) == null) {
										//Modify the limits
										HashMap<String, Integer> limit = new HashMap<String, Integer>();
										limit.put("Fall", 0);
										limit.put("Summer", 0);
										limit.put("Winter", 0);
										limit.put(semester, 1);
										courseLimit.put(studentID, limit);
									} else {
										HashMap<String, Integer> limit = courseLimit.get(studentID);
										int m = limit.get(semester);
										m = m+1;
										limit.put(semester, m);
										courseLimit.put(studentID, limit);
									}
								}
								ServerLogging.getLogMessage(localHostName, UserID + studentID + " enroll "+ courseID +" success");
								return studentID + " enroll "+ courseID +" success";
							}
							ServerLogging.getLogMessage(localHostName, UserID + "This "+ courseID +" is full");
							return "This "+ courseID +" is full";
						}
					}
				}
			}
			ServerLogging.getLogMessage(localHostName, UserID + "Enrol failed" + courseID);
			return "Enrol failed" + courseID;
		}
		
		//Enrol course on the remote server.
		//Check the department limit of the course.
		if(deptlimit.containsKey(studentID)) {
			//reach the limit of outter department course.
			if(deptlimit.get(studentID) >= 2) {
				ServerLogging.getLogMessage(localHostName, UserID + studentID + "can not enrol course from other department any more");
				return studentID + "can not enrol course from other department any more";
			}
		}
		//check if the studentID exist in the courseLimit.
		if (courseLimit.containsKey(studentID)) {
			HashMap<String, Integer> limit = courseLimit.get(studentID);
			if(limit.get(semester) == null) {
				//Initial the limits
				limit.put(semester, 0);
				courseLimit.put(studentID, limit);
			}
			//check if the course number has reached the upper limit.
			if(limit.get(semester) >= 3) {
				ServerLogging.getLogMessage(localHostName, UserID + "course number in "+ semester +" has reached the upper limit");
				return "course number in "+ semester +" has reached the upper limit";
			}
		}
		//Enrol course on COMP server.
		if(courseID.contains("COMP")) {
			reply = UDPrequest(5555, "enrolCourse." + semester + "." + courseID + "." + studentID);
			//Check the reply status.
			if(reply.contains("success")) {
				//Modify department limit.
				if(deptlimit.get(studentID) == null) {
					deptlimit.put(studentID, 1);
				} else {
					int n = deptlimit.get(studentID);
					n = n+1;
					deptlimit.put(studentID, n);
				}
				//Modify course limit in that semester.
				if(courseLimit.get(studentID) == null) {
					HashMap<String, Integer> limit = new HashMap<String, Integer>();
					limit.put(semester, 1);
					courseLimit.put(studentID, limit);
				}else {
					HashMap<String, Integer> limit = courseLimit.get(studentID);
					int m = limit.get(semester);
					m = m+1;
					limit.put(semester, m);
					courseLimit.put(studentID, limit);
				}
				//Modify courselist for student.
				if(students.get(studentID) == null) {
					enrolled.put(courseID, semester);
					students.put(studentID, enrolled);
				} else {
					enrolled = students.get(studentID);
					enrolled.put(courseID, semester);
					students.put(studentID, enrolled);
				}
			}
			ServerLogging.getLogMessage(localHostName, UserID + reply);
			return reply;
		}
		//Enrol course on INSE server.
		if(courseID.contains("INSE")) {
			reply = UDPrequest(6667, "enrolCourse." + semester + "." + courseID + "." + studentID);
			//Check the reply status.
			if(reply.contains("success")) {
				//Modify department limit.
				if(deptlimit.get(studentID) == null) {
					deptlimit.put(studentID, 1);
				} else {
					int n = deptlimit.get(studentID);
					n = n+1;
					deptlimit.put(studentID, n);
				}
				//Modify course limit in that semester.
				if(courseLimit.get(studentID) == null) {
					HashMap<String, Integer> limit = new HashMap<String, Integer>();
					limit.put(semester, 1);
					courseLimit.put(studentID, limit);
				}else {
					HashMap<String, Integer> limit = courseLimit.get(studentID);
					int m = limit.get(semester);
					m = m+1;
					limit.put(semester, m);
					courseLimit.put(studentID, limit);
				}
				//Modify courselist for student.
				if(students.get(studentID) == null) {
					enrolled.put(courseID, semester);
					students.put(studentID, enrolled);
				} else {
					enrolled = students.get(studentID);
					enrolled.put(courseID, semester);
					students.put(studentID, enrolled);
				}
			}
			ServerLogging.getLogMessage(localHostName, UserID + reply);
			return reply;
		}
		ServerLogging.getLogMessage(localHostName, UserID + "Not correct courseID" + courseID);
		return "Not correct courseID" + courseID;
	}

	
	
	//dropCourse
	public synchronized String dropCourse(String studentID, String courseID) throws RemoteException {
		String reply = "";
		List<String> namelist = new LinkedList<String>();
		
		//Check drop course from local server or remote server.
		if(courseID.contains(localHostName)) {
			
			for(int i = 0; i < 3; i++) {
				if(enrollist.containsKey(courseID+semname[i])) {
					namelist = enrollist.get(courseID+semname[i]);
					if(namelist == null) {
						ServerLogging.getLogMessage(localHostName, UserID + " No student in enrollist");
						return "No student in enrollist";
					}
					
					//Remove studentID from namelist.
					if(namelist.contains(studentID)) {
						namelist.remove(studentID);
						enrollist.put(courseID+semname[i], namelist);
							
						//Modify the capacity.
						HashMap<String, Integer> sem = courseRecords.get(semname[i]);
						int cap = sem.get(courseID);
						sem.put(courseID, cap + 1);
						courseRecords.put(semname[i], sem);
							
						//If the student is the local department student.
						if(studentID.contains(localHostName)) {

							//Modify the course list of student.
							HashMap<String,String> enrolled = students.get(studentID);
							enrolled.remove(courseID, semname[i]);
							students.put(studentID, enrolled);
									
							//Modify the course limit in that semester of student.
							HashMap<String, Integer> limit = courseLimit.get(studentID);
							int m = limit.get(semname[i]);
							m = m+1;
							limit.put(semname[i], m);
							courseLimit.put(studentID, limit);
						}
						//drop course success.
						ServerLogging.getLogMessage(localHostName, UserID + " Drop "+ courseID + " for " + studentID + " success");
						return "Drop "+ courseID + " for " + studentID + " success";
					}
					ServerLogging.getLogMessage(localHostName, UserID + " " + studentID + " is not in this course");
					return studentID + " is not in this course";		
				}
			}
			ServerLogging.getLogMessage(localHostName, UserID + " Remove "+ studentID +" from stuList failed");
			return "Remove "+ studentID +" from stuList failed";
		}
		
		//Remote drop course
		if(courseID.contains("COMP")) {
			reply = UDPrequest(5555, "dropCourse." + studentID + "." + courseID);
			//Check the reply status.
			if(reply.contains("success")) {

				//Modify department limit.
				int n = deptlimit.get(studentID);
				n = n - 1;
				deptlimit.put(studentID, n);
				
				//Modify courselist for student.
				HashMap<String,String> enrolled = students.get(studentID);
				String semester = enrolled.get(courseID);
				enrolled.remove(courseID, semester);
				students.put(studentID, enrolled);
				
				//Modify course limit in that semester.
				HashMap<String, Integer> limit = courseLimit.get(studentID);
				int m = limit.get(semester);
				m = m - 1;
				limit.put(semester, m);
				courseLimit.put(studentID, limit);
			}
			ServerLogging.getLogMessage(localHostName, UserID + " "+ reply);		
			return reply;
		}
		
		if(courseID.contains("INSE")) {
			reply = UDPrequest(6667, "dropCourse." + studentID + "." + courseID);
			//Check the reply status.
			if(reply.contains("success")) {

				//Modify department limit.
				int n = deptlimit.get(studentID);
				n = n - 1;
				deptlimit.put(studentID, n);
				
				//Modify courselist for student.
				HashMap<String,String> enrolled = students.get(studentID);
				String semester = enrolled.get(courseID);
				enrolled.remove(courseID, semester);
				students.put(studentID, enrolled);
				
				//Modify course limit in that semester.
				HashMap<String, Integer> limit = courseLimit.get(studentID);
				int m = limit.get(semester);
				m = m - 1;
				limit.put(semester, m);
				courseLimit.put(studentID, limit);
			}
			ServerLogging.getLogMessage(localHostName, UserID + " "+ reply);
			return reply;
		}
		//
		ServerLogging.getLogMessage(localHostName, UserID + " No such course.");
		return "No such course.";
	}

	//getClassSche return all the courses enrolled by specific studentID.
	public synchronized String getClassSche(String studentID) throws RemoteException {
		if(students == null) {
			ServerLogging.getLogMessage(localHostName, "Empty class schedule.");
			return "Empty class schedule.";
		}
		HashMap<String, String> Sche = students.get(studentID);
		if (Sche == null) {
			ServerLogging.getLogMessage(localHostName, "Empty class schedule for " + studentID);
			return "Empty class schedule for " + studentID;
		}
		ServerLogging.getLogMessage(localHostName, Sche.toString());
		return Sche.toString();
	}
	
	//UDPrequest send 
	public static String UDPrequest(int serverPort, String param) {
		DatagramSocket aSocket = null;
		try {
			aSocket = new DatagramSocket();
			byte[] message = param.getBytes();
			InetAddress aHost = InetAddress.getByName("localhost");
			DatagramPacket request = new DatagramPacket(message, param.length(), aHost, serverPort);
			aSocket.send(request);
			byte[] buffer = new byte[1000];
			DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
			aSocket.receive(reply);
			return new String(reply.getData());
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("IO: " + e.getMessage());
		} finally {
			if (aSocket != null)
				aSocket.close();
		}
		ServerLogging.getLogMessage(localHostName, "failed");
		return "failed";
	}
	
	//UDPreceiver receive UDP request and choose the right method.
	public void UDPreceiver(int port) {
		byte[] result = null;
		String feedback = null; 
		DatagramSocket aSocket = null;
		
		//
		try {
			aSocket = new DatagramSocket(port);
			byte[] buffer = new byte[1000];
			System.out.println("New tread Started............");
			while (true) {
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				String str = new String(request.getData());
				String arg[] = str.split("\\.");
				String fliter = arg[0];
				//
				//compare method.
				if(fliter.equals("listCourseAvl")) {
					String semes = arg[1].trim();
					if(semes.contains("Fall")) {
						HashMap<String, Integer> c = courseRecords.get("Fall");
						if(c != null) {
							feedback = c.toString();
							result = feedback.getBytes();
						}
					}
					if(semes.contains("Winter")) {
						HashMap<String, Integer> c = courseRecords.get("Winter");
						if(c != null) {
							feedback = c.toString();
							result = feedback.getBytes();
						}
					}
					if(semes.contains("Summer")) {
						HashMap<String, Integer> c = courseRecords.get("Summer");
						if(c != null) {
							feedback = c.toString();
							result = feedback.getBytes();
						}
					}					
					if(feedback == null) {
						feedback = localHostName + " have no course";
						result = feedback.getBytes();
					}
				}		
				
				//UDP removeCourse
				//arg[0] = method   arg[1] = studentID   arg[2] = courseID    arg[3].trim() = semester
				if(fliter.equals("removeCourse")) {
					ServerLogging.getLogMessage(localHostName, arg[1]+ " " + arg[2] + " " + arg[3].trim());
					String semester = arg[3].trim();
					//Remove course from students.
					HashMap<String, String> enrolled = students.get(arg[1]);
					if(enrolled.remove(arg[2], semester)) {
						students.put(arg[1], enrolled);
					
						//modify the course limit.
						HashMap<String, Integer> limit = courseLimit.get(arg[1]);
						int m = limit.get(semester);
						m = m - 1;
						limit.put(semester, m);
						courseLimit.put(arg[1], limit);
					
						//modify the department limit.
						int n = deptlimit.get(arg[1]);
						n = n - 1;
						deptlimit.put(arg[1], n);
					
						feedback = "removeCourse success";
						result = feedback.getBytes();
						}else {
							feedback = "remove students failed";
							result = feedback.getBytes();
						}
				}
				
				//UDP enrolCourse
			    //arg[0] = method   arg[1] = semester    arg[2] = courseID    stu = studentID
				if(fliter.equals("enrolCourse")) {
					feedback = enrolCourse(arg[3].trim(), arg[2], arg[1]);
					result = feedback.getBytes();
				}
				
				//UDP dropCourse
				//arg[0] = method   arg[1] = studentID    arg[2] = couseID
				if(fliter.equals("dropCourse")){
					feedback = dropCourse(arg[1], arg[2].substring(0, 8));
					result = feedback.getBytes();
				}
				
				DatagramPacket reply = new DatagramPacket(result, feedback.length(), request.getAddress(), request.getPort());
				aSocket.send(reply);
				}
		} catch (SocketException e) {
			System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO: " + e.getMessage());
		} finally {
			//
			feedback = null;
			if (aSocket != null)
				aSocket.close();
		}
	}
}
