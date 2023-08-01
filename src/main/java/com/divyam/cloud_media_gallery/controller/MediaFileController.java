package com.divyam.cloud_media_gallery.controller;

import com.divyam.cloud_media_gallery.exception.ResourceNotFoundException;
import com.divyam.cloud_media_gallery.exception.UserNotFound;
import com.divyam.cloud_media_gallery.model.Conversation;
import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.payload.response.ConversationResponse;
import com.divyam.cloud_media_gallery.repo.ConversationRepo;
import com.divyam.cloud_media_gallery.service.ConversationService;
import com.divyam.cloud_media_gallery.service.MediaService;
import com.divyam.cloud_media_gallery.service.UserService;
import com.divyam.cloud_media_gallery.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public")
public class MediaFileController {

    @Autowired
    MediaService mediaService;

    @Autowired
    UserService userService;

    @Autowired
    ConversationRepo conversationRepo;

    @Value("${project.media}")
    private String path;

    @GetMapping("/media/{mediaId}/file/original")
    public ResponseEntity<?> getMediaFile(@PathVariable long mediaId, @RequestHeader(name = HttpHeaders.RANGE, defaultValue = "=-") String range) throws IOException {
        Media media = mediaService.loadMedia(mediaId);
        String filePath = path + File.separator + media.getFile().getIdentifier().toString() + File.separator + media.getFile().getName();
        System.out.println(filePath);
        FileInputStream inputStream = new FileInputStream(filePath);
        byte[] stream = null;
        ResponseEntity.BodyBuilder builder = null;
        if(media.getFile().getContentType().toUpperCase(Locale.ROOT).equals("IMAGE")){
            builder = ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofDays(1)));
            stream = inputStream.readAllBytes();
        } else{

        String[] bytes = range.split("=")[1].split("-");
        Long byteStart = null;
        Long byteEnd = null;


        if(bytes.length >= 1){
            byteStart = Long.parseLong(bytes[0]);
        }

        if(bytes.length >= 2){
            byteEnd = Long.parseLong(bytes[1]);
        }

//        System.out.println(Arrays.toString(bytes));
//        System.out.println(byteStart);
//        System.out.println(byteEnd);

        if(byteStart != null){
            inputStream.skipNBytes(byteStart);
        } else
            byteStart = 0L;
        byteEnd = (Math.min((1024 * 1024) + byteStart, media.getFile().getSize()));
        System.out.println(byteEnd);
        stream = inputStream.readNBytes((int) (byteEnd - byteStart));
        String contentRange = "bytes " + byteStart + "-" + (byteEnd - 1) + "/" + media.getFile().getSize();

        builder = ResponseEntity.status(HttpStatus.PARTIAL_CONTENT).header(HttpHeaders.ACCEPT_RANGES, "bytes").header(HttpHeaders.CONTENT_RANGE,contentRange);
        }
        inputStream.close();

//        System.out.println(stream.available());

        return builder.contentType(new MediaType(media.getFile().getContentType(),media.getFile().getContentSubType())).body(stream);
    }

    @GetMapping("/media/{mediaId}/file/thumbnail")
    public ResponseEntity<?> getMediaFileThumbnail(@PathVariable long mediaId) throws IOException {
        Media media = mediaService.loadMedia(mediaId);
        String filePath = path + File.separator + media.getFile().getIdentifier().toString() + File.separator + Constants.THUMBNAIL_FOLDER + File.separator + media.getFile().getName();
        System.out.println(filePath);
        FileInputStream inputStream = new FileInputStream(filePath);
        byte[] stream = inputStream.readAllBytes();
        inputStream.close();
//        System.out.println(stream.available());

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofDays(7))).contentType(Objects.equals(media.getFile().getContentType(), "image") ? new MediaType(media.getFile().getContentType(), media.getFile().getContentSubType()) : new MediaType("image","png")).body(stream);
    }

    @GetMapping("/conversation/{conversationId}/icon")
    public ResponseEntity<?> getConversationIcon(@PathVariable long conversationId) throws IOException {
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        String filePath = path + File.separator + conversation.getIcon().getIdentifier() + File.separator + conversation.getIcon().getName();
        System.out.println(filePath);
        FileInputStream inputStream = new FileInputStream(filePath);
        byte[] stream = inputStream.readAllBytes();
        inputStream.close();
//        System.out.println(stream.available());

        return ResponseEntity.ok().contentType(new MediaType(conversation.getIcon().getContentType(),conversation.getIcon().getContentSubType())).body(stream);
    }

    @GetMapping("/conversation/{conversationId}/thumbnail")
    public ResponseEntity<?> getConversationIconThumbnail(@PathVariable long conversationId) throws IOException {
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        String filePath = path + File.separator + conversation.getIcon().getIdentifier() + File.separator + Constants.THUMBNAIL_FOLDER + File.separator + conversation.getIcon().getName();
        System.out.println(filePath);
        FileInputStream inputStream = new FileInputStream(filePath);
        byte[] stream = inputStream.readAllBytes();
        inputStream.close();
//        System.out.println(stream.available());

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofDays(7))).contentType(new MediaType(conversation.getIcon().getContentType(),conversation.getIcon().getContentSubType())).body(stream);
    }

    @GetMapping("/user/{username}/icon")
    public ResponseEntity<?> getUserIcon(@PathVariable String username) throws IOException {
        User user = userService.getUser(username);
        if(user == null){
            throw new UserNotFound(username);
        }
        String filePath = path + File.separator + user.getIcon().getIdentifier() + File.separator + user.getIcon().getName();
        System.out.println(filePath);
        FileInputStream inputStream = new FileInputStream(filePath);
        byte[] stream = inputStream.readAllBytes();
        inputStream.close();
//        System.out.println(stream.available());

        return ResponseEntity.ok().contentType(new MediaType(user.getIcon().getContentType(),user.getIcon().getContentSubType())).body(stream);
    }

    @GetMapping("/user/{username}/thumbnail")
    public ResponseEntity<?> getUserIconThumbnail(@PathVariable String username) throws IOException {
        User user = userService.getUser(username);
        if(user == null){
            throw new UserNotFound(username);
        }
        String filePath = path + File.separator + user.getIcon().getIdentifier() + File.separator + Constants.THUMBNAIL_FOLDER + File.separator + user.getIcon().getName();
        System.out.println(filePath);
        FileInputStream inputStream = new FileInputStream(filePath);
        byte[] stream = inputStream.readAllBytes();
        inputStream.close();
//        System.out.println(stream.available());

        return ResponseEntity.ok().cacheControl(CacheControl.maxAge(Duration.ofDays(7))).contentType(new MediaType(user.getIcon().getContentType(),user.getIcon().getContentSubType())).body(stream);
    }



}