package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.ConversationActivity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {
    private long id;
    private String name;
    private String admin;
    private String createdBy;
    private boolean isGroup;
    private int mediaCount;
    private Set<UserResponse> members;
    private int memberCount;
    private Instant updatedAt;
    private Instant createdAt;
    private SharedMediaResponse lastMediaShared;
    private ActivityResponse lastActivity;
    private String iconURL;
    private String iconThumbnail;
}