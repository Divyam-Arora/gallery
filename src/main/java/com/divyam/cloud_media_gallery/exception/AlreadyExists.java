package com.divyam.cloud_media_gallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AlreadyExists extends RuntimeException{
    public AlreadyExists(String resourceName) {
        super(resourceName + " already exists");
    }
}