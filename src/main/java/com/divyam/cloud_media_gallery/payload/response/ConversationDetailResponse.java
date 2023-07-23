package com.divyam.cloud_media_gallery.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDetailResponse {
    private ConversationResponse info;
    private List<SharedMediaResponse> media;
    private List<UserResponse> members;
}