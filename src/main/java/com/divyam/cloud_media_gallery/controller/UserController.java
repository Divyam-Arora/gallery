package com.divyam.cloud_media_gallery.controller;

import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.divyam.cloud_media_gallery.payload.response.UserDetailResponse;
import com.divyam.cloud_media_gallery.payload.response.UserResponse;
import com.divyam.cloud_media_gallery.service.ConversationService;
import com.divyam.cloud_media_gallery.service.UserService;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.divyam.cloud_media_gallery.model.Role;
import com.divyam.cloud_media_gallery.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class UserController {
  private final UserService userService;
  private final ConversationService conversationService;

  @GetMapping("/user")
  public ResponseEntity<List<User>> getUsers() {
    return ResponseEntity.ok().body(userService.getUsers());
  }

  @PostMapping("/user")
  public ResponseEntity<User> addUser(@RequestBody User user) {
    URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/user").toUriString());
    return ResponseEntity.created(uri).body(userService.saveUser(user));
  }

  @GetMapping(value = {"/user/get"})
  public ResponseEntity<?> searchUser(@RequestParam(value = "search", defaultValue = "") String search){
    return ResponseEntity.ok(userService.searchUsers(search));
  }

  @GetMapping("/user/{username}")
  public ResponseEntity<?> getUser(@PathVariable String username){

    return ResponseEntity.ok(userService.getPersonDetails(username));
  }

  @GetMapping("/user/{username}/groups")
  public ResponseEntity<?> getSharedGroups(@PathVariable String username, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "40") int size){
    return ResponseEntity.ok(conversationService.getSharedGroups(username,page,size));
  }

  @GetMapping("user/{username}/media")
  public ResponseEntity<?> getUserSharedMedia(@PathVariable String username, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "40") int size, @RequestParam(value = "extra", defaultValue = "0") int extra, @RequestParam(value = "search", defaultValue = "") String search){
    return ResponseEntity.ok(userService.getPersonSharedMedia(username, page, size, extra, search));
  }

  @GetMapping("user/{username}/media/{mediaId}/conversations")
  public ResponseEntity<?> getSharedMediaConversations(@PathVariable String username, @PathVariable Long mediaId, @RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "40") int size){
    return ResponseEntity.ok(conversationService.getSharedMediaConversations(username,mediaId,page, size));
  }

  @GetMapping("/user/me")
  public ResponseEntity<?> getCurrentUser(){
    User user = userService.loadUserFromContext();
    return ResponseEntity.ok().body(new UserResponse(user));
  }

  @PostMapping("user/me/icon")
  public ResponseEntity<?> editUserIcon(MultipartFile file) throws IOException, ImageWriteException, ImageReadException {
    return ResponseEntity.ok(new UserResponse(userService.editUserIcon(file)));
  }

  @DeleteMapping("user/me/icon")
  public ResponseEntity<?> deleteUserIcon(){
    return ResponseEntity.ok(userService.deleteUserIcon());
  }

  @PostMapping("/role")
  public ResponseEntity<Role> addRole(@RequestBody Role role) {
    URI uri = URI.create(ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/role").toUriString());
    return ResponseEntity.created(uri).body(userService.saveRole(role));
  }

  @PostMapping("/user/addrole")
  public ResponseEntity<?> addRoleToUser(@RequestBody RoleToUserForm form) {
    userService.addRoleToUser(form.getUsername(), form.getRoleName());
    return ResponseEntity.ok().build();
  }

  @GetMapping("/public/refresh")
  public void refresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      try {
        String refresh_token = authorizationHeader.substring("Bearer ".length());
        Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT decodedJWT = verifier.verify(refresh_token);
        String username = decodedJWT.getSubject();
        User user = userService.getUser(username);

        String access_token = JWT.create()
            .withSubject(user.getUsername())
            .withExpiresAt(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
            .withIssuer(request.getContextPath().toString())
            .withClaim("roles",
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList()))
            .sign(algorithm);
        // response.setHeader("access_token", access_token);

        HashMap<String, String> tokens = new HashMap<>();
        tokens.put("access_token", access_token);
        tokens.put("refresh_token", refresh_token);
        tokens.put("username", user.getUsername());
        tokens.put("expirationDate", new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000).toString());
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), tokens);

      } catch (TokenExpiredException e){
          response.setHeader("error", e.getMessage());
          response.setStatus(HttpStatus.FORBIDDEN.value());
          // response.sendError(HttpStatus.FORBIDDEN.value());
          HashMap<String, String> error = new HashMap<>();
          error.put("message", e.getMessage());
          error.put("code", "REFRESH_TOKEN_EXPIRED");
          response.setContentType("application/json");
          new ObjectMapper().writeValue(response.getOutputStream(), error);
      }
      catch (Exception e) {
        response.setHeader("error", e.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        // response.sendError(HttpStatus.FORBIDDEN.value());
        HashMap<String, String> error = new HashMap<>();
        error.put("message", "Invalid token");
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), error);
      }
    } else {
      response.setStatus(HttpStatus.FORBIDDEN.value());
      HashMap<String, String> error = new HashMap<>();
      error.put("message", "Access denied");
      response.setContentType("application/json");
      new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
  }
}

@Data
class RoleToUserForm {
  private String username;
  private String roleName;
}