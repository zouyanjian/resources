package com.feature.resources.server.dto;

/**
 * User: ZouYanjian
 * Date: 12-6-18
 * Time: 下午4:19
 * FileName:WorkspaceDTO
 */

public class WorkSpaceDTO {
    private String id;
    private String name;
    private String userId;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
