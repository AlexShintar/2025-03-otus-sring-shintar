package ru.otus.hw.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.otus.hw.dao.QuestionDao;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.domain.Student;
import ru.otus.hw.domain.TestResult;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = TestServiceImpl.class)
class TestServiceImplTest {

    private static final String PROMPT_SELECT = "Select answer number:";
    private static final String ERROR_SELECT = "Invalid choice. Enter a number between %d and %d";
    private static final String ERROR_NO_QUEST = "No questions found";

    @MockitoBean
    private LocalizedIOService ioService;

    @MockitoBean
    private QuestionDao questionDao;

    @Autowired
    private TestServiceImpl testService;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        testStudent = new Student("Bob", "Smith");
    }

    @Test
    @DisplayName("Should throw QuestionReadException when no questions are returned")
    void shouldThrowWhenNoQuestions() {
        when(questionDao.findAll()).thenReturn(Collections.emptyList());
        when(ioService.getMessage("TestService.error.noQuestions"))
                .thenReturn(ERROR_NO_QUEST);

        QuestionReadException ex = assertThrows(
                QuestionReadException.class,
                () -> testService.executeTestFor(testStudent)
        );
        assertEquals(ERROR_NO_QUEST, ex.getMessage());
    }

    @Test
    @DisplayName("Should ask questions and return correct TestResult")
    void shouldAskQuestionsAndReturnCorrectResult() {

        Answer a1 = new Answer("Science doesn't know this yet", true);
        Answer a2 = new Answer("Absolutely not", false);
        Question q1 = new Question("Is there life on Mars?", List.of(a1, a2));


        when(questionDao.findAll()).thenReturn(List.of(q1));


        when(ioService.getMessage("TestService.prompt.select"))
                .thenReturn(PROMPT_SELECT);
        String formattedError = String.format(ERROR_SELECT, 1, 2);
        when(ioService.getMessage("TestService.error.select", 1, 2))
                .thenReturn(formattedError);


        when(ioService.readIntForRangeWithPrompt(
                eq(1), eq(2),
                eq(PROMPT_SELECT),
                eq(formattedError)
        )).thenReturn(1);


        TestResult result = testService.executeTestFor(testStudent);


        assertEquals(testStudent, result.getStudent());
        assertEquals(1, result.getAnsweredQuestions().size());
        assertEquals(1, result.getRightAnswersCount());
        assertEquals(q1, result.getAnsweredQuestions().get(0));
    }
}
