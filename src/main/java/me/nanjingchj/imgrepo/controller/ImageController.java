package me.nanjingchj.imgrepo.controller;

import lombok.Getter;
import me.nanjingchj.imgrepo.model.ImageItem;
import me.nanjingchj.imgrepo.service.ImageRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

@RestController
@RequestMapping("/")
public class ImageController {
    private final ImageRepoService imageRepoService;

    @Autowired
    public ImageController(ImageRepoService imageRepoService) {
        this.imageRepoService = imageRepoService;
    }

    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public void uploadImage(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws IOException {
        try {
            imageRepoService.addImage(new ImageItem(file.getOriginalFilename(), file.getBytes()));
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
