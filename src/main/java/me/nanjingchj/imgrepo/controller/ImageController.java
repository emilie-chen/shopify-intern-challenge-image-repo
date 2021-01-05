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
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    private final ImageRepoService imageRepoService;

    @Autowired
    public ImageController(ImageRepoService imageRepoService) {
        this.imageRepoService = imageRepoService;
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public ImageUploadResponseDto uploadImage(@RequestParam("file") MultipartFile file, HttpServletResponse response) {
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
