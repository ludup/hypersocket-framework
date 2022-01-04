package com.hypersocket.dashboard;

import java.util.Collection;
import java.util.List;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface OverviewWidgetService {

	void registerWidget(String resourceKey, OverviewWidget widget);

	List<OverviewWidget> getWidgets(String resourceKey) throws AccessDeniedException;

	List<OverviewWidget> getAllWidgets(String resourceKey) throws AccessDeniedException;

	Collection<Link> getLinks() throws ResourceException;
	
	Collection<Link> getVideos() throws ResourceException;
	
	Collection<Link> getDocumentation() throws ResourceException;

	Collection<Link> getFirstSteps() throws ResourceException;

	boolean hasActiveWidgets(String resourceKey);
}
