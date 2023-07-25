package com.divyam.cloud_media_gallery;

import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.Role;
import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.repo.MediaRepo;
import com.divyam.cloud_media_gallery.service.MediaService;
import com.divyam.cloud_media_gallery.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;

@SpringBootApplication
@EnableJpaAuditing
public class CloudMediaGalleryApplication {

	@Value("${project.media}")
	private String path;

	public static void main(String[] args) {
		SpringApplication.run(CloudMediaGalleryApplication.class, args);
	}

	 @Bean
	 CommandLineRunner run(UserService userService, MediaRepo mediaRepo) {
	 return args -> {

//		 if(userService.getUser("divyam") == null){
//			 userService.saveRole(new Role(null, "ROLE_USER"));
//			 userService.saveRole(new Role(null, "ROLE_ADMIN"));
//
//			 userService.saveUser(new User(null, "Divyam", "Arora","divyamk.a.83@gmail.com", "divyam", "123",null, new
//					 ArrayList<>()));
//
//			 userService.addRoleToUser("divyam", "ROLE_ADMIN");
//		 }

		 File f = new File(path);
		 if(!f.exists()){
			 System.out.println(path);
			 System.out.println(f.mkdir());
		 }

//		 for(Media media : mediaRepo.findAll()){
//			 Instant createdAt = media.getCreatedAt();
//			 LocalDate date = LocalDate.ofInstant(createdAt,ZoneId.of("Asia/Kolkata"));
//			 media.setDate(date.getDayOfMonth());
//			 media.setMonth(date.getMonth().toString());
//			 media.setYear(date.getYear());
//			 mediaRepo.save(media);
//		 }
	 };
	 }

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
//				registry.addMapping("/api/**").allowedOrigins("http://localhost:3000").allowedMethods("GET", "POST","PUT", "DELETE");
				registry.addMapping("/api/**").allowedOrigins("https://gallery.up.railway.app").allowedMethods("GET", "POST","PUT", "DELETE");
				;
			}
		};
	}
}