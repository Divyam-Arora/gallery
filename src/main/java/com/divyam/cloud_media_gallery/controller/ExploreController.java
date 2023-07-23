package com.divyam.cloud_media_gallery.controller;

import com.divyam.cloud_media_gallery.service.ExploreService;
import com.divyam.cloud_media_gallery.service.MediaProperty;
import com.divyam.cloud_media_gallery.service.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequestMapping("/api/explore")
public class ExploreController {

    @Autowired
    ExploreService exploreService;

    @GetMapping("")
    public ResponseEntity<?> getMedia(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "40") Integer size, @RequestParam(value = "s") String search, @RequestParam(value = "type", required = false) MediaType type, @RequestParam(value = "target", defaultValue = "Name")MediaProperty target){

        return ResponseEntity.ok(exploreService.exploreMedia(page, size, search, type == null ? "" : type.name().toLowerCase(Locale.ROOT),target));

    }
}