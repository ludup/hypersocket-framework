package com.hypersocket.client.rmi;

import java.io.Serializable;

public class ConnectionStatusImpl implements ConnectionStatus, Serializable {

	private static final long serialVersionUID = 296406363321007200L;

	int status;
	Connection connection;
	
	public ConnectionStatusImpl(Connection connection, int status) {
		this.connection = connection;
		this.status = status;
	}
	
	@Override
	public Connection getConnection() {
		return connection;
	}

	@Override
	public int getStatus() {
		return status;
	}

}
