package com.divyam.cloud_media_gallery.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedConversationMedia {

    private long id;
    private Media media;
    private String message;
    private Conversation conversation;
    private Instant createdAt;
    private Instant updatedAt;
    private Long conversationCount;
}