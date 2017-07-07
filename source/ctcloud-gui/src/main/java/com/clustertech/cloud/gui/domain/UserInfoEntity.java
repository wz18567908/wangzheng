package com.clustertech.cloud.gui.domain;

import java.util.List;

/*
 * This object is used for testing login, it will be delete in the future.
 */
public class UserInfoEntity {
    private int id;
    private String username;
    private List<String> roles;
    private List<String> permissions;

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public List<String> getRoles() {
        return roles;
    }
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getPermissions() {
        return permissions;
    }
    public void setPermissions(List<String> permissions) {
        this.permissions = permissions;
    }
}
