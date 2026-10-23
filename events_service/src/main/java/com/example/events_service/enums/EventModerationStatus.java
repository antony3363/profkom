package com.example.events_service.enums;

public enum EventModerationStatus {
    DRAFT,      // черновик — видим только владельцу (ownerId), ждёт заполнения, не заявка
    SUBMITTED,  // заявка отправлена, ждёт решения Литвинова
    APPROVED,   // принято, баллы за посещение зафиксированы, мероприятие опубликовано
    DEFERRED,   // отложено, вернётся в очередь на рассмотрение
    REJECTED    // отклонено окончательно
}
