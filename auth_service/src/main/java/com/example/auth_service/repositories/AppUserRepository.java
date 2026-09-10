package com.example.auth_service.repositories;

import com.example.auth_service.entities.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByLichnostId(long lichnostId);
    boolean existsByLichnostId(long lichnostId);
}
