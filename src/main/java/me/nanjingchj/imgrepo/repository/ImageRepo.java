package me.nanjingchj.imgrepo.repository;

import me.nanjingchj.imgrepo.model.ImageItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepo extends JpaRepository<ImageItem, String> {
    Optional<ImageItem> findByName(String name);
}
