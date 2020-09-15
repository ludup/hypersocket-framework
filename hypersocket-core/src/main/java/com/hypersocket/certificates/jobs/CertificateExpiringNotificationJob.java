package com.hypersocket.certificates.jobs;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.commons.lang3.time.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.certificates.CertificateResourceRepository;
import com.hypersocket.certificates.CertificateResourceService;
import com.hypersocket.certificates.CertificateResourceServiceImpl;
import com.hypersocket.message.MessageResourceService;
import com.hypersocket.scheduler.PermissionsAwareJob;
import com.hypersocket.utils.HypersocketUtils;

public class CertificateExpiringNotificationJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(CertificateExpiringNotificationJob.class);

	@Autowired
	private MessageResourceService messageService; 
	
	@Autowired
	private CertificateExpiringMessageRepository messageRepository;
	
	@Autowired
	private CertificateResourceRepository repository;
	
	@Autowired
	private CertificateResourceService resourceService; 
	
	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		for(CertificateResource resource : repository.getResources(getCurrentRealm())) {
			
			try {
				X509Certificate x509 = resourceService.getX509Certificate(resource);
				
				for(Integer days : messageRepository.getIntValues(
						messageService.getMessageById(CertificateResourceServiceImpl.MESSAGE_CERTIFICATE_EXPIRING, resource.getRealm()),
						"certificate.reminder.days")) {
					Date startDate = DateUtils.addDays(HypersocketUtils.today(), days);
					Date endDate = DateUtils.addDays(startDate, 1);
					if(log.isDebugEnabled()) {
						log.debug(String.format("Processing certificates that expire in %d days between %s and %s", days, 
								HypersocketUtils.formatDateTime(startDate), HypersocketUtils.formatDateTime(endDate)));
					}
					
					if(x509.getNotAfter().after(startDate) && x509.getNotAfter().before(endDate)) {
						resourceService.sendExpiringNotification(resource, x509);
						break;
					} else if(x509.getNotAfter().before(new Date())) {
						resourceService.sendExpiringNotification(resource, x509);
						break;
					}
					
					
				}
			} catch (CertificateException e) {
				log.error("Could not parse X509 certificate", e);
			}
		}
	}
}
