#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Сквозной прогон всей системы ЧЕРЕЗ API Gateway (порт 8090), с реальными JWT
вместо ручной подстановки X-Person-Id/X-User-Role/X-School-Id — так, как это
происходит в реальности: Gateway сам вызывает /api/v1/auth/verify и подставляет
заголовки, downstream-сервисы просто доверяют тому, что пришло от Gateway.

Единственное место, где мы обходим API — самый первый бутстрап administratora
(personId=301): в системе ещё нет ни одного ADMIN, а без ADMIN некому выдать роль
через PUT /api/v1/users/{userId}/role. Поэтому первую роль ADMIN проставляем
напрямую в БД (psql) — это ровно то, что в проде сделал бы оператор при первом
разворачивании системы (seed/миграция), а не дыра в авторизации.

Сценарий:
  Auth: bootstrap ADMIN (person=301) -> login -> verify через Gateway
  Profile: школа/направление/группа/3 профиля (301=Литвинов, 302=профорг, 303=студент)
  Auth: назначение роли PROFORG_SCHOOL person=302 через PUT /users/{id}/role (Gateway, Authorization: Bearer)
  Events: профорг подаёт заявку (Bearer proforg-токен) -> Литвинов принимает (Bearer admin-токен)
  CheckIn: студент сканирует QR (Bearer student-токен) -> баллы должны начислиться (gRPC в Transactions)
  Transactions: баланс студента = 50
  Shop: товар за 30 баллов -> покупка студентом (Bearer student-токен) -> баллы списываются (gRPC)
  Transactions: баланс студента = 20
