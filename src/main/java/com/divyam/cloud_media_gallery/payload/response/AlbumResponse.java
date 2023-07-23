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
public class AlbumResponse {

    private long id;
    private String name;
    private String description;
    private Instant date;

    public AlbumResponse(Album album) {
        this.id = album.getId();
        this.name = album.getName();
        this.description = album.getDescription();
        this.date = album.getCreatedAt();
    }
}