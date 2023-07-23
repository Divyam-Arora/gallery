package com.divyam.cloud_media_gallery.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PeopleSharedMediaResponse {
    List<UserDetailResponse> people;
    List<SharedMediaResponse> shared;
    List<MediaListItemResponse> unshared;
}