package ru.otus.hw.service;

import ru.otus.hw.domain.Answer;
import ru.otus.hw.domain.Question;

public class QuestionFormatterImpl implements QuestionFormatter {

    @Override
    public String format(Question question, int questionIndex) {

        StringBuilder sb = new StringBuilder();

        sb.append("--------------------------------------------------\n\n");
        sb.append(String.format("Question %d: %s%n\n", questionIndex, question.text()));

        int answerIndex = 1;
        for (Answer answer : question.answers()) {
            sb.append(String.format("  %d) %s%n\n", answerIndex++, answer.text()));
        }
        return sb.toString();
    }
}