package com.hypersocket.dashboard.message;

import java.util.List;

import com.hypersocket.permissions.AccessDeniedException;

public interface DashboardMessageService {

	List<DashboardMessage> getMessages() throws AccessDeniedException;

	List<DashboardMessage> getUnexpiredMessages(int pageNum)
			throws AccessDeniedException;

	void saveNewMessages(DashboardMessage[] dashboardMessageList)
			throws AccessDeniedException;

	DashboardMessage saveNewMessage(DashboardMessage dashboardMessage)
			throws AccessDeniedException;

	Long getMessageCount() throws AccessDeniedException;

}
