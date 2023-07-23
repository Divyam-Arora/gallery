package com.divyam.cloud_media_gallery.filter;

import java.io.IOException;
import java.util.*;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CustomAuthorizationFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
    if (request.getServletPath().startsWith("/api/public")) {
      filterChain.doFilter(request, response);
    } else {
      String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
      if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {

          String token = authorizationHeader.substring("Bearer ".length());
          Algorithm algorithm = Algorithm.HMAC256("secret".getBytes());
          JWTVerifier verifier = JWT.require(algorithm).build();
          DecodedJWT decodedJWT = verifier.verify(token);
          String username = decodedJWT.getSubject();
          String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
          Collection<SimpleGrantedAuthority> authorities = new ArrayList<>();
          Arrays.stream(roles).forEach(role -> {
            authorities.add(new SimpleGrantedAuthority(role));
          });

          UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username,
              null, authorities);

          SecurityContextHolder.getContext().setAuthentication(authenticationToken);

          filterChain.doFilter(request, response);

      } else {
        response.setStatus(HttpStatus.FORBIDDEN.value());
        HashMap<String, String> error = new HashMap<>();
        error.put("message", "Access denied");
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), error);
        filterChain.doFilter(request, response);
      }
    }
    }
    catch (TokenExpiredException e){
        response.setHeader("error", e.getMessage());
        response.setStatus(HttpStatus.FORBIDDEN.value());
        // response.sendError(HttpStatus.FORBIDDEN.value());
        HashMap<String, String> error = new HashMap<>();
        error.put("message", e.getMessage());
        error.put("code", "TOKEN_EXPIRED");
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
    catch (Exception e) {
      response.setHeader("error", e.getMessage());
      response.setStatus(HttpStatus.FORBIDDEN.value());
      // response.sendError(HttpStatus.FORBIDDEN.value());
      HashMap<String, String> error = new HashMap<>();
      error.put("message", e.getMessage());
      response.setContentType("application/json");
      new ObjectMapper().writeValue(response.getOutputStream(), error);
    }
  }

}