// StudentProfileRepository.java
package com.learningassistant.repositories;

import com.learningassistant.models.StudentProfile;
import com.learningassistant.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentProfileRepository extends JpaRepository<StudentProfile, Long> {
    Optional<StudentProfile> findByUser(User user);
}
