package com.divyam.cloud_media_gallery.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationActivityResponse {
    private ConversationResponse conversation;
    private List<ActivityDetailResponse> activities;
}