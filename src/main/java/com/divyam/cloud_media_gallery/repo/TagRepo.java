package com.divyam.cloud_media_gallery.repo;

import com.divyam.cloud_media_gallery.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepo extends JpaRepository<Tag,Long> {
    Optional<Tag> findByValue(String value);
    List<Tag> findTop5ValueByValueStartsWithOrderByValueAsc(String value);
}