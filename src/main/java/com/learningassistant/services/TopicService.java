// TopicService.java
package com.learningassistant.services;

import com.learningassistant.models.Subject;
import com.learningassistant.models.Topic;
import com.learningassistant.repositories.TopicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class TopicService {
    private final TopicRepository topicRepository;

    @Autowired
    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }

    public List<Topic> getTopicsBySubject(Subject subject) {
        return topicRepository.findBySubject(subject);
    }

    public Optional<Topic> getTopicById(Long id) {
        return topicRepository.findById(id);
    }

    public List<Topic> searchTopics(String query) {
        return topicRepository.findByNameContainingIgnoreCase(query);
    }

    @Transactional
    public Topic createTopic(Topic topic) {
        return topicRepository.save(topic);
    }

    @Transactional
    public Topic updateTopic(Topic topic) {
        return topicRepository.save(topic);
    }

    @Transactional
    public void deleteTopic(Long id) {
        topicRepository.deleteById(id);
    }
}
