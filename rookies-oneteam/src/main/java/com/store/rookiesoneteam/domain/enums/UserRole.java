package com.store.rookiesoneteam.domain.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserRole {
    ADMIN("관리자"),
    USER("사용자유형");

    private final String description;
}
