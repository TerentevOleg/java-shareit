package ru.practicum.shareit.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.user.controller.UserController;
import ru.practicum.shareit.user.service.UserService;

import javax.validation.ConstraintViolationException;

import java.util.Set;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
class ErrorHandlerTest {
    @Autowired
    MockMvc mvc;

    @MockBean
    UserService userService;

    @Test
    void handleNotFoundException() throws Exception {
        when(userService.getAll())
                .thenThrow(new NotFoundException(""));

        mvc.perform(get("/users"))
                .andExpect(status().isNotFound());
    }

    @Test
    void handleCustomValidationException() throws Exception {
        when(userService.getAll())
                .thenThrow(new CustomValidationException(""));

        mvc.perform(get("/users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleDataIntegrityViolationException() throws Exception {
        when(userService.getAll())
                .thenThrow(new DataIntegrityViolationException(""));

        mvc.perform(get("/users"))
                .andExpect(status().isConflict());
    }

    @Test
    void handleAuthenticationErrorException() throws Exception {
        when(userService.getAll())
                .thenThrow(new AuthenticationErrorException(""));

        mvc.perform(get("/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void handleConstraintViolationException() throws Exception {
        when(userService.getAll())
                .thenThrow(new ConstraintViolationException(Set.of()));

        mvc.perform(get("/users"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handleUnexpectedException() throws Exception {
        when(userService.getAll())
                .thenThrow(new RuntimeException(""));

        mvc.perform(get("/users"))
                .andExpect(status().isInternalServerError());
    }
}