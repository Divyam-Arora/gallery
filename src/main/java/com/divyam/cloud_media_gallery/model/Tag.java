package com.divyam.cloud_media_gallery.model;

import com.divyam.cloud_media_gallery.model.audit.DateAudit;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.ResultCheckStyle;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tag extends DateAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(unique = true)
    private String value;

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(value)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof final Tag tag) {

            return new EqualsBuilder()
                    .append(value, tag.value)
                    .isEquals();
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return value;
    }
}