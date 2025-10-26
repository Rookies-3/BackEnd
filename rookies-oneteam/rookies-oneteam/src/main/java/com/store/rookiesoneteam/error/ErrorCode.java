package com.store.rookiesoneteam.error;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 BAD_REQUEST
    INVALID_PASSWORD("비밀번호는 8자 이상, 영문, 숫자, 특수문자를 포함해야 합니다.", HttpStatus.BAD_REQUEST),
    INVALID_INPUT("입력값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    INCORRECT_PASSWORD("현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 409 CONFLICT
    DUPLICATE_USERNAME("이미 사용중인 아이디입니다.", HttpStatus.CONFLICT),
    DUPLICATE_NICKNAME("이미 사용중인 닉네임입니다.", HttpStatus.CONFLICT),
    DUPLICATE_EMAIL("이미 사용중인 이메일입니다.", HttpStatus.CONFLICT),
    DUPLICATE_PHONE("이미 사용중인 전화번호입니다.", HttpStatus.CONFLICT),

    // 404 NOT_FOUND
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    // 500 INTERNAL_SERVER_ERROR
    INTERNAL_SERVER_ERROR("서버에 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String message;
    private final HttpStatus httpStatus;
}
