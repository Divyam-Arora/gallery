package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.Action;
import com.divyam.cloud_media_gallery.model.ConversationActivity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDetailResponse<T> {
    private long id;
    private UserResponse by;
    private Action action;
    private T target;
    private Instant on;

    public ActivityDetailResponse(ConversationActivity activity, T target){
        this.id = activity.getId();
        this.by = new UserResponse(activity.getBy());
        this.action = activity.getAction();
        this.target = target;
        this.on = activity.getCreatedAt();
    }
}