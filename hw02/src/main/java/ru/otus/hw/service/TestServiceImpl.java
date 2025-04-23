package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Value("${TestService.prompt.start}")
    private String promptStart;

    @Value("${TestService.prompt.select}")
    private String promptSelect;

    @Value("${TestService.prompt.question}")
    private String promptQuestion;

    @Value("${TestService.error.select}")
    private String errorSelectPattern;

    @Value("${TestService.error.noQuestions}")
    private String errorNoQuestions;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine(promptStart);

        var questions = questionDao.findAll();
        if (questions.isEmpty()) {
            throw new QuestionReadException(errorNoQuestions);
        }
        var testResult = new TestResult(student);
        int questionIndex = 1;

        for (var question : questions) {
            askAndEvaluateQuestion(question, testResult, questionIndex++);
        }
        return testResult;
    }

    private void askAndEvaluateQuestion(Question question, TestResult result, int index) {
        printQuestionWithAnswers(question, index);

        int max = question.answers().size();
        String errorSelect = String.format(errorSelectPattern, 1, max);

        int choice = ioService.readIntForRangeWithPrompt(
                1, max, promptSelect, errorSelect
        );
        boolean isAnswerValid = question.answers().get(choice - 1).isCorrect();
        result.applyAnswer(question, isAnswerValid);
    }

    private void printQuestionWithAnswers(Question question, int index) {
        ioService.printFormattedLine(promptQuestion, index, question.text());
        List<Answer> answers = question.answers();
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("  %d) %s", i + 1, answers.get(i).text());
        }
    }
}