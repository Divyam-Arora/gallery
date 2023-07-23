package com.divyam.cloud_media_gallery.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.divyam.cloud_media_gallery.model.Role;

public interface RoleRepo extends JpaRepository<Role, Long> {
  Role findByName(String name);
}
