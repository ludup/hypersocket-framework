package com.hypersocket.triggers.actions.ip;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.scheduler.SchedulerService;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.ValidationException;

@Component
public class MonitorPortTask extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(BlockIPTask.class);

	public static final String RESOURCE_BUNDLE = "MonitorPortTask";
	public static final String TASK_RESOURCE_KEY = "monitorPort";
	public static final String ATTR_IP = "monitorPort.ip";
	public static final String ATTR_PORT = "monitorPort.port";
	public static final String ATTR_TIMEOUT = "monitorPort.timeout";

	Map<String, String> blockedIPUnblockSchedules = new HashMap<String, String>();
	Set<String> blockedIps = new HashSet<String>();

	@Autowired
	MonitorPortTaskRepository repository;

	@Autowired
	HypersocketServer server;

	@Autowired
	TriggerResourceService triggerService;

	@Autowired
	I18NService i18nService;

	@Autowired
	SchedulerService schedulerService;

	@Autowired
	TaskProviderService taskService;

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);
		taskService.registerActionProvider(this);
	}

	@Override
	public String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { TASK_RESOURCE_KEY };
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {
		if (!parameters.containsKey(ATTR_IP)) {
			throw new ValidationException(I18N.getResource(Locale.getDefault(),
					RESOURCE_BUNDLE, "monitorPort.host.required"));
		}
		if (!parameters.containsKey(ATTR_PORT)) {
			throw new ValidationException(I18N.getResource(Locale.getDefault(),
					RESOURCE_BUNDLE, "monitorPort.port.required"));
		}
		if (!parameters.containsKey(ATTR_TIMEOUT)) {
			throw new ValidationException(I18N.getResource(Locale.getDefault(),
					RESOURCE_BUNDLE, "monitorPort.timout.required"));
		}

	}

	@Override
	public TaskResult execute(Task task, SystemEvent event)
			throws ValidationException {
		String ipAddress = repository.getValue(task, ATTR_IP);
		int port = Integer.valueOf(repository.getValue(task, ATTR_PORT));
		int timeout = Integer.valueOf(repository.getValue(task, ATTR_TIMEOUT));
		try {
			InetAddress address = InetAddress.getByName(ipAddress);
			InetSocketAddress bindAddr = new InetSocketAddress(address, port);
			Socket sck = new Socket();
			sck.connect(bindAddr, timeout * 60 * 1000);
			sck.close();
			return new MonitorPortResult(this, true, event.getCurrentRealm(),
					task, ipAddress, port, timeout);
		} catch (Exception e) {
			log.error("Failed to monitor connection to  host:" + ipAddress
					+ " port :" + port, e);
			return new MonitorPortResult(this, e, event.getCurrentRealm(),
					task, ipAddress, port, timeout);
		}
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
