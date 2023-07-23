package com.divyam.cloud_media_gallery.payload.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaDate {
    private Integer date;
    private String month;
    private Integer year;
}