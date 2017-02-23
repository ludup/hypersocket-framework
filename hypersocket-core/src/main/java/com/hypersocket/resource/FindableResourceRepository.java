package com.hypersocket.resource;

import com.hypersocket.permissions.AccessDeniedException;

public interface FindableResourceRepository<T extends AbstractResource> {

    T getResourceById(Long id) throws ResourceNotFoundException, AccessDeniedException;
}
