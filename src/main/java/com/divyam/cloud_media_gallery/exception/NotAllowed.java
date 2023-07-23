package com.divyam.cloud_media_gallery.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class NotAllowed extends RuntimeException{
    public NotAllowed(String m){
        super(m + " is not allowed!");
    }
}