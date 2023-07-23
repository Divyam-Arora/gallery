package com.divyam.cloud_media_gallery.controller;

import com.divyam.cloud_media_gallery.payload.request.EditRequest;
import com.divyam.cloud_media_gallery.payload.request.AlbumRequest;
import com.divyam.cloud_media_gallery.payload.response.Success;
import com.divyam.cloud_media_gallery.service.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/album")
public class AlbumController {

    @Autowired
    AlbumService albumService;

    @PostMapping("")
    public ResponseEntity<?> createAlbum(@RequestBody AlbumRequest albumRequest){

        return ResponseEntity.ok().body(albumService.create(albumRequest));
    }

    @GetMapping("")
    public ResponseEntity<?> getAllAlbums(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "40") Integer size, @RequestParam(value = "extra", defaultValue = "0") Integer extra, @RequestParam(value = "search" , defaultValue = "") String search){
        return ResponseEntity.ok(albumService.loadAllAlbums(page, size, extra, search));
    }

    @GetMapping("/{albumId}")
    public ResponseEntity<?> getAlbum(@PathVariable Long albumId){
        return ResponseEntity.ok(albumService.loadAlbum(albumId));
    }

    @GetMapping("/{albumId}/media")
    public ResponseEntity<?> getAlbumMedia(@PathVariable Long albumId, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "40") int size, @RequestParam(value = "extra", defaultValue = "0") int extra, @RequestParam(value = "search", defaultValue = "") String search){
        return ResponseEntity.ok().body(albumService.loadAlbumMedia(albumId,page,size,extra, search));
    }

    @GetMapping("/{albumId}/media/all")
    public ResponseEntity<?> getAllAlbumMedia(@PathVariable Long albumId){
        return ResponseEntity.ok(albumService.loadAllAlbumMedia(albumId));
    }

    @DeleteMapping("/{albumId}")
    public ResponseEntity<?> deleteAlbum(@PathVariable Long albumId){
        albumService.deleteAlbum(albumId);
        return ResponseEntity.ok().body(new Success("Successfully deleted album with id " + albumId));
    }

    @PutMapping("/{albumId}")
    public ResponseEntity<?> editAlbum(@PathVariable Long albumId, @RequestBody AlbumRequest albumRequest){
        return ResponseEntity.ok(albumService.editAlbum(albumId,albumRequest));
    }

    @PutMapping("/{albumId}/media")
    public ResponseEntity<?> editAlbumMedia(@PathVariable Long albumId, @RequestBody EditRequest<Long> media){
        albumService.addMedia(albumId, media.getToAdd());
        albumService.removeMedia(albumId, media.getToRemove());
        return ResponseEntity.ok().body(albumService.loadAlbumListResponse(albumId));
    }
}