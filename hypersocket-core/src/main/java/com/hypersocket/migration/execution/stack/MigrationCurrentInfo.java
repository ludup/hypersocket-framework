package com.hypersocket.migration.execution.stack;

public class MigrationCurrentInfo {

    private Object bean;
    private String propName;

    public MigrationCurrentInfo(Object bean, String propName) {
        this.bean = bean;
        this.propName = propName;
    }

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public String getPropName() {
        return propName;
    }

    public void setPropName(String propName) {
        this.propName = propName;
    }
}
