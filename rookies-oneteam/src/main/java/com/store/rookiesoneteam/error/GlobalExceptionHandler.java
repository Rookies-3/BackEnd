package com.store.rookiesoneteam.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 우리가 직접 발생시키는 예외를 처리
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
        log.error("CustomException: {}", e.getErrorCode().getMessage());
        return ErrorResponse.toResponseEntity(e.getErrorCode());
    }

    // @Valid 어노테이션으로 인한 유효성 검사 실패 시 발생하는 예외를 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        // 어떤 필드에서 어떤 에러가 발생했는지 상세한 메시지를 생성합니다.
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> "[" + fieldError.getField() + "]: " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("ValidationException: {}", errorMessage);

        // INVALID_INPUT 에러 코드를 사용하여, 상세 메시지와 함께 응답을 생성합니다.
        return ErrorResponse.toResponseEntity(ErrorCode.INVALID_INPUT, errorMessage);
    }
}
