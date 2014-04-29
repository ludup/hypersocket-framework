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
			throws NumberFormatException, UnauthorizedException, AccessDeniedException {
		
		Integer start = Integer.parseInt(request.getParameter("iDisplayStart"));
		Integer length = Integer.parseInt(request
				.getParameter("iDisplayLength"));

		List<ColumnSort> sorting = new ArrayList<ColumnSort>();
		int count = Integer.parseInt(request.getParameter("iSortingCols"));
		for (int i = 0; i < count; i++) {
			int col = Integer.parseInt(request.getParameter("iSortCol_" + i));
			String sor = request.getParameter("sSortDir_" + i);
			sorting.add(new ColumnSort(processor.getColumn(col), Sort
					.valueOf(sor.toUpperCase())));
		}

		String searchPattern = request.getParameter("sSearch");
		if(searchPattern.indexOf('*') >-1) {
			searchPattern = searchPattern.replace('*', '%');
		}
		
		if(searchPattern.indexOf('%')==-1) {
			searchPattern += "%";
		}
		
		return new DataTablesResult(processor.getPage(searchPattern, start, length,
				sorting.toArray(new ColumnSort[0])),
				processor.getTotalCount(searchPattern), Integer.parseInt(request
						.getParameter("sEcho")));
	}
}
