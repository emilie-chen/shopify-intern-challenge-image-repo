package me.nanjingchj.imgrepo.service;

import me.nanjingchj.imgrepo.model.ImageItem;
import me.nanjingchj.imgrepo.repository.ImageRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class ImageRepoService {
    private final ImageRepo imageRepo;

    @Autowired
    public ImageRepoService(ImageRepo imageRepo) {
        this.imageRepo = imageRepo;
    }

    public ImageItem getImageByName(String name) {
        Optional<ImageItem> img = imageRepo.findByName(name);
        if (img.isPresent()) {
            return img.get();
        } else {
            throw new NoSuchElementException("Image Not Found");
        }
    }

    public ImageItem getImageById(String id) {
        Optional<ImageItem> img = imageRepo.findById(id);
        if (img.isPresent()) {
            return img.get();
        } else {
            throw new NoSuchElementException("Image Not Found");
        }
    }

    public void addImage(ImageItem item) {
        imageRepo.save(item);
    }

    public void deleteImageById(String id) {
        imageRepo.deleteById(id);
    }

    public boolean hasImageId(String id) {
        Optional<ImageItem> img = imageRepo.findById(id);
        return img.isPresent();
    }

    public boolean hasImageName(String name) {
        Optional<ImageItem> img = imageRepo.findByName(name);
        return img.isPresent();
    }

}
