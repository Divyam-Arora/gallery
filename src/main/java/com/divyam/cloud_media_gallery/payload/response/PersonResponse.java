package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PersonResponse {
    private UserDetailResponse person;
    private List<SharedMediaResponse> sharedMedia;
}