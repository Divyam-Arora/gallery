package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.util.Helpers;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String iconURL;
    private String iconThumbnail;

    public UserResponse(User user){
        this.id = user.getUsername();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.username = user.getUsername();
        this.email = user.getEmail();
        if(user.getIcon() != null){
        this.iconURL = Helpers.generateIconURL(user.getUsername(),"user") + "icon?id=" + user.getIcon().getId();
        this.iconThumbnail = Helpers.generateIconURL(user.getUsername(), "user") + "thumbnail?id=" + user.getIcon().getId();
        }
    }

    public static List<UserResponse> getUserResponseList(List<User> users){
        return users.stream().map(UserResponse::new).collect(Collectors.toList());
    }
}