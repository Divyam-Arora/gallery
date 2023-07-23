package com.divyam.cloud_media_gallery.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.divyam.cloud_media_gallery.model.User;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface UserRepo extends JpaRepository<User, Long> {
  User findByUsername(String username);

  List<User> getSharedMediaUsers(long ownerId, Pageable pageable);

  @Query(value = "select distinct u from User u where username like %?1% or firstName like %?1% or lastName like %?1%")
  List<User> findBySearch(String search, Pageable pageable);

  @Query(value = "select u.* from user as u inner join conversation_members as m on u.id = m.members_id and m.conversation_id = ?1 having u.id != ?2 order by u.username", countQuery = "select count(u.id) from user as u inner join conversation_members as m on u.id = m.members_id and m.conversation_id = ?1 having u.id != ?2 order by u.username",nativeQuery = true)
  Page<User> findMembersByConversation(long conversationId, long currentUser, Pageable pageable);

  @Query(value = "select u.username from user as u inner join conversation_members as m on u.id = m.members_id and m.conversation_id = ?1 having u.username != ?2 order by u.username", nativeQuery = true)
  Set<String> findAllMembersByConversation(long conversationId, String currentUser);

  @Query(value = "select * from user as u inner join conversation_members as m on u.id = m.members_id where m.conversation_id = ?1 having u.id != ?2 order by u.username limit 9", nativeQuery = true)
  List<User> find9MembersByConversation(long conversationId, long currentUser);

  @Query(value = "select * from user as u inner join conversation_members as m on u.id = m.members_id where m.conversation_id = ?1 having u.id != ?2 limit 1", nativeQuery = true)
  User find1MemberByConversation(long conversationId, long currentUser);

  List<User> findByUsernameIn(List<String> usernames);
}