package com.hypersocket.resource;

import com.hypersocket.permissions.AccessDeniedException;

import java.util.List;

public interface FindableResourceRepository<T extends AbstractResource> {

    T getResourceById(Long id) throws ResourceNotFoundException, AccessDeniedException;

    List<T> getResourcesByIds(Long...ids);
}
