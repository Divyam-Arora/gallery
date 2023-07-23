package com.divyam.cloud_media_gallery.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MembersRequest {
    Set<String> toAdd;
    Set<String> toRemove;
}