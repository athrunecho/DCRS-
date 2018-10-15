package server;

import java.rmi.registry.*;

import dcrsImpl.INSERemoteImpl;

public class InseServer{

	public static void main(String[] args) throws Exception {
		
		INSERemoteImpl stub = new INSERemoteImpl();

		Registry registry = LocateRegistry.createRegistry(1236);
		
		registry.bind("INSEServer", stub);
		
		System.out.println("INSEServer is Started");
		
		//UDPreceiver receive UDP request and choose the right method.
		Runnable COMP = () -> {
			try{
			//port for receive from COMP
			stub.UDPreceiver(6666);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
		Runnable SOEN = () -> {
			try{
			//port for receive from INSE
			stub.UDPreceiver(6667);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};

		Thread COMPtread = new Thread(COMP);
		COMPtread.start();
		Thread SOENtread = new Thread(SOEN);
		SOENtread.start();
	}
  }