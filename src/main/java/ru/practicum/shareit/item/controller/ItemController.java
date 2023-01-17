package ru.practicum.shareit.item.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ItemController {
    private final ItemService itemService;

    @GetMapping("/{id}")
    public ItemDtoResponse getById(@PathVariable long id,
                                   @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getById(id, userId);
    }

    @GetMapping
    public List<ItemDtoResponse> getAll(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.getAll(userId);
    }

    @PostMapping
    public ItemDto add(@RequestBody @Valid ItemDto itemDto,
                       @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.add(itemDto, userId);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDtoResponse addComment(@RequestBody @Valid CommentDtoRequest commentDtoRequest,
                                         @PathVariable long itemId,
                                         @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.addComment(commentDtoRequest, itemId, userId);
    }

    @PatchMapping("/{id}")
    public ItemDto patch(@PathVariable long id,
                         @RequestBody @Valid ItemPatchDto patchDto,
                         @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemService.patch(id, patchDto, userId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        return text.isBlank() ? Collections.emptyList() : itemService.search(text);
    }
}