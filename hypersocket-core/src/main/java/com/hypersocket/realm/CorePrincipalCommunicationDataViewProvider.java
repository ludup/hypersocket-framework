package com.hypersocket.realm;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.CommunicationDataView.CoreCommunicationDataView;

@Service
@Qualifier(CorePrincipalCommunicationDataViewProvider.CORE_PRINCIPAL_COMMUNICATION_DATA_VIEW_PROVIDER)
public class CorePrincipalCommunicationDataViewProvider implements PrincipalCommunicationDataViewProvider {
	
	private static final String TAG = "tag";

	private static final String RESOURCE_KEY = "resourceKey";

	public static final String CORE_PRINCIPAL_COMMUNICATION_DATA_VIEW_PROVIDER 
		= "CorePrincipalCommunicationDataViewProvider";

	@Autowired
	private RealmService realmService;
	
	@Autowired
	private AuthenticatedService authenticatedService;
	
	@Autowired
	private I18NService i18nService;
	
	@Override
	public Set<? extends CommunicationDataView> getPrincipalCommunicationDataView(Realm realm, Long id) 
			throws AccessDeniedException {
		
		var principal = realmService.getPrincipalById(id);
		
		if (principal == null) {
			return Collections.emptySet();
		}
		
		var properties = realmService.getUserPropertyTemplates(principal);
		
		var dataViews = new LinkedHashSet<CommunicationDataView>();
		
		var templates = properties.stream()
							.flatMap(prop -> prop.getTemplates().stream())
							.collect(Collectors.toSet());
		
		
		var view = new CoreCommunicationDataView(CommunicationDataView.COMMON_TYPE_EMAIL, 
				new String [] { principal.getPrimaryEmail() }, 
				Map.of(
						RESOURCE_KEY, "email",
						TAG, i18nService.getResource("communication.data.view.tag.email.primary", 
								authenticatedService.getCurrentLocale())
					)
				);
		
		dataViews.add(view);
		
		templates.forEach(template -> {
			
			String key = template.getResourceKey();
			String[] value = ResourceUtils.explodeValues(template.getValue());
			
			CommunicationDataView communicationDataView = null;
			
			switch (key) {
				
				case "mobile":
					communicationDataView = new CoreCommunicationDataView(CommunicationDataView.COMMON_TYPE_MOBILE, 
							value, 
							Map.of(
									RESOURCE_KEY, key,
									TAG, i18nService.getResource("communication.data.view.tag.mobile.primary", 
											authenticatedService.getCurrentLocale())
								)
							);
					
					dataViews.add(communicationDataView);
					break;
				
				case "user.email":
					communicationDataView = new CoreCommunicationDataView(CommunicationDataView.COMMON_TYPE_EMAIL, 
							value, 
							Map.of(
									RESOURCE_KEY, key,
									TAG, i18nService.getResource("communication.data.view.tag.email.secondary", 
											authenticatedService.getCurrentLocale())
								)
							);
					
					dataViews.add(communicationDataView);
					break;
				
				case "user.mobile":
					communicationDataView = new CoreCommunicationDataView(CommunicationDataView.COMMON_TYPE_MOBILE, 
							value, 
							Map.of(
									RESOURCE_KEY, key,
									TAG, i18nService.getResource("communication.data.view.tag.mobile.secondary", 
											authenticatedService.getCurrentLocale())
								)
							);
					
					dataViews.add(communicationDataView);
					break;
				
				case "secondaryMobile":
					communicationDataView = new CoreCommunicationDataView(CommunicationDataView.COMMON_TYPE_MOBILE, 
							value, 
							Map.of(
									RESOURCE_KEY, key,
									TAG, i18nService.getResource("communication.data.view.tag.additional.mobile", 
											authenticatedService.getCurrentLocale())
								)
							);
					
					dataViews.add(communicationDataView);
					break;
				
				case "secondaryEmail":
					communicationDataView = new CoreCommunicationDataView(CommunicationDataView.COMMON_TYPE_EMAIL, 
							value, 
							Map.of(
									RESOURCE_KEY, key,
									TAG, i18nService.getResource("communication.data.view.tag.additional.email", 
											authenticatedService.getCurrentLocale())
								)
							);
					
					dataViews.add(communicationDataView);
					break;
	
				default:
					break;
			}
		});
		
		return dataViews;
		
	}

}
