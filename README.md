# Автор

Смоленцев Роман Юрьевич, гр. 5130901/10203.

# Password Manager Backend

Безопасный бэкенд-сервис для менеджера паролей, разработанный на Kotlin, Ktor и PostgreSQL.

## Возможности

- Аутентификация пользователей с использованием JWT токенов
- Безопасное хранение паролей с шифрованием AES
- Управление настройками пользователя
- Настраиваемое время автоматического выхода
- Поддержка Docker для простого развертывания
- Swagger UI для документации API
- CORS поддержка для безопасного взаимодействия с клиентским приложением

## Технологии

- Kotlin 1.9.22
- Ktor 2.3.8
- PostgreSQL 15
- Exposed ORM 0.45.0
- JWT Auth 4.4.0
- BCrypt для хеширования паролей
- HikariCP для пула соединений
- Swagger UI для документации API

## Требования

- Java 17 или выше
- Docker и Docker Compose
- PostgreSQL 15 (при локальном запуске)

## Переменные окружения

Создайте файл `.env` в корневой директории со следующими переменными:

```env
# Настройки базы данных
DB_USER=postgres
DB_PASSWORD=postgres
DB_URL=jdbc:postgresql://localhost:5432/pass_manager

# Настройки JWT
JWT_SECRET=your-secret-key
JWT_LIFETIME_MINUTES=60
```

## Запуск с использованием Docker Compose

1. Соберите и запустите сервисы:
   ```bash
   docker-compose up --build
   ```

2. Приложение будет доступно по адресу: `http://localhost:8080`
3. База данных будет доступна по адресу: `localhost:5432`
4. Swagger UI будет доступен по адресу: `http://localhost:8080/swagger`

## Локальный запуск

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd pass-manager-backend
```

2. Создайте базу данных PostgreSQL:
```sql
CREATE DATABASE pass_manager;
```

3. Настройте конфигурацию в файле `src/main/resources/application.conf`:
- Укажите правильные параметры подключения к базе данных
- Измените секретный ключ JWT на свой

4. Соберите и запустите приложение:
```bash
./gradlew build
./gradlew run
```

## API Endpoints

### Аутентификация

- `POST /register` - Регистрация нового пользователя
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```

- `POST /login` - Вход в систему и получение JWT токена
  ```json
  {
    "username": "string",
    "password": "string"
  }
  ```

- `PUT /change-master-password` - Изменение мастер-пароля (требуется аутентификация)
  ```json
  {
    "oldMasterPassword": "string",
    "newMasterPassword": "string"
  }
  ```

### Управление паролями

- `GET /passwords` - Получение всех паролей (требуется аутентификация)
- `GET /passwords/{id}` - Получение пароля по ID (требуется аутентификация)
- `POST /passwords` - Добавление нового пароля (требуется аутентификация)
  ```json
  {
    "resourceName": "string",
    "username": "string",
    "password": "string",
    "notes": "string",
    "masterPassword": "string"
  }
  ```
- `PUT /passwords/{id}` - Обновление пароля (требуется аутентификация)
  ```json
  {
    "resourceName": "string",
    "username": "string",
    "password": "string",
    "notes": "string",
    "masterPassword": "string"
  }
  ```
- `DELETE /passwords/{id}` - Удаление пароля (требуется аутентификация)
- `POST /passwords/{id}/decrypt` - Дешифрование пароля
  ```json
  {
    "masterPassword": "string"
  }
  ```

### Настройки пользователя

- `GET /settings` - Получение настроек пользователя (требуется аутентификация)
- `PUT /settings` - Обновление настроек пользователя (требуется аутентификация)
  ```json
  {
    "autoLogoutMinutes": number
  }
  ```

## Функции безопасности

- Шифрование паролей с использованием безопасного хеширования
- Аутентификация на основе JWT
- Настраиваемое время жизни токена
- Функция автоматического выхода
- Безопасные подключения к базе данных

## Структура проекта

```
src/main/kotlin/com/passmanager/
├── Application.kt          # Точка входа в приложение, содержит маршрутизацию и обработку запросов
├── config/
│   └── SwaggerConfig.kt   # Конфигурация Swagger UI
├── database/
│   └── Tables.kt          # Определения таблиц базы данных
├── exceptions/
│   └── Exceptions.kt      # Пользовательские исключения
├── models/
│   └── Models.kt          # Модели данных
└── utils/
    ├── DatabaseFactory.kt # Фабрика подключений к базе данных
    ├── SecurityUtils.kt   # Утилиты для работы с безопасностью
    └── InstantSerializer.kt # Сериализатор для работы с датами

src/main/resources/
├── application.conf       # Конфигурация приложения
└── openapi/
    └── documentation.yaml # OpenAPI спецификация
```

## Документация API

Полная документация API доступна через Swagger UI по адресу `http://localhost:8080/swagger`. Документация включает:
- Описание всех доступных эндпоинтов
- Схемы запросов и ответов
- Примеры использования
- Требования аутентификации

