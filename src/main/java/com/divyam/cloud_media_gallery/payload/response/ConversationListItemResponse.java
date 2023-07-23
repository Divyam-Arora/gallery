package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.Conversation;
import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.util.Helpers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationListItemResponse {
    private long id;
    private String name;
    private boolean isGroup;
    private int memberCount;
    private Set<UserResponse> members;
    private String iconThumbnail;

    public ConversationListItemResponse(Conversation conversation, User currentUser){
        this.id = conversation.getId();
        this.name = conversation.getName();
        this.isGroup = conversation.isGroup();
        this.memberCount = conversation.getMemberCount();
        this.members = new HashSet<>(UserResponse.getUserResponseList(conversation.getMembers().stream().filter(member -> !Objects.equals(member.getId(), currentUser.getId())).toList().stream().limit(2).collect(Collectors.toList())));
        if(conversation.getIcon() != null)
        this.iconThumbnail = Helpers.generateIconURL(conversation.getId(),"conversation") + "thumbnail";
    }
}