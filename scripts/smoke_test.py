#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Сквозной прогон всей системы через реальные HTTP-вызовы (без Gateway — заголовки
X-Lichnost-Id/X-User-Role/X-School-Id выставляются здесь вручную, как это в проде
делал бы Gateway после проверки JWT).

См. также smoke_test_gateway.py — тот же сценарий, но через реальный Gateway
(порт 8090) с настоящими JWT вместо ручной подстановки заголовков.

Сценарий:
  Profile: школа -> направление -> группа -> 3 профиля (Литвинов=1 admin, профорг=2, студент=3)
  Profile: назначить профорга школе
  Events: профорг подаёт заявку на мероприятие -> Литвинов принимает (баллы=50)
  CheckIn: студент сканирует QR мероприятия -> баллы должны начислиться (gRPC в Transactions)
  Transactions: проверяем баланс студента = 50
  Shop: создаём товар за 30 баллов -> студент покупает -> баллы должны списаться (gRPC)
  Transactions: проверяем баланс студента = 20
"""
import json
import sys
import urllib.request
import urllib.error
from datetime import datetime, timedelta

PROFILE = "http://localhost:8085"
TRANSACTIONS = "http://localhost:8084"
AUTH = "http://localhost:8081"
EVENTS = "http://localhost:8080"
CHECKIN = "http://localhost:8082"
SHOP = "http://localhost:8083"

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


def now_plus(hours):
    return (datetime.now() + timedelta(hours=hours)).replace(microsecond=0).isoformat()


def main():
    print("== Auth: login/verify/refresh/logout ==")
    _, tokens = call("POST", f"{AUTH}/api/v1/auth/login", {"lichnostId": 1}, expect=200)
    access = tokens["accessToken"]
    refresh = tokens["refreshToken"]
    call("GET", f"{AUTH}/api/v1/auth/verify", headers={"Authorization": f"Bearer {access}"}, expect=200)
    _, refreshed = call("POST", f"{AUTH}/api/v1/auth/refresh", {"refreshToken": refresh}, expect=200)
    call("POST", f"{AUTH}/api/v1/auth/logout", {"refreshToken": refreshed["refreshToken"]}, expect=204)

    admin_hdr = {"X-User-Role": "ADMIN"}

    print("\n== Profile: школа/направление/группа ==")
    _, school = call("POST", f"{PROFILE}/api/v1/schools", {"title": "Школа информатики"}, admin_hdr, expect=201)
    school_id = school["schoolId"]

    _, program = call("POST", f"{PROFILE}/api/v1/programs",
                       {"title": "Программная инженерия", "schoolId": school_id}, admin_hdr, expect=201)
    program_id = program["programId"]

    _, group = call("POST", f"{PROFILE}/api/v1/groups",
                     {"title": "ИТ-101", "programId": program_id}, admin_hdr, expect=201)
    group_id = group["groupId"]

    print("\n== Profile: профили (1=admin/Литвинов, 2=профорг школы, 3=студент) ==")
    call("POST", f"{PROFILE}/api/v1/profiles",
         {"lichnostId": 1, "groupId": group_id, "firstName": "Александр", "lastName": "Литвинов",
          "email": "litvinov@profkom.test"}, expect=201)
    call("POST", f"{PROFILE}/api/v1/profiles",
         {"lichnostId": 2, "groupId": group_id, "firstName": "Профорг", "lastName": "Школьный",
          "email": "proforg@profkom.test"}, expect=201)
    call("POST", f"{PROFILE}/api/v1/profiles",
         {"lichnostId": 3, "groupId": group_id, "firstName": "Студент", "lastName": "Тестовый",
          "email": "student@profkom.test"}, expect=201)

    call("PUT", f"{PROFILE}/api/v1/schools/{school_id}/proforg", {"proforgId": 2}, admin_hdr, expect=200)

    print("\n== Events: заявка профорга -> одобрение Литвиновым ==")
    proforg_hdr = {"X-User-Role": "PROFORG_SCHOOL", "X-School-Id": school_id}
    event_body = {
        "title": "Посвящение в студенты",
        "description": "Первое мероприятие семестра",
        "shortDescription": "Посвящение",
        "registrationStartAt": now_plus(-1),
        "registrationEndAt": now_plus(1),
        "startAt": now_plus(2),
        "endAt": now_plus(4),
        "ownerId": 2,
        "schoolId": school_id,
        "requestedPointsPerAttendee": 50,
        "registrationRequired": False,
    }
    _, event = call("POST", f"{EVENTS}/api/v1/events", event_body, proforg_hdr, expect=201)
    event_id = event["eventId"]

    admin_lichnost_hdr = {"X-User-Role": "ADMIN", "X-Lichnost-Id": 1}
    _, approved = call("POST", f"{EVENTS}/api/v1/events/{event_id}/accept",
                        {"pointsPerAttendee": 50}, admin_lichnost_hdr, expect=200)
    assert_eq("moderationStatus после accept", approved.get("moderationStatus"), "APPROVED")
    assert_eq("status после accept", approved.get("status"), "PUBLISHED")

    print("\n== CheckIn: студент сканирует QR мероприятия ==")
    _, qr = call("GET", f"{CHECKIN}/api/v1/qr/events/{event_id}", expect=200)
    _, checkin = call("POST", f"{CHECKIN}/api/v1/check-ins",
                       {"type": "SELF_SCAN", "qrPayload": qr["payload"]},
                       {"X-Lichnost-Id": 3}, expect=201)

    print("\n== Transactions: баланс студента после чек-ина (ждём начисления через gRPC) ==")
    _, wallet = call("GET", f"{TRANSACTIONS}/api/v1/wallets/me",
                      headers={"X-Lichnost-Id": 3, "X-User-Role": "STUDENT"}, expect=200)
    assert_eq("баланс студента после чек-ина", wallet.get("balance"), 50)

    print("\n== Shop: товар за 30 баллов -> покупка студентом ==")
    product_body = {
        "title": "Худи ПрофКом",
        "slug": "hoodie-profkom",
        "description": "Тестовый товар",
        "price": 30,
        "variants": [{"stock": 10}],
    }
    _, product = call("POST", f"{SHOP}/api/v1/products", product_body, expect=201)
    variant_id = product["variants"][0]["variantId"]

    _, purchase = call("POST", f"{SHOP}/api/v1/purchases",
                        {"variantId": variant_id, "count": 1}, {"X-Lichnost-Id": 3}, expect=201)
    assert_eq("статус покупки", purchase.get("status"), "CONFIRMED")

    print("\n== Transactions: баланс студента после покупки ==")
    _, wallet2 = call("GET", f"{TRANSACTIONS}/api/v1/wallets/me",
                       headers={"X-Lichnost-Id": 3, "X-User-Role": "STUDENT"}, expect=200)
    assert_eq("баланс студента после покупки", wallet2.get("balance"), 20)

    print("\n" + "=" * 60)
    if FAILURES:
        print(f"ПРОВАЛЕНО ШАГОВ: {len(FAILURES)}")
        for f in FAILURES:
            print(f"  - {f}")
        sys.exit(1)
    else:
        print("ВСЕ ШАГИ ПРОШЛИ УСПЕШНО")


if __name__ == "__main__":
    main()
