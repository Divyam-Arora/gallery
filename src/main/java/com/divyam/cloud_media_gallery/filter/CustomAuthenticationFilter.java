package com.divyam.cloud_media_gallery.filter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.divyam.cloud_media_gallery.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  @Autowired
  private UserService userService;

  private AuthenticationManager authenticationManager;

  public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
    this.authenticationManager = authenticationManager;
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
      throws AuthenticationException {

    try {
      UsernameAndPasswordAuthenticationRequest req = new ObjectMapper().readValue(request.getInputStream(),
          UsernameAndPasswordAuthenticationRequest.class);

      Authentication authentication = new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());

      return authenticationManager.authenticate(authentication);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (Exception e) {
      throw new RuntimeException("Invalid username or password");
    }

  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
      Authentication authentication) throws IOException, ServletException {
    User user = (User) authentication.getPrincipal();
    Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
    String access_token = JWT.create()
        .withSubject(user.getUsername())
        .withExpiresAt(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000))
        .withIssuer(request.getContextPath().toString())
        .withClaim("roles",
            user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
        .sign(algorithm);

    String refresh_token = JWT.create()
        .withSubject(user.getUsername())
        .withExpiresAt(new Date(System.currentTimeMillis() + 365 * 24 * 60 * 60 * 1000))
        .withIssuer(request.getContextPath().toString())
        .sign(algorithm);

    // response.setHeader("access_token", access_token);

    HashMap<String, String> tokens = new HashMap<>();
    tokens.put("access_token", access_token);
    tokens.put("refresh_token", refresh_token);
    tokens.put("username", user.getUsername());
    tokens.put("expirationDate", new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000).toString());
    response.setContentType("application/json");
    new ObjectMapper().writeValue(response.getOutputStream(), tokens);

    // super.successfulAuthentication(request, response, chain, authResult);
  }

}