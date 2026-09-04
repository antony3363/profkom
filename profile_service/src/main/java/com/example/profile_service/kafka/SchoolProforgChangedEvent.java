package com.example.profile_service.kafka;

/**
 * Публикуется при смене School.proforgId. oldProforgId/newProforgId nullable —
 * школа может остаться без профорга или получить его впервые.
 */
public record SchoolProforgChangedEvent(long schoolId, Long oldProforgId, Long newProforgId) {
}
