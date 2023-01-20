package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AuthenticationErrorException;
import ru.practicum.shareit.exception.CustomValidationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemMapper itemMapper;
    private final CommentMapper commentMapper;

    @Override
    public ItemDtoResponse getById(long id, long userId) {
        getUser(userId);
        Item item = getItem(id);
        return item.getOwner().getId().equals(userId) ?
                formDtoResponseWithBookings(item) :
                formDtoResponse(item);
    }

    @Override
    public List<ItemDtoResponse> getAll(long userId) {
        User owner = getUser(userId);
        List<Item> items = itemRepository.findAllByOwnerOrderByIdAsc(owner);
        List<Comment> comments = commentRepository.findAllByItemIn(items);
        Map<Item, List<Comment>> commentsByItems = comments.stream()
                .collect(Collectors.groupingBy(Comment::getItem, Collectors.toList()));
        LocalDateTime now = LocalDateTime.now();
        List<Booking> lastBookings = bookingRepository.findAllByItemInAndStartLessThanEqualAndStatusIsOrderByStartDesc(
                items, now, BookingStatus.APPROVED);
        Map<Item, Booking> lastBookingsByItems = lastBookings.stream()
                .collect(Collectors.toMap(Booking::getItem, Function.identity(), (booking1, booking2) -> booking1));
        List<Booking> nextBookings = bookingRepository.findAllByItemInAndStartAfterAndStatusIsOrderByStartDesc(
                items, now, BookingStatus.APPROVED);
        Map<Item, Booking> nextBookingsByItems = nextBookings.stream()
                .collect(Collectors.toMap(Booking::getItem, Function.identity(), (booking1, booking2) -> booking1));

        return items.stream()
                .map(item -> itemMapper.toDtoResponse(item, commentsByItems.get(item),
                        lastBookingsByItems.get(item), nextBookingsByItems.get(item)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ItemDto add(ItemDto itemDto, long userId) {
        User owner = getUser(userId);
        Item item = itemMapper.fromDto(itemDto);
        item.setOwner(owner);
        item = itemRepository.save(item);
        log.debug("ItemServiceImpl: add item " + item + ".");
        return itemMapper.toDto(item);
    }

    @Override
    @Transactional
    public CommentDtoResponse addComment(CommentDtoRequest commentDtoRequest, long itemId, long userId) {
        Item item = getItem(itemId);
        User user = getUser(userId);
        bookingRepository.findFirstByItemAndBookerAndEndBefore(item, user, LocalDateTime.now())
                .orElseThrow(() -> new CustomValidationException(
                        "ItemServiceImpl: user id=" + userId + " doesn't have finished booking of item id=" + itemId));
        Comment comment = commentMapper.fromDto(commentDtoRequest);
        comment.setItem(item);
        comment.setAuthor(user);
        comment.setCreated(LocalDateTime.now());
        comment = commentRepository.save(comment);
        log.debug("ItemServiceImpl: add comment: " + comment + ".");
        return commentMapper.toDto(comment);
    }

    @Override
    @Transactional
    public ItemDto patch(long itemId, ItemPatchDto itemPatchDto, long userId) {
        Item item = getItem(itemId);
        if (!item.getOwner().getId().equals(userId)) {
            throw new AuthenticationErrorException(
                    "ItemServiceImpl: user id=" + userId + " is not owner of item id=" + itemId + ".");
        }
        itemMapper.updateWithPatchDto(item, itemPatchDto);
        log.debug("ItemServiceImpl: patch item " + item + ".");
        return itemMapper.toDto(item);
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemRepository.searchByNameOrDescription(text.toLowerCase()).stream()
                .map(itemMapper::toDto)
                .collect(Collectors.toList());
    }

    private Item getItem(long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("ItemServiceImpl: item with id=" + itemId + " not found."));
    }

    private User getUser(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("ItemServiceImpl: user with id=" + userId + " not found."));
    }

    private ItemDtoResponse formDtoResponse(Item item) {
        List<Comment> comments = commentRepository.findAllByItem(item);
        return itemMapper.toDtoResponse(item, comments);
    }

    private ItemDtoResponse formDtoResponseWithBookings(Item item) {
        LocalDateTime now = LocalDateTime.now();
        Booking lastBooking = bookingRepository
                .findFirstByItemAndStartLessThanEqualAndStatusIsOrderByStartDesc(
                        item, now, BookingStatus.APPROVED)
                .orElse(null);
        Booking nextBooking = bookingRepository
                .findFirstByItemAndStartAfterAndStatusIsOrderByStartDesc(
                        item, now, BookingStatus.APPROVED)
                .orElse(null);
        List<Comment> comments = commentRepository.findAllByItem(item);
        return itemMapper.toDtoResponse(item, comments, lastBooking, nextBooking);
    }
}