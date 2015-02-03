package com.hypersocket.tables.json;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.tables.BootstrapTablesResult;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;

public class BootstrapTablesController extends AuthenticatedController {

	Logger log = LoggerFactory.getLogger(BootstrapTablesController.class);

	protected BootstrapTablesResult processBootstrapTablesRequest(
			HttpServletRequest request, BootstrapTablesPageProcessor processor)
			throws NumberFormatException, UnauthorizedException,
			AccessDeniedException {

		Integer start = (request.getParameter("offset") == null ? 0 : Integer
				.parseInt(request.getParameter("offset")));
		Integer length = (request.getParameter("limit") == null ? Integer.MAX_VALUE
				: Integer.parseInt(request.getParameter("limit")));

		List<ColumnSort> sorting = new ArrayList<ColumnSort>();
		// int count = (request.getParameter("iSortingCols") == null ? 0 :
		// Integer
		// .parseInt(request.getParameter("iSortingCols")));
		// for (int i = 0; i < count; i++) {
		if (request.getParameter("sort") != null) {
			int col = Integer.parseInt(request.getParameter("sort"));
			String sor = request.getParameter("order");
			sorting.add(new ColumnSort(processor.getColumn(col), Sort
					.valueOf(sor.toUpperCase())));
		}

		String searchPattern = request.getParameter("search") == null ? ""
				: request.getParameter("search");
		if (searchPattern.indexOf('*') > -1) {
			searchPattern = searchPattern.replace('*', '%');
		}

		if (searchPattern.indexOf('%') == -1) {
			searchPattern += "%";
		}

		return new BootstrapTablesResult(processor.getPage(searchPattern,
				start, length, sorting.toArray(new ColumnSort[0])),
				processor.getTotalCount(searchPattern));
	}
}
