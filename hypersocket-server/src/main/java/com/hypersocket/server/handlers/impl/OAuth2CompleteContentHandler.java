package com.hypersocket.server.handlers.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.server.HypersocketServer;
import com.hypersocket.server.handlers.HttpRequestHandler;
import com.hypersocket.server.handlers.impl.ContentHandlerImpl.CSPFilter;
import com.hypersocket.utils.HttpUtils;

@Component
public class OAuth2CompleteContentHandler extends HttpRequestHandler implements ContentHandler {
	static final Logger LOG = LoggerFactory.getLogger(OAuth2CompleteContentHandler.class);

	public static final String PATH_PREFIX = "/oauth2Complete";
	
	
	public static class OAuth2Request {
		private final String codeChallenge;
		private final String state;
		private final String baseUri;
		private final String redirectUri;
		private final String clientId;
		private final String clientSecret;
		private final String codeVerifier;

		public OAuth2Request(String baseUri, HttpServletRequest req, String serverBasePath) {
			this(baseUri, req, serverBasePath, null, null);
		}

		public OAuth2Request(String baseUri, HttpServletRequest req, String serverBasePath, String clientId, String clientSecret) {
			this.baseUri = baseUri;
			this.clientId = clientId;
			this.clientSecret = clientSecret;
			try {
				URL requestURL = new URL(req.getRequestURL().toString());
				redirectUri =  requestURL.getProtocol() + "://" + 
						requestURL.getHost() + ( requestURL.getPort() == -1 ? "" : ":" + requestURL.getPort() ) + serverBasePath + OAuth2CompleteContentHandler.PATH_PREFIX;
				
				codeVerifier = genToken(); 
				
				MessageDigest digest = MessageDigest.getInstance("SHA256");
				digest.reset();
				digest.update(codeVerifier.getBytes("UTF-8"));
				byte[] hashed = digest.digest();
				codeChallenge = Base64.getEncoder().encodeToString(hashed);
			}
			catch(NoSuchAlgorithmException | UnsupportedEncodingException | MalformedURLException nsae) {
				throw new IllegalStateException("Failed to create OAuth2 request.", nsae);
			}

			state = genToken();
		}
		
		public String uri(String scope) { 
			try {
				var clientIdTxt = clientId == null ? "" : ( 
						"client_id=" + URLEncoder.encode(clientId, "UTF-8") + "&"
				);
				if(clientSecret != null)  {  
					clientIdTxt  += "client_secret=" + URLEncoder.encode(clientSecret, "UTF-8") + "&";
				}
				return baseUri + 
						"/app/oauth2?" + clientIdTxt + 
						"state=" + URLEncoder.encode(state, "UTF-8") + "&" + 
						"scope=" + scope + "&" +
						"response_type=code&" +
						"code_challenge=" +  URLEncoder.encode(codeChallenge, "UTF-8") +  "&" +
						"code_challenge_method=S256&" +
						"redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8");
			}
			catch(UnsupportedEncodingException uee) {
				throw new IllegalStateException(uee);
			}
		}

		public String codeVerifier() {
			return codeVerifier;
		}

