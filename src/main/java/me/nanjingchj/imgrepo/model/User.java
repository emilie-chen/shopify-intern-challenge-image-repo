package me.nanjingchj.imgrepo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
public class User {
    public User(String username, String passwordDigest) {
        this.username = username;
        this.password = passwordDigest;
    }

    @Id
    @Getter
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "uuid2")
    @Column(name = "id", unique = true)
    private String id;

    @Getter
    @Setter
    @Column(name = "username", unique = true)
    private String username;

    @Getter
    @Setter
    @Column(name = "password")
    private String password;
}
