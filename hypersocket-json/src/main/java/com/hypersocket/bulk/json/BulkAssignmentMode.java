package com.hypersocket.bulk.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.hypersocket.properties.enumeration.Displayable;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum BulkAssignmentMode implements Displayable<BulkAssignmentMode> {
    Merge("Merge"), OverWrite("Over Write");

    private String display;
    private int id;
    private static BulkAssignmentMode[] cache = BulkAssignmentMode.values();

    BulkAssignmentMode(String display) {
        this.display = display;
        this.id = this.ordinal();
    }

    @Override
    public String getDisplay() {
        return display;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public BulkAssignmentMode fromOrdinal(int ordinal){
        return cache[ordinal];
    }

    @JsonCreator
    public static BulkAssignmentMode forValue(String value) {
        int id = Integer.parseInt(value);
        return cache[id];
    }

    @Override
    public String toString() {
        return Integer.toString(this.ordinal());
    }

}
