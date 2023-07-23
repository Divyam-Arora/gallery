package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.util.Constants;
import com.divyam.cloud_media_gallery.util.Helpers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
@Data
@AllArgsConstructor
public class MediaListItemResponse {
    private long id;
    private Instant date;
    private String url;
    private String hd_url;
    private String media_type;
    private String alt;
    private UserResponse owner;

    public MediaListItemResponse(Media media) {
        this.id = media.getId();
        this.date = media.getCreatedAt();
        this.url = Helpers.generateMediaURL(media) + Constants.URL_THUMBNAIL_EXTENSION;
        this.hd_url = Helpers.generateMediaURL(media) + Constants.URL_FILE_EXTENSION;
        this.media_type = media.getFile().getContentType();
        this.alt = media.getFile().getName();
        this.owner = new UserResponse(media.getOwner());
    }

    public static List<MediaListItemResponse> getMediaListItemResponseList(List<Media> mediaList){
        return mediaList.stream().map(MediaListItemResponse::new).collect(Collectors.toList());
    }

    public static List<String> getMediaThumbnailsList(List<Media> mediaList){
        return mediaList.stream().map(media -> Helpers.generateMediaURL(media)+ Constants.URL_THUMBNAIL_EXTENSION).collect(Collectors.toList());
    }
}