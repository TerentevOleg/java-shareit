package ru.practicum.shareit.exception;

public class AuthenticationErrorException extends RuntimeException {
    public AuthenticationErrorException(String message) {
        super(message);
    }
}