"""
import json
import subprocess
import sys
import urllib.request
import urllib.error
from datetime import datetime, timedelta

GATEWAY = "http://localhost:8090"

PSQL = r"C:\Program Files\PostgreSQL\17\bin\psql.exe"
PG_ENV = {"PGPASSWORD": "123"}

ADMIN_PERSON_ID = 301
PROFORG_PERSON_ID = 302
STUDENT_PERSON_ID = 303

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


def bearer(token):
    return {"Authorization": f"Bearer {token}"}


def now_plus(hours):
    return (datetime.now() + timedelta(hours=hours)).replace(microsecond=0).isoformat()


def psql(sql):
    result = subprocess.run(
        [PSQL, "-U", "postgres", "-d", "auth_db", "-t", "-A", "-c", sql],
        capture_output=True, text=True, env={**__import__("os").environ, **PG_ENV},
    )
    if result.returncode != 0:
        raise RuntimeError(f"psql failed: {result.stderr}")
    return result.stdout.strip()


def login(person_id):
    _, tokens = call("POST", f"{GATEWAY}/api/v1/auth/login", {"personId": person_id}, expect=200)
    return tokens["accessToken"]


def main():
    print("== Auth: bootstrap первого ADMIN (person=301) напрямую через БД ==")
    login(ADMIN_PERSON_ID)  # создаёт строку app_users со стандартной ролью STUDENT
    psql(f"UPDATE app_users SET role='ADMIN' WHERE person_id={ADMIN_PERSON_ID};")
    admin_token = login(ADMIN_PERSON_ID)  # перелогин — новый JWT уже с ролью ADMIN
    _, verified = call("GET", f"{GATEWAY}/api/v1/auth/verify", headers=bearer(admin_token), expect=200)
    assert_eq("роль администратора после бутстрапа", verified.get("role"), "ADMIN")

    print("\n== Profile: школа/направление/группа (через Gateway, Bearer admin) ==")
    _, school = call("POST", f"{GATEWAY}/api/v1/schools",
                      {"title": "Школа информатики (Gateway smoke)"}, bearer(admin_token), expect=201)
    school_id = school["schoolId"]

    _, program = call("POST", f"{GATEWAY}/api/v1/programs",
                       {"title": "Программная инженерия", "schoolId": school_id}, bearer(admin_token), expect=201)
    program_id = program["programId"]

    _, group = call("POST", f"{GATEWAY}/api/v1/groups",
                     {"title": "ИТ-101", "programId": program_id}, bearer(admin_token), expect=201)
    group_id = group["groupId"]

    print("\n== Profile: профили (301=Литвинов, 302=профорг школы, 303=студент) ==")
    call("POST", f"{GATEWAY}/api/v1/profiles",
         {"personId": ADMIN_PERSON_ID, "groupId": group_id, "firstName": "Александр", "lastName": "Литвинов",
          "email": "litvinov.gw@profkom.test"}, expect=201)
    call("POST", f"{GATEWAY}/api/v1/profiles",
         {"personId": PROFORG_PERSON_ID, "groupId": group_id, "firstName": "Профорг", "lastName": "Школьный",
          "email": "proforg.gw@profkom.test"}, expect=201)
    call("POST", f"{GATEWAY}/api/v1/profiles",
         {"personId": STUDENT_PERSON_ID, "groupId": group_id, "firstName": "Студент", "lastName": "Тестовый",
          "email": "student.gw@profkom.test"}, expect=201)

    call("PUT", f"{GATEWAY}/api/v1/schools/{school_id}/proforg",
         {"proforgId": PROFORG_PERSON_ID}, bearer(admin_token), expect=200)

    print("\n== Auth: назначение роли PROFORG_SCHOOL person=302 (через Gateway, Bearer admin) ==")
    login(PROFORG_PERSON_ID)  # создаёт строку app_users со стандартной ролью STUDENT
    proforg_user_id = psql(f"SELECT user_id FROM app_users WHERE person_id={PROFORG_PERSON_ID};")
    call("PUT", f"{GATEWAY}/api/v1/users/{proforg_user_id}/role",
         {"role": "PROFORG_SCHOOL", "schoolId": school_id}, bearer(admin_token), expect=200)
    proforg_token = login(PROFORG_PERSON_ID)  # перелогин — JWT уже с ролью PROFORG_SCHOOL и schoolId
    _, verified_proforg = call("GET", f"{GATEWAY}/api/v1/auth/verify", headers=bearer(proforg_token), expect=200)
    assert_eq("роль профорга школы", verified_proforg.get("role"), "PROFORG_SCHOOL")
    assert_eq("schoolId профорга в токене", verified_proforg.get("schoolId"), school_id)

    student_token = login(STUDENT_PERSON_ID)

    print("\n== Events: заявка профорга (Bearer proforg) -> одобрение Литвиновым (Bearer admin) ==")
    event_body = {
        "title": "Посвящение в студенты (Gateway)",
        "description": "Первое мероприятие семестра",
        "shortDescription": "Посвящение",
        "registrationStartAt": now_plus(-1),
        "registrationEndAt": now_plus(1),
        "startAt": now_plus(2),
        "endAt": now_plus(4),
        "ownerId": PROFORG_PERSON_ID,
        "schoolId": school_id,
        "requestedPointsPerAttendee": 50,
        "registrationRequired": False,
    }
    _, event = call("POST", f"{GATEWAY}/api/v1/events", event_body, bearer(proforg_token), expect=201)
    event_id = event["eventId"]

    _, approved = call("POST", f"{GATEWAY}/api/v1/events/{event_id}/accept",
                        {"pointsPerAttendee": 50}, bearer(admin_token), expect=200)
    assert_eq("moderationStatus после accept", approved.get("moderationStatus"), "APPROVED")
    assert_eq("status после accept", approved.get("status"), "PUBLISHED")

    print("\n== CheckIn: студент сканирует QR мероприятия (Bearer student) ==")
    _, qr = call("GET", f"{GATEWAY}/api/v1/qr/events/{event_id}", expect=200)
    call("POST", f"{GATEWAY}/api/v1/check-ins",
         {"type": "SELF_SCAN", "qrPayload": qr["payload"]}, bearer(student_token), expect=201)

    print("\n== Transactions: баланс студента после чек-ина (ждём начисления через gRPC) ==")
    _, wallet = call("GET", f"{GATEWAY}/api/v1/wallets/me", headers=bearer(student_token), expect=200)
    assert_eq("баланс студента после чек-ина", wallet.get("balance"), 50)

    print("\n== Shop: товар за 30 баллов -> покупка студентом (Bearer student) ==")
    product_body = {
        "title": "Худи ПрофКом (Gateway)",
        "description": "Тестовый товар",
        "price": 30,
        "variants": [{"stock": 10}],
    }
    _, product = call("POST", f"{GATEWAY}/api/v1/products", product_body, expect=201)
    variant_id = product["variants"][0]["variantId"]

    _, purchase = call("POST", f"{GATEWAY}/api/v1/purchases",
                        {"variantId": variant_id, "count": 1}, bearer(student_token), expect=201)
    assert_eq("статус покупки", purchase.get("status"), "CONFIRMED")

    print("\n== Transactions: баланс студента после покупки ==")
    _, wallet2 = call("GET", f"{GATEWAY}/api/v1/wallets/me", headers=bearer(student_token), expect=200)
    assert_eq("баланс студента после покупки", wallet2.get("balance"), 20)

    print("\n== Негативные проверки на самом Gateway ==")
    status, _ = call("GET", f"{GATEWAY}/api/v1/wallets/me", expect=403)  # без токена нет identity
    call("GET", f"{GATEWAY}/api/v1/wallets/me", headers={"Authorization": "Bearer garbage.token.here"}, expect=401)

    print("\n" + "=" * 60)
    if FAILURES:
        print(f"ПРОВАЛЕНО ШАГОВ: {len(FAILURES)}")
        for f in FAILURES:
            print(f"  - {f}")
        sys.exit(1)
    else:
        print("ВСЕ ШАГИ ПРОШЛИ УСПЕШНО (через Gateway, с реальными JWT)")


if __name__ == "__main__":
    main()
