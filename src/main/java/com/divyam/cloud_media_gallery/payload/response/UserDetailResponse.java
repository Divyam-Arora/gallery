package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.util.Helpers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDetailResponse {
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Instant since;
    private int sharedMediaCount;
    private int sharedGroupCount;
    private String iconThumbnail;
    private String iconURL;

    public UserDetailResponse(User user, int sharedMediaCount, int sharedGroupCount){
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUsername();
        this.since = user.getCreatedAt();
        this.sharedMediaCount = sharedMediaCount;
        this.sharedGroupCount = sharedGroupCount;
        if(user.getIcon() != null){
        this.iconThumbnail = Helpers.generateIconURL(user.getUsername(), "user") + "thumbnail?id=" + user.getIcon().getId();
        this.iconURL = Helpers.generateIconURL(user.getUsername(), "user");
        }
    }
}