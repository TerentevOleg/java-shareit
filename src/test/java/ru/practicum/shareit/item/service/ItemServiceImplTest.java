package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import ru.practicum.shareit.booking.dto.*;
import ru.practicum.shareit.booking.mapper.BookingShortMapper;
import ru.practicum.shareit.booking.mapper.BookingShortMapperImpl;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.model.BookingStatus;
import ru.practicum.shareit.booking.storage.BookingRepository;
import ru.practicum.shareit.exception.AuthenticationErrorException;
import ru.practicum.shareit.exception.CustomValidationException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.*;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.CommentMapperImpl;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.mapper.ItemMapperImpl;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.storage.CommentRepository;
import ru.practicum.shareit.item.storage.ItemRepository;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.storage.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.storage.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ItemServiceImplTest {

    @InjectMocks
    private ItemServiceImpl itemService;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private ItemRequestRepository itemRequestRepository;
    @Spy
    private static ItemMapper itemMapper = new ItemMapperImpl();
    @Spy
    private static CommentMapper commentMapper = new CommentMapperImpl();
    @Spy
    private static BookingShortMapper bookingShortMapper = new BookingShortMapperImpl();

    @NoArgsConstructor(staticName = "defaultBuilder")
    @AllArgsConstructor(staticName = "all")
    @Setter
    @Accessors(chain = true, fluent = true)
    private static class TestItemBuilder {
        private Long id = 1L;
        private String name = "name";
        private String description = "description";
        private Boolean available = true;
        private Long requestId = 10L;
        private Long ownerId = 20L;
        private Long lastBookingId = 30L;
        private Long nextBookingId = 40L;
        private List<TestCommentBuilder> testCommentBuilders = List.of(
                TestCommentBuilder.all(50L, "Comment1", LocalDateTime.now().minusDays(1),
                        60L, "author1", id),
                TestCommentBuilder.all(70L, "Comment2", LocalDateTime.now().minusDays(2),
                        80L, "author2", id)
        );

        public Item buildItem() {
            Item item = new Item();
            item.setId(id);
            item.setName(name);
            item.setDescription(description);
            item.setAvailable(available);
            if (Objects.nonNull(requestId)) {
                ItemRequest itemRequest = new ItemRequest();
                itemRequest.setId(requestId);
                item.setRequest(itemRequest);
            }
            User owner = new User();
            owner.setId(ownerId);
            item.setOwner(owner);
            return item;
        }

        public Booking buildLastBooking() {
            if (Objects.isNull(lastBookingId)) {
                return null;
            }
            Booking lastBooking = new Booking();
            lastBooking.setId(lastBookingId);
            Item item = new Item();
            item.setId(id);
            lastBooking.setItem(item);
            return lastBooking;
        }

        public Booking buildNextBooking() {
            if (Objects.isNull(nextBookingId)) {
                return null;
            }
            Booking nextBooking = new Booking();
            nextBooking.setId(nextBookingId);
            Item item = new Item();
            item.setId(id);
            nextBooking.setItem(item);
            return nextBooking;
        }

        public List<Comment> buildComments() {
            return testCommentBuilders.stream()
                    .map(TestCommentBuilder::buildComment)
                    .collect(Collectors.toList());
        }

        public ItemDto buildDto() {
            return new ItemDto(id, name, description, available, requestId);
        }

        public ItemDtoResponse buildDtoResponse() {
            return new ItemDtoResponse(id, name, description, available, requestId,
                    Objects.isNull(lastBookingId) ? null :
                            new BookingDtoShort(lastBookingId, null, null, null, null),
                    Objects.isNull(nextBookingId) ? null :
                            new BookingDtoShort(nextBookingId, null, null, null, null),
                    testCommentBuilders.stream().map(TestCommentBuilder::buildDtoResponse).collect(Collectors.toList()));
        }
    }

    @NoArgsConstructor(staticName = "defaultBuilder")
    @AllArgsConstructor(staticName = "all")
    @Setter
    @Accessors(chain = true, fluent = true)
    private static class TestCommentBuilder {
        private Long id = 100L;
        private String text = "comment1";
        private LocalDateTime created = LocalDateTime.of(1993, 12, 3, 10, 1);
        private Long authorId = 111L;
        private String authorName = "author1";
        private Long itemId = 121L;

        public Comment buildComment() {
            Comment comment = new Comment();
            comment.setId(id);
            comment.setText(text);
            comment.setCreated(created);
            User author = new User();
            author.setId(authorId);
            author.setName(authorName);
            comment.setAuthor(author);
            Item item = new Item();
            item.setId(itemId);
            comment.setItem(item);
            return comment;
        }

        public CommentDtoResponse buildDtoResponse() {
            return new CommentDtoResponse(id, text, authorName, created);
        }
    }

    @BeforeAll
    static void beforeAll() {
        ReflectionTestUtils.setField(itemMapper, "bookingShortMapper", bookingShortMapper);
        ReflectionTestUtils.setField(itemMapper, "commentMapper", commentMapper);
    }

    @Test
    void givenAddWithCorrectDto_whenAdd_thenReturnDto() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        ItemDto responseDto = itemBuilder.buildDto();
        Item responseItem = itemBuilder.buildItem();
        itemBuilder.id(null);
        ItemDto requestDto = itemBuilder.buildDto();
        Item requestItem = itemBuilder.buildItem();
        Long ownerId = requestItem.getOwner().getId();
        Long requestId = requestItem.getRequest().getId();

        Mockito.when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(requestItem.getOwner()));
        Mockito.when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.of(requestItem.getRequest()));
        Mockito.when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(responseItem);

        assertEquals(responseDto, itemService.add(requestDto, ownerId));

        Mockito.verify(itemRepository)
                .save(Mockito.argThat(item -> Objects.nonNull(item) &&
                        Objects.equals(item.getId(), requestItem.getId()) &&
                        Objects.equals(item.getName(), requestItem.getName()) &&
                        Objects.equals(item.getDescription(), requestItem.getDescription()) &&
                        Objects.equals(item.getAvailable(), requestItem.getAvailable()) &&
                        Objects.nonNull(item.getOwner()) &&
                        Objects.equals(item.getOwner().getId(), requestItem.getOwner().getId()) &&
                        Objects.nonNull(item.getRequest()) &&
                        Objects.equals(item.getRequest().getId(), requestItem.getRequest().getId())));
    }

    @Test
    void givenAddWithCorrectDtoNullRequest_whenAdd_thenReturnDtoWithNullRequestDto() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder().requestId(null);
        ItemDto responseDto = itemBuilder.buildDto();
        Item responseItem = itemBuilder.buildItem();
        itemBuilder.id(null);
        ItemDto requestDto = itemBuilder.buildDto();
        Item requestItem = itemBuilder.buildItem();
        Long ownerId = requestItem.getOwner().getId();

        Mockito.when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(requestItem.getOwner()));
        Mockito.when(itemRepository.save(Mockito.any(Item.class)))
                .thenReturn(responseItem);

        assertEquals(responseDto, itemService.add(requestDto, ownerId));

        Mockito.verify(itemRequestRepository, Mockito.never()).findById(Mockito.anyLong());

        Mockito.verify(itemRepository)
                .save(Mockito.argThat(item -> Objects.nonNull(item) &&
                        Objects.equals(item.getId(), requestItem.getId()) &&
                        Objects.equals(item.getName(), requestItem.getName()) &&
                        Objects.equals(item.getDescription(), requestItem.getDescription()) &&
                        Objects.equals(item.getAvailable(), requestItem.getAvailable()) &&
                        Objects.nonNull(item.getOwner()) &&
                        Objects.equals(item.getOwner().getId(), requestItem.getOwner().getId()) &&
                        Objects.isNull(item.getRequest())));
    }

    @Test
    void givenAddWithIncorrectOwner_whenAdd_thenThrowNotFoundException() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder().requestId(null);
        Item responseEntity = itemBuilder.buildItem();
        itemBuilder.id(null);
        ItemDto requestDto = itemBuilder.buildDto();
        Long ownerId = responseEntity.getOwner().getId();

        Mockito.when(userRepository.findById(ownerId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.add(requestDto, ownerId));
    }

    @Test
    void givenAddWithIncorrectRequest_whenAdd_thenThrowNotFoundException() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item responseItem = itemBuilder.buildItem();
        itemBuilder.id(null);
        ItemDto requestDto = itemBuilder.buildDto();
        Long ownerId = responseItem.getOwner().getId();
        Long requestId = responseItem.getRequest().getId();

        Mockito.when(userRepository.findById(ownerId))
                .thenReturn(Optional.of(responseItem.getOwner()));
        Mockito.when(itemRequestRepository.findById(requestId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.add(requestDto, ownerId));
    }

    @Test
    void givenPatchWithName_whenPatch_thenReturnPatchedDto() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item item = itemBuilder.buildItem();
        String patchName = item.getName() + " updated";
        itemBuilder.name(patchName);
        ItemDto responseDto = itemBuilder.buildDto();
        long itemId = item.getId();
        long ownerId = item.getOwner().getId();

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        assertEquals(responseDto,
                itemService.patch(itemId, new ItemPatchDto(patchName, null, null), ownerId));
    }

    @Test
    void givenPatchWithDescription_whenPatch_thenReturnPatchedDto() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item item = itemBuilder.buildItem();
        String patchDescription = item.getDescription() + " updated";
        itemBuilder.description(patchDescription);
        ItemDto responseDto = itemBuilder.buildDto();
        long itemId = item.getId();
        long ownerId = item.getOwner().getId();

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        assertEquals(responseDto,
                itemService.patch(itemId, new ItemPatchDto(null, patchDescription, null), ownerId));
    }

    @Test
    void givenPatchWithAvailable_whenPatch_thenReturnPatchedDto() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item item = itemBuilder.buildItem();
        Boolean patchAvailable = !item.getAvailable();
        itemBuilder.available(patchAvailable);
        ItemDto dto = itemBuilder.buildDto();
        long itemId = item.getId();
        long ownerId = item.getOwner().getId();

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        assertEquals(dto,
                itemService.patch(itemId, new ItemPatchDto(null, null, patchAvailable), ownerId));
    }

    @Test
    void givenPatchWithAllFieldsNull_whenPatch_thenReturnEqualDto() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item item = itemBuilder.buildItem();
        ItemDto dto = itemBuilder.buildDto();
        long itemId = item.getId();
        long ownerId = item.getOwner().getId();

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        assertEquals(dto,
                itemService.patch(itemId, new ItemPatchDto(null, null, null), ownerId));
    }

    @Test
    void givenPatchWithIncorrectItemId_whenPatch_thenThrowNotFoundException() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item item = itemBuilder.buildItem();
        long itemId = item.getId() + 1;
        long ownerId = item.getOwner().getId();

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.patch(itemId, new ItemPatchDto(null, null, null), ownerId));
    }

    @Test
    void givenPatchWithWrongOwner_whenPatch_thenThrowAuthenticationErrorException() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item item = itemBuilder.buildItem();
        long itemId = item.getId();
        long ownerId = item.getOwner().getId();

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(item));

        assertThrows(AuthenticationErrorException.class, () -> itemService.patch(itemId,
                new ItemPatchDto(null, null, null), ownerId + 1));
    }

    @Test
    void givenGetByIdCorrectWithOwnerUserId_whenGetById_thenReturnExtendedDtoWithBookings() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item itemBuild = itemBuilder.buildItem();
        ItemDtoResponse responseItemDto = itemBuilder.buildDtoResponse();
        Long itemId = itemBuild.getId();
        Long userId = itemBuild.getOwner().getId();

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(itemBuild.getOwner()));
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(itemBuild));
        Mockito.when(bookingRepository.findFirstByItemAndStartLessThanEqualAndStatusIsOrderByStartDesc(
                        Mockito.any(Item.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(BookingStatus.class)))
                .thenReturn(Optional.ofNullable(itemBuilder.buildLastBooking()));
        Mockito.when(bookingRepository.findFirstByItemAndStartAfterAndStatusIsOrderByStartDesc(
                        Mockito.any(Item.class), Mockito.any(LocalDateTime.class),
                        Mockito.any(BookingStatus.class)))
                .thenReturn(Optional.ofNullable(itemBuilder.buildNextBooking()));
        Mockito.when(commentRepository.findAllByItem(Mockito.any(Item.class)))
                .thenReturn(itemBuilder.buildComments());

        LocalDateTime minCurTime = LocalDateTime.now();
        assertEquals(responseItemDto, itemService.getById(itemId, userId));
        LocalDateTime maxCurTime = LocalDateTime.now();

        Mockito.verify(bookingRepository).findFirstByItemAndStartLessThanEqualAndStatusIsOrderByStartDesc(
                Mockito.argThat(item -> Objects.nonNull(item) &&
                        Objects.equals(item.getId(), item.getId())),
                Mockito.argThat(localDateTime -> !minCurTime.isAfter(localDateTime) &&
                        !maxCurTime.isBefore(localDateTime)),
                Mockito.eq(BookingStatus.APPROVED)
        );

        Mockito.verify(bookingRepository).findFirstByItemAndStartAfterAndStatusIsOrderByStartDesc(
                Mockito.argThat(item -> Objects.nonNull(item) &&
                        Objects.equals(item.getId(), item.getId())),
                Mockito.argThat(localDateTime -> !minCurTime.isAfter(localDateTime) &&
                        !maxCurTime.isBefore(localDateTime)),
                Mockito.eq(BookingStatus.APPROVED)
        );

        Mockito.verify(commentRepository).findAllByItem(
                Mockito.argThat(item -> Objects.nonNull(item) &&
                        Objects.equals(item.getId(), item.getId()))
        );
    }

    @Test
    void givenGetByIdCorrectWithUserOtherThanOwner_whenGetById_thenReturnExtendedDtoWithoutBookings() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item itemBuild = itemBuilder.buildItem();
        List<Comment> commentItem = itemBuilder.buildComments();
        itemBuilder.lastBookingId(null).nextBookingId(null);
        ItemDtoResponse responseItemDto = itemBuilder.buildDtoResponse();
        Long itemId = itemBuild.getId();
        long userId = itemBuild.getOwner().getId() + 1;
        User user = new User();
        user.setId(userId);

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(itemBuild));
        Mockito.when(commentRepository.findAllByItem(Mockito.any(Item.class)))
                .thenReturn(commentItem);

        assertEquals(responseItemDto, itemService.getById(itemId, userId));

        Mockito.verify(commentRepository).findAllByItem(
                Mockito.argThat(item -> Objects.nonNull(item) &&
                        Objects.equals(item.getId(), itemBuild.getId()))
        );
    }

    @Test
    void givenGetByIdWithIncorrectUserId_whenGetVyId_thenThrowNotFoundException() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item item = itemBuilder.buildItem();
        Long itemId = item.getId();
        long userId = item.getOwner().getId() + 1;

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(itemId, userId));
    }

    @Test
    void givenGetByIdWithIncorrectItemId_whenGetById_thenThrowNotFoundException() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item item = itemBuilder.buildItem();
        long itemId = item.getId() + 1;
        long userId = item.getOwner().getId();
        User user = new User();
        user.setId(userId);

        Mockito.when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getById(itemId, userId));
    }

    @Test
    void givenGetAllCorrectWithPagination_whenGetAll_thenReturnDtoList() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item item = itemBuilder.buildItem();
        long from = 0;
        int size = 1;

        Mockito.when(userRepository.findById(item.getOwner().getId()))
                .thenReturn(Optional.of(item.getOwner()));
        Mockito.when(itemRepository.findAllByOwnerOrderByIdAsc(Mockito.any(User.class), Mockito.any(Pageable.class)))
                .thenReturn(List.of(item));
        Mockito.when(commentRepository.findAllByItemIn(Mockito.anyList()))
                .thenReturn(itemBuilder.buildComments());
        Mockito.when(bookingRepository.findAllByItemInAndStartLessThanEqualAndStatusIsOrderByStartDesc(
                        Mockito.anyList(), Mockito.any(LocalDateTime.class),
                        Mockito.any(BookingStatus.class)))
                .thenReturn(List.of(itemBuilder.buildLastBooking()));
        Mockito.when(bookingRepository.findAllByItemInAndStartAfterAndStatusIsOrderByStartAsc(
                        Mockito.anyList(), Mockito.any(LocalDateTime.class),
                        Mockito.any(BookingStatus.class)))
                .thenReturn(List.of(itemBuilder.buildNextBooking()));

        assertEquals(List.of(itemBuilder.buildDtoResponse()),
                itemService.getAll(item.getOwner().getId(), from, size));
    }

    @Test
    void givenGetAllWithIncorrectUserId_whenGetAll_thenThrowNotFoundException() {
        long userId = 1L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> itemService.getAll(userId, 0, 1));
    }

    @Test
    void givenSearchWithPagination_whenSearch_thenReturnListOfDto() {
        TestItemBuilder itemBuilder = TestItemBuilder.defaultBuilder();
        Item item = itemBuilder.buildItem();
        String text = "TeXt";
        String lowerCaseText = "text";
        long from = 0;
        int size = 1;

        Mockito.when(itemRepository.searchByNameOrDescription(lowerCaseText, PageRequest.of((int) (from / size), size)))
                .thenReturn(List.of(item));

        assertEquals(List.of(itemBuilder.buildDto()),
                itemService.search(text, from, size));

        Mockito.verify(itemRepository, Mockito.never())
                .searchByNameOrDescription(Mockito.any(String.class), Mockito.eq(Pageable.unpaged()));
    }

    @Test
    void givenAddCommentCorrect_whenAddComment_thenReturnDto() {
        TestCommentBuilder builder = TestCommentBuilder.defaultBuilder();
        CommentDtoResponse responseCommentDto = builder.buildDtoResponse();
        Comment responseComment = builder.buildComment();
        builder.id(null);
        Comment requestComment = builder.buildComment();
        String text = requestComment.getText();
        Long itemId = requestComment.getItem().getId();
        Long authorId = requestComment.getAuthor().getId();

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(requestComment.getItem()));
        Mockito.when(userRepository.findById(authorId))
                .thenReturn(Optional.of(requestComment.getAuthor()));
        Mockito.when(bookingRepository.findFirstByItemAndBookerAndEndBefore(Mockito.any(Item.class),
                        Mockito.any(User.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.of(new Booking()));
        Mockito.when(commentRepository.save(Mockito.any(Comment.class)))
                .thenReturn(responseComment);

        LocalDateTime startCurTime = LocalDateTime.now();
        assertEquals(responseCommentDto,
                itemService.addComment(new CommentDtoRequest(text), itemId, authorId));
        LocalDateTime endCurTime = LocalDateTime.now();

        Mockito.verify(commentRepository).save(Mockito.argThat(comment -> Objects.nonNull(comment) &&
                Objects.equals(comment.getId(), requestComment.getId()) &&
                Objects.equals(comment.getText(), requestComment.getText()) &&
                !comment.getCreated().isBefore(startCurTime) &&
                !comment.getCreated().isAfter(endCurTime) &&
                Objects.nonNull(comment.getItem()) &&
                Objects.equals(comment.getItem().getId(), itemId) &&
                Objects.nonNull(comment.getAuthor()) &&
                Objects.equals(comment.getAuthor().getId(), authorId) &&
                Objects.equals(comment.getAuthor().getName(), requestComment.getAuthor().getName())));

        Mockito.verify(bookingRepository).findFirstByItemAndBookerAndEndBefore(
                Mockito.argThat(item -> Objects.nonNull(item) &&
                        Objects.equals(item.getId(), itemId)),
                Mockito.argThat(user -> Objects.nonNull(user) &&
                        Objects.equals(user.getId(), authorId) &&
                        Objects.equals(user.getName(), requestComment.getAuthor().getName())),
                Mockito.any(LocalDateTime.class)
        );
    }

    @Test
    void givenAddCommentWithIncorrectItemId_whenAddComment_thenThrowNotFoundException() {
        TestCommentBuilder builder = TestCommentBuilder.defaultBuilder();
        builder.id(null);
        Comment requestComment = builder.buildComment();
        long itemId = requestComment.getItem().getId() + 1;
        String text = requestComment.getText();
        Long authorId = requestComment.getAuthor().getId();

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(new CommentDtoRequest(text), itemId, authorId));
    }

    @Test
    void givenAddCommentWithIncorrectAuthorId_whenAddComment_thenThrowNotFoundException() {
        TestCommentBuilder builder = TestCommentBuilder.defaultBuilder();
        builder.id(null);
        Comment requestComment = builder.buildComment();
        String text = requestComment.getText();
        Long itemId = requestComment.getItem().getId();
        long authorId = requestComment.getAuthor().getId() + 1;

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(requestComment.getItem()));
        Mockito.when(userRepository.findById(authorId))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> itemService.addComment(new CommentDtoRequest(text), itemId, authorId));
    }

    @Test
    void givenAddCommentByUserWithNoFinishedBookings_whenAddComment_thenThrowCustomValidationException() {
        TestCommentBuilder builder = TestCommentBuilder.defaultBuilder();
        builder.id(null);
        Comment requestComment = builder.buildComment();
        String text = requestComment.getText();
        Long itemId = requestComment.getItem().getId();
        Long authorId = requestComment.getAuthor().getId();

        Mockito.when(itemRepository.findById(itemId))
                .thenReturn(Optional.of(requestComment.getItem()));
        Mockito.when(userRepository.findById(authorId))
                .thenReturn(Optional.of(requestComment.getAuthor()));
        Mockito.when(bookingRepository.findFirstByItemAndBookerAndEndBefore(Mockito.any(Item.class),
                        Mockito.any(User.class), Mockito.any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThrows(CustomValidationException.class,
                () -> itemService.addComment(new CommentDtoRequest(text), itemId, authorId));
    }
}