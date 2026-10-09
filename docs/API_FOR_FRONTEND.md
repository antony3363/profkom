# API ПрофКом — справочник для фронтенд-разработчиков

Этот файл описывает реальные HTTP-эндпоинты бэкенда на текущий момент (сгенерировано по факту кода, не по ТЗ). Если по коду видно расхождение с ожидаемым поведением или отсутствует проверка прав — это явно отмечено пометкой **⚠**, а не скрыто.

## 1. Общие принципы

**Base URL** — всегда через API Gateway, никогда напрямую на порт сервиса:

```
http://<gateway-host>:8090
```

В локальной разработке — `http://localhost:8090`.

**Аутентификация**:
1. Получить пару токенов через `POST /api/v1/auth/login`.
2. На каждый последующий запрос класть заголовок:
   ```
   Authorization: Bearer <accessToken>
   ```
3. Когда `accessToken` истёк (см. `expiresInSeconds` в ответе логина) — обменять `refreshToken` через `POST /api/v1/auth/refresh` на новую пару.
4. При выходе — вызвать `POST /api/v1/auth/logout`, чтобы инвалидировать refresh-токен на сервере.

Клиенту **не нужно** и не следует самому формировать заголовки `X-Lichnost-Id` / `X-User-Role` / `X-School-Id` — это внутренний механизм. Gateway сам стирает эти заголовки, если их прислал клиент, проверяет `Authorization: Bearer`, и подставляет свои — сервисы за Gateway доверяют только тому, что подставил он. Если эндпоинт публичный (например `/login`), `Authorization` не нужен вовсе.

**Роли** (`UserRole` в auth_service):
- `STUDENT` — авторизован, не член профсоюза.
- `UNION_MEMBER` — студент ПО, копит/тратит баллы.
- `PROFORG_SCHOOL` — профорг школы, доступен рабочий кошелёк школы и создание мероприятий.
- `ADMIN` — сотрудник профкома.

⚠ **Это не иерархия в смысле кода.** Выше роли перечислены "по возрастанию прав" по смыслу бизнес-процесса, но на уровне реализации каждый эндпоинт сам сравнивает `X-User-Role` на точное совпадение — старшая роль НЕ подставляется автоматически туда, где ожидается младшая/другая роль. Конкретный пример: `POST /api/v1/transactions/school-award` требует ровно `PROFORG_SCHOOL` (`role != "PROFORG_SCHOOL"` → `403`) — `ADMIN` этим эндпоинтом воспользоваться не может, несмотря на то, что формально стоит "выше".

Отдельно от роли — `membershipStatus` профиля (`ACTIVE` / `GRADUATED` / `EXPELLED` / `BLOCKED`), это не про доступ к API, а про статус членства в профсоюзе.

**Формат ошибок** (одинаковый во всех сервисах):
```json
{ "timestamp": "...", "status": 404, "error": "Not Found", "message": "..." }
```
Для ошибок валидации тела запроса (400) дополнительно есть поле `errors`: `{ "имя_поля": "текст ошибки" }`.

**⚠ Важное общее ограничение:** у многих эндпоинтов ниже в коде вообще нет проверки роли на уровне контроллера (значит, любой авторизованный, а иногда и неавторизованный запрос пройдёт). Это отмечено у каждого такого эндпоинта. Фронтенду всё равно нужно ориентироваться на предполагаемую роль ниже (это то, как задумано), но нельзя полагаться на то, что бэкенд сам заблокирует случайное использование не той ролью — сейчас это не гарантировано.

---

## 2. Auth Service — `/api/v1/auth/**`, `/api/v1/users/**`

