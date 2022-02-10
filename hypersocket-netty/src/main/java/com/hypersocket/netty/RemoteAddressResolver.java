package com.hypersocket.netty;

import java.net.InetSocketAddress;

public final class RemoteAddressResolver {

	final private boolean resolveHostName;
	final private InetSocketAddress inetSocketAddress;
	
	public RemoteAddressResolver(String ipString, int port, boolean resolveHostName) {
		
		this.resolveHostName = resolveHostName;
		
		if (this.resolveHostName) {
			this.inetSocketAddress = new InetSocketAddress(ipString, port);
		} else {
			this.inetSocketAddress = InetSocketAddress.createUnresolved(ipString, port);
		}
	}
	
	public String hostName() {
		return inetSocketAddress.getHostName();
	}
	
	public String remoteAddress() {
		return inetSocketAddress.getAddress().getHostAddress();
	}

	public boolean isResolveHostName() {
		return resolveHostName;
	}

	public InetSocketAddress getInetSocketAddress() {
		return inetSocketAddress;
	}
	
}
