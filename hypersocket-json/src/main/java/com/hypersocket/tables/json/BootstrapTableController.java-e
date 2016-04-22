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
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Sort;

public class BootstrapTableController extends AuthenticatedController {

	Logger log = LoggerFactory.getLogger(BootstrapTableController.class);

	protected BootstrapTableResult processDataTablesRequest(
			HttpServletRequest request, BootstrapTablePageProcessor processor)
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
		
		if(request.getParameter("order")!=null) {
			
			String order = request.getParameter("order");
			String column = request.getParameter("sort");
			sorting.add(new ColumnSort(processor.getColumn(column), Sort
						.valueOf(order.toUpperCase())));
		}

		String searchPattern = "";
		String searchColumn = "";
		
		if(request.getParameter("sSearch") != null) {
			searchPattern = request.getParameter("sSearch");
		} else if(request.getParameter("search") != null) {
			searchPattern = request.getParameter("search");
		}
		
		if(request.getParameter("searchColumn") != null) {
			searchColumn = request.getParameter("searchColumn");
		}

		BootstrapTableResult result = new BootstrapTableResult(processor.getPage(
				searchColumn,
				searchPattern, 
				start,
				length, 
				sorting.toArray(new ColumnSort[0])),
				processor.getTotalCount(searchColumn, searchPattern));

		return result;
	}
}
