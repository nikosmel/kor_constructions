package com.korconstructions.repository;

import com.korconstructions.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // JpaRepository provides: findAll(), findById(), save(), deleteById(), existsById()
    // No additional methods needed
}
