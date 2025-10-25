package com.store.rookiesoneteam.domain.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageType {
    TALK("일반채팅을 위한 채팅 메시지"),
    EVALUATE("평가 및 피드백을 위한 채팅 메시지");

    private final String description;
}
