package com.divyam.cloud_media_gallery.service;

import com.divyam.cloud_media_gallery.repo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AllService {
    @Autowired
    SharedRepo sharedRepo;

    @Autowired
    MediaRepo mediaRepo;

    @Autowired
    FileRepo fileRepo;

    @Autowired
    ConversationRepo conversationRepo;

    @Autowired
    ConversationActivityRepo conversationActivityRepo;

    @Autowired
    AlbumRepo albumRepo;

    @Autowired
    AlbumMediaRepo albumMediaRepo;

    @Autowired
    UserRepo userRepo;

    @Transactional
    public void wipeAllClean(){
        sharedRepo.deleteAll();
        conversationActivityRepo.deleteAll();
        albumMediaRepo.deleteAll();
        albumRepo.deleteAll();
        conversationRepo.deleteAll();
        mediaRepo.deleteAll();
        userRepo.resetIcons();
        fileRepo.deleteAll();
    }

}