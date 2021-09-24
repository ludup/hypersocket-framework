package com.hypersocket.service;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractIPManageableService implements IPManageableService {

	private List<Listener> listeners = new ArrayList<>();

	@Override
	public void addListener(Listener listener) {
		listeners.add(listener);		
	}

	@Override
	public void removeListener(Listener listener) {
		listeners.remove(listener);				
	}

	@Override
	public final void stopService() {
		try {
			onStopService();
		}
		finally {
			firePortsChanged();
		}
	}

	protected void firePortsChanged() {
		for(int i = listeners.size() - 1 ; i >= 0 ; i--)
			listeners.get(i).portsChanged();
	}

	@Override
	public final boolean startService() {
		try {
			return onStartService();
		}
		finally {
			firePortsChanged();
		}
	}

	protected abstract void onStopService();

	protected abstract boolean onStartService();

}
