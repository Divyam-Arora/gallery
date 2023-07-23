package com.divyam.cloud_media_gallery.payload.response;

import com.divyam.cloud_media_gallery.model.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {

    private String id;
    private String value;

    public TagResponse(Tag tag){
        this.id =tag.getValue();
        this.value = tag.getValue();
    }

}