package com.divyam.cloud_media_gallery.controller;

import com.divyam.cloud_media_gallery.exception.NotAllowed;
import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.payload.request.EditRequest;
import com.divyam.cloud_media_gallery.payload.request.ConversationMediaEditRequest;
import com.divyam.cloud_media_gallery.payload.request.MediaDate;
import com.divyam.cloud_media_gallery.payload.response.MediaListItemResponse;
import com.divyam.cloud_media_gallery.service.*;
import com.divyam.cloud_media_gallery.util.Constants;
import com.divyam.cloud_media_gallery.util.Helpers;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    MediaService mediaService;

    @Autowired
    UserService userService;

    @Autowired
    AlbumService albumService;

    @Autowired
    ConversationService conversationService;

    @Autowired
    ShareService shareService;

    @Autowired
    TagService tagService;

    @Autowired
    FileService fileService;

    @Value("${project.media}")
    private String path;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(MultipartFile[] media) throws IOException, ImageWriteException, ImageReadException {
        List<MediaListItemResponse> response = new ArrayList<>();
        for (MultipartFile mediaItem : media) {
            if(!List.of("image", "video").contains(mediaItem.getContentType().split("/")[0])){
                throw new NotAllowed("File type " + mediaItem.getContentType());
            }
            com.divyam.cloud_media_gallery.model.File file = fileService.saveFile(mediaItem);
            Media mediaMeta = mediaService.saveMediaMeta(file);
            MediaListItemResponse mediaListItemResponse = new MediaListItemResponse();
            mediaListItemResponse.setId(mediaMeta.getId());
            mediaListItemResponse.setDate(mediaMeta.getCreatedAt());
            mediaListItemResponse.setMedia_type(mediaMeta.getFile().getContentType());
            mediaListItemResponse.setAlt(mediaMeta.getFile().getName());
            mediaListItemResponse.setUrl(Helpers.generateIconURL(mediaMeta.getId(),"media") + Constants.URL_THUMBNAIL_EXTENSION);
            mediaListItemResponse.setHd_url(Helpers.generateIconURL(mediaMeta.getId(),"media") + Constants.URL_FILE_EXTENSION);
            response.add(mediaListItemResponse);
        }
        Collections.reverse(response);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("")
    public ResponseEntity<?> getAllMedia(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "40") Integer size, @RequestParam(value = "extra", defaultValue = "0") Integer extra, @RequestParam(value = "year", required = false) Integer year,  @RequestParam(value = "month", required = false) String month,  @RequestParam(value = "date", required = false) Integer date) {
        return ResponseEntity.ok().body(mediaService.loadAllMedia(page, size, extra, new MediaDate(date,month,year)));
    }

    @GetMapping("/{mediaId}")
    public ResponseEntity<?> getMedia(@PathVariable long mediaId) {

        return ResponseEntity.ok().body(mediaService.loadUserMedia(mediaId));
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<?> deleteMedia(@PathVariable long mediaId) throws IOException {
        return ResponseEntity.ok(mediaService.deleteMedia(mediaId));
    }

    @GetMapping("/{mediaId}/albums")
    public ResponseEntity<?> getMediaAlbums(@PathVariable long mediaId) {
        return ResponseEntity.ok(mediaService.getAlbumsByMedia(mediaId));
    }

    @GetMapping("/{mediaId}/albums/all")
    public ResponseEntity<?> getAllMediaAlbums(@PathVariable long mediaId) {
        return ResponseEntity.ok(mediaService.getAllAlbumsByMedia(mediaId));
    }

    @PostMapping("/{mediaId}/albums")
    public ResponseEntity<?> editMediaAlbums(@PathVariable long mediaId, @RequestBody EditRequest<Long> request){
        Media media = albumService.addMediaToAlbums(mediaId, request.getToAdd());
        media = albumService.removeMediaFromAlbums(mediaId, request.getToRemove());
        return ResponseEntity.ok(mediaService.generateMediaResponse(media));
    }

    @GetMapping("/{mediaId}/conversations")
    public ResponseEntity<?> getMediaConversations(@PathVariable long mediaId) {
        return ResponseEntity.ok(mediaService.getConversationsByMedia(mediaId));
    }

    @GetMapping("/{mediaId}/conversations/all")
    public ResponseEntity<?> getAllMediaConversations(@PathVariable long mediaId) {
        return ResponseEntity.ok(mediaService.getAllConversationsByMedia(mediaId));
    }

    @PostMapping("/{mediaId}/conversations")
    public ResponseEntity<?> editMediaConversations(@PathVariable long mediaId, @RequestBody ConversationMediaEditRequest request){
        conversationService.checkConversations(request.getConversationsToAdd());
        conversationService.checkConversations(request.getConversationsToRemove());
        mediaService.checkUserMedia(List.of(mediaId));
        userService.checkUsers(request.getPeopleToAdd());
        return ResponseEntity.ok(mediaService.generateMediaResponse(shareService.shareMedia(mediaId, request)));
    }

    @GetMapping("/{mediaId}/tags")
    public ResponseEntity<?> getMediaTags(@PathVariable long mediaId){
        return ResponseEntity.ok(mediaService.getMediaTags(mediaId));
    }

    @PostMapping("/{mediaId}/tags")
    public ResponseEntity<?> editMediaTags(@PathVariable long mediaId, @RequestBody EditRequest<String> request){
        tagService.editTags(mediaId,request.getToRemove(),EditAction.REMOVE);
        List<String> tags = tagService.editTags(mediaId,request.getToAdd(),EditAction.ADD);
        return ResponseEntity.ok(tags);
    }

//    @GetMapping("/{mediaId}/file")
//    public ResponseEntity<?> getMediaFile(@PathVariable long mediaId) throws IOException {
//        Media media = mediaService.loadMedia(mediaId);
//        String filePath = path + File.separator + media.getIdentifier().toString() + File.separator + media.getName();
//        System.out.println(filePath);
//        byte[] stream = new FileInputStream(filePath).readAllBytes();
////        System.out.println(stream.available());
//
//        return ResponseEntity.ok().contentType(new MediaType(media.getContentType(), media.getContentSubType())).body(stream);
//    }

    @GetMapping("/year")
    public ResponseEntity<?> getAllYear(){
        return ResponseEntity.ok(Map.of("years",mediaService.getAllYear()));
    }

    @GetMapping("/year/{year}/month")
    public ResponseEntity<?> getAllMonth(@PathVariable int year){
        return ResponseEntity.ok(Map.of("months",mediaService.getAllMonth(year)));
    }

    @GetMapping("/year/{year}/month/{month}/date")
    public ResponseEntity<?> getAllDate(@PathVariable int year, @PathVariable String month){
        return ResponseEntity.ok(Map.of("dates", mediaService.getAllDate(year,month)));
    }
}