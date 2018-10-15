package dcrsInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteInterface extends Remote {
	
	public void login(String userID) throws RemoteException;

	public String addCourse(String courseID, String semester, int capacity) throws RemoteException;
	
	public String removeCourse(String courseID, String semester) throws RemoteException;
	
	public String listCourseAvl(String semester) throws RemoteException;
	
	public String enrolCourse(String studentID, String courseID, String semester) throws RemoteException;
	
	public String dropCourse(String studentID, String courseID) throws RemoteException;
	
	public String getClassSche(String studetID) throws RemoteException;
}
