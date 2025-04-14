package com.hypersocket.messagedelivery;

import java.util.ArrayList;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MessageDeliveryPreSendCheckConfiguration {
	
	private static Logger log = LoggerFactory
			.getLogger(MessageDeliveryPreSendCheckConfiguration.class);
	
	/**
	 * Wanted tight control on which bean gets registered for Spring hence used
	 * Java's SPI to find the instance.
	 * 
	 * It asks Java SPI to find the beans if more than 1 found throws exception,
	 * in case none found registers a default implementation making operation idempotent.
	 * 
	 * @return instance of MessageDeliveryPreSendCheck for Spring
	 */
	@Bean
    public MessageDeliveryPreSendCheck getMessageDeliveryPreSendCheck() {
		
		var loader = ServiceLoader.load(MessageDeliveryPreSendCheck.class);

        var plugins = new ArrayList<>();
        loader.iterator().forEachRemaining(plugins::add);

        if (plugins.size() > 1) {
            throw new IllegalStateException("Multiple MessageDeliveryPreSendCheck implementations found: " +
                plugins.stream().map(p -> p.getClass().getName()).collect(Collectors.toList()));
        }
        
        MessageDeliveryPreSendCheck instance = null;
        											
        if (plugins.isEmpty()) {
        	instance = realm -> true;
        	log.info("Registered default implementation for MessageDeliveryPreSendCheck");
        } else {
        	instance = (MessageDeliveryPreSendCheck) plugins.get(0);
        	log.info("Registered '{}' implementation for MessageDeliveryPreSendCheck.", instance.getClass().getSimpleName());
        }
        
        return instance;
        
    }
}
