package com.divyam.cloud_media_gallery.repo;

import com.divyam.cloud_media_gallery.model.Album;
import com.divyam.cloud_media_gallery.model.AlbumMedia;
import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepo extends JpaRepository<Album, Long> {
    List<Album> findByOrderByIdDesc();
    List<Album> findByOwnerOrderByIdDesc(User user);
    Page<Album> findByOwner(User user, Pageable pageable);
    Page<Album> findByOwnerAndNameContainingOrOwnerAndDescriptionContainingOrderByUpdatedAtDesc(User aUser, String name, User bUser, String description,Pageable pageable);

}