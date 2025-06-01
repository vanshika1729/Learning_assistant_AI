package com.learningassistant.services;

import com.learningassistant.models.*;
import com.learningassistant.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final QuizAttemptRepository attemptRepository;
    private final UserAnswerRepository userAnswerRepository;
    private final TopicRepository topicRepository;
    private final LearningMaterialRepository learningMaterialRepository;

    @Autowired
    public QuizService(QuizRepository quizRepository,
                       QuestionRepository questionRepository,
                       AnswerRepository answerRepository,
                       QuizAttemptRepository attemptRepository,
                       UserAnswerRepository userAnswerRepository,
                       TopicRepository topicRepository,
                       LearningMaterialRepository learningMaterialRepository) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.attemptRepository = attemptRepository;
        this.userAnswerRepository = userAnswerRepository;
        this.topicRepository = topicRepository;
        this.learningMaterialRepository = learningMaterialRepository;
    }

    /**
     * Get all quizzes in the system
     *
     * @return List of all quizzes
     */
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    /**
     * Get quizzes filtered by topic
     *
     * @param topicId The ID of the topic to filter by
     * @return List of quizzes for the given topic
     */
    public List<Quiz> getQuizzesByTopic(Long topicId) {
        Optional<Topic> topicOpt = topicRepository.findById(topicId);

        if (topicOpt.isPresent()) {
            Topic topic = topicOpt.get();
            return quizRepository.findByTopic(topic);
        }

        return new ArrayList<>();
    }

    /**
     * Get a quiz by its ID
     *
     * @param quizId The ID of the quiz to retrieve
     * @return Optional containing the quiz if found
     */
    public Optional<Quiz> getQuizById(Long quizId) {
        return quizRepository.findById(quizId);
    }

    /**
     * Start a new quiz attempt for a user
     *
     * @param user The user taking the quiz
     * @param quizId The ID of the quiz to start
     * @return The created quiz attempt
     */
    public QuizAttempt startQuiz(User user, Long quizId) {
        Optional<Quiz> quizOpt = quizRepository.findById(quizId);

        if (quizOpt.isPresent()) {
            Quiz quiz = quizOpt.get();

            QuizAttempt attempt = new QuizAttempt();
            attempt.setUser(user);
            attempt.setQuiz(quiz);
            attempt.setStartTime(LocalDateTime.now());
            attempt.setStatus("IN_PROGRESS");

            return attemptRepository.save(attempt);
        }

        throw new IllegalArgumentException("Quiz not found");
    }

    /**
     * Get a quiz attempt by its ID
     *
     * @param attemptId The ID of the attempt to retrieve
     * @return Optional containing the attempt if found
     */
    public Optional<QuizAttempt> getAttemptById(Long attemptId) {
        return attemptRepository.findById(attemptId);
    }

    /**
     * Save a user's answer to a question
     *
     * @param attemptId The ID of the quiz attempt
     * @param questionId The ID of the question being answered
     * @param answerId The ID of the selected answer (for multiple choice)
     * @param textAnswer The text answer (for free text questions)
     * @param timeSpent Time spent on the question in seconds
     * @return The saved user answer
     */
    @Transactional
    public UserAnswer saveUserAnswer(Long attemptId, Long questionId, Long answerId,
                                     String textAnswer, Integer timeSpent) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz attempt not found"));

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));

        // Check if the user already answered this question
        Optional<UserAnswer> existingAnswer = userAnswerRepository
                .findByAttemptAndQuestion(attempt, question);

        UserAnswer userAnswer;

        if (existingAnswer.isPresent()) {
            userAnswer = existingAnswer.get();
        } else {
            userAnswer = new UserAnswer();
            userAnswer.setAttempt(attempt);
            userAnswer.setQuestion(question);
        }

        // Set answer for multiple choice questions
        if (answerId != null) {
            Optional<Answer> selectedAnswer = answerRepository.findById(answerId);
            if (selectedAnswer.isPresent()) {
                userAnswer.setSelectedAnswer(selectedAnswer.get());
                userAnswer.setIsCorrect(selectedAnswer.get().getIsCorrect());
            }
        }

        // Set text answer for free text questions
        if (textAnswer != null && !textAnswer.isEmpty()) {
            userAnswer.setTextAnswer(textAnswer);

            // For text answers, correctness might need to be evaluated differently
            // For now, marking as false until manually reviewed or auto-graded
            userAnswer.setIsCorrect(false);
        }

        userAnswer.setTimeSpent(timeSpent);
        return userAnswerRepository.save(userAnswer);
    }

    /**
     * Finish a quiz attempt and calculate the score
     *
     * @param attemptId The ID of the attempt to finish
     * @return The updated quiz attempt with score
     */
    @Transactional
    public QuizAttempt finishQuiz(Long attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz attempt not found"));

        // Set end time
        attempt.setEndTime(LocalDateTime.now());
        attempt.setStatus("COMPLETED");

        // Calculate score
        List<UserAnswer> answers = userAnswerRepository.findByAttempt(attempt);
        int totalQuestions = attempt.getQuiz().getQuestions().size();
        long correctAnswers = answers.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsCorrect()))
                .count();

        double scorePercentage = totalQuestions > 0
                ? (correctAnswers * 100.0) / totalQuestions
                : 0;

        attempt.setScore(scorePercentage);

        return attemptRepository.save(attempt);
    }

    /**
     * Get all answers for a quiz attempt
     *
     * @param attemptId The ID of the attempt
     * @return List of user answers for the attempt
     */
    public List<UserAnswer> getAttemptAnswers(Long attemptId) {
        QuizAttempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz attempt not found"));

        return userAnswerRepository.findByAttempt(attempt);
    }

    /**
     * Get all quiz attempts for a user
     *
     * @param user The user to get attempts for
     * @return List of quiz attempts for the user
     */
    public List<QuizAttempt> getUserAttempts(User user) {
        return attemptRepository.findByUserOrderByStartTimeDesc(user);
    }

    /**
     * Create a new quiz
     *
     * @param quiz The quiz to create
     * @return The saved quiz
     */
    @Transactional
    public Quiz createQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    /**
     * Update an existing quiz
     *
     * @param quiz The updated quiz
     * @return The saved quiz
     */
    @Transactional
    public Quiz updateQuiz(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    /**
     * Delete a quiz
     *
     * @param quizId The ID of the quiz to delete
     */
    @Transactional
    public void deleteQuiz(Long quizId) {
        quizRepository.deleteById(quizId);
    }

    /**
     * Get quizzes based on difficulty level
     *
     * @param minLevel Minimum difficulty level
     * @param maxLevel Maximum difficulty level
     * @return List of quizzes within the difficulty range
     */
    public List<Quiz> getQuizzesByDifficultyRange(int minLevel, int maxLevel) {
        return quizRepository.findByDifficultyLevelBetween(minLevel, maxLevel);
    }

    /**
     * Get quizzes for a list of topics
     *
     * @param topics List of topics to find quizzes for
     * @return List of quizzes related to the given topics
     */
    public List<Quiz> getQuizzesForTopics(List<Topic> topics) {
        return quizRepository.findByTopicIn(topics);
    }

    /**
     * Get weak topics for a user based on quiz results
     *
     * @param user The user to analyze
     * @param attemptId The quiz attempt ID to analyze
     * @return List of topics the user is weak in
     */
    public List<Topic> getWeakTopicsForUser(User user, Long attemptId) {
        QuizAttempt currentAttempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz attempt not found"));

        // Get user's answers from this attempt
        List<UserAnswer> answers = userAnswerRepository.findByAttempt(currentAttempt);

        // Group incorrect answers by sub-topic
        Map<Topic, Long> incorrectByTopic = answers.stream()
                .filter(a -> Boolean.FALSE.equals(a.getIsCorrect()))
                .collect(Collectors.groupingBy(
                        a -> a.getQuestion().getTopic() != null ?
                                a.getQuestion().getTopic() :
                                a.getQuestion().getQuiz().getTopic(),
                        Collectors.counting()
                ));

        // Find topics with at least 2 incorrect answers
        List<Topic> weakTopics = incorrectByTopic.entrySet().stream()
                .filter(e -> e.getValue() >= 2)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // If no weak topics found, use the quiz's main topic
        if (weakTopics.isEmpty()) {
            weakTopics.add(currentAttempt.getQuiz().getTopic());
        }

        return weakTopics;
    }

    /**
     * Get recommended learning materials for a list of topics
     *
     * @param topics List of topics to get materials for
     * @return List of learning materials for the given topics
     */
    public List<LearningMaterial> getRecommendedMaterialsForTopics(List<Topic> topics) {
        return learningMaterialRepository.findByTopicInOrderByDifficultyLevelAsc(topics);
    }
}