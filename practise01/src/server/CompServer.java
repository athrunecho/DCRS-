package server;


import java.rmi.registry.*;

import dcrsImpl.COMPRemoteImpl;;

public class CompServer{

	public static void main(String[] args) throws Exception {
		
		COMPRemoteImpl stub = new COMPRemoteImpl();

		Registry registry = LocateRegistry.createRegistry(1234);
		
		registry.bind("COMPServer", stub);
		
		System.out.println("COMPServer is Started");
		
		//UDPreceiver receive UDP request and choose the right method.
		Runnable SOEN = () -> {
			try{
			//port for receive from SOEN
			stub.UDPreceiver(5555);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		Runnable INSE = () -> {
			try{
			//port for receive from INSE
			stub.UDPreceiver(5556);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		Thread SOENtread = new Thread(SOEN);
		SOENtread.start();
		Thread INSEtread = new Thread(INSE);
		INSEtread.start();
		}
}
