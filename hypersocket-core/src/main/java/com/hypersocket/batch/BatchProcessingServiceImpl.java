package com.hypersocket.batch;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.resource.ResourceException;

public abstract class BatchProcessingServiceImpl implements BatchProcessingService {

	static Logger log = LoggerFactory.getLogger(BatchProcessingServiceImpl.class);
	
	protected abstract BatchProcessingItemRepository getRepository();
	
	@SuppressWarnings("unchecked")
	public void processBatchItems(BatchProcessor processor) {
		
		Collection<BatchItem> items = getRepository().allResources();
			
		if(items.isEmpty()) {
			if(log.isInfoEnabled()) {
				log.info(String.format("There are no outstanding key synchronization jobs"));
			}
			return;
		}
			
		if(log.isInfoEnabled()) {
			log.info(String.format("Processing %d batch items", items.size()));
		}
			
		for(BatchItem item : items) {
				
			try {
				processor.process(item, getRepository().getProperties(item, true));			
			} catch(Throwable t) {
				log.error("Failed to process batch item", t);
			} finally {
				try {
					getRepository().deleteResource(item);
				} catch (ResourceException e) {
					log.error("Failed to delete batch item resource", e);
				}
			}
		}
		
	}
}
