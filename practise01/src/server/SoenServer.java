package server;


import java.rmi.registry.*;

import dcrsImpl.SOENRemoteImpl;;

public class SoenServer{

	public static void main(String[] args) throws Exception {
		
		SOENRemoteImpl stub = new SOENRemoteImpl();

		Registry registry = LocateRegistry.createRegistry(1235);
		
		registry.bind("SOENServer", stub);
		
		System.out.println("SOENServer is Started");
		
		//UDPreceiver receive UDP request and choose the right method.
		Runnable COMP = () -> {
			try{
			//port for receive from SOEN
			stub.UDPreceiver(7777);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		Runnable INSE = () -> {
			try{
			//port for receive from INSE
			stub.UDPreceiver(7778);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		Thread COMPtread = new Thread(COMP);
		COMPtread.start();
		Thread INSEtread = new Thread(INSE);
		INSEtread.start();
		}
}
