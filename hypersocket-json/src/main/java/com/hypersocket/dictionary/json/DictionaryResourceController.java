package com.hypersocket.dictionary.json;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.dictionary.DictionaryResourceColumns;
import com.hypersocket.dictionary.DictionaryResourceService;
import com.hypersocket.dictionary.DictionaryResourceServiceImpl;
import com.hypersocket.dictionary.Word;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.PropertyItem;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.json.ResourceUpdate;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class DictionaryResourceController extends ResourceController {
	final static Logger LOG = LoggerFactory.getLogger(DictionaryResourceController.class);

	@Autowired
	private DictionaryResourceService resourceService;

	@AuthenticationRequired
	@RequestMapping(value = "dictionary/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public BootstrapTableResult<?> tableResources(final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		return processDataTablesRequest(request, new BootstrapTablePageProcessor() {

			@Override
			public Column getColumn(String col) {
				return DictionaryResourceColumns.valueOf(col.toUpperCase());
			}

			@Override
			public List<?> getPage(String searchColumn, String searchPattern, int start, int length,
					ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException {
				return resourceService.searchResources(null, searchColumn, searchPattern,
						start, length, sorting);
			}

			@Override
			public Long getTotalCount(String searchColumn, String searchPattern)
					throws UnauthorizedException, AccessDeniedException {
				return resourceService.getResourceCount(null, searchColumn,
						searchPattern);
			}
		});
	}

	@AuthenticationRequired
	@RequestMapping(value = "dictionary/template", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getResourceTemplate(HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate());
	}

	@AuthenticationRequired
	@RequestMapping(value = "dictionary/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceList<PropertyCategory> getActionTemplate(HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, ResourceNotFoundException {
		Word resource = resourceService.getResourceById(id);
		return new ResourceList<PropertyCategory>(resourceService.getPropertyTemplate(resource));
	}

	@AuthenticationRequired
	@RequestMapping(value = "dictionary/word/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public Word getResource(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException, ResourceNotFoundException, SessionTimeoutException {

		return resourceService.getResourceById(id);

	}

	@AuthenticationRequired
	@RequestMapping(value = "dictionary/properties", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<Word> createOrUpdateResource(HttpServletRequest request, HttpServletResponse response,
			@RequestBody ResourceUpdate resource)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		try {
			Word newResource;
			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : resource.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}
			Locale locale = null;
			if(properties.containsKey("dictionary.locale")) {
				locale = Locale.forLanguageTag(properties.get("dictionary.locale"));
			}
			if (resource.getId() != null) {
				newResource = resourceService.updateResource(resourceService.getResourceById(resource.getId()),
						locale, resource.getName());
			} else {
				newResource = resourceService.createResource(locale, resource.getName());
			}
			return new ResourceStatus<Word>(newResource,
					I18N.getResource(sessionUtils.getLocale(request), DictionaryResourceServiceImpl.RESOURCE_BUNDLE,
							resource.getId() != null ? "resource.updated.info" : "resource.created.info",
							resource.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<Word>(false, e.getMessage());
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "dictionary/upload", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<Long> upload(HttpServletRequest request, HttpServletResponse response,
			@RequestPart(value = "file") MultipartFile file,
			@RequestParam String locale,
			@RequestParam(required = false) boolean ignoreDuplicates)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		try {
			String characterEncoding = request.getCharacterEncoding();
			if (StringUtils.isBlank(characterEncoding))
				characterEncoding = "UTF-8";
			try (InputStream inputStream = file.getInputStream()) {
				long rows = resourceService.importDictionary(StringUtils.isBlank(locale) ? null : Locale.forLanguageTag(locale),
						new InputStreamReader(inputStream, characterEncoding), ignoreDuplicates);
				return new ResourceStatus<Long>(rows, I18N.getResource(sessionUtils.getLocale(request),
						DictionaryResourceServiceImpl.RESOURCE_BUNDLE, "dictionary.imported.info", rows));
			}
		} catch (DataIntegrityViolationException e) {
			return new ResourceStatus<Long>(false, I18N.getResource(sessionUtils.getLocale(request),
					DictionaryResourceServiceImpl.RESOURCE_BUNDLE, "dictionary.imported.duplicateRecords"));
		} catch (ResourceException e) {
			return new ResourceStatus<Long>(false, e.getMessage());
		} catch (Throwable e) {
			LOG.error("Unexpected error", e);
			return new ResourceStatus<Long>(false, I18N.getResource(sessionUtils.getLocale(request),
					DictionaryResourceServiceImpl.RESOURCE_BUNDLE, "error.unexpectedError", e.getMessage()));
		} 
	}

	@AuthenticationRequired
	@RequestMapping(value = "dictionary/properties/{id}", method = RequestMethod.DELETE, produces = {
			"application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public ResourceStatus<Word> deleteResource(HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		try {

			Word resource = resourceService.getResourceById(id);

			if (resource == null) {
				return new ResourceStatus<Word>(false, I18N.getResource(sessionUtils.getLocale(request),
						DictionaryResourceServiceImpl.RESOURCE_BUNDLE, "error.invalidResourceId", id));
			}

			String preDeletedName = resource.getText();
			resourceService.deleteResource(resource);

			return new ResourceStatus<Word>(true, I18N.getResource(sessionUtils.getLocale(request),
					DictionaryResourceServiceImpl.RESOURCE_BUNDLE, "resource.deleted.info", preDeletedName));

		} catch (ResourceException e) {
			return new ResourceStatus<Word>(false, e.getMessage());
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "dictionary/deleteAll", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<Long> deleteAll(HttpServletRequest request, HttpServletResponse response,
			@RequestBody List<Long> ids) throws Exception {
		return callAsRequestAuthenticatedContext(request, () -> {
			try {
				resourceService.deleteResources(ids);
				return new ResourceStatus<Long>(true, I18N.getResource(sessionUtils.getLocale(request),
						DictionaryResourceServiceImpl.RESOURCE_BUNDLE, "dictionary.delete.info", ids.size()));

			} catch (ResourceException e) {
				return new ResourceStatus<Long>(false, e.getMessage());
			}
		});
	}

}
