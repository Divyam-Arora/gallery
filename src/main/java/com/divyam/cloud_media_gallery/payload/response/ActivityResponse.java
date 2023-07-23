package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.Action;
import com.divyam.cloud_media_gallery.model.ConversationActivity;
import com.divyam.cloud_media_gallery.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ActivityResponse {
    private String by;
    private Action action;
    private Long targetId;
    private String targetString;
    private Instant on;

    public ActivityResponse(ConversationActivity conversationActivity){
        if(conversationActivity != null){
        this.by = conversationActivity.getBy().getUsername();
        this.action = conversationActivity.getAction();
        this.targetId = conversationActivity.getTargetId();
        this.targetString = conversationActivity.getTargetString();
        this.on = conversationActivity.getCreatedAt();
        }
    }
}