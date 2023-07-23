package com.divyam.cloud_media_gallery.service;

import com.divyam.cloud_media_gallery.exception.NotAllowed;
import com.divyam.cloud_media_gallery.exception.ResourceAccessDenied;
import com.divyam.cloud_media_gallery.exception.ResourceNotFoundException;
import com.divyam.cloud_media_gallery.exception.UserNotFound;
import com.divyam.cloud_media_gallery.model.*;
import com.divyam.cloud_media_gallery.payload.request.ConversationMediaEditRequest;
import com.divyam.cloud_media_gallery.payload.response.ActivityDetailResponse;
import com.divyam.cloud_media_gallery.payload.response.MediaListItemResponse;
import com.divyam.cloud_media_gallery.payload.response.MediaResponse;
import com.divyam.cloud_media_gallery.payload.response.SharedMediaResponse;
import com.divyam.cloud_media_gallery.repo.ConversationActivityRepo;
import com.divyam.cloud_media_gallery.repo.MediaRepo;
import com.divyam.cloud_media_gallery.repo.ConversationRepo;
import com.divyam.cloud_media_gallery.repo.SharedRepo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ShareService {

    @Autowired
    SharedRepo sharedRepo;

    @Autowired
    UserService userService;

    @Autowired
    MediaRepo mediaRepo;

    @Autowired
    ConversationActivityRepo conversationActivityRepo;

    @Autowired
    ConversationRepo conversationRepo;

    public Conversation createConversation(List<User> members, boolean isGroup, String name){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = new Conversation();
        conversation.setAdmin(currentUser);
        conversation.setCreatedBy(currentUser);
        conversation.setGroup(isGroup);
        conversation.setMediaCount(0);
        conversation.setName(name);
        conversation.setMembers(new HashSet<>(members));
        conversation.setMemberCount(members.size());
        conversationRepo.saveAndFlush(conversation);
        conversationActivityRepo.save(new ConversationActivity(conversation, Action.Created, currentUser, conversation.getId(), null));
        return conversation;
    }

    public void updateConversation(Conversation conversation){
        conversationRepo.save(conversation);
    }

    public Conversation getOrCreateConversation(List<String> p){
        List<String> people  = new ArrayList<>(p);
        User currentUser = userService.loadUserFromContext();
        List<User> members = new ArrayList<>();
        List<Long> memberIds = new ArrayList<>();
        for(String username : people){
            User user = userService.getUser(username);
            if(user == null){
                throw new UserNotFound(username);
            } else{
                members.add(user);
                memberIds.add(user.getId());
            }
        }
        people.add(currentUser.getUsername());
        Conversation conversation = conversationRepo.findConversationByMembers(memberIds, memberIds.size());
        if(conversation == null){
            return createConversation(members, true, null);
        }

        return conversation;
    }

    public Conversation getOrCreateConversation(String username){
        User currentUser = userService.loadUserFromContext();
        User user = userService.getUser(username);
        if(user == null)
            throw new UserNotFound(username);

        Conversation conversation = conversationRepo.findConversationByMembers(List.of(currentUser.getId(), user.getId()), 2);

        if(conversation == null || conversation.isGroup()){
            List<User> members = new ArrayList<>();
            members.add(currentUser);
            members.add(user);
            return createConversation(members, false, user.getUsername());
        }

        return conversation;
    }

    public List<SharedMediaResponse> shareWithPeople(List<Long> media, List<String> people){
        User currentUser = userService.loadUserFromContext();
        List<SharedMediaResponse> mediaList = new ArrayList<>();
        for (String username: people){
            User shareTo = userService.getUser(username);
            if(Objects.equals(shareTo.getId(), currentUser.getId())) throw  new NotAllowed("Sharing with yourself");
            Conversation conversation = getOrCreateConversation(username);
            int count = 0;
            for(Long mediaId : media){
                Media m = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media","id",mediaId));
                if(!Objects.equals(m.getOwner().getId(), currentUser.getId())) throw new ResourceAccessDenied("media");
                SharedObject sharedObject =  editMediaInConversation(m, conversation, currentUser, EditAction.ADD);
                if(sharedObject.getShared() != null){
                    mediaList.add(new SharedMediaResponse(m, sharedObject.getShared().getCreatedAt(), null));
                    count++;
                }
            }
            conversation.setMediaCount(conversation.getMediaCount() + count);
            System.out.println("update conversation " + conversation.getId());
//            conversation.setUpdatedAt(Instant.now());
            updateConversation(conversation);
        }
        return mediaList;
    }

    public List<MediaListItemResponse> unShareWithPeople(List<Long> media, List<String> people){
        User currentUser = userService.loadUserFromContext();
        List<MediaListItemResponse> mediaList = new ArrayList<>();
        for (String username: people){
            User shareTo = userService.getUser(username);
            if(shareTo == null) throw new UserNotFound(username);
            Conversation conversation = conversationRepo.findConversationByMembers(List.of(currentUser.getId(), shareTo.getId()), 2);
            int count = 0;

            if(conversation == null){
                throw new ResourceNotFoundException("Conversation","member",shareTo.getUsername());
            }

            for(Long mediaId: media){
                Media m = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media","id",mediaId));
                if(editMediaInConversation(m, conversation, currentUser, EditAction.REMOVE).getShared() != null){
                    mediaList.add(new MediaListItemResponse(m));
                    count++;
                }
            }
            conversation.setMediaCount(conversation.getMediaCount() - count);
            updateConversation(conversation);
        }
        return mediaList;
    }

    public List<ActivityDetailResponse<SharedMediaResponse>> shareWithConversation(Long conversationId, List<Long> media){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        Conversation userConversation = conversationRepo.findByConversationAndMember(conversationId,currentUser.getId());
        if(userConversation == null){
            throw new ResourceAccessDenied("Conversation");
        }
        int count = 0;
        List<ActivityDetailResponse<SharedMediaResponse>> activityList = new ArrayList<>();

        for(Long mediaId : media){
            Media m = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media","id",mediaId));
            if(!Objects.equals(m.getOwner().getId(), currentUser.getId())) throw new ResourceAccessDenied("media");
            SharedObject sharedObject = editMediaInConversation(m, conversation, currentUser, EditAction.ADD);
            if(sharedObject.getShared() != null) {
                activityList.add(new ActivityDetailResponse<SharedMediaResponse>(sharedObject.getActivity(), new SharedMediaResponse(m, sharedObject.getActivity().getCreatedAt(), null)));
                count++;
            }
        }
        userConversation.setMediaCount(userConversation.getMediaCount() + count);
        updateConversation(userConversation);
        Collections.reverse(activityList);
        return activityList;
    }

    public List<ActivityDetailResponse<ActivityDetailResponse<Long>>> unShareWithConversation(long conversationId, List<Long> media) {
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
        Conversation userConversation = conversationRepo.findByConversationAndMember(conversationId, currentUser.getId());
        if (userConversation == null) {
            throw new ResourceAccessDenied("Conversation");
        }
        int count = 0;
        List<ActivityDetailResponse<ActivityDetailResponse<Long>>> activityList = new ArrayList<>();

        for (Long mediaId : media) {
            Media m = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media", "id", mediaId));
            if (!Objects.equals(m.getOwner().getId(), currentUser.getId())) throw new ResourceAccessDenied("media");
            SharedObject sharedObject = editMediaInConversation(m, conversation, currentUser, EditAction.REMOVE);
            if (sharedObject.getShared() != null) {
                activityList.add(new ActivityDetailResponse<ActivityDetailResponse<Long>>(conversationActivityRepo.save(new ConversationActivity(conversation, Action.UnShared_Activity, currentUser, sharedObject.getActivity().getId(), null)), new ActivityDetailResponse<Long>(sharedObject.getActivity(), sharedObject.getActivity().getTargetId())));
                count++;
            }
        }
        userConversation.setMediaCount(userConversation.getMediaCount() - count);
        updateConversation(userConversation);

        return activityList;
    }

    public Media shareMedia(long mediaId, ConversationMediaEditRequest request){
        User currentUser = userService.loadUserFromContext();
        Media media = mediaRepo.findById(mediaId).orElse(null);
        for(Long conversationId: request.getConversationsToAdd()){
            Conversation conversation = conversationRepo.findById(conversationId).orElse(null);
            if(editMediaInConversation(media,conversation,currentUser,EditAction.ADD).getShared() != null){
                conversation.setMediaCount(conversation.getMediaCount() + 1);
                updateConversation(conversation);
            }
        }
        for(Long conversationId: request.getConversationsToRemove()){
            Conversation conversation = conversationRepo.findById(conversationId).orElse(null);
            if(editMediaInConversation(media,conversation,currentUser,EditAction.REMOVE).getShared() != null){
                conversation.setMediaCount(conversation.getMediaCount() - 1);
                updateConversation(conversation);
            }
        }
        for(String username: request.getPeopleToAdd()){
            Conversation conversation = getOrCreateConversation(username);
            if(editMediaInConversation(media,conversation,currentUser,EditAction.ADD).getShared() != null){
                conversation.setMediaCount(conversation.getMediaCount() + 1);
                updateConversation(conversation);
            }
        }
        return media;
    }

//    public Media unShareMediaFromConversations(long mediaId, List<Long> conversations){
//        User currentUser = userService.loadUserFromContext();
//        Media media = mediaRepo.findById(mediaId).orElse(null);
//        for(Long conversationId: conversations){
//            Conversation conversation = conversationRepo.findById(conversationId).orElse(null);
//            if(editMediaInConversation(media,conversation,currentUser,EditAction.REMOVE).getShared() != null){
//                conversation.setMediaCount(conversation.getMediaCount() - 1);
//                updateConversation(conversation);
//            }
//        }
//        return media;
//    }
//
//    public Media shareMediaToPeople(long mediaId, List<String> people ){
//        User currentUser = userService.loadUserFromContext();
//        Media media = mediaRepo.findById(mediaId).orElse(null);
//        for(String username: people){
//            Conversation conversation = getOrCreateConversation(username);
//            if(editMediaInConversation(media,conversation,currentUser,EditAction.ADD).getShared() != null){
//                conversation.setMediaCount(conversation.getMediaCount() - 1);
//                updateConversation(conversation);
//            }
//        }
//        return media;
//    }

    public List<Long> getPersonAllSharedMedia(String username){
        User currentUser = userService.loadUserFromContext();
        User person = userService.getUser(username);
        if(person == null) throw new UserNotFound(username);

        return sharedRepo.findAllMediaByPeopleAndOwner(currentUser.getId(), person.getId());
    }

    public SharedObject editMediaInConversation(Media media, Conversation conversation, User currentUser, EditAction action){
        SharedObject sharedObject = new SharedObject();
        Shared shared = sharedRepo.findByMediaAndConversation(media, conversation);
        if(shared == null && action == EditAction.ADD) {
            Shared sh = new Shared();
            sh.setMedia(media);
            sh.setConversation(conversation);
            sharedRepo.save(sh);
            ConversationActivity activity = new ConversationActivity(conversation, Action.Shared, currentUser, media.getId(), null);
            conversationActivityRepo.save(activity);
            sharedObject.setShared(sh);
            sharedObject.setActivity(activity);
        } else if(shared != null && action == EditAction.REMOVE){
            ConversationActivity activity = conversationActivityRepo.findTop1ByConversationAndActionAndTargetIdOrderByIdDesc(conversation,Action.Shared, media.getId());
            activity.setAction(Action.UnShared);
            sharedRepo.delete(shared);
            conversationActivityRepo.save(activity);
            sharedObject.setShared(shared);
            sharedObject.setActivity(activity);
        }
        return sharedObject;
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class SharedObject{
    private Shared shared;
    private ConversationActivity activity;
}