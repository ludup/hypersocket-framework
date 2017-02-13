package com.hypersocket.bulk.json;

import java.util.List;

public interface BulkAssignmentRepository {
    void bulkAssignRolesToResource(List<Long> roleIds, List<Long> resourceIds,
                                          BulkAssignmentMode bulkAssignmentMode);
}
