package com.divyam.cloud_media_gallery.repo;

import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.service.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MediaRepo extends JpaRepository<Media,Long> {
    List<Media> findByOrderByIdDesc();
    Page<Media> findByOwner(User user, Pageable page);
    Page<Media> findByOwnerAndYear(User user, int year, Pageable page);
    Page<Media> findByOwnerAndYearAndMonth(User user, int year, String month, Pageable page);
    Page<Media> findByOwnerAndYearAndMonthAndDate(User user, int year, String Month, int date, Pageable page);
    List<Media> findByOwnerOrderByIdDesc(User user, Pageable page);

    @Query("select m.id from Media as m where m.id in ?1 and m.owner.id = ?2")
    List<Long> findUserMediaFromMedia(List<Long> media, Long currentUser);

    @Query("select year from Media where owner = ?1 group by year")
    List<Integer> findAllYear(User owner);

    @Query("select month from Media where owner = ?1 and year = ?2 group by month")
    List<String> findAllMonthByYear(User owner, int year);

    @Query("select date from Media where owner = ?1 and year = ?2 and month = ?3 group by date")
    List<Integer> findAllDateByYearAndMonth(User owner, int year, String month);

    @Query(value = "select m.* from media as m join File as f on m.file_id = f.id where m.owner_id = ?1 and f.content_type like %?2% and f.name regexp ?3 group by m.id order by m.updated_at desc", nativeQuery = true, countQuery = "select count(m.id) from Media as m join File as f on m.file_id = f.id where m.owner_id = ?1 and f.content_type like %?2% and f.name regexp ?3 group by m.id order by m.updated_at desc")
    Page<Media> findUserMediaByNameAndType(Long userId, String type, String search, Pageable pageable);

    @Query(value = "select m.* from media as m join file as f on m.file_id = f.id join media_tags as mt on mt.media_id = m.id join tag as t on t.id = mt.tags_id where m.owner_id = ?1 and f.content_type like %?2% and t.value regexp ?3 group by m.id order by m.updated_at desc", nativeQuery = true, countQuery = "select count(m.id) from media as m join file as f on m.file_id = f.id join media_tags as mt on mt.media_id = m.id join tag as t on t.id = mt.tags_id where m.owner_id = ?1 and f.content_type like %?2% and t.value regexp ?3 group by m.id order by m.updated_at desc")
    Page<Media> findUserMediaByTagAndType(Long userId, String type, String search, Pageable pageable);
}