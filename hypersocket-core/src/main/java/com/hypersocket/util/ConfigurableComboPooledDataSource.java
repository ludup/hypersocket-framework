package com.hypersocket.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.mchange.v2.c3p0.AbstractComboPooledDataSource;

public class ConfigurableComboPooledDataSource extends AbstractComboPooledDataSource {

	public ConfigurableComboPooledDataSource() {
		super();
	}

	public ConfigurableComboPooledDataSource(boolean autoregister) {
		super(autoregister);
	}

	public ConfigurableComboPooledDataSource(String configName) {
		super(configName);
	}

	// serialization stuff -- set up bound/constrained property event handlers on
	// deserialization
	private static final long serialVersionUID = 1;
	private static final short VERSION = 0x0002;

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeShort(VERSION);
		setNumHelperThreads(1);
	}
	
	@Override
	public synchronized void setNumHelperThreads( int numHelperThreads )
	{
		if(numHelperThreads == 0)
			if(Boolean.getBoolean("hypersocket.development"))
				super.setNumHelperThreads(5);
			else
				super.setNumHelperThreads(Runtime.getRuntime().availableProcessors() * 5);
		else {
			super.setNumHelperThreads(numHelperThreads);
		}
	}

	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
		short version = ois.readShort();
		switch (version) {
		case VERSION:
			// ok
			break;
		default:
			throw new IOException("Unsupported Serialized Version: " + version);
		}
	}

}
