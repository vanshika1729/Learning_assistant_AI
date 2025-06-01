package com.learningassistant.controllers;

import com.learningassistant.models.Quiz;
import com.learningassistant.models.QuizAttempt;
import com.learningassistant.models.User;
import com.learningassistant.models.UserAnswer;
import com.learningassistant.services.QuizService;
import com.learningassistant.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/quiz")
public class QuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private UserService userService;

    /**
     * Display all available quizzes
     */
    @GetMapping("/list")
    public String listQuizzes(Model model) {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        model.addAttribute("quizzes", quizzes);
        return "quiz/list";
    }

    /**
     * Display quizzes filtered by topic
     */
    @GetMapping("/topic/{topicId}")
    public String listQuizzesByTopic(@PathVariable Long topicId, Model model) {
        model.addAttribute("quizzes", quizService.getQuizzesByTopic(topicId));
        model.addAttribute("topicId", topicId);
        return "quiz/list";
    }

    /**
     * Display quiz details before starting
     */
    @GetMapping("/{quizId}")
    public String showQuizDetails(@PathVariable Long quizId, Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(quizId);

        if (quizOpt.isPresent()) {
            Quiz quiz = quizOpt.get();
            model.addAttribute("quiz", quiz);
            return "quiz/details";
        } else {
            return "redirect:/quiz/list";
        }
    }

    /**
     * Start a quiz and show the quiz interface
     */
    @GetMapping("/{quizId}/start")
    public String startQuiz(@PathVariable Long quizId,
                            @AuthenticationPrincipal User user,
                            Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(quizId);

        if (quizOpt.isPresent()) {
            Quiz quiz = quizOpt.get();

            // Create a new quiz attempt
            QuizAttempt attempt = quizService.startQuiz(user, quizId);

            model.addAttribute("quiz", quiz);
            model.addAttribute("attempt", attempt);

            return "quiz/take";
        } else {
            return "redirect:/quiz/list";
        }
    }

    /**
     * Process the user's answers and submit the quiz
     */
    @PostMapping("/submit/{attemptId}")
    public String submitQuiz(@PathVariable Long attemptId,
                             @RequestParam Map<String, String> formData,
                             @AuthenticationPrincipal User user,
                             RedirectAttributes redirectAttributes) {

        Optional<QuizAttempt> attemptOpt = quizService.getAttemptById(attemptId);

        if (attemptOpt.isPresent() && attemptOpt.get().getUser().getId().equals(user.getId())) {
            QuizAttempt attempt = attemptOpt.get();

            // Process each question
            for (Map.Entry<String, String> entry : formData.entrySet()) {
                String paramName = entry.getKey();
                String paramValue = entry.getValue();

                // Process multiple choice answers
                if (paramName.startsWith("question_")) {
                    Long questionId = Long.parseLong(paramName.substring(9));
                    Long answerId = Long.parseLong(paramValue);

                    // Get time spent on this question (if available)
                    Integer timeSpent = null;
                    String timeKey = "timeSpent_" + questionId;
                    if (formData.containsKey(timeKey)) {
                        timeSpent = Integer.parseInt(formData.get(timeKey));
                    }

                    quizService.saveUserAnswer(attemptId, questionId, answerId, null, timeSpent != null ? timeSpent : 0);
                }

                // Process text answers
                else if (paramName.startsWith("text_answer_")) {
                    Long questionId = Long.parseLong(paramName.substring(12));
                    String textAnswer = paramValue;

                    // Get time spent on this question (if available)
                    Integer timeSpent = null;
                    String timeKey = "timeSpent_" + questionId;
                    if (formData.containsKey(timeKey)) {
                        timeSpent = Integer.parseInt(formData.get(timeKey));
                    }

                    quizService.saveUserAnswer(attemptId, questionId, null, textAnswer, timeSpent != null ? timeSpent : 0);
                }
            }

            // Finish the quiz and calculate score
            attempt = quizService.finishQuiz(attemptId);

            redirectAttributes.addFlashAttribute("score", attempt.getScore());
            return "redirect:/quiz/result/" + attemptId;
        }

        return "redirect:/quiz/list";
    }

    /**
     * Display quiz results
     */
    @GetMapping("/result/{attemptId}")
    public String showQuizResult(@PathVariable Long attemptId,
                                 @AuthenticationPrincipal User user,
                                 Model model) {

        Optional<QuizAttempt> attemptOpt = quizService.getAttemptById(attemptId);

        if (attemptOpt.isPresent() && attemptOpt.get().getUser().getId().equals(user.getId())) {
            QuizAttempt attempt = attemptOpt.get();
            List<UserAnswer> userAnswers = quizService.getAttemptAnswers(attemptId);

            model.addAttribute("attempt", attempt);
            model.addAttribute("userAnswers", userAnswers);
            model.addAttribute("quiz", attempt.getQuiz());

            return "quiz/result";
        }

        return "redirect:/dashboard";
    }

    /**
     * Show user's quiz history
     */
    @GetMapping("/history")
    public String showQuizHistory(@AuthenticationPrincipal User user, Model model) {
        List<QuizAttempt> attempts = quizService.getUserAttempts(user);
        model.addAttribute("attempts", attempts);
        return "quiz/history";
    }

    // Admin endpoints for quiz management

    @GetMapping("/admin/create")
    public String showCreateQuizForm(Model model) {
        model.addAttribute("quiz", new Quiz());
        // Add topics for dropdown
        return "quiz/admin/create";
    }

    @PostMapping("/admin/create")
    public String createQuiz(@ModelAttribute Quiz quiz, RedirectAttributes redirectAttributes) {
        Quiz savedQuiz = quizService.createQuiz(quiz);
        redirectAttributes.addFlashAttribute("message", "Quiz created successfully!");
        return "redirect:/quiz/admin/edit/" + savedQuiz.getId();
    }

    @GetMapping("/admin/edit/{quizId}")
    public String showEditQuizForm(@PathVariable Long quizId, Model model) {
        Optional<Quiz> quizOpt = quizService.getQuizById(quizId);

        if (quizOpt.isPresent()) {
            model.addAttribute("quiz", quizOpt.get());
            return "quiz/admin/edit";
        }

        return "redirect:/quiz/admin/list";
    }

    @PostMapping("/admin/edit/{quizId}")
    public String updateQuiz(@PathVariable Long quizId,
                             @ModelAttribute Quiz quiz,
                             RedirectAttributes redirectAttributes) {
        quizService.updateQuiz(quiz);
        redirectAttributes.addFlashAttribute("message", "Quiz updated successfully!");
        return "redirect:/quiz/admin/list";
    }

    @GetMapping("/admin/delete/{quizId}")
    public String deleteQuiz(@PathVariable Long quizId, RedirectAttributes redirectAttributes) {
        quizService.deleteQuiz(quizId);
        redirectAttributes.addFlashAttribute("message", "Quiz deleted successfully!");
        return "redirect:/quiz/admin/list";
    }

    @GetMapping("/admin/list")
    public String listAllQuizzes(Model model) {
        model.addAttribute("quizzes", quizService.getAllQuizzes());
        return "quiz/admin/list";
    }
}