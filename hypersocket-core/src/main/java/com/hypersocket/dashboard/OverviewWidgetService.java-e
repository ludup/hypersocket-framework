package com.hypersocket.dashboard;

import java.util.Collection;
import java.util.List;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface OverviewWidgetService {

	public void registerWidget(OverviewWidget widget);

	public List<OverviewWidget> getWidgets() throws AccessDeniedException;

	public Collection<Link> getLinks() throws ResourceException;
	
	public Collection<Link> getVideos() throws ResourceException;
	
	public Collection<Link> getDocumentation() throws ResourceException;
}
