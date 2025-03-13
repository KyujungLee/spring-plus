package org.example.expert.domain.log;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    // saveManager() 트랜잭션과 별개로 트랜잭션 할당
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(Long requestUserId, Long targetTodoId, Long targetUserId){
        logRepository.save(
                Log.builder()
                .requestUserId(requestUserId)
                .targetTodoId(targetTodoId)
                .targetUserId(targetUserId)
                .build()
        );
    }
}
