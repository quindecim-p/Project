package com.example.domain.model.exceptions;

public class CriticalConsistencyException extends RuntimeException {
    public CriticalConsistencyException(String message) {
        super(message);
    }
}
