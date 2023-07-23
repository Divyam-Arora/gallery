package com.divyam.cloud_media_gallery.service;

import com.divyam.cloud_media_gallery.exception.ResourceAccessDenied;
import com.divyam.cloud_media_gallery.exception.ResourceNotFoundException;
import com.divyam.cloud_media_gallery.model.Action;
import com.divyam.cloud_media_gallery.model.Media;
import com.divyam.cloud_media_gallery.model.Tag;
import com.divyam.cloud_media_gallery.model.User;
import com.divyam.cloud_media_gallery.payload.request.MediaDate;
import com.divyam.cloud_media_gallery.payload.response.*;
import com.divyam.cloud_media_gallery.repo.*;
import com.divyam.cloud_media_gallery.util.Constants;
import com.divyam.cloud_media_gallery.util.Helpers;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MediaService {
    @Autowired
    MediaRepo mediaRepo;

    @Autowired
    UserService userService;

    @Autowired
    SharedRepo sharedRepo;

    @Autowired
    ConversationRepo conversationRepo;

    @Autowired
    ConversationActivityRepo conversationActivityRepo;

    @Autowired
    AlbumRepo albumRepo;

    @Autowired
    AlbumMediaRepo albumMediaRepo;

    @Autowired
    AlbumService albumService;

    @Value("${project.media}")
    private String path;

    String dateFormat = "d MMM yyyy";

    public Media saveMediaMeta(com.divyam.cloud_media_gallery.model.File file) throws IOException {
        Media media = new Media();

        media.setOwner(userService.loadUserFromContext());
        media.setFile(file);

        LocalDate date = LocalDate.now();
        media.setDate(date.getDayOfMonth());
        media.setMonth(date.getMonth().toString());
        media.setYear(date.getYear());

        mediaRepo.save(media);

        return media;
    }

    public UUID saveMedia(String path, MultipartFile file) throws IOException {
        String name = file.getOriginalFilename();
        UUID identifier = UUID.randomUUID();
        String filePath = path + File.separator + identifier.toString() + File.separator + name;
        File f = new File(path + File.separator + identifier.toString() + File.separator);
        f.mkdir();
        File thumbFile = new File(f.getPath() +File.separator+ "thumbnail" + File.separator);
        thumbFile.mkdir();

        InputStream inputStream = file.getInputStream();
        Files.copy(inputStream, Paths.get(filePath));

        BufferedImage img = ImageIO.read(new File(filePath)); // load image
        BufferedImage scaledImg = Scalr.resize(img, 300);
        ImageIO.write(scaledImg,file.getContentType().split("/")[1],new File(thumbFile.getPath()+File.separator + file.getOriginalFilename()));
        return identifier;
    }

    public PageResponse<MediaListItemResponse> loadAllMedia(int page, int size, int extra, MediaDate mediaDate){
        User user = userService.loadUserFromContext();
        page = Helpers.calculateNextPage(page, size, extra);
        Pageable p = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Media> mediaPage = null;
        if(mediaDate.getDate() != null && mediaDate.getMonth() != null && mediaDate.getYear() != null){
             mediaPage = mediaRepo.findByOwnerAndYearAndMonthAndDate(user, mediaDate.getYear(),mediaDate.getMonth(),mediaDate.getDate(),p);
        } else if(mediaDate.getMonth() != null && mediaDate.getYear() != null){
            mediaPage = mediaRepo.findByOwnerAndYearAndMonth(user, mediaDate.getYear(), mediaDate.getMonth(),p);
        } else if(mediaDate.getYear() != null){
            mediaPage = mediaRepo.findByOwnerAndYear(user, mediaDate.getYear(),p);
        } else{
            mediaPage = mediaRepo.findByOwner(user, p);
        }
        List<Media> allMedia = mediaPage.getContent();
        List<MediaListItemResponse> mediaListItemResponseList = new ArrayList<>();
//        List<MediaDateGroup> mediaDateGroupList = new ArrayList<>();
        for(Media media : allMedia){
            mediaListItemResponseList.add(new MediaListItemResponse(media));
        }

        return new PageResponse<>(mediaPage.getNumber(),mediaPage.getTotalPages(),mediaPage.getTotalElements(), mediaPage.hasNext(), mediaListItemResponseList.subList(Helpers.getNewPageStartIndex(mediaListItemResponseList,size,extra),mediaListItemResponseList.size()));
    }

    public Media loadMedia(long id){
        return mediaRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("media","id",id));
    }

    public MediaResponse loadUserMedia(long id){
        User user = userService.loadUserFromContext();
        Media media = mediaRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("media","id",id));
        if(Objects.equals(media.getOwner().getUsername(), user.getUsername())){
        return generateMediaResponse(media);
        } else if(sharedRepo.findByMediaAndUserAndOwnerConversation(media.getId(),user.getId(),media.getOwner().getId()) != null){
            return new MediaResponse(media);
        } else{
            throw new ResourceAccessDenied("media");
        }
    }

    public List<AlbumListItemResponse> getAlbumsByMedia(long mediaId){
        User user = userService.loadUserFromContext();
        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media","id",mediaId));
        if(Objects.equals(media.getOwner().getUsername(), user.getUsername())){
            return albumMediaRepo.findByMediaOrderByUpdatedAtDesc(media).stream().map(albumMedia -> albumService.generateAlbumResponse(albumMedia.getAlbum())).collect(Collectors.toList());
        }
        else{
            throw new ResourceAccessDenied("media");
        }
    }

    public List<Long> getAllAlbumsByMedia(long mediaId){
        User user = userService.loadUserFromContext();
        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media","id",mediaId));
        if(Objects.equals(media.getOwner().getUsername(), user.getUsername())){
            return albumMediaRepo.findAllByMedia(media);
        }
        else{
            throw new ResourceAccessDenied("media");
        }
    }

    public List<ConversationListItemResponse> getConversationsByMedia(long mediaId){
        User user = userService.loadUserFromContext();
        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media","id",mediaId));
        if(Objects.equals(media.getOwner().getUsername(), user.getUsername())){
            return sharedRepo.findByMediaOrderByCreatedAtDesc(media).stream().map(shared -> new ConversationListItemResponse(shared.getConversation(), user)).collect(Collectors.toList());
        }
        else{
            throw new ResourceAccessDenied("media");
        }
    }

    public List<Long> getAllConversationsByMedia(long mediaId){
        User user = userService.loadUserFromContext();
        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media","id",mediaId));
        if(Objects.equals(media.getOwner().getUsername(), user.getUsername())){
            return sharedRepo.findAllConversationsByMedia(media);
        }
        else{
            throw new ResourceAccessDenied("media");
        }
    }

    public void checkUserMedia(List<Long> media){
        User user  = userService.loadUserFromContext();
        if(mediaRepo.findUserMediaFromMedia(media, user.getId()).size() != media.size()){
            throw new ResourceAccessDenied("media");
        }
    }

    public List<Integer> getAllYear(){

        return mediaRepo.findAllYear(userService.loadUserFromContext());
    }

    public List<String> getAllMonth(int year){
        return mediaRepo.findAllMonthByYear(userService.loadUserFromContext(),year);
    }

    public List<Integer> getAllDate(int year, String month){
        return mediaRepo.findAllDateByYearAndMonth(userService.loadUserFromContext(),year, month);
    }

    public List<TagResponse> getMediaTags(long mediaId){
        User currentUser = userService.loadUserFromContext();
        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media","id",mediaId));
        if(!Objects.equals(media.getOwner().getUsername(), currentUser.getUsername()))
            throw new ResourceAccessDenied("media");
        return media.getTags().stream().map(TagResponse::new).collect(Collectors.toList());
    }

    @Transactional
    public long deleteMedia(long mediaId) throws IOException {
        User currentUser = userService.loadUserFromContext();
        Media media = mediaRepo.findById(mediaId).orElseThrow(() -> new ResourceNotFoundException("media","id",mediaId));
        if(!Objects.equals(media.getOwner().getUsername(), currentUser.getUsername()))
            throw new ResourceAccessDenied("media");
        conversationActivityRepo.updateActivityActionByActionAndTarget(mediaId, Action.Shared, Action.UnShared);
        sharedRepo.deleteByMedia(mediaId);
        albumMediaRepo.deleteByMedia(mediaId);
        FileSystemUtils.deleteRecursively(Path.of(path + java.io.File.separator + media.getFile().getIdentifier().toString()));
        mediaRepo.delete(media);
        return mediaId;
    }

    public MediaResponse generateMediaResponse(Media media){
        User currentUser = userService.loadUserFromContext();
        com.divyam.cloud_media_gallery.model.File file = media.getFile();
        MediaResponse response = new MediaResponse(media);
        response.setConversations(sharedRepo.findTop6ByMediaOrderByCreatedAtDesc(media).stream().map(sh -> new ConversationListItemResponse(sh.getConversation(), currentUser)).collect(Collectors.toList()));
        response.setAlbums(albumMediaRepo.findTop6ByMediaOrderByUpdatedAtDesc(media).stream().map(am -> albumService.generateAlbumResponse(am.getAlbum())).collect(Collectors.toList()));

        return response;
    }
}