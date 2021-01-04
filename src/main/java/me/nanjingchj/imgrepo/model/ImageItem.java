package me.nanjingchj.imgrepo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "images")
@NoArgsConstructor
public class ImageItem {
    public ImageItem(String name, byte[] image) {
        this.name = name;
        this.image = image;
    }

    @Id
    @Getter
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;

    @Getter
    @Setter
    @Column(name = "name")
    private String name;

    @Getter
    @Setter
    @Lob
    @Column(name = "image", columnDefinition="BLOB")
    private byte[] image;
}
