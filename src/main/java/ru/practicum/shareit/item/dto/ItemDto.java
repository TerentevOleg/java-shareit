package ru.practicum.shareit.item.dto;

import lombok.Value;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Value
public class ItemDto {
    Long id;

    @NotBlank(groups = FullValidated.class)
    @NullableNotBlank(groups = PatchValidated.class)
    String name;

    @NotBlank(groups = FullValidated.class)
    @NullableNotBlank(groups = PatchValidated.class)
    String description;

    @NotNull(groups = FullValidated.class)
    Boolean available;
}
