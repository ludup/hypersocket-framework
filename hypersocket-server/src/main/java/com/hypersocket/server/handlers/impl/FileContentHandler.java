/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.server.handlers.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class FileContentHandler extends ContentHandlerImpl {

	static Logger log = LoggerFactory.getLogger(FileContentHandler.class);
	
	List<File> baseDirs = new ArrayList<File>();
	
	public FileContentHandler(String name, int priority, File baseDir) {
		super(name, priority);
		addBaseDir(baseDir);
	}

	public FileContentHandler(String name, int priority) {
		super(name, priority);
	}
	
	public String getResourceName() {
		return "file";
	}
	
	public void addBaseDir(File baseDir) {
		baseDirs.add(baseDir);
	}

	@Override
	public InputStream getResourceStream(String path)
			throws FileNotFoundException {
		return new FileInputStream(resolveFile(path));
	}
	
	protected File resolveFile(String path) throws FileNotFoundException {
		for(File p : baseDirs) {
			File f = new File(p, path);
			if(log.isDebugEnabled()) {
				log.debug("Attempting to resolve " + f.getAbsolutePath());
			}
			if(f.exists()) {
				if(log.isDebugEnabled()) {
					log.debug("Resolved "+  f.getAbsolutePath());
				}
				return f;
			}
		}
		throw new FileNotFoundException("Unable to resolve path " + path);
	}

	@Override
	public long getResourceLength(String path) throws FileNotFoundException {
		return resolveFile(path).length();
	}

	@Override
	public long getLastModified(String path) throws FileNotFoundException {
		return resolveFile(path).lastModified();
	}

	@Override
	public int getResourceStatus(String path) {

		try {
			File f = resolveFile(path);
			if (f.isHidden() || !f.exists()) {
				return HttpStatus.SC_NOT_FOUND;
			}
			if (!f.isFile()) {
				return HttpStatus.SC_FORBIDDEN;
			}
			return HttpStatus.SC_OK;
		} catch (FileNotFoundException e) {
			return HttpStatus.SC_NOT_FOUND;
		}
	}

}
