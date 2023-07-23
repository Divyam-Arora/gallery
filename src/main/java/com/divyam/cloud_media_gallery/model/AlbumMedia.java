package com.divyam.cloud_media_gallery.model;

import com.divyam.cloud_media_gallery.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"album_id", "media_id"}))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlbumMedia extends DateAudit implements Comparable<AlbumMedia>{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Album album;

    @ManyToOne
    private Media media;

    @Override
    public int compareTo(AlbumMedia o) {
        return (int) (this.id - o.getId());
    }
}