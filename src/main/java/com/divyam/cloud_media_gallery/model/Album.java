package com.divyam.cloud_media_gallery.model;

import com.divyam.cloud_media_gallery.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Album extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Lob
    private String description;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    private User owner;

    @OneToMany(mappedBy = "album")
    @OrderBy(value = "id desc")
    private List<AlbumMedia> media = new ArrayList<>();

    public void addMedia(List<Media> mediaList){
        media.addAll(mediaList.stream().map(m -> new AlbumMedia(null, this, m)).toList());
    }

    public void deleteMedia(List<Media> mediaList){
        mediaList.forEach(m -> {
            media.removeIf(am -> Objects.equals(am.getMedia(), m));
        });
    }

}