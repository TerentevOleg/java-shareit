package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;


import javax.validation.constraints.NotBlank;

@Value
public class ItemRequestDtoRequest {

    @NotBlank
    String description;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public ItemRequestDtoRequest(@JsonProperty("description") String description) {
        this.description = description;
    }
}

