package com.lookforbest.service.impl;

import com.lookforbest.dto.request.CompareRequest;
import com.lookforbest.entity.CompareSession;
import com.lookforbest.exception.ResourceNotFoundException;
import com.lookforbest.repository.CompareSessionRepository;
import com.lookforbest.service.CompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompareServiceImpl implements CompareService {

    private final CompareSessionRepository compareSessionRepository;

    @Override
    @Transactional
    public CompareSession createSession(CompareRequest request, Long userId) {
        CompareSession session = new CompareSession();
        session.setId(UUID.randomUUID().toString());
        session.setRobotIds(request.getRobotIds());
        session.setUserId(userId);
        return compareSessionRepository.save(session);
    }

    @Override
    @Transactional(readOnly = true)
    public CompareSession getSession(String sessionId) {
        return compareSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("CompareSession: " + sessionId));
    }
}
