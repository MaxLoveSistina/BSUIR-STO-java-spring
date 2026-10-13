# АвтоСервис (СТО)

Веб-приложение для станции технического обслуживания с двумя ролями — **мастер** и **клиент**.
Структура и стек повторяют проект CryptoExchange: Spring Boot 3 + PostgreSQL + JWT + Swagger, React (Vite), Docker, CI.

## Возможности

**Мастер**
- ведёт собственный каталог услуг: цена работы, базовый срок, дешёвые и дорогие материалы (название и цена);
- скрывает/показывает/удаляет услуги (удаление мягкое — старые заказы не меняются);
- видит заказы только на свои услуги, ведёт их по статусам `NEW → IN_PROGRESS → COMPLETED` (или `CANCELLED`);
- видит **своих клиентов**: сколько заказов и на какую сумму.

**Клиент**
- выбирает мастера и **несколько услуг** из его набора;
- для каждой услуги выбирает **материалы** (бюджетные / премиальные) и **срок** (стандартный / ускоренный / срочный);
- сразу видит **общую стоимость и срок**, оформляет заказ, следит за статусом, может отменить заказ в статусе `NEW`.

У разных мастеров — разные услуги и разные клиенты: заказ привязан к одному мастеру, услугу другого мастера добавить нельзя.

## Расчёт стоимости

```
работа  = цена_работы × коэффициент_срока
позиция = работа + цена_выбранных_материалов
дни     = max(1, ceil(базовые_дни × коэффициент_длительности))
ИТОГО   = сумма позиций;   срок заказа = сумма дней позиций
```

| Срок          | Код        | Цена работы | Длительность |
|---------------|------------|-------------|--------------|
| Стандартный   | `STANDARD` | ×1.00       | ×1.00        |
| Ускоренный    | `EXPRESS`  | ×1.30       | ×0.50        |
| Срочный       | `URGENT`   | ×1.60       | ×0.25        |

Коэффициенты хранятся в `DeadlineType`, фронтенд получает их из `GET /api/v1/meta/deadlines`.
Материалы на коэффициент срока не умножаются. Название и цены копируются в позицию заказа, поэтому изменение каталога не влияет на оформленные заказы.

Пример: колодки (работа 2 500 ₽, 2 дня), премиум-материалы 5 500 ₽, срок «Ускоренный» → 2 500 × 1.3 + 5 500 = **8 750 ₽**, 1 день.

## Запуск

### Docker (всё сразу)
```bash
cp .env.example .env      # задайте DB_PASS и JWT_SECRET
docker compose up --build
```
- Приложение: http://localhost:3000
- Swagger: http://localhost:8080/swagger-ui.html

### Локально
```bash
# PostgreSQL с базой autoservice (параметры — переменные DB_* в application.properties)
cd backend && mvn spring-boot:run
cd frontend && npm install && npm run dev    # http://localhost:5173, /api проксируется на :8080
```

### Демо-данные
При пустой БД (`DEMO_DATA=true`) создаются пользователи с паролем `password123`:
`master_ivan`, `master_alex` (у каждого свой набор услуг), `client_anna`, `client_oleg`.

## API (кратко)

| Метод и путь | Доступ | Назначение |
|---|---|---|
| `POST /api/v1/auth/register`, `/login` | все | регистрация (`ROLE_CLIENT`/`ROLE_MASTER`), вход |
| `GET /api/v1/masters`, `/{id}/services`, `/api/v1/meta/deadlines` | все | мастера, их услуги, сроки |
| `GET/POST /api/v1/master/services`, `PUT/DELETE /{id}` | мастер | каталог услуг |
| `GET /api/v1/master/orders[?status=]`, `PATCH /{id}/status` | мастер | заказы и смена статуса |
| `GET /api/v1/master/clients` | мастер | клиенты мастера |
| `POST /api/v1/client/orders/calculate` | клиент | расчёт стоимости без создания заказа |
| `POST/GET /api/v1/client/orders`, `PATCH /{id}/cancel` | клиент | оформление, список, отмена |

Пример расчёта:
```json
POST /api/v1/client/orders/calculate
{ "masterId": 1, "items": [
  { "serviceId": 1, "materialType": "ECONOMY", "deadlineType": "STANDARD" },
  { "serviceId": 2, "materialType": "PREMIUM", "deadlineType": "EXPRESS" } ] }
```

## Структура
```
backend/   Spring Boot: entity, repository, service, mapper, pricing, web (controller, dto, security)
frontend/  React + Vite (клиентский и мастерский кабинеты), nginx.conf
docker-compose.yml, .env.example, .github/workflows/build.yml
```

## Тесты
`cd backend && mvn verify` — юнит-тесты расчёта и сервиса заказов, сквозной интеграционный тест на H2 (`OrderFlowIntegrationTest`), отчёт JaCoCo.
