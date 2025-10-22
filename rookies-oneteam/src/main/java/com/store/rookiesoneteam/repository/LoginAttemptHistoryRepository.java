// rookies-oneteam/src/main/java/com/store/rookiesoneteam/repository/LoginAttemptHistoryRepository.java
package com.store.rookiesoneteam.repository;

import com.store.rookiesoneteam.domain.entity.LoginAttemptHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginAttemptHistoryRepository extends JpaRepository<LoginAttemptHistory, Long> {
    // 특정 사용자의 최근 N개의 실패 기록을 가져오는 쿼리 (LoginServiceImpl에서 주석 처리된 로직을 위해 필요)
    List<LoginAttemptHistory> findTop5ByUsernameOrderByAttemptedAtDesc(String username);
}