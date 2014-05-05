/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.certs.json;

import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.certs.CertificateService;
import com.hypersocket.certs.FileFormatException;
import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class CertificateController extends AuthenticatedController {

	@Autowired
	CertificateService certificateService;

	@AuthenticationRequired
	@RequestMapping(value = "certificates", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public CertificateStatus getState(HttpServletRequest request,
			HttpServletResponse response) throws UnauthorizedException,
			AccessDeniedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request), certificateService);
		try {

			CertificateStatus status = new CertificateStatus();

			status.setSuccess(true);
			status.setInstalledCertificate(certificateService
					.hasInstalledCertificate());
			status.setMatchingCertificate(certificateService
					.hasWorkingCertificate());

			return status;
		} finally {
			clearAuthenticatedContext(certificateService);
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public CertificateStatus resetKey(HttpServletRequest request,
			HttpServletResponse response) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request), certificateService);

		try {
			CertificateStatus status = new CertificateStatus();

			if (certificateService.resetPrivateKey()) {
				status.setSuccess(true);
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateService.RESOURCE_BUNDLE, "success.resetKey"));
			} else {
				status.setSuccess(false);
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateService.RESOURCE_BUNDLE, "error.resetKey"));
			}

			status.setInstalledCertificate(certificateService
					.hasInstalledCertificate());
			status.setMatchingCertificate(certificateService
					.hasWorkingCertificate());

			return status;
		} finally {
			clearAuthenticatedContext(certificateService);
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/key", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public CertificateStatus uploadKey(HttpServletRequest request,
			HttpServletResponse response,
			@RequestPart(value = "file") MultipartFile file,
			@RequestPart(value = "bundle") MultipartFile bundle,
			@RequestPart(value = "key") MultipartFile key,
			@RequestParam(value = "passphrase") String passphrase)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request), certificateService);

		try {
			CertificateStatus status = new CertificateStatus();
			status.setSuccess(false);
			try {
				if (certificateService.updatePrivateKey(key, passphrase, file, bundle)) {
					status.setSuccess(true);
					status.setMessage(I18N.getResource(
							sessionUtils.getLocale(request),
							CertificateService.RESOURCE_BUNDLE,
							"info.keyUploaded"));
				} else {
					status.setMessage(I18N.getResource(
							sessionUtils.getLocale(request),
							CertificateService.RESOURCE_BUNDLE,
							"error.generalError"));
				}
			} catch (InvalidPassphraseException e) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateService.RESOURCE_BUNDLE,
						"error.invalidPassphrase"));
			} catch (FileFormatException e) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateService.RESOURCE_BUNDLE,
						"error.invalidFormat"));
			}

			status.setInstalledCertificate(certificateService
					.hasInstalledCertificate());
			status.setMatchingCertificate(certificateService
					.hasWorkingCertificate());

			return status;

		} finally {
			clearAuthenticatedContext(certificateService);
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/cert", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public CertificateStatus uploadCertificate(HttpServletRequest request,
			HttpServletResponse response,
			@RequestPart(value = "file") MultipartFile file,
			@RequestPart(value = "bundle") MultipartFile bundle)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request), certificateService);

		try {
			CertificateStatus status = new CertificateStatus();
			status.setSuccess(false);
			try {
				certificateService.updateCertificate(file, bundle);
				status.setSuccess(true);
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateService.RESOURCE_BUNDLE,
						"info.certUploaded"));
				
			} catch (FileFormatException e) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateService.RESOURCE_BUNDLE,
						"error.invalidFormat"));
			} catch (CertificateExpiredException e) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateService.RESOURCE_BUNDLE, "error.certExpired"));
			} catch (CertificateNotYetValidException e) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateService.RESOURCE_BUNDLE,
						"error.certNotYetValid"));
			} catch(Exception ex) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateService.RESOURCE_BUNDLE,
						"error.generalError", ex.getMessage()));
			}

			status.setInstalledCertificate(certificateService
					.hasInstalledCertificate());
			status.setMatchingCertificate(certificateService
					.hasWorkingCertificate());

			return status;

		} finally {
			clearAuthenticatedContext(certificateService);
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/generateCSR", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public CertificateStatus generateCSR(HttpServletRequest request,
			HttpServletResponse response,
			@RequestParam(value = "cn") String cn,
			@RequestParam(value = "ou") String ou,
			@RequestParam(value = "o") String o,
			@RequestParam(value = "l") String l,
			@RequestParam(value = "s") String s,
			@RequestParam(value = "c") String c) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request), certificateService);

		try {
			CertificateStatus status = new CertificateStatus();
			status.setSuccess(false);
			try {
				String csr = certificateService.generateCSR(cn, ou, o, l, s, c);
				status.setMessage(csr);
				status.setSuccess(true);
			} catch (Exception e) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateService.RESOURCE_BUNDLE,
						"error.genericError"));
			}

			status.setInstalledCertificate(certificateService
					.hasInstalledCertificate());
			status.setMatchingCertificate(certificateService
					.hasWorkingCertificate());

			return status;

		} finally {
			clearAuthenticatedContext(certificateService);
		}
	}

}
