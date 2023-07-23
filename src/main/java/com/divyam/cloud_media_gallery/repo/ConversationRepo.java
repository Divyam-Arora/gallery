package com.divyam.cloud_media_gallery.repo;

import com.divyam.cloud_media_gallery.model.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Repository
public interface ConversationRepo extends JpaRepository<Conversation, Long> {

    @Query(value = "select c.* from conversation as c inner join conversation_members as m on c.id = m.conversation_id and m.members_id in ?1 where c.member_count = ?2 group by c.id having count(*) = ?2", nativeQuery = true)
    Conversation findConversationByMembers(List<Long> members, int count);

    @Query(value = "select c.* from conversation as c inner join conversation_members as m on c.id = m.conversation_id inner join User as u on m.members_id = u.id and u.username in ?1 and c.is_group = ?3 where c.member_count = ?2 group by c.id having count(*) = ?2 limit 1", nativeQuery = true)
    Conversation findConversationByUsernames(Set<String> members, int count, boolean isGroup);

    @Query(value = "select c.* from conversation as c inner join conversation_members as m on c.id = m.conversation_id and m.members_id in ?1 where c.member_count >= ?2 group by c.id having count(*) = ?2", countQuery = "select count(c.*) from conversation as c inner join conversation_members as m on c.id = m.conversation_id and m.members_id in ?1 where c.member_count >= ?2 group by c.id having count(*) = ?2",nativeQuery = true)
    Page<Conversation> findConversationsByMembers(List<Long> members, int count, Pageable pageable);

    @Query(value = "select c.id from conversation as c inner join conversation_members as m on c.id = m.conversation_id and m.members_id in ?1 where c.member_count >= ?2 group by c.id having count(*) = ?2", nativeQuery = true)
    List<Long> findAllConversationByMembers(List<Long> members, int count);

    @Query(value = "select c.* from conversation as c inner join conversation_members as m on c.id = m.conversation_id and m.members_id in (?1 , ?2) where c.member_count >= 2 and c.is_group = true group by c.id having count(*) = 2 order by c.updated_at desc", countQuery = "select c.* from conversation as c inner join conversation_members as m on c.id = m.conversation_id and m.members_id in (?1 , ?2) where c.member_count >= 2 and c.is_group = true group by c.id having count(*) = 2 order by c.updated_at desc",nativeQuery = true)
    Page<Conversation> findSharedGroupByMember(long currentUser, long user, Pageable pageable);

    @Query(value = "select count(*) from conversation where id in (select c.id from conversation as c inner join conversation_members as m on c.id = m.conversation_id and m.members_id in (?1 , ?2) where c.member_count >= 2 and c.is_group = true group by c.id having count(*) = 2)", nativeQuery = true)
    Integer findSharedGroupCountByMember(long currentUser, long user);

    @Query(value = "select c.* from conversation as c inner join conversation_members as m on c.id = m.conversation_id inner join user as u on u.id = m.members_id where c.id in (select c.id from conversation as c inner join conversation_members as m on c.id = m.conversation_id where m.members_id = ?1 and c.member_count >= 1 group by c.id having count(*) >= 1) and (c.name regexp ?2 or u.username regexp ?2 or u.first_name regexp ?2) group by c.id having count(*) >= 1 order by c.updated_at desc", countQuery = "select count(c.id) from conversation as c inner join conversation_members as m on c.id = m.conversation_id inner join user as u on u.id = m.members_id where c.id in (select c.id from conversation as c inner join conversation_members as m on c.id = m.conversation_id where m.members_id = ?1 and c.member_count >= 1 group by c.id having count(*) >= 1) and (c.name regexp ?2 or u.username regexp ?2 or u.first_name regexp ?2) group by c.id having count(*) >= 1 order by c.updated_at desc",nativeQuery = true)
    Page<Conversation> findConversationsByMember(long userId, String search, Pageable pageable);

    @Query(value = "select c.* from conversation as c inner join conversation_members as m on c.id = m.conversation_id where m.members_id = ?1 and c.member_count >= 1 group by c.id having count(*) >= 1", nativeQuery = true)
    List<Conversation> findAllConversationByMember(long userId);

    @Query(value = "select count(*) from shared where conversation_id in ?1 and media_id = ?2", nativeQuery = true)
    Long findConversationCountBySharedMedia(List<Long> conversations, Long mediaId);

    @Query(value = "select c.* from shared as sh inner join conversation as c on sh.conversation_id = c.id where conversation_id in ?1 and media_id = ?2",countQuery = "select count(c.id) from shared as sh inner join conversation as c on sh.conversation_id = c.id where conversation_id in ?1 and media_id = ?2",nativeQuery = true)
    Page<Conversation> findConversationsBySharedMedia(List<Long> conversations, Long mediaId, Pageable pageable);

    @Query(value = "select c.* from conversation as c inner join conversation_members as m on c.id = m.conversation_id inner join user as u on u.id = m.members_id where c.id in (select c.id from conversation as c inner join conversation_members as m on c.id = m.conversation_id where m.members_id = ?1 and c.member_count >= 1 and c.updated_at > ?3 group by c.id having count(*) >= 1) and (c.name like ?2% or u.username like ?2% or u.first_name like ?2%) group by c.id having count(*) >= 1 order by c.updated_at desc", nativeQuery = true)
    List<Conversation> findRecentConversationByMember(long userId, String search, Instant date);

    @Query(value = "select c.* from conversation as c inner join conversation_members as m on c.id = m.conversation_id where m.members_id = ?2 and c.id = ?1 group by c.id", nativeQuery = true)
    Conversation findByConversationAndMember(long conversationID, long memberId);

    @Query(value = "select c.* from conversation as c inner join conversation_members as m on c.id = m.conversation_id where m.members_id = ?2 and c.id in ?1 group by c.id", nativeQuery = true)
    List<Long> findUserConversationsFromConversations(List<Long> conversations, Long currentUser);

}