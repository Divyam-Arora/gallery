package com.divyam.cloud_media_gallery.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EditRequest<T> {
    private List<T> toAdd;
    private List<T> toRemove;
}