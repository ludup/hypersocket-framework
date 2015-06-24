package com.hypersocket.client.rmi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="connections")
public class ConnectionImpl implements Connection, Serializable {

	private static final long serialVersionUID = 7020419491848339718L;

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	Long id;
	
	@Column(nullable=false)
	String hostname;
	
	@Column(nullable=false)
	Integer port = new Integer(443);
	
	@Column(nullable=false)
	String path = "/hypersocket";
	
	@Column(nullable=true)
	String realm;
	
	@Column(nullable=true)
	String username;
	
	@Column(nullable=true)
	String hashedPassword;
	
	@Column
	boolean stayConnected;
	
	@Column
	boolean connectAtStartup;
	
	// Not peristed
	private String serverVersion;
	private UpdateState updateState;
	private String serial;
	
	
	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getHostname() {
		return hostname;
	}
	
	@Override
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	
	public String getRealm() {
		return realm;
	}
	
	public void setRealm(String realm) {
		this.realm = realm;
	}
	
	@Override
	public int getPort() {
		return port;
	}	

	@Override
	public String getPath() {
		return path;
	}
	
	@Override
	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public String getUsername() {
		return username;
	}
	
	@Override
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Override
	public String getHashedPassword() {
		return hashedPassword;
	}
	
	@Override
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

	@Override
	public boolean isStayConnected() {
		return stayConnected;
	}

	@Override
	public void setStayConnected(boolean stayConnected) {
		this.stayConnected = stayConnected;
	}

	@Override
	public boolean isConnectAtStartup() {
		return connectAtStartup;
	}

	@Override
	public void setConnectAtStartup(boolean connectAtStartup) {
		this.connectAtStartup = connectAtStartup;
	}

	@Override
	public void setPort(Integer port) {
		this.port = port;
	}	
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.defaultReadObject();
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		out.defaultWriteObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((hostname == null) ? 0 : hostname.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		result = prime * result + ((port == null) ? 0 : port.hashCode());
		result = prime * result + ((realm == null) ? 0 : realm.hashCode());
		result = prime * result
				+ ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConnectionImpl other = (ConnectionImpl) obj;
		if (hostname == null) {
			if (other.hostname != null)
				return false;
		} else if (!hostname.equals(other.hostname))
			return false;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		if (port == null) {
			if (other.port != null)
				return false;
		} else if (!port.equals(other.port))
			return false;
		if (realm == null) {
			if (other.realm != null)
				return false;
		} else if (!realm.equals(other.realm))
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}

	@Override
	public void setUpdateState(UpdateState updateState) {
		this.updateState = updateState;
	}

	@Override
	public UpdateState getUpdateState() {
		return updateState;
	}

	@Override
	public String getServerVersion() {
		return serverVersion;
	}

	@Override
	public String getSerial() {
		return serial;
	}

	@Override
	public void setSerial(String serial) {
		this.serial = serial;
	}

	
}
