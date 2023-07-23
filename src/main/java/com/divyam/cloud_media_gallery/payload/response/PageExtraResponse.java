package com.divyam.cloud_media_gallery.payload.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PageExtraResponse<T,S> extends PageResponse<T>{
    private S extra;

    public PageExtraResponse(int page, int totalPages, long totalElements, boolean hasNext, List<T> response, S extra){
        super(page,totalPages,totalElements, hasNext,response);
        this.extra = extra;
    }
}