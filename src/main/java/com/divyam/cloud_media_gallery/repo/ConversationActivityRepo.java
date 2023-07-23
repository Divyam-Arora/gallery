package com.divyam.cloud_media_gallery.repo;

import com.divyam.cloud_media_gallery.model.Action;
import com.divyam.cloud_media_gallery.model.Conversation;
import com.divyam.cloud_media_gallery.model.ConversationActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Repository
public interface ConversationActivityRepo extends JpaRepository<ConversationActivity, Long> {

    ConversationActivity findTop1ByConversationOrderByCreatedAtDesc(Conversation conversation);

    @Query("select a.conversation from ConversationActivity as a where a.conversation in ?1 and a.createdAt > ?2 group by a.conversation order by a.id desc")
    List<Conversation> findAllUserConversationsByLatestActivity(List<Conversation> conversations, Instant since);

    @Query("select a from ConversationActivity as a where a.conversation = ?1 and a.createdAt > ?2 order by a.id desc")
    List<ConversationActivity> findAllLatestActivityByConversation(Conversation conversation, Instant since);

    Page<ConversationActivity> findByConversationAndActionNotOrderByIdDesc(Conversation conversation, Action action, Pageable pageable);

    ConversationActivity findTop1ByConversationAndActionAndTargetIdOrderByIdDesc(Conversation conversation, Action action, Long targetId);

    @Modifying
    @Query("update ConversationActivity a set a.action = ?3 where a.action = ?2 and a.targetId = ?1")
    void updateActivityActionByActionAndTarget(long targetId, Action actionFrom, Action actionTo);
}