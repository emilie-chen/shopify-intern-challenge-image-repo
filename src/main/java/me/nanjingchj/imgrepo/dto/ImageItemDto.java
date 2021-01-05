package me.nanjingchj.imgrepo.dto;

import lombok.Value;
import me.nanjingchj.imgrepo.model.ImageItem;

@Value
public class ImageItemDto {
    public ImageItemDto(ImageItem img) {
        this.id = img.getId();
        this.name = img.getName();
    }

    String id;
    String name;
}
