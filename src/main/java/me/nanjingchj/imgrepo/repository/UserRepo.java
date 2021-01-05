package me.nanjingchj.imgrepo.repository;

import me.nanjingchj.imgrepo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
}
