package com.example.profile_service.enums;

public enum MembershipStatus {
    ACTIVE,     // учится, состоит в профсоюзе
    GRADUATED,  // выпустился — необратимо, исключается из подсчёта непогашенного обязательства по баллам
    EXPELLED,   // отчислен — необратимо, аналогично GRADUATED
    BLOCKED     // заблокирован администратором — обратимо, баллы всё ещё считаются обязательством
}
