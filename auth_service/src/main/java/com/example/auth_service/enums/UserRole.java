package com.example.auth_service.enums;

public enum UserRole {
    STUDENT,         // авторизован, но не член профсоюза
    UNION_MEMBER,    // студент ПО — копит/тратит баллы
    PROFORG_SCHOOL,  // профорг школы — переводы со счёта школы, заявки на мероприятия
    ADMIN            // Литвинов/сотрудник профкома — без ограничений
}
