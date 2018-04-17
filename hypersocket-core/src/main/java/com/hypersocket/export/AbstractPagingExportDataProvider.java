package com.hypersocket.export;

import java.util.List;
import java.util.Map;

import com.hypersocket.permissions.AccessDeniedException;

public abstract class AbstractPagingExportDataProvider<T> implements ExportDataProvider {

	private int index;
	private Map<String, String> next;
	private List<T> page;
	private int pageNo;
	private boolean eof;
	private int pageSize;

	protected AbstractPagingExportDataProvider(int pageSize) {
		this.pageSize = pageSize;
	}

	@Override
	public Map<String, String> next() {
		if (eof)
			throw new IllegalStateException("Already passed EOF.");
		checkNext();
		try {
			if (next == null)
				eof = true;
			return next;
		} finally {
			next = null;
		}
	}

	@Override
	public boolean hasNext() {
		if (eof)
			return false;
		checkNext();
		return next != null && !next.isEmpty();
	}

	protected abstract List<T> fetchPage(int startPosition) throws AccessDeniedException;

	protected abstract Map<String, String> convertToMap(T entity);

	void checkNext() {
		while (true) {
			if (next == null) {
				if (page == null) {
					/* New page needed */
					try {
						page = fetchPage(pageNo * pageSize);
					} catch (AccessDeniedException e) {
						throw new SecurityException("Failed to fetch page.", e);
					}
					if (page.isEmpty())
						/* EOF */
						break;
					index = 0;
				}
				if (index < page.size()) {
					/* Ok, use this item */
					next = convertToMap(page.get(index));
					index++;
					break;
				} else {
					/* End of page, reset and get next page */
					pageNo++;
					page = null;
				}
			} else
				break;
		}
	}

}
