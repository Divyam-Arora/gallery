package com.divyam.cloud_media_gallery.model;

import com.divyam.cloud_media_gallery.model.audit.DateAudit;
import lombok.*;

import javax.persistence.*;
import java.util.*;

@NoArgsConstructor
@Getter
@Setter
@Entity
public class Media extends DateAudit implements Comparable<Media>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private int date;
    private String month;
    private int year;

    @OneToOne(cascade = {CascadeType.REMOVE})
    private File file;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    private User owner;

    @ManyToMany(cascade = {CascadeType.REFRESH,CascadeType.MERGE,CascadeType.DETACH})
    @OrderBy("value asc")
    private Set<Tag> tags;

    @Override
    public int compareTo(Media o) {
        return (int)(this.getId() - o.getId());
    }

    public void addTag(Tag tag){
        this.tags.add(tag);
    }

    public void removeTag(Tag tag){
        this.tags.remove(tag);
    }
}