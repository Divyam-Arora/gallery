package com.divyam.cloud_media_gallery.model;

import com.divyam.cloud_media_gallery.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ConversationActivity extends DateAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    private Conversation conversation;

    @Enumerated(EnumType.STRING)
    private Action action;

    @ManyToOne
    private User by;

    private Long targetId;

    private String targetString;

    public ConversationActivity(Conversation conversation, Action action, User user, Long targetId, String targetString){
        this.conversation = conversation;
        this.action = action;
        this.by = user;
        this.targetId = targetId;
        this.targetString = targetString;
    }

}