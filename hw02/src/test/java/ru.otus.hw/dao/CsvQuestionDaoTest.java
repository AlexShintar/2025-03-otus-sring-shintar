package ru.otus.hw.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CsvQuestionDaoTest {

    private static final String EXISTING_CSV = "questionsTest.csv";
    private static final String MISSING_CSV = "nonexistent.csv";

    @Mock
    private TestFileNameProvider fileNameProvider;

    private CsvQuestionDao dao;

    @BeforeEach
    void setUp() {
        dao = new CsvQuestionDao(fileNameProvider);
    }

    @Test
    @DisplayName("Should load questions and answers from CSV resource when file exists")
    void shouldLoadQuestionsFromCsvResource() {
        when(fileNameProvider.getTestFileName()).thenReturn(EXISTING_CSV);

        List<Question> questions = dao.findAll();

        assertFalse(questions.isEmpty(), "Questions list should not be empty");

        Question q = questions.get(0);
        assertEquals("Is chocolate a vegetable?", q.text());

        List<Answer> answers = q.answers();
        assertEquals(3, answers.size(), "There should be 3 answers");

        assertEquals("It grows on trees, so yes — let’s go with that", answers.get(0).text());
        assertFalse(answers.get(0).isCorrect());

        assertEquals("Only in dreams and food pyramids", answers.get(1).text());
        assertFalse(answers.get(1).isCorrect());

        assertEquals("Unfortunately, no", answers.get(2).text());
        assertTrue(answers.get(2).isCorrect());
    }

    @Test
    @DisplayName("Should throw QuestionReadException when file name is blank")
    void shouldThrowWhenFileNameEmpty() {
        when(fileNameProvider.getTestFileName()).thenReturn("   ");

        QuestionReadException ex = assertThrows(
                QuestionReadException.class,
                dao::findAll,
                "Expected exception when fileName is blank"
        );
        assertEquals("File name is not provided", ex.getMessage());
    }

    @Test
    @DisplayName("Should throw QuestionReadException when resource is missing")
    void shouldThrowWhenResourceNotFound() {
        when(fileNameProvider.getTestFileName()).thenReturn(MISSING_CSV);

        QuestionReadException ex = assertThrows(
                QuestionReadException.class,
                dao::findAll,
                "Expected exception when resource is not found"
        );
        assertTrue(
                ex.getMessage().contains("Resource not found"),
                "Exception message should mention missing resource"
        );
    }
}
