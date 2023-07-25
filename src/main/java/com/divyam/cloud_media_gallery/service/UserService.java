package com.divyam.cloud_media_gallery.service;

import java.io.IOException;
import java.util.*;

import com.divyam.cloud_media_gallery.exception.AlreadyExists;
import com.divyam.cloud_media_gallery.exception.UserNotFound;
import com.divyam.cloud_media_gallery.model.*;
import com.divyam.cloud_media_gallery.payload.response.*;
import com.divyam.cloud_media_gallery.repo.ConversationRepo;
import com.divyam.cloud_media_gallery.repo.SharedRepo;
import com.divyam.cloud_media_gallery.util.Helpers;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.divyam.cloud_media_gallery.repo.RoleRepo;
import com.divyam.cloud_media_gallery.repo.UserRepo;

import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService implements UserDetailsService {

  private final UserRepo userRepo;
  private final RoleRepo roleRepo;
  private final PasswordEncoder passwordEncoder;
  private final SharedRepo sharedRepo;
  private final ConversationRepo conversationRepo;
  private final FileService fileService;

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = userRepo.findByUsername(username);
    if (user == null) {
      throw new UsernameNotFoundException("username not found");
    }

    Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();

    user.getRoles().forEach(role -> {
      authorities.add(new SimpleGrantedAuthority(role.getName()));
    });

    return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), authorities);
  }


  public User getUser(String username) {

    return userRepo.findByUsername(username);
  }

  public UserDetailResponse getPersonDetails(String username){
    User person = userRepo.findByUsername(username);
    User currentUser = loadUserFromContext();
    if(person == null) throw new UserNotFound(username);

    return new UserDetailResponse(person,sharedRepo.findSharedMediaCount(currentUser.getId(),person.getId()),conversationRepo.findSharedGroupCountByMember(currentUser.getId(),person.getId()));
  }

  public PageResponse<SharedMediaResponse> getPersonSharedMedia(String username, int page, int size, int extra, String search){
    User person = userRepo.findByUsername(username);
    User currentUser = loadUserFromContext();
    if(person == null) throw new UserNotFound(username);
    page = Helpers.calculateNextPage(page, size, extra);
    String searchString = Helpers.getSearchRegexpString(search);

    List<Long> conversations = conversationRepo.findAllConversationByMembers(List.of(currentUser.getId(), person.getId()),2);
    Page<Shared> sharedMedia = sharedRepo.findByOwnerAndSharedTo(currentUser.getId(),person.getId(),searchString, conversations,PageRequest.of(page,size));
    List<SharedMediaResponse> mediaResponse = new ArrayList<>();

    for(Shared sh: sharedMedia.getContent()){
      mediaResponse.add(new SharedMediaResponse(sh.getMedia(),sh.getCreatedAt(), conversationRepo.findConversationCountBySharedMedia(conversations,sh.getMedia().getId())));
    }

    return new PageResponse<>(sharedMedia.getNumber(),sharedMedia.getTotalPages(),sharedMedia.getTotalElements(),sharedMedia.hasNext(),mediaResponse.subList(Helpers.getNewPageStartIndex(mediaResponse,size, extra),mediaResponse.size()));
  }

  public User saveUser(User user) {

    User existingUser = userRepo.findByUsername(user.getUsername());

    if(existingUser != null){
      throw new AlreadyExists("username");
    }

    user.setPassword(passwordEncoder.encode(user.getPassword()));

    Role role = roleRepo.findByName("ROLE_USER");

    user.getRoles().add(role);

    return userRepo.save(user);
  }


  public Role saveRole(Role role) {
    return roleRepo.save(role);
  }


  public void addRoleToUser(String username, String roleName) {

    User user = userRepo.findByUsername(username);
    Role role = roleRepo.findByName(roleName);

    user.getRoles().add(role);
  }


  public List<User> getUsers() {
    return userRepo.findAll();
  }

  public List<UserResponse> searchUsers(String search){
    User currentUser = loadUserFromContext();
    List<User> users = new ArrayList<>();

    if(search.length() == 0){
      users = userRepo.getSharedMediaUsers(loadUserFromContext().getId(), PageRequest.of(0, 20));

    } else{
      users = userRepo.findBySearch(search, PageRequest.of(0,20));
    }
    List<UserResponse> userDetailResponseList = new ArrayList<>();
    for(User user: users){
      if(Objects.equals(user.getId(), currentUser.getId())){
        continue;
      }
      userDetailResponseList.add(new UserResponse(user));
    }
    return userDetailResponseList;
  }

  public User loadUserFromContext() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userRepo.findByUsername(authentication.getPrincipal().toString());
  }

  public User editUserIcon(MultipartFile file) throws IOException, ImageWriteException, ImageReadException {
    User user = loadUserFromContext();
    File icon = fileService.saveFile(file);
    user.setIcon(icon);
    userRepo.save(user);
    return user;
  }

  public User deleteUserIcon(){
    User user = loadUserFromContext();
    user.setIcon(null);
    userRepo.save(user);
    return user;
  }

  public void checkUsers(List<String> usernames){
    if(userRepo.findByUsernameIn(usernames).size() != usernames.size())
      throw new UserNotFound("");
  }
}