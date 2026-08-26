package com.example.profile_service.repositories;

import com.example.profile_service.entities.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramRepository extends JpaRepository<Program, Long> {
    List<Program> findBySchool_SchoolId(Long schoolId);
}
