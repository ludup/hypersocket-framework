package com.hypersocket.realm;

import com.hypersocket.resource.AbstractResource;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "master_password")
public class MasterPassword extends AbstractResource{

    @Column(name="password")
    String password;

    @Column(name="password_algorithm")
    String passwordAlgorithm;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPasswordAlgorithm() {
        return passwordAlgorithm;
    }

    public void setPasswordAlgorithm(String passwordAlgorithm) {
        this.passwordAlgorithm = passwordAlgorithm;
    }
}
