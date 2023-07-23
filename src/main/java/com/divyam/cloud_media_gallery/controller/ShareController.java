package com.divyam.cloud_media_gallery.controller;

import com.divyam.cloud_media_gallery.payload.request.ShareRequest;
import com.divyam.cloud_media_gallery.payload.response.MediaListItemResponse;
import com.divyam.cloud_media_gallery.payload.response.PeopleSharedMediaResponse;
import com.divyam.cloud_media_gallery.payload.response.SharedMediaResponse;
import com.divyam.cloud_media_gallery.service.ShareService;
import com.divyam.cloud_media_gallery.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ShareController {

    @Autowired
    ShareService shareService;

    @Autowired
    UserService userService;

    @PostMapping("/share/people")
    public ResponseEntity<?> share(@RequestBody ShareRequest shareRequest){
        return ResponseEntity.ok(shareService.shareWithPeople(shareRequest.getToAdd(), shareRequest.getPeople()));
    }

    @PostMapping("/share/edit/people/media")
    public ResponseEntity<?> editPersonSharedMedia(@RequestBody ShareRequest shareRequest){
        List<SharedMediaResponse> share = shareService.shareWithPeople(shareRequest.getToAdd(), shareRequest.getPeople());
        List<MediaListItemResponse> unShare = shareService.unShareWithPeople(shareRequest.getToRemove(), shareRequest.getPeople());
        return ResponseEntity.ok(new PeopleSharedMediaResponse(List.of(userService.getPersonDetails(shareRequest.getPeople().get(0))),share,unShare));
    }

//    @GetMapping("/share/people/media/all")
//    public ResponseEntity<?> getAllPeopleSharedMedia(@RequestBody List<String> people){
//        return ResponseEntity.ok(shareService.getAllSharedMedia(people));
//    }

    @GetMapping("/share/person/{username}/media/all")
    public ResponseEntity<?> getAllPersonSharedMedia(@PathVariable String username){
        return ResponseEntity.ok(shareService.getPersonAllSharedMedia(username));
    }


}