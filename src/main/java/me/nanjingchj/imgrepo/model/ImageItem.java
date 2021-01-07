package me.nanjingchj.imgrepo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "images")
@NoArgsConstructor
public class ImageItem {
    public ImageItem(String name, String owner, boolean isPublic, Set<String> accessibleToIds, byte[] image) {
        this.name = name;
        this.owner = owner;
        if (isPublic) {
            makePublic();
        } else {
            setAccessibleTo(accessibleToIds);
        }
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
    @Column(name = "owner")
    private String owner;

    @Column(name = "accessible_to")
    private String accessibleTo;

    public Set<String> getAccessibleTo() {
        return new HashSet<>(Arrays.asList(accessibleTo.split(";")));
    }

    private String setToString(Set<String> userIds) {
        StringBuilder sb = new StringBuilder();
        for (String id : userIds) {
            sb.append(id).append(";");
        }
        sb.setLength(Math.max(sb.length() - 1, 0)); // remove trailing semicolon
        return sb.toString();
    }

    public void setAccessibleTo(Set<String> userIds) {
        userIds.add(owner);
        accessibleTo = setToString(userIds);
    }

    public void makePublic() {
        accessibleTo = "all";
    }

    public void makeFullyPrivate() {
        accessibleTo = owner;
    }

    public boolean isPublic() {
        return accessibleTo.equals("all");
    }

    public void addAccessibleTo(String userId) {
        if (isPublic()) return;
        Set<String> ids = getAccessibleTo();
        if (ids.contains(userId)) return;
        ids.add(userId);
        setAccessibleTo(ids);
    }

    public boolean isAccessibleTo(String userId) {
        if (isPublic()) {
            return true;
        }
        return getAccessibleTo().contains(userId);
    }

    public void removeAccessibleTo(String userId) {
        Set<String> accessibleToUsers = getAccessibleTo();
        if (!accessibleToUsers.contains(userId)) return;
        accessibleToUsers.remove(userId);
        setAccessibleTo(accessibleToUsers);
    }

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
