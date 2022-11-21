package com.hypersocket.realm;

import java.util.Set;

public interface PrincipalCommunicationDataView {

	Principal getPrincipal();
	
	Set<CommunicationDataView> getCommunicationDataViews();
	
	public class CorePrincipalCommunicationDataView implements PrincipalCommunicationDataView {
		
		private final Principal principal;
		
		private final Set<CommunicationDataView> communicationDataViews;
		
		public CorePrincipalCommunicationDataView(Principal principal, Set<CommunicationDataView> communicationDataViews) {
			this.principal = principal;
			this.communicationDataViews = communicationDataViews;
		}

		@Override
		public Principal getPrincipal() {
			return principal;
		}

		@Override
		public Set<CommunicationDataView> getCommunicationDataViews() {
			return communicationDataViews;
		}
		
	}
}
