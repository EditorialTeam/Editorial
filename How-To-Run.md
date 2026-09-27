# 📰 Editorial System

Информационная система управления редакцией / издательством. Построена на **Java 21**, чистой архитектуре (**Clean Architecture + MVP**), **PostgreSQL** и **Apache POI**.

---

## 🛠 Системные требования

Перед началом работы убедитесь, что на компьютере установлены:

- **Java Development Kit (JDK) 21**
- **Apache Maven 3.9+**
- **Docker** и **Docker Compose**
- **Git**

---

## 🚀 Быстрый старт

### 1. Клонирование репозитория

```bash
git clone https://github.com/Vladusecho/Editorial.git
cd Editorial
```

### 2. Настройка переменных окружения

В корне проекта создайте локальный файл `.env` на основе шаблона `.env.example`.

Windows (PowerShell):

```powershell
Copy-Item .env.example .env
```

Linux / macOS:

```bash
cp .env.example .env
```

Файл `.env` содержит настройки PostgreSQL и JDBC-подключения приложения:

```dotenv
POSTGRES_DB=editorial
POSTGRES_USER=editorial_app
POSTGRES_PASSWORD=editorial
POSTGRES_PORT=5433

DB_URL=jdbc:postgresql://127.0.0.1:5433/editorial
DB_USER=editorial_app
DB_PASSWORD=editorial
```

Те же параметры должны быть в `src/main/resources/database.properties`:

```properties
db.url=jdbc:postgresql://localhost:5433/editorial
db.user=editorial_app
db.password=editorial
db.driver=org.postgresql.Driver
```

`POSTGRES_*` читает Docker Compose. `db.*` читает само приложение при старте.

### 3. Запуск базы данных через Docker Compose

PostgreSQL поднимается отдельным сервисом `database` из `docker-compose.yaml`. Образ — `postgres:18.6`, контейнер — `editorial-postgres`. Порт хоста берётся из `POSTGRES_PORT` (по умолчанию **5433**) и пробрасывается на `5432` внутри контейнера, чтобы не пересекаться с локальным Postgres на 5432. Данные хранятся в volume `editorial-postgres-data`.

Запуск в фоне:

```bash
docker compose up -d
```

Compose подставляет переменные из `.env`, ждёт healthcheck (`pg_isready`) и поднимает базу. Проверить состояние:

```bash
docker compose ps
```

Контейнер `editorial-postgres` должен быть в статусе `Up (healthy)`. Логи:

```bash
docker compose logs -f database
```

Остановка:

```bash
docker compose down
```

Полная очистка данных (удаляет volume):

```bash
docker compose down -v
```

### 4. Сборка и запуск тестов

Тесты используют **Embedded PostgreSQL** и **Mockito**, поэтому выполняются автономно и не затирают данные рабочей базы:

```bash
mvn clean test
```

### 5. Запуск приложения

При старте **Flyway** применяет миграции из `src/main/resources/db/migration/`: создаёт таблицы `articles`, `roles`, `users`, `article_statuses` и заполняет справочники ролей и статусов.

Через Maven Exec Plugin:

```bash
mvn compile exec:java
```

Главный класс — `application.Application` (`src/application/Application.java`).

Через IntelliJ IDEA:

1. Откройте проект и дождитесь индексации Maven.
2. Откройте `src/application/Application.java`.
3. Запустите `Application.main()` (зелёная стрелка или `Shift + F10`).

Перед запуском база из шага 3 должна быть в статусе `healthy`.

---

## 🖥 Навигация по консольному интерфейсу

После запуска отобразится главное меню:

```text
-------------------------
1. Articles menu
2. Users menu
3. Get stats
4. Export to Excel
0. Exit
-------------------------
Enter command:
```

### 1. Меню статей (`Articles menu`)

- `1` — просмотр всех статей.
- `2` — добавление статьи (нужен существующий `author_id`).
- `3` — редактирование статьи (заголовок, содержимое, статус). Пустой ввод оставляет поле без изменений.
- `4` — удаление статьи по ID.
- `5` — поиск статьи по ID.
- `6` — фильтрация по дате публикации (`YYYY-MM-DD`) или статусу.
- `7` — сортировка по ID, заголовку или дате публикации (по возрастанию или убыванию).
- `8` — поиск по ключевым словам.
- `0` — назад в главное меню.

Статусы статьи: `PENDING`, `MODERATING`, `REJECTED`, `PUBLISHED`.

### 2. Меню пользователей (`Users menu`)

- `1` — регистрация пользователя (логин, email, хеш пароля, роль: `ADMIN`, `EDITOR`, `AUTHOR`).
- `2` — редактирование пользователя по ID. Пустой ввод оставляет поле без изменений.
- `3` — удаление пользователя по ID.
- `4` — поиск пользователя по ID.
- `5` — список всех пользователей.
- `0` — назад в главное меню.

### 3. Статистика (`Get stats`)

Выводит сводку редакции:

- число пользователей;
- число статей;
- статьи в статусах `PENDING`, `MODERATING`, `PUBLISHED` и `REJECTED`.

### 4. Экспорт в Excel (`Export to Excel`)

- `1` — выгрузка статей;
- `2` — выгрузка пользователей;
- `0` — назад в главное меню.

Если нажать **Enter**, файл сохранится в корень проекта как `articles.xlsx` или `users.xlsx`. Можно указать относительный или абсолютный путь (`exports/2026/report.xlsx` или `C:\Users\...\Desktop\data.xlsx`) — недостающие каталоги будут созданы.
