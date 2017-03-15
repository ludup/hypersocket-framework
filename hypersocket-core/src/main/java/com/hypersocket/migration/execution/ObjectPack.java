package com.hypersocket.migration.execution;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hypersocket.migration.helper.MigrationDeserializer;
import com.hypersocket.repository.AbstractEntity;

import java.util.List;

public class ObjectPack {

    private String group;
    private List<AbstractEntity> objectList;

    public ObjectPack() {}

    public ObjectPack(String group, List<AbstractEntity> objectList) {
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
    public List<AbstractEntity> getObjectList() {
        return objectList;
    }

    public void setObjectList(List<AbstractEntity> objectList) {
        this.objectList = objectList;
    }
}
