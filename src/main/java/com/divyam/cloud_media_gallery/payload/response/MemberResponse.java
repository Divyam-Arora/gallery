package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponse {
    private String firstName;
    private String lastName;
    private String username;
    private String admin;

    public MemberResponse(User user, User admin){
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUsername();
        this.admin = admin.getUsername();
    }
}