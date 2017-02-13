package com.hypersocket.bulk.json;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.session.json.SessionTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class BulkAssignmentController extends ResourceController {

    static Logger log = LoggerFactory.getLogger(BulkAssignmentController.class);

    public static final String RESOURCE_BUNDLE = "BulkAssignmentService";

    @Autowired
    BulkAssignmentService bulkAssignmentService;

    @AuthenticationRequired
    @RequestMapping(value = "assignable/bulk", method = RequestMethod.POST, produces = { "application/json" })
    @ResponseBody
    @ResponseStatus(value = HttpStatus.OK)
    public RequestStatus bulkAssignmentResource(HttpServletRequest request,
                                                        HttpServletResponse response,
                                                        @RequestBody BulkAssignment bulkAssignment)
            throws AccessDeniedException, UnauthorizedException,
            SessionTimeoutException {
        setupAuthenticatedContext(sessionUtils.getSession(request),
                sessionUtils.getLocale(request));
        try {
            bulkAssignmentService.bulkAssignRolesToResource(bulkAssignment);

            return new RequestStatus(true,
                    I18N.getResource(sessionUtils.getLocale(request),
                            RESOURCE_BUNDLE,
                            "bulk.assignemnt.success"));
        } catch (Exception e) {
            log.error("Problem in bulk assignment.", e);
            return new RequestStatus(false, e.getMessage());

        } finally {
            clearAuthenticatedContext();
        }
    }
}
