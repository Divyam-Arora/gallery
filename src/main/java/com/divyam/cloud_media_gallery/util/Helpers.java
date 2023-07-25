package com.divyam.cloud_media_gallery.util;

import com.divyam.cloud_media_gallery.model.Media;
import lombok.NoArgsConstructor;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@NoArgsConstructor
public class Helpers {

    public static String generateMediaURL(Media media){
        String url;
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().scheme("https").build().toUriString();
        url = baseUrl + "/api/public/media/" + media.getId() + "/";

        return url;
    }

    public static String generateIconURL(Object id, String type){
        String url;
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().scheme("https").build().toUriString();
        url = baseUrl + "/api/public/" + type + "/" + id + "/";

        return url;
    }

    public static int calculateNextPage(int page, int size, int extra){
        return Math.max(0,page + Math.floorDiv(extra, size));
    }

    public static int getNewPageStartIndex(List<?> list, int size, int extra){
        int startIndex=0;
        if(extra < 0){
            startIndex = Math.min(list.size(), size + (extra % size));
            if(Math.abs(extra % size) == 0){
                startIndex = 0;
            }
        } else{
            startIndex = Math.min(extra % size,list.size());
        }
        return startIndex;
    }

    public static String getSearchRegexpString(String search){
        return ".*" + search.replaceAll(" ", ".*|.*") + ".*";
    }
}