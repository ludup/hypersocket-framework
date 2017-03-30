package com.hypersocket.migration.execution;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hypersocket.migration.helper.MigrationDeserializer;

import java.util.List;
import java.util.Map;

public class ObjectPack {

    private String group;
    private List<MigrationObjectWithMeta> objectList;
    private Map<String, List<Map<String, ?>>> customOperationsMap;

    public ObjectPack() {}

    public ObjectPack(String group, List<MigrationObjectWithMeta> objectList) {
        this.group = group;
        this.objectList = objectList;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @JsonDeserialize(using = MigrationDeserializer.class)
    public List<MigrationObjectWithMeta> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<MigrationObjectWithMeta> objectList) {
        this.objectList = objectList;
    }

    public Map<String, List<Map<String, ?>>> getCustomOperationsMap() {
        return customOperationsMap;
    }

    public void setCustomOperationsMap(Map<String, List<Map<String, ?>>> customOperationsMap) {
        this.customOperationsMap = customOperationsMap;
    }
}
