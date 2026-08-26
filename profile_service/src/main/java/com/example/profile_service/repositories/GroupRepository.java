package com.example.profile_service.repositories;

import com.example.profile_service.entities.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByProgram_ProgramId(Long programId);
}
