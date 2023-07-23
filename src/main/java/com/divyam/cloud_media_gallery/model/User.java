package com.divyam.cloud_media_gallery.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.*;

import com.divyam.cloud_media_gallery.model.audit.DateAudit;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@NamedNativeQuery(name = "User.getSharedMediaUsers", query = "select user.* from conversation_members as m inner join conversation as c on m.conversation_id = c.id inner join user on user.id = m.members_id where c.id in (select distinct conversation_id from conversation_members where members_id = ?1) and user.id != ?1 group by user.id order by c.updated_at desc", resultClass = User.class)
public class User extends DateAudit implements Comparable<User>{
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private String username;
  private String password;

  @OneToOne
  private File icon;

  @ManyToMany(fetch = FetchType.EAGER)
  private Collection<Role> roles = new ArrayList<>();

  @Override
  public String toString() {
    return "User{" +
            "id=" + id +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", username='" + username + '\'' +
            ", password='" + password + '\'' +
            ", roles=" + roles +
            '}';
  }

  @Override
  public int compareTo(User o) {
    return this.getId().compareTo(o.getId());
  }
}