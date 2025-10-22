// rookies-oneteam/src/main/java/com/store/rookiesoneteam/repository/QuizRepository.java
package com.store.rookiesoneteam.repository;

import com.store.rookiesoneteam.domain.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    // 필요한 추가 쿼리 메서드를 여기에 정의할 수 있습니다.
}