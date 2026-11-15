package com.patota.patota.controller;

import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail naoEncontrado(EntityNotFoundException erro) {
        return problema(HttpStatus.NOT_FOUND, erro.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail dadosInvalidos(MethodArgumentNotValidException erro) {
        String detalhes = erro.getBindingResult().getFieldErrors().stream()
                .map(campo -> campo.getField() + ": " + campo.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return problema(HttpStatus.BAD_REQUEST, detalhes);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumentoInvalido(IllegalArgumentException erro) {
        return problema(HttpStatus.BAD_REQUEST, erro.getMessage());
    }

    @ExceptionHandler({IllegalStateException.class, DataIntegrityViolationException.class})
    public ProblemDetail conflito(Exception erro) {
        String detalhe = erro instanceof DataIntegrityViolationException
                ? "O registro viola uma restrição de dados." : erro.getMessage();
        return problema(HttpStatus.CONFLICT, detalhe);
    }

    private ProblemDetail problema(HttpStatus status, String detalhe) {
        return ProblemDetail.forStatusAndDetail(status, detalhe);
    }
}
