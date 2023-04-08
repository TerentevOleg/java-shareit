package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDtoRequest;
import ru.practicum.shareit.request.dto.ItemRequestDtoResponse;
import ru.practicum.shareit.request.mapper.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;

    @Override
    @Transactional
    public ItemRequestDtoResponse add(ItemRequestDtoRequest dto, long userId) {
        User requester = getUser(userId);
        ItemRequest request = itemRequestMapper.fromDto(dto);
        request.setRequester(requester);
        request.setCreated(LocalDateTime.now());
        request = itemRequestRepository.save(request);
        log.debug("Add item request {}", request);
        return itemRequestMapper.toDto(request);
    }

    @Override
    public ItemRequestDto findById(long id, long userId) {
        getUser(userId);
        ItemRequest itemRequest = getItemRequest(id);
        List<Item> items = itemRepository.findAllByRequest(itemRequest);
        return itemRequestMapper.toRequestDto(itemRequest, items);
    }

    @Override
    public List<ItemRequestDto> findByRequester(long requesterId) {
        User requester = getUser(requesterId);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequester(requester,
                Sort.by(Sort.Direction.DESC, "created"));
        List<Item> items = itemRepository.findAllByRequestIn(itemRequests);
        return itemRequestMapper.toRequestDto(itemRequests, formItemsByRequestIds(items));
    }

    @Override
    public List<ItemRequestDto> findByOtherUsers(long userId, long from, int size) {
        User exceptedRequester = getUser(userId);
        Sort sort = Sort.by(Sort.Direction.DESC, "created");
        Pageable pageable = PageRequest.of((int) (from / size), size, sort);
        List<ItemRequest> itemRequests = itemRequestRepository.findAllByRequesterIsNot(exceptedRequester, pageable);
        List<Item> items = itemRepository.findAllByRequestIn(itemRequests);
        return itemRequestMapper.toRequestDto(itemRequests, formItemsByRequestIds(items));
    }

    private ItemRequest getItemRequest(long requestId) {
        return itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Item request with id=" + requestId + " not found."));
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found."));
    }

    private Map<Long, List<Item>> formItemsByRequestIds(List<Item> allItems) {
        return allItems.stream()
                .collect(Collectors.groupingBy(item -> item.getRequest().getId(), Collectors.toList()));
    }
}
