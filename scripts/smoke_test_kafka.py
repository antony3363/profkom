#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Проверка двух Kafka-интеграций auth_service <-> profile_service (напрямую по
портам, без Gateway — тестируется само событийное взаимодействие, а не
маршрутизация/JWT, это уже покрыто smoke_test_gateway.py).

Требует запущенный Kafka-брокер (localhost:9092, топики user-registered и
school-proforg-changed — см. scripts/... установку в памяти проекта) и живые
auth_service (8081) + profile_service (8085).

Сценарий:
  1. UserRegistered: login с email/firstName/lastName (personId ещё не существовал)
     -> auth_service публикует событие -> profile_service создаёт профиль автоматически.
  2. Обратная совместимость: login без email/имени -> профиль НЕ создаётся
     (старый ручной POST /profiles остаётся рабочим для этого случая).
  3. SchoolProforgChanged: школа создаётся, назначается профорг A (ни разу не
     логинившийся) -> auth_service создаёт для него учётку с ролью PROFORG_SCHOOL.
  4. Профорга меняют на B -> A разжалован обратно в STUDENT, B получил роль.

Используются случайные personId в диапазоне 900000-999999, чтобы скрипт можно
было перезапускать без конфликтов с данными других smoke-тестов.
"""
import json
import random
import sys
import time
import urllib.request
import urllib.error

AUTH = "http://localhost:8081"
PROFILE = "http://localhost:8085"

KAFKA_LAG_SECONDS = 2  # пауза после публикации события перед проверкой результата

FAILURES = []


def call(method, url, body=None, headers=None, expect=None):
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(url, data=data, method=method)
    req.add_header("Content-Type", "application/json")
    for k, v in (headers or {}).items():
        req.add_header(k, str(v))
    try:
        with urllib.request.urlopen(req, timeout=10) as resp:
            status = resp.status
            raw = resp.read()
            payload = json.loads(raw) if raw else None
    except urllib.error.HTTPError as e:
        status = e.code
        raw = e.read()
        try:
            payload = json.loads(raw) if raw else None
        except json.JSONDecodeError:
            payload = raw.decode("utf-8", errors="replace")

    ok = (status == expect) if expect is not None else (200 <= status < 300)
    mark = "OK " if ok else "FAIL"
    print(f"[{mark}] {method} {url} -> {status}")
    if not ok:
        print(f"       response: {payload}")
        FAILURES.append(f"{method} {url} -> {status} (expected {expect})")
    return status, payload


def assert_eq(label, actual, expected):
    ok = actual == expected
    mark = "OK " if ok else "FAIL"
    print(f"[{mark}] {label}: {actual} (ожидалось {expected})")
    if not ok:
        FAILURES.append(f"{label}: got {actual}, expected {expected}")


def login(person_id, email=None, first_name=None, last_name=None):
    body = {"personId": person_id}
    if email:
        body.update({"email": email, "firstName": first_name, "lastName": last_name})
    _, tokens = call("POST", f"{AUTH}/api/v1/auth/login", body, expect=200)
    return tokens["accessToken"]


def verify(token):
    _, v = call("GET", f"{AUTH}/api/v1/auth/verify", headers={"Authorization": f"Bearer {token}"}, expect=200)
    return v


def main():
    base = random.randint(900000, 999999)
    new_student, no_email_student, proforg_a, proforg_b = base, base + 1, base + 2, base + 3

    print("== 1. UserRegistered: login с email -> авто-создание профиля ==")
    login(new_student, "kafka.smoke@profkom.test", "Кафка", "Тестов")
    time.sleep(KAFKA_LAG_SECONDS)
    _, profile = call("GET", f"{PROFILE}/api/v1/profiles/{new_student}", expect=200)
    assert_eq("firstName авто-профиля", profile.get("firstName"), "Кафка")
    assert_eq("email авто-профиля", profile.get("email"), "kafka.smoke@profkom.test")

    print("\n== 2. Обратная совместимость: login без email -> профиль не создаётся ==")
    login(no_email_student)
    time.sleep(KAFKA_LAG_SECONDS)
    status, _ = call("GET", f"{PROFILE}/api/v1/profiles/{no_email_student}", expect=404)

    print("\n== 3. SchoolProforgChanged: назначение нового профорга (ни разу не логинившегося) ==")
    admin_hdr = {"X-User-Role": "ADMIN"}
    _, school = call("POST", f"{PROFILE}/api/v1/schools", {"title": f"Kafka smoke school {base}"}, admin_hdr, expect=201)
    school_id = school["schoolId"]

    call("PUT", f"{PROFILE}/api/v1/schools/{school_id}/proforg", {"proforgId": proforg_a}, admin_hdr, expect=200)
    time.sleep(KAFKA_LAG_SECONDS)
    verified_a = verify(login(proforg_a))
    assert_eq("роль proforg_a после назначения", verified_a.get("role"), "PROFORG_SCHOOL")
    assert_eq("schoolId proforg_a после назначения", verified_a.get("schoolId"), school_id)

    print("\n== 4. Смена профорга: A разжалован, B назначен ==")
    call("PUT", f"{PROFILE}/api/v1/schools/{school_id}/proforg", {"proforgId": proforg_b}, admin_hdr, expect=200)
    time.sleep(KAFKA_LAG_SECONDS)
    verified_a_after = verify(login(proforg_a))
    assert_eq("роль proforg_a после смены", verified_a_after.get("role"), "STUDENT")
    assert_eq("schoolId proforg_a после смены", verified_a_after.get("schoolId"), None)

    verified_b = verify(login(proforg_b))
    assert_eq("роль proforg_b после назначения", verified_b.get("role"), "PROFORG_SCHOOL")
    assert_eq("schoolId proforg_b после назначения", verified_b.get("schoolId"), school_id)

    print("\n" + "=" * 60)
    if FAILURES:
        print(f"ПРОВАЛЕНО ШАГОВ: {len(FAILURES)}")
        for f in FAILURES:
            print(f"  - {f}")
        sys.exit(1)
    else:
        print("ВСЕ ШАГИ ПРОШЛИ УСПЕШНО (Kafka: UserRegistered + SchoolProforgChanged)")


if __name__ == "__main__":
    main()
