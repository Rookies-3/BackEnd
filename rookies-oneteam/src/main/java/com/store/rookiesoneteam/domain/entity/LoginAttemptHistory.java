// rookies-oneteam/src/main/java/com/store/rookiesoneteam/domain/entity/LoginAttemptHistory.java
package com.store.rookiesoneteam.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Table(name = "login_attempt_history")
@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class LoginAttemptHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private boolean success;

    @Column(nullable = false, length = 255)
    private String reason; // 실패 이유 (예: 비밀번호 불일치, 휴면 계정)

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime attemptedAt;
}