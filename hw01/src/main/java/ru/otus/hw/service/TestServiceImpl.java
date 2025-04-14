package ru.otus.hw.service;

import lombok.RequiredArgsConstructor;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

@RequiredArgsConstructor
public class TestServiceImpl implements TestService {

    private final IOService ioService;

    private final QuestionDao questionDao;

    private final QuestionFormatter questionFormatter;

    @Override
    public void executeTest() {
        ioService.printLine("");
        ioService.printFormattedLine("Please answer the questions below%n");

        var questions = questionDao.findAll();

        if (!questions.isEmpty()) {
            int questionIndex = 1;
            for (Question question : questions) {
                ioService.printLine(questionFormatter.format(question, questionIndex++));
            }
        } else {
            throw new QuestionReadException("No questions found.");
        }
    }
}
