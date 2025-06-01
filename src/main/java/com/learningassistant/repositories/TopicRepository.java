// TopicRepository.java
package com.learningassistant.repositories;

import com.learningassistant.models.Subject;
import com.learningassistant.models.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    List<Topic> findBySubject(Subject subject);
    List<Topic> findByNameContainingIgnoreCase(String name);
}