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

    private final LocalizedIOService ioService;

    private final QuestionDao questionDao;

    @Override
    public TestResult executeTestFor(Student student) {
        ioService.printLine("");
        ioService.printLineLocalized("TestService.answer.the.questions");
        ioService.printLine("");

        var questions = questionDao.findAll();
        if (questions.isEmpty()) {
            throw new QuestionReadException(ioService.getMessage("TestService.error.noQuestions"));
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
        String prompt = ioService.getMessage("TestService.prompt.select");
        String errorMessage = ioService.getMessage("TestService.error.select", 1, max);

        int choice = ioService.readIntForRangeWithPrompt(1, max, prompt, errorMessage);

        boolean isAnswerValid = question.answers().get(choice - 1).isCorrect();
        result.applyAnswer(question, isAnswerValid);
    }

    private void printQuestionWithAnswers(Question question, int index) {
        ioService.printFormattedLineLocalized("TestService.prompt.question", index, question.text());
        List<Answer> answers = question.answers();
        for (int i = 0; i < answers.size(); i++) {
            ioService.printFormattedLine("  %d) %s", i + 1, answers.get(i).text());
        }
    }
}
