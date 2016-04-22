package com.hypersocket.dashboard.message;

import java.util.List;

public interface DashboardMessageService {

	List<DashboardMessage> getMessages();

	List<DashboardMessage> getUnexpiredMessages(int pageNum);

	void saveNewMessages(DashboardMessage[] dashboardMessageList);

	DashboardMessage saveNewMessage(DashboardMessage dashboardMessage);

	Long getMessageCount();

}
