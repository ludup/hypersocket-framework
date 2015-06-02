package com.hypersocket.tables.json;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.DataTablesResult;
import com.hypersocket.tables.Sort;

public class DataTablesController extends AuthenticatedController {

	Logger log = LoggerFactory.getLogger(DataTablesController.class);

	protected DataTablesResult processDataTablesRequest(
			HttpServletRequest request, DataTablesPageProcessor processor)
			throws NumberFormatException, UnauthorizedException,
			AccessDeniedException {

		Integer start = 0;
		if(request.getParameter("iDisplayStart") != null) {
				start = Integer.parseInt(request.getParameter("iDisplayStart"));
		} else if(request.getParameter("offset") != null) {
			start = Integer.parseInt(request.getParameter("offset"));
		}
		
		Integer length = 0;
		if(request.getParameter("iDisplayLength") != null) {
			length = Integer.parseInt(request.getParameter("iDisplayLength"));
		} else if(request.getParameter("limit") != null) {
			length = Integer.parseInt(request.getParameter("limit"));
		}
		
		List<ColumnSort> sorting = new ArrayList<ColumnSort>();
		int count = (request.getParameter("iSortingCols") == null ? 0 : Integer
				.parseInt(request.getParameter("iSortingCols")));
		for (int i = 0; i < count; i++) {
			int col = Integer.parseInt(request.getParameter("iSortCol_" + i));
			String sor = request.getParameter("sSortDir_" + i);
			sorting.add(new ColumnSort(processor.getColumn(col), Sort
					.valueOf(sor.toUpperCase())));
		}

		String searchPattern = "";
		
		if(request.getParameter("sSearch") != null) {
			searchPattern = request.getParameter("sSearch");
		} else if(request.getParameter("search") != null) {
			searchPattern = request.getParameter("search");
		}
		
		if (searchPattern.indexOf('*') > -1) {
			searchPattern = searchPattern.replace('*', '%');
		}

		if (searchPattern.indexOf('%') == -1) {
			searchPattern += "%";
		}

		return new DataTablesResult(processor.getPage(searchPattern, start,
				length, sorting.toArray(new ColumnSort[0])),
				processor.getTotalCount(searchPattern),
				(request.getParameter("sEcho") == null ? 0 : Integer
						.parseInt(request.getParameter("sEcho"))));
	}
}
