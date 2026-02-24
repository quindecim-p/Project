package com.example.domain.model.exceptions;

public class ServiceUnavailableException extends BusinessException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
