package com.divyam.cloud_media_gallery.service;

import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.payload.response.MediaListItemResponse;
import com.divyam.cloud_media_gallery.payload.response.PageExtraResponse;
import com.divyam.cloud_media_gallery.repo.MediaRepo;
import com.divyam.cloud_media_gallery.util.Helpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
public class ExploreService {

    @Autowired
    UserService userService;

    @Autowired
    MediaRepo mediaRepo;

    public PageExtraResponse<MediaListItemResponse,String> exploreMedia(int page, int size, String search, String type, MediaProperty target){
        User currentUser = userService.loadUserFromContext();
        page = Helpers.calculateNextPage(page,size,0);
        Page<Media> mediaPage = null;
        String searchString = ".*" + search.replaceAll(" ", ".*|.*") + ".*";
        if(target == MediaProperty.title){
            mediaPage = mediaRepo.findUserMediaByNameAndType(currentUser.getId(), type.toLowerCase(Locale.ROOT), searchString, PageRequest.of(page,size));
        }
        if(target == MediaProperty.tag){
            mediaPage = mediaRepo.findUserMediaByTagAndType(currentUser.getId(), type.toLowerCase(Locale.ROOT), searchString, PageRequest.of(page, size));
        }


        List<MediaListItemResponse> responseList = MediaListItemResponse.getMediaListItemResponseList(mediaPage.getContent());

        return new PageExtraResponse<>(mediaPage.getNumber(), mediaPage.getTotalPages(), mediaPage.getTotalElements(), mediaPage.hasNext(), responseList, target.name());
    }
}