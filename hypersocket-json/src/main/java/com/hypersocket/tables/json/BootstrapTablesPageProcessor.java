package com.hypersocket.tables.json;

import java.util.Collection;

import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;

public interface BootstrapTablesPageProcessor {

	Column getColumn(int col);

	Collection<?> getPage(String searchPattern, int start, int length,
			ColumnSort[] sorting) throws UnauthorizedException,
			AccessDeniedException;

	Long getTotalCount(String searchPattern) throws UnauthorizedException,
			AccessDeniedException;

}
