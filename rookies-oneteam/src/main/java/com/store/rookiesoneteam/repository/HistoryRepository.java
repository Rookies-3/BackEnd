// rookies-oneteam/src/main/java/com/store/rookiesoneteam/repository/HistoryRepository.java
package com.store.rookiesoneteam.repository;

import com.store.rookiesoneteam.domain.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<History, Long> {
    // 특정 사용자의 퀴즈 기록 조회 등 필요한 쿼리 메서드를 여기에 정의할 수 있습니다.
    // List<History> findAllByUser_Id(Long userId);
}