### Вход и токены

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/auth/login` | Оба (первый экран входа) | `{ "lichnostId": number, "email"?: string, "firstName"?: string, "lastName"?: string }`. ⚠ Заглушка реального SSO ТПУ: сейчас `lichnostId` передаётся напрямую, в целевой версии вместо этого будет передаваться authorization code от ТПУ. `email`/`firstName`/`lastName` нужны только при самом первом входе конкретного человека. | `200`: `{ "accessToken": string, "refreshToken": string, "expiresInSeconds": number }` |
| `POST /api/v1/auth/refresh` | Оба | `{ "refreshToken": string }` | `200`: та же структура, что у `/login` |
| `POST /api/v1/auth/logout` | Оба | `{ "refreshToken": string }` | `204 No Content` |
| `GET /api/v1/auth/verify` | **Не для фронтенда.** Вызывается только самим Gateway для интроспекции токена. | — | — |

### Пользователи/роли

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `GET /api/v1/users/{userId}` | Оба. ⚠ Нет проверки роли — любой авторизованный запрос пройдёт. | — | `200`: `{ "userId": number, "lichnostId": number, "role": "STUDENT"\|"UNION_MEMBER"\|"PROFORG_SCHOOL"\|"ADMIN", "schoolId": number\|null, "createdAt": datetime }` |
| `PUT /api/v1/users/{userId}/role` | Веб-админка (только `ADMIN`, проверяется). Временная замена авто-назначения роли профорга через Kafka. | `{ "role": "STUDENT"\|"UNION_MEMBER"\|"PROFORG_SCHOOL"\|"ADMIN", "schoolId"?: number }` (`schoolId` обязателен по смыслу, если `role=PROFORG_SCHOOL`) | `200`: тот же формат, что у `GET /users/{id}` |

---

## 3. Profile Service — `/api/v1/schools/**`, `/api/v1/programs/**`, `/api/v1/groups/**`, `/api/v1/profiles/**`

### Школы / направления / группы (иерархический справочник)

Одинаковый паттерн для всех трёх уровней — создание и назначение профорга только `ADMIN`, чтение — открыто всем.

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/schools` | Веб-админка (`ADMIN`) | `{ "title": string }` | `201`: `{ "schoolId", "title", "proforgId": number\|null, "createdAt" }` |
| `GET /api/v1/schools/{schoolId}` | Оба | — | `200`: то же тело |
| `GET /api/v1/schools` | Оба (список всех школ) | — | `200`: массив школ |
| `PUT /api/v1/schools/{schoolId}/proforg` | Веб-админка (`ADMIN`) | `{ "proforgId": number }` | `200`: школа |
| `POST /api/v1/programs` | Веб-админка (`ADMIN`) | `{ "title": string, "schoolId": number }` | `201`: `{ "programId", "title", "schoolId", "proforgId", "createdAt" }` |
| `GET /api/v1/programs/{programId}` | Оба | — | `200` |
| `GET /api/v1/programs?schoolId=<id>` | Оба | query-параметр `schoolId` обязателен | `200`: список направлений школы |
| `PUT /api/v1/programs/{programId}/proforg` | Веб-админка (`ADMIN`) | `{ "proforgId": number }` | `200` |
| `POST /api/v1/groups` | Веб-админка (`ADMIN`) | `{ "title": string, "programId": number }` | `201`: `{ "groupId", "title", "programId", "proforgId", "createdAt" }` |
| `GET /api/v1/groups/{groupId}` | Оба | — | `200` |
| `GET /api/v1/groups?programId=<id>` | Оба | query-параметр `programId` обязателен | `200`: список групп направления |
| `PUT /api/v1/groups/{groupId}/proforg` | Веб-админка (`ADMIN`) | `{ "proforgId": number }` | `200` |

### Профили студентов

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/profiles` | Веб-админка (по смыслу — ручное создание профиля). ⚠ Нет проверки роли в коде вообще. В целевой архитектуре профиль создаётся автоматически по Kafka-событию при первом логине — этот путь остаётся как временная замена. | `{ "lichnostId": number, "groupId"?: number, "firstName": string, "lastName": string, "secondName"?: string, "email": string, "cardNumber"?: string, "image"?: string }` | `201`: `UserProfileResponseDTO` (см. ниже) |
| `GET /api/v1/profiles/me` | Мобильное приложение (свой профиль, требует Bearer-токен) | — | `200`: `UserProfileResponseDTO` |
| `GET /api/v1/profiles/{lichnostId}` | Оба. ⚠ Нет проверки роли — любой может посмотреть чужой профиль по id, включая email/номер карты. | — | `200`: `UserProfileResponseDTO` |
| `GET /api/v1/profiles?groupId=<id>` | Оба (список профилей группы) | — | `200`: массив `UserProfileResponseDTO` |
| `PUT /api/v1/profiles/me` | Мобильное приложение (студент правит свои данные) | `{ "groupId"?, "firstName"?, "lastName"?, "secondName"?, "email"?, "cardNumber"?, "image"? }`. Поле `membershipStatus`, даже если прислать, всегда обнуляется сервером — сменить его через этот путь нельзя. | `200`: `UserProfileResponseDTO` |
| `PUT /api/v1/profiles/{lichnostId}` | Веб-админка (только `ADMIN`) — единственный путь сменить `membershipStatus` | `{ ...то же самое, включая "membershipStatus"?: "ACTIVE"\|"GRADUATED"\|"EXPELLED"\|"BLOCKED" }` | `200`: `UserProfileResponseDTO` |

`UserProfileResponseDTO`:
```json
{
  "lichnostId": number,
  "groupId": number | null,
  "firstName": string,
  "lastName": string,
  "secondName": string | null,
  "email": string,
  "cardNumber": string | null,
  "image": string | null,
  "membershipStatus": "ACTIVE" | "GRADUATED" | "EXPELLED" | "BLOCKED",
  "updatedAt": datetime
}
```

---

## 4. Events Service — `/api/v1/events/**`, `/api/v1/volunteer_records/**`

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/events` | ⚠ Требует уточнения, каким клиентом реально пользуются профорги — доступно роли `PROFORG_SCHOOL` (только для своей школы) или `ADMIN`. Подача заявки на мероприятие. Сервер проверяет цепочку `registrationStartAt < registrationEndAt <= startAt < endAt` (400, если нарушена) и отклоняет точный дубликат по `title`+`startAt`+`endAt` (409). | `{ "title": string, "description": string, "shortDescription": string, "image"?: string, "registrationStartAt": datetime, "registrationEndAt": datetime, "startAt": datetime, "endAt": datetime, "availableGroupIds"?: number[], "ownerId": number, "schoolId"?: number, "requestedPointsPerAttendee"?: number, "registrationRequired": boolean }` | `201`: `EventResponseDTO` |
| `GET /api/v1/events/{eventId}` | Оба (полная карточка мероприятия) | — | `200`: `EventResponseDTO` |
| `GET /api/v1/events` | Оба (лента/каталог мероприятий) | — | `200`: массив `EventCatalogDTO`: `{ "eventId", "title", "shortDescription", "image", "startAt", "endAt", "registrationEndAt", "pointsPerAttendee": number|null }` |
| `GET /api/v1/events/short/{eventId}` | Оба (короткая карточка, например для списков/уведомлений) | — | `200`: `{ "eventId", "title", "shortDescription", "startAt", "endAt" }` |
| `PUT /api/v1/events/{eventId}` | ⚠ Нет проверки роли вообще — любой запрос пройдёт. По смыслу должно быть ограничено. Та же проверка дат, что и при создании, применяется к итоговому состоянию (с учётом уже сохранённых значений для полей, которые вы не передали). Пустой `title` теперь отклоняется (400). ⚠ Пустая строка `""` для `registrationStartAt`/`registrationEndAt`/`startAt`/`endAt` технически неотличима от отсутствующего поля (Jackson превращает `""` в `null` до любой валидации) — такое значение сейчас молча игнорируется (старое значение остаётся), а не отклоняется с ошибкой. | `{ любое подмножество полей создания + "status"?: "DRAFT"\|"PUBLISHED"\|"CANCELLED"\|"ARCHIVED" }` | `200`: `EventResponseDTO`. `400`, если итоговые даты нарушают порядок или `title` пустой. |
| `DELETE /api/v1/events/{eventId}` | ⚠ Нет проверки роли вообще. | — | `204` |
| `POST /api/v1/events/{eventId}/accept` | Веб-админка (только `ADMIN`) — принять заявку, зафиксировать баллы, опубликовать | `{ "pointsPerAttendee": number }` | `200`: `EventResponseDTO` (`moderationStatus=APPROVED`, `status=PUBLISHED`) |
| `POST /api/v1/events/{eventId}/defer` | Веб-админка (только `ADMIN`) — отложить, не финальное решение | — | `200`: `EventResponseDTO` (`moderationStatus=DEFERRED`) |
| `POST /api/v1/events/{eventId}/reject` | Веб-админка (только `ADMIN`) — отклонить финально | — | `200`: `EventResponseDTO` (`moderationStatus=REJECTED`, `status=CANCELLED`) |

`EventResponseDTO` (полная карточка):
```json
{
  "eventId": uuid, "title": string, "description": string, "shortDescription": string,
  "image": string | null,
  "registrationStartAt": datetime, "registrationEndAt": datetime, "startAt": datetime, "endAt": datetime,
  "availableGroupIds": number[] | null,
  "ownerId": number, "schoolId": number | null,
  "status": "DRAFT" | "PUBLISHED" | "CANCELLED" | "ARCHIVED",
  "moderationStatus": "SUBMITTED" | "APPROVED" | "DEFERRED" | "REJECTED",
  "requestedPointsPerAttendee": number | null, "pointsPerAttendee": number | null,
  "reviewedBy": number | null, "reviewedAt": datetime | null,
  "registrationRequired": boolean,
  "createdAt": datetime, "updatedAt": datetime
}
```
Каталог (`GET /api/v1/events`) отдаёт только опубликованные мероприятия с привязкой к школе — служебные (`schoolId=null`) туда не попадают.

### Волонтёры мероприятия

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/volunteer_records` | ⚠ Требует уточнения (нет проверки роли) — вероятно организатор/профорг записывает волонтёра на мероприятие. | `{ "lichnostId": number, "eventId": uuid }` | `201`: `{ "volunteerEntryId": uuid, "lichnostId": number, "eventId": uuid, "fullName": string, "groupTitle": string }` |
| `GET /api/v1/volunteer_records/{id}` | Оба | — | `200` |
| `GET /api/v1/volunteer_records?eventId=<id>` | Оба (без параметра — список всех волонтёров всех мероприятий) | — | `200`: массив |
| `DELETE /api/v1/volunteer_records/{id}` | ⚠ Нет проверки роли. | — | `204` |

---

## 5. Check-In Service — `/api/v1/check-ins/**`, `/api/v1/registrations/**`, `/api/v1/qr/**`

### QR-коды

| Метод и путь | Кто | Ответ |
|---|---|---|
| `GET /api/v1/qr/me` | Мобильное приложение — студент показывает свой личный QR на входе | `200`: `{ "payload": string }` |
| `GET /api/v1/qr/events/{eventId}` | ⚠ Требует уточнения (нет проверки роли) — QR самого мероприятия, обычно выводится на экран/плакат на входе, которое сканирует участник | `200`: `{ "payload": string }` |

`payload` — непрозрачная строка, её содержимое не нужно парсить на клиенте — просто передавать обратно в `POST /api/v1/check-ins`.

### Чек-ин (отметка присутствия)

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/check-ins` | Мобильное приложение — оба сценария происходят с телефона: (1) `SELF_SCAN` — студент сканирует QR мероприятия своим приложением (требует `X-Lichnost-Id`, то есть Bearer-токен); (2) `STAFF_SCAN` — волонтёр/организатор сканирует личный QR участника. ⚠ Для `STAFF_SCAN` личность и роль вызывающего вообще не проверяются — идентификатор берётся из отсканированного QR участника, а не из токена того, кто сканирует. На практике это значит, что чек-ин чужого человека может выполнить любой авторизованный пользователь (даже `STUDENT`), а не только волонтёр/профорг/админ — сейчас это ограничено только тем, что нужно физически получить чей-то QR-код. | `{ "type": "SELF_SCAN"\|"STAFF_SCAN", "qrPayload": string, "eventId"?: uuid }`. `qrPayload` — для `SELF_SCAN` это payload мероприятия (из `/qr/events/{id}`), для `STAFF_SCAN` — payload участника (из его `/qr/me`). `eventId` обязателен только для `STAFF_SCAN`. | `201`: `{ "checkInId": uuid, "registrationId": uuid\|null, "lichnostId": number, "eventId": uuid, "type": "SELF_SCAN"\|"STAFF_SCAN", "createdAt": datetime }` |
| `GET /api/v1/check-ins/{checkInId}` | Оба | — | `200` |
| `GET /api/v1/check-ins?registrationId=<id>` или `?eventId=<id>` | Веб-админка (отчёты по посещаемости; без параметров — все чек-ины) | — | `200`: массив |
| `DELETE /api/v1/check-ins/{checkInId}` | ⚠ Нет проверки роли. | — | `204` |

**Важно для фронтенда**: если у мероприятия уже установлен `pointsPerAttendee` (заявка одобрена), баллы начисляются автоматически на бэкенде сразу после успешного чек-ина — дополнительно вызывать что-либо в Transactions Service не нужно. Если начисление не удалось (сервис баллов недоступен), сам чек-ин всё равно считается успешным (баллы могут быть начислены вручную позже) — это стоит учитывать в UX (не показывать гарантированное начисление баллов сразу как факт).

### Регистрация на мероприятие (когда `registrationRequired=true`)

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/registrations` | Мобильное приложение — студент записывается на мероприятие заранее. ⚠ Нет проверки роли/что `lichnostId` совпадает с вызывающим. | `{ "lichnostId": number, "eventId": uuid }` | `201`: `{ "registrationId": uuid, "lichnostId": number, "eventId": uuid, "createdAt": datetime }` |
| `GET /api/v1/registrations/{id}` | Оба | — | `200` |
| `GET /api/v1/registrations?eventId=<id>` или `?lichnostId=<id>` | Оба (список участников мероприятия / мероприятий человека) | — | `200`: массив |
| `DELETE /api/v1/registrations/{id}` | Мобильное приложение (отмена своей регистрации). ⚠ Нет проверки, что отменяет владелец. | — | `204` |

---

## 6. Shop Service — `/api/v1/products/**`, `/api/v1/categories/**`, `/api/v1/product-media/**`, `/api/v1/purchases/**`

### Категории и товары

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/categories` | Веб-админка (по смыслу). ⚠ Нет проверки роли. | `{ "title": string, "parentId"?: uuid }` | `201`: `{ "categoryId", "parentId", "title", "createdAt" }` |
| `GET /api/v1/categories/{id}` | Оба | — | `200` |
| `GET /api/v1/categories?parentId=<id>` | Оба (без параметра — категории верхнего уровня) | — | `200`: массив |
| `DELETE /api/v1/categories/{id}` | Веб-админка (по смыслу). ⚠ Нет проверки роли. | — | `204` |
| `POST /api/v1/products` | Веб-админка (по смыслу). ⚠ Нет проверки роли. У товара обязателен минимум один вариант, даже если у него нет размера/цвета (тогда `size`/`color` = null). | `{ "title": string, "description"?: string, "price": number (баллы, ≥0), "categoryId"?: uuid, "variants": [{ "size"?: "XS".."XXL", "color"?: string, "stock": number (≥0), "priceOverride"?: number }] }` (минимум 1 вариант) | `201`: `ProductResponseDTO` |
| `GET /api/v1/products/{id}` | Оба (полная карточка товара) | — | `200`: `ProductResponseDTO` |
| `GET /api/v1/products?categoryId=<id>` | Оба (каталог) | — | `200`: массив `{ "productId", "title", "price": number, "coverMediaId": uuid|null, "totalStock": number }` |
| `PUT /api/v1/products/{id}` | Веб-админка (по смыслу). ⚠ Нет проверки роли. | `{ любое подмножество: "title"?, "description"?, "price"?, "categoryId"?, "coverMediaId"?, "status"?: "DRAFT"\|"PUBLISHED"\|"ARCHIVED" }` | `200`: `ProductResponseDTO` |
| `DELETE /api/v1/products/{id}` | Веб-админка (по смыслу). ⚠ Нет проверки роли. | — | `204` |

`ProductResponseDTO`:
```json
{
  "productId": uuid, "categoryId": uuid | null, "title": string, "description": string | null,
  "price": number, "status": "DRAFT" | "PUBLISHED" | "ARCHIVED", "coverMediaId": uuid | null,
  "variants": [ { "variantId": uuid, "productId": uuid, "size": "XS".."XXL" | null, "color": string | null,
                  "stock": number, "priceOverride": number | null, "createdAt": datetime } ],
  "createdAt": datetime, "updatedAt": datetime
}
```

### Медиа товара

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/product-media` | Веб-админка. ⚠ Нет проверки роли. | `{ "productId": uuid, "media": string, "isCover": boolean, "sortOrder": number }` | `201`: `{ "mediaId", "productId", "media", "isCover", "sortOrder", "createdAt" }` |
| `GET /api/v1/product-media?productId=<id>` | Оба | — | `200`: массив |
| `DELETE /api/v1/product-media/{mediaId}` | Веб-админка. ⚠ Нет проверки роли. | — | `204` |

### Покупки

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/purchases` | Мобильное приложение (требует Bearer-токен покупателя) | `{ "variantId": uuid, "count": number (>0) }` | `201`: `PurchaseResponseDTO` |
| `GET /api/v1/purchases/{id}` | Оба | — | `200`: `PurchaseResponseDTO` |
| `GET /api/v1/purchases` | Мобильное приложение (требует Bearer-токен, список **своих** покупок) | — | `200`: массив `PurchaseResponseDTO` |
| `POST /api/v1/purchases/{id}/confirm` | **Не для фронтенда.** Реальный поток списания баллов теперь идёт синхронно внутри `POST /purchases` через внутренний gRPC-вызов в Transactions Service — этот REST-путь остался от более раннего варианта и фронтендом вызываться не должен. | — | — |
| `POST /api/v1/purchases/{id}/cancel` | ⚠ Требует уточнения — нет проверки роли/владельца. | — | `200`: `PurchaseResponseDTO` |
| `POST /api/v1/purchases/{id}/refund` | Веб-админка (по смыслу — оформление возврата). ⚠ Нет проверки роли. | — | `200`: `PurchaseResponseDTO` |

**Важно для фронтенда**: `POST /api/v1/purchases` списывает баллы сразу же (синхронно). Если баллов не хватает — ответ **`409 Conflict`** (стандартный формат ошибки, см. раздел 1), **не** `201`. Покупка в этом случае вообще не создаётся (откатывается целиком вместе со списанием стока) — в теле ошибки нет `purchaseId`, повторно искать её через `GET /purchases/{id}` не нужно, это будет `404`. То же самое (`409`, ничего не создаётся) — если Transactions Service временно недоступен. Единственный успешный результат этого запроса — `201` со `status: "CONFIRMED"`; `status: "CANCELLED"` в ответе этого эндпоинта появиться не может, `CANCELLED`/`REFUNDED` видны только у покупок, полученных другими путями (`GET /purchases/{id}`, `GET /purchases`) для записей, отменённых после факта (например через `/refund`).

`PurchaseResponseDTO`:
```json
{
  "purchaseId": uuid, "buyerId": number, "variantId": uuid,
  "productTitle": string, "variantLabel": string,
  "unitPrice": number, "count": number, "amount": number,
  "status": "PENDING" | "CONFIRMED" | "CANCELLED" | "REFUNDED",
  "transactionId": uuid | null, "description": string | null, "createdAt": datetime
}
```

---

## 7. Transactions Service — `/api/v1/wallets/**`, `/api/v1/transactions/**`

### Кошельки

| Метод и путь | Кто | Ответ |
|---|---|---|
| `GET /api/v1/wallets/me` | Мобильное приложение — свой личный баланс (требует Bearer-токен) | `200`: `WalletResponseDTO` |
| `GET /api/v1/wallets/{walletId}` | Оба | `200`: `WalletResponseDTO` |
| `GET /api/v1/wallets/school/{schoolId}` | Мобильное приложение (профорг школы смотрит рабочий кошелёк) и/или Веб-админка | `200`: `WalletResponseDTO` |

`WalletResponseDTO`:
```json
{
  "walletId": uuid, "walletType": "PERSONAL" | "SCHOOL",
  "ownerUserId": number | null, "schoolId": number | null,
  "balance": number, "unlimited": boolean, "updatedAt": datetime
}
```
`unlimited=true` — только у кошельков ADMIN, проверка баланса при списании для них пропускается.

### Начисления и переводы

| Метод и путь | Кто | Тело запроса | Ответ |
|---|---|---|---|
| `POST /api/v1/transactions/event-reward` | **Вероятно не нужен фронтенду напрямую** (только `ADMIN`). ⚠ Уточнение: `POST /events/{id}/accept` сам по себе баллы никому не начисляет — он только фиксирует `pointsPerAttendee` на мероприятии. Реальное начисление конкретному человеку происходит позже и отдельно, автоматически при его чек-ине (см. раздел 5) — этот REST-путь дублирует ту же внутреннюю gRPC-логику для ручного/аварийного начисления в обход чек-ина. | `{ "receiverUserId": number, "amount": number (>0), "eventId": uuid, "description"?: string }` | `201`: `TransactionResponseDTO` |
| `POST /api/v1/transactions/transfer` | Веб-админка (только `ADMIN`) — ручной перевод баллов человеку или на рабочий кошелёк школы | `{ "receiverUserId"?: number, "receiverSchoolId"?: number, "amount": number (>0), "description"?: string }` (ровно одно из двух полей получателя) | `201`: `TransactionResponseDTO` |
| `POST /api/v1/transactions/school-award` | Мобильное приложение — профорг школы (`PROFORG_SCHOOL`) награждает студента из рабочего кошелька своей школы | `{ "receiverUserId": number, "amount": number (>0), "description"?: string }` | `201`: `TransactionResponseDTO` |
| `POST /api/v1/transactions/purchase-charge` | **Не для фронтенда.** Легаси-путь, реальное списание за покупку идёт через внутренний gRPC-вызов из Shop Service. | — | — |
| `POST /api/v1/transactions/refund` | **Не для фронтенда.** Аналогично, реальный возврат — через gRPC. | — | — |
| `GET /api/v1/transactions?walletId=<id>` | Оба — история операций по кошельку (и в приложении студента, и в админке) | — | `200`: массив `TransactionResponseDTO` |

`TransactionResponseDTO`:
```json
{
  "transactionId": uuid, "senderWalletId": uuid | null, "receiverWalletId": uuid | null,
  "type": "EVENT_REWARD" | "TRANSFER" | "PURCHASE" | "REFUND",
  "amount": number, "status": "PENDING" | "SUCCESS" | "FAILED",
  "relatedEventId": uuid | null, "relatedPurchaseId": uuid | null,
  "description": string | null, "createdAt": datetime
}
```

---

## 8. Сводная таблица: кто вызывает что

| Клиент | Эндпоинты |
|---|---|
| **Веб-админка** | `PUT /users/{id}/role`, все `POST/PUT` в `/schools`, `/programs`, `/groups`, `PUT /profiles/{id}` (смена статуса членства), `POST /events/{id}/accept\|defer\|reject`, `GET /check-ins` (отчёты), управление каталогом (`/products`, `/categories`, `/product-media`), `POST /purchases/{id}/refund`, `POST /transactions/transfer` |
| **Мобильное приложение** | `GET/PUT /profiles/me`, лента `GET /events`, `GET /qr/me`, `POST /check-ins` (оба типа сканирования), `POST /registrations`, `POST /purchases`, `GET /purchases` (свои), `GET /wallets/me`, `POST /transactions/school-award` (для роли профорга школы) |
| **Оба** | `POST /auth/login\|refresh\|logout`, все публичные `GET`-справочники (школы/направления/группы/мероприятия/товары/категории), `GET /profiles/{id}`, `GET /transactions?walletId=` |
| **Требует уточнения у бэкенда** (нет проверки роли в коде — непонятно, кто должен это вызывать) | `POST /events` (кто именно — мобильное приложение профорга или админка), `GET /qr/events/{id}`, `POST/DELETE /volunteer_records`, `PUT/DELETE /events/{id}`, `DELETE /check-ins/{id}`, `DELETE /registrations/{id}`, `POST /purchases/{id}/cancel`, весь `POST/PUT/DELETE` в `/categories`, `/products`, `/product-media` (сейчас открыт всем, предполагается что админка) |
| **Не для фронтенда** (внутренние/легаси пути) | `GET /auth/verify`, `POST /purchases/{id}/confirm`, `POST /transactions/purchase-charge`, `POST /transactions/refund`, `POST /transactions/event-reward` |
