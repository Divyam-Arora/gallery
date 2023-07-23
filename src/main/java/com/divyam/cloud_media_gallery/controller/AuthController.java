package com.divyam.cloud_media_gallery.controller;

import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.payload.response.Success;
import com.divyam.cloud_media_gallery.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Success> register(@RequestBody User user){
        userService.saveUser(user);
        return ResponseEntity.ok().body(new Success("Registration successful"));
    }
}