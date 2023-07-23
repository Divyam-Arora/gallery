package com.divyam.cloud_media_gallery.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConversationMediaEditRequest {
    private List<Long> conversationsToAdd;
    private List<Long> conversationsToRemove;
    private List<String> peopleToAdd;
}