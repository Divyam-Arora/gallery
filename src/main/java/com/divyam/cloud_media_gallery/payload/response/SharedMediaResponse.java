package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.util.Constants;
import com.divyam.cloud_media_gallery.util.Helpers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SharedMediaResponse {
    private long id;
    private Instant date;
    private String url;
    private String hd_url;
    private String media_type;
    private String alt;
    private UserResponse owner;
    private Instant sharedOn;
    private Long conversationCount;

    public SharedMediaResponse(Media media, Instant sharedOn, Long conversationCount){
        this.id = media.getId();
        this.date = media.getCreatedAt();
        this.url = Helpers.generateMediaURL(media) + Constants.URL_THUMBNAIL_EXTENSION;
        this.hd_url = Helpers.generateMediaURL(media) + Constants.URL_FILE_EXTENSION;
        this.media_type = media.getFile().getContentType();
        this.alt = media.getFile().getName();
        this.owner = new UserResponse(media.getOwner());
        this.sharedOn = sharedOn;
        this.conversationCount = conversationCount;
    }
}