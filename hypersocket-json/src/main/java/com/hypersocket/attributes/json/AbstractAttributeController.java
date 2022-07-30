package com.hypersocket.attributes.json;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;

import com.hypersocket.attributes.AbstractAttribute;
import com.hypersocket.attributes.AttributeService;
import com.hypersocket.attributes.RealmAttributeCategory;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.permissions.RoleUtils;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public abstract class AbstractAttributeController<A extends AbstractAttribute<C>, C extends RealmAttributeCategory<A>>
		extends ResourceController {

	protected AttributeService<A, C> service;

	private String resourceBundle;
	private String prefix;

	protected AbstractAttributeController(String resourceBundle, String prefix) {
		this.resourceBundle = resourceBundle;
		this.prefix = prefix;
	}
	
	public ResourceList<A> listAttributes(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		return new ResourceList<A>(
				service.getResources(sessionUtils
						.getCurrentRealm(request)));
	}

	public BootstrapTableResult<?> tableAttributes(final HttpServletRequest request)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		return processDataTablesRequest(request, new BootstrapTablePageProcessor() {

			@Override
			public Column getColumn(String col) {
				return AbstractAttributeController.this.getColumn(col);
			}

			@Override
			public List<?> getPage(String searchColumn, String searchPattern, int start, int length,
					ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException {
				return service.searchResources(sessionUtils.getCurrentRealm(request), searchColumn, searchPattern,
						start, length, sorting);

			}

			@Override
			public Long getTotalCount(String searchColumn, String searchPattern)
					throws UnauthorizedException, AccessDeniedException {
				return service.getResourceCount(sessionUtils.getCurrentRealm(request), searchColumn, searchPattern);
			}
		});
	}

	protected abstract Column getColumn(String col);

	public ResourceStatus<A> createOrUpdateAttribute(HttpServletRequest request, HttpServletResponse response,
			AttributeUpdate attribute)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {

		try {

			A newAttribute;

			Set<Role> roles = RoleUtils.processPermissions(attribute.getRoles());

			if (attribute.getId() != null) {
				newAttribute = service.updateAttribute(service.getResourceById(attribute.getId()), attribute.getName(),
						attribute.getCategory(), attribute.getDescription(), attribute.getDefaultValue(),
						attribute.getWeight(), attribute.getType(), attribute.getDisplayMode(), attribute.getReadOnly(), 
						attribute.getRequired(), attribute.getEncrypted(), attribute.getVariableName(), roles, attribute.getOptions());
			} else {
				newAttribute = service.createAttribute(attribute.getName(), attribute.getCategory(),
						attribute.getDescription(), attribute.getDefaultValue(), attribute.getWeight(),
						attribute.getType(), attribute.getDisplayMode(), attribute.getReadOnly(), attribute.getRequired(),
						attribute.getEncrypted(), attribute.getVariableName(), roles, attribute.getOptions());
			}
			return new ResourceStatus<A>(newAttribute,
					I18N.getResource(sessionUtils.getLocale(request), resourceBundle,
							attribute.getId() != null ? prefix + ".updated.info" : prefix + ".created.info",
							attribute.getName()));
		} catch (ResourceException e) {
			return new ResourceStatus<A>(false, e.getMessage());

		}
	}

	public ResourceStatus<A> deleteAttribute(HttpServletRequest request, HttpServletResponse response, Long id)
			throws AccessDeniedException, UnauthorizedException, SessionTimeoutException {
		try {

			A attribute = service.getResourceById(id);

			if (attribute == null) {
				return new ResourceStatus<A>(false, I18N.getResource(sessionUtils.getLocale(request), resourceBundle,
						"error.invalidAttributeId", id));
			}

			String preDeletedName = attribute.getName();
			service.deleteAttribute(attribute);

			return new ResourceStatus<A>(true, I18N.getResource(sessionUtils.getLocale(request), resourceBundle,
					prefix + ".deleted.info", preDeletedName));

		} catch (ResourceException ex) {
			return new ResourceStatus<A>(false, ex.getMessage());
		}
	}
}
