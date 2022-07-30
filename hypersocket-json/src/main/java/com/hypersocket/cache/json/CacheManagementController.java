package com.hypersocket.cache.json;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.Cache.Entry;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.hypersocket.auth.json.AuthenticatedController;
import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.cache.CacheService;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.NameValuePair;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.RealmServiceImpl;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;

@Controller
public class CacheManagementController extends AuthenticatedController {

	@Autowired
	private CacheService cacheService;
	@Autowired
	private PermissionService permissionService;

	@AuthenticationRequired
	@RequestMapping(value = "cache/clear/{name}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public RequestStatus clear(HttpServletRequest request, HttpServletResponse response, @PathVariable String name)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, FileNotFoundException {
		checkAccess();
		getCache(name).clear();
		return new RequestStatus(true, I18N.getResource(sessionUtils.getLocale(request),
				RealmServiceImpl.RESOURCE_BUNDLE, "resource.deleted"));
	}

	@AuthenticationRequired
	@RequestMapping(value = "cache/{name}/{key}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public RequestStatus delete(HttpServletRequest request, HttpServletResponse response, @PathVariable String name,
			@PathVariable String key)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, FileNotFoundException {
		checkAccess();
		getCache(name).remove(key);
		return new RequestStatus(true, I18N.getResource(sessionUtils.getLocale(request),
				RealmServiceImpl.RESOURCE_BUNDLE, "resource.deleted"));
	}

	@AuthenticationRequired
	@RequestMapping(value = "cache/get/{name}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public Map<String, String> get(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String name)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, FileNotFoundException {
		checkAccess();
		Cache<Object, Object> cache = getCache(name);
		
		/* We can't really just dump out the objects as is, as this results in infinite 
		 * recursions. So just dump string representation which should be enough for the 
		 * simple cases.
		 */
		Map<String, String> str = new HashMap<>();
		for(Iterator<Entry<Object, Object>> it = cache.iterator(); it.hasNext(); ) {
			Entry<Object, Object> en = it.next();
			str.put(String.valueOf(en.getKey()), String.valueOf(en.getValue()));
		}
		return str;
	}

	@AuthenticationRequired
	@RequestMapping(value = "cache/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public List<String> listNames(HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		checkAccess();
		List<String> names = new ArrayList<>();
		for (String n : cacheService.getCacheManager().getCacheNames())
			names.add(n);
		return names;
	}

	@AuthenticationRequired
	@RequestMapping(value = "cache/{name}", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	@AuthenticatedContext
	public RequestStatus set(HttpServletRequest request, @PathVariable String name, @RequestBody NameValuePair[] items)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException, ResourceException,
			FileNotFoundException {
		checkAccess();
		for (NameValuePair nvp : items)
			getCache(name).put(nvp.getName(), nvp.getValue());
		return new RequestStatus(true, I18N.getResource(sessionUtils.getLocale(request),
				RealmServiceImpl.RESOURCE_BUNDLE, "resource.saved"));
	}

	private void checkAccess() throws AccessDeniedException {
		Principal currentPrincipal = permissionService.getCurrentPrincipal();
		if (!permissionService.hasAdministrativePermission(currentPrincipal)
				&& currentPrincipal.getType() != PrincipalType.SERVICE)
			throw new AccessDeniedException();
	}

	private Cache<Object, Object> getCache(String name) throws FileNotFoundException {
		try {
			Cache<Object, Object> cache = cacheService.getCacheManager().getCache(name);
			if (cache == null)
				throw new FileNotFoundException(String.format("No cache named %s", name));
			return cache;
		} catch (IllegalArgumentException iae) {
			String pat = "was defined with specific types Cache<";
			int idx = iae.getMessage().indexOf(pat);
			if (idx == -1)
				throw iae;
			else {
				String left = iae.getMessage().substring(idx + pat.length());
				idx = left.indexOf(">");
				left = left.substring(0, idx);
				String[] s = left.split(",");
				try {
					Class<?> keyClass = Class.forName(s[0].trim().substring(6));
					Class<?> valClass = Class.forName(s[1].trim().substring(6));
					@SuppressWarnings("unchecked")
					Cache<Object, Object> cache = (Cache<Object, Object>) cacheService.getCacheManager().getCache(name, keyClass, valClass);
					if (cache == null)
						throw new FileNotFoundException(String.format("No cache named %s", name));
					return cache;
				} catch (ClassNotFoundException cnfe) {
					throw new FileNotFoundException(String.format("No cache named %s", name));
				}
			}
		}
	}
}
