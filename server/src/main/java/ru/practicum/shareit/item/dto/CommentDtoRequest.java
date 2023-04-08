package ru.practicum.shareit.item.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class CommentDtoRequest {

    String text;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CommentDtoRequest(@JsonProperty("text") String text) {
        this.text = text;
    }
}