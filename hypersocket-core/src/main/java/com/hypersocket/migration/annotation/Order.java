package com.hypersocket.migration.annotation;

public @interface Order {
    enum Direction {ASC, DESC}

    Direction direction() default Direction.ASC;

    String property() default "created";
}


