package ru.practicum.shareit.request.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.service.ItemRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequestMapping(path = "/requests")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Validated
public class ItemRequestController {

    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDtoResponse add(@RequestBody @Valid ItemRequestDtoRequest itemRequestDto,
                                      @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.add(itemRequestDto, userId);
    }

    @GetMapping
    public List<ItemRequestDto> findByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.findByRequester(userId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> findAllByOtherUsers(
            @RequestParam(defaultValue = "0") @PositiveOrZero Long from,
            @RequestParam(defaultValue = "10") @Positive Integer size,
            @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.findByOtherUsers(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto findById(@PathVariable long requestId,
                                              @RequestHeader("X-Sharer-User-Id") long userId) {
        return itemRequestService.findById(requestId, userId);
    }





}
