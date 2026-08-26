package com.example.transactions_service.enums;

public enum TransactionType {
    EVENT_REWARD,  // начисление за посещение мероприятия (одобрено Литвиновым при модерации)
    TRANSFER,      // ручной перевод (Литвинов -> человек/школа, школа -> студент)
    PURCHASE,      // списание за покупку в Shop Service
    REFUND         // возврат за отменённую/возвращённую покупку
}
