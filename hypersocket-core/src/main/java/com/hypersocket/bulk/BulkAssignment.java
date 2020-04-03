package com.hypersocket.bulk;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BulkAssignment {

    private List<Long> resourceIds;
    private List<Long> roleIds;
    private BulkAssignmentMode mode;

    public List<Long> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<Long> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }

    public BulkAssignmentMode getMode() {
        return mode;
    }

    public void setMode(BulkAssignmentMode mode) {
        this.mode = mode;
    }
}
