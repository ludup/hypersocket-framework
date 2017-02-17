package com.hypersocket.properties.enumeration;

public interface Displayable<E> {
    String getDisplay();
    int getId();
    String getName();
    E fromOrdinal(int ordinal);
}
