package com.divyam.cloud_media_gallery.repo;

import com.divyam.cloud_media_gallery.model.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepoTest {

    @Autowired
    private UserRepo userRepo;

    @Test
    void findByUsername() {
        //given
        User user = new User(null,"Divyam","Arora", "divyamk.a.83@gmail.com", "divyam", "123",null, null);

        userRepo.save(user);
        //when

        User created = userRepo.findByUsername("divyam");
        //then

        Assertions.assertThat(created).isEqualByComparingTo(user);
    }
}