		public String state() {
			return state;
		}
	}

	public static class OAuth2Token {
		private String token;
		private String refreshToken;
		private long expires;

		public OAuth2Token(String token, String refreshToken, long expires) {
			super();
			this.token = token;
			this.refreshToken = refreshToken;
			this.expires = expires;
		}

		public String getToken() {
			return token;
		}

		public String getRefreshToken() {
			return refreshToken;
		}

		public long getExpires() {
			return expires;
		}

	}
	
	public interface OAuth2Authorized {
		void handleAuthorization(OAuth2Token token, HttpServletRequest request, HttpServletResponse response,
				OAuth2Authorization authorization) throws Exception;
	}

	public static class OAuth2Authorization {
		public static final String ATTRIBUTE_NAME = OAuth2Authorization.class.getName();

		private final String state;
		private String browserUri;
		private final String tokenUri;
		private final String codeVerifier;
		private final String redirectUri;
		private final String clientId;
		private final OAuth2Authorized onAuthorized;

		public OAuth2Authorization(String browserUri, 
				OAuth2Request req, OAuth2Authorized onAuthorized) {
			this.clientId = req.clientId;
			this.browserUri = browserUri;
			this.codeVerifier = req.codeVerifier;
			this.redirectUri = req.redirectUri;
			this.state = req.state;
			this.onAuthorized = onAuthorized;

			tokenUri = req.baseUri + "/app/api/oauth2/token"; 
			
		}

		public String getRedirectUri() {
			return redirectUri;
		}

		public String getCodeVerifier() {
			return codeVerifier;
		}

		public String getTokenUri() {
			return tokenUri;
		}

		public String getBrowserUri() {
			return browserUri;
		}

		public String getState() {
			return state;
		}

		protected void setBrowserUri(String browserUri) {
			this.browserUri = browserUri;
		}

		protected final void handleAuthorization(OAuth2Token token, HttpServletRequest request,
				HttpServletResponse response,
				OAuth2Authorization authorization) throws Exception {
			onAuthorized.handleAuthorization(token, request, response, authorization);
		}

		public String getClientId() {
			return clientId;
		}

	}

	@Autowired
	private HypersocketServer server;
	@Autowired
	private HttpUtils httpUtils;

	private Map<String, OAuth2Authorization> authorizations = new HashMap<>();

	public OAuth2CompleteContentHandler() {
		super("oAuth", 9999);
	}

	@Override
	public String getResourceName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public InputStream getResourceStream(String path) throws FileNotFoundException {
		throw new UnsupportedOperationException();
	}

	@Override
	public long getResourceLength(String path) throws FileNotFoundException {
		return 0;
	}

	@Override
	public long getLastModified(String path) throws FileNotFoundException {
		return System.currentTimeMillis();
	}

	@Override
	public int getResourceStatus(String path) throws RedirectException {
		synchronized (authorizations) {
			return HttpStatus.SC_OK;
		}
	}

	@Override
	public void addAlias(String alias, String path) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addFilter(ContentFilter filter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeAlias(String string) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void addDynamicPage(String path) {
		throw new UnsupportedOperationException();

	}

	@Override
	public boolean hasAlias(String alias) {
		return false;
	}

	@Override
	public boolean handlesRequest(HttpServletRequest request) {
		String uri = request.getRequestURI();
		return uri.equals(server.getBasePath() + PATH_PREFIX);
	}

	@Override
	public void handleHttpRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		synchronized (authorizations) {
			String state = request.getParameter("state");
			if (state == null) {
				LOG.error("No state parameter provided for oauth2 handler.");
				response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			OAuth2Authorization c = authorizations.get(state);
			if (c == null) {
				LOG.warn("LogonState has expired.");
				response.sendError(HttpStatus.SC_INTERNAL_SERVER_ERROR);
				return;
			}
			request.setAttribute(OAuth2Authorization.ATTRIBUTE_NAME, c);

			/*
			 * If the authhorized service returned an error, then redirect to the original
			 * URI and show an error message.
			 */
			String error = request.getParameter("error");
			String errorDescription = request.getParameter("errorDescription");
			if (StringUtils.isNotBlank(error)) {
				if (StringUtils.isBlank(errorDescription)) {
					errorDescription = "The authorization server returned the error '" + error + "'";
				}
			}
			
			String redirectTo = c.getBrowserUri();

			try {
				if (StringUtils.isNotBlank(errorDescription)) {
					throw new IllegalStateException(
							"Failure OAuth response. " + errorDescription);
				}

				/* Get expected parameters */
				String code = request.getParameter("code");
				if (StringUtils.isBlank(code)) {
					throw new IllegalArgumentException("No code parameter provided for oauth2 handler.");
				}

				LOG.info(String.format("Handling oauth reply for state %s. Token URI is %s", state, c.getTokenUri()));

				/*
				 * Exchange the authorization code for an access token, but instead of providing
				 * a pre-registered client secret, you send the PKCE secret generated at the
				 * beginning of the flow.
				 */
				Map<String, String> parameters = new HashMap<>();
				parameters.put("grant_type", "authorization_code");
				parameters.put("code", code);
				parameters.put("redirect_uri", c.getRedirectUri());
				parameters.put("client_id", c.getClientId());
				parameters.put("code_verifier", c.getCodeVerifier());

				Map<String, String> headers = new HashMap<>();

				/*
				 * TODO this is really really bad and relies on a really really bad hack in the
				 * hypersocket server.
				 */
				headers.put("Origin", "moz-extension://");

				String tokenResponse = httpUtils.doHttpPost(c.getTokenUri(), parameters, true, headers);
				ObjectMapper mapper = new ObjectMapper();
				JsonNode tokenResponseObj = mapper.readTree(tokenResponse);
				if (tokenResponseObj.has("error")) {
					String err = tokenResponseObj.get("error").asText();
					String description = tokenResponseObj.has("error_description")
							? tokenResponseObj.get("error_description").asText()
							: null;
					throw new IllegalStateException(err + ". " + (description == null ? "" : " " + description));
				}

				String accessToken = tokenResponseObj.get("access_token").asText();
				String refreshToken = tokenResponseObj.has("refresh_token")
						? tokenResponseObj.get("refresh_token").asText()
						: null;
				long expires = System.currentTimeMillis() + (tokenResponseObj.get("expires_in").asInt() * 1000);
				OAuth2Token token = new OAuth2Token(accessToken, refreshToken, expires);

				c.handleAuthorization(token, request, response, c);
			} catch (Exception e) {
				LOG.error("Failed to complete authorization (" + c.getTokenUri() + ").", e);

				request.getSession().setAttribute("flashStyle", "danger");
				request.getSession().setAttribute("flash", e.getMessage());
				
				redirectTo = c.getBrowserUri();
			}

			response.setHeader(HttpHeaders.LOCATION, redirectTo);
			response.sendError(HttpStatus.SC_MOVED_TEMPORARILY);
		}
	}

	@Override
	public boolean getDisableCache() {
		return true;
	}

	public void expectAuthorize(OAuth2Authorization auth) {
		synchronized (authorizations) {
			authorizations.put(auth.getState(), auth);
			// TODO expire the authorizations after certain amount time
		}
	}

	@PostConstruct
	private void setup() {
		server.registerHttpHandler(this);
	}

	@Override
	public void addCSPFilter(CSPFilter filter) {
		throw new UnsupportedOperationException();
	}

	public static String genToken() {
		return UUID.randomUUID().toString().replace("-", "");
	}
}
