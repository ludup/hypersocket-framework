package com.hypersocket.bulk.json;

import com.hypersocket.resource.ResourceException;

public interface BulkAssignmentService {
    void bulkAssignRolesToResource(BulkAssignment bulkAssignment) throws ResourceException;
}
