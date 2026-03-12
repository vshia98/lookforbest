package com.lookforbest.service;

import com.lookforbest.dto.request.CompareRequest;
import com.lookforbest.entity.CompareSession;

import java.util.List;

public interface CompareService {
    CompareSession createSession(CompareRequest request, Long userId);
    CompareSession getSession(String sessionId);
}
