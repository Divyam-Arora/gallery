package com.divyam.cloud_media_gallery.repo;

import com.divyam.cloud_media_gallery.model.Album;
import com.divyam.cloud_media_gallery.model.AlbumMedia;
import com.divyam.cloud_media_gallery.model.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumMediaRepo extends JpaRepository<AlbumMedia, Long> {
    AlbumMedia findByAlbumAndMedia(Album album, Media media);
    List<AlbumMedia> findTop6ByMediaOrderByUpdatedAtDesc(Media media);
    List<AlbumMedia> findByMediaOrderByUpdatedAtDesc(Media media);

    @Query("select am.album.id from AlbumMedia as am where am.media = ?1")
    List<Long> findAllByMedia(Media media);
    Page<AlbumMedia> findByAlbum(Album album, Pageable pageable);

    @Query(value = "select media_id from album_media where album_id=?1", nativeQuery = true)
    List<Long> findIdByAlbum(Long albumId);

    @Query(value = "select distinct am.id, am.created_at, am.updated_at, am.album_id, am.media_id from album_media am join media m on m.id = am.media_id join file f on m.file_id = f.id left join media_tags as mt on mt.media_id = m.id left join tag as t on t.id = mt.tags_id where album_id = ?1 and (f.name regexp ?2 or t.value regexp ?2)", countQuery = "select count(distinct am.id) from album_media am join media m on m.id = am.media_id join file f on m.file_id = f.id left join media_tags as mt on mt.media_id = m.id left join tag as t on t.id = mt.tags_id where album_id = ?1 and (f.name regexp ?2 or t.value regexp ?2)", nativeQuery = true)
    Page<AlbumMedia> findByAlbumAndName(long albumId, String name, Pageable pageable);

    @Query(value = "select am.id, am.created_at, am.updated_at, am.album_id, am.media_id from album_media am join media m on m.id where album_id = ?1 and name like %?2%", nativeQuery = true)
    List<AlbumMedia> findByAlbumAndName1(long albumId, String name);

    @Query(value = "delete from album_media where media_id = ?1", nativeQuery = true)
    @Modifying
    void deleteByMedia(long mediaId);
//    List<AlbumMedia> findByAlbumIdAndMediaIdIn(longList<long> media)
}