package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
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

    private static final String PROMPT_START = "Please answer the questions below%n";

    private static final String PROMPT_SELECT = "Select answer number:";

    private static final String PROMPT_QUESTION = "Question %d: %s";

    private static final String ERROR_SELECT = "Invalid choice. Enter a number between %d and %d";

    private static final String ERROR_NO_QUEST = "No questions found";

    private final IOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printFormattedLine(PROMPT_START);

        var questions = questionDao.findAll();
        if (questions.isEmpty()) {
            throw new QuestionReadException(ERROR_NO_QUEST);
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
        String errorSelect = String.format(ERROR_SELECT, 1, max);

        int choice = ioService.readIntForRangeWithPrompt(
                1, max, PROMPT_SELECT, errorSelect
        );
        boolean isAnswerValid = question.answers().get(choice - 1).isCorrect();
        result.applyAnswer(question, isAnswerValid);
    }

    private void printQuestionWithAnswers(Question question, int index) {
        ioService.printFormattedLine(PROMPT_QUESTION, index, question.text());
        List<Answer> answers = question.answers();
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("  %d) %s", i + 1, answers.get(i).text());
        }
    }
}