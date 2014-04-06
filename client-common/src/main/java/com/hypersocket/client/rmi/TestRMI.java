package com.hypersocket.client.rmi;

import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class TestRMI {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new RMISecurityManager());
			}

			Registry registry = LocateRegistry.getRegistry(50000);

			ConnectionService connectionService = (ConnectionService) registry
					.lookup("connectionService");

//			Connection con = connectionService.createNew();

//			con.setConnectAtStartup(false);
//			con.setHashedPassword("123");
//			con.setHostname("glade.shacknet.nu");
//			con.setPort(443);
//			con.setStayConnected(true);
//			con.setUsername("admin");
//
//			connectionService.save(con);

			
			List<Connection> connections = connectionService.getConnections();
			
			for(Connection c : connections) {
				System.out.println(c.getId());
				System.out.println(c.getHostname());
				System.out.println(c.getPath());
				System.out.println(c.getPort());
				System.out.println(c.getUsername());
				System.out.println(c.getHashedPassword());
				System.out.println();
			}
			
			
			ConfigurationService configurationService = (ConfigurationService) 
					registry.lookup("configurationService");
			
			String value1 = configurationService.getValue("donotsave", "i should not be saved");
			System.out.println(value1);
			
			configurationService.setValue("save me", "I should be saved");
			String value2 = configurationService.getValue("save me", "i should not see this default");
			System.out.println(value2);
			
			for(ConfigurationItem i : configurationService.getConfigurationItems()) {
				System.out.println(i.getName() + "=" + i.getValue());
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
