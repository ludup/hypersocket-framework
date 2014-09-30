package com.hypersocket.client;

import java.beans.Introspector;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.rmi.ClientService;
import com.hypersocket.client.rmi.ConfigurationService;
import com.hypersocket.client.rmi.ConnectionService;
import com.hypersocket.client.rmi.ResourceService;
import com.hypersocket.client.service.ClientServiceImpl;
import com.hypersocket.client.service.ConfigurationServiceImpl;
import com.hypersocket.client.service.ConnectionServiceImpl;
import com.hypersocket.client.service.ResourceServiceImpl;

public class Main {

	static Logger log = LoggerFactory.getLogger(Main.class);

	ConnectionServiceImpl connectionService;
	ConfigurationServiceImpl configurationService;
	ResourceService resourceService;
	ClientServiceImpl clientService;
	Properties properties = new Properties();
	Registry registry;
	int port;

	Runnable restartCallback;
	Runnable shutdownCallback;
	ClassLoader classLoader;
	static Main instance;

	public Main(Runnable restartCallback, Runnable shutdownCallback) {
		this.restartCallback = restartCallback;
		this.shutdownCallback = shutdownCallback;
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
			log.info("Creating ResourceService");
		}

		resourceService = new ResourceServiceImpl();
		
		if (log.isInfoEnabled()) {
			log.info("Creating ClientService");
		}

		clientService = new ClientServiceImpl(connectionService,
				configurationService, resourceService);

		
	
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
			registry.unbind("resourceService");
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
			publishService(ResourceService.class, resourceService);
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
	public void run() {

		File logs = new File("logs");
		logs.mkdirs();

		PropertyConfigurator.configureAndWatch("conf" + File.separator
				+ "log4j.properties");
		
		System.setProperty("java.rmi.server.hostname", "localhost");
		
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		while(true) {
			try {
				buildServices();
	
				if (!publishServices()) {
					System.exit(1);
				}
	
				if (!start()) {
					System.exit(2);
				}
				
				poll();
			} catch (Exception e) {
				log.error("Failed to start", e);
				try {
					Thread.sleep(2500L);
				} catch (InterruptedException e1) {
				}
			}
		}
	}
	
	public static Main getInstance() {
		return instance;
	}
	
	public void restart() {
		restartCallback.run();
	}
	
	public void shutdown() {
		shutdownCallback.run();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		instance = new Main(new DefaultRestartCallback(), new DefaultShutdownCallback());
		instance.run();
	}
	
	public static void runApplication(Runnable restartCallback,
			Runnable shutdownCallback) throws IOException {

		new Main(restartCallback, shutdownCallback).run();

	}
	
	static class DefaultRestartCallback implements Runnable {

		@Override
		public void run() {
			
			if(log.isInfoEnabled()) {
				log.info("There is no restart mechanism available. Shutting down");
			}
			
			System.exit(0);
		}
		
	}
	
	static class DefaultShutdownCallback implements Runnable {

		@Override
		public void run() {
			
			if(log.isInfoEnabled()) {
				log.info("Shutting down using default shutdown mechanism");
			}
			
			System.exit(0);
		}
		
	}

}
