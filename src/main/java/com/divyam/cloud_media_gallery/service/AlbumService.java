package com.divyam.cloud_media_gallery.service;

import com.divyam.cloud_media_gallery.exception.ResourceAccessDenied;
import com.divyam.cloud_media_gallery.exception.ResourceNotFoundException;
import com.divyam.cloud_media_gallery.model.Album;
import com.divyam.cloud_media_gallery.model.AlbumMedia;
import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.payload.request.AlbumRequest;
import com.divyam.cloud_media_gallery.payload.response.*;
import com.divyam.cloud_media_gallery.repo.AlbumMediaRepo;
import com.divyam.cloud_media_gallery.repo.AlbumRepo;
import com.divyam.cloud_media_gallery.repo.MediaRepo;
import com.divyam.cloud_media_gallery.util.Constants;
import com.divyam.cloud_media_gallery.util.Helpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class AlbumService {

    @Autowired
    AlbumRepo albumRepo;

    @Autowired
    UserService userService;

    @Autowired
    MediaRepo mediaRepo;

    @Autowired
    AlbumMediaRepo albumMediaRepo;

    public AlbumListItemResponse create(AlbumRequest request) {
        Album album = new Album();
        album.setName(request.getName());
        album.setOwner(userService.loadUserFromContext());
        Album newAlbum = albumRepo.save(album);
        return generateAlbumResponse(newAlbum);
    }

    public PageResponse<AlbumListItemResponse> loadAllAlbums(int page, int size, int extra, String search) {
        User user = userService.loadUserFromContext();
        page = Helpers.calculateNextPage(page, size, extra);
        PageRequest p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Album> albumPage = albumRepo.findByOwnerAndNameContainingOrOwnerAndDescriptionContainingOrderByUpdatedAtDesc(user, search,user,search, p);
        List<Album> albumList = albumPage.getContent();
        List<AlbumListItemResponse> customAlbums = new ArrayList<>();
        for (Album album : albumList) {
            AlbumListItemResponse customAlbum = generateAlbumResponse(album);
            customAlbums.add(customAlbum);
        }

        return new PageResponse<>(albumPage.getNumber(), albumPage.getTotalPages(), albumPage.getTotalElements(), albumPage.hasNext(), customAlbums.subList(Helpers.getNewPageStartIndex(customAlbums, size, extra), customAlbums.size()));
    }

    public AlbumListItemResponse generateAlbumResponse(Album album) {
        AlbumListItemResponse customAlbum = new AlbumListItemResponse();
        customAlbum.setId(album.getId());
        customAlbum.setName(album.getName());
        customAlbum.setDescription(album.getDescription());
        customAlbum.setCreatedAt(album.getCreatedAt());
        int count = album.getMedia().size();
        customAlbum.setMediaCount(count);
        customAlbum.setThumbnail(count > 0 ? Helpers.generateMediaURL(album.getMedia().get(0).getMedia()) + Constants.URL_THUMBNAIL_EXTENSION : null);
        List<String> thumbnails = new ArrayList<>();
        for (int i = 0; i < Math.min(3, count); i++) {
            thumbnails.add(Helpers.generateMediaURL(album.getMedia().get(i).getMedia()) + Constants.URL_THUMBNAIL_EXTENSION);
        }
        customAlbum.setThumbnails(thumbnails);
        return customAlbum;
    }

    public AlbumResponse loadAlbum(long albumId) {
        User user = userService.loadUserFromContext();
        Album album = albumRepo.findById(albumId).orElseThrow(() -> new ResourceNotFoundException("Album", "id", albumId));
        if (!Objects.equals(user.getId(), album.getOwner().getId())) {
            throw new ResourceAccessDenied("Album");
        }
        AlbumResponse response = new AlbumResponse();
        response.setId(albumId);
        response.setName(album.getName());
        response.setDescription(album.getDescription());
        response.setDate(album.getCreatedAt());

        return response;
    }

    public PageResponse<MediaListItemResponse> loadAlbumMedia(long albumId, int page, int size, int extra, String search){
        User user = userService.loadUserFromContext();
        Album album = albumRepo.findById(albumId).orElseThrow(() -> new ResourceNotFoundException("Album", "id", albumId));
        if (!Objects.equals(user.getId(), album.getOwner().getId())) {
            throw new ResourceAccessDenied("Album");
        }
        String searchString = Helpers.getSearchRegexpString(search);

        page = Helpers.calculateNextPage(page, size, extra);
        Pageable p = PageRequest.of(page,size,Sort.by("id").descending());
        Page<AlbumMedia> mediaPage = albumMediaRepo.findByAlbumAndName(album.getId(),searchString, p);
        List<MediaListItemResponse> customMediaList = new ArrayList<>();
        for (AlbumMedia albumMedia : mediaPage.getContent()) {
            MediaListItemResponse mediaListItemResponse = new MediaListItemResponse(albumMedia.getMedia());
            customMediaList.add(mediaListItemResponse);
        }

        return new PageResponse<>(mediaPage.getNumber(),mediaPage.getTotalPages(), mediaPage.getTotalElements(), mediaPage.hasNext(), customMediaList.subList(Helpers.getNewPageStartIndex(customMediaList, size, extra), customMediaList.size()));
    }

    public List<Long> loadAllAlbumMedia(long albumId){
        User user = userService.loadUserFromContext();
        Album album = albumRepo.findById(albumId).orElseThrow(() -> new ResourceNotFoundException("Album", "id", albumId));
        if (!Objects.equals(user.getId(), album.getOwner().getId())) {
            throw new ResourceAccessDenied("Album");
        }

//        page = Helpers.calculateNextPage(page, size, extra);
//        Pageable p = PageRequest.of(page,size,Sort.by("id").descending());
//        Page<AlbumMedia> mediaPage = albumMediaRepo.findByAlbum(album, p);
//        List<MediaListItemResponse> customMediaList = new ArrayList<>();
//        for (AlbumMedia albumMedia : mediaPage.getContent()) {
//            MediaListItemResponse mediaListItemResponse = new MediaListItemResponse(albumMedia.getMedia());
//            customMediaList.add(mediaListItemResponse);
//        }

        return albumMediaRepo.findIdByAlbum(album.getId());
    }

    public AlbumListItemResponse loadAlbumListResponse(long albumId) {
        User user = userService.loadUserFromContext();
        Album album = albumRepo.findById(albumId).orElseThrow(() -> new ResourceNotFoundException("Album", "id", albumId));
        if (!Objects.equals(user.getId(), album.getOwner().getId())) {
            throw new ResourceAccessDenied("Album");
        }
        return generateAlbumResponse(album);
    }

    public AlbumListItemResponse editAlbum(long albumId, AlbumRequest request) {
        User user = userService.loadUserFromContext();
        Album album = albumRepo.findById(albumId).orElseThrow(() -> new ResourceNotFoundException("Album", "id", albumId));
        if (!Objects.equals(user.getId(), album.getOwner().getId())) {
            throw new ResourceAccessDenied("Album");
        }
        album.setName(request.getName());
        album.setDescription(request.getDescription());
        return generateAlbumResponse(albumRepo.save(album));
    }

    public void addMedia(long albumId, List<Long> toAdd) {
        User user = userService.loadUserFromContext();
        AtomicBoolean isChanged = new AtomicBoolean(false);
        Album album = albumRepo.findById(albumId).orElseThrow(() -> new ResourceNotFoundException("Album", "id", albumId));
        if (!Objects.equals(album.getOwner().getId(), user.getId())) {
            throw new ResourceAccessDenied("Album");
        }
        toAdd.forEach(id -> {
            Media media = mediaRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Media", "id", id));
            if (!Objects.equals(media.getOwner().getId(), user.getId())) {
                throw new ResourceAccessDenied("Media");
            }
            AlbumMedia old = albumMediaRepo.findByAlbumAndMedia(album, media);
            if (old == null) {
                AlbumMedia albumMedia = new AlbumMedia();
                albumMedia.setAlbum(album);
                albumMedia.setMedia(media);
                albumMediaRepo.save(albumMedia);
                isChanged.set(true);
            }
        });

        if(isChanged.get()){
            album.setUpdatedAt(Instant.now());
            albumRepo.save(album);
        }
    }

    public void removeMedia(long albumId, List<Long> toRemove) {
        User user = userService.loadUserFromContext();
        Album album = albumRepo.findById(albumId).orElseThrow(() -> new ResourceNotFoundException("Album", "id", albumId));
        if (!Objects.equals(album.getOwner().getId(), user.getId())) {
            throw new ResourceAccessDenied("Album");
        }
        toRemove.forEach(id -> {
            Media media = mediaRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Media", "id", id));
            AlbumMedia old = albumMediaRepo.findByAlbumAndMedia(album, media);
            if (old != null) {
                albumMediaRepo.delete(old);
            }
        });
    }

    public Media addMediaToAlbums(long mediaId, List<Long> albums){
        User user = userService.loadUserFromContext();
        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("Media", "id", mediaId));
        if (!Objects.equals(media.getOwner().getId(), user.getId())) {
            throw new ResourceAccessDenied("Media");
        }
        albums.forEach(albumId -> {
            Album album = albumRepo.findById(albumId).orElseThrow(() -> new ResourceNotFoundException("Album", "id", albumId));
            if (!Objects.equals(album.getOwner().getId(), user.getId())) {
                throw new ResourceAccessDenied("Album");
            }
            AlbumMedia old = albumMediaRepo.findByAlbumAndMedia(album, media);
            if (old == null) {
                AlbumMedia albumMedia = new AlbumMedia();
                albumMedia.setAlbum(album);
                albumMedia.setMedia(media);
                albumMediaRepo.save(albumMedia);
                album.setUpdatedAt(Instant.now());
                albumRepo.save(album);
            }
        });

        return media;
    }

    public Media removeMediaFromAlbums(long mediaId, List<Long> albums){
        User user = userService.loadUserFromContext();
        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("Media", "id", mediaId));
        if (!Objects.equals(media.getOwner().getId(), user.getId())) {
            throw new ResourceAccessDenied("Media");
        }
        albums.forEach(albumId -> {
            Album album = albumRepo.findById(albumId).orElseThrow(() -> new ResourceNotFoundException("Album", "id", albumId));
            if (!Objects.equals(album.getOwner().getId(), user.getId())) {
                throw new ResourceAccessDenied("Album");
            }
            AlbumMedia old = albumMediaRepo.findByAlbumAndMedia(album, media);
            if (old != null) {
                albumMediaRepo.delete(old);
            }
        });

        return media;
    }

    public Long deleteAlbum(long albumId) {
        User user = userService.loadUserFromContext();
        Album album = albumRepo.findById(albumId).orElseThrow(() -> new ResourceNotFoundException("Album", "id", albumId));
        if (!Objects.equals(user.getId(), album.getOwner().getId())) {
            throw new ResourceAccessDenied("Album");
        }
        albumMediaRepo.deleteAll(album.getMedia());
        albumRepo.delete(album);

        return albumId;
    }
}