package com.hypersocket.dashboard.message;

import java.util.List;

import com.hypersocket.resource.AbstractResourceRepository;

public interface DashboardMessageRepository extends
		AbstractResourceRepository<DashboardMessage> {

	void saveNewMessages(List<DashboardMessage> dashboardMessageList);

	List<DashboardMessage> getMessages();

	List<DashboardMessage> getUnexpiredMessages(int pageNum);

	DashboardMessage getMessage(DashboardMessage dashboardMessage);

	void saveNewMessage(DashboardMessage dashboardMessage);

	Long getMessageCount();

}
