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
	public boolean equals(Object obj) {
		if(obj instanceof ConnectionImpl) {
			ConnectionImpl c2 = (ConnectionImpl) obj;
			return c2.getId().equals(id);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
	
	
}
