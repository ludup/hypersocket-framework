package com.hypersocket.dashboard;

import java.util.List;

import com.hypersocket.permissions.AccessDeniedException;

public interface OverviewWidgetService {

	public void registerWidget(OverviewWidget widget);

	public List<OverviewWidget> getWidgets() throws AccessDeniedException;
}
