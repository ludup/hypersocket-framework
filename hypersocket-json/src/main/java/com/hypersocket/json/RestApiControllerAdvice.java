package com.hypersocket.json;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.hypersocket.error.ErrorEntity;
import com.hypersocket.local.LocalUser;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceBadRequestException;
import com.hypersocket.resource.ResourceNotFoundException;

@RestControllerAdvice(annotations = RestController.class)
public class RestApiControllerAdvice {

    static Logger log = LoggerFactory.getLogger(RestApiControllerAdvice.class);

    @ModelAttribute
    public void addAttributes(Model model, HttpServletRequest request) {
        LocalUser user = (LocalUser) request.getAttribute(RestApi.API_USER);
        if(user != null){
            model.addAttribute(RestApi.API_USER, user);
        }

        String masterPassword = request.getHeader(RestApi.HTTP_HEADER_MASTER_PASSWORD);
        if(StringUtils.isNotBlank(masterPassword)){
            model.addAttribute(RestApi.MODEL_TOKEN_MASTER_PASSWORD, masterPassword);
        }
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorEntity> notFound(ResourceNotFoundException e){
        String incidentId = getIncidentId();
        log.error("Problem in fetching resource -> Incident Id {}", incidentId, e);
        return new ResponseEntity<ErrorEntity>(new ErrorEntity("Resource you are trying to fetch is not present.", incidentId),
                HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceBadRequestException.class)
    public ResponseEntity<ErrorEntity> badRequest(ResourceBadRequestException e){
        String incidentId = getIncidentId();
        log.error("Problem in fetching resource -> Incident Id {} -> Code {}", incidentId, e.getCode(), e);
        return new ResponseEntity<ErrorEntity>(new ErrorEntity("Request not understood, missing proper arguments or invalid argument value.",
                incidentId, e.getCode()),
                HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorEntity> notFound(AccessDeniedException e){
        String incidentId = getIncidentId();
        log.error("Problem in fetching resource -> Incident Id {}", incidentId, e);
        return new ResponseEntity<ErrorEntity>(new ErrorEntity("You do not have access to the resource.", incidentId), HttpStatus.FORBIDDEN);
    }

    private String getIncidentId() {
        return UUID.randomUUID().toString();
    }

}
