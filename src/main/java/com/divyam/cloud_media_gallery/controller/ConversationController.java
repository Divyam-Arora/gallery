package com.divyam.cloud_media_gallery.controller;

import com.divyam.cloud_media_gallery.payload.request.ConversationShareRequest;
import com.divyam.cloud_media_gallery.payload.request.GroupNameRequest;
import com.divyam.cloud_media_gallery.payload.request.MembersRequest;
import com.divyam.cloud_media_gallery.payload.response.ActivityDetailResponse;
import com.divyam.cloud_media_gallery.payload.response.ConversationActivityResponse;
import com.divyam.cloud_media_gallery.payload.response.SharedMediaResponse;
import com.divyam.cloud_media_gallery.service.ConversationService;
import com.divyam.cloud_media_gallery.service.MediaService;
import com.divyam.cloud_media_gallery.service.ShareService;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ConversationController {

    @Autowired
    ConversationService conversationService;

    @Autowired
    ShareService shareService;

    @Autowired
    MediaService mediaService;

    @Value("${project.media}")
    private String path;

    @GetMapping("conversation")
    public ResponseEntity<?> getUserConversations(@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "40") Integer size, @RequestParam(value = "extra", defaultValue = "0") Integer extra, @RequestParam(value = "search" , defaultValue = "") String search, @RequestParam(value = "recent", defaultValue = "0") long recent, @RequestParam(value = "active", defaultValue = "false") Boolean active){
        return ResponseEntity.ok(conversationService.getAllConversationsByCurrentUser(page,size,extra,search, recent, active));
    }

    @GetMapping("conversation/{conversationId}")
    public ResponseEntity<?> getUserConversation(@PathVariable Long conversationId){
        return ResponseEntity.ok(conversationService.getConversationDetails(conversationId));
    }

    @GetMapping("conversation/activity")
    public ResponseEntity<?> getLatestActivity(@RequestParam(value = "sinceConversation", defaultValue = "0") Long sinceConversation, @RequestParam(value = "conversationId", defaultValue = "0") Long conversationId, @RequestParam(value = "sinceActivity", defaultValue = "0") Long sinceActivity, @RequestParam(value = "search", defaultValue = "") String search){
        return ResponseEntity.ok(conversationService.getRecentConversationActivity(conversationId, sinceConversation, sinceActivity, search));
    }

    @PostMapping("conversation")
    public ResponseEntity<?> EditConversationMembers(@RequestBody MembersRequest members, @RequestParam(value = "id", required = false) Long conversationId){
        return ResponseEntity.ok(conversationService.editGroupMembers(members.getToAdd(), members.getToRemove(), conversationId));
    }

    @PostMapping("conversation/{conversationId}/icon")
    public ResponseEntity<?> EditConversationIcon(@PathVariable Long conversationId, @RequestBody MultipartFile file) throws IOException, ImageWriteException, ImageReadException {
        return ResponseEntity.ok(conversationService.editConversationIcon(conversationId, file, path));
    }

    @DeleteMapping("conversation/{conversationId}/icon")
    public ResponseEntity<?> DeleteConversationIcon(@PathVariable Long conversationId){
        return ResponseEntity.ok(conversationService.deleteConversationIcon(conversationId));
    }

    @GetMapping("conversation/{conversationId}/info")
    public ResponseEntity<?> getUserConversationInfo(@PathVariable Long conversationId){
        return ResponseEntity.ok(conversationService.getConversationInfo(conversationId));
    }

    @GetMapping("conversation/{conversationId}/members")
    public ResponseEntity<?> getUserConversationMembers(@PathVariable Long conversationId,@RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "40") Integer size, @RequestParam(value = "extra", defaultValue = "0") Integer extra, @RequestParam(value = "search" , defaultValue = "") String search){
        return ResponseEntity.ok(conversationService.getConversationMembers(conversationId, page, size, extra, search));
    }

    @GetMapping("conversation/{conversationId}/members/all")
    public ResponseEntity<?> getAllUserConversationMembers(@PathVariable Long conversationId){
        return ResponseEntity.ok(conversationService.getAllConversationMembers(conversationId));
    }

    @GetMapping("conversation/{conversationId}/activity")
    public ResponseEntity<?> getUserConversationMedia(@PathVariable Long conversationId, @RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "40") Integer size, @RequestParam(value = "extra", defaultValue = "0") Integer extra, @RequestParam(value = "search" , defaultValue = "") String search, @RequestParam(value = "recent", defaultValue = "0") long recent){
        return ResponseEntity.ok(conversationService.getConversationActivity(conversationId, page, size, extra, search));
    }

    @GetMapping("conversation/{conversationId}/media")
    public ResponseEntity<?> getUserConversationActivity(@PathVariable Long conversationId, @RequestParam(value = "page", defaultValue = "0") Integer page, @RequestParam(value = "size", defaultValue = "40") Integer size, @RequestParam(value = "search" , defaultValue = "") String search){
        return ResponseEntity.ok(conversationService.getConversationMedia(conversationId, page, size, search));
    }

    @GetMapping("conversation/{conversationId}/media/all")
    public ResponseEntity<?> getAllUserConversationMedia(@PathVariable Long conversationId){
        return ResponseEntity.ok(conversationService.getAllConversationMediaId(conversationId));
    }

    @PostMapping("conversation/{conversationId}/media")
    public ResponseEntity<?> ShareConversationMedia(@PathVariable Long conversationId, @RequestBody ConversationShareRequest conversationShareRequest){
        ConversationActivityResponse response = new ConversationActivityResponse();
        List<ActivityDetailResponse<ActivityDetailResponse<Long>>> unshared = shareService.unShareWithConversation(conversationId, conversationShareRequest.getToRemove());
        List<ActivityDetailResponse<SharedMediaResponse>> shared = shareService.shareWithConversation(conversationId, conversationShareRequest.getToAdd());
        response.setConversation(conversationService.getConversationDetails(conversationId));

        ArrayList<ActivityDetailResponse> allActivities = new ArrayList<>(unshared);
        allActivities.addAll(shared);
        response.setActivities(allActivities);
        return ResponseEntity.ok(response);
    }

    @PutMapping("conversation/{conversationId}/name")
    public ResponseEntity<?> EditConversationName(@PathVariable Long conversationId, @RequestBody GroupNameRequest request){
        return ResponseEntity.ok(conversationService.EditConversationName(conversationId, request.getName()));
    }

    @DeleteMapping("conversation/{conversationId}/leave")
    public ResponseEntity<?> LeaveConversation(@PathVariable Long conversationId){
        conversationService.leaveConversation(conversationId);
        return ResponseEntity.ok().build();
    }
}