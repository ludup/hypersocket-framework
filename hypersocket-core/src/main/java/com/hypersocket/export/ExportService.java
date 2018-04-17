package com.hypersocket.export;

import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;

public interface ExportService {
	static Logger log = LoggerFactory.getLogger(ExportService.class);

	void downloadCSV(Realm realm, ExportDataProvider provider, boolean outputHeaders, String delimiter,
			CommonEndOfLineEnum terminate, String wrap, String escape, String attributes, 
			OutputStream out, Locale locale) throws AccessDeniedException, UnsupportedEncodingException;
}
