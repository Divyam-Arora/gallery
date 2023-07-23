package com.divyam.cloud_media_gallery.exception;

public class ResourceAccessDenied extends RuntimeException{
    private String resourceName;

    public ResourceAccessDenied(String resourceName) {
        super(String.format("Cannot access %s", resourceName));
        this.resourceName = resourceName;
    }

    public String getResourceName() {
        return resourceName;
    }
}