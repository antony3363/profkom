package com.example.profile_service.repositories;

import com.example.profile_service.entities.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    List<UserProfile> findByGroup_GroupId(Long groupId);
    List<UserProfile> findByLichnostIdIn(List<Long> lichnostIds);
    boolean existsByEmail(String email);
    boolean existsByCardNumber(String cardNumber);
}
