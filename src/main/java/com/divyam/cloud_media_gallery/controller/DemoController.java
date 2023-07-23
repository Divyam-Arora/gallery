package com.divyam.cloud_media_gallery.controller;

import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class DemoController {

  @Autowired
  UserService userService;

  @GetMapping("/hello")
  public String hello() {
    User user = userService.loadUserFromContext();
    return user.toString();
  }

  @PostMapping("/upload")
  public ResponseEntity<?> upload(@RequestBody MultipartFile[] media) throws IOException {
    System.out.println(media[0].getName());
    System.out.println(media[0].getContentType());
    String[] mediaType = media[0].getContentType().split("/");
    return ResponseEntity.ok().contentType(new MediaType(mediaType[0],mediaType[1])).body(media[0].getBytes());
  }
}