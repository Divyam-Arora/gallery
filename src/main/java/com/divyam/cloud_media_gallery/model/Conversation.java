package com.divyam.cloud_media_gallery.model;

import com.divyam.cloud_media_gallery.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Conversation extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @ManyToOne
    private User admin;

    @ManyToOne
    private User createdBy;

    private boolean isGroup;

    @ManyToMany(cascade = {CascadeType.REFRESH, CascadeType.MERGE, CascadeType.DETACH})
    @OrderBy("username asc")
    private Set<User> members;

    private int mediaCount = 0;

    private int memberCount;

    @OneToOne
    private File icon;
}