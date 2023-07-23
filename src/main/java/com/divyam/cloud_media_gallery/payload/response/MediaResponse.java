package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.Tag;
import com.divyam.cloud_media_gallery.util.Constants;
import com.divyam.cloud_media_gallery.util.Helpers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MediaResponse {

    private long id;
    private long size;
    private String contentType;
    private String contentSubType;
    private String url;
    private String hd_url;
    private String name;
    private String alt;
    private UserResponse owner;
    private Instant createdAt;
    private List<String> tags;
    private long height;
    private long width;
    private List<ConversationListItemResponse> conversations;
    private List<AlbumListItemResponse> albums;

    public MediaResponse(Media media) {
        this.id = media.getId();
        this.size = media.getFile().getSize();
        this.contentType = media.getFile().getContentType();
        this.contentSubType = media.getFile().getContentSubType();
        this.url = Helpers.generateMediaURL(media) + Constants.URL_THUMBNAIL_EXTENSION;
        this.hd_url = Helpers.generateMediaURL(media) + Constants.URL_FILE_EXTENSION;
        this.name = media.getFile().getName();
        this.alt = media.getFile().getName();
        this.owner = new UserResponse(media.getOwner());
        this.createdAt = media.getCreatedAt();
        this.tags = media.getTags().stream().map(Tag::getValue).collect(Collectors.toList());
        this.height = media.getFile().getHeight();
        this.width = media.getFile().getWidth();
    }
}