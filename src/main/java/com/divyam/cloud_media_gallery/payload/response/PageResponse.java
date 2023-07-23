package com.divyam.cloud_media_gallery.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PageResponse<T> {
    private int page;
    private int totalPages;
    private long totalElements;
    private boolean hasNext;
    private List<T> response;
}