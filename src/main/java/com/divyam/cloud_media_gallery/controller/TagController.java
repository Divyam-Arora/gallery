package com.divyam.cloud_media_gallery.controller;

import com.divyam.cloud_media_gallery.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    TagService tagService;

    @GetMapping("")
    public ResponseEntity<?> getTags(@RequestParam(value = "s", defaultValue = "") String tag){
        return ResponseEntity.ok(tagService.getTags(tag));
    }
}