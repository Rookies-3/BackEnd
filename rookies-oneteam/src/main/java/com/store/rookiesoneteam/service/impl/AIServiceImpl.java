package com.store.rookiesoneteam.service.impl;

import com.store.rookiesoneteam.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AIServiceImpl implements AIService {

    @Override
    @Async // 이 메소드를 비동기적으로 실행하도록 설정합니다.
    public CompletableFuture<String> getAIResponse(String userMessage) {
        log.info("AI가 사용자 메시지에 대해 응답을 생성하고 있습니다: '{}'", userMessage);

        try {
            // 실제 AI가 응답을 생성하는 데 시간이 걸리는 것을 시뮬레이션합니다.
            Thread.sleep(2000); // 2초 지연
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("AI 응답 생성 중 스레드 오류 발생", e);
        }

        String aiResponse = "'" + userMessage + "'에 대한 저의 생각은... 아주 흥미로운 질문이네요. 더 깊이 논의해보고 싶습니다.";
        log.info("AI 응답 생성 완료: '{}'", aiResponse);

        // 생성된 응답을 CompletableFuture로 감싸서 즉시 반환합니다.
        return CompletableFuture.completedFuture(aiResponse);
    }
}
