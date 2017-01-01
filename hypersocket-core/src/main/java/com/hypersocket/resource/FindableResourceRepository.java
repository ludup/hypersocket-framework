package com.hypersocket.resource;

import com.hypersocket.permissions.AccessDeniedException;

public interface FindableResourceRepository<T extends Resource> {

    T getResourceById(Long id) throws ResourceNotFoundException, AccessDeniedException;
}
