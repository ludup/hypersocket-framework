package com.hypersocket.service;

import java.util.Objects;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(as = IPManageableService.class)
public interface IPManageableService extends ManageableService {

	public enum Proto {
		TCP, UDP
	}

	public class Port {

		private Proto proto;
		private int number;
		
		public Port(Proto proto, int number) {
			this.proto = proto;
			this.number = number;
		}

		public Proto getProto() {
			return proto;
		}

		public void setProto(Proto proto) {
			this.proto = proto;
		}

		public int getNumber() {
			return number;
		}

		public void setNumber(int number) {
			this.number = number;
		}

		@Override
		public int hashCode() {
			return Objects.hash(number, proto);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Port other = (Port) obj;
			return number == other.number && proto == other.proto;
		}
	}
	
	public interface Listener {
		void portsChanged();
	}
	
	default boolean isRunning() {
		for(ServiceStatus s : getStatus()) {
			if(s.isRunning())
				return true;
		}
		return false;
	}

	default void restartService() {
		boolean run  = isRunning();
		stopService();
		if(run)
			startService();
		
	}
	
	Port[] getPorts();
	
	void addListener(Listener listener);
	
	void removeListener(Listener listener);

}
