package com.divyam.cloud_media_gallery.service;

import com.divyam.cloud_media_gallery.exception.ResourceAccessDenied;
import com.divyam.cloud_media_gallery.exception.ResourceNotFoundException;
import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.Tag;
import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.payload.response.TagResponse;
import com.divyam.cloud_media_gallery.repo.MediaRepo;
import com.divyam.cloud_media_gallery.repo.TagRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    TagRepo tagRepo;

    @Autowired
    MediaRepo mediaRepo;

    @Autowired
    UserService userService;

    public List<String> editTags(long mediaId, List<String> tags, EditAction action){
        User currentUser = userService.loadUserFromContext();
        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media","id",mediaId));
        if(!Objects.equals(media.getOwner().getUsername(), currentUser.getUsername())){
            throw new ResourceAccessDenied("media");
        }
        for(String tag: tags){
            tag = tag.toUpperCase(Locale.ROOT);
            Tag t = new Tag();
            t.setValue(tag);
            if(action == EditAction.ADD)
                media.addTag(tagRepo.findByValue(tag).orElseGet(() -> tagRepo.save(t)));
            else
                media.removeTag(t);
        }

        mediaRepo.save(media);

        return media.getTags().stream().map(Tag::getValue).sorted().toList();
    }

    public List<String> getTags(String search){
        return tagRepo.findTop5ValueByValueStartsWithOrderByValueAsc(search.toUpperCase(Locale.ROOT)).stream().map(Tag::getValue).toList();
    }
}