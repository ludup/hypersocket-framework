package com.hypersocket.client;

import java.beans.Introspector;
import java.io.File;
import java.io.FileOutputStream;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Properties;
import java.util.Random;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.rmi.ClientService;
import com.hypersocket.client.rmi.ConfigurationService;
import com.hypersocket.client.rmi.ConnectionService;
import com.hypersocket.client.service.ClientServiceImpl;
import com.hypersocket.client.service.ConfigurationServiceImpl;
import com.hypersocket.client.service.ConnectionServiceImpl;

public class Main {

	static Logger log = LoggerFactory.getLogger(Main.class);

	ConnectionServiceImpl connectionService;
	ConfigurationServiceImpl configurationService;
	ClientServiceImpl clientService;
	Properties properties = new Properties();
	Registry registry;
	int port;

	Main() {
		
	}

	void buildServices() throws RemoteException {

		int attempts = 100;
		while (attempts > 0) {
			port = randInt(49152, 65535);
			try {
				if (log.isInfoEnabled()) {
					log.info("Trying RMI server on port " + port);
				}
				registry = LocateRegistry.createRegistry(port);
				if (log.isInfoEnabled()) {
					log.info("RMI server started on port " + port);
				}
				properties.put("port", String.valueOf(port));
				FileOutputStream out;
				if (Boolean.getBoolean("hypersocket.development")) {
					File f = new File(System.getProperty("user.home")
							+ File.separator + ".hypersocket" + File.separator
							+ "conf" + File.separator + "rmi.properties");
					f.getParentFile().mkdirs();
					out = new FileOutputStream(f);
				} else {
					File f = new File("conf" + File.separator
							+ "rmi.properties");
					f.getParentFile().mkdirs();
					out = new FileOutputStream("conf" + File.separator
							+ "rmi.properties");
				}

				try {
					properties.store(out, "Hypersocket Client Service");
				} finally {
					out.close();
				}
				break;
			} catch (Exception e) {
				attempts--;
				continue;
			}
		}
		
		if(registry==null) {
			throw new RemoteException("Failed to startup after 100 attempts");
		}

		if (log.isInfoEnabled()) {
			log.info("Creating ConnectionService");
		}
		connectionService = new ConnectionServiceImpl();

		if (log.isInfoEnabled()) {
			log.info("Creating ConfigurationService");
		}

		configurationService = new ConfigurationServiceImpl();

		if (log.isInfoEnabled()) {
			log.info("Creating ClientService");
		}

		clientService = new ClientServiceImpl(connectionService,
				configurationService);
	}

	public static int randInt(int min, int max) {
		Random rand = new Random();
		int randomNum = rand.nextInt((max - min) + 1) + min;
		return randomNum;
	}
	
	void poll() {
		
		try {
			while(true) {
				try {
					Thread.sleep(2500L);
				} catch (InterruptedException e) {
					
				}
				
				registry.list();

			}
		} catch (AccessException e) {
			log.error("RMI server seems to have failed", e);
		} catch (RemoteException e) {
			log.error("RMI server seems to have failed", e);
		}
		
		try {
			registry.unbind("clientService");
		} catch (Exception e) {
		}

		try {
			registry.unbind("configurationService");
		} catch (Exception e) {
		}

		try {
			registry.unbind("connectionService");
		} catch (Exception e) {
		}
		
		try {
			UnicastRemoteObject.unexportObject(registry, true);
		} catch (NoSuchObjectException e) {
		}
	}

	boolean publishServices() {

		try {
			publishService(ConnectionService.class, connectionService);
			publishService(ConfigurationService.class, configurationService);
			publishService(ClientService.class, clientService);
			return true;
		} catch (Exception e) {
			log.error("Failed to publish service", e);
			return false;
		}
	}

	<T extends Remote> void publishService(Class<T> type, T obj)
			throws Exception {

		String name = Introspector.decapitalize(type.getSimpleName());

		if (log.isInfoEnabled()) {
			log.info("Publishing service " + name);
		}

		Remote stub = UnicastRemoteObject.exportObject(obj, port);
		registry.rebind(name, stub);

		if (log.isInfoEnabled()) {
			log.info("Published service " + name);
		}

	}

	public boolean start() {
		return clientService.startService();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		File logs = new File("logs");
		logs.mkdirs();

		PropertyConfigurator.configureAndWatch("conf" + File.separator
				+ "log4j.properties");
		
		System.setProperty("java.rmi.server.hostname", "localhost");
		
		BasicConfigurator.configure();
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		Main main = new Main();

		while(true) {
			try {
				main.buildServices();
	
				if (!main.publishServices()) {
					System.exit(1);
				}
	
				if (!main.start()) {
					System.exit(2);
				}
				
				main.poll();
			} catch (Exception e) {
				log.error("Failed to start", e);
				try {
					Thread.sleep(2500L);
				} catch (InterruptedException e1) {
				}
			}
		}
	}

}
