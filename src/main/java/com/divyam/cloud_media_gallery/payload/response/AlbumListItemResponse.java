package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.Album;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlbumListItemResponse {
    private long id;
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    private long mediaCount;
    private String thumbnail;
    private List<String> thumbnails;

}