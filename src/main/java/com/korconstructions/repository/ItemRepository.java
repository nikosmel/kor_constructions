package com.korconstructions.repository;

import com.korconstructions.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    // JpaRepository provides: findAll(), findById(), save(), deleteById(), existsById()
    // No additional methods needed
}
