package com.divyam.cloud_media_gallery.service;

import com.divyam.cloud_media_gallery.exception.NotAllowed;
import com.divyam.cloud_media_gallery.exception.ResourceAccessDenied;
import com.divyam.cloud_media_gallery.exception.ResourceNotFoundException;
import com.divyam.cloud_media_gallery.exception.UserNotFound;
import com.divyam.cloud_media_gallery.model.*;
import com.divyam.cloud_media_gallery.payload.response.*;
import com.divyam.cloud_media_gallery.repo.*;
import com.divyam.cloud_media_gallery.util.Helpers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ConversationService {

    @Autowired
    UserService userService;

    @Autowired
    ConversationRepo conversationRepo;

    @Autowired
    SharedRepo sharedRepo;

    @Autowired
    ShareService shareService;

    @Autowired
    UserRepo userRepo;

    @Autowired
    ConversationActivityRepo conversationActivityRepo;

    @Autowired
    MediaService mediaService;

    @Autowired
    FileService fileService;

    public PageResponse<ConversationResponse> getAllConversationsByCurrentUser(int page, int size, int extra, String search, long recent){
        User currentUser = userService.loadUserFromContext();
        search = Helpers.getSearchRegexpString(search);
        if(recent > 0){
            Instant milli = Instant.ofEpochMilli(recent);
            List<Conversation> conversations= conversationRepo.findRecentConversationByMember(currentUser.getId(), search, milli);
            PageResponse<ConversationResponse> response = new PageResponse<>();
            response.setResponse(conversations.stream().map(this::generateConversationResponse).collect(Collectors.toList()));
            return response;
        }
        page = Helpers.calculateNextPage(page,size,extra);
        Page<Conversation> conversationPage = conversationRepo.findConversationsByMember(currentUser.getId(), search,PageRequest.of(page,size));
        List<ConversationResponse> conversationListResponse = new ArrayList<>();
        for(Conversation conversation: conversationPage.getContent()){
            conversationListResponse.add(generateConversationResponse(conversation));
        }

        return new PageResponse<>(conversationPage.getNumber(), conversationPage.getTotalPages(), conversationPage.getTotalElements(), conversationPage.hasNext(), conversationListResponse.subList(Helpers.getNewPageStartIndex(conversationListResponse,size,extra), conversationListResponse.size()));
    }

    public PageResponse<ConversationResponse> getSharedGroups(String username, int page, int size){
        User currentUser = userService.loadUserFromContext();
        User user = userService.getUser(username);
        if(user == null)
            throw new UserNotFound(username);

        page = Helpers.calculateNextPage(page, size, 0);
        Page<Conversation> groupPage = conversationRepo.findSharedGroupByMember(currentUser.getId(), user.getId(), PageRequest.of(page, size));

        List<ConversationResponse> response = new ArrayList<>();

        for(Conversation group : groupPage.getContent()){
            response.add(generateConversationResponse(group));
        }

        return new PageResponse<>(groupPage.getNumber(), groupPage.getTotalPages(), groupPage.getTotalElements(), groupPage.hasNext(), response.subList(Helpers.getNewPageStartIndex(response,size, 0), response.size()));
    }

    public PageResponse<ConversationResponse> getSharedMediaConversations(String username, Long mediaId, int page, int size){
        User currentUser = userService.loadUserFromContext();
        User user = userService.getUser(username);
        if(user == null)
            throw new UserNotFound(username);

        page = Helpers.calculateNextPage(page, size, 0);
        List<Long> conversations = conversationRepo.findAllConversationByMembers(List.of(currentUser.getId(), user.getId()),2);
        Page<Conversation> conversationPage = conversationRepo.findConversationsBySharedMedia(conversations,mediaId,PageRequest.of(page, size));

        List<ConversationResponse> response = new ArrayList<>();

        for(Conversation group : conversationPage.getContent()){
            response.add(generateConversationResponse(group));
        }

        return new PageResponse<>(conversationPage.getNumber(), conversationPage.getTotalPages(), conversationPage.getTotalElements(), conversationPage.hasNext(), response.subList(Helpers.getNewPageStartIndex(response,size, 0), response.size()));
    }

    public ConversationRecentActivityResponse getRecentConversationActivity(Long conversationId, Long since, Long lastConversation, String search){
        User currentUser = userService.loadUserFromContext();
        Instant milli = Instant.ofEpochMilli(since);
        String searchString = Helpers.getSearchRegexpString(search);
        List<Conversation> conversations = conversationRepo.findConversationsByMember(currentUser.getId(),searchString, PageRequest.of(0,100)).getContent();
        List<Conversation> latestConversations = conversationActivityRepo.findAllUserConversationsByLatestActivity(conversations, milli);

        ConversationRecentActivityResponse response = new ConversationRecentActivityResponse();
        response.setConversations(latestConversations.stream().map(this::generateConversationResponse).collect(Collectors.toList()));

        if(conversationId > 0){
            Instant activityMilli = Instant.ofEpochMilli(lastConversation);
            Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
            Conversation memberConversation = conversationRepo.findByConversationAndMember(conversationId, currentUser.getId());
            if(memberConversation == null) throw new ResourceAccessDenied("Conversation");

            response.setConversationActivity(conversationActivityRepo.findAllLatestActivityByConversation(conversation, activityMilli).stream().map(this::generateActivityResponse).collect(Collectors.toList()));
        }

        return response;
    }

    public Conversation getOrCreateConversation(Set<String> members, User currentUser, boolean isGroup){
        Conversation conversation = conversationRepo.findConversationByUsernames(members,members.size(),isGroup);
        if(conversation == null){
            Conversation newConversation = new Conversation();
            Set<User> users = new HashSet<>();
            for(String username: members){
                User user = userRepo.findByUsername(username);
                if(user == null)
                    throw new UserNotFound(username);
                users.add(user);
            }
            newConversation.setMembers(users);
            newConversation.setAdmin(currentUser);
            newConversation.setMemberCount(users.size());
            newConversation.setCreatedBy(currentUser);
            newConversation.setMediaCount(0);
            newConversation.setGroup(isGroup);
            conversationRepo.save(newConversation);
            ConversationActivity activity = new ConversationActivity(newConversation, Action.Created, currentUser, newConversation.getId(), null);
            conversationActivityRepo.save(activity);

            return newConversation;
        }
        return conversation;
    }

    public ConversationResponse editGroupMembers(Set<String> toAdd, Set<String> toRemove, Long conversationId){
        User currentUser = userService.loadUserFromContext();
        if(conversationId == null){
            if(toAdd.size() < 2)
                throw new NotAllowed("group with less than 3 members");
            toAdd.add(currentUser.getUsername());
        return generateConversationResponse(getOrCreateConversation(toAdd,currentUser,true));
        }

        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        if(!conversation.isGroup())
            throw new NotAllowed("Adding members in this conversation");
        if (!Objects.equals(conversation.getAdmin().getUsername(), currentUser.getUsername()))
            throw new ResourceAccessDenied("Admin privileges");

        Set<User> conversationMembers = conversation.getMembers();
        toAdd.forEach((username) -> {
            User user = userRepo.findByUsername(username);
            if(user == null){
                throw new UserNotFound(username);
            }
            conversationMembers.add(user);
            conversationActivityRepo.save(new ConversationActivity(conversation,Action.Added, currentUser, user.getId(), user.getUsername()));
        });
        toRemove.forEach(username -> {
            User user = userRepo.findByUsername(username);
            if(user != null){
                conversationMembers.remove(user);
                conversationActivityRepo.save(new ConversationActivity(conversation,Action.Removed, currentUser, user.getId(), user.getUsername()));
            }
        });
        conversation.setMembers(new HashSet<>(conversationMembers));
        conversation.setMemberCount(conversationMembers.size());
        conversationRepo.save(conversation);
        return generateConversationResponse(conversation);

    }

    public ConversationResponse editConversationIcon(long conversationId, MultipartFile file, String path) throws IOException {
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        if(!conversation.isGroup())
            throw new NotAllowed("Adding an icon in this conversation");
        if (!Objects.equals(conversation.getAdmin().getUsername(), currentUser.getUsername()))
            throw new ResourceAccessDenied("Admin privileges");
        if(file.getSize() > 5 * 1024 * 1024){
            throw new NotAllowed("File size more than 5 MB");
        }
        String type = Objects.requireNonNull(file.getContentType()).split("/")[0];
        if(!Objects.equals(type, "image"))
            throw new NotAllowed(type + " file format");

        conversation.setIcon(fileService.saveFile(file));
        conversationRepo.save(conversation);
        conversationActivityRepo.save(new ConversationActivity(conversation, Action.Changed_Icon,currentUser, conversation.getIcon().getId(), null));
        return generateConversationResponse(conversation);

        // TODO: 4/26/2023 Delete previous Icon when replacing for a new one
    }

    public ConversationResponse deleteConversationIcon(long conversationId){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        if(!conversation.isGroup())
            throw new NotAllowed("Adding an icon in this conversation");
        if (!Objects.equals(conversation.getAdmin().getUsername(), currentUser.getUsername()))
            throw new ResourceAccessDenied("Admin privileges");

        conversation.setIcon(null);
        conversationRepo.save(conversation);
        conversationActivityRepo.save(new ConversationActivity(conversation, Action.Removed_Icon,currentUser, null, null));
        return generateConversationResponse(conversation);

        // TODO: 4/26/2023 Delete previous Icon when replacing for a new one
    }

    public ConversationResponse getConversationDetails(long conversationId){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        Conversation memberConversation = conversationRepo.findByConversationAndMember(conversationId, currentUser.getId());
        if(memberConversation == null) throw new ResourceAccessDenied("Conversation");

        return generateConversationResponse(conversation);
    }

    public PageResponse<ActivityDetailResponse> getConversationActivity(long conversationId, int page, int size, int extra, String search){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        Conversation memberConversation = conversationRepo.findByConversationAndMember(conversationId, currentUser.getId());
        if(memberConversation == null) throw new ResourceAccessDenied("Conversation");

        page = Helpers.calculateNextPage(page,size, extra);
        Page<ConversationActivity> activityPage = conversationActivityRepo.findByConversationAndActionNotOrderByIdDesc(memberConversation,Action.UnShared_Activity,PageRequest.of(page, size));
        List<ActivityDetailResponse> activityResponse = new ArrayList<>();

        for(ConversationActivity activity : activityPage.getContent()){
            ActivityDetailResponse ac = generateActivityResponse(activity);
            activityResponse.add(ac);
        }

        return new PageResponse<>(activityPage.getNumber(), activityPage.getTotalPages(), activityPage.getTotalElements(), activityPage.hasNext(),activityResponse.subList(Helpers.getNewPageStartIndex(activityResponse,size,extra),activityResponse.size()));
    }

    public PageResponse<SharedMediaResponse> getConversationMedia(long conversationId, int page, int size, String search){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        Conversation memberConversation = conversationRepo.findByConversationAndMember(conversationId, currentUser.getId());
        if(memberConversation == null) throw new ResourceAccessDenied("Conversation");
        String searchString = Helpers.getSearchRegexpString(search);
        page = Helpers.calculateNextPage(page,size, 0);
        Page<Shared> sharedPage = sharedRepo.findByConversationOrderByIdDesc(conversation.getId(), searchString, PageRequest.of(page, size));
        List<SharedMediaResponse> response = new ArrayList<>();

        for(Shared sh : sharedPage.getContent()){
            response.add(new SharedMediaResponse(sh.getMedia(), sh.getCreatedAt(), null));
        }

        return  new PageResponse<>(sharedPage.getNumber(), sharedPage.getTotalPages(), sharedPage.getTotalElements(), sharedPage.hasNext(), response.subList(Helpers.getNewPageStartIndex(response,size,0),response.size()));
    }

    public List<Long> getAllConversationMediaId(long conversationId){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        Conversation memberConversation = conversationRepo.findByConversationAndMember(conversationId, currentUser.getId());
        if(memberConversation == null) throw new ResourceAccessDenied("Conversation");
        return sharedRepo.findAllMediaByConversation(memberConversation, currentUser);
    }

    public ConversationDetailResponse getConversationInfo(long conversationId){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        Conversation memberConversation = conversationRepo.findByConversationAndMember(conversationId, currentUser.getId());
        if(memberConversation == null) throw new ResourceAccessDenied("Conversation");

        ConversationDetailResponse response = new ConversationDetailResponse();
        response.setInfo(generateConversationResponse(conversation));
        response.setMedia(sharedRepo.findTop8ByConversationOrderByIdDesc(conversation).stream().map((shared -> new SharedMediaResponse(shared.getMedia(), shared.getCreatedAt(), null))).collect(Collectors.toList()));
        response.setMembers(UserResponse.getUserResponseList(Stream.concat(Stream.of(currentUser),userRepo.find9MembersByConversation(conversationId, currentUser.getId()).stream()).toList()));

        return response;

    }

    public PageResponse<MemberResponse> getConversationMembers(long conversationId, int page, int size, int extra, String search){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        Conversation memberConversation = conversationRepo.findByConversationAndMember(conversationId, currentUser.getId());
        if(memberConversation == null) throw new ResourceAccessDenied("Conversation");

        page = Helpers.calculateNextPage(page,size, extra);
        Page<User> memberPage = userRepo.findMembersByConversation(conversationId, currentUser.getId(), PageRequest.of(page, size));
        List<MemberResponse> members = new ArrayList<>();

        if(page == 0){
            members.add(new MemberResponse(currentUser, conversation.getAdmin()));
        }

        for(User user : memberPage.getContent()){
            members.add(new MemberResponse(user, conversation.getAdmin()));
        }

        return new PageResponse<>(memberPage.getNumber(), memberPage.getTotalPages(), memberPage.getTotalElements(), memberPage.hasNext(), members.subList(Helpers.getNewPageStartIndex(members,size, extra),members.size()));
    }

    public Set<String> getAllConversationMembers(long conversationId){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        Conversation memberConversation = conversationRepo.findByConversationAndMember(conversationId, currentUser.getId());
        if(memberConversation == null) throw new ResourceAccessDenied("Conversation");

        return userRepo.findAllMembersByConversation(conversationId, currentUser.getUsername());
    }

    public String EditConversationName(long conversationId, String name){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        if (!Objects.equals(conversation.getAdmin().getUsername(), currentUser.getUsername()))
            throw new ResourceAccessDenied("Admin privileges");
        if(!conversation.isGroup())
            throw new NotAllowed("Setting name in this conversation");
        conversation.setName(name);
        conversationRepo.save(conversation);
        conversationActivityRepo.save(new ConversationActivity(conversation, name.length() > 0 ? Action.Changed_Name : Action.Removed_Name,currentUser, null, name));
        return name;
    }

    public void leaveConversation(long conversationId){
        User currentUser = userService.loadUserFromContext();
        Conversation conversation = conversationRepo.findById(conversationId).orElseThrow(() -> new ResourceNotFoundException("Conversation","id",conversationId));
        Conversation memberConversation = conversationRepo.findByConversationAndMember(conversationId, currentUser.getId());
        if(memberConversation == null) throw new ResourceAccessDenied("Conversation");

        if(Objects.equals(conversation.getAdmin().getUsername(), currentUser.getUsername())){
            User member = userRepo.find1MemberByConversation(conversationId, currentUser.getId());
            conversation.setAdmin(member);
        }
        shareService.unShareWithConversation(conversationId,sharedRepo.findAllMediaByConversation(conversation,currentUser));
        conversation.getMembers().removeIf(member -> Objects.equals(member.getUsername(), currentUser.getUsername()));
        Set<User> newMembers = new HashSet<>(conversation.getMembers());
        conversation.setMembers(newMembers);
        conversation.setMemberCount(conversation.getMemberCount() - 1);
        conversationActivityRepo.save(new ConversationActivity(conversation, Action.Left, currentUser, null, null));
        conversationRepo.save(conversation);
    }

    public void checkConversations(List<Long> conversations){
        User currentUser = userService.loadUserFromContext();
        List<Long> userConversations = conversationRepo.findUserConversationsFromConversations(conversations, currentUser.getId());
        if(userConversations.size() != conversations.size()){
            throw new ResourceAccessDenied("conversation");
        }
    }

    public ConversationResponse generateConversationResponse(Conversation conversation){
        User currentUser = userService.loadUserFromContext();
        ConversationResponse conversationResponse = new ConversationResponse();
        conversationResponse.setId(conversation.getId());
        conversationResponse.setName(conversation.getName());
        conversationResponse.setAdmin(conversation.getAdmin().getUsername());
        conversationResponse.setCreatedBy(conversation.getCreatedBy().getUsername());
        conversationResponse.setGroup(conversation.isGroup());
        conversationResponse.setMembers(new HashSet<>(UserResponse.getUserResponseList(conversation.getMembers().stream().filter(member -> !Objects.equals(member.getId(), currentUser.getId())).toList().stream().limit(2).collect(Collectors.toList()))));
        conversationResponse.setMemberCount(conversation.getMemberCount());
        conversationResponse.setMediaCount(conversation.getMediaCount());
        conversationResponse.setUpdatedAt(conversation.getUpdatedAt());
        conversationResponse.setCreatedAt(conversation.getCreatedAt());
        Shared shared = sharedRepo.findTop1ByConversationOrderByIdDesc(conversation);
        if(shared != null)
        conversationResponse.setLastMediaShared(new SharedMediaResponse(shared.getMedia(),shared.getCreatedAt(), null));
        conversationResponse.setLastActivity(new ActivityResponse(conversationActivityRepo.findTop1ByConversationOrderByCreatedAtDesc(conversation)));
        if(conversation.getIcon() != null){
        conversationResponse.setIconURL(Helpers.generateIconURL(conversation.getId(), "conversation") + "icon?id=" + conversation.getIcon().getId());
        conversationResponse.setIconThumbnail(Helpers.generateIconURL(conversation.getId(), "conversation") + "thumbnail?id=" + conversation.getIcon().getId());
        }
        return conversationResponse;
    }

    public ActivityDetailResponse generateActivityResponse(ConversationActivity activity){
        ActivityDetailResponse activityDetailResponse = new ActivityDetailResponse();
        activityDetailResponse.setId(activity.getId());
        activityDetailResponse.setAction(activity.getAction());
        activityDetailResponse.setBy(new UserResponse(activity.getBy()));
        activityDetailResponse.setOn(activity.getCreatedAt());
        activityDetailResponse.setTarget(activity.getTargetId());

        switch (activity.getAction()) {
            case Shared -> activityDetailResponse.setTarget(new SharedMediaResponse(mediaService.loadMedia(activity.getTargetId()),activity.getCreatedAt(), null));
            case UnShared_Activity -> activityDetailResponse.setTarget(new ActivityDetailResponse(conversationActivityRepo.findById(activity.getTargetId()).get(),activity.getCreatedAt()));
            case Added, Removed -> activityDetailResponse.setTarget(userService.getUser(activity.getTargetString()) != null ? userService.getUser(activity.getTargetString()).getUsername() : "unnamed");
        }

        return activityDetailResponse;
    }
}