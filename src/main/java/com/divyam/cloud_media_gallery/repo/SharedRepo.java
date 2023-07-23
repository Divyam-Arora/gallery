package com.divyam.cloud_media_gallery.repo;

import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.Conversation;
import com.divyam.cloud_media_gallery.model.Shared;
import com.divyam.cloud_media_gallery.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SharedRepo extends JpaRepository<Shared, Long> {

    @Query(value = "select sh.* from shared as sh inner join media on sh.media_id = media.id join file as f on f.id = media.file_id left join media_tags as mt on mt.media_id = media.id left join tag as t on t.id = mt.tags_id where sh.conversation_id in ?4 and (f.name regexp ?3 or t.value regexp ?3) and media.owner_id in (?1,?2) group by media.id order by sh.id desc", countQuery = "select sh.* from shared as sh inner join media on sh.media_id = media.id join file as f on f.id = media.file_id left join media_tags as mt on mt.media_id = media.id left join tag as t on t.id = mt.tags_id where sh.conversation_id in ?4 and (f.name regexp ?3 or t.value regexp ?3) and media.owner_id in (?1,?2) group by media.id order by sh.id desc", nativeQuery = true)
    Page<Shared> findByOwnerAndSharedTo(long currentUser, long person, String search, List<Long> conversations, Pageable pageable);

    @Query(value = "select count(distinct media.id) from shared sh inner join media on sh.media_id = media.id where sh.conversation_id in (select c.id from conversation c inner join conversation_members m on c.id = m.conversation_id and m.members_id in (?1,?2) where c.member_count >= 2 group by c.id having count(*) >= 2) and media.owner_id in (?1,?2)", nativeQuery = true)
    int findSharedMediaCount(long currentUser, long person);

    Shared findByMediaAndConversation(Media media, Conversation conversation);

    List<Shared> findTop8ByConversationOrderByIdDesc(Conversation conversation);
    Shared findTop1ByConversationOrderByIdDesc(Conversation conversation);

    @Query(value = "select sh.* from shared as sh join media as m on m.id = sh.media_id join file as f on f.id = m.file_id left join media_tags as mt on mt.media_id = m.id left join tag as t on t.id = mt.tags_id where sh.conversation_id = ?1 and (f.name regexp ?2 or t.value regexp ?2) group by sh.id order by sh.id desc",countQuery = "select count(sh.id) from shared as sh join media as m on m.id = sh.media_id join file as f on f.id = m.file_id left join media_tags as mt on mt.media_id = m.id left join tag as t on t.id = mt.tags_id where sh.conversation_id = ?1 and (f.name regexp ?2 or t.value regexp ?2) group by sh.id order by sh.id desc",nativeQuery = true)
    Page<Shared> findByConversationOrderByIdDesc(long conversationId, String search,Pageable pageable);

    @Query("select s from Shared s where s.conversation = ?1 and s.createdAt > ?2")
    List<Shared> findRecentByConversation(Conversation conversation, Instant date);

    @Query(value = "select media.* from shared sh inner join media on sh.media_id = media.id where sh.conversation_id in (select c.id from conversation c inner join conversation_members m on c.id = m.conversation_id and m.members_id in (?2,?3) where c.member_count >= 2 group by c.id having count(*) >= 2) and media.id = ?1 limit 1", nativeQuery = true)
    Object findByMediaAndUserAndOwnerConversation(Long mediaId, long currentUserId, long ownerId);

    List<Shared> findTop6ByMediaOrderByCreatedAtDesc(Media media);
    List<Shared> findByMediaOrderByCreatedAtDesc(Media media);

    @Query("select s.conversation.id from Shared as s where s.media = ?1 order by s.createdAt desc")
    List<Long> findAllConversationsByMedia(Media media);

    @Query(value = "select media.id from shared sh inner join media on sh.media_id = media.id where sh.conversation_id in (select c.id from conversation c inner join conversation_members m on c.id = m.conversation_id and m.members_id in (?1,?2) where c.member_count = 2 group by c.id having count(*) = 2) and media.owner_id in (?1)", nativeQuery = true)
    List<Long> findAllMediaByPeopleAndOwner(long currentUser, long shareTo);

    @Query(value = "select distinct m.id from Shared sh inner join Media m on m = sh.media where sh.conversation = ?1 and m.owner = ?2")
    List<Long> findAllMediaByConversation(Conversation conversation, User currentUser);

    @Query(value = "select count(distinct m.id) from Shared sh inner join Media m on m = sh.media where sh.conversation = ?1 and m.owner = ?2")
    long findAllMediaCountByConversation(Conversation conversation, User currentUser);

    @Modifying
    @Query(value = "delete from shared where media_id = ?1", nativeQuery = true)
    void deleteByMedia(long mediaId);

}