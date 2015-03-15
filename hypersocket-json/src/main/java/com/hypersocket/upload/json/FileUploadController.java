package com.hypersocket.upload.json;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import com.hypersocket.upload.FileUploadServiceImpl;

@Controller
public class FileUploadController extends ResourceController {

	@Autowired
	FileUploadService service;

	@AuthenticationRequired
	@RequestMapping(value = "fileUpload/metainfo/{uuid}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public FileUpload getFile(final HttpServletRequest request,
			HttpServletResponse response, @PathVariable String uuid)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return service.getFileByUuid(uuid);

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "fileUpload/file", method = RequestMethod.POST, produces = { "application/json" })
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
			fileUpload = service.createFile(file, realm);

			return new ResourceStatus<FileUpload>(fileUpload, I18N.getResource(
					sessionUtils.getLocale(request),
					FileUploadServiceImpl.RESOURCE_BUNDLE,
					"fileUpload.uploaded.info", fileUpload.getName()));

		} catch (ResourceCreationException e) {
			return new ResourceStatus<FileUpload>(false, e.getMessage());
		} catch (Throwable e) {
			return new ResourceStatus<FileUpload>(false, I18N.getResource(
					sessionUtils.getLocale(request),
					FileUploadServiceImpl.RESOURCE_BUNDLE, "fileUpload.error",
					e.getMessage()));

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "fileUpload/file/{uuid}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<FileUpload> deleteFile(
			final HttpServletRequest request, HttpServletResponse response,
			@PathVariable String uuid) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			FileUpload fileUpload = service.getFileByUuid(uuid);
			if (fileUpload == null) {
				return new ResourceStatus<FileUpload>(false, I18N.getResource(
						sessionUtils.getLocale(request),
						FileUploadServiceImpl.RESOURCE_BUNDLE,
						"fileUpload.cannotFindFile", uuid));
			}

			String preDeletedName = fileUpload.getFileName();
			service.deleteFile(fileUpload);
			return new ResourceStatus<FileUpload>(true, I18N.getResource(
					sessionUtils.getLocale(request),
					FileUploadServiceImpl.RESOURCE_BUNDLE,
					"fileUpload.deleted", preDeletedName));

		} catch (ResourceException e) {
			return new ResourceStatus<FileUpload>(false, e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "fileUpload/file/{uuid}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	public void downloadFile(HttpServletRequest request,
			HttpServletResponse response, @PathVariable String uuid)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			service.downloadURIFile(uuid, response);

		} catch (Exception e) {
			throw new RuntimeException("IOError writing file to output stream");
		}
	}
}
