package me.nanjingchj.imgrepo.controller;

import lombok.Getter;
import me.nanjingchj.imgrepo.dto.ImageItemDto;
import me.nanjingchj.imgrepo.dto.ImageUploadResponseDto;
import me.nanjingchj.imgrepo.model.ImageItem;
import me.nanjingchj.imgrepo.service.ImageRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    private static final Set<String> ALLOWED_FILE_TYPES = Stream.of("png", "jpg", "jpeg", "bmp").collect(Collectors.toUnmodifiableSet());

    private final ImageRepoService imageRepoService;

    @Autowired
    public ImageController(ImageRepoService imageRepoService) {
        this.imageRepoService = imageRepoService;
    }

    private static String getFileExtensionFromFileName(String fileName) {
        if (!fileName.contains(".")) {
            return "";
        } else {
            String[] segments = fileName.split("\\.");
            return segments[segments.length - 1].toLowerCase();
        }
    }

    private static boolean isFileTypeAllowed(String fileType) {
        return ALLOWED_FILE_TYPES.contains(fileType);
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public ImageUploadResponseDto uploadImage(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
        // ensure that the file has an image extension name
        if (!isFileTypeAllowed(getFileExtensionFromFileName(Objects.requireNonNull(file.getOriginalFilename())))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ImageUploadResponseDto("");
        }
        try {
            ImageItem item = new ImageItem(file.getOriginalFilename(), file.getBytes());
            imageRepoService.addImage(item);
            response.setStatus(HttpServletResponse.SC_OK);
            return new ImageUploadResponseDto(item.getId());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return new ImageUploadResponseDto("");
        }
    }

    @RequestMapping(path = "/{id}", method = RequestMethod.GET)
    public ResponseEntity<Resource> getImage(@PathVariable("id") String id, HttpServletResponse response) {
        ImageItem img;
        try {
            img = imageRepoService.getImageById(id);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        ByteArrayResource imageRes = new ByteArrayResource(img.getImage());
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-disposition", "attachment; filename=" + img.getName());
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(imageRes);
    }

    @RequestMapping(path = "/name/{name}", method = RequestMethod.GET)
    public List<ImageItemDto> getImageByName(@PathVariable("name") String name, HttpServletResponse response) {
        try {
            return imageRepoService.getAllImagesByName(name).stream().map(ImageItemDto::new).collect(Collectors.toList());
        } catch (NoSuchElementException e) {
            e.printStackTrace();
        }
        return new LinkedList<>();
    }

    @RequestMapping(path = "/delete/{id}", method = RequestMethod.DELETE)
    public void deleteImage(@PathVariable("id") String id, HttpServletResponse response) {
        if (imageRepoService.hasImageId(id)) {
            imageRepoService.deleteImageById(id);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
