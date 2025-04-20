package ru.otus.hw.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class TestServiceImplTest {

    @Mock
    private IOService ioService;

    @Mock
    private QuestionDao questionDao;

    @Mock
    private QuestionFormatter questionFormatter;

    @InjectMocks
    private TestServiceImpl testService;

    @Test
    public void testExecuteTestShouldPrintQuestions() {

        Question question1 = new Question("Is there life on Mars?",
                Arrays.asList(new Answer("Science doesn't know this yet", true)));
        Question question2 = new Question("How should resources be loaded form jar in Java?",
                Arrays.asList(new Answer("ClassLoader#geResource#getFile + FileReader", false)));

        List<Question> questions = Arrays.asList(question1, question2);


        when(questionDao.findAll()).thenReturn(questions);

        when(questionFormatter.format(question1, 1))
                .thenReturn("Question 1: Is there life on Mars?");
        when(questionFormatter.format(question2, 2))
                .thenReturn("Question 2: How should resources be loaded form jar in Java?");


        testService.executeTest();

        InOrder inOrder = inOrder(ioService);
        inOrder.verify(ioService).printLine("");
        inOrder.verify(ioService).printFormattedLine("Please answer the questions below%n");
        inOrder.verify(ioService).printLine("Question 1: Is there life on Mars?");
        inOrder.verify(ioService).printLine("Question 2: How should resources be loaded form jar in Java?");
        verifyNoMoreInteractions(ioService);
    }

    @Test
    public void testExecuteTestThrowsExceptionWhenNoQuestions() {

        when(questionDao.findAll()).thenReturn(Collections.emptyList());

        QuestionReadException exception = assertThrows(QuestionReadException.class,
                () -> testService.executeTest());
        assertEquals("No questions found.", exception.getMessage());
    }
}