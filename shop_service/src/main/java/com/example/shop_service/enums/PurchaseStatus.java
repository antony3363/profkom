package com.example.shop_service.enums;

public enum PurchaseStatus {
    PENDING,    // создана, стоимость ещё не списана в Transactions Service
    CONFIRMED,  // баллы списаны, покупка подтверждена
    CANCELLED,  // отменена до подтверждения оплаты, сток возвращён
    REFUNDED    // подтверждённая покупка возвращена, сток и баллы возвращены
}
