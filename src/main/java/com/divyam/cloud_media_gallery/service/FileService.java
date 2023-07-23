package com.divyam.cloud_media_gallery.service;

import com.divyam.cloud_media_gallery.exception.NotAllowed;
import com.divyam.cloud_media_gallery.model.File;
import com.divyam.cloud_media_gallery.repo.FileRepo;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    FileRepo fileRepo;

    @Value("${project.media}")
    private String path;

    public File saveFile(MultipartFile file) throws IOException {
        String[] mediaType = file.getContentType().split("/");
        if(mediaType[1].equals("x-matroska"))
            mediaType[1] = "mkv";
        System.out.println(file.getContentType());

        if(mediaType[0].toLowerCase(Locale.ROOT).equals("image") && file.getSize() > 10 * 1024 * 1024){
            throw new NotAllowed("image size greater than 10MB");
        }

        if(mediaType[0].toLowerCase(Locale.ROOT).equals("video") && file.getSize() > 50 * 1024 * 1024){
            throw new NotAllowed("video size greater than 50MB");
        }

        String name = file.getOriginalFilename();
        UUID identifier = UUID.randomUUID();
        File fileObj = new File();
        fileObj.setIdentifier(identifier);
        fileObj.setSize(file.getSize());
        fileObj.setName(name);
        fileObj.setContentType(mediaType[0]);
        fileObj.setContentSubType(mediaType[1]);

        String filePath = path + java.io.File.separator + identifier.toString() + java.io.File.separator + name;
        java.io.File f = new java.io.File(path + java.io.File.separator + identifier.toString() + java.io.File.separator);
        f.mkdir();
        java.io.File thumbFile = new java.io.File(f.getPath() + java.io.File.separator+ "thumbnail" + java.io.File.separator);
        thumbFile.mkdir();

        InputStream inputStream = file.getInputStream();
        long copy = Files.copy(inputStream, Paths.get(filePath));


        if(mediaType[0].toLowerCase(Locale.ROOT).equals("image")){
            BufferedImage img = ImageIO.read(new java.io.File(filePath)); // load image
            BufferedImage scaledImg = Scalr.resize(img, 300);
            ImageIO.write(scaledImg,file.getContentType().split("/")[1],new java.io.File(thumbFile.getPath()+ java.io.File.separator + file.getOriginalFilename()));
            fileObj.setHeight(img.getHeight());
            fileObj.setWidth(img.getWidth());
            img.flush();
            scaledImg.flush();
        } else{
            FFmpegLogCallback.set();
            FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(filePath);
            try{
                frameGrabber.setFormat(mediaType[1]);
                frameGrabber.start(true);
                frameGrabber.setFrameNumber(frameGrabber.getLengthInFrames()/ 2);
                Frame frame = frameGrabber.grabKeyFrame();
                if(frame == null){
                    frameGrabber.setFrameNumber(1);
                    frame = frameGrabber.grabKeyFrame();
                }
                BufferedImage image = new Java2DFrameConverter().convert(frame);
                BufferedImage scaledImg = Scalr.resize(image, 300);
                ImageIO.write(scaledImg,"png",new java.io.File(thumbFile.getPath()+ java.io.File.separator + file.getOriginalFilename()));
                fileObj.setHeight(frameGrabber.getImageHeight());
                fileObj.setWidth(frameGrabber.getImageWidth());
                image.flush();
                scaledImg.flush();
            } catch (FFmpegFrameGrabber.Exception exception){
                System.out.println(exception.toString());
            }
            frameGrabber.stop();
            frameGrabber.close();
        }



        fileRepo.save(fileObj);
        inputStream.close();

        return fileObj;
    }
}