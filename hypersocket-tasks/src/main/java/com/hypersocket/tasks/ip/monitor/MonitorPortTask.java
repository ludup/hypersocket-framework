package com.hypersocket.tasks.ip.monitor;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.AbstractTaskResult;
import com.hypersocket.triggers.ValidationException;

@Component
public class MonitorPortTask extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(MonitorPortTask.class);

	public static final String RESOURCE_BUNDLE = "MonitorPortTask";
	public static final String TASK_RESOURCE_KEY = "monitorPort";
	public static final String ATTR_IP = "monitorPort.ip";
	public static final String ATTR_PORT = "monitorPort.port";
	public static final String ATTR_TIMEOUT = "monitorPort.timeout";

	@Autowired
	MonitorPortTaskRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	EventService eventService;

	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
		taskService.registerTaskProvider(this);
		eventService.registerEvent(MonitorPortResult.class, RESOURCE_BUNDLE);
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
					RESOURCE_BUNDLE, "monitorPort.timeout.required"));
		}

	}

	@Override
	public AbstractTaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {
		String ipAddress = processTokenReplacements(repository.getValue(task, ATTR_IP), event);
		int port = Integer.valueOf(repository.getValue(task, ATTR_PORT));
		int timeout = Integer.valueOf(repository.getValue(task, ATTR_TIMEOUT));
		try {
			InetAddress address = InetAddress.getByName(ipAddress);
			InetSocketAddress bindAddr = new InetSocketAddress(address, port);
			Socket sck = new Socket();
			sck.connect(bindAddr, timeout * 1000);
			sck.close();
			return new MonitorPortResult(this, true, currentRealm,
					task, ipAddress, port, timeout);
		} catch (Exception e) {
			log.error("Failed to monitor connection to  host:" + ipAddress
					+ " port :" + port, e);
			return new MonitorPortResult(this, e, currentRealm,
					task, ipAddress, port, timeout);
		}
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { MonitorPortResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
