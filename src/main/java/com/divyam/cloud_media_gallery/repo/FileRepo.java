package com.divyam.cloud_media_gallery.repo;

import com.divyam.cloud_media_gallery.model.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepo extends JpaRepository<File, Long> {
}