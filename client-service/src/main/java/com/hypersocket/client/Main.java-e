package com.hypersocket.client;

import java.beans.Introspector;
import java.io.File;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.quartz.SchedulerException;
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

	Registry registry;

	Main() {

	}

	void buildServices() throws RemoteException, SchedulerException {

		registry = LocateRegistry.createRegistry(50000);

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

		Remote stub = UnicastRemoteObject.exportObject(obj, 50000);
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
		BasicConfigurator.configure();
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new RMISecurityManager());
		}

		Main main = new Main();

		try {
			main.buildServices();

			if (!main.publishServices()) {
				System.exit(1);
			}

			if (!main.start()) {
				System.exit(2);
			}
		} catch (Exception e) {
			log.error("Failed to start", e);
		}

	}

}
