package ru.otus.hw.dao;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import ru.otus.hw.config.TestFileNameProvider;
import ru.otus.hw.dao.dto.QuestionDto;
import ru.otus.hw.domain.Question;
import ru.otus.hw.exceptions.QuestionReadException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CsvQuestionDao implements QuestionDao {
    private final TestFileNameProvider fileNameProvider;

    @Override
    public List<Question> findAll() {
        String fileName = fileNameProvider.getTestFileName();
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new QuestionReadException("File name is not provided");
        }
        ClassLoader classLoader = getClass().getClassLoader();
        InputStream resourceStream = classLoader.getResourceAsStream(fileName);
        if (resourceStream == null) {
            throw new QuestionReadException("Resource not found: " + fileName);
        }
        try (Reader reader = new InputStreamReader(resourceStream, StandardCharsets.UTF_8)) {
            CsvToBean<QuestionDto> csvToBean = new CsvToBeanBuilder<QuestionDto>(reader)
                    .withType(QuestionDto.class)
                    .withSkipLines(1)
                    .withSeparator(';')
                    .build();

            return csvToBean.parse()
                    .stream()
                    .map(QuestionDto::toDomainObject)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new QuestionReadException("Error reading CSV file", e);
        }
    }
}
