package ru.otus.hw.converters;

import org.springframework.stereotype.Component;
import ru.otus.hw.dto.CommentDto;
import ru.otus.hw.models.Comment;

@Component
public class CommentConverter {
    public String commentToString(CommentDto commentDto) {
        return "Id: %d, for book with Id: %d, content: %s".formatted(
                commentDto.getId(),
                commentDto.getBookId(),
                commentDto.getContent()
        );
    }

    public CommentDto toDto(Comment comment) {
        return new CommentDto(comment.getId(), comment.getContent(), comment.getBook().getId());
    }
}