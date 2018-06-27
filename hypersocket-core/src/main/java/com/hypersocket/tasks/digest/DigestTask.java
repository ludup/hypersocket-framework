package com.hypersocket.tasks.digest;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.upload.FileUploadService;

@Component
public class DigestTask extends AbstractTaskProvider {

	public static final String TASK_RESOURCE_KEY = "digestTask";

	public static final String RESOURCE_BUNDLE = "DigestTask";
	
	@Autowired
	DigestTaskRepository repository;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	EventService eventService;

	@Autowired
	I18NService i18nService; 

	@Autowired
	FileUploadService uploadService; 
	
	public DigestTask() {
	}
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(DigestTaskResult.class,
				RESOURCE_BUNDLE);
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

	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {

		String source = repository.getValue(task, "digest.source");
		String fileUpload = repository.getValue(task, "digest.sourceUpload");	
		String filename = processTokenReplacements(repository.getValue(task, "digest.sourceFile"), event);
		String sourceText = processTokenReplacements(repository.getValue(task, "digest.sourceText"), event);
		String type = repository.getValue(task, "digest.type");
		
		InputStream in = null;
		
		try {
			MessageDigest  digest = MessageDigest.getInstance(type);
			
			
    		if("UPLOAD".equals(source)) {
    			in = uploadService.getInputStream(fileUpload);
    		} else if("TEXT".equals(source)) {
    			in = IOUtils.toInputStream(sourceText);
    		} else {
    			in = new FileInputStream(filename);
    		}
			
			byte[] tmp = new byte[4096];
			int r;
			while((r = in.read(tmp)) > -1) {
				digest.update(tmp, 0, r);
			}
			
			byte[] hash = digest.digest();
			
	
			// Task is performed here
			return new DigestTaskResult(this, true, currentRealm, task, Hex.encodeHexString(hash));
			
		
		} catch(Throwable e) { 
			return new DigestTaskResult(this, e, currentRealm, task);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}
	
	public String[] getResultResourceKeys() {
		return new String[] { DigestTaskResult.EVENT_RESOURCE_KEY };
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
	
	@Override
	public boolean isSystem() {
		return true;
	}

}
