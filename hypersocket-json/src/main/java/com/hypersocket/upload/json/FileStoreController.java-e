package com.hypersocket.upload.json;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NServiceImpl;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceExportException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import com.hypersocket.upload.FileUploadServiceImpl;
import com.hypersocket.utils.HypersocketUtils;

@Controller
public class FileStoreController extends ResourceController {

	static Logger log = LoggerFactory.getLogger(FileStoreController.class);
	
	@Autowired
	FileUploadService resourceService;

	@AuthenticationRequired
	@RequestMapping(value = "files/file/{uuid}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<FileUpload> getFile(final HttpServletRequest request,
			HttpServletResponse response, @PathVariable String uuid)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return new ResourceStatus<FileUpload>(resourceService.getFileByUuid(uuid));

		} catch(ResourceException ex) { 
			return new ResourceStatus<FileUpload>(false, ex.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "files/file", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<FileUpload> createFile(HttpServletRequest request,
			HttpServletResponse response,
			@RequestPart(value = "file") MultipartFile file)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			FileUpload fileUpload;

			Realm realm = sessionUtils.getCurrentRealm(request);
			fileUpload = resourceService.createFile(file, realm, "upload");

			return new ResourceStatus<FileUpload>(fileUpload, I18N.getResource(
					sessionUtils.getLocale(request),
					FileUploadServiceImpl.RESOURCE_BUNDLE,
					"fileUpload.uploaded.info", fileUpload.getName()));

		} catch (ResourceCreationException e) {
			log.error("File upload failed", e);
			return new ResourceStatus<FileUpload>(false, e.getMessage());
		} catch (Throwable e) {
			log.error("File upload failed", e);
			return new ResourceStatus<FileUpload>(false, I18N.getResource(
					sessionUtils.getLocale(request),
					FileUploadServiceImpl.RESOURCE_BUNDLE, "fileUpload.error",
					e.getMessage()));

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "files/file/{uuid}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<FileUpload> deleteFile(
			final HttpServletRequest request, HttpServletResponse response,
			@PathVariable String uuid) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			FileUpload fileUpload = resourceService.getFileByUuid(uuid);
			if (fileUpload == null) {
				return new ResourceStatus<FileUpload>(false, I18N.getResource(
						sessionUtils.getLocale(request),
						FileUploadServiceImpl.RESOURCE_BUNDLE,
						"fileUpload.cannotFindFile", uuid));
			}

			String preDeletedName = fileUpload.getFileName();
			resourceService.deleteFile(fileUpload);
			return new ResourceStatus<FileUpload>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					FileUploadServiceImpl.RESOURCE_BUNDLE,
					"fileUpload.deleted", preDeletedName));

		} catch (ResourceException e) {
			log.error("File upload failed", e);
			return new ResourceStatus<FileUpload>(false, e.getMessage());
		}  catch (Throwable e) {
			log.error("File upload failed", e);
			return new ResourceStatus<FileUpload>(false, I18N.getResource(
					sessionUtils.getLocale(request),
					FileUploadServiceImpl.RESOURCE_BUNDLE, "fileUpload.error",
					e.getMessage()));

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "files/download/{uuid}", method = RequestMethod.GET)
	@ResponseStatus(value = HttpStatus.OK)
	public void downloadFile(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String uuid)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, IOException, ResourceNotFoundException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			resourceService.downloadURIFile(uuid, request, response, true);

		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "files/export/{id}", method = RequestMethod.GET, produces = { "text/plain" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public String export(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException,
			ResourceExportException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		try {
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ resourceService.getResourceCategory() + "-"
					+ resourceService.getResourceById(id).getName() + ".json\"");
			return resourceService.exportResoure(id);
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "files/export", method = RequestMethod.GET, produces = { "text/plain" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public String exportAll(HttpServletRequest request,
			HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException,
			ResourceExportException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Thread.sleep(1000);
		} catch (Exception e) {
		}
		try {
			response.setHeader("Content-Disposition", "attachment; filename=\""
					+ resourceService.getResourceCategory() + "-all" + ".json\"");
			return resourceService.exportResources(resourceService.allResources());
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "files/import", method = { RequestMethod.POST }, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<FileUpload> importLaunchers(
			HttpServletRequest request, HttpServletResponse response,
			@RequestParam(value = "file") MultipartFile jsonFile,
			@RequestParam(required=false) boolean dropExisting)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			Thread.sleep(2000);
		} catch (Exception e) {
		}
		try {
			String json = IOUtils.toString(jsonFile.getInputStream());
			if (!HypersocketUtils.isValidJSON(json)) {
				throw new ResourceException(I18NServiceImpl.USER_INTERFACE_BUNDLE,
						"error.incorrectJSON");
			}
			Collection<FileUpload> collects = resourceService
					.importResources(json, getCurrentRealm(), dropExisting);
			return new ResourceStatus<FileUpload>(
					true,
					I18N.getResource(
							sessionUtils.getLocale(request),
							I18NServiceImpl.USER_INTERFACE_BUNDLE,
							"resource.import.success", collects.size()));
		} catch (ResourceException e) {
			return new ResourceStatus<FileUpload>(false, e.getMessage());
		} catch (Exception e) {
			return new ResourceStatus<FileUpload>(
					false,
					I18N.getResource(
							sessionUtils.getLocale(request),
							I18NServiceImpl.USER_INTERFACE_BUNDLE,
							"resource.import.failure",
							e.getMessage()));
		} finally {
			clearAuthenticatedContext();
		}
	}
}
