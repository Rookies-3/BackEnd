package com.store.rookiesoneteam.domain.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum UserRole {
    ADMIN("관리자"),
    USER1("사용자유형1"),
    USER2("사용자유형2");

    private final String description;
}
