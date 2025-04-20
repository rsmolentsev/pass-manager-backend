# Автор

Смоленцев Роман Юрьевич, гр. 5130901/10203.

# Password Manager Backend

Безопасный бэкенд-сервис для менеджера паролей, разработанный на Kotlin, Ktor и PostgreSQL.

## Возможности

- Аутентификация пользователей с использованием JWT токенов
- Безопасное хранение паролей с шифрованием
- Управление настройками пользователя
- Настраиваемое время автоматического выхода
- Поддержка Docker для простого развертывания

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

## API Endpoints

### Аутентификация

- `POST /register` - Регистрация нового пользователя
  ```json
  {
    "username": "string",
    "masterPassword": "string"
  }
  ```

- `POST /login` - Вход в систему и получение JWT токена
  ```json
  {
    "username": "string",
    "masterPassword": "string"
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
    "notes": "string"
  }
  ```
- `PUT /passwords/{id}` - Обновление пароля (требуется аутентификация)
  ```json
  {
    "resourceName": "string",
    "username": "string",
    "password": "string",
    "notes": "string"
  }
  ```
- `DELETE /passwords/{id}` - Удаление пароля (требуется аутентификация)

### Настройки пользователя

- `GET /settings` - Получение настроек пользователя (требуется аутентификация)
- `PUT /settings` - Обновление настроек пользователя (требуется аутентификация)
  ```json
  {
    "autoLogoutMinutes": number
  }
  ```

## Примеры запросов

### Регистрация пользователя
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","masterPassword":"password123"}'
```

### Вход в систему
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### Добавление пароля
```bash
curl -X POST http://localhost:8080/passwords \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"resourceName":"example.com","username":"user@example.com","password":"secret123","notes":"Personal account"}'
```

### Получение списка паролей
```bash
curl -X GET http://localhost:8080/passwords \
  -H "Authorization: Bearer <your-token>"
```

### Получение пароля по ID
```bash
curl -X GET http://localhost:8080/passwords/1 \
  -H "Authorization: Bearer <your-token>"
```

### Обновление пароля
```bash
curl -X PUT http://localhost:8080/passwords/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"resourceName":"example.com","username":"user@example.com","password":"newsecret123","notes":"Updated account"}'
```

### Удаление пароля
```bash
curl -X DELETE http://localhost:8080/passwords/1 \
  -H "Authorization: Bearer <your-token>"
```

### Изменение мастер-пароля
```bash
curl -X PUT http://localhost:8080/change-master-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"oldMasterPassword":"oldpass123","newMasterPassword":"newpass123"}'
```

### Обновление настроек
```bash
curl -X PUT http://localhost:8080/settings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"autoLogoutMinutes":60}'
```

## Безопасность

- Все пароли хешируются с использованием BCrypt
- Хранение паролей в базе данных зашифровано с использованием AES
- JWT токены используются для аутентификации
- CORS настроен для безопасного взаимодействия с клиентским приложением
- Секретные данные хранятся в переменных окружения

### Данные

- Данные PostgreSQL сохраняются в именованном томе `postgres_data`
- Конфигурация приложения монтируется из локального файла

## Конфигурация

Приложение можно настроить через переменные окружения или `application.conf`:

### Настройки базы данных
- `DB_URL` - URL подключения к базе данных
- `DB_USER` - Имя пользователя базы данных
- `DB_PASSWORD` - Пароль базы данных

### Настройки JWT
- `JWT_SECRET` - Секретный ключ для подписи JWT токенов
- `JWT_LIFETIME_MINUTES` - Время жизни токена в минутах

## Функции безопасности

- Шифрование паролей с использованием безопасного хеширования
- Аутентификация на основе JWT
- Настраиваемое время жизни токена
- Функция автоматического выхода
- Безопасные подключения к базе данных

## Разработка

### Сборка проекта

```bash
./gradlew build
```

### Запуск тестов

```bash
./gradlew test
```

### Локальный запуск

1. Запустите PostgreSQL
2. Настройте переменные окружения
3. Запустите приложение:
   ```bash
   ./gradlew run
   ```

## Структура проекта

```
src/main/kotlin/com/passmanager/
├── Application.kt          # Точка входа в приложение
├── database/
│   ├── Database.kt        # Конфигурация базы данных
│   └── Tables.kt          # Определения таблиц базы данных
├── models/
│   └── Models.kt          # Модели данных
├── plugins/
│   ├── Authentication.kt  # Плагин аутентификации
│   ├── Routing.kt         # Определения маршрутов
│   └── Serialization.kt   # JSON сериализация
└── utils/
    └── DatabaseFactory.kt # Фабрика подключений к базе данных
```

## Установка и запуск

### Локальный запуск

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

### Запуск через Docker

1. Клонируйте репозиторий:
```bash
git clone <repository-url>
cd pass-manager-backend
```

2. Создайте файл `.env` и настройте переменные окружения:
```bash
cp example.env .env
# Отредактируйте .env файл, указав свои значения
```

3. Запустите приложение:
```bash
docker-compose up -d
```

4. Приложение будет доступно по адресу: `http://localhost:8080`

## Swagger UI

Документация API доступна по адресу: `http://localhost:8080/swagger`

## Тестирование API

### Регистрация пользователя
```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","masterPassword":"password123"}'
```

### Вход в систему
```bash
curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"password123"}'
```

### Добавление пароля
```bash
curl -X POST http://localhost:8080/passwords \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"resourceName":"example.com","username":"user@example.com","password":"secret123","notes":"Personal account"}'
```

### Получение списка паролей
```bash
curl -X GET http://localhost:8080/passwords \
  -H "Authorization: Bearer <your-token>"
```

### Обновление настроек
```bash
curl -X PUT http://localhost:8080/settings \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"autoLogoutMinutes":60}'
```

### Изменение мастер-пароля
```bash
curl -X PUT http://localhost:8080/change-master-password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <your-token>" \
  -d '{"oldMasterPassword":"oldpass123","newMasterPassword":"newpass123"}'
```

### Получение пароля по ID
```bash
curl -X GET http://localhost:8080/passwords/1 \
  -H "Authorization: Bearer <your-token>"
```

### Удаление пароля
```bash
curl -X DELETE http://localhost:8080/passwords/1 \
  -H "Authorization: Bearer <your-token>"
```