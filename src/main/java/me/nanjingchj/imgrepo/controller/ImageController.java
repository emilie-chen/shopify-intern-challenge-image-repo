package me.nanjingchj.imgrepo.controller;

import me.nanjingchj.imgrepo.dto.ImageItemDto;
import me.nanjingchj.imgrepo.dto.ImageUploadResponseDto;
import me.nanjingchj.imgrepo.model.ImageItem;
import me.nanjingchj.imgrepo.service.ImageRepoService;
import me.nanjingchj.imgrepo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/image")
public class ImageController {
    private static final Set<String> ALLOWED_FILE_TYPES = Stream.of("png", "jpg", "jpeg", "bmp").collect(Collectors.toUnmodifiableSet());

    private final ImageRepoService imageRepoService;
    private final UserService userService;

    @Autowired
    public ImageController(ImageRepoService imageRepoService, UserService userService) {
        this.imageRepoService = imageRepoService;
        this.userService = userService;
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
    public ImageUploadResponseDto uploadImage(@RequestParam("file") MultipartFile file, @RequestParam("session") String token, @RequestParam("isPublic") boolean isPublic, HttpServletResponse response) {
        // ensure log in
        if (!userService.isUserLoggedInByToken(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        // refresh login status
        userService.refreshTokenExpiry(token);
        response.addCookie(userService.createSessionCookie(token));
        // get user id
        String id = userService.getIdFromToken(token);
        // ensure that the file has an image extension name
        if (!isFileTypeAllowed(getFileExtensionFromFileName(Objects.requireNonNull(file.getOriginalFilename())))) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return new ImageUploadResponseDto("");
        }
        try {
            ImageItem item = new ImageItem(file.getOriginalFilename(), id, isPublic, Set.of(id), file.getBytes());
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
    public ResponseEntity<Resource> getImage(@PathVariable("id") String id, @RequestParam("session") String token, HttpServletResponse response) {
        // ensure log in
        if (!userService.isUserLoggedInByToken(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        // refresh login status
        userService.refreshTokenExpiry(token);
        response.addCookie(userService.createSessionCookie(token));
        // get user id
        String userId = userService.getIdFromToken(token);
        ImageItem img;
        try {
            img = imageRepoService.getImageById(id);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
        // check permissions
        if (!img.isAccessibleTo(userId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        ByteArrayResource imageRes = new ByteArrayResource(img.getImage());
        response.setContentType("application/x-msdownload");
        response.setHeader("Content-disposition", "attachment; filename=" + img.getName());
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(imageRes);
    }

    /**
     * this method is publicly available
     */
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
    public void deleteImage(@PathVariable("id") String id, @RequestParam("session") String token, HttpServletResponse response) {
        // ensure log in
        if (!userService.isUserLoggedInByToken(token)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        // refresh login status
        userService.refreshTokenExpiry(token);
        response.addCookie(userService.createSessionCookie(token));
        // get user id
        String userId = userService.getIdFromToken(token);
        if (imageRepoService.hasImageId(id)) {
            ImageItem img = imageRepoService.getImageById(id);
            // check permissions
            if (!img.isAccessibleTo(userId)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            imageRepoService.deleteImageById(id);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

}
