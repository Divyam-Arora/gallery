package com.divyam.cloud_media_gallery.exception;

import com.divyam.cloud_media_gallery.payload.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ResourceAccessDenied.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Object accessDenied(ResourceAccessDenied ex) {
        String result = ex.getMessage();
        System.out.println("###########"+result);
        return new ErrorResponse(result);
    }

    @ExceptionHandler(AlreadyExists.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public Object alreadyExists(AlreadyExists ex){
        System.out.println("############# " + ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(UserNotFound.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public Object userNotFound(UserNotFound ex){
        System.out.println("############# " + ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(NotAllowed.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public Object notAllowed(NotAllowed ex){
        System.out.println("############# " + ex.getMessage());
        return new ErrorResponse(ex.getMessage());
    }